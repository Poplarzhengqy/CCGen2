/**
 * Copyright (c) 1998-2012 Novell, Inc. and its licensors. All rights reserved.
 * 
 * This work is governed by the Common Public Attribution License Version 1.0 (the
 * "CPAL"); you may not use this file except in compliance with the CPAL. You may
 * obtain a copy of the CPAL at http://www.opensource.org/licenses/cpal_1.0. The
 * CPAL is based on the Mozilla Public License Version 1.1 but Sections 14 and 15
 * have been added to cover use of software over a computer network and provide
 * for limited attribution for the Original Developer. In addition, Exhibit A has
 * been modified to be consistent with Exhibit B.
 * 
 * Software distributed under the CPAL is distributed on an "AS IS" basis, WITHOUT
 * WARRANTY OF ANY KIND, either express or implied. See the CPAL for the specific
 * language governing rights and limitations under the CPAL.
 * 
 * The Original Code is ICEcore, now called Kablink. The Original Developer is
 * Novell, Inc. All portions of the code written by Novell, Inc. are Copyright
 * (c) 1998-2012 Novell, Inc. All Rights Reserved.
 * 
 * Attribution Information:
 * Attribution Copyright Notice: Copyright (c) 1998-2012 Novell, Inc. All Rights Reserved.
 * Attribution Phrase (not exceeding 10 words): [Powered by Kablink]
 * Attribution URL: [www.kablink.org]
 * Graphic Image as provided in the Covered Code
 * [ssf/images/pics/powered_by_icecore.png].
 * Display of Attribution Information is required in Larger Works which are
 * defined in the CPAL as a work which combines Covered Code or portions thereof
 * with code not governed by the terms of the CPAL.
 * 
 * NOVELL and the Novell logo are registered trademarks and Kablink and the
 * Kablink logos are trademarks of Novell, Inc.
 */
package org.kablink.teaming.module.binder.impl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.dom4j.Element;
import org.kablink.teaming.ConfigurationException;
import org.kablink.teaming.NotSupportedException;
import org.kablink.teaming.ObjectKeys;
import org.kablink.teaming.context.request.RequestContextHolder;
import org.kablink.teaming.dao.util.SFQuery;
import org.kablink.teaming.domain.Attachment;
import org.kablink.teaming.domain.Binder;
import org.kablink.teaming.domain.ChangeLog;
import org.kablink.teaming.domain.DefinableEntity;
import org.kablink.teaming.domain.Definition;
import org.kablink.teaming.domain.Description;
import org.kablink.teaming.domain.EntityIdentifier;
import org.kablink.teaming.domain.Entry;
import org.kablink.teaming.domain.Event;
import org.kablink.teaming.domain.FileAttachment;
import org.kablink.teaming.domain.Folder;
import org.kablink.teaming.domain.FolderEntry;
import org.kablink.teaming.domain.HistoryStamp;
import org.kablink.teaming.domain.Principal;
import org.kablink.teaming.domain.TitleException;
import org.kablink.teaming.domain.User;
import org.kablink.teaming.domain.UserPrincipal;
import org.kablink.teaming.domain.VersionAttachment;
import org.kablink.teaming.domain.WorkflowResponse;
import org.kablink.teaming.domain.WorkflowState;
import org.kablink.teaming.domain.WorkflowSupport;
import org.kablink.teaming.domain.AuditTrail.AuditType;
import org.kablink.teaming.lucene.Hits;
import org.kablink.teaming.module.binder.impl.EntryDataErrors.Problem;
import org.kablink.teaming.module.binder.processor.EntryProcessor;
import org.kablink.teaming.module.definition.DefinitionUtils;
import org.kablink.teaming.module.file.FilesErrors;
import org.kablink.teaming.module.file.FilterException;
import org.kablink.teaming.module.file.WriteFilesException;
import org.kablink.teaming.module.shared.AccessUtils;
import org.kablink.teaming.module.shared.ChangeLogUtils;
import org.kablink.teaming.module.shared.EntityIndexUtils;
import org.kablink.teaming.module.shared.EntryBuilder;
import org.kablink.teaming.module.shared.InputDataAccessor;
import org.kablink.teaming.module.shared.SearchUtils;
import org.kablink.teaming.module.workflow.WorkflowProcessUtils;
import org.kablink.teaming.module.workflow.WorkflowUtils;
import org.kablink.teaming.search.BasicIndexUtils;
import org.kablink.teaming.search.IndexErrors;
import org.kablink.teaming.search.IndexSynchronizationManager;
import org.kablink.teaming.search.LuceneReadSession;
import org.kablink.teaming.search.QueryBuilder;
import org.kablink.teaming.search.SearchObject;
import org.kablink.teaming.search.filter.SearchFilter;
import org.kablink.teaming.security.acl.AclContainer;
import org.kablink.teaming.security.acl.AclControlled;
import org.kablink.teaming.task.TaskHelper;
import org.kablink.teaming.util.FileUploadItem;
import org.kablink.teaming.util.SPropsUtil;
import org.kablink.teaming.util.SimpleProfiler;
import org.kablink.teaming.web.util.BinderHelper;
import org.kablink.teaming.web.util.ListFolderHelper;
import org.kablink.teaming.web.util.MarkupUtil;
import org.kablink.util.Validator;
import org.kablink.util.search.Constants;
import org.kablink.util.search.FieldFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.web.multipart.MultipartFile;

/**
 * Add entries to the binder.
 * 
 * @author Jong Kim
 */
