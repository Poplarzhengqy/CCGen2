/**
 * Copyright (c) 1998-2009 Novell, Inc. and its licensors. All rights reserved.
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
 * (c) 1998-2009 Novell, Inc. All Rights Reserved.
 * 
 * Attribution Information:
 * Attribution Copyright Notice: Copyright (c) 1998-2009 Novell, Inc. All Rights Reserved.
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
package org.kablink.teaming.module.file.impl;

import static org.kablink.util.search.Restrictions.conjunction;
import static org.kablink.util.search.Restrictions.eq;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.crypto.SecretKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.Query;
import org.dom4j.Element;
import org.kablink.teaming.BinderQuotaException;
import org.kablink.teaming.DataQuotaException;
import org.kablink.teaming.FileSizeLimitException;
import org.kablink.teaming.InternalException;
import org.kablink.teaming.ObjectKeys;
import org.kablink.teaming.UncheckedIOException;
import org.kablink.teaming.UserQuotaException;
import org.kablink.teaming.context.request.RequestContext;
import org.kablink.teaming.context.request.RequestContextHolder;
import org.kablink.teaming.dao.util.FilterControls;
import org.kablink.teaming.dao.util.ObjectControls;
import org.kablink.teaming.domain.Attachment;
import org.kablink.teaming.domain.Binder;
import org.kablink.teaming.domain.ChangeLog;
import org.kablink.teaming.domain.CustomAttribute;
import org.kablink.teaming.domain.DefinableEntity;
import org.kablink.teaming.domain.Definition;
import org.kablink.teaming.domain.Description;
import org.kablink.teaming.domain.Entry;
import org.kablink.teaming.domain.FileAttachment;
import org.kablink.teaming.domain.FileItem;
import org.kablink.teaming.domain.Folder;
import org.kablink.teaming.domain.FolderEntry;
import org.kablink.teaming.domain.HistoryStamp;
import org.kablink.teaming.domain.NoUserByTheIdException;
import org.kablink.teaming.domain.Principal;
import org.kablink.teaming.domain.Reservable;
import org.kablink.teaming.domain.ReservedByAnotherUserException;
import org.kablink.teaming.domain.TitleException;
import org.kablink.teaming.domain.User;
import org.kablink.teaming.domain.UserPrincipal;
import org.kablink.teaming.domain.VersionAttachment;
import org.kablink.teaming.domain.ZoneConfig;
import org.kablink.teaming.domain.EntityIdentifier.EntityType;
import org.kablink.teaming.domain.FileAttachment.FileLock;
import org.kablink.teaming.domain.FileAttachment.FileStatus;
import org.kablink.teaming.fi.connection.ResourceDriverManager;
import org.kablink.teaming.lucene.Hits;
import org.kablink.teaming.module.admin.AdminModule;
import org.kablink.teaming.module.binder.BinderModule;
import org.kablink.teaming.module.binder.BinderModule.BinderOperation;
import org.kablink.teaming.module.definition.DefinitionUtils;
import org.kablink.teaming.module.file.ChecksumMismatchException;
import org.kablink.teaming.module.file.ContentFilter;
import org.kablink.teaming.module.file.ConvertedFileModule;
import org.kablink.teaming.module.file.DeleteVersionException;
import org.kablink.teaming.module.file.FileIndexData;
import org.kablink.teaming.module.file.FileModule;
import org.kablink.teaming.module.file.FilesErrors;
import org.kablink.teaming.module.file.FilterException;
import org.kablink.teaming.module.file.LockIdMismatchException;
import org.kablink.teaming.module.file.LockedByAnotherUserException;
import org.kablink.teaming.module.folder.FolderModule;
import org.kablink.teaming.module.folder.FolderModule.FolderOperation;
import org.kablink.teaming.module.impl.CommonDependencyInjection;
import org.kablink.teaming.module.profile.ProfileModule;
import org.kablink.teaming.module.profile.ProfileModule.ProfileOperation;
import org.kablink.teaming.module.shared.ChangeLogUtils;
import org.kablink.teaming.module.shared.FileUtils;
import org.kablink.teaming.module.shared.FolderUtils;
import org.kablink.teaming.relevance.Relevance;
import org.kablink.teaming.repository.RepositoryServiceException;
import org.kablink.teaming.repository.RepositorySession;
import org.kablink.teaming.repository.RepositorySessionFactory;
import org.kablink.teaming.repository.RepositorySessionFactoryUtil;
import org.kablink.teaming.repository.RepositoryUtil;
import org.kablink.teaming.repository.archive.ArchiveStore;
import org.kablink.teaming.search.LuceneReadSession;
import org.kablink.teaming.search.QueryBuilder;
import org.kablink.teaming.search.SearchObject;
import org.kablink.teaming.security.AccessControlException;
import org.kablink.teaming.util.ExtendedMultipartFile;
import org.kablink.teaming.util.FileHelper;
import org.kablink.teaming.util.FilePathUtil;
import org.kablink.teaming.util.FileStore;
import org.kablink.teaming.util.FileUploadItem;
import org.kablink.teaming.util.GangliaMonitoring;
import org.kablink.teaming.util.ReflectHelper;
import org.kablink.teaming.util.SPropsUtil;
import org.kablink.teaming.util.SimpleMultipartFile;
import org.kablink.teaming.util.SimpleProfiler;
import org.kablink.teaming.util.SizeMd5Pair;
import org.kablink.teaming.util.SpringContextUtil;
import org.kablink.teaming.web.util.MiscUtil;
import org.kablink.util.KeyValuePair;
import org.kablink.util.Validator;
import org.kablink.util.search.Constants;
import org.kablink.util.search.Criteria;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.FileCopyUtils;


/**
 * This implementing class utilizes transactional demarcation strategies that 
 * are finer granularity than typical module implementations, in an effort to
 * avoid lengthy transaction duration that could have occured if the actual
 * file update operation was made during the transaction. To support that,
 * the public methods exposed by this implementation are not transaction 
 * demarcated. Instead, this implementation uses helper methods (defined in
 * another collaborator object) for the part of the operations that might
 * require interaction with the database (that is, those operations that 
 * change the state of one or more domain objects) and put the transactional 
 * support around those methods hence reducing individual transaction duration.
 * Of course, this finer granularity transactional control will be of no effect
 * if the caller of this service was already transactional (i.e., it controls
 * transaction bounrary that is more coarse). Whenever possible, this practise 
 * is discouraged for obvious performance/scalability reasons.  
 * 
 * @author jong
 *
 */
public class FileModuleImpl extends CommonDependencyInjection implements FileModule, InitializingBean {

	private static final String FAILED_FILTER_FILE_DELETE 			= "delete";
	private static final String FAILED_FILTER_FILE_MOVE 			= "move";
	private static final String FAILED_FILTER_FILE_DEFAULT			= FAILED_FILTER_FILE_DELETE;
	private static final String FAILED_FILTER_TRANSACTION_CONTINUE 	= "continue";
	private static final String FAILED_FILTER_TRANSACTION_ABORT 	= "abort";
	private static final String FAILED_FILTER_TRANSACTION_DEFAULT	= FAILED_FILTER_TRANSACTION_ABORT;
	private static final Long 	MEGABYTES							= 1024L * 1024L;
		
	protected Log logger = LogFactory.getLog(getClass());

	private TransactionTemplate transactionTemplate;
	private ContentFilter[] contentFilters; // may be null
	private String failedFilterFile;
	private String failedFilterTransaction;
	private int lockExpirationAllowance; // number of seconds
	private FileStore cacheFileStoreText;
	private FileStore cacheFileStoreImage;
	private FileStore cacheFileStoreHtml;
	private String[] ooNonzerolengthExts;
		
	protected TransactionTemplate getTransactionTemplate() {
		return transactionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	public void setLockExpirationAllowance(int lockExpirationAllowance) {
		this.lockExpirationAllowance = lockExpirationAllowance;
	}
	
	protected int getLockExpirationAllowanceMilliseconds() {
		return this.lockExpirationAllowance * 1000;
	}
	
	protected String getFailedFilterFile() {
		return failedFilterFile;
	}
	
	protected AdminModule getAdminModule() {
		// Can't use IoC due to circular dependency
		return (AdminModule) SpringContextUtil.getBean("adminModule");
	}

	protected BinderModule getBinderModule() {
		// Can't use IoC due to circular dependency
		return (BinderModule) SpringContextUtil.getBean("binderModule");
	}

	protected FolderModule getFolderModule() {
		// Can't use IoC due to circular dependency
		return (FolderModule) SpringContextUtil.getBean("folderModule");
	}

	protected ProfileModule getProfileModule() {
		// Can't use IoC due to circular dependency
		return (ProfileModule) SpringContextUtil.getBean("profileModule");
	}

	protected ConvertedFileModule getConvertedFileModule() {
		// Can't use IoC due to circular dependency
		return (ConvertedFileModule) SpringContextUtil.getBean("convertedFileModule");
	}

	protected void initFailedFilterFile() {
		String failedFilterFile = SPropsUtil.getString("file.content.filter.failed.filter.file", "");
		
		if(failedFilterFile.equals("")) {
			this.failedFilterFile = FAILED_FILTER_FILE_DEFAULT;
		}
		else if(FAILED_FILTER_FILE_DELETE.equalsIgnoreCase(failedFilterFile)) {
			this.failedFilterFile = FAILED_FILTER_FILE_DELETE;
		}
		else if(FAILED_FILTER_FILE_MOVE.equalsIgnoreCase(failedFilterFile)) {
			this.failedFilterFile = FAILED_FILTER_FILE_MOVE;
		}
		else {
			logger.info("Unknown value " + failedFilterFile + 
					" for property file.content.filter.failed.filter.file: Using default value " 
					+ FAILED_FILTER_FILE_DEFAULT);
			this.failedFilterFile = FAILED_FILTER_FILE_DEFAULT;
		}
	}
	
	protected String getFailedFilterTransaction() {
		return failedFilterTransaction;
	}
	
	protected void initFailedFilterTransaction() {
		String failedFilterTransaction = SPropsUtil.getString("file.content.filter.failed.filter.transaction", "");
		
		if(failedFilterTransaction.equals("")) {
			this.failedFilterTransaction = FAILED_FILTER_TRANSACTION_DEFAULT;			
		}
		else if(FAILED_FILTER_TRANSACTION_CONTINUE.equals(failedFilterTransaction)) {
			this.failedFilterTransaction = FAILED_FILTER_TRANSACTION_CONTINUE;
		}
		else if(FAILED_FILTER_TRANSACTION_ABORT.equals(failedFilterTransaction)) {
			this.failedFilterTransaction = FAILED_FILTER_TRANSACTION_ABORT;
		}
		else {
			logger.info("Unknown value " + failedFilterTransaction +
					" for property file.content.filter.failed.filter.transaction: Using default value "
					+ FAILED_FILTER_TRANSACTION_DEFAULT);
			this.failedFilterTransaction = FAILED_FILTER_TRANSACTION_DEFAULT;
		}
	}
		
	protected ContentFilter[] getContentFilters() {
		return contentFilters;
	}
	
	protected void initContentFilter() throws Exception {
		String[] contentFilterClassNames = SPropsUtil.getStringArray("file.content.filter.classes", ",");
		if(contentFilterClassNames != null && contentFilterClassNames.length > 0) {
			contentFilters = new ContentFilter[contentFilterClassNames.length];
			for(int i = 0; i < contentFilterClassNames.length; i++) {
				contentFilters[i] = (ContentFilter) ReflectHelper.getInstance(contentFilterClassNames[i]);
			}
		}		
		initFailedFilterFile();
		
		initFailedFilterTransaction();		
	}
	
	public void afterPropertiesSet() throws Exception {
		cacheFileStoreText = new FileStore(SPropsUtil.getString("cache.file.store.dir"), ObjectKeys.CONVERTER_DIR_TEXT);
		cacheFileStoreHtml = new FileStore(SPropsUtil.getString("cache.file.store.dir"), ObjectKeys.CONVERTER_DIR_HTML);
		cacheFileStoreImage = new FileStore(SPropsUtil.getString("cache.file.store.dir"), ObjectKeys.CONVERTER_DIR_IMAGE);
		
		ooNonzerolengthExts = SPropsUtil.getStringArray("openoffice.nonzerolength.extensions", ",");
		
		initContentFilter();	
	}

	public FilesErrors deleteFiles(final Binder binder, 
			final DefinableEntity entry, boolean deleteMirroredSource, 
			FilesErrors errors) {
		if(errors == null)
			errors = new FilesErrors();
		
		boolean updateMetadata = false;
		List<ChangeLog> changeLogs = new ArrayList<ChangeLog>();
		Collection<FileAttachment> fAtts = entry.getFileAttachments();
		if (!fAtts.isEmpty()) {
			for(FileAttachment fAtt :fAtts) {

				try {
					ChangeLog changeLog = deleteFileInternal(binder, entry, fAtt, 
							deleteMirroredSource, errors, updateMetadata);
					if(changeLog != null)
						changeLogs.add(changeLog);
				}
				catch(Exception e) {
					logger.error("Error deleting file " + fAtt.getFileItem().getName(), e);
					errors.addProblem(new FilesErrors.Problem
    					(fAtt.getRepositoryName(),  fAtt.getFileItem().getName(), 
    							FilesErrors.Problem.OTHER_PROBLEM, e));
				}
			}
		} else {
			updateMetadata = true; //nothing to update, but want directory deleted
		}
		
		String entityPath = FilePathUtil.getEntityDirPath(binder, entry);
		try {
			cacheFileStoreText.deleteDirectory(entityPath);
		}
		catch(Exception e) {
			logger.error("Error deleting the entry's cache directory [" +
					cacheFileStoreText.getAbsolutePath(entityPath) + "]", e);
		}
		try {
			cacheFileStoreHtml.deleteDirectory(entityPath);
		}
		catch(Exception e) {
			logger.error("Error deleting the entry's cache directory [" +
					cacheFileStoreHtml.getAbsolutePath(entityPath) + "]", e);
		}
		try {
			cacheFileStoreImage.deleteDirectory(entityPath);
		}
		catch(Exception e) {
			logger.error("Error deleting the entry's cache directory [" +
					cacheFileStoreImage.getAbsolutePath(entityPath) + "]", e);
		}
		
		if(!updateMetadata) {
			// Since there was no in-line transaction for updating metadata,
			// we must run a separate transaction to record the change logs. 
			// The following call also ensures that, even in the situation where
			// the operation was not entirely successful, we still reflect the
			// correponding metadata changes back to the database. 
			writeDeleteChangeLogTransactional(changeLogs);
		}
				
		return errors;
	}
	
	public void deleteCachedFiles(Binder binder, DefinableEntity entry) {
		String entityPath = FilePathUtil.getEntityDirPath(binder, entry);
		try {
			cacheFileStoreText.deleteDirectory(entityPath);
		}
		catch(Exception e) {
			logger.error("Error deleting the entry's cache directory [" +
					cacheFileStoreText.getAbsolutePath(entityPath) + "]", e);
		}
		try {
			cacheFileStoreHtml.deleteDirectory(entityPath);
		}
		catch(Exception e) {
			logger.error("Error deleting the entry's cache directory [" +
					cacheFileStoreHtml.getAbsolutePath(entityPath) + "]", e);
		}
		try {
			cacheFileStoreImage.deleteDirectory(entityPath);
		}
		catch(Exception e) {
			logger.error("Error deleting the entry's cache directory [" +
					cacheFileStoreImage.getAbsolutePath(entityPath) + "]", e);
		}
	}
	
	public FilesErrors deleteFile(Binder binder, DefinableEntity entry,
			FileAttachment fAtt, FilesErrors errors) {
		if(errors == null)
			errors = new FilesErrors();
		
		try {
			deleteFileInternal(binder, entry, fAtt, true, errors, true);
		}
		catch(Exception e) {
			logger.error("Error deleting file " + fAtt.getFileItem().getName(), e);
			errors.addProblem(new FilesErrors.Problem
					(fAtt.getRepositoryName(),  fAtt.getFileItem().getName(), 
							FilesErrors.Problem.OTHER_PROBLEM, e));
			//make sure any updates that happened get recored
			triggerUpdateTransaction(null);
		}
				
		String faPath = FilePathUtil.getFileAttachmentDirPath(binder, entry, fAtt);
		try {
			cacheFileStoreText.deleteDirectory(faPath);
		}
		catch(Exception e) {
			logger.error("Error deleting the file attachment's cache directory [" +
					cacheFileStoreText.getAbsolutePath(faPath) + "]", e);
		}
		try {
			cacheFileStoreHtml.deleteDirectory(faPath);
		}
		catch(Exception e) {
			logger.error("Error deleting the file attachment's cache directory [" +
					cacheFileStoreHtml.getAbsolutePath(faPath) + "]", e);
		}
		try {
			cacheFileStoreImage.deleteDirectory(faPath);
		}
		catch(Exception e) {
			logger.error("Error deleting the file attachment's cache directory [" +
					cacheFileStoreImage.getAbsolutePath(faPath) + "]", e);
		}
		//Mark that this entity was modified
		setEntityModification(entry);
		
		return errors;
	}
	
	public void readFile(Binder binder, DefinableEntity entry, FileAttachment fa, 
			OutputStream out) {
		String versionName = null;
		String latestVersionName = null;
		
		if(fa instanceof VersionAttachment) {
			versionName = ((VersionAttachment) fa).getVersionName();
		}
		else {
			if(fa.getHighestVersion() != null) {
				if(fa.getFileLock() == null)
					versionName = fa.getHighestVersion().getVersionName();
				else
					latestVersionName = fa.getHighestVersion().getVersionName();				
			}
			else {
				// There is no content to read.
				return;
			}
		}
		
		RepositoryUtil.readVersionedFile(fa, binder, entry, versionName, latestVersionName, out);	
		
		GangliaMonitoring.incrementFileReads();
	}
	
	public InputStream readFile(Binder binder, DefinableEntity entry, FileAttachment fa) { 
		String versionName = null;
		String latestVersionName = null;
		
		if(fa instanceof VersionAttachment) {
			versionName = ((VersionAttachment) fa).getVersionName();
		}
		else {
			if(fa.getHighestVersion() != null) {
				if(fa.getFileLock() == null)
					versionName = fa.getHighestVersion().getVersionName();
				else
					latestVersionName = fa.getHighestVersion().getVersionName();
			}
			else {
				// There is no content to read.
				return new ByteArrayInputStream(new byte[0]);
			}
		}
		
		InputStream result = RepositoryUtil.readVersionedFile(fa, binder, entry, versionName, latestVersionName, false);
		
		GangliaMonitoring.incrementFileReads();
		
		return result;
	}
	
    public FilesErrors writeFiles(Binder binder, DefinableEntity entry, 
    		List fileUploadItems, FilesErrors errors) 
    	throws ReservedByAnotherUserException {
		return writeFiles(binder, entry, fileUploadItems, errors, Boolean.TRUE);
    }
    
	private FilesErrors writeFiles(Binder binder, DefinableEntity entry, 
    		List fileUploadItems, FilesErrors errors, Boolean prune) 
    	throws ReservedByAnotherUserException {
		if(errors == null)
    		errors = new FilesErrors();
    	
    	// Read-write operations require up-to-date view of the lock and
    	// reservation state. So we must take care of expired locks first
    	// (if any). This runs in its own transaction.
    	//closeExpiredLocksTransactional(binder, entry, true);
    	
    	checkReservation(entry);
    	
    	for (int i = 0; i < fileUploadItems.size();) {
    		FileUploadItem fui = (FileUploadItem) fileUploadItems.get(i);
    		try {
    			// Unlike deleteFileInternal, writeFileTransactional is transactional.
    			// See the comment in writeFileMetadataTransactional for reason. 
    			if (this.writeFileTransactional(binder, entry, fui, errors)) {
    				//	only advance on success
    				++i;
    				GangliaMonitoring.incrementFileWrites();
    			} else {//error handled
	    			if (fui.isRegistered()) getCoreDao().unRegisterFileName(binder, fui.getOriginalFilename());
    				fileUploadItems.remove(i);
    			}
    		} catch (TitleException lx) {
    			//pass up
    			throw lx;
    		}
    		catch(Exception e) {
    			if (fui.isRegistered()) getCoreDao().unRegisterFileName(binder, fui.getOriginalFilename());
    			logger.error("Error writing file " + fui.getOriginalFilename(), e);
    			errors.addProblem(new FilesErrors.Problem
    					(fui.getRepositoryName(),  fui.getOriginalFilename(), 
    							FilesErrors.Problem.OTHER_PROBLEM, e));
    			fileUploadItems.remove(i);
    		}
    	}
		//Now see if any versions need to be pruned
    	if (prune) {
    		//Go prune the minor versions
    		pruneFileVersions(binder, entry);
    	}
    	//Set the agingEnabled flag on all versions subject to aging
    	FileUtils.setFileVersionAging(entry);
    	
    	// Because writeFileTransactional itself is transactional, we do not trigger
    	// another transaction here. 
		
		return errors;
    }
	
	// No transaction - The caller will manage transaction demarcation
	@Override
    public FileAttachment _addNetFolderFileInSync(Folder folder, FolderEntry entry, FileUploadItem fui) 
    throws UncheckedIOException {
    	FileAttachment fAtt = createFileAttachment(entry, fui);
    	String versionName;
		try {
			versionName = createVersionedFile(null, folder, entry, fui);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
    	long fileSize = fui.getCallerSpecifiedContentLength();
    	fAtt.getFileItem().setLength(fileSize);
    	createVersionAttachment(fAtt, versionName);
    	getCoreDao().save(fAtt);
    	
    	writeFileMetadataNonTransactional(folder, entry, fui, fAtt, true, true);
    	
    	GangliaMonitoring.incrementFileWrites();
    	
    	return fAtt;
    }
    
	public FilesErrors writeFilesValidationOnly(Binder binder, DefinableEntity entry, 
    		List fileUploadItems, FilesErrors errors) 
    	throws ReservedByAnotherUserException {
		if(errors == null)
    		errors = new FilesErrors();
    	
    	checkReservation(entry);
    	
    	for (int i = 0; i < fileUploadItems.size();) {
    		FileUploadItem fui = (FileUploadItem) fileUploadItems.get(i);
    		try {
    			if (this.writeFileValidationOnly(binder, entry, fui, errors)) {
    				//	only advance on success
    				++i;
    			} else {//error handled
    				fileUploadItems.remove(i);
    			}
    		}
    		catch(Exception e) {
    			errors.addProblem(new FilesErrors.Problem
    					(fui.getRepositoryName(),  fui.getOriginalFilename(), 
    							FilesErrors.Problem.OTHER_PROBLEM, e));
    			fileUploadItems.remove(i);
    		}
    	}
		
		return errors;
    }
	
	public void pruneFileVersions(Binder binder, DefinableEntity entry) {
		Long maxVersions = getBinderModule().getBinderVersionsToKeep(binder);
		pruneFileVersions(binder, entry, maxVersions);
	}
	
	public void pruneFileVersions(Binder binder, DefinableEntity entry, Long maxVersions) {
    	if (maxVersions != null) {
	    	Collection<FileAttachment> atts = entry.getFileAttachments();
	    	for (FileAttachment fa : atts) {
				Integer currentMajorVersion = -1;
				int minorVersionsSeen = 0;
        		Set<VersionAttachment> fileVersions = fa.getFileVersions();
        		for (VersionAttachment va : fileVersions) {
					//Is this version in the same major version category?
        			if (va.getMajorVersion() != currentMajorVersion) {
        				//This is a new major version category, reset the counters
        				currentMajorVersion = va.getMajorVersion();
        				minorVersionsSeen = 0;
        			}
        			if (minorVersionsSeen > maxVersions) {
        				//This version is over the number to be kept, so delete it
        				try {
        					deleteVersion(binder, entry, va);
        				} catch(Exception e) {}
        			}
        			minorVersionsSeen++;
				}
	    	}
    	}
	}
    
    protected void executeContentFilters(Binder binder, DefinableEntity entity, String fileName, FileUploadItem fui)
    throws IOException, FilterException, UncheckedIOException {
    	InputStream is;
    	for(int i = 0; i < contentFilters.length; i++) {
    		is = fui.getInputStream();
    		long begin = System.nanoTime();
    		try {
    			contentFilters[i].filter(binder, entity, fileName, is);
    		}
    		finally {
    			endFiltering(begin, fileName, contentFilters[i]);
    			try {
    				is.close();
    			}
    			catch(IOException ignore) {}
    		}
    	}
    }
    
	private void endFiltering(long begin, String fileName, ContentFilter filter) {
		if(debugEnabled) {
			double diff = (System.nanoTime() - begin)/1000000.0;
			logger.debug(diff + " ms, " + fileName + " filtered with " + filter.getClass().getSimpleName());
		}	
	}

    public FilesErrors verifyCheckSums(List fileUploadItems) {
        FilesErrors errors = null;
        // Note that we do not have to use String comparison in the expression
        // below. Just reference comparison is enough.
        if(getFailedFilterTransaction() == FAILED_FILTER_TRANSACTION_CONTINUE) {
            errors = new FilesErrors();
        }

        for(int i = 0; i < fileUploadItems.size();) {
            FileUploadItem fui = (FileUploadItem) fileUploadItems.get(i);
            if(fui.calledByFileSync()) {
            	if(logger.isDebugEnabled())
            		logger.debug("Skipping checksum verification on file " + fui.getOriginalFilename());
            	i++;
            	continue;
            }
            
            try {
                SimpleProfiler.start("verifyCheckSums_makeReentrant");
                fui.makeReentrant();
                SimpleProfiler.stop("verifyCheckSums_makeReentrant");

                if (!fui.verifyCheckSum()) {
                    // Remove the failed file from the list first.
                    fileUploadItems.remove(i);
                    try {
                        fui.delete();
                    } catch (IOException e1) {
                        logger.error("Failed to delete bad file " + fui.getOriginalFilename(), e1);
                    }
                    if(errors != null) {
                        // Since we are not throwing an exception immediately in
                        // this case, log the error right here.
                        logger.error("Error verifying the file " + fui.getOriginalFilename() + " check sum for user " +  RequestContextHolder.getRequestContext().toString());
                        errors.addProblem(new FilesErrors.Problem
                            (fui.getRepositoryName(),  fui.getOriginalFilename(),
                                    FilesErrors.Problem.PROBLEM_CHECKSUM_MISMATCH));
                    } else {
                        throw new ChecksumMismatchException(fui.getName());
                    }
                } else {
                    ++i;
                }
            }
            catch(IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        if(errors == null)
            errors = new FilesErrors();

        return errors;
    }

	public FilesErrors filterFiles(Binder binder, DefinableEntity entity, List fileUploadItems) 
		throws FilterException {
		if(contentFilters == null)
			return new FilesErrors(); // Nothing to filter with. Return empty error.

		FilesErrors errors = null;
		// Note that we do not have to use String comparison in the expression
		// below. Just reference comparison is enough. 
		if(getFailedFilterTransaction() == FAILED_FILTER_TRANSACTION_CONTINUE) {
			errors = new FilesErrors();
		}
		
    	for(int i = 0; i < fileUploadItems.size();) {
    		FileUploadItem fui = (FileUploadItem) fileUploadItems.get(i);
            if(fui.calledByFileSync()) {
            	if(logger.isDebugEnabled())
            		logger.debug("Skipping filters on file " + fui.getOriginalFilename());
            	i++;
            	continue;
            }
    		try {
    			SimpleProfiler.start("filterFiles_makeReentrant");
        		fui.makeReentrant();
    			SimpleProfiler.stop("filterFiles_makeReentrant");

    			SimpleProfiler.start("filterFiles_executeContentFilters");
        		executeContentFilters(binder, entity, fui.getOriginalFilename(), fui);
    			SimpleProfiler.stop("filterFiles_executeContentFilters");
    			
    			//Only advance on success
    			++i;
    		}
    		catch(IOException e) {
    			throw new UncheckedIOException(e);
    		}
    		catch(FilterException e) {
    			if(errors != null) {
    				// Since we are not throwing an exception immediately in 
    				// this case, log the error right here. 
    				logger.error("Error filtering file " + fui.getOriginalFilename() + " for user " +  RequestContextHolder.getRequestContext().toString() + org.kablink.teaming.util.Constants.NEWLINE + e.toString());
    			}
    			// Remove the failed file from the list first. 
    			fileUploadItems.remove(i);
    			if(getFailedFilterFile() == FAILED_FILTER_FILE_DELETE) {
    				try {
						fui.delete();
					} catch (IOException e1) {
						logger.error("Failed to delete bad file " + fui.getOriginalFilename(), e1);
					}
    			}
    			else {
    				try {
    					moveFilterFailedFile(binder, fui);
					} catch (IOException e1) {
						logger.error("Failed to move bad file " + fui.getOriginalFilename(), e1);
					}
    			}
    			if(errors != null) {
        			if (fui.isRegistered()) getCoreDao().unRegisterFileName(binder, fui.getOriginalFilename());
    				errors.addProblem(new FilesErrors.Problem
    					(fui.getRepositoryName(),  fui.getOriginalFilename(),
    							FilesErrors.Problem.PROBLEM_FILTERING, e));
    			}
    			else {
    				//clean all newly registered titles out
   					for (int j=0; j< fileUploadItems.size(); ++j) {
   						FileUploadItem fu = (FileUploadItem) fileUploadItems.get(j);
   		       			if (fu.isRegistered()) { 
 	    					getCoreDao().unRegisterFileName(binder, fu.getOriginalFilename());
   						}  		       		     	
    				}
    				throw e;
    			}
    		}
    	}
	
    	if(errors == null)
    		errors = new FilesErrors();
    	
    	return errors;
	}
	
	public void lock(Binder binder, DefinableEntity entity, FileAttachment fa, 
			String lockId, String lockSubject, Date expirationDate, String lockOwnerInfo)
		throws ReservedByAnotherUserException, 
			LockedByAnotherUserException, LockIdMismatchException, 
			UncheckedIOException, RepositoryServiceException {
		User user = RequestContextHolder.getRequestContext().getUser();
		
		// The following call is commented out because: We want to write a
		// bit more tolerating system where client (WebDAV client in this case)
		// can still renew a lock that has already expired. For that reason,
		// we do not want to close all expired locks here. 
    	//closeExpiredLocksTransactional(binder, entity, true);
    	
    	Reservable reservable = null;
    	HistoryStamp reservation = null;
    	if(entity instanceof Reservable) {
    		reservable = (Reservable) entity;
    		reservation = reservable.getReservation();
    	}
    	List newObjs = new ArrayList();
    	if(reservation == null || reservation.getPrincipal().equals(user)) {
    		// This is one of the following three conditions:
    		// 1) The entity is not reservable
    		// 2) The entity is reservable but not reserved by anyone
    		// 3) The entity is reservable and reserved by the calling user
    		
    		FileAttachment.FileLock lock = fa.getFileLock();
    		if(lock == null) { // The file is not locked
    			// Lock the file
    			fa.setFileLock(new FileAttachment.FileLock(lockId, lockSubject, 
    					user, expirationDate, lockOwnerInfo));
    		}
    		else { // The file is locked
    			if(lock.getOwner().equals(user)) {
    				// The lock is owned by the same user
        			if(lock.getId().equals(lockId)) { // Lock id matches
        				// Renew the lock
        				lock.setExpirationDate(expirationDate);
        			}
        			else { // Lock id does not match
        				throw new LockIdMismatchException();    				
        			}     		
    			}
    			else { // The lock is owned by another user
    				// Because we chose not to close expired locks above, we 
    				// must test for expired lock here. 
    				if(isLockExpired(lock)) { // The lock has expired
    					// Commit any pending changes associated with the expired lock
    					commitPendingChanges(binder, entity, fa, lock, newObjs); 
    					// Set the new lock.
    					fa.setFileLock(new FileLock(lockId, lockSubject, 
    							user, expirationDate, lockOwnerInfo));
    				}
    				else { // The lock is still effective
        				throw new LockedByAnotherUserException(entity, fa, lock.getOwner());    					
    				}
    			}
    		}
    	}
    	else {
    		// The entity is reservable and reserved by another user
    		throw new ReservedByAnotherUserException(reservable);
    	}
    	
    	triggerUpdateTransaction(newObjs);
	}

    public void unlock(Binder binder, DefinableEntity entity, FileAttachment fa,
    		String lockId) throws LockedByAnotherUserException, LockIdMismatchException,
    		UncheckedIOException, RepositoryServiceException {
		User user = RequestContextHolder.getRequestContext().getUser();
		
    	//closeExpiredLocksTransactional(binder, entity, true);
    	
		FileAttachment.FileLock lock = fa.getFileLock();

		if(lock != null) {
			if(lock.getOwner().equals(user)) {
				// The file is locked by the calling user.
				if(lock.getId().equals(lockId)) {
					// The lock id matches.
					List newObjs = new ArrayList();
					
					// Commit any pending changes associated with the lock. In this 
					// case, we don't care if the lock is effective or expired.
					commitPendingChanges(binder, entity, fa, lock, newObjs);
					
					fa.setFileLock(null); // Clear the lock
					
					triggerUpdateTransaction(newObjs);
				}
				else {
					// The lock id doesn't match.
					throw new LockIdMismatchException();
				}
			}
			else {
				// The lock is owned by someone else.
				throw new LockedByAnotherUserException(entity, fa, lock.getOwner());
			}
		}
		else {
			// The file isn't locked by anyone. Noop.
			logger.debug("not locked");
			return;
		}
    }

    public void forceUnlock(Binder binder, DefinableEntity entity, FileAttachment fa) 
    throws UncheckedIOException, RepositoryServiceException {
		FileAttachment.FileLock lock = fa.getFileLock();

		if(lock != null ) { // lock exists
			// Commit any pending changes associated with the lock. In this 
			// case, we don't care if the lock is effective or expired.
			List newObjs = new ArrayList();
			
			try {
				commitPendingChanges(binder, entity, fa, lock, newObjs);
			}
			catch(Exception e) {
				// Do not let any error in committing the pending changes to fail "forcible" unlocking of the file. 
				logger.error("Error during forcible unlock. Unlock will proceed despite of the error.", e);
			}
			
			fa.setFileLock(null); // Clear the lock
			
			triggerUpdateTransaction(newObjs);
		}
    }


	public void bringLocksUptodate(Binder binder, DefinableEntity entity) 
		throws RepositoryServiceException, UncheckedIOException {
		closeExpiredLocksTransactional(binder, entity, true);
	}
	
	public void revertFileVersion(DefinableEntity entity, VersionAttachment va) 
			throws UncheckedIOException, RepositoryServiceException, DataQuotaException {
		revertFileVersion(entity, va, Boolean.TRUE);
	}
	public void revertFileVersion(DefinableEntity entity, VersionAttachment va, Boolean prune) 
			throws UncheckedIOException, RepositoryServiceException, DataQuotaException {
		revertFileVersion(entity, va, Boolean.TRUE, ChangeLog.FILEMODIFY_REVERT);
	}
	public void revertFileVersion(DefinableEntity entity, VersionAttachment va, Boolean prune, String changeLogCause) 
			throws UncheckedIOException, RepositoryServiceException, DataQuotaException {
		//First, check to see if there is quota enough for this
		User user = RequestContextHolder.getRequestContext().getUser();
		checkQuota(user, va.getFileItem().getLength(), va.getFileItem().getName());
		checkBinderQuota(entity.getParentBinder(), va.getFileItem().getLength(), va.getFileItem().getName());

		Binder binder = entity.getParentBinder();
		if (EntityType.folder.equals(entity.getEntityType()) || 
				EntityType.workspace.equals(entity.getEntityType())) {
			binder = (Binder)entity;
		}
		VersionAttachment oldTopVa = va.getParentAttachment().getHighestVersion();

		List<FileUploadItem> fuis = new ArrayList<FileUploadItem>();
    	Collection<FileAttachment> atts = entity.getFileAttachments();
    	FileUploadItem fui;
     	String name;
    	SimpleMultipartFile file;
    	FileAttachment fa = va.getParentAttachment();
    	name = fa.getName(); 
    	int type = FileUploadItem.TYPE_FILE;
    	if (Validator.isNull(name)) type = FileUploadItem.TYPE_ATTACHMENT;
    			
		// Preserve modification time of the source for the target
		file = new ExtendedMultipartFile(va.getFileItem().getName(),
			readFile(binder, entity, va), va.getModification().getDate());
		fui = new FileUploadItem(type, name, file, va.getRepositoryName());
   		fuis.add(fui);

   		boolean prune2 = prune;
   		Long maxVersionsToKeep = getBinderModule().getBinderVersionsToKeep(binder);
   		if (maxVersionsToKeep != null && maxVersionsToKeep == 0) {
   			//This is a special case. We explicitly turn off pruning of versions 
   			//  so the user doesn't lose the original top version during this operation
   			prune2 = Boolean.FALSE;
   		}
    	try {	
    		writeFiles(binder, entity, fuis, null, prune2);
    	}
    	finally {
	    	for(FileUploadItem f : fuis) {
	    		try {
	    			f.delete();
	    		}
	    		catch(IOException ignore) {}
	    	}
    	}
    	if (prune && maxVersionsToKeep != null && maxVersionsToKeep == 0) {
    		//This is a special case. Make sure to keep at least one version
    		pruneFileVersions(binder, entity, maxVersionsToKeep + 1);
    	}
    	
    	//Copy up the status, comment and major version
    	VersionAttachment newTopVa = fa.getHighestVersion();
    	newTopVa.setFileStatus(va.getFileStatus());
    	newTopVa.getParentAttachment().setFileStatus(va.getFileStatus());
    	newTopVa.getFileItem().setDescription(va.getFileItem().getDescription());
    	newTopVa.getParentAttachment().setMajorVersion(newTopVa.getMajorVersion());
    	newTopVa.getParentAttachment().setMinorVersion(newTopVa.getMinorVersion());

    	getConvertedFileModule().deleteCacheHtmlFile(binder, entity, fa);
    	getConvertedFileModule().deleteCacheTextFile(binder, entity, fa);
    	getConvertedFileModule().deleteCacheImageFile(binder, entity, fa);
    	setEntityModification(entity);
    	entity.incrLogVersion();
    	ChangeLog changes = new ChangeLog(entity, changeLogCause);
		ChangeLogUtils.buildLog(changes, newTopVa.getParentAttachment());
		saveChangeLogTransactional(changes);
	}

	public void modifyFileComment(DefinableEntity entity, FileAttachment fileAtt, Description description) {
		fileAtt.getFileItem().setDescription(description);
		if (fileAtt instanceof FileAttachment) {
			VersionAttachment hVer = fileAtt.getHighestVersion();
			if (hVer != null && hVer.getParentAttachment() == fileAtt) {
				hVer.getFileItem().setDescription(description);
			}
		}
		setEntityModification(entity);
		entity.incrLogVersion();
		ChangeLog changes = new ChangeLog(entity, ChangeLog.FILEMODIFY_SET_COMMENT);
		ChangeLogUtils.buildLog(changes, fileAtt);
		saveChangeLogTransactional(changes);
	}
	
	public void modifyFileStatus(DefinableEntity entity, FileAttachment fileAtt, FileStatus fileStatus) {
		fileAtt.setFileStatus(FileStatus.valueOf(fileStatus));
		if (!(fileAtt instanceof VersionAttachment)) {
			VersionAttachment hVer = fileAtt.getHighestVersion();
			if (hVer != null && hVer.getParentAttachment() == fileAtt) {
				hVer.setFileStatus(FileStatus.valueOf(fileStatus));
			}
		}
		setEntityModification(entity);
		entity.incrLogVersion();
		ChangeLog changes = new ChangeLog(entity, ChangeLog.FILEMODIFY_SET_STATUS);
		ChangeLogUtils.buildLog(changes, fileAtt);
		saveChangeLogTransactional(changes);
	}
	
	public void incrementMajorFileVersion(DefinableEntity entity, FileAttachment fileAtt) {
		//First, make a copy of the higest version (if there is quota)
		revertFileVersion(entity, fileAtt.getHighestVersion(), Boolean.FALSE);
		fileAtt = fileAtt.getHighestVersion().getParentAttachment();
		fileAtt.setMajorVersion(fileAtt.getMajorVersion() + 1);
		fileAtt.setMinorVersion(0);
		fileAtt.setAgingEnabled(Boolean.FALSE);
		VersionAttachment hVer = fileAtt.getHighestVersion();
		if (hVer != null && hVer.getParentAttachment() == fileAtt) {
			hVer.setMajorVersion(fileAtt.getMajorVersion());
			hVer.setMinorVersion(fileAtt.getMinorVersion());
			hVer.setAgingEnabled(fileAtt.getAgingEnabled());
		}
    	Binder binder;
    	if (entity instanceof Entry) {
    		binder = ((Entry)entity).getParentBinder();
    	} else {
    		binder = (Binder) entity;
    	}
		pruneFileVersions(binder, entity);	//After all the work is finished, make sure to prune the versions
		FileUtils.setFileVersionAging(entity);	//Set the agingEnabled flag appropriately for all file attachments
		setEntityModification(entity);
		entity.incrLogVersion();
		ChangeLog changes = new ChangeLog(entity, ChangeLog.FILEMODIFY_INCR_MAJOR_VERSION);
		ChangeLogUtils.buildLog(changes, fileAtt);
		saveChangeLogTransactional(changes);
	}
	
	private void saveChangeLogTransactional(final ChangeLog changeLog) {
        getTransactionTemplate().execute(new TransactionCallback() {
        	public Object doInTransaction(TransactionStatus status) {  
                getCoreDao().save(changeLog);
            	return null;
        	}
        });	
	}
	
	public void renameFile(Binder binder, DefinableEntity entity, 
			FileAttachment fa, String newName) 
			throws UncheckedIOException, RepositoryServiceException {
		if(fileExistsInRepository(fa)) {
			// Rename the file in the repository
			RepositoryUtil.moveFile(fa.getRepositoryName(), binder, entity, 
					fa.getFileItem().getName(), binder, entity, newName);
		}
		else {
			// This use case is caused by Map Network Place on Windows 7.
			// When you create a new file through Map Network Place on Windows 7,
			// it first creates an empty file with the default name and then rename
			// it. In that case, we want the title of the entry to be the same as
			// the final name that user specifies, not the default name Windows 7
			// uses. 
			if(entity.getTitle().equals(fa.getFileItem().getName())) {
				entity.setTitle(newName);
			}
		}
		
		// Change our metadata - note that all that needs to change is the
		// file name. Other things such as mod date, etc., remain unchanged.
		//binder files are not registered
		if (binder.isLibrary() && !binder.equals(entity)) 
			getCoreDao().updateFileName(binder, entity, fa.getFileItem().getName(), newName);
        if ((entity.getEntryDefId() != null)  && DefinitionUtils.isSourceItem(entity.getEntryDefDoc(), fa.getName(), "title")) {
          	//if tracking unique titles, remove old title
        	String oldTitle = entity.getNormalTitle();
            //check title
        	entity.setTitle(newName);			   			   
           	if ((entity.getParentBinder() != null) && entity.getParentBinder().isUniqueTitles()) 
           		getCoreDao().updateTitle(entity.getParentBinder(), entity, oldTitle, entity.getNormalTitle());
        }
		fa.getFileItem().setName(newName);
		
		for(Iterator i = fa.getFileVersionsUnsorted().iterator(); i.hasNext();) {
			VersionAttachment v = (VersionAttachment) i.next();
			v.getFileItem().setName(newName);
		}
		entity.incrLogVersion();
		ChangeLog changes = new ChangeLog(entity, ChangeLog.FILERENAME);
		ChangeLogUtils.buildLog(changes, fa);
		getCoreDao().save(changes);
	}
	
	public void moveFiles(Binder binder, DefinableEntity entity, 
			Binder destBinder, DefinableEntity destEntity, String[] toFileNames)
	throws UncheckedIOException, RepositoryServiceException {
    	Collection<FileAttachment> atts = entity.getFileAttachments();
    	//Decrement and increment the various quota counts
		Long totalFileSizes = 0L;
    	for (FileAttachment att : atts) {
    		Set<VersionAttachment> fileVersions = att.getFileVersions();
    		for (VersionAttachment fv : fileVersions) {
    			//Count the file sizes for all versions in the entry
    			totalFileSizes += fv.getFileItem().getLength();
    		}
    	}
    	if (!binder.isMirrored()) {
    		getBinderModule().decrementDiskSpaceUsed(binder, totalFileSizes);
    	}
    	if (!destBinder.isMirrored()) {
    		getBinderModule().incrementDiskSpaceUsed(destBinder, totalFileSizes);
    	}

    	//first register file names, so if one fails, files are not copied
    	int i = 0;
    	String toFileName;
    	for(FileAttachment fa :atts) {
    		toFileName = (toFileNames != null)? toFileNames[i] : fa.getFileItem().getName();
   			if (binder.isLibrary() && !binder.equals(entity))
   				getCoreDao().unRegisterFileName(binder, fa.getFileItem().getName());
    		if (destBinder.isLibrary() && !destBinder.equals(destEntity))
    			getCoreDao().registerFileName(destBinder, destEntity, toFileName);
    		i++;
    	}
    	
		// Rename the file in the repository
    	i = 0;
       	for(FileAttachment fa :atts) {   
    		toFileName = (toFileNames != null)? toFileNames[i] : fa.getFileItem().getName();
       		if(!ObjectKeys.FI_ADAPTER.equals(fa.getRepositoryName())) { // regular repository
       	 		RepositoryUtil.moveFile(fa.getRepositoryName(), binder, entity, 
       					fa.getFileItem().getName(), destBinder, destEntity, 
       					toFileName);       			
       		}
       		else { // mirrored repository
       			moveMirroredFile(binder, entity, destBinder, destEntity, fa, toFileName);
       		}
   			ChangeLog changes = new ChangeLog(entity, ChangeLog.FILEMOVE);
			ChangeLogUtils.buildLog(changes, fa);
			getCoreDao().save(changes);
			// Now that we're done with the existing fa object (hence we don't need access to
			// the previous name), we can finally change its name to the new name if different.
			fa.getFileItem().setName(toFileName);
			i++;
       	}	
	}

	protected void moveMirroredFile(Binder binder, DefinableEntity entity, 
			Binder destBinder, DefinableEntity destEntity, FileAttachment fa, String toFileName) {
		if(binder.getResourceDriverName().equals(destBinder.getResourceDriverName())) {
			// Both source and destination binders use the same resource
			// driver. Move is possible.
   	 		RepositoryUtil.moveFile(fa.getRepositoryName(), binder, entity, 
   					fa.getFileItem().getName(), destBinder, destEntity, 
   					toFileName);       								
		}
		else {
			// Source and destination binders do not share the same driver. 
			// Move is not possible in this case. We have to mimic it by
			// copy followed by delete. 
			RepositorySession session = RepositorySessionFactoryUtil.openSession(fa.getRepositoryName(), destBinder.getResourceDriverName(), ResourceDriverManager.FileOperation.CREATE_FILE, destBinder);
			try {
				InputStream is = readFile(binder, entity, fa);
				long size = fa.getFileItem().getLength();
				try {
					createVersionedWithInputData(session, destBinder, destEntity, toFileName, true, is, size);
				}
				finally {
					try {
						is.close();
					}
					catch(IOException e) {
						logger.warn(e);
					}
				}
			}
			finally {
				session.close();
			}
			
			session = RepositorySessionFactoryUtil.openSession(fa.getRepositoryName(), binder.getResourceDriverName(), ResourceDriverManager.FileOperation.DELETE, binder);
			try {
				session.delete(binder, entity, fa.getFileItem().getName());
			}
			finally {
				session.close();
			}
		}
	}

	public void copyFiles(Binder binder, DefinableEntity entity, 
			Binder destBinder, DefinableEntity destEntity, String[] toFileNames)
	throws UncheckedIOException, RepositoryServiceException {
		List<FileUploadItem> fuis = new ArrayList<FileUploadItem>();
    	Collection<FileAttachment> atts = entity.getFileAttachments();
    	FileUploadItem fui;
     	String targetName;
     	String targetRepositoryName;
    	SimpleMultipartFile file;
    	int i=0;
    	for(FileAttachment fa :atts) {
    		targetName = fa.getName(); 
  			targetRepositoryName = fa.getRepositoryName();
  			if(ObjectKeys.FI_ADAPTER.equalsIgnoreCase(fa.getRepositoryName())) {
  				// This means that the source entity is a mirrored file entry.
  				if(!destBinder.isMirrored()) {
  					// The destination binder is not a mirrored folder. 
  					// We need to identify correct default data name and repository to use for the destination based on its definition.
  					Element item = FolderUtils.getDefinitionItemForNonMirroredFile(destEntity.getEntryDefDoc());
  					if(item != null) {
  						String itemName = item.attributeValue("name");
  						if("atttachFiles".equals(itemName)) {
  							targetName = null;
  						}
  						else {
  							targetName = DefinitionUtils.getPropertyValue(item, "name");
  						}
						targetRepositoryName = DefinitionUtils.getPropertyValue(item, "storage");
  					}
  					else {
  						targetName = null;
  						targetRepositoryName = RepositoryUtil.getDefaultRepositoryName();
  					}
  				}
  			} else {
  				//The source entity is not a mirrored file
  				if (destBinder.isMirrored()) {
  					//But the destination binder is mirrored. We have to get the right repository name
  					targetRepositoryName = ObjectKeys.FI_ADAPTER;
  				}
  			}
  			String toFileName = (toFileNames != null)? toFileNames[i] : fa.getFileItem().getName();
			// Preserve modification time of the source for the target
  			file = new ExtendedMultipartFile(toFileName,
				readFile(binder, entity, fa), fa.getModification().getDate());
    		int type = FileUploadItem.TYPE_FILE;
    		if(Validator.isNull(targetName))
			type = FileUploadItem.TYPE_ATTACHMENT;
			fui = new FileUploadItem(type, targetName, file, targetRepositoryName);
			//register here so entire copy fails if any one file is an issue
	   		if (destBinder.isLibrary() && !(destEntity instanceof Binder)) {
	   			getCoreDao().registerFileName(destBinder, destEntity, toFileName);
	   			fui.setRegistered(true);
	   		}
	   		fuis.add(fui);
	   		i++;
    	}
    	try {	
    		writeFiles(destBinder, destEntity, fuis, null);
    	}
    	finally {
	    	for(FileUploadItem f : fuis) {
	    		try {
	    			f.delete();
	    		}
	    		catch(IOException ignore) {}
	    	}
    	}
	}
	
	public void encryptVersion(Binder binder, final DefinableEntity entity, 
			final VersionAttachment va, FilesErrors errors) {
		
		//Take the versionAttachment and copy it to the front of the list encrypted.
		//Then delete the old one
		revertFileVersion(entity, va, false, ChangeLog.FILEMODIFY_ENCRYPT);
		deleteVersion(binder, entity, va);
	}

	public void deleteVersion(Binder binder, DefinableEntity entity, 
			VersionAttachment va) throws DeleteVersionException {
		//List<String> beforeVersionNames = RepositoryUtil.getVersionNames(va.getRepositoryName(), binder, entity, 
		//		va.getFileItem().getName());
		
		if (entity instanceof FolderEntry) {
			getFolderModule().checkAccess((FolderEntry)entity, FolderOperation.deleteEntry);
		} else if (entity instanceof Principal) {
			getProfileModule().checkAccess((Principal)entity, ProfileOperation.deleteEntry);
		} else {
			getBinderModule().checkAccess(binder, BinderOperation.deleteBinder);
		}
		
		// Check if the version is the only one remaining for the file. 
		FileAttachment fa = va.getParentAttachment();
		if(fa.getFileVersionsUnsorted().size() <= 1)
			throw new DeleteVersionException(va);
			
		// Delete the version from the repository if the repository allows
		// deletion of a version.
		RepositorySessionFactory rsf = 
			RepositorySessionFactoryUtil.getRepositorySessionFactory
			(fa.getRepositoryName());
		
		if(rsf.isVersionDeletionAllowed()) {		
			RepositoryUtil.deleteVersion(fa.getRepositoryName(), binder, entity, 
				va.getFileItem().getName(), va.getVersionName());
		}
		else {
			logger.info("Version " + va.getVersionNumber() + " of file [" + va.getFileItem().getName() + 
					"] is not physically deleted from repository " + fa.getRepositoryName() + 
					" because it does not allow deletion of a version");
		}

   		// Decrement disk space used for this version
   		ZoneConfig zoneConf = getCoreDao().loadZoneConfig(
				RequestContextHolder.getRequestContext()
				.getZoneId());
   		if (zoneConf.isDiskQuotaEnabled()) {
			User user = getProfileDao().loadUser(va.getCreation().getPrincipal().getId(), 
					RequestContextHolder.getRequestContext().getZoneName());
			user.decrementDiskSpaceUsed(va.getFileItem().getLength());
   		}
   		if (zoneConf.isBinderQuotaInitialized()) {
   			//Once the binder quotas have been initialized, we must keep track of the space used
   			//Decrement the space used all the way up the parent tree.
   			getBinderModule().decrementDiskSpaceUsed(binder, va.getFileItem().getLength());
   		}

   		// Update the metadata
		entity.incrLogVersion();
		ChangeLog changes = new ChangeLog(entity, ChangeLog.FILEVERSIONDELETE);
		ChangeLogUtils.buildLog(changes, va);

		fa.removeFileVersion(va);
		
		// Get the highest previous version
		VersionAttachment highestVa = (VersionAttachment) fa.getFileVersions().iterator().next();
		highestVa.setAgingEnabled(Boolean.FALSE);
		
		// Copy the last-modified date
		fa.setModification(highestVa.getModification());
		// Copy the file length and other values
		fa.setFileItem(highestVa.getFileItem());
		fa.setMajorVersion(highestVa.getMajorVersion());
		fa.setMinorVersion(highestVa.getMinorVersion());
		fa.setAgingEnabled(highestVa.getAgingEnabled());
		fa.setFileStatus(highestVa.getFileStatus());
		getConvertedFileModule().deleteCacheHtmlFile(binder, entity, fa);
		getConvertedFileModule().deleteCacheTextFile(binder, entity, fa);
    	getConvertedFileModule().deleteCacheImageFile(binder, entity, fa);
		
		// Since creation date is not really useful, we will leave it alone. 
		
		//List<String> afterVersionNames = RepositoryUtil.getVersionNames(va.getRepositoryName(), binder, entity, 
		//		va.getFileItem().getName());
		
		saveChangeLogTransactional(changes);
		//Mark that this entity was modified
		setEntityModification(entity); 
		FileUtils.setFileVersionAging(entity);
	}

	public Map<String,FileIndexData> getChildrenFileDataFromIndex(Long binderId) {
		// look for the specific binder id
    	// look only for attachments
    	Criteria crit = new Criteria()
    	    .add(conjunction()	
    			.add(eq(Constants.BINDER_ID_FIELD, binderId.toString()))
   				.add(eq(Constants.DOC_TYPE_FIELD,Constants.DOC_TYPE_ATTACHMENT))
     		);
		// We use search engine to get the list of file names in the specified folder.
        List<FileIndexData> results = getFileDataFromIndex(crit);
        Map<String, FileIndexData> resultMap = new HashMap<String, FileIndexData>();
        for (FileIndexData data : results) {
            resultMap.put(data.getName(), data);
        }
        return resultMap;
	}

    public List<FileIndexData> getFileDataFromIndex(Criteria crit) {
        QueryBuilder qb = new QueryBuilder(true, false);
        org.dom4j.Document qTree = crit.toQuery(); //save for debug
        SearchObject so = qb.buildQuery(qTree);

        // create Lucene query
        Query soQuery = so.getLuceneQuery();

        if(logger.isDebugEnabled()) {
            logger.debug("Query is: " + qTree.asXML());
            logger.debug("Query is: " + soQuery.toString());
        }

        LuceneReadSession luceneSession = getLuceneSessionFactory().openReadSession();

        Hits hits = null;
        try {
	        hits = luceneSession.search(RequestContextHolder.getRequestContext().getUserId(),
	        		so.getAclQueryStr(), Constants.SEARCH_MODE_NORMAL, soQuery, null, 0, Integer.MAX_VALUE);
        }
        finally {
            luceneSession.close();
        }

        List<FileIndexData> result = new ArrayList<FileIndexData>();
        int count = hits.length();
        org.apache.lucene.document.Document doc;
        String fileName;
        for(int i = 0; i < count; i++) {
        	doc = hits.doc(i);
        	fileName = doc.get(Constants.FILENAME_FIELD);
        	if(fileName != null) {
        		try {
	        		result.add(new FileIndexData(doc));
        		}
        		catch(Exception ignore) {
        			// skip to next doc
        			logger.warn("Skipping file '" + fileName + "' due to error in index data: " + ignore.toString());
        		}
        	}
        }

        return result;
    }

	public Map<String,Long> getChildrenFileNamesUsingDatabaseWithoutAccessCheck(Long binderId) {
		List<Object[]> objs = getCoreDao().loadObjects(
				new ObjectControls(FileAttachment.class, new String[]{"fileItem.name", "owner.ownerId"}),
				new FilterControls("owner.owningBinderId", binderId),
				RequestContextHolder.getRequestContext().getZoneId());
		Map<String,Long> result = new HashMap<String,Long>();
		for(Object[] obj:objs)
			result.put((String)obj[0], (Long)obj[1]);
		return result;
	}

	public Map<String,Long> getChildrenFileNamesUsingSearchIndex(Binder binder) {
		// look for the specific binder id
    	// look only for attachments
    	Criteria crit = new Criteria()
    	    .add(conjunction()	
    			.add(eq(Constants.BINDER_ID_FIELD, binder.getId().toString()))
   				.add(eq(Constants.DOC_TYPE_FIELD,Constants.DOC_TYPE_ATTACHMENT))
     		);
		// We use search engine to get the list of file names in the specified folder.
		QueryBuilder qb = new QueryBuilder(true,false);
    	org.dom4j.Document qTree = crit.toQuery(); //save for debug
		SearchObject so = qb.buildQuery(qTree);   	
   	
    	// create Lucene query    	
    	Query soQuery = so.getLuceneQuery();
    	    	
    	if(logger.isDebugEnabled()) {
    		logger.debug("Query is: " + qTree.asXML());
    		logger.debug("Query is: " + soQuery.toString());
    	}
    	
    	LuceneReadSession luceneSession = getLuceneSessionFactory().openReadSession();
        
    	Hits hits = null;
        try {
	        hits = luceneSession.search(RequestContextHolder.getRequestContext().getUserId(),
	        		so.getAclQueryStr(), Constants.SEARCH_MODE_NORMAL, soQuery, null, 0, Integer.MAX_VALUE);
        }
        finally {
            luceneSession.close();
        }
    	
        Map<String,Long> result = new HashMap<String,Long>();
        int count = hits.length();
        org.apache.lucene.document.Document doc;
        String fileName;
        Long entryId;
        for(int i = 0; i < count; i++) {
        	doc = hits.doc(i);
        	fileName = doc.get(Constants.FILENAME_FIELD);
        	if(fileName != null) {
        		try {
	        		entryId = Long.valueOf(doc.get(Constants.DOCID_FIELD));
	        		result.put(fileName, entryId);
        		}
        		catch(Exception ignore) {}
        	}
        }
        
        return result;
	}

    public Long deleteAgedFileVersions(DefinableEntity entity, Date agingDate) {
		Binder binder = entity.getParentBinder();
		Date now = new Date();
		if (entity.getEntityType().equals(EntityType.folder) || entity.getEntityType().equals(EntityType.workspace)) {
			binder = (Binder)entity;
		}
		long counter = 0;
		for (Attachment att : entity.getAttachments()) {
			if (att instanceof FileAttachment) {
				FileAttachment fAtt = (FileAttachment)att;
				for (VersionAttachment vAtt : (Set<VersionAttachment>)fAtt.getFileVersions()) {
					Date versionAgingDate = vAtt.getAgingDate();
					if (vAtt.isAgingEnabled() && versionAgingDate != null && 
							versionAgingDate.before(now)) {
						//This version is subject to a binder aging date
						deleteVersion(binder, entity, vAtt);
						counter++;
					} else if (vAtt.isAgingEnabled() && versionAgingDate == null && 
							vAtt.getCreation().getDate().before(agingDate)) {
						//This file version was created before the zone default aging date.
						//  It is subject to aging, so delete it
						deleteVersion(binder, entity, vAtt);
						counter++;
					}
				}
			}
		}
		return counter;
	}

	
	private void triggerUpdateTransaction(final List newObjs) {
        getTransactionTemplate().execute(new TransactionCallback() {
        	public Object doInTransaction(TransactionStatus status) {
        		if(newObjs != null) {
        			for(Object newObj:newObjs)
        				getCoreDao().save(newObj);
        		}
                return null;
        	}
        });	
	}
	
	private void deletePrimaryFile(Binder binder, DefinableEntity entry,
			String relativeFilePath, String repositoryName, FilesErrors errors) {
		RepositorySession session = RepositorySessionFactoryUtil.openSession(repositoryName, binder.getResourceDriverName(), ResourceDriverManager.FileOperation.DELETE, binder);
		
		try {
			try {
				session.delete(binder, entry, relativeFilePath);
			}
			catch(Exception e) {
				logger.error("Error deleting primary file " + relativeFilePath, e);
				errors.addProblem(new FilesErrors.Problem
						(repositoryName, relativeFilePath, 
								FilesErrors.Problem.PROBLEM_DELETING_PRIMARY_FILE, e));
			}
		} finally {
			session.close();
		}		
	}
	
	private ChangeLog deleteFileInternal(Binder binder, DefinableEntity entry,
			FileAttachment fAtt, boolean deleteMirroredSource, 
			FilesErrors errors, boolean updateMetadata) {
		return deleteFileInternal2(binder, entry, fAtt, deleteMirroredSource, errors, updateMetadata);
	}
	
	private ChangeLog deleteFileInternal2(Binder binder, DefinableEntity entry,
			FileAttachment fAtt, boolean deleteMirroredSource, 
			FilesErrors errors, boolean updateMetadata) {
		String relativeFilePath = fAtt.getFileItem().getName();
		String repositoryName = fAtt.getRepositoryName();
		
		// Forcefully unlock the file (if locked). We discard pending
		// changes for the file for obvious reason - The file is soon
		// to be deleted.
		// 
		// 6/18/06 JK - It appears that JSR-170 does not provide a way to 
		// uncheckout a previously checked-in file (strange?) Therefore, 
		// we will simply checkin the file (hence possibly creating a 
		// new version) before deletion. Not ideal, but it works. 
		try {
			//closeLock(binder, entry, fAtt, false);
			closeLock(binder, entry, fAtt, true, null);
		}
		catch(Exception e) {
			logger.error("Error canceling lock on file " + relativeFilePath, e);
			errors.addProblem(new FilesErrors.Problem
					(repositoryName, relativeFilePath, 
							FilesErrors.Problem.PROBLEM_CANCELING_LOCK, e));
			// We proceed despite of the problem hoping we can still delete it.
		}

		List<KeyValuePair> archiveURIs = new ArrayList<KeyValuePair>();
		String archiveStoreName = null;

		if(!ObjectKeys.FI_ADAPTER.equals(fAtt.getRepositoryName())) { // regular repository
			// If there is any problem with the repository itself or communication 
			// with the repository, the following call is expected to detect it
			// (indirectly) and throw an exception right here. In essence, it will 
			// prevent delete operation from proceeding when it really shouldn't. 
			// The idea is that, we want delete operation to be reasonably forgiving, 
			// since it is quite frustrating for people to unable to delete
			// something just because the operation encountered a non-critical error.
			// The most important thing about deletion is the post-condition. As long
			// as the post-condition is acceptable, deletion should proceed even 
			// when there are errors. However, there are obviously exceptions to the
			// tolerability, and the following call reveals one such condition - 
			// that is, connection or configuration related problems about the
			// repository. Such problems must be reported, remedied, and then the
			// user should be able to repeate the same operation, which is, in 
			// this case, a delete operation. 
			int fileInfo = RepositoryUtil.fileInfo(repositoryName, binder, entry, relativeFilePath);
			
			if(fileInfo != RepositorySession.NON_EXISTING_FILE) {
				// Archive the contents of the file. We archive all versions of the file.
				ArchiveStore archiveStore = RepositorySessionFactoryUtil.getArchiveStore(repositoryName);
				if(archiveStore != null) {
					archiveStoreName = archiveStore.getName();
					for(Iterator i = fAtt.getFileVersionsUnsorted().iterator(); i.hasNext();) {
						VersionAttachment v = (VersionAttachment) i.next();
						try {
							String archiveURI = archiveStore.write(binder, entry, v);
							
							if(archiveURI != null)
								archiveURIs.add(new KeyValuePair(String.valueOf(v.getVersionNumber()), archiveURI));
						}
						catch(Exception e) {
							logger.error("Error archiving file " + relativeFilePath + " version " + v.getVersionNumber(), e);
							errors.addProblem(new FilesErrors.Problem
									(repositoryName, relativeFilePath,
											FilesErrors.Problem.PROBLEM_ARCHIVING, e));
							// Well, this is not a good situation. The repository is reachable, 
							// the file exists in the repository, but for some reason, we could
							// not archive it. It is most likely that, for some reason, the 
							// particular version of the file does not exist in the repository, etc.
							// Since there is no other good alternative, we proceed, hoping that
							// we can at least archive "some" versions of the file. 
						}
					}
				}
				// Delete primary file
				deletePrimaryFile(binder, entry, relativeFilePath, repositoryName, errors);
			}
			else {
				// Since regular repository is supposed to be owned exclusively by Aspen,
				// this is not expected to occur. So we log it as warning.
				logger.warn("The file " + relativeFilePath + " does not exist in the repository " + repositoryName);
			}	
		}
		else if (deleteMirroredSource) { // mirrored repository & delete mirrored source
			// We do not archive files in mirrored repository.
			int fileInfo = RepositoryUtil.fileInfo(repositoryName, binder, entry, relativeFilePath);
			
			if(fileInfo != RepositorySession.NON_EXISTING_FILE) {
				// Delete primary file.
				deletePrimaryFile(binder, entry, relativeFilePath, repositoryName, errors);
			}
			else {
				// Since mirrored repository is accessible outside of Aspen as well, it
				// is conceivable that the file was removed externally just prior to
				// this code being executed. So we log it as info.
				logger.info("The file " + relativeFilePath + " does not exist in the repository " + repositoryName + " through driver " + binder.getResourceDriverName());				
			}
		}
		else { // mirrored repository & do not delete mirrored source
			// We do not archive files in mirrored repository. 
			// No primary file to delete.
		}
				
   		ChangeLog changeLog = new ChangeLog(entry, ChangeLog.FILEDELETE);
   		Element parent = ChangeLogUtils.buildLog(changeLog, fAtt);
   		if(archiveURIs.size() > 0) {
   			Element fileElem = parent.addElement(ObjectKeys.XTAG_ELEMENT_TYPE_FILEARCHIVE);
   			if(archiveStoreName != null)
   				fileElem.addAttribute(ObjectKeys.XTAG_FILE_ARCHIVE_STORE_NAME, archiveStoreName);
   			for(KeyValuePair pair : archiveURIs) {
   				Element verElem = fileElem.addElement(ObjectKeys.XTAG_ELEMENT_TYPE_VERSIONARCHIVE);
   				verElem.addAttribute(ObjectKeys.XTAG_FILE_VERSION_NUMBER, pair.getKey()); 
   				verElem.addAttribute(ObjectKeys.XTAG_FILE_ARCHIVE_URI, pair.getValue());
   			}
   		}
   		
   		// If this attachment has a relevance UUID...
   		String relevanceUUID = fAtt.getRelevanceUUID();
   		if (MiscUtil.hasString(relevanceUUID)) {
   			// ...and relevance integration is enabled...
   			Relevance relevanceEngine = getRelevanceManager().getRelevanceEngine();
   			if (relevanceEngine.isRelevanceEnabled()) {
   				// ...remove it from relevance tracking.
   				relevanceEngine.removeAttachmentUUID(relevanceUUID);
   			}
   		}
   		
   		// Decrement disk space used for each version as well as the primary
   		ZoneConfig zoneConf = getCoreDao().loadZoneConfig(
				RequestContextHolder.getRequestContext()
				.getZoneId());
   		if (zoneConf.isDiskQuotaEnabled()) {
   			for(Iterator i = fAtt.getFileVersionsUnsorted().iterator(); i.hasNext();) {
   				VersionAttachment v = (VersionAttachment) i.next();
   				if (v.getRepositoryName().equalsIgnoreCase(ObjectKeys.FI_ADAPTER)) break;
   				
   				// Decrement the disk space used by this user.
   				try
   				{
   					User user = getProfileDao().loadUser(v.getCreation().getPrincipal().getId(), RequestContextHolder.getRequestContext().getZoneName());
   					user.decrementDiskSpaceUsed(v.getFileItem().getLength());
   				}
   				catch ( NoUserByTheIdException ex )
   				{
   					// Nothing to do here.  This means that we are deleting a file
   					// that is in a deleted user's workspace.
   				}
   			}
   		}
   		if (zoneConf.isBinderQuotaInitialized()) {
   			//Once the binder quotas have been initialized, we must keep track of the space used
   			for(Iterator i = fAtt.getFileVersionsUnsorted().iterator(); i.hasNext();) {
   				VersionAttachment v = (VersionAttachment) i.next();
   				if (v.getRepositoryName().equalsIgnoreCase(ObjectKeys.FI_ADAPTER)) break;
   				
   				//Decrement the space used all the way up the parent tree.
   				getBinderModule().decrementDiskSpaceUsed(binder, v.getFileItem().getLength());
   			}
   		}
   		
    	// System.out.println(changeLog.getXmlNoHeader());
    	if(updateMetadata)
			writeDeleteMetaDataTransactional(binder, entry, fAtt, changeLog);
		
		return changeLog;
	}
	
	private void writeDeleteMetaDataTransactional(final Binder binder, 
			final DefinableEntity entry, final FileAttachment fAtt,
			final ChangeLog changeLog)  {
		// Remove metadata and log change
	       getTransactionTemplate().execute(new TransactionCallback() {
	       	public Object doInTransaction(TransactionStatus status) {  
	       		getCoreDao().save(changeLog);
            	
				entry.removeAttachment(fAtt);
				//file names on binders are not registered
				if (binder.isLibrary() && !binder.equals(entry)) getCoreDao().updateFileName(binder, entry, fAtt.getFileItem().getName(), null);
				// Do NOT reset the title of the entry even if the title came from the file 
				// being deleted and the parent folder required unique titles.
				// In other word, deleting a file from an entry returns its file name to the
				// namespace pool but not the entry's title. If the user needs to create a
				// new entry with the same title (as opposed to re-using the old entry), he
				// will have to manually delete the old entry before creating a new one
				// (related ICEcore issue number is #1803).
				/*
		        if ((entry.getEntryDef() != null)  && DefinitionUtils.isSourceItem(entry.getEntryDef().getDefinition(), fAtt.getName(), ObjectKeys.FIELD_ENTITY_TITLE)) {
		        	//if tracking unique titles, remove old title
		        	if ((entry.getParentBinder() != null) && entry.getParentBinder().isUniqueTitles()) getCoreDao().updateTitle(entry.getParentBinder(), entry, entry.getNormalTitle(), null);
		        	//check title for entries
		        	entry.setTitle("");			   			   
				}
				*/
			        
	            return null;
	       	}
	     });	
	}

	private void writeDeleteChangeLogTransactional(final List<ChangeLog> changeLogs) {
		// We want to start a transaction even when there is nothing to write
		// (ie, empty changeLogs), so that any pending updates that the caller
		// made up to this point can get recorded permanently.
        getTransactionTemplate().execute(new TransactionCallback() {
        	public Object doInTransaction(TransactionStatus status) {  
                for(ChangeLog changeLog : changeLogs) {
                	getCoreDao().save(changeLog);
                }
            	return null;
        	}
        });	
	}
	
	private void moveFilterFailedFile(Binder binder, FileUploadItem fui) throws IOException {
		File filteringFailedDir = SPropsUtil.getFile("file.content.filter.failed.dir");
		if(!filteringFailedDir.exists())
			FileHelper.mkdirs(filteringFailedDir);
		File outFile = new File(filteringFailedDir, makeFileName(binder, fui));
		// The caller is responsible for ensuring that the fui is reentrant.
		InputStream is = fui.getInputStream();
		try {
			OutputStream os = new BufferedOutputStream(new FileOutputStream(outFile));
			try {
				FileCopyUtils.copy(is, os);
				fui.delete();
			}
			finally {
				try {
					os.close();
				}
				catch(IOException ignore) {}
			}
		}
		finally {
			try {
				is.close();
			}
			catch(IOException ignore) {}
		}
	}
	
	private static final String DELIM = "_";
	private String makeFileName(Binder binder, FileUploadItem fui) {
		RequestContext rc = RequestContextHolder.getRequestContext();
		StringBuffer sb = new StringBuffer();
		sb.append(rc.getZoneName()).	// zone name
			append(DELIM).
			append(rc.getUserName()).	// user name
			append(DELIM).
			append(binder.getId()).		// binder id
			append(DELIM).
			append(fui.getOriginalFilename()).	// file name
			append(DELIM).
			append(System.currentTimeMillis());	// timestamp (in milliseconds)
		return sb.toString();
	}
	
	private void writeFileMetadataTransactional(final Binder binder, final DefinableEntity entry, 
    		final FileUploadItem fui, final FileAttachment fAtt, final boolean isNew, final boolean versionCreated) {	
		getTransactionTemplate().execute(new TransactionCallback() {
        	public Object doInTransaction(TransactionStatus status) {
        		writeFileMetadataNonTransactional(binder, entry, fui, fAtt, isNew, versionCreated);
                return null;
       	}
       });
	}
	
	private void writeFileMetadataNonTransactional(final Binder binder, final DefinableEntity entry, 
    		final FileUploadItem fui, final FileAttachment fAtt, final boolean isNew, final boolean versionCreated) {	
		//Copy the "description" into the file attachment
		fAtt.getFileItem().setDescription(fui.getDescription());
		if(isNew) {
    		// Important: Since file attachment is stored into custom 
			// attribute using its id value rather than association, 
			// this new object must be persisted here just in case it 
			// is to be put into custom attribute down below. This 
			// piece of code makes it impossible for the caller to 
			// utilize the usual tactic of delaying update db 
			// transaction to the very end of the operation.  
    		getCoreDao().save(fAtt);    		
		}
    	if (fui.getType() == FileUploadItem.TYPE_FILE) {
    		setCustomAttribute(entry, fui, fAtt, true);
		} else if (fui.getType() == FileUploadItem.TYPE_ATTACHMENT) {
			// Add the file attachment to the entry only if new file. 
    		setCustomAttribute(entry, fui, fAtt, false);
			if(isNew) {
				entry.addAttachment(fAtt);
			}
			
			String contentId = fui.getContentId();
			if (contentId != null && !"".equals(contentId)) {
    			String description = entry.getDescription().getText();
    			if (description != null && description.indexOf(contentId) > -1) {
    				description = description.replaceAll("\"cid:[\\s]*" + Pattern.quote(contentId) + "\"", "\"{{attachmentUrl: " + fAtt.getFileItem().getName() + "}}\"");
    				entry.getDescription().setText(description);
    			}
			}
			
		}  else if (fui.getType() == FileUploadItem.TYPE_TITLE) {
			String title = fui.getOriginalFilename();
			CustomAttribute ca = entry.getCustomAttribute(fui.getName());
			if (ca != null) {
				//exists - only allow 1 file, move to attachments
				Set fAtts = (Set) ca.getValueSet();
				for (Iterator iter=fAtts.iterator(); iter.hasNext(); ) {
					FileAttachment fa = (FileAttachment)iter.next();
					//if not the same - move it
					if (!fAtt.equals(fa)) {
						fa.setName(null);
						for (Iterator iter2=fa.getFileVersions().iterator(); iter2.hasNext();) {
							FileAttachment a = (FileAttachment)iter2.next();
							a.setName(null);
						}
					}
				}
   				fAtts = new HashSet();
				fAtts.add(fAtt); 
		    	ca.setValue(fAtts);
			} else {
				Set fAtts = new HashSet();
				fAtts.add(fAtt);
				entry.addCustomAttribute(fui.getName(), fAtts);
			}
        	//if tracking unique titles, remove old title and add new
         	String oldTitle = entry.getNormalTitle();
   			entry.setTitle(title);
   		   	if ((entry.getParentBinder() != null) && entry.getParentBinder().isUniqueTitles()) getCoreDao().updateTitle(entry.getParentBinder(), entry, oldTitle, entry.getNormalTitle());
		} 
   		//add file name so not null
    	if (Validator.isNull(entry.getTitle())) entry.setTitle(fAtt.getFileItem().getName());
		ChangeLog changes = null;
    	if (isNew)
    		changes = new ChangeLog(entry, ChangeLog.FILEADD);
    	else if(versionCreated)
    		changes = new ChangeLog(entry, ChangeLog.FILEMODIFY);
    	
    	if(versionCreated) {
    		// The content was committed creating a new version. Increment disk usage for the user.
    		incrementDiskSpaceUsed(fAtt);
    	}
    	
    	if(changes != null) {
        	ChangeLogUtils.buildLog(changes, fAtt);
    		getCoreDao().save(changes);
    	}
    }

	/*
	protected void setLockMetadata(final FileAttachment fAtt, final FileLock lock) {
        getTransactionTemplate().execute(new TransactionCallback() {
        	public Object doInTransaction(TransactionStatus status) {
        		fAtt.setFileLock(lock);    
                return null;
        	}
        });
	}
	
	protected void removeAttachmentMetadata(final DefinableEntity entry, final FileAttachment fAtt) {
        getTransactionTemplate().execute(new TransactionCallback() {
        	public Object doInTransaction(TransactionStatus status) {
        		entry.removeAttachment(fAtt);  
                
                return null;
        	}
        });
	}*/
	 
	private boolean writeFilePreCheck(Binder binder, DefinableEntity entry, 
    		FileUploadItem fui, FilesErrors errors) {
		int type = fui.getType();
		if(type != FileUploadItem.TYPE_FILE && 
				type != FileUploadItem.TYPE_ATTACHMENT && 
				type != FileUploadItem.TYPE_TITLE) {
			logger.error("Unrecognized file processing type " + type + " for ["
					+ fui.getName() + ","
					+ fui.getOriginalFilename() + "]");
			throw new InternalException();
		}
		
		String relativeFilePath = fui.getOriginalFilename();
		String repositoryName = fui.getRepositoryName();

		// First, find out whether or not this is a new file for the entry.
		// It is important to note that, as far as identity/existence test
		// goes, the namespace is flat for a single instance of Entry.
		// In other words, regardless of the data elements used for accessing
		// the file, the files are treated identical globally within a single
		// Entry instance as long as their file names are identical. 
		// flatten repository namespace to reduce confusion
		FileAttachment fAtt = entry.getFileAttachment(relativeFilePath);
    	if ((fAtt != null) && !repositoryName.equals(fAtt.getRepositoryName())) {
			errors.addProblem(new FilesErrors.Problem
					(fAtt.getRepositoryName(), relativeFilePath, 
							FilesErrors.Problem.PROBLEM_FILE_EXISTS));
			return false;
    	}

		if(repositoryName.equals(ObjectKeys.FI_ADAPTER)) {
			if(!binder.isMirrored()) {
				errors.addProblem(new FilesErrors.Problem
						(repositoryName, relativeFilePath, 
								FilesErrors.Problem.PROBLEM_MIRRORED_FILE_IN_REGULAR_FOLDER, 
								new IllegalArgumentException("Binder [" + binder.getPathName() + "] is not a mirrored folder")));
				return false;				
			}
			else {
				List<FileAttachment> fas = entry.getFileAttachments(ObjectKeys.FI_ADAPTER); // should be at most 1 in size
	   			if(fas.size() > 1)
	   				logger.warn("Integrity error: Entry " + entry.getId() + " in binder [" + binder.getPathName() + "] mirrors multiple files");
				for(FileAttachment fa : fas) {
					if(!relativeFilePath.equals(fa.getFileItem().getName())) {
						errors.addProblem(new FilesErrors.Problem
								(repositoryName, relativeFilePath, 
										FilesErrors.Problem.PROBLEM_MIRRORED_FILE_MULTIPLE, 
										new IllegalArgumentException("The entry " + entry.getId() + 
												" already mirrors another file [" + fa.getFileItem().getName() + "]")));
						return false;					
					}
				}
			}
		}
		
		return true;
	}
	
	/**
	 * return true if primary file successfull written
	 * return false or throw exception if either primary not written or metadata update failed.
	 */
    private boolean writeFileTransactional(Binder binder, DefinableEntity entry, 
    		FileUploadItem fui, FilesErrors errors) {
		String relativeFilePath = fui.getOriginalFilename();
		String repositoryName = fui.getRepositoryName();
		FileAttachment fAtt = entry.getFileAttachment(relativeFilePath);

    	/// Work Flow:
    	/// step1: write primary file
    	/// step2: update metadata in database
    	
    	Boolean encryptAllFiles = SPropsUtil.getBoolean("file.encryption.encryptAll", false);
    	if (!binder.isMirrored() && (encryptAllFiles || getBinderModule().isBinderFileEncryptionEnabled(binder))) {
    		//All files should be encrypted or this binder requires that all files be encrypted, 
    		//  so mark that the file should be encrypted.
    		try {
				//Get the key to use when encrypting and decrypting
    			CryptoFileEncryption cfe = new CryptoFileEncryption();
				SecretKey key = cfe.getSecretKey();
				if (key == null) {
	    			errors.addProblem(new FilesErrors.Problem
	    					(fui.getRepositoryName(), fui.getOriginalFilename(), 
	    							FilesErrors.Problem.PROBLEM_ENCRYPTION_FAILED));
	    			return false;
				}
				fui.setEncryptionKey(key.getEncoded());
	    		fui.setIsEncrypted(true);
    		} catch(Exception e) {
    			errors.addProblem(new FilesErrors.Problem
    					(fui.getRepositoryName(), fui.getOriginalFilename(), 
    							FilesErrors.Problem.PROBLEM_ENCRYPTION_FAILED));
    			return false;
    		}

    	}

    	if(!writeFilePreCheck(binder, entry, fui, errors))
    		return false;
    	
    	boolean isNew = false;
    	
		RepositorySession session = null;
		
		if(fAtt == null) { // New file
			// If we're dealing with a mirrored folder (net folder), and we're creating a new mirrored
			// file entry in the database as part of file sync process, and the caller (i.e. file sync
			// process) specified the content legnth of the file, then we have all the metadata 
			// information necessary to create the file attachment object in the database WITHOUT
			// ever having to further interact with the back-end file system. Therefore, we do NOT
			// need to incur the overhead of opening a session for that file.
			if(!ObjectKeys.FI_ADAPTER.equalsIgnoreCase(fui.getRepositoryName()) || 
					fui.isSynchToRepository() ||
					fui.getCallerSpecifiedContentLength() == null)
				session = RepositorySessionFactoryUtil.openSession(repositoryName, binder.getResourceDriverName(), ResourceDriverManager.FileOperation.CREATE_FILE, binder);
		}
		else { // Existing file
			session = RepositorySessionFactoryUtil.openSession(repositoryName, binder.getResourceDriverName(), ResourceDriverManager.FileOperation.UPDATE, entry);
		}

    	try {
    		boolean versionCreated = false;
    		try {
	    		// Store primary file first, since we do not want to generate secondary
	    		// files unless we could successfully store the primary file first. 
	    		
	    		if(fAtt == null) { // New file for the entry
	    			SimpleProfiler.start("writeFile_createFile");
	    			isNew = true;
	    			fAtt = createFile(session, binder, entry, fui);
	    			versionCreated = true;
	    			SimpleProfiler.stop("writeFile_createFile");
	    		}
	    		else { // Existing file for the entry
	    			SimpleProfiler.start("writeFile_writeExistingFile");
	    			// Bug #637636 - In order to treat the files that only differ in case as a single
	    			// file, we normalize the file names of all versions of a single file to the file name 
	    			// of the initial version. 
	    			fui.setOriginalFilename(fAtt.getFileItem().getName());
	    			if(writeExistingFile(session, binder, entry, fui) != null)
	    				versionCreated = true;
	    			SimpleProfiler.stop("writeFile_writeExistingFile");
	    		}
    		}
    		catch(DataQuotaException e) {
    			errors.addProblem(new FilesErrors.Problem(repositoryName, relativeFilePath, -1, e));
    			return false;
    		}
    		catch(Exception e) {
    			logger.error("Error storing primary file " + relativeFilePath, e);
    			// We failed to write the primary file. In this case, we 
    			// discard the rest of the operation (i.e., step2 thru 4).
    			errors.addProblem(new FilesErrors.Problem
    					(repositoryName, relativeFilePath, 
    							FilesErrors.Problem.PROBLEM_STORING_PRIMARY_FILE, e));
    			return false;
    		}		

	    	// Update metadata - We do this only after successfully writing
	    	// the file to the repository to ensure that our metadata describes
	    	// what actually exists. Of course, there could be a failure scenario
	    	// where this metadata update fails leaving the file dangling in the
	    	// repository. But that is expected to be a lot more rare, and not
	    	// quite as destructive as the other case. But the bottom line is, 
	    	// unless we have a single transaction that spans both repository
	    	// update and database update all within a single unit, there will
	    	// always be error cases that can leave the data inconsistent. 
	    	// When a repository supports JCA, this should be possible to do
	    	// using JTA. But that's not always available, and this version of
	    	// the system does not try to address that. 
    		SimpleProfiler.start("writeFile_MetadataTransactional");
	    	writeFileMetadataTransactional(binder, entry, fui, fAtt, isNew, versionCreated);
    		SimpleProfiler.stop("writeFile_MetadataTransactional");
	    	
        	//SimpleProfiler.done(logger);

	    	return true;
    	}
    	finally {
    		if(session != null)
    			session.close();
    	}
    }
    
    private boolean writeFileValidationOnly(Binder binder, DefinableEntity entry, 
    		FileUploadItem fui, FilesErrors errors) {
    	
		String relativeFilePath = fui.getOriginalFilename();
		String repositoryName = fui.getRepositoryName();
		FileAttachment fAtt = entry.getFileAttachment(relativeFilePath);

    	if(!writeFilePreCheck(binder, entry, fui, errors))
    		return false;
    	
    	if((binder instanceof Folder) && binder.isLibrary()) {
    		Folder folder = (Folder) binder;
    		FolderEntry otherEntry = getFolderModule().getLibraryFolderEntryByFileName(folder, relativeFilePath);
    		if(otherEntry != null && !otherEntry.getId().equals(entry.getId())) {
    			errors.addProblem(new FilesErrors.Problem
    					(repositoryName, relativeFilePath, 
    							FilesErrors.Problem.PROBLEM_FILE_EXISTS));
    			return false;
    		}
    	}
    	
		try {
    		checkDataQuota(binder, fui);
			
    		if(fAtt != null) { //Existing file for the entry
    	    	User user = RequestContextHolder.getRequestContext().getUser();
    	    	// Check lock
    	    	FileAttachment.FileLock lock = fAtt.getFileLock();    	
    	    	if(lock != null) {
    	    		if(!isLockExpired(lock)) { // We have an effective lock.
    	    			if(!lock.getOwner().equals(user))
    	    				throw new LockedByAnotherUserException(entry, fAtt, lock.getOwner());
    	    		}
    	    	}

    		}
		}
		catch(DataQuotaException e) {
			errors.addProblem(new FilesErrors.Problem(repositoryName, relativeFilePath, -1, e));
			return false;
		}
		catch(LockedByAnotherUserException e) {
			errors.addProblem(new FilesErrors.Problem(repositoryName, relativeFilePath, -1, e));
			return false;
		}
		catch(IOException e) {
			errors.addProblem(new FilesErrors.Problem(repositoryName, relativeFilePath, -1, new UncheckedIOException(e)));
			return false;
		}		

    	return true;
    }
    
    private String writeExistingFile(RepositorySession session,
    		Binder binder, DefinableEntity entry, FileUploadItem fui)
		throws LockedByAnotherUserException, RepositoryServiceException, IOException, DataQuotaException {
    	User user = RequestContextHolder.getRequestContext().getUser();
    	String relativeFilePath = fui.getOriginalFilename();
		// flatten repository namespace to reduce confusion
//    	FileAttachment fAtt = entry.getFileAttachment(fui.getRepositoryName(), relativeFilePath);
    	FileAttachment fAtt = entry.getFileAttachment(relativeFilePath);
    	
    	// Before checking the lock, we must make sure that the lock state is
    	// up-to-date.
    	List newObjs = new ArrayList();
    	if(closeExpiredLock(session, binder, entry, fAtt, true, newObjs) || newObjs.size() > 0) {
    		// Handling of expired lock resulted in some changes to the metadata. 
    		// We want to commit this changes separately from the main work that this
    		// method is being invoked to perform, since they are two completely separate
    		// works and we do not want the outcome of the main work to affect 
    		// the durability of the changes incurred inside closeExpiredLock().
    		triggerUpdateTransaction(newObjs);
    	}
    	
    	// Now that lock state is current, we can test it for the user.
    	checkLock(entry, fAtt);
    	
    	// Check data quota
    	checkDataQuota(binder, fui);
    	
    	String versionName = null;

    	if(!fileExistsInRepository(fAtt) &&
				fAtt.getLastVersion().intValue() == 1 &&
				fAtt.getMajorVersion().intValue() == 1 &&
				fAtt.getMinorVersion().intValue() == 0) {
			// This is the special scenario captured in Bug #632279. 
			// This part adds special logic to work around the issue.
    		versionName = createVersionedFile(session, binder, entry, fui);
    		long fsize;
    		if(fui.getCallerSpecifiedContentLength() != null)
    			fsize = fui.getCallerSpecifiedContentLength().longValue();
    		else
    			fsize = session.getContentLengthVersioned(binder, entry, relativeFilePath, versionName);
			fAtt.getFileItem().setLength(fsize);
            fAtt.getFileItem().setMd5(fui.getMd5());
			fAtt.setEncrypted(fui.getIsEncrypted());
			fAtt.setEncryptionKey(fui.getEncryptionKey());
			createVersionAttachment(fAtt, versionName);	    		
			if(logger.isDebugEnabled())
				logger.debug("Updating existing file " + relativeFilePath + " with initial version");
		}
		else {
	    	FileAttachment.FileLock lock = fAtt.getFileLock();
	    	
	    	// All expired locks were taken care of higher up in the call stack.
	    	// Also owner check was done already. So we can assume that lock, 
	    	// if exists, is effective and owned by the calling user.
	    	
	    	Long fileSize = null;
	    	int fileInfo = session.fileInfo(binder, entry, relativeFilePath);
	    	if(fileInfo == RepositorySession.VERSIONED_FILE) { // Normal condition
	    		UpdateInfo updateInfo = updateVersionedFile(session, binder, entry, fui, lock);
	    		versionName = updateInfo.versionName;
	    		fileSize = updateInfo.fileLength;
				fAtt.setEncrypted(fui.getIsEncrypted());
				fAtt.setEncryptionKey(fui.getEncryptionKey());
	    	}
	    	else if(fileInfo == RepositorySession.NON_EXISTING_FILE) {
				// For some reason the file doesn't exist in the repository.
				// That is, our metadata says it exists, but the repository 
				// says otherwise. This reflects some previous error condition.
				// For example, previous attempt to add the file may have
				// failed partially. Or someone may have gone and errorneously
				// deleted the file from the repository. At any rate, the
				// end result is descrepency between the repository system
				// and our metadata. Although not ideal, better response to
				// this kind of situation appears to be the one that is more
				// forgiving or self-curing. This part of code implements that.
	    		versionName = createVersionedFile(session, binder, entry, fui);
	    		long fsize;
	    		if(fui.getCallerSpecifiedContentLength() != null)
	    			fsize = fui.getCallerSpecifiedContentLength().longValue();
	    		else
	    			fsize = session.getContentLengthVersioned(binder, entry, relativeFilePath, versionName);
	    		fileSize = Long.valueOf(fsize);
    			fAtt.setEncrypted(fui.getIsEncrypted());
    			fAtt.setEncryptionKey(fui.getEncryptionKey());
	    	}
	    	else {
	    		throw new InternalException();
	    	}
	    	
	    	if(lock != null && versionName == null) {
	    		// A lock existed at the time of writing the content and the writing
	    		// didn't create a new version as of yet. This means that we have a
	    		// pending change that we need to commit/checkin when we release the
	    		// lock later. So mark the lock as dirty.
	    		lock.setDirty(Boolean.TRUE);
	    	}
	    	
			//if we are adding a new version of an existing attachment to 
			//a uniqueName item, set flag - (will already be set if originally added
			//through a unique element.  In other works, once unique always unique
			updateFileAttachment(fAtt, user, versionName, fileSize, fui.getMd5(), fui.getModDate(), fui.getModifierName(),
					fui.getDescription());
		}
		
		return versionName;
    }

    private void updateFileAttachment(FileAttachment fAtt, 
			UserPrincipal user, String versionName, Long contentLength, String md5,
			Date modDate, String modName, Description description) {
    	HistoryStamp now = new HistoryStamp(user);
    	HistoryStamp mod;
       	if(modDate != null) {
			if (Validator.isNotNull(modName)) {
				user = getProfileDao().findUserByName(modName, RequestContextHolder.getRequestContext().getZoneName());
			}
    		mod = new HistoryStamp(user, modDate);
    	} else
    		mod = now;
    	
		fAtt.setModification(mod);
		fAtt.setFileStatus(FileStatus.not_set.ordinal());
		FileItem fItem = fAtt.getFileItem();

		if(contentLength != null)
			fItem.setLength(contentLength);
		
		if(md5 != null)
			fItem.setMd5(md5);

		if(description != null)
			fItem.setDescription(description);
		
		if(versionName != null) {
			// The repository system supports versioning.        			
			int versionNumber = fAtt.getLastVersion().intValue() + 1;
			fAtt.setLastVersion(new Integer(versionNumber));
			fAtt.setMinorVersion(fAtt.getMinorVersion() + 1);
			
			VersionAttachment vAtt = new VersionAttachment();
			// Creation time is always current real time, whereas modification
			// time could be anything that the caller specified it to be
			// (only the latter contains useful business value). 
			vAtt.setCreation(now);
			vAtt.setModification(fAtt.getModification());
			vAtt.setFileItem(fItem);
			vAtt.setVersionNumber(versionNumber);
			vAtt.setMajorVersion(fAtt.getMajorVersion());
			vAtt.setMinorVersion(fAtt.getMinorVersion());
			vAtt.setVersionName(versionName);
			vAtt.setRepositoryName(fAtt.getRepositoryName());
			vAtt.setEncrypted(fAtt.getEncrypted());
			vAtt.setEncryptionKey(fAtt.getEncryptionKey());
			fAtt.addFileVersion(vAtt);
			// Do this only if a new version has actually been created.
			FileUtils.setFileVersionAging(fAtt.getOwner().getEntity());
		}
	}
    
    private class UpdateInfo {
    	private String versionName = null;
    	private Long fileLength = null;
        private String fileMd5 = null;
    }
    
    private UpdateInfo updateVersionedFile(RepositorySession session, Binder binder, 
    		DefinableEntity entity, FileUploadItem fui, FileAttachment.FileLock lock) 
    throws IOException {
    	String relativeFilePath = fui.getOriginalFilename();
    	UpdateInfo updateInfo = new UpdateInfo();
		if(fui.isSynchToRepository()) {
    		// Attempt to check out the file. If the file was already checked out
    		// this is noop. So no harm. 
    		session.checkout(binder, entity, relativeFilePath);
    		// Update the file content
    		InputStream in = fui.getInputStream();
    		SizeMd5Pair sizeMd5Pair = fui.makeReentrant();
    		try {
    			updateWithInputData(session, binder, entity, relativeFilePath, in, sizeMd5Pair.getSize(), sizeMd5Pair.getMd5());
    		}
    		finally {
    			try {
    				in.close();
    			}
    			catch(IOException e) {}
    		}
    		if(lock == null) {
    			// This update request is being made without the user's prior 
    			// obtaining lock. Since there's no lock to associate the 
    			// checkout with, we must checkin the file here. 
    			// sort of like auto-commit = true
    			updateInfo.versionName = session.checkin(binder, entity, relativeFilePath);
    			long fsize;
    			if(fui.getCallerSpecifiedContentLength() != null)
    				fsize = fui.getCallerSpecifiedContentLength().longValue();
    			else
    				fsize = session.getContentLengthVersioned(binder, entity, relativeFilePath, updateInfo.versionName);
    			updateInfo.fileLength = Long.valueOf(fsize);
                updateInfo.fileMd5 = sizeMd5Pair.getMd5();
    		}
    		else {
    			// auto-commit = false
    			updateInfo.fileLength = sizeMd5Pair.getSize();
                updateInfo.fileMd5 = sizeMd5Pair.getMd5();
    		}
		}
		else { // This condition can occur only for mirrored folder when synching inbound from the source 
			updateInfo.versionName = RepositoryUtil.generateRandomVersionName();
			if(fui.calledByFileSync()) {
				updateInfo.fileLength = fui.getCallerSpecifiedContentLength();
				updateInfo.fileMd5 = fui.getMd5();
			}
			else {
	            SizeMd5Pair sizeMd5Pair = fui.makeReentrant();
				updateInfo.fileLength = sizeMd5Pair.getSize();
	            updateInfo.fileMd5 = sizeMd5Pair.getMd5();
			}
		}

		return updateInfo;
    }
    
    private String createVersionedFile(RepositorySession session, Binder binder, 
    		DefinableEntity entity, FileUploadItem fui) 
    throws IOException {
    	String versionName = null;
    	
		if(fui.isSynchToRepository()) {
			InputStream in = fui.getInputStream();
			long size = fui.makeReentrant().getSize();
			try {
				versionName = createVersionedWithInputData(session, binder, entity,
						fui.getOriginalFilename(), fui.isSynchToRepository(), in, size);
			}
			finally {
				// Make sure to close the stream even when the above call fails. 
				// In normal case, we will be closing it twice, but that should be ok.
				try {
					in.close();
				}
				catch(IOException e) {}
			}
		}
		else {
			versionName = RepositoryUtil.generateRandomVersionName();
		}
		return versionName;
    }
    
    /**
	 * Creates a new file in the system.
	 * <p>
	 * This method adds the file to the repository system, creates a new
	 * <code>FileAttachment</code> object corresponding to the file being 
	 * created, and returns it so that it can be persisted by the caller.
	 * In other words, persisting the new metadata in our database is not
	 * a responsibility of this method.  
	 */
	private FileAttachment createFile(RepositorySession session, 
			Binder binder, DefinableEntity entry, FileUploadItem fui) 
		throws RepositoryServiceException, IOException, DataQuotaException {	
		// Since we are creating a new file, file locking doesn't concern us.
		
		checkDataQuota(binder, fui);
				
		FileAttachment fAtt = createFileAttachment(entry, fui);
			
		if(ObjectKeys.FI_ADAPTER.equalsIgnoreCase(fui.getRepositoryName()) // Bug #688072  
				|| fui.makeReentrant().getSize() > 0) {
			// For mirrored file entry, allow creation of empty file (to deal with bug #688072).
			// For regular file entry, creation of an empty file version is not allowed (for
			// bug #632279 explained below). This compromise is reasonable because mirrored
			// folders do not support versioning, hence it doesn't suffer from the same problem
			// caused by extraneous versions created from empty contents.
			String versionName = createVersionedFile(session, binder, entry, fui);
							
			long fileSize;
			
			if(fui.getCallerSpecifiedContentLength() != null)
				fileSize = fui.getCallerSpecifiedContentLength().longValue();
			else
				fileSize = session.getContentLengthVersioned(binder, entry, fui.getOriginalFilename(), versionName);
					
			fAtt.getFileItem().setLength(fileSize);
            fAtt.getFileItem().setMd5(fui.getMd5());
	
			createVersionAttachment(fAtt, versionName);	
		}
		else {
			// Bug #632279 - When creating a new file through Map Network Drive
			// interface from Windows 7, Windows WebDAV implementation issues PUT
			// requests twice - first with empty content, and second with the real
			// content. This results in creating two versions in Teaming. 
			// To work around this undesirable effect, we add this special logic.
			
			if(logger.isDebugEnabled())
				logger.debug("Creating new file " + fui.getOriginalFilename() + " without initial version");
		}

		return fAtt;
	}
	
	private void checkDataQuota(Binder binder, FileUploadItem fui) throws IOException {
		if (!ObjectKeys.FI_ADAPTER.equalsIgnoreCase(fui.getRepositoryName())) { 
			Long fileSize = fui.makeReentrant().getSize();
			
			//Check that the user is not over the user quota
			checkQuota(RequestContextHolder.getRequestContext().getUser(),
					fileSize,
					fui.getOriginalFilename());
			
			//Check that the binder and its parents aren't over quota
			checkBinderQuota(binder, fileSize, fui.getOriginalFilename());
			
			//Check if not too big
			checkFileSizeLimit(binder, fileSize, fui.getOriginalFilename());			
		}
	}
	
	public boolean checkIfQuotaWouldBeExceeded(Binder binder, long fileSize, String fileName) {
		//Check to see if adding a file of this size would exceed quota
		//Return true = quota would be exceeded
		User user = RequestContextHolder.getRequestContext().getUser();
		try {
			checkQuota(user, fileSize,fileName);
			checkBinderQuota(binder, fileSize, fileName);
		} catch(DataQuotaException e) {
			return true;
		}
		return false;
	}
	
	private void checkQuota(User user, long fileSize, String fileName) throws DataQuotaException {
		// first check properties to see if quotas is enabled on this system
		ZoneConfig zoneConf = getCoreDao().loadZoneConfig(
				RequestContextHolder.getRequestContext()
				.getZoneId());
		if (zoneConf.isDiskQuotaEnabled()) {
			long userQuota = zoneConf.getDiskQuotaUserDefault();

			if (user.getDiskQuota() != 0L) {
				userQuota = user.getDiskQuota();
			} else {
				if (user.getMaxGroupsQuota() != 0L) {
					userQuota = user.getMaxGroupsQuota();
				}
			}

			if (userQuota == -1L) return;  // -1 = unlimited
			
			userQuota = userQuota * MEGABYTES;

			if(SPropsUtil.getBoolean("data.quota.strict.conformance", true)) {
				// strict conformance - allow a transaction only if the user quota
				// will not be exceeded after the transaction commits.
				if(userQuota < user.getDiskSpaceUsed() + fileSize)
					throw new UserQuotaException(fileName);					
			}
			else {
				// soft conformance - allow a transaction if the user quota wasn't
				// exceeded when the transaction began.
				if ((userQuota < user.getDiskSpaceUsed()))
					throw new UserQuotaException(fileName);
			}
		}
	}
	
	private void checkBinderQuota(Binder binder, Long fileSize, String fileName) 
			throws DataQuotaException {
		if (!getBinderModule().isBinderDiskQuotaOk(binder, fileSize)) {
			//Adding this file would cause the quota to be exceeded
			throw new BinderQuotaException(fileName);
		}
	}
	
	private void checkFileSizeLimit(Binder binder, Long fileSize, String fileName) 
			throws DataQuotaException {
		User user = RequestContextHolder.getRequestContext().getUser();
		//Check that the file isn't too big
		//If there is a binder setting, it must match irrespective of user settings
		Long maxFileSize = getBinderModule().getBinderMaxFileSize(binder);
		if (maxFileSize != null) {
			//There is a file size limit, go check it
			if (fileSize > maxFileSize * ObjectKeys.MEGABYTES) {
				throw new FileSizeLimitException(fileName);
			}
		}
		//Check the system default and the user limits
		Long userMaxFileSize = user.getFileSizeLimit();
		Long userMaxGroupsFileSize = user.getMaxGroupsFileSizeLimit();
		Long fileSizeLimit = null;
		if (userMaxGroupsFileSize != null) {
			//Start with the group setting (if any)
			fileSizeLimit = userMaxGroupsFileSize;
		}
		if (userMaxFileSize != null) {
			//If there is a user setting, use that (even if it is less than the group setting)
			fileSizeLimit = userMaxFileSize;
		}
		if (fileSizeLimit == null) {
			//There aren't any per-user or per-group settings, so see if there is a site default
			fileSizeLimit = getAdminModule().getFileSizeLimitUserDefault();
		}
		//Now check to see if the file size is above the limit
		if (fileSizeLimit != null && fileSize > fileSizeLimit * ObjectKeys.MEGABYTES) {
			throw new FileSizeLimitException(fileName);
		}
	}
	
	private String createVersionedWithInputData(RepositorySession session,
			Binder binder, DefinableEntity entry, String relativeFilePath, 
			boolean synchToRepository, Object inputData, long size)
		throws RepositoryServiceException {
		String versionName = null;
		/*if(inputData instanceof MultipartFile) {
			versionName = service.createVersioned(session, binder, entry, 
					relativeFilePath, (MultipartFile) inputData);
		}
		else*/ if(inputData instanceof byte[]) {
			versionName = session.createVersioned(binder, entry, relativeFilePath,
					new ByteArrayInputStream((byte[]) inputData), ((byte[]) inputData).length);
		}
		else if(inputData instanceof InputStream) {
			versionName = session.createVersioned(binder, entry, relativeFilePath, 
					(InputStream) inputData, size);
		}
		else {
			throw new InternalException("Illegal input type [" + inputData.getClass().getName() + "]");
		}		
		
		return versionName;
	}
	
	private void updateWithInputData(RepositorySession session,
			Binder binder, DefinableEntity entry, String relativeFilePath, Object inputData, long size, String md5)
		throws RepositoryServiceException {
		/*if(inputData instanceof MultipartFile) {
			service.update(session, binder, entry, 
					relativeFilePath, (MultipartFile) inputData);
		}
		else*/ if(inputData instanceof byte[]) {
			session.update(binder, entry, relativeFilePath,
					new ByteArrayInputStream((byte[]) inputData), ((byte[]) inputData).length);
		}
		else if(inputData instanceof InputStream) {
			session.update(binder, entry, relativeFilePath, 
					(InputStream) inputData, size);
		}
		else {
			throw new InternalException("Illegal input type [" + inputData.getClass().getName() + "]");
		}		
	}
	
	private FileAttachment createFileAttachment(DefinableEntity entry, FileUploadItem fui) {
    	// TODO Take care of file path info?
    	
		Long zoneId =  RequestContextHolder.getRequestContext().getZoneId();
		
        String relativeFilePath = fui.getOriginalFilename();
	
		FileAttachment fAtt = new FileAttachment();
		fAtt.setOwner(entry);
		
		User creator = RequestContextHolder.getRequestContext().getUser();
		if(fui.getCreatorId() != null) {
			try {
				creator = getProfileDao().loadUser(fui.getCreatorId(), zoneId);
			}
			catch(Exception e) {
				logger.warn("Error loading user by ID '" + fui.getCreatorId() + "'", e);
			}
		}
		else if(fui.getCreatorName() != null) {
			try {
				creator = getProfileDao().findUserByName(fui.getCreatorName(), zoneId);
			}
			catch(Exception e) {
				logger.warn("Error loading user by name '" + fui.getCreatorName() + "'", e);
			}
		}
		HistoryStamp creation = new HistoryStamp(creator);
		fAtt.setCreation(creation);
		
		Date modDate;
		if(fui.getModDate() != null)
			modDate = fui.getModDate();
		else 
			modDate = creation.getDate();
		
		User modifier = (User) creation.getPrincipal();
		if(fui.getModifierId() != null) {
			try {
				modifier = getProfileDao().loadUser(fui.getModifierId(), zoneId);
			}
			catch(Exception e) {
				logger.warn("Error loading user by ID '" + fui.getModifierId() + "'", e);
			}
		}
		else if(fui.getModifierName() != null) {
			try {
				modifier = getProfileDao().findUserByName(fui.getModifierName(), zoneId);
			}
			catch(Exception e) {
				logger.warn("Error loading user by name '" + fui.getModifierName() + "'", e);
			}
		}
		HistoryStamp mod = new HistoryStamp(modifier, modDate);
		fAtt.setModification(mod);
		
		fAtt.setLastVersion(1);
		fAtt.setMajorVersion(1);
		fAtt.setMinorVersion(0);
		fAtt.setAgingEnabled(Boolean.FALSE);
    	fAtt.setRepositoryName(fui.getRepositoryName());
    	//set attribute name - null if not not named
    	fAtt.setName(fui.getName());
    	fAtt.setEncrypted(fui.getIsEncrypted());
    	fAtt.setEncryptionKey(fui.getEncryptionKey());
    	FileItem fItem = new FileItem();
    	fItem.setName(relativeFilePath);
    	fItem.setDescription(fui.getDescription());
    	// Optimization: Do NOT try to get the file size directly from the 
    	// FileUploadItem object. In the case where the content is available
    	// only as a InputStream (as opposed to a File), getting the size 
    	// here causes the content to be stored in a temporary file on disk
    	// just to be able to figure out the size. This additional copy is,
    	// not to mention, inefficient. Therefore, we will get the size 
    	// information from the repository after the content has been actually
    	// written to the repository.
    	
    	fAtt.setFileItem(fItem);
	
    	return fAtt;
	}
	
	/**
	 * This method is called ONLY WHEN a new file is being created
	 * (not to be confused with updateVersionAttachment which is called when
	 * a file is being updated). 
	 * 
	 * @param fAtt
	 * @param versionName
	 */
	private void createVersionAttachment(FileAttachment fAtt, String versionName) {
		VersionAttachment vAtt = new VersionAttachment();
		// Since this is the only version for the file, we can safely set its
		// dates equal to those of FileAttachment.
		vAtt.setCreation(fAtt.getCreation());
		vAtt.setModification(fAtt.getModification());
		vAtt.setFileItem(fAtt.getFileItem());
		vAtt.setVersionNumber(1);
		vAtt.setMajorVersion(1);
		vAtt.setMinorVersion(0);
		vAtt.setAgingEnabled(Boolean.FALSE);
		vAtt.setVersionName(versionName);
		vAtt.setRepositoryName(fAtt.getRepositoryName());
		vAtt.setEncrypted(fAtt.getEncrypted());
		vAtt.setEncryptionKey(fAtt.getEncryptionKey());
		fAtt.addFileVersion(vAtt);
	}
	
	
    private boolean closeLock(Binder binder, DefinableEntity entity, 
    		FileAttachment fa, boolean commit, List newObjs) throws RepositoryServiceException,
    		UncheckedIOException {
    	boolean metadataDirty = false;
    	
    	FileAttachment.FileLock lock = fa.getFileLock();
    	
    	if (lock != null) {
			if (commit) { 
				// Commit pending changes if any. We don't care whether the 
				// lock is currently effective or expired.
				commitPendingChanges(binder, entity, fa, lock, newObjs);
			} 
			else { // Discard pending changes if any
				RepositoryUtil.uncheckout(fa.getRepositoryName(), 
						binder, entity, fa.getFileItem().getName()); 					
			}

			fa.setFileLock(null); // Clear the lock

			metadataDirty = true;
		}
    	
    	return metadataDirty;
    }
    
    private void closeExpiredLocksTransactional(Binder binder, DefinableEntity entity,
    		boolean commit) throws RepositoryServiceException,
    		UncheckedIOException {
    	List newObjs = new ArrayList();
    	if(closeExpiredLocks(binder, entity, commit, newObjs) || newObjs.size() > 0)
    		triggerUpdateTransaction(newObjs);   	
    }
    		
    private boolean closeExpiredLocks(Binder binder, DefinableEntity entity,
    		boolean commit, List newObjs) throws RepositoryServiceException,
    		UncheckedIOException {
    	boolean metadataDirty = false; 
    	
		// Iterate over file attachments and close each expired lock.
		Collection<FileAttachment> fAtts = entity.getFileAttachments();
    	for(FileAttachment fa :fAtts) {
			if(closeExpiredLock(binder, entity, fa, commit, newObjs))
				metadataDirty = true;
		}

		return metadataDirty;
    }
    
    /*
    private void closeExpiredLockTransactional(Binder binder, DefinableEntity entity,
    		FileAttachment fa, boolean commit) throws RepositoryServiceException,
    		UncheckedIOException {
    	if(closeExpiredLock(binder, entity, fa, commit))
    		this.triggerUpdateTransaction();
    }*/
    
    private boolean closeExpiredLock(Binder binder, DefinableEntity entity, 
    		FileAttachment fa, boolean commit, List newObjs) throws RepositoryServiceException,
    		UncheckedIOException {
		RepositorySession session = RepositorySessionFactoryUtil.openSession(fa.getRepositoryName(), binder.getResourceDriverName(), ResourceDriverManager.FileOperation.UPDATE, entity);

		try {
			return closeExpiredLock(session, binder, entity, fa, commit, newObjs);
		}
		finally {
			session.close();
		}	
	}
    
    private boolean closeExpiredLock(RepositorySession session,
    		Binder binder, DefinableEntity entity, FileAttachment fa, 
    		boolean commit, List newObjs) throws RepositoryServiceException, UncheckedIOException {
    	boolean metadataDirty = false;
    	
    	FileAttachment.FileLock lock = fa.getFileLock();
    	
    	if(lock != null) {
        	if(isLockExpired(lock)) { // Lock expired
				if(commit) { // Commit pending changes if any
					commitPendingChanges(session, binder, entity, fa, lock, newObjs); 
				}
				else {	// Discard pending changes if any
					session.uncheckout(binder, entity, fa.getFileItem().getName());
				}

				fa.setFileLock(null); // Clear the expired lock

				metadataDirty = true;	
        	}
    	}
    	
    	return metadataDirty;
    }
    
    private boolean commitPendingChanges(Binder binder, DefinableEntity entity,
    		FileAttachment fa, FileAttachment.FileLock lock, List newObjs)
    	throws RepositoryServiceException, UncheckedIOException {
		RepositorySession session = RepositorySessionFactoryUtil.openSession(fa.getRepositoryName(), binder.getResourceDriverName(), ResourceDriverManager.FileOperation.UPDATE, entity);

		try {
			return commitPendingChanges(session, binder, entity, fa,
					lock, newObjs);
		}
		finally {
			session.close();
		}			
    }
    
    private boolean commitPendingChanges(RepositorySession session,
    		Binder binder, DefinableEntity entity, FileAttachment fa, 
    		FileAttachment.FileLock lock, List newObjs)
    	throws RepositoryServiceException, UncheckedIOException {
    	String relativeFilePath = fa.getFileItem().getName();
		
		boolean metadataDirty = false; 
		boolean tryCheckin = false;
		
		if(fileExistsInRepository(fa)) {
			if(session.getFactory().supportSmartCheckin()) {			
				// Attempt to check in. If the file was previously checked out (and
				// only if any change has made since checkout??), this will create 
				// a new version and return the name of the new version. If not,
				// it will return the name of the latest existing version.
				
				// Let the repository make the decision as to whether we should 
				// create a new version or not. Although client has enough info
				// to make this decision, I prefer delegating it to the repository
				// to increase the chance of being in the right state after the
				// call is finished (ie, post-condition is better met this way
				// in an non-ideal world).
				tryCheckin = true;
			}
			else {
				// Let the client make the decision.
				if(Boolean.TRUE.equals(lock.isDirty()))
					tryCheckin = true;
			}
		}
		else {
			// For this file, only the metadata exists, but not the actual file in the
			// repository.
			// This can happen when WebDAV client initializes a new file with zero-length
			// content. Since there's no file in the repository, there is nothing to
			// commit anything to either.
		}
		
		if(tryCheckin) {
			String versionName = session.checkin(binder, entity, relativeFilePath);
			if(versionName != null) {
				// The repository says it created a new version. Although repository
				// wouldn't lie, we still need to check to make sure that we do not
				// end up in a situation where two metadata rows point to the same
				// version in the repository based on the reality that the operations
				// on the repository and the metadata are not performed in a truly
				// atomic manner due to lack of such transactional support (Paranoic?)
				VersionAttachment va = fa.findFileVersion(versionName);
				if(va == null) {
					// The checkin method above returned a version name that we don't
					// know about, which means that it must have created a new version
					// of the file.
					// Note: Never compare the returned version name against the highest
					// version alone in our metadata. It is not guaranteed to work always.
					// Instead, we must check the name against all versions. This is due
					// to the possibility (extremely unlikely but nevertheless theoretically
					// possible) of version out-of-orderness between the metadata side
					// and the repository side. For example, it is possible that V1 in 
					// metadata points to v2 in the repository, while V2 in metadata
					// points to v1 in the repository. This can happen when you have two
					// threads (or two nodes in a cluster) try to create a new version
					// of the same file exactly at the same time. Because the updates
					// to the db metadata and to the repository metadata are not 
					// transactionally atomic, two concurrent requests can in theory
					// result in such intertwined scenario. Consequently the notion of 
					// highest version may not necessarily agree between the application
					// side and the repository side. Therefore it is important to check
					// the returned version name against all versions.
					
					// One more hack(?) - To work around the OpenOffice problem in Bug #651442,
					// we add some additional checking here specific to OpenOffice to prevent
					// extraneous and empty file version to be created in the system.
					long size = session.getContentLengthVersioned(binder, entity, relativeFilePath, versionName);
					if(size == 0 
							&& session.getFactory().isVersionDeletionAllowed()
							// To handle Bug 710153, do not limit file extensions to just native OO files.
							// && isNonzerolengthOpenOfficeFile(relativeFilePath)
							) {
						logger.info("Deleting version [" + versionName + "] of file [" + relativeFilePath +
								"] from repository [" + fa.getRepositoryName() + 
								"] to prevent OO or similar editors from errorneously creating zero-length file.");
						session.deleteVersion(binder, entity, relativeFilePath, versionName);
						VersionAttachment vAtt = fa.getHighestVersion();
						if(vAtt != null) {
							fa.setModification(vAtt.getModification());
							fa.setFileStatus(vAtt.getFileStatus());
							fa.getFileItem().setLength(vAtt.getFileItem().getLength());
							fa.getFileItem().setMd5(vAtt.getFileItem().getMd5());
							fa.getFileItem().setDescription(vAtt.getFileItem().getDescription());
							metadataDirty = true;
						}
					}
					else {
						Long contentLength = Long.valueOf(session.getContentLengthVersioned(binder, entity, 
								relativeFilePath, versionName));
						updateFileAttachment(fa, lock.getOwner(), versionName, contentLength, fa.getFileItem().getMd5(), null, null, null);
						metadataDirty = true;
		            	// add the size of the file to the users disk usage
		            	incrementDiskSpaceUsed(fa);		
		            	ChangeLog changes = new ChangeLog(entity, ChangeLog.FILEMODIFY);
		            	ChangeLogUtils.buildLog(changes, fa);
		            	newObjs.add(changes);
					}
				}  
			}
		}
		
		lock.setDirty(Boolean.FALSE);
		
		return metadataDirty;
    }
    
    private boolean isNonzerolengthOpenOfficeFile(String fileName) {
		fileName = fileName.toLowerCase();
		for (String s : ooNonzerolengthExts)
			if (fileName.endsWith(s))
				return true;
		return false;
	}
    
    private void checkReservation(DefinableEntity entity) 
    	throws ReservedByAnotherUserException {
		User user = RequestContextHolder.getRequestContext().getUser();

    	HistoryStamp reservation = null;
    	if(entity instanceof Reservable)
    		reservation = ((Reservable) entity).getReservation();
    	
    	if(reservation != null && !reservation.getPrincipal().equals(user)) {
    		// The entry is currently under reservation by another user.
    		throw new ReservedByAnotherUserException((Reservable) entity);
    	}
    }
    
    private void checkLock(DefinableEntity entity, FileAttachment fa) 
    	throws LockedByAnotherUserException {
    	// This method assumes that the lock has been brought up-to-date
    	// prior to the execution of this method. In other words, expired
    	// lock has already been taken care of. Therefore the lock, if
    	// exists, is effective. 
    	
		User user = RequestContextHolder.getRequestContext().getUser();

		FileLock lock = fa.getFileLock();
		if(lock != null && !lock.getOwner().equals(user)) {
			// The file is locked by another user.
			throw new LockedByAnotherUserException(entity, fa, lock.getOwner());
		}
    }
    
    private boolean isLockExpired(FileLock lock) {
    	// Note that we take additional 
		// "allowance" value into consideration when computing the
		// expiration date used for the comparison. This is to 
		// account for the situation where subsequent lock renewal
		// or data update request following initial lock request is
		// delayed significantly that it actually arrives at the server
		// after the initial lock has expired. Although expected to be
		// rare, this situation can occur by environmental flunctuations
		// such as unusual network latency or extremely high system 
		// load, etc. To prevent undesired version proliferation from
		// occuring as result, we give each lock some reasonable 
		// allowance (ie, extended life).
    	
    	return (lock.getExpirationDate().getTime() + 
    			this.getLockExpirationAllowanceMilliseconds() <= System.currentTimeMillis());
    }
    
    public FileAttachment getFileAttachmentById(String fileId) 
    		throws AccessControlException {
		User user = RequestContextHolder.getRequestContext().getUser();
    	FileAttachment fa = null;
    	fa = (FileAttachment) coreDao.load(FileAttachment.class, fileId);
    	if (fa != null) {
    		//Check the access to the owning entity
    		DefinableEntity entity = fa.getOwner().getEntity();
    		if (entity instanceof Binder) {
    			getBinderModule().checkAccess(entity.getId(), user);
    		} else if (entity instanceof FolderEntry) {
    			getFolderModule().checkAccess(((FolderEntry)entity), FolderOperation.readEntry);
    		}
    	}
    	return fa;
    }
    
    @SuppressWarnings("unchecked")
	private void setCustomAttribute(DefinableEntity entry, FileUploadItem fui, FileAttachment fAtt, boolean addToFront) {
    	// Is the FileUploadItem named?
		Set fAtts = null;
		String fuiName = fui.getName();
		if ((null == fuiName) || (0 == fuiName.length())) {
			// No!  Is the entry it's being uploaded to a file entry?
			Definition def = null;
			if(entry.getEntryDefId() != null)
				def = getCoreDao().loadDefinition(entry.getEntryDefId(), RequestContextHolder.getRequestContext().getZoneId());
			String defId = ((null == def) ? null : def.getInternalId());
			if ((null == defId) || (!(defId.equalsIgnoreCase(ObjectKeys.DEFAULT_LIBRARY_ENTRY_DEF)))) {
				// No!  Then we don't store a custom attribute for it.
				return;
			}
			
			// Yes, it's a file entry!  Use upload for the
			// FileUploadItem's name.  This will cause it to play
			// nicely with the other uploaded items in the entry.
			fuiName = "upload";
		}
		
		// Find custom attribute by the attribute name. 
		CustomAttribute ca = entry.getCustomAttribute(fuiName);
		if (ca != null) {
			fAtts = ((Set) ca.getValueSet());
		} else {
			fAtts = new LinkedHashSet();
		}

		// Simply because the file already exists for the entry does
		// not mean that it is known through this particular data
		// element (i.e., custom attribute). So we need to make
		// sure that it is made visible through this element.
		if (!addToFront || fAtts.isEmpty() || !(fAtts instanceof LinkedHashSet)) {
			//Just add this item. It will be put at the back of the set
			fAtts.add(fAtt); // If it is already in the set, this will have no effect.
		} else {
			//We want to add this attachment to the front of the set
			LinkedHashSet fAttsOrdered = new LinkedHashSet();
			fAttsOrdered.add(fAtt);      //Put the new item at the front
			fAttsOrdered.addAll(fAtts);  //Add the rest of the items in the order they were in
			fAtts = fAttsOrdered;
		}
		if (ca != null) {
			ca.setValue(fAtts);
		} else {
			entry.addCustomAttribute(fuiName, fAtts);
		}
   }
    
    private void incrementDiskSpaceUsed(FileAttachment fAtt) {
    	ZoneConfig zoneConf = getCoreDao().loadZoneConfig(
				RequestContextHolder.getRequestContext()
				.getZoneId());
    	if (zoneConf.isDiskQuotaEnabled() && !fAtt.getRepositoryName().equalsIgnoreCase(ObjectKeys.FI_ADAPTER)) {
    		UserPrincipal up = fAtt.getModification().getPrincipal();
    		if(up instanceof User) {        		
        		User user = (User) up;
        		user.incrementDiskSpaceUsed(fAtt.getFileItem().getLength());
    		}
    	} 	
    	if (zoneConf.isBinderQuotaInitialized() && !fAtt.getRepositoryName().equalsIgnoreCase(ObjectKeys.FI_ADAPTER)) {
        	DefinableEntity entity = fAtt.getOwner().getEntity();
        	Binder binder;
        	if (entity instanceof Entry) {
        		binder = ((Entry)entity).getParentBinder();
        	} else {
        		binder = (Binder) entity;
        	}
    		getBinderModule().incrementDiskSpaceUsed(binder, fAtt.getFileItem().getLength());
    	} 	
    }
    
    protected void setEntityModification(DefinableEntity entity) {
    	User user = RequestContextHolder.getRequestContext().getUser();
    	Calendar now = Calendar.getInstance();
    	now.setTime(new Date());
		entity.setModification(new HistoryStamp(user, now.getTime()));

    }
    
    protected void setFileAttachmentModification(FileAttachment fa) {
    	User user = RequestContextHolder.getRequestContext().getUser();
    	Calendar now = Calendar.getInstance();
    	now.setTime(new Date());
		fa.setModification(new HistoryStamp(user, now.getTime()));
    }
    
    private boolean fileExistsInRepository(FileAttachment fa) {
    	// This special condition is introduced to deal with the scenario 
    	// reported in the Bug #632279.
    	return (fa.getFileVersionsUnsorted().size() > 0);
    }
    
	public int checkQuotaAndFileSizeLimit(Long userId, Binder binder, long fileSize, String fileName) {
    	User user;
    	if(userId != null)
    		user = (User)getProfileDao().loadUserDeadOrAlive(userId, RequestContextHolder.getRequestContext().getZoneId());
    	else
    		user = RequestContextHolder.getRequestContext().getUser();
		try {
			checkQuota(user, fileSize, fileName);
		}
		catch(DataQuotaException e) {
			return 1; // user quota
		}
		try {
			checkBinderQuota(binder, fileSize, fileName);
		}
		catch(DataQuotaException e) {
			return 2; // binder quota
		}
		try {
			checkFileSizeLimit(binder, fileSize, fileName);
		}
		catch(DataQuotaException e) {
			return 3; // file size limit
		}
		return 0;
	}
	
}