@SuppressWarnings("unchecked")
public abstract class AbstractEntryProcessor extends AbstractBinderProcessor 
	implements EntryProcessor {
    
	private static final int DEFAULT_MAX_CHILD_ENTRIES = ObjectKeys.LISTING_MAX_PAGE_SIZE;
	//***********************************************************************************************************	
    
	@Override
	public Entry addEntry(final Binder binder, Definition def, Class clazz, 
    		final InputDataAccessor inputData, Map fileItems, Map options) 
    	throws WriteFilesException, WriteEntryDataException, WriteEntryDataException {
        // This default implementation is coded after template pattern. 
        SimpleProfiler.start("addEntry");
		
        final Map ctx = new HashMap();
        if (options != null) ctx.putAll(options);
        addEntry_setCtx(binder, ctx);
    	Map entryDataAll;
    	EntryDataErrors entryDataErrors = new EntryDataErrors();
    	
    	SimpleProfiler.start("addEntry_toEntryData");
        entryDataAll = addEntry_toEntryData(binder, def, inputData, fileItems, ctx);
        SimpleProfiler.stop("addEntry_toEntryData");
        
        final Map entryData = (Map) entryDataAll.get(ObjectKeys.DEFINITION_ENTRY_DATA);
        List fileUploadItems = (List) entryDataAll.get(ObjectKeys.DEFINITION_FILE_DATA);
        entryDataErrors = (EntryDataErrors) entryDataAll.get(ObjectKeys.DEFINITION_ERRORS);
        if (entryDataErrors.getProblems().size() > 0) {
        	//An error occurred processing the entry Data
        	throw new WriteEntryDataException(entryDataErrors);
        }
        Entry newEntry = null;
        try {
        	
        	SimpleProfiler.start("addEntry_create");
        	final Entry entry = addEntry_create(def, clazz, ctx);
        	newEntry = entry;
        	SimpleProfiler.stop("addEntry_create");
        
        	SimpleProfiler.start("addEntry_transactionExecute");
        	// 	The following part requires update database transaction.
        	getTransactionTemplate().execute(new TransactionCallback() {
        		@Override
				public Object doInTransaction(TransactionStatus status) {
        			//need to set entry/binder information before generating file attachments
        			//Attachments/Events need binder info for AnyOwner
                	SimpleProfiler.start("addEntry_fillIn");
        			addEntry_fillIn(binder, entry, inputData, entryData, ctx);
                	SimpleProfiler.stop("addEntry_fillIn");
                	SimpleProfiler.start("addEntry_preSave");
        			addEntry_preSave(binder, entry, inputData, entryData, ctx);
                	SimpleProfiler.stop("addEntry_preSave");
                	SimpleProfiler.start("addEntry_save");
        			addEntry_save(binder, entry, inputData, entryData,ctx);
        			SimpleProfiler.stop("addEntry_save");
                	SimpleProfiler.start("addEntry_postSave");
         			addEntry_postSave(binder, entry, inputData, entryData, ctx);
                	SimpleProfiler.stop("addEntry_postSave");
       			return null;
        		}
        	});
        	SimpleProfiler.stop("addEntry_transactionExecute");
        	
           	// We must save the entry before processing files because it makes use
        	// of the persistent id of the entry. 
            SimpleProfiler.start("addEntry_filterFiles");
        	FilesErrors filesErrors = addEntry_filterFiles(binder, entry, entryData, fileUploadItems, ctx);
        	SimpleProfiler.stop("addEntry_filterFiles");

        	SimpleProfiler.start("addEntry_processFiles");
        	// We must save the entry before processing files because it makes use
        	// of the persistent id of the entry. 
        	filesErrors = addEntry_processFiles(binder, entry, fileUploadItems, filesErrors, ctx);
        	
        	getTransactionTemplate().execute(new TransactionCallback() {
        		@Override
				public Object doInTransaction(TransactionStatus status) {
		        	//See if there were any file notes added
		        	Set<Attachment> atts = entry.getAttachments();
		        	for (Attachment att : atts) {
		        		if (att instanceof FileAttachment) {
		        			FileAttachment fa = (FileAttachment)att;
		        			String itemName = fa.getName();
		        			if (itemName != null) {
		        				String fileNote = inputData.getSingleValue(itemName + fa.getId().toString() + ".description");
		        				if (fileNote != null) {
		        					fa.getFileItem().setDescription(fileNote);
		        					VersionAttachment hVer = fa.getHighestVersion();
		        					if (hVer != null && hVer.getParentAttachment() == fa) {
		        						hVer.getFileItem().setDescription(fileNote);
		        					}
		        				}
		        			}
		        		}
		        	}
		        	return null;
        		}
        	});
        	SimpleProfiler.stop("addEntry_processFiles");
        
        	// 	The following part requires update database transaction.
        	getTransactionTemplate().execute(new TransactionCallback() {
        		@Override
				public Object doInTransaction(TransactionStatus status) {
                    	//After the entry is successfully added, start up any associated workflows
        			SimpleProfiler.start("addEntry_startWorkflow");
                	addEntry_startWorkflow(entry, ctx);
                	SimpleProfiler.stop("addEntry_startWorkflow");
       			return null;
        		}
        	});
 
        	SimpleProfiler.start("addEntry_indexAdd");
        	// This must be done in a separate step after persisting the entry,
        	// because we need the entry's persistent ID for indexing. 
        	
        	addEntry_indexAdd(binder, entry, inputData, fileUploadItems, ctx);
        	SimpleProfiler.stop("addEntry_indexAdd");
        	
        	SimpleProfiler.start("addEntry_done");
        	addEntry_done(binder, entry, inputData, ctx);
        	SimpleProfiler.stop("addEntry_done");
        	
         	if(filesErrors.getProblems().size() > 0) {
        		// At least one error occured during the operation. 
        		throw new WriteFilesException(filesErrors, entry.getId());
        	}
        	else {
        		return entry;
        	}
        } catch(WriteFilesException ex) {
        	//See if there was an entry created. If so, delete it.
        	if (ex.getEntityId() != null && newEntry != null && ex.getEntityId().equals(newEntry.getId())) {
        		deleteEntry(binder, newEntry, false, new HashMap());
        		ex.setEntityId(null);
        	}
        	throw ex;
        } catch (DataIntegrityViolationException e) {
            if (newEntry != null && newEntry.getId()!=null) {
           		deleteEntry(binder, newEntry, false, new HashMap());
           	}
            throw new DataIntegrityViolationException(e.getLocalizedMessage(), e);
        } catch(Exception ex) {
        	entryDataErrors.addProblem(new Problem(Problem.GENERAL_PROBLEM, ex));
        	if (newEntry != null && newEntry.getId()!=null) {
        		deleteEntry(binder, newEntry, false, new HashMap());
        	}
        	throw new WriteEntryDataException(entryDataErrors);
        } finally {
           	cleanupFiles(fileUploadItems);       	
            SimpleProfiler.stop("addEntry");
        }
    }

	/*
	@Override
	public List<FolderEntry> _addNetFolderEntries(final Folder folder, Definition def, 
			final List<InputDataAccessor> inputDataList, List<Map> fileItemsList, List<Map> optionsList) 
    	throws WriteFilesException, WriteEntryDataException, WriteEntryDataException {
		// OLD ONE $$$$$ TBR
		ArrayList<FolderEntry> result = new ArrayList<FolderEntry>(inputDataList.size());
		for(int i = 0; i < inputDataList.size(); i++) {
			result.set(i, (FolderEntry) this.addEntry(folder, def, FolderEntry.class, inputDataList.get(i), fileItemsList.get(i), optionsList.get(i)));
		}
		return result;
	}
	*/

    protected void addEntry_setCtx(Binder binder, Map ctx) {
    }

    @Override
	public List<FolderEntry> _addNetFolderEntriesInSync(final Folder folder, final Definition def, 
			final List<InputDataAccessor> inputDataList, final List<Map> fileItemsList, final List<Map> optionsList) 
    	throws WriteFilesException, WriteEntryDataException, WriteEntryDataException {
		final ArrayList<FolderEntry> result = new ArrayList<FolderEntry>(inputDataList.size());
				
		// Start a transaction
    	getTransactionTemplate().execute(new TransactionCallback() {
    		@Override
			public Object doInTransaction(TransactionStatus status) {
    			InputDataAccessor inputData;
    			Map fileItems;
    			Map options;
    			String title;
    			FileUploadItem fui;
    			FolderEntry entry;
    			FileAttachment fAtt;
	            getCoreDao().lock(folder);
    			for(int i = 0; i < inputDataList.size(); i++) {
    				inputData = inputDataList.get(i);
    				fileItems = fileItemsList.get(i);
    				options = optionsList.get(i);
    				title = inputDataList.get(i).getSingleValue("title");
    				fui = new FileUploadItem(FileUploadItem.TYPE_TITLE, (String) fileItems.keySet().iterator().next(), (MultipartFile) fileItems.values().iterator().next(), ObjectKeys.FI_ADAPTER);
    				fui.setSynchToRepository(false);
    				
    				
    				
    				entry = new FolderEntry();
    				entry.setEntryDef(def);
    	    		entry.setDefinitionType(new Integer(def.getType()));

    	    		
    	            folder.addEntry((FolderEntry)entry);
    	            
    	            User user;
    				Calendar cdate = (Calendar)options.get(ObjectKeys.INPUT_OPTION_CREATION_DATE);
    				Long cid = (Long)options.get(ObjectKeys.INPUT_OPTION_CREATION_ID);
    				String cname = (String)options.get(ObjectKeys.INPUT_OPTION_CREATION_NAME);
    				processCreationTimestamp(entry, cdate, cid, cname);

    	            
    				
    				Calendar mdate = (Calendar)options.get(ObjectKeys.INPUT_OPTION_MODIFICATION_DATE);
    				Long mid = (Long)options.get(ObjectKeys.INPUT_OPTION_MODIFICATION_ID);
    				String mname = (String)options.get(ObjectKeys.INPUT_OPTION_MODIFICATION_NAME);
    				processModificationTimestamp(entry, mdate, mid, mname);

    	            
    			    entry.setParentBinder(folder);
    			    entry.setLogVersion(Long.valueOf(1));

    	            
    		        //initialize collections, or else hibernate treats any new 
    		        //empty collections as a change and attempts a version update which
    		        //may happen outside the transaction
    		        entry.getAttachments();
    		        entry.getEvents();
    		        entry.getCustomAttributes();
    		        
    		        entry.setTitle(title);
    		        entry.updateLastActivity(entry.getModification().getDate());

    		        // Do we really need this?
    		        /* 
    	    		Statistics statistics = getFolderStatistics(folder);
    		    	statistics.addStatistics(entry.getEntryDefId(), entry.getEntryDefDoc(), entry.getCustomAttributes());
    		    	setFolderStatistics(folder, statistics);
    		    	*/

    		        getCoreDao().save(entry);

    		        
    				if (folder.isUniqueTitles()) 
    					getCoreDao().updateTitle(folder, entry, null, entry.getNormalTitle());


    				processChangeLog(entry, ChangeLog.ADDENTRY);
    		    	getReportModule().addAuditTrail(AuditType.add, entry);
    		    	
    		    	
    		    	
	    			getCoreDao().registerFileName(folder, entry, fui.getOriginalFilename());
	    			fui.setRegistered(true);


	    			// Create file metadata
	    			fAtt = getFileModule()._addNetFolderFileInSync(folder, entry, fui);

	    			// Since we are not indexing file content (which also implies that we are not reading
	    			// file content) and the entry is initially tiny (it only has a title), we can afford
	    			// performing indexing within the database transaction.
	    			
	    			// Index the entry
	    	        IndexSynchronizationManager.addDocument(buildIndexDocumentFromEntry(folder, entry, Collections.EMPTY_LIST));
	    	        // Index the file
	    			IndexSynchronizationManager.addDocument(buildIndexDocumentFromEntryFile(folder, entry, fAtt, fui, Collections.EMPTY_LIST));

	    			result.set(i, entry);
    			}    			
   			return null;
    		}
    	}); // End the transaction
				
    	
    	// Do this outside of database transaction
    	
		return result;
	}

    
    private void checkInputFilesForNonMirroredBinder(List fileUploadItems, FilesErrors errors) {
		for (int i = 0; i < fileUploadItems.size();) {
			FileUploadItem fui = (FileUploadItem) fileUploadItems.get(i);
			if (fui.getRepositoryName().equals(ObjectKeys.FI_ADAPTER)) {
				fileUploadItems.remove(i);
				errors.addProblem(new FilesErrors.Problem(
								fui.getRepositoryName(),
								fui.getOriginalFilename(),
								FilesErrors.Problem.PROBLEM_MIRRORED_FILE_IN_REGULAR_FOLDER));
			} else {
				i++;
			}
		}
	}
    
    private void checkInputFilesForMirroredBinder(Binder binder, List fileUploadItems, FilesErrors errors) {
		String mirroredFileName = null;
		if(binder.getResourceDriverName() == null)
			throw new ConfigurationException("Resource driver must be specified for mirrored folder");
		boolean readonly = getResourceDriverManager().isReadonly(binder.getResourceDriverName());
		for(int i = 0; i < fileUploadItems.size();) {
			FileUploadItem fui = (FileUploadItem) fileUploadItems.get(i);
			if(fui.getRepositoryName().equals(ObjectKeys.FI_ADAPTER)) {
				if(fui.isSynchToRepository() && readonly) {
   					fileUploadItems.remove(i);
    				errors.addProblem(new FilesErrors.Problem
							(fui.getRepositoryName(), fui.getOriginalFilename(), 
									FilesErrors.Problem.PROBLEM_MIRRORED_FILE_READONLY_DRIVER));					
				}
				else {
					if(mirroredFileName == null) {
						mirroredFileName = fui.getOriginalFilename();
						i++;
					}
					else {
						if(mirroredFileName.equals(fui.getOriginalFilename())) {
							// This is a very questionable use case. However, since 
							// the expected post-condition is still stable, we will
							// let it continue.
							i++;
						}
						else {
		   					fileUploadItems.remove(i);
		    				errors.addProblem(new FilesErrors.Problem
									(fui.getRepositoryName(), fui.getOriginalFilename(), 
											FilesErrors.Problem.PROBLEM_MIRRORED_FILE_MULTIPLE));
						}
					}
				}
			}
			else {
				fileUploadItems.remove(i);
				errors.addProblem(new FilesErrors.Problem(
								fui.getRepositoryName(),
								fui.getOriginalFilename(),
								FilesErrors.Problem.PROBLEM_REGULAR_FILE_IN_MIRRORED_FOLDER));			
			}
		}
    }
    
    protected FilesErrors addEntry_filterFiles(Binder binder, Entry entry, 
    		Map entryData, List fileUploadItems, Map ctx) throws FilterException {
   		FilesErrors nameErrors = new FilesErrors();
   		
   		checkInputFileNames(fileUploadItems, nameErrors);
    			 
   		if(!binder.isMirrored()) {
   			checkInputFilesForNonMirroredBinder(fileUploadItems, nameErrors);
   		}
   		else {
   			checkInputFilesForMirroredBinder(binder, fileUploadItems, nameErrors);
   		}

        FilesErrors checkSumErrors = getFileModule().verifyCheckSums(fileUploadItems);
   		
   		if (binder.isLibrary()) {
    		// 	Make sure the file name is unique if requested		
    		for (int i=0; i<fileUploadItems.size(); ) {
    			FileUploadItem fui = (FileUploadItem)fileUploadItems.get(i);
    			try {
    				getCoreDao().registerFileName(binder, entry, fui.getOriginalFilename());
    				fui.setRegistered(true);
    				++i;
    			} catch (TitleException te) {
    				fileUploadItems.remove(i);
    				nameErrors.addProblem(new FilesErrors.Problem(null, 
       	   					fui.getOriginalFilename(), FilesErrors.Problem.PROBLEM_FILE_EXISTS, te));
    			}
    		}
   		}
   		FilesErrors filterErrors = getFileModule().filterFiles(binder, entry, fileUploadItems);
    	filterErrors.getProblems().addAll(nameErrors.getProblems());
    	filterErrors.getProblems().addAll(checkSumErrors.getProblems());
    	return filterErrors;
    }

    protected FilesErrors addEntry_processFiles(Binder binder, 
    		Entry entry, List fileUploadItems, FilesErrors filesErrors, Map ctx) {
    	return getFileModule().writeFiles(binder, entry, fileUploadItems, filesErrors);
    }
    
    protected Map addEntry_toEntryData(Binder binder, Definition def, InputDataAccessor inputData, Map fileItems, Map ctx) {
        //Call the definition processor to get the entry data to be stored
        if (def != null) {
        	return getDefinitionModule().getEntryData(def.getDefinition(), inputData, fileItems);
        } else {
        	//handle basic fields only without definition
        	Map entryDataAll = new HashMap();
	        Map entryData = new HashMap();
			List fileData = new ArrayList();
			EntryDataErrors entryDataErrors = new EntryDataErrors();
			entryDataAll.put(ObjectKeys.DEFINITION_ENTRY_DATA, entryData);
			entryDataAll.put(ObjectKeys.DEFINITION_FILE_DATA, fileData);
			entryDataAll.put(ObjectKeys.DEFINITION_ERRORS, entryDataErrors);
 			if (inputData.exists(ObjectKeys.FIELD_ENTITY_TITLE)) entryData.put(ObjectKeys.FIELD_ENTITY_TITLE, inputData.getSingleValue("title"));
			if (inputData.exists(ObjectKeys.FIELD_ENTITY_DESCRIPTION)) {
				Description description = new Description();
				description.setText(inputData.getSingleValue(ObjectKeys.FIELD_ENTITY_DESCRIPTION));
				MarkupUtil.scanDescriptionForUploadFiles(description, ObjectKeys.FIELD_ENTITY_DESCRIPTION, fileData);
				MarkupUtil.scanDescriptionForAttachmentFileUrls(description);
				MarkupUtil.scanDescriptionForICLinks(description);
				MarkupUtil.scanDescriptionForYouTubeLinks(description);
				if (!inputData.exists(ObjectKeys.FIELD_ENTITY_DESCRIPTION_FORMAT)) {
					description.setFormat(Description.FORMAT_HTML);
				} else {
					String format = inputData.getSingleValue(ObjectKeys.FIELD_ENTITY_DESCRIPTION_FORMAT);
					if (!format.equals("")) {
						description.setFormat(Integer.valueOf(format));
					} else {
						description.setFormat(Description.FORMAT_HTML);
					}
				}
				entryData.put(ObjectKeys.FIELD_ENTITY_DESCRIPTION, description);
			}
      	
        	return entryDataAll;
        }
    }
    
    protected Entry addEntry_create(Definition def, Class clazz, Map ctx)  {
    	try {
    		Constructor<Entry> c = clazz.getDeclaredConstructor();
    		c.setAccessible(true);
    		Entry entry = (Entry) c.newInstance();
           	entry.setEntryDef(def);
        	if (def != null) {
        		entry.setDefinitionType(new Integer(def.getType()));
  	        	String icon = DefinitionUtils.getPropertyValue(def.getDefinition().getRootElement(), "icon");
   	        	if (Validator.isNotNull(icon)) entry.setIconName(icon);
         	}
        	return entry;
    	} catch (Exception ex) {
    		return null;
    	}
    }
    
    protected void addEntry_fillIn(Binder binder, Entry entry, InputDataAccessor inputData, Map entryData, Map ctx) {  
        processCreationTimestamp(entry, ctx);
        processModificationTimestamp(entry, entry.getCreation(), ctx);
        entry.setParentBinder(binder);
        entry.setLogVersion(Long.valueOf(1));
        
        //initialize collections, or else hibernate treats any new 
        //empty collections as a change and attempts a version update which
        //may happen outside the transaction
        entry.getAttachments();
        entry.getEvents();
        entry.getCustomAttributes();
        
        // The entry inherits acls from the parent by default, except workflow
        if ((entry instanceof AclControlled) && (binder instanceof AclContainer)) {
        	getAclManager().doInherit((AclContainer)binder, (AclControlled) entry);
        }
        for (Iterator iter=entryData.values().iterator(); iter.hasNext();) {
        	Object obj = iter.next();
        	//need to generate id for the event so its id can be saved in customAttr
        	if (obj instanceof Event) {
        		getCoreDao().save(obj);
        	}
        }
        
		TaskHelper.processTaskCompletion(entry, inputData, entryData);        
        EntryBuilder.buildEntry(entry, entryData);
        
    }

    //inside write transaction
    protected void addEntry_preSave(Binder binder, Entry entry, InputDataAccessor inputData, Map entryData, Map ctx) {
    }

    //inside write transaction
    protected void addEntry_save(Binder binder, Entry entry, InputDataAccessor inputData, Map entryData, Map ctx){
        getCoreDao().save(entry);
    }
    
    //inside write transaction
    protected void addEntry_postSave(Binder binder, Entry entry, InputDataAccessor inputData, Map entryData, Map ctx) {
    	//create history - using timestamp and version from fillIn
		if (binder.isUniqueTitles()) getCoreDao().updateTitle(binder, entry, null, entry.getNormalTitle());
    	
		//generate event uid
		Iterator<Event> it = entry.getEvents().iterator();
		while (it.hasNext()) {
			Event event = it.next();
			event.generateUid(entry);
		}
		
		processChangeLog(entry, ChangeLog.ADDENTRY);
    	getReportModule().addAuditTrail(AuditType.add, entry);
    }

    protected void addEntry_indexAdd(Binder binder, Entry entry, 
    		InputDataAccessor inputData, List fileUploadItems, Map ctx){
        //no tags typically exists on a new entry - reduce db lookups by supplying list
    	List tags = null;
  	   if (ctx != null && Boolean.TRUE.equals(ctx.get(ObjectKeys.INPUT_OPTION_NO_INDEX))) return;
    	if (ctx != null) tags = (List)ctx.get(ObjectKeys.INPUT_FIELD_TAGS);
    	if (tags == null) tags = new ArrayList();
    	indexEntry(binder, entry, fileUploadItems, null, true, tags);
    }
 
    protected void addEntry_done(Binder binder, Entry entry, InputDataAccessor inputData, Map ctx) {
    }
 
    //inside write transaction
    protected void addEntry_startWorkflow(Entry entry, Map ctx){
    	if (!(entry instanceof WorkflowSupport)) return;
    	if (ctx != null && Boolean.TRUE.equals(ctx.get(ObjectKeys.INPUT_OPTION_NO_WORKFLOW))) return;
    	Binder binder = entry.getParentBinder();
    	Map workflowAssociations = (Map) binder.getWorkflowAssociations();
    	if (workflowAssociations != null) {
    		//See if the entry definition type has an associated workflow
    		if (entry.getEntryDefId() != null) {
    			Definition wfDef = (Definition)workflowAssociations.get(entry.getEntryDefId());
    			if (wfDef != null)	{
    				getWorkflowModule().addEntryWorkflow((WorkflowSupport)entry, entry.getEntityIdentifier(), wfDef, null);
    				WorkflowSupport wEntry = (WorkflowSupport) entry;
    				Set<WorkflowState> stateList = wEntry.getWorkflowStates();
    				for (WorkflowState ws : stateList) {
	    				WorkflowProcessUtils.processChangeLog(ws.getState(), ChangeLog.STARTWORKFLOW, wEntry);
    				}
    			}
    		}
    	}
    }
 	
    //no transaction expected
    @Override
	public void modifyEntry(final Binder binder, final Entry entry, 
    		final InputDataAccessor inputData, Map fileItems, 
    		final Collection deleteAttachments, final Map<FileAttachment,String> fileRenamesTo, Map options)  
    		throws WriteFilesException, WriteEntryDataException {
    	if(options != null && Boolean.TRUE.equals(options.get(ObjectKeys.INPUT_OPTION_VALIDATION_ONLY))) {
    		modifyEntryValidationOnly(binder, entry, inputData, fileItems, deleteAttachments, fileRenamesTo, options);
    	}
    	else {
    		modifyEntryNormal(binder, entry, inputData, fileItems, deleteAttachments, fileRenamesTo, options);
    	}
    }
    
    //***********************************************************************************************************
    //no transaction expected
    protected void modifyEntryNormal(final Binder binder, final Entry entry, 
    		final InputDataAccessor inputData, Map fileItems, 
    		final Collection deleteAttachments, final Map<FileAttachment,String> fileRenamesTo, Map options)  
    		throws WriteFilesException, WriteEntryDataException {
       SimpleProfiler.start("modifyEntryNormal");

       final Map ctx = new HashMap();
       if (options != null) ctx.putAll(options);
       modifyEntry_setCtx(entry, ctx);

    	Map entryDataAll;
    	EntryDataErrors entryDataErrors = new EntryDataErrors();

    	SimpleProfiler.start("modifyEntry_toEntryData");
    	entryDataAll = modifyEntry_toEntryData(entry, inputData, fileItems, ctx);
	    SimpleProfiler.stop("modifyEntry_toEntryData");
    	
	    final Map entryData = (Map) entryDataAll.get(ObjectKeys.DEFINITION_ENTRY_DATA);
	    List fileUploadItems = (List) entryDataAll.get(ObjectKeys.DEFINITION_FILE_DATA);
	    entryDataErrors = (EntryDataErrors) entryDataAll.get(ObjectKeys.DEFINITION_ERRORS);
        if (entryDataErrors.getProblems().size() > 0) {
        	//An error occurred processing the entry Data
        	throw new WriteEntryDataException(entryDataErrors);
        }
        
	    try {	    	
	    	SimpleProfiler.start("modifyEntry_transactionExecute1");
	    	// The following part requires update database transaction.
	    	//ctx can be used by sub-classes to pass info
	    	getTransactionTemplate().execute(new TransactionCallback() {
	    		@Override
				public Object doInTransaction(TransactionStatus status) {
	    			SimpleProfiler.start("modifyEntry_fillIn");
	    			modifyEntry_fillIn(binder, entry, inputData, entryData, ctx);
	    			SimpleProfiler.stop("modifyEntry_fillIn");
	    			SimpleProfiler.start("modifyEntry_startWorkflow");
	    			modifyEntry_startWorkflow(entry, ctx);
	    			SimpleProfiler.stop("modifyEntry_startWorkflow");
	    			SimpleProfiler.start("modifyEntry_postFillIn");
	    			modifyEntry_postFillIn(binder, entry, inputData, entryData, fileRenamesTo, ctx);
	    			SimpleProfiler.stop("modifyEntry_postFillIn");
	    			return null;
	    		}});
	    	SimpleProfiler.stop("modifyEntry_transactionExecute1");
		    	
	    	//handle outside main transaction so main changeLog doesn't reflect attachment changes
	        SimpleProfiler.start("modifyBinder_removeAttachments");
	    	List<FileAttachment> filesToDeindex = new ArrayList<FileAttachment>();
	    	List<FileAttachment> filesToReindex = new ArrayList<FileAttachment>();	    
            modifyEntry_removeAttachments(binder, entry, deleteAttachments, filesToDeindex, filesToReindex, ctx);
	        SimpleProfiler.stop("modifyBinder_removeAttachments");
	    	
	    	SimpleProfiler.start("modifyEntry_filterFiles");
	    	FilesErrors filesErrors = modifyEntry_filterFiles(binder, entry, entryData, fileUploadItems, ctx);
	    	SimpleProfiler.stop("modifyEntry_filterFiles");

           	SimpleProfiler.start("modifyEntry_processFiles");
	    	filesErrors = modifyEntry_processFiles(binder, entry, fileUploadItems, filesErrors, ctx);
	    	SimpleProfiler.stop("modifyEntry_processFiles");
	    	
	    	SimpleProfiler.start("modifyEntry_transactionExecute2");
	    	getTransactionTemplate().execute(new TransactionCallback() {
	    		@Override
				public Object doInTransaction(TransactionStatus status) {
		        	//See if there were any file notes added
		        	Set<Attachment> atts = entry.getAttachments();
		        	for (Attachment att : atts) {
		        		if (att instanceof FileAttachment) {
		        			FileAttachment fa = (FileAttachment)att;
		        			String itemName = fa.getName();
		        			if (itemName != null) {
		        				String fileNote = inputData.getSingleValue(itemName + fa.getId().toString() + ".description");
		        				if (fileNote != null) {
		        					fa.getFileItem().setDescription(fileNote);
		        					VersionAttachment hVer = fa.getHighestVersion();
		        					if (hVer != null && hVer.getParentAttachment() == fa) {
		        						hVer.getFileItem().setDescription(fileNote);
		        					}
		        				}
		        			}
		        		}
		        	}
		        	return null;
	    		}
	    	});
	    	SimpleProfiler.stop("modifyEntry_transactionExecute2");

	    	// Since index update is implemented as removal followed by add, 
	    	// the update requests must be added to the removal and then add
	    	// requests respectively. 
	    	filesToDeindex.addAll(filesToReindex);
	    	SimpleProfiler.start("modifyEntry_indexRemoveFiles");
	    	modifyEntry_indexRemoveFiles(binder, entry, filesToDeindex, ctx);
	    	SimpleProfiler.stop("modifyEntry_indexRemoveFiles");

	    	// Can the entry be running a workflow?
	    	if (entry instanceof WorkflowSupport) {
	    		// Yes!  Could have a ACLs set in the workflow?
	    		WorkflowSupport wfs = ((WorkflowSupport) entry);
	    		if (wfs.hasAclSet()) {
	    			// Yes!  Then before indexing, we need to make sure
	    			// the workflow states don't have any member IDs
	    			// cached.
	    			//
	    			// Bugzilla 712550 (DRF):
	    			//    This is done so that when modifying an entry,
	    			//    the IDs associated with any workflow ACLs
	    			//    that may be modified by the entry
	    			//    modification are re-read BEFORE indexing.
	    			//    Without this, if ACL checks had been made
	    			//    prior to the modify, the modify would use
	    			//    the previous IDs, not those that may have
	    			//    been set as part of the modify.
	    			wfs.clearStateMembersCache();
	    		}
	    	}
	    	
	    	SimpleProfiler.start("modifyEntry_indexAdd");
	    	modifyEntry_indexAdd(binder, entry, inputData, fileUploadItems, filesToReindex,ctx);
	    	SimpleProfiler.stop("modifyEntry_indexAdd");
	    	
	    	SimpleProfiler.start("modifyEntry_done");
	    	modifyEntry_done(binder, entry, inputData,ctx);
	    	SimpleProfiler.stop("modifyEntry_done");
	    	
	    	if(filesErrors.getProblems().size() > 0) {
	    		// At least one error occurred during the operation. 
	    		throw new WriteFilesException(filesErrors);
	    	}	    	
        } catch(WriteFilesException ex) {
        	throw ex;
        } catch(Exception ex) {
        	entryDataErrors.addProblem(new Problem(Problem.GENERAL_PROBLEM, ex));
        	throw new WriteEntryDataException(entryDataErrors);
 	    }finally {
		    cleanupFiles(fileUploadItems);
	        SimpleProfiler.stop("modifyEntryNormal");
	    }
	}

    //no transaction expected
    protected void modifyEntryValidationOnly(final Binder binder, final Entry entry, 
    		final InputDataAccessor inputData, Map fileItems, 
    		final Collection deleteAttachments, final Map<FileAttachment,String> fileRenamesTo, Map options)  
    		throws WriteFilesException, WriteEntryDataException {
       SimpleProfiler.start("modifyEntryFileUploadDryRun");

       final Map ctx = new HashMap();
       if (options != null) ctx.putAll(options);
       modifyEntry_setCtx(entry, ctx);

    	Map entryDataAll;
    	EntryDataErrors entryDataErrors = new EntryDataErrors();

    	entryDataAll = modifyEntry_toEntryData(entry, inputData, fileItems, ctx);
    	
	    @SuppressWarnings("unused")
		final Map entryData = (Map) entryDataAll.get(ObjectKeys.DEFINITION_ENTRY_DATA);
	    List fileUploadItems = (List) entryDataAll.get(ObjectKeys.DEFINITION_FILE_DATA);
	    entryDataErrors = (EntryDataErrors) entryDataAll.get(ObjectKeys.DEFINITION_ERRORS);
        if (entryDataErrors.getProblems().size() > 0) {
        	//An error occurred processing the entry Data
        	throw new WriteEntryDataException(entryDataErrors);
        }
        
	    try {	    	
	    	FilesErrors filesErrors = new FilesErrors();
	    	filesErrors = modifyEntry_processFiles(binder, entry, fileUploadItems, filesErrors, ctx);
	    		    	
	    	if(filesErrors.getProblems().size() > 0) {
	    		// At least one error occurred during the operation. 
	    		throw new WriteFilesException(filesErrors);
	    	}	    	
        } catch(WriteFilesException ex) {
        	throw ex;
        } catch(Exception ex) {
        	entryDataErrors.addProblem(new Problem(Problem.GENERAL_PROBLEM, ex));
        	throw new WriteEntryDataException(entryDataErrors);
 	    }finally {
	        SimpleProfiler.stop("modifyEntryFileUploadDryRun");
	    }
	}

    protected void modifyEntry_setCtx(Entry entry, Map ctx) {
    	//save normalized title and title before changes
		ctx.put(ObjectKeys.FIELD_ENTITY_NORMALIZED_TITLE, entry.getNormalTitle());
		ctx.put(ObjectKeys.FIELD_ENTITY_TITLE, entry.getTitle());
    }
   protected FilesErrors modifyEntry_filterFiles(Binder binder, Entry entry,
    		Map entryData, List fileUploadItems, Map ctx) throws FilterException, TitleException {
   		FilesErrors nameErrors = new FilesErrors();
   		
   		SimpleProfiler.start("modifyEntry_filterFiles_validate");
   		checkInputFileNames(fileUploadItems, nameErrors);
   		
   		if(!binder.isMirrored()) {
   			checkInputFilesForNonMirroredBinder(fileUploadItems, nameErrors);
   		}
   		else {
   			List<FileAttachment> fas = entry.getFileAttachments(ObjectKeys.FI_ADAPTER); // should be at most 1 in size
   			if(fas.size() > 1)
   				logger.warn("Integrity error: Entry " + entry.getId() + " in binder [" + binder.getPathName() + "] mirrors multiple files");
   			if(fas.isEmpty()) {
   	   			checkInputFilesForMirroredBinder(binder, fileUploadItems, nameErrors);
   			}
   			else {
   	  			for(int i = 0; i < fileUploadItems.size(); i++) {
   	   				FileUploadItem fui = (FileUploadItem) fileUploadItems.get(i);
   	   				if(fui.getRepositoryName().equals(ObjectKeys.FI_ADAPTER)) {
	   					for(FileAttachment fa : fas) {
	   						if(!fui.getOriginalFilename().equals(fa.getFileItem().getName())) {
	   		   					fileUploadItems.remove(i);
	   							nameErrors.addProblem(new FilesErrors.Problem
	   									(fui.getRepositoryName(), fui.getOriginalFilename(), 
	   											FilesErrors.Problem.PROBLEM_MIRRORED_FILE_MULTIPLE));
	   							i--;
	   							break;					
	   						}
	   					}
   	   				}
   	  			}   				
   			}
   		}
   		SimpleProfiler.stop("modifyEntry_filterFiles_validateInput");

        FilesErrors checkSumErrors = getFileModule().verifyCheckSums(fileUploadItems);

   		SimpleProfiler.start("modifyEntry_filterFiles_registerFileName");
    	// 	Make sure the file name is unique if requested		
    	for (int i=0; i<fileUploadItems.size(); ) {
    		FileUploadItem fui = (FileUploadItem)fileUploadItems.get(i);
       		String name = fui.getOriginalFilename();
    		try {
    			//check if name exists
    			FileAttachment fa = entry.getFileAttachment(name);
    			if (fa == null) {
    		   		if (binder.isLibrary()) {
    		   			getCoreDao().registerFileName(binder, entry, fui.getOriginalFilename());
    		   			fui.setRegistered(true);
    		   		}
    		   		++i;
       			} else if (!fui.getRepositoryName().equals(fa.getRepositoryName())) {
       	   			fileUploadItems.remove(i);
       	   			nameErrors.addProblem(new FilesErrors.Problem(null, 
       	  					fui.getOriginalFilename(), FilesErrors.Problem.PROBLEM_FILE_EXISTS));
       			} else 	++i;      				
    		} catch (TitleException te) {
    			fileUploadItems.remove(i);
    			nameErrors.addProblem(new FilesErrors.Problem(null, 
       	  					fui.getOriginalFilename(), FilesErrors.Problem.PROBLEM_FILE_EXISTS));
    		}
    	}
   		SimpleProfiler.stop("modifyEntry_filterFiles_registerFileName");
   		
   		SimpleProfiler.start("modifyEntry_filterFiles_filterFiles");
    	FilesErrors filterErrors = getFileModule().filterFiles(binder, entry, fileUploadItems);
   		SimpleProfiler.stop("modifyEntry_filterFiles_filterFiles");

   		filterErrors.getProblems().addAll(nameErrors.getProblems());
       filterErrors.getProblems().addAll(checkSumErrors.getProblems());
    	return filterErrors;
    }

    protected FilesErrors modifyEntry_processFiles(Binder binder, 
    		Entry entry, List fileUploadItems, FilesErrors filesErrors, Map ctx) {
    	if(ctx != null && Boolean.TRUE.equals(ctx.get(ObjectKeys.INPUT_OPTION_VALIDATION_ONLY)))
    		return getFileModule().writeFilesValidationOnly(binder, entry, fileUploadItems, filesErrors);
    	else
    		return getFileModule().writeFiles(binder, entry, fileUploadItems, filesErrors);
    }
    protected void modifyEntry_removeAttachments(Binder binder, Entry entry, 
    		Collection deleteAttachments, List<FileAttachment> filesToDeindex,
    		List<FileAttachment> filesToReindex, Map ctx) {
       	removeAttachments(binder, entry, deleteAttachments, filesToDeindex, filesToReindex);
       	
    }
    protected void modifyEntry_indexRemoveFiles(Binder binder, Entry entry, Collection<FileAttachment> filesToDeindex, Map ctx) {
    	removeFilesIndex(entry, filesToDeindex);
    }
   
    protected Map modifyEntry_toEntryData(Entry entry, InputDataAccessor inputData, Map fileItems, Map ctx) {
        boolean fieldsOnly = false;
        //Call the definition processor to get the entry data to be stored
        if (entry.getEntryDefId() != null) {
            Map entryDataAll = getDefinitionModule().getEntryData(entry.getEntryDefDoc(), inputData, fileItems, fieldsOnly);
             return entryDataAll;
        } else {
           	Map entryDataAll = new HashMap();
	        entryDataAll.put(ObjectKeys.DEFINITION_ENTRY_DATA,  new HashMap());
	        entryDataAll.put(ObjectKeys.DEFINITION_FILE_DATA,  new ArrayList());
	        entryDataAll.put(ObjectKeys.DEFINITION_ERRORS,  new EntryDataErrors());
	        return entryDataAll;
        }
    }
    //inside write transaction
    protected void modifyEntry_fillIn(Binder binder, Entry entry, InputDataAccessor inputData, Map entryData, Map ctx) {  
        processModificationTimestamp(entry, null, ctx);
        entry.incrLogVersion();
        for (Iterator iter=entryData.entrySet().iterator(); iter.hasNext();) {
        	Map.Entry mEntry = (Map.Entry)iter.next();
        	//need to generate id for the event so its id can be saved in customAttr
        	if (entry.getCustomAttribute((String)mEntry.getKey()) == null) {
        			Object obj = mEntry.getValue();
        			if (obj instanceof Event)
        				getCoreDao().save(obj);
        	}
        }
        
		TaskHelper.processTaskCompletion(entry, inputData, entryData);        
        EntryBuilder.updateEntry(entry, entryData);
 
    }
    //inside write transaction
    protected void modifyEntry_startWorkflow(Entry entry, Map ctx) {
    	if (!(entry instanceof WorkflowSupport)) return;
    	if (ctx != null && Boolean.TRUE.equals(ctx.get(ObjectKeys.INPUT_OPTION_NO_WORKFLOW))) return;
    	WorkflowSupport wEntry = (WorkflowSupport)entry;
    	//see if updates to entry, trigger transitions in workflow
    	if (!wEntry.getWorkflowStates().isEmpty()) getWorkflowModule().modifyWorkflowStateOnUpdate(wEntry);
     }   
    //inside write transaction
    protected void modifyEntry_postFillIn(Binder binder, Entry entry, InputDataAccessor inputData, 
    		Map entryData, Map<FileAttachment,String> fileRenamesTo, Map ctx) {
    	//create history - using timestamp and version from fillIn
  		if (entry.isTop() && binder.isUniqueTitles()) getCoreDao().updateTitle(binder, entry, (String)ctx.get(ObjectKeys.FIELD_ENTITY_NORMALIZED_TITLE), entry.getNormalTitle());		
    	reorderFiles(entry, inputData, entryData);
    	editFileComments(entry, inputData);
    	processChangeLog(entry, ChangeLog.MODIFYENTRY);
    	getReportModule().addAuditTrail(AuditType.modify, entry);

    	if(fileRenamesTo != null)
	    	for(FileAttachment fa : fileRenamesTo.keySet()) {
	    		String toName = fileRenamesTo.get(fa);
	    		getFileModule().renameFile(binder, entry, fa, toName);
	    	}
    }
    
    protected void modifyEntry_indexAdd(Binder binder, Entry entry, 
    		InputDataAccessor inputData, List fileUploadItems, 
    		Collection<FileAttachment> filesToIndex, Map ctx) {
  	   if (ctx != null && Boolean.TRUE.equals(ctx.get(ObjectKeys.INPUT_OPTION_NO_INDEX))) return;
  	    	//tags will be null for now
    	indexEntry(binder, entry, fileUploadItems, filesToIndex, false, 
    			(ctx == null ? null : (List)ctx.get(ObjectKeys.INPUT_FIELD_TAGS )));
    }

    protected void modifyEntry_done(Binder binder, Entry entry, InputDataAccessor inputData, Map ctx) {
    }
    //***********************************************************************************************************   
    //inside write transaction    
    @Override
	public Entry copyEntry(Binder binder, Entry source, Binder destination, String[] toFileNames, Map options) {
		throw new NotSupportedException(
				"errorcode.notsupported.copyEntry", new String[]{source.getTitle()});
    }
 
    @Override
	public void disableEntry(final Principal entry, final boolean disable) {
		SimpleProfiler.start("deleteEntry_transactionExecute");
		getTransactionTemplate().execute(new TransactionCallback() {
    		@Override
			public Object doInTransaction(TransactionStatus status) {
    			SimpleProfiler.start("deleteEntry_disableEntry");
    			entry.setDisabled(disable);
    			SimpleProfiler.stop("deleteEntry_disableEntry");
    			return null;
    		}});
    	SimpleProfiler.stop("deleteEntry_transactionExecute");
 	   if (disable && (!(entry instanceof UserPrincipal))) {
		   //Remove the entry from the index
 	    	final Map ctx = new HashMap();
 	    	deleteEntry_indexDel(entry.getParentBinder(), entry, ctx);
	   } else {
		   //Add the entry to the index
		   indexEntry(entry);
	   }
    }
    //***********************************************************************************************************   
    //no transaction expected
    @Override
	public void deleteEntry(final Binder parentBinder, final Entry entry, final boolean deleteMirroredSource, Map options) {
        SimpleProfiler.start("deleteEntry");

    	final Map ctx = new HashMap();
    	if (options != null) ctx.putAll(options);
    	deleteEntry_setCtx(entry, ctx);
    	final List<ChangeLog> changeLogs = new ArrayList();
    	//setup change logs, so have a complete picture of the entry.
    	//don't commit until transaction
    	deleteEntry_processChangeLogs(parentBinder, entry, ctx, changeLogs);
		SimpleProfiler.start("deleteEntry_processFiles");
		//do outside transation cause archiveing takes a long time
		deleteEntry_processFiles(parentBinder, entry, deleteMirroredSource, ctx);
		SimpleProfiler.stop("deleteEntry_processFiles");
		//	may have taken awhile to remove attachments	
		SimpleProfiler.start("deleteEntry_transactionExecute");
		getCoreDao().refresh(parentBinder);
		getTransactionTemplate().execute(new TransactionCallback() {
    		@Override
			public Object doInTransaction(TransactionStatus status) {
    			SimpleProfiler.start("deleteEntry_preDelete");
    			deleteEntry_preDelete(parentBinder, entry, ctx);
    			SimpleProfiler.stop("deleteEntry_preDelete");
        
    			SimpleProfiler.start("deleteEntry_workflow");
    			deleteEntry_workflow(parentBinder, entry, ctx);
    			SimpleProfiler.stop("deleteEntry_workflow");
                 
    			SimpleProfiler.start("deleteEntry_delete");
    			deleteEntry_delete(parentBinder, entry, ctx);
    			SimpleProfiler.stop("deleteEntry_delete");
        
    			SimpleProfiler.start("deleteEntry_postDelete");
    			deleteEntry_postDelete(parentBinder, entry, ctx);
    			SimpleProfiler.stop("deleteEntry_postDelete");
    			for (ChangeLog changeLog:changeLogs) {
    				getCoreDao().save(changeLog);
    			}
        
    			return null;
    		}});
    	SimpleProfiler.stop("deleteEntry_transactionExecute");
    	SimpleProfiler.start("deleteEntry_indexDel");
    	deleteEntry_indexDel(parentBinder, entry, ctx);
    	SimpleProfiler.stop("deleteEntry_indexDel");
        SimpleProfiler.stop("deleteEntry");
   }
    //no transaction
    protected void deleteEntry_setCtx(Entry entry, Map ctx) {
    }
    //no transaction
   	protected void deleteEntry_processChangeLogs(Binder parentBinder, Entry entry, Map ctx, List changeLogs) {
   		//create history prior to delete.
   		User user = RequestContextHolder.getRequestContext().getUser();
   		entry.setModification(new HistoryStamp(user));
   		entry.incrLogVersion();
   		//record current state of object, but don't save until in transaction
   		//this is setup here so the deleteFiles logs have the correct version/date
   		changeLogs.add(processChangeLog(entry, ChangeLog.DELETEENTRY, false));
   	}
    //inside write transaction
    protected void deleteEntry_preDelete(Binder parentBinder, Entry entry, Map ctx) {
   		if (entry.isTop() && parentBinder.isUniqueTitles()) 
   			getCoreDao().updateTitle(parentBinder, entry, entry.getNormalTitle(), null);		
   		// Make sure that the audit trail's timestamp is identical to the modification time of the entry. 
    	getReportModule().addAuditTrail(AuditType.delete, entry, entry.getModification().getDate());
    }
        
    //inside write transaction
    protected void deleteEntry_workflow(Binder parentBinder, Entry entry, Map ctx) {
    	if (entry instanceof WorkflowSupport)
    		getWorkflowModule().deleteEntryWorkflow((WorkflowSupport)entry);
    }
    
    //no transaction
    protected void deleteEntry_processFiles(Binder parentBinder, Entry entry, boolean deleteMirroredSource, Map ctx) {
    	//attachment meta-data not deleted.  Done in optimized delete entry
    	if(deleteMirroredSource && parentBinder.isMirrored()) {
    		if(getResourceDriverManager().isReadonly(parentBinder.getResourceDriverName())) {
    			if(logger.isDebugEnabled())
    				logger.debug("Source file will not be deleted when deleting mirrored entry " + entry.getId()
    						+ " because corresponding resource driver is read-only");
    			deleteMirroredSource = false;
    		}
    	}
    	getFileModule().deleteFiles(parentBinder, entry, deleteMirroredSource, null);
    }
    //inside write transaction    
    protected void deleteEntry_delete(Binder parentBinder, Entry entry, Map ctx) {
    	//use the optimized deleteEntry or hibernate deletes each collection entry one at a time
    	getCoreDao().delete(entry);   
    }
    //inside write transaction
    protected void deleteEntry_postDelete(Binder parentBinder, Entry entry, Map ctx) {
   }
    //no transaction
    protected void deleteEntry_indexDel(Binder parentBinder, Entry entry, Map ctx) {
        // Delete the document that's currently in the index.
    	// Since all matches will be deleted, this will also delete the attachments
        IndexSynchronizationManager.deleteDocument(entry.getIndexDocumentUid());
   }
    //***********************************************************************************************************
    @Override
	public void moveEntry(Binder binder, Entry entry, Binder destination, String[] toFileNames, Map options) {
		throw new NotSupportedException(
				"errorcode.notsupported.moveEntry", new String[]{entry.getTitle()});
    }
    //***********************************************************************************************************
    //no transaction
    @Override
	public Binder copyBinder(Binder source, Binder destination, Map options) {
    	Binder binder = super.copyBinder(source, destination, options);
    	copyEntries(source, binder, options);
    	return binder;
    }

    //***********************************************************************************************************
    //no transaction
    @Override
	public void copyEntries(Binder source, Binder destination, Map options) {
		throw new NotSupportedException("errorcode.notsupported.copyEntry", "ALL");

    }
    //***********************************************************************************************************
    /*
     * classes must provide code to delete files belonging to entries
     */
    @Override
	protected abstract void deleteBinder_processFiles(Binder binder, Map ctx);
    /*
     * classes must provide code to delete entries in the binder
     */   
    @Override
	protected abstract void deleteBinder_delete(Binder binder, boolean deleteMirroredSource, Map ctx);
    @Override
	protected void deleteBinder_indexDel(Binder binder, Map ctx) {
        // Delete the document that's currently in the index.
    	// Since all matches will be deleted, this will also delete the attachments
    	indexDeleteEntries(binder);
    	super.deleteBinder_indexDel(binder, ctx);
    }
	    
    //***********************************************************************************************************
     protected void indexDeleteEntries(Binder binder) {
		// Since all matches will be deleted, this will also delete the attachments 
		IndexSynchronizationManager.deleteDocuments(new Term(Constants.BINDER_ID_FIELD, binder.getId().toString()));
    }
     //***********************************************************************************************************
     @Override
	public void addEntryWorkflow(Binder binder, Entry entry, Definition definition, Map options) {
    	  		if (!(entry instanceof WorkflowSupport)) return;
  		WorkflowSupport wEntry = (WorkflowSupport)entry;
  		//set up version for all loggin
  		entry.incrLogVersion();
  		getWorkflowModule().addEntryWorkflow(wEntry, entry.getEntityIdentifier(), definition, options);
  		if (wEntry.getWorkflowChange() != null) processChangeLog(entry, ChangeLog.STARTWORKFLOW);
  		if (options != null && Boolean.TRUE.equals(options.get(ObjectKeys.INPUT_OPTION_NO_INDEX))) return;
  		indexEntry(entry);
  		getRssModule().updateRssFeed(entry); 
     }
     //***********************************************************************************************************
     @Override
	public void deleteEntryWorkflow(Binder binder, Entry entry, Definition definition) {
 		if (!(entry instanceof WorkflowSupport)) return;
  		WorkflowSupport wEntry = (WorkflowSupport)entry;
  		//set up version for all loggin
  		entry.incrLogVersion();
  		
  		getWorkflowModule().deleteEntryWorkflow(wEntry, definition);
  		processChangeLog(entry, ChangeLog.ENDWORKFLOW);
   		indexEntry(entry);
   		getRssModule().updateRssFeed(entry); 
     }
   //***********************************************************************************************************
    @Override
	public void modifyWorkflowState(Binder binder, Entry entry, Long tokenId, String toState) {

 		if (!(entry instanceof WorkflowSupport)) return;
 		WorkflowSupport wEntry = (WorkflowSupport)entry;
		//Find the workflowState
		WorkflowState ws = wEntry.getWorkflowState(tokenId);
 		if (ws != null) {
 	   		entry.incrLogVersion();
 	        getWorkflowModule().modifyWorkflowState(wEntry, ws, toState);
			processChangeLog(entry, ChangeLog.MODIFYWORKFLOWSTATE);
			indexEntry(entry);
		}
    }
 
    @Override
	public void setWorkflowResponse(Binder binder, Entry entry, Long stateId, InputDataAccessor inputData, Boolean canModifyEntry)  {
		if (!(entry instanceof WorkflowSupport)) return;
 		WorkflowSupport wEntry = (WorkflowSupport)entry;
        User user = RequestContextHolder.getRequestContext().getUser();

 		WorkflowState ws = wEntry.getWorkflowState(stateId);
		Definition def = ws.getDefinition();
		boolean changes = false;
		Map questions = WorkflowUtils.getQuestions(def, ws);
		for (Iterator iter=questions.entrySet().iterator(); iter.hasNext();) {
			Map.Entry me = (Map.Entry)iter.next();
			String question = (String)me.getKey();
			if (!inputData.exists(question)) continue;

			String response = inputData.getSingleValue(question);
			Map qData = (Map)me.getValue();
			Map rData = (Map)qData.get(ObjectKeys.WORKFLOW_QUESTION_RESPONSES);
			if (!rData.containsKey(response)) {
				throw new IllegalArgumentException("Illegal workflow response: " + response);
			}
			//now see if response to this question from this user exists
			Set responses = wEntry.getWorkflowResponses();
			boolean found=false;
			WorkflowResponse wr=null;
			for (Iterator iter2=responses.iterator(); iter2.hasNext();) {
				wr = (WorkflowResponse)iter2.next();
				if (def.getId().equals(wr.getDefinitionId()) && 
						question.equals(wr.getName()) &&
						user.getId().equals(wr.getResponderId())) {
					found = true;
					break;
				}			
			}
			if ((!WorkflowProcessUtils.checkIfQuestionRespondersSpecified(wEntry, ws, question) && canModifyEntry) || 
					BinderHelper.checkIfWorkflowResponseAllowed(wEntry, ws, question) || 
	    			(WorkflowProcessUtils.checkIfQuestionRespondersIncludeForumDefault(wEntry, ws, question) &&
	    			canModifyEntry)) {
				if (found) {
					if (!response.equals(wr.getResponse())) {
						//if no changes have been made update timestamp
						if (!changes) {
							entry.setModification(new HistoryStamp(user));
							entry.incrLogVersion();	
							changes = true;
						}
						wr.setResponse(response);
						wr.setResponseDate(entry.getModification().getDate());
						wEntry.removeWorkflowResponse(wr);
						wEntry.addWorkflowResponse(wr);
					}
				} else {
					//if no changes have been made update timestamp
					if (!changes) {
						entry.setModification(new HistoryStamp(user));
						entry.incrLogVersion();				
						changes = true;
					}
					wr = new WorkflowResponse();
					wr.setResponderId(user.getId());
					wr.setResponseDate(entry.getModification().getDate());
					wr.setDefinitionId(def.getId());
					wr.setName(question);
					wr.setResponse(response);
					wr.setOwner(entry);
					getCoreDao().save(wr);
					wEntry.addWorkflowResponse(wr);
				}
			}
		}
		if (changes) {
			getWorkflowModule().modifyWorkflowStateOnResponse(wEntry);
			entry.incrLogVersion();
			processChangeLog(entry, ChangeLog.ADDWORKFLOWRESPONSE);
	    	getReportModule().addAuditTrail(AuditType.modify, entry);
			indexEntry(entry);
		}
     }

    //***********************************************************************************************************
    /**
     * Index binder and its entries
     */
    @Override
	public IndexErrors indexBinder(Binder binder, boolean includeEntries, boolean deleteIndex, Collection tags) {
    	IndexErrors errors = super.indexBinder(binder, includeEntries, deleteIndex, tags);
    	if (includeEntries == false) return errors;
    	IndexErrors entryErrors = indexEntries(binder, deleteIndex, false);
    	errors.add(entryErrors);
    	return errors;
    }
    
    
    // This method indexes a binder and it's entries by deleting and reindexing each entry one at a time.
    public IndexErrors indexBinderIncremental(Binder binder, boolean includeEntries, boolean deleteIndex, Collection tags) {
    	IndexErrors errors = super.indexBinder(binder, includeEntries, deleteIndex, tags);
    	if (includeEntries == false) return errors;
    	IndexErrors entryErrors = indexEntries(binder, deleteIndex, true);
    	errors.add(entryErrors);
    	IndexSynchronizationManager.applyChanges(0);
    	return errors;
    }
    
    //If called from write transaction, make sure session is flushed cause this bypasses
    //hibernate loading of collections and goes to database directly.
    protected IndexErrors indexEntries(Binder binder, boolean deleteIndex, boolean deleteEntries) {
    	IndexErrors errors = new IndexErrors();
    	SimpleProfiler.start("indexEntries");
    	//may already have been handled with an optimized query
    	if (deleteIndex && !deleteEntries) {
        	SimpleProfiler.start("indexEntries_preIndex");
    		indexEntries_preIndex(binder);
        	SimpleProfiler.stop("indexEntries_preIndex");
    	}
  		//flush any changes so any exiting changes don't get lost on the evict
 //   	SimpleProfiler.startProfiler("indexEntries_flush");
 //   	getCoreDao().flush();
 //   	SimpleProfiler.stopProfiler("indexEntries_flush");
  		SFQuery query = indexEntries_getQuery(binder);
  		int threshhold = SPropsUtil.getInt("lucene.flush.threshold", 100);
       	try {       
       		List batch = new ArrayList();
  			List docs = new ArrayList();
  			int total=0;
      		while (query.hasNext()) {
       			int count=0;
       			batch.clear();
       			docs.clear();
       			// get 1000 entries, then build collections by hand 
       			//for performance
       			while (query.hasNext() && (count < 1000)) {
       				Object obj = query.next();
       				if (obj instanceof Object[])
       					obj = ((Object [])obj)[0];
       				batch.add(obj);
       				++count;
       			}
       			total += count;
       			//have 1000 entries, manually load their collections
       			SimpleProfiler.start("indexEntries_load");
       			indexEntries_load(binder, batch);
       			SimpleProfiler.stop("indexEntries_load");
       			logger.info("Indexing at " + total + "(" + binder.getPathName() + ")");
       			SimpleProfiler.start("indexEntries_loadTags");
       			Map tags = indexEntries_loadTags(binder, batch);
       			SimpleProfiler.stop("indexEntries_loadTags");
       			for (int i=0; i<batch.size(); ++i) {
       				Entry entry = (Entry)batch.get(i);
       				if (deleteEntries) {
       					IndexSynchronizationManager.deleteDocument(entry.getIndexDocumentUid());
       				}
   					List entryTags = (List)tags.get(entry.getEntityIdentifier());
       				if (indexEntries_validate(binder, entry)) {
       					// 	Create an index document from the entry object. 
       					// Entry already deleted from index, so pretend we are new
       	       			SimpleProfiler.start("indexEntries_indexEntryWithAttachments");
       				   	try {
       				   		IndexErrors entryErrors = indexEntryWithAttachments(binder, entry, entry.getFileAttachments(), null, true, entryTags);
       				   		errors.add(entryErrors);
       				   	} catch(Exception e) {
       				   		logger.error("Error indexing entry: (" + entry.getId().toString() + ") " + entry.getTitle(), e);
       				   		errors.addError(entry);
       				   	}
       	       			SimpleProfiler.stop("indexEntries_indexEntryWithAttachments");
       				   	indexEntries_postIndex(binder, entry);
       				}
           			getCoreDao().evict(entryTags);
       				getCoreDao().evict(entry);
          	  		//apply after we have gathered a few
   	       			SimpleProfiler.start("indexEntries_applyChanges");
           	   		IndexSynchronizationManager.applyChanges(threshhold);
   	       			SimpleProfiler.stop("indexEntries_applyChanges");
       			}
       	 	            	            
       			// Register the index document for indexing.
       			logger.info("Indexing done at " + total + "("+ binder.getPathName() + ")");
       		
        	}
        	
        } finally {
        	//clear out anything remaining
        	query.close();
        	SimpleProfiler.stop("indexEntries");
        }
        return errors;
    }
    protected void indexEntries_preIndex(Binder binder) {
    	indexDeleteEntries(binder); 
    }
 
   	protected abstract SFQuery indexEntries_getQuery(Binder binder);
   	protected boolean indexEntries_validate(Binder binder, Entry entry) {
   		return true;
   	}
   	protected void indexEntries_postIndex(Binder binder, Entry entry) {
   	}
   	protected void indexEntries_load(Binder binder, List entries)  {
   		// bulkd load any collections that neeed to be indexed
   		getCoreDao().bulkLoadCollections(entries);
   	}
	protected Map indexEntries_loadTags(Binder binder, List<Entry> entries) {
		List<EntityIdentifier> ids = new ArrayList();
		for (Entry e: entries) {
			ids.add(e.getEntityIdentifier());
		}
		return getCoreDao().loadAllTagsByEntity(ids);
	}
 
    //***********************************************************************************************************
   	@Override
	public IndexErrors indexEntries(Collection entries) {
   		IndexErrors errors = new IndexErrors();
   		for (Iterator iter=entries.iterator(); iter.hasNext();) {
   			Entry entry = (Entry)iter.next();
   			IndexErrors entryErrors = indexEntry(entry);
   			errors.add(entryErrors);
   		}
   		return errors;
   	}
	
    //***********************************************************************************************************
    @Override
	public Map getBinderEntries(Binder binder, String[] entryTypes, Map options) {
        //search engine will only return entries you have access to
         //validate entry count
    	//do actual search index query
        Hits hits = getBinderEntries_doSearch(binder, entryTypes, options);
        //iterate through results
        List childEntries = SearchUtils.getSearchEntries(hits);
        SearchUtils.extendPrincipalsInfo(childEntries, getProfileDao(), getEntryPrincipalField());
       	Map model = new HashMap();
        model.put(ObjectKeys.BINDER, binder);      
        model.put(ObjectKeys.SEARCH_ENTRIES, childEntries);
        model.put(ObjectKeys.SEARCH_COUNT_TOTAL, new Integer(hits.getTotalHits()));
        //Total number of results found
        model.put(ObjectKeys.TOTAL_SEARCH_COUNT, new Integer(hits.getTotalHits()));
        //Total number of results returned
        model.put(ObjectKeys.TOTAL_SEARCH_RECORDS_RETURNED, new Integer(hits.length()));
        return model;
   }
    
    protected int getBinderEntries_maxEntries(int maxChildEntries) {
        if (maxChildEntries == 0 || maxChildEntries == Integer.MAX_VALUE) maxChildEntries = DEFAULT_MAX_CHILD_ENTRIES;
        return maxChildEntries;
    }
     
    protected Hits getBinderEntries_doSearch(Binder binder, String [] entryTypes, 
    		Map options) {
    	int maxResults = 0;
       	int searchOffset = 0;
   		boolean includeNestedBinders;
   		Integer searchMode = null;
    	Binder searchBinder = binder;
   		
        if (options != null) {
        	if (options.containsKey(ObjectKeys.SEARCH_MAX_HITS)) 
        		maxResults = (Integer) options.get(ObjectKeys.SEARCH_MAX_HITS);
        
        	if (options.containsKey(ObjectKeys.SEARCH_OFFSET)) 
    			searchOffset = (Integer) options.get(ObjectKeys.SEARCH_OFFSET);
        	
       		includeNestedBinders = options.containsKey(ObjectKeys.SEARCH_INCLUDE_NESTED_BINDERS);
       		
       		searchMode = (Integer) options.get(ObjectKeys.SEARCH_MODE);
        }
        else {
       		includeNestedBinders = false;
        }
        if (searchOffset < 0) searchOffset = 0;
    	maxResults = getBinderEntries_maxEntries(maxResults); 
       	Hits hits = null;
       	org.dom4j.Document queryTree = null;
       	if ((options != null) && options.containsKey(ObjectKeys.FOLDER_ENTRY_TO_BE_SHOWN)) {
	   		SearchFilter searchFilter = new SearchFilter(true);
	   		searchFilter.addEntryId((String) options.get(ObjectKeys.FOLDER_ENTRY_TO_BE_SHOWN));
	   		getBinderEntries_getSearchDocument(searchBinder, entryTypes, includeNestedBinders, searchFilter);
	   		queryTree = SearchUtils.getInitalSearchDocument(searchFilter.getFilter(), null);
       	} else {

           	SearchFilter searchFilter = new SearchFilter(true);

           	if ((options != null) && options.containsKey(ObjectKeys.SEARCH_SEARCH_FILTER)) {
           		org.dom4j.Document userSearchFilter = (org.dom4j.Document) options.get(ObjectKeys.SEARCH_SEARCH_FILTER);
               	if (userSearchFilter != null) {
               		searchFilter.appendFilter(userSearchFilter);
               	}       		
           	}

        	if ((options != null) && options.containsKey(ObjectKeys.SEARCH_SEARCH_DYNAMIC_FILTER)) {
        		org.dom4j.Document userDynamicSearchFilter = (org.dom4j.Document) options.get(ObjectKeys.SEARCH_SEARCH_DYNAMIC_FILTER);
            	if (userDynamicSearchFilter != null) {
            		searchFilter.appendFilter(userDynamicSearchFilter);
            	}
        	}

           	if ((options != null) && options.containsKey(ObjectKeys.SEARCH_IS_PERSON) && 
           			(Boolean)options.get(ObjectKeys.SEARCH_IS_PERSON)) {
           		// This term will only include users consider to be a
           		// person (i.e., no E-mail posting agent, ...)
           		SearchFilter searchTermFilter = new SearchFilter();
           		searchTermFilter.addAndPersonFlagFilter(true);
               	searchFilter.appendFilter(searchTermFilter.getFilter());
           	}

           	// Handle any internal/external user filtering.
           	Boolean isInternal = ((options != null) ? ((Boolean) options.get(ObjectKeys.SEARCH_IS_INTERNAL)) : null);
           	Boolean isExternal = ((options != null) ? ((Boolean) options.get(ObjectKeys.SEARCH_IS_EXTERNAL)) : null);
           	if ((null != isInternal) && isInternal) {
           		// This term will only include internal users.
           		SearchFilter searchTermFilter = new SearchFilter();
           		searchTermFilter.addAndInternalFilter(true);
               	searchFilter.appendFilter(searchTermFilter.getFilter());
           	}
           	if ((null != isExternal) && isExternal) {
           		// This term will only include non-internal (i.e.,
           		// external) users.
           		SearchFilter searchTermFilter = new SearchFilter();
           		searchTermFilter.addAndInternalFilter(false);
               	searchFilter.appendFilter(searchTermFilter.getFilter());
           	}

           	// Handle any enabled/disabled user filtering.
           	Boolean isDisabledUsers = ((options != null) ? ((Boolean) options.get(ObjectKeys.SEARCH_IS_DISABLED_USERS)) : null);
           	Boolean isEnabledUsers  = ((options != null) ? ((Boolean) options.get(ObjectKeys.SEARCH_IS_ENABLED_USERS))  : null);
           	if ((null != isDisabledUsers) && isDisabledUsers) {
           		// This term will only include disabled users.
           		SearchFilter searchTermFilter = new SearchFilter();
           		searchTermFilter.addAndDisabledUserFilter(true);
               	searchFilter.appendFilter(searchTermFilter.getFilter());
           	}
           	if ((null != isEnabledUsers) && isEnabledUsers) {
           		// This term will only include enabled users.
           		SearchFilter searchTermFilter = new SearchFilter();
           		searchTermFilter.addAndDisabledUserFilter(false);
               	searchFilter.appendFilter(searchTermFilter.getFilter());
           	}

           	// Handle any virtual/physical filtering.
        	if ((options != null) && options.containsKey(ObjectKeys.FOLDER_MODE_TYPE)) {
        		ListFolderHelper.ModeType mode = ((ListFolderHelper.ModeType) options.get(ObjectKeys.FOLDER_MODE_TYPE));
        		if ((null != mode) && (ListFolderHelper.ModeType.VIRTUAL == mode)) {
        			searchBinder = null;
        		}
        	}

        	// Finally, perform the search.
        	getBinderEntries_getSearchDocument(searchBinder, entryTypes, includeNestedBinders, searchFilter);
	   		queryTree = SearchUtils.getInitalSearchDocument(searchFilter.getFilter(), null);
        	SearchUtils.getQueryFields(queryTree, options); 
       	}       	
    	
       	//Create the Lucene query
    	QueryBuilder qb = new QueryBuilder(true, false);
    	SearchObject so = qb.buildQuery(queryTree);
    	
    	//Set the sort order
    	SortField[] fields = SearchUtils.getSortFields(options); 
    	so.setSortBy(fields);
    	Query soQuery = so.getLuceneQuery();    //Get the query into a variable to avoid doing this very slow operation twice
    	
    	if(logger.isDebugEnabled()) {
    		logger.debug("Query is: " + queryTree.asXML());
    		logger.debug("Query is: " + soQuery.toString());
    	}
    	
    	if(searchMode == null) {
    		// Search mode is not specified by the caller. Let's see if we can figure it out.
    		if(searchBinder != null && AccessUtils.testReadAccess(searchBinder)) {
    			searchMode = Integer.valueOf(Constants.SEARCH_MODE_PREAPPROVED_PARENTS);
    		}
    		else {
    			searchMode = Constants.SEARCH_MODE_NORMAL;
    		}
    	}
    	
    	LuceneReadSession luceneSession = getLuceneSessionFactory().openReadSession();
        
        try {
	        //Make sure to get inaccessible sub-folders that have visible folders further down the tree
        	hits = luceneSession.searchFolderOneLevelWithInferredAccess(RequestContextHolder.getRequestContext().getUserId(),
	        		so.getAclQueryStr(), searchMode.intValue(), soQuery, so.getSortBy(), searchOffset, 
	        		maxResults, binder.getId(), binder.getPathName());
        }
        finally {
            luceneSession.close();
        }
    	
        return hits;
     
    }
    
    private void getBinderEntries_getSearchDocument(Binder binder, String [] entryTypes, boolean includeNestedBinders, SearchFilter searchFilter) {
    	// Add a new AND block to the search filter.
		searchFilter.newCurrentFilterTermsBlock(true);

		// Is the search to include nested binders?
		boolean hasBinder = (null != binder);
		String binderId = (hasBinder ? String.valueOf(binder.getId()) : null);
		if (includeNestedBinders) {
			// Yes!  Add an OR block into the AND added above...
			Element nestedOrCFT = searchFilter.newNestedFilterTermsBlock(false);

			// ...add an AND to that OR to find the entries...
    		searchFilter.newNestedFilterTermsBlock(true);
	   		searchFilter.addDocumentType(Constants.DOC_TYPE_ENTRY);
			searchFilter.addEntryTypes(entryTypes);
			if (hasBinder) {
				searchFilter.addFolderId(binderId);
			}

			// ...and add another AND to the OR to find the nested
			// ...binders.
	   		searchFilter.setCurrentFilterTerms(nestedOrCFT);
    		searchFilter.newNestedFilterTermsBlock(true);
	   		searchFilter.addDocumentType(Constants.DOC_TYPE_BINDER);
	   		if (hasBinder) {
	   			searchFilter.addBinderParentId(binderId);
	   		}
		}
		
		else {
			// No, the search doesn't include nested binders!  Simply
			// search for the entries.
			if (hasBinder) {
				searchFilter.addFolderId(binderId);
			}
	   		searchFilter.addDocumentType(Constants.DOC_TYPE_ENTRY);
			searchFilter.addEntryTypes(entryTypes);
		}
    }

    @Override
	public IndexErrors indexEntry(Entry entry) {
    	return indexEntry(entry.getParentBinder(), entry, null, null, false, null);
    }
    /**
     * Index entry and optionally its attached files.
     * 
     * @param biner
     * @param entry
     * @param fileUploadItems uploaded files or <code>null</code>. 
     * At minimum, those files in the list must be indexed.  
     * @param filesToIndex a list of FileAttachments or <code>null</code>. 
     * At minimum, those files in the list must be indexed. 
     * @param newEntry
     */
    protected IndexErrors indexEntry(Binder binder, Entry entry, List fileUploadItems, 
    		Collection<FileAttachment> filesToIndex, boolean newEntry, Collection tags) {
    	// Logically speaking, the only files we need to index are the ones
    	// that have been uploaded (fileUploadItems) and the ones explicitly
    	// specified (in the filesToIndex). In ideal world, indexing only
    	// those subset will yield better performance. However, the way 
    	// file indexing currently works is that, whenever some aspect of its 
    	// enclosing entry changes, it deletes not only the entry but also all 
    	// file attachments from the index, because each file index entry 
    	// actually duplicates the common data from the entry itself. (see 
    	// overloaded indexEntry method in this class). Therefore, we must 
    	// (re)index "all" the file attachments in the entry regardless of 
    	// whether a particular file content has changed or not. 
    	// Consequently we obtain and pass "all" the attachments to the 
    	// following method and ignore the filesToIndex list (for now).
    	
    	return indexEntryWithAttachments(binder, entry, entry.getFileAttachments(), fileUploadItems, newEntry, tags);
    }
    
    /**
     * Index entry along with its file attachments. 
     * 
     * @param binder
     * @param entry
     * @param fileAttachments list of FileAttachments that need to be (re)indexed.
     * Only those files explicitly listed in this list are indexed. 
     * @param fileUploadItems a list of uploaded files or a <code>null</code>.
     * If each FileAttachment in fileAttachments list finds a corresponding
     * uploaded file in this list, the content of the uploaded file can be
     * used for indexing. Otherwise, the file content must be obtained from
     * the repository. Note that the elements in this list do not positionally
     * correspond to the elements in fileAttachments list. 
     * @param newEntry
     */
	protected IndexErrors indexEntryWithAttachments(Binder binder, Entry entry,
			Collection<FileAttachment> fileAttachments, List fileUploadItems, boolean newEntry, Collection tags) {
		IndexErrors errors = new IndexErrors();
		if(SPropsUtil.getBoolean("indexing.escalate.add.to.update", true))
			newEntry = false;
		
		if(!newEntry) {
			// This is modification. We must first delete existing document(s) from the index.
			
			// Since all matches will be deleted, this will also delete the attachments 
	        IndexSynchronizationManager.deleteDocument(entry.getIndexDocumentUid());
		}
		
        // Create an index document from the entry object.
       // Register the index document for indexing.
        IndexSynchronizationManager.addDocument(buildIndexDocumentFromEntry(entry.getParentBinder(), entry, tags));
        //Create separate documents one for each attached file and index them.
        for(FileAttachment fa : fileAttachments) {
        	FileUploadItem fui = null;
        	if(fileUploadItems != null)
        		fui = findFileUploadItem(fileUploadItems, fa.getRepositoryName(), fa.getFileItem().getName());
        	try {
        		IndexSynchronizationManager.addDocument(buildIndexDocumentFromEntryFile(binder, entry, fa, fui, tags));
           		// Register the index document for indexing.
	        } catch (Exception ex) {
		       		//log error but continue
		       		logger.error("Error indexing file for entry " + entry.getId() + " attachment " + fa, ex);
		       		errors.addError(entry);
        	}
         }
        return errors;
 	}
	
    @Override
	public org.apache.lucene.document.Document buildIndexDocumentFromEntry(Binder binder, Entry entry, Collection tags) {
    	SimpleProfiler.start("buildIndexDocumentFromEntry");
    	org.apache.lucene.document.Document indexDoc = new org.apache.lucene.document.Document();
        boolean fieldsOnly = false;
    	fillInIndexDocWithCommonPartFromEntry(indexDoc, binder, entry, fieldsOnly);
    	
        // Add creation-date and modification date from entry
    	// Not in common part, cause files use different dates
        EntityIndexUtils.addCreation(indexDoc, entry.getCreation(), fieldsOnly);
        EntityIndexUtils.addModification(indexDoc, entry.getModification(), fieldsOnly);
        
        //Add a field indicating if the entry is reserved
        EntityIndexUtils.addReserved(indexDoc, entry, fieldsOnly);
 
        // Add document type
        BasicIndexUtils.addDocType(indexDoc, Constants.DOC_TYPE_ENTRY, fieldsOnly);

        if (binder.isLibrary()) {
            Field libraryField = FieldFactory.createFieldStoredNotAnalyzed(Constants.IS_LIBRARY_FIELD, Boolean.toString(true));
            indexDoc.add(libraryField);
        }

        // Add the events - special indexing for calendar view
        EntityIndexUtils.addEvents(indexDoc, entry, fieldsOnly);
        
        // Add the tags for this entry
        if (tags == null) tags =  getCoreDao().loadAllTagsByEntity(entry.getEntityIdentifier());
        EntityIndexUtils.addTags(indexDoc, entry, tags, fieldsOnly);
                
        // Add attached files to entry only
        EntityIndexUtils.addAttachedFileIds(indexDoc, entry, fieldsOnly);
        
    	SimpleProfiler.stop("buildIndexDocumentFromEntry");
        return indexDoc;
    }
    protected org.apache.lucene.document.Document buildIndexDocumentFromEntryFile
	(Binder binder, Entry entry, FileAttachment fa, FileUploadItem fui, Collection tags) {
    	SimpleProfiler.start("buildIndexDocumentFromEntryFile");
    	org.apache.lucene.document.Document indexDoc = new org.apache.lucene.document.Document();
    	//do common part first. Indexing a file overrides some values
    	fillInIndexDocWithCommonPartFromEntry(indexDoc, binder, entry, true);
        BasicIndexUtils.addAttachmentType(indexDoc, Constants.ATTACHMENT_TYPE_ENTRY, true);
  		buildIndexDocumentFromFile(indexDoc, binder, entry, fa, fui, tags, binder.isLibrary());
    	SimpleProfiler.stop("buildIndexDocumentFromEntryFile");
   		return indexDoc;
 
    }

    /**
     * Fill in the Lucene document object with information that is common between
     * entry doc and attachment docs. We duplicate data so that we won't have to
     * perform multiple queries or run our own filtering in multiple steps. 
     * 
     * @param indexDoc
     * @param binder
     * @param entry
     */
    protected void fillInIndexDocWithCommonPartFromEntry(org.apache.lucene.document.Document indexDoc, 
    		Binder binder, Entry entry, boolean fieldsOnly) {
      	EntityIndexUtils.addEntryType(indexDoc, entry, fieldsOnly);       
        // Add ACL field. We only need to index ACLs for read access.
   		EntityIndexUtils.addReadAccess(indexDoc, binder, entry, fieldsOnly);
      		
        EntityIndexUtils.addParentBinder(indexDoc, entry, fieldsOnly);

        // Add the workflows if any
        EntityIndexUtils.addWorkflow(indexDoc, entry, fieldsOnly);
        fillInIndexDocWithCommonPart(indexDoc, binder, entry, fieldsOnly);
    }
    

    @Override
	public ChangeLog processChangeLog(DefinableEntity entry, String operation) {
		return processChangeLog(entry, operation, true);
	}
	public ChangeLog processChangeLog(DefinableEntity entry, String operation, boolean saveIt) {
		if (entry instanceof Binder) return processChangeLog((Binder)entry, operation);
		ChangeLog changes = new ChangeLog(entry, operation);
		ChangeLogUtils.buildLog(changes, entry);
		if (saveIt) getCoreDao().save(changes);
		return changes;
	}

}
