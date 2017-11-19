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
package org.kablink.teaming.module.template.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.kablink.teaming.ConfigurationException;
import org.kablink.teaming.NoObjectByTheIdException;
import org.kablink.teaming.NotSupportedException;
import org.kablink.teaming.ObjectKeys;
import org.kablink.teaming.context.request.RequestContextHolder;
import org.kablink.teaming.dao.util.FilterControls;
import org.kablink.teaming.domain.Binder;
import org.kablink.teaming.domain.BinderQuota;
import org.kablink.teaming.domain.CustomAttribute;
import org.kablink.teaming.domain.Dashboard;
import org.kablink.teaming.domain.Definition;
import org.kablink.teaming.domain.Description;
import org.kablink.teaming.domain.EntityDashboard;
import org.kablink.teaming.domain.HistoryStamp;
import org.kablink.teaming.domain.NoBinderByTheNameException;
import org.kablink.teaming.domain.NoDefinitionByTheIdException;
import org.kablink.teaming.domain.Principal;
import org.kablink.teaming.domain.TemplateBinder;
import org.kablink.teaming.domain.UserPrincipal;
import org.kablink.teaming.domain.Workspace;
import org.kablink.teaming.domain.EntityIdentifier.EntityType;
import org.kablink.teaming.module.admin.AdminModule;
import org.kablink.teaming.module.binder.BinderModule;
import org.kablink.teaming.module.binder.BinderModule.BinderOperation;
import org.kablink.teaming.module.binder.impl.WriteEntryDataException;
import org.kablink.teaming.module.binder.processor.BinderProcessor;
import org.kablink.teaming.module.dashboard.DashboardModule;
import org.kablink.teaming.module.definition.DefinitionModule;
import org.kablink.teaming.module.definition.DefinitionUtils;
import org.kablink.teaming.module.file.FileModule;
import org.kablink.teaming.module.file.WriteFilesException;
import org.kablink.teaming.module.folder.FolderModule;
import org.kablink.teaming.module.impl.CommonDependencyInjection;
import org.kablink.teaming.module.shared.EntryBuilder;
import org.kablink.teaming.module.shared.InputDataAccessor;
import org.kablink.teaming.module.shared.MapInputData;
import org.kablink.teaming.module.shared.ObjectBuilder;
import org.kablink.teaming.module.shared.XmlUtils;
import org.kablink.teaming.module.template.TemplateModule;
import org.kablink.teaming.module.workspace.WorkspaceModule;
import org.kablink.teaming.search.IndexSynchronizationManager;
import org.kablink.teaming.security.AccessControlException;
import org.kablink.teaming.security.function.Function;
import org.kablink.teaming.security.function.WorkAreaFunctionMembership;
import org.kablink.teaming.security.function.WorkAreaOperation;
import org.kablink.teaming.util.NLT;
import org.kablink.teaming.util.SZoneConfig;
import org.kablink.teaming.util.Utils;
import org.kablink.teaming.web.util.DashboardHelper;
import org.kablink.util.GetterUtil;
import org.kablink.util.Validator;

import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;


public class TemplateModuleImpl extends CommonDependencyInjection implements
		TemplateModule {
	private static final String[] defaultDefAttrs = new String[]{ObjectKeys.FIELD_INTERNALID, ObjectKeys.FIELD_ENTITY_DEFTYPE};

    protected DefinitionModule definitionModule;
	/**
	 * Setup by spring
	 * @param definitionModule
	 */
	public void setDefinitionModule(DefinitionModule definitionModule) {
		this.definitionModule = definitionModule;
	}
   	protected DefinitionModule getDefinitionModule() {
		return definitionModule;
	}

	protected AdminModule adminModule;
	public void setAdminModule(AdminModule adminModule) {
		this.adminModule = adminModule;
	}
   	protected AdminModule getAdminModule() {
		return adminModule;
	}

	protected WorkspaceModule workspaceModule;
	public void setWorkspaceModule(WorkspaceModule workspaceModule) {
		this.workspaceModule = workspaceModule;
	}
   	protected WorkspaceModule getWorkspaceModule() {
		return workspaceModule;
	}

	protected FolderModule folderModule;
	public void setFolderModule(FolderModule folderModule) {
		this.folderModule = folderModule;
	}
   	protected FolderModule getFolderModule() {
		return folderModule;
	}
 
	protected BinderModule binderModule;
	public void setBinderModule(BinderModule binderModule) {
		this.binderModule = binderModule;
	}
   	protected BinderModule getBinderModule() {
		return binderModule;
	}

   	protected FileModule fileModule;
   	public void setFileModule(FileModule fileModule) {
   		this.fileModule = fileModule;
   	}
   	protected FileModule getFileModule() {
   		return fileModule;
   	}	

	protected DashboardModule dashboardModule;
	public void setDashboardModule(DashboardModule dashboardModule) {
		this.dashboardModule = dashboardModule;
	}
   	protected DashboardModule getDashboardModule() {
		return dashboardModule;
	}

   	private TransactionTemplate transactionTemplate;
    protected TransactionTemplate getTransactionTemplate() {
		return transactionTemplate;
	}
	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	private BinderProcessor loadBinderProcessor(Binder binder) {
		return (BinderProcessor)getProcessorManager().getProcessor(binder, binder.getProcessorKey(BinderProcessor.PROCESSOR_KEY));
	}

	public boolean updateDefaultTemplates(Long topId, boolean replace) {
		Workspace top = (Workspace)getCoreDao().loadBinder(topId, topId);
		boolean result = true;
		
		//default definitions stored in separate config file
		String startupConfig = SZoneConfig.getString(top.getName(), "property[@name='startupConfig']", "config/startup.xml");
		SAXReader reader = new SAXReader(false);  
		InputStream in=null;
		try {
			in = new ClassPathResource(startupConfig).getInputStream();
			Document cfg = reader.read(in);
			in.close();
			//Now setup configurations
			List elements = cfg.getRootElement().selectNodes("templateFile");
			for (int i=0; i<elements.size(); ++i) {
				Element element = (Element)elements.get(i);
				String file = element.getTextTrim();
				reader = new SAXReader(false);  
				try {
					in = new ClassPathResource(file).getInputStream();
					Document doc = reader.read(in);
					Long templateId;
					try {
						templateId = addTemplate(null, doc, replace).getId();
					} catch(Exception e) {
						if (replace) {
							//Failed to replace the template, throw error
							throw e;
						}
						templateId = null;
					}
					if (templateId == null) result = false;
					getCoreDao().flush();
				} catch (Exception ex) {
					logger.error("Cannot add template:" + file + " " + ex.getMessage());
					return false; //cannot continue, rollback is enabled
				} finally {
					if (in!=null) in.close();
				}
			}
		} catch (Exception ex) {
			logger.error("Cannot read startup configuration:", ex);
		}
		return result;
	}
    //These should be created when the zone is created, but just incase provide minimum backup
	public TemplateBinder addDefaultTemplate(int type) {
	   	//This is called as a side effect to bootstrap
		Long zoneId = RequestContextHolder.getRequestContext().getZoneId();
		List defs = new ArrayList();
		TemplateBinder config = new TemplateBinder();
		Definition entryDef;
		switch (type) {
			case Definition.FOLDER_VIEW: {
				List result = getCoreDao().loadObjects(TemplateBinder.class, 
							new FilterControls(defaultDefAttrs, new Object[]{ObjectKeys.DEFAULT_FOLDER_CONFIG, Integer.valueOf(type)}), zoneId);
				if (!result.isEmpty()) return (TemplateBinder)result.get(0);
				config.setTemplateTitle("__template_default_folder");
				config.setTemplateDescription("__template_default_folder_description");
				config.setInternalId(ObjectKeys.DEFAULT_FOLDER_CONFIG);
				entryDef = getDefinitionModule().addDefaultDefinition(Definition.FOLDER_VIEW);
				config.setEntryDef(entryDef);
				defs.add(entryDef);
				defs.add(getDefinitionModule().addDefaultDefinition(Definition.FOLDER_ENTRY));
				break;
			}
			case Definition.WORKSPACE_VIEW: {
				List result = getCoreDao().loadObjects(TemplateBinder.class, 
						new FilterControls(defaultDefAttrs, new Object[]{ObjectKeys.DEFAULT_WORKSPACE_CONFIG, Integer.valueOf(type)}), zoneId);
				if (!result.isEmpty()) return (TemplateBinder)result.get(0);
				config.setTemplateTitle("__template_workspace");
				config.setTemplateDescription("__template_workspace_description");
				config.setInternalId(ObjectKeys.DEFAULT_WORKSPACE_CONFIG);
				entryDef = getDefinitionModule().addDefaultDefinition(type);
				config.setEntryDef(entryDef);
				defs.add(entryDef);
				break;
			}
			case Definition.USER_WORKSPACE_VIEW: {
				List result = getCoreDao().loadObjects(TemplateBinder.class, 
						new FilterControls(defaultDefAttrs, new Object[]{ObjectKeys.DEFAULT_USER_WORKSPACE_CONFIG, Integer.valueOf(type)}), zoneId);
				if (!result.isEmpty()) return (TemplateBinder)result.get(0);
				
				config.setTemplateTitle("__template_user_workspace");
				config.setTemplateDescription("__template_user_workspace_description");
				config.setInternalId(ObjectKeys.DEFAULT_USER_WORKSPACE_CONFIG);
				entryDef = getDefinitionModule().addDefaultDefinition(type);
				config.setEntryDef(entryDef);
				defs.add(entryDef);
				break;
			}
			case Definition.EXTERNAL_USER_WORKSPACE_VIEW: {
				List result = getCoreDao().loadObjects(TemplateBinder.class, 
						new FilterControls(defaultDefAttrs, new Object[]{ObjectKeys.DEFAULT_EXTERNAL_USER_WORKSPACE_CONFIG, Integer.valueOf(type)}), zoneId);
				if (!result.isEmpty()) return (TemplateBinder)result.get(0);
				
				config.setTemplateTitle("__template_external_user_workspace");
				config.setTemplateDescription("__template_external_user_workspace_description");
				config.setInternalId(ObjectKeys.DEFAULT_EXTERNAL_USER_WORKSPACE_CONFIG);
				entryDef = getDefinitionModule().addDefaultDefinition(type);
				config.setEntryDef(entryDef);
				defs.add(entryDef);
				break;
			}
		default: {
			throw new IllegalArgumentException("Invalid type:" + type);
			}
		}
		
		Map updates = new HashMap();
		config.setTitle(config.getTemplateTitle());
		config = doAddTemplate(config, type, updates);
		config.setDefinitionsInherited(false);
		config.setDefinitions(defs);
		
		if (type == Definition.USER_WORKSPACE_VIEW) {
			//get default folder definition
			HistoryStamp stamp = new HistoryStamp(RequestContextHolder.getRequestContext().getUser());
			TemplateBinder def = addDefaultTemplate(Definition.FOLDER_VIEW);
			TemplateBinder newDef = new TemplateBinder(def);
			newDef.setCreation(stamp);
			newDef.setModification(stamp);
			newDef.setOwner(null);
			newDef.setFunctionMembershipInherited(true);
			newDef.setPathName(config.getPathName() + "/" + newDef.getTitle());
			getCoreDao().save(newDef);  //generateId for binderKey needed by custom attributes
			config.addBinder(newDef);
			copyBinderAttributes(def, newDef);
			
		}
		return config;
	 }
	//add template
	 public TemplateBinder addTemplate(Binder localBinderParent, int type, Map updates) {
	    checkAccess(TemplateOperation.manageTemplate);
		TemplateBinder template = new TemplateBinder();
		String name = (String)updates.get(ObjectKeys.FIELD_BINDER_NAME);
		//only top level needs a name
		if (Validator.isNull(name)) {
			 name = (String)updates.get(ObjectKeys.FIELD_TEMPLATE_TITLE);
			 if (Validator.isNull(name)) {
				 throw new IllegalArgumentException(NLT.get("general.required.name"));
			 }
		}
		if (localBinderParent != null) {
			//This is a local template. Store the parent binder id
			template.setTemplateOwningBinderId(localBinderParent.getId());
		}
		template.setName(name);
		if (!validateTemplateName(null, name)) throw new NotSupportedException("errorcode.notsupported.duplicateTemplateName", new Object[]{name});
		Definition entryDef = getDefinitionModule().addDefaultDefinition(type);
		template.setEntryDef(entryDef);
		if (type == Definition.FOLDER_VIEW) template.setLibrary(true);
		List definitions = new ArrayList();
		definitions.add(entryDef);
		template.setDefinitionsInherited(false);
		template.setDefinitions(definitions);
		template.setFunctionMembershipInherited(true);
       	String icon = DefinitionUtils.getPropertyValue(entryDef.getDefinition().getRootElement(), "icon");
       	if (Validator.isNotNull(icon)) template.setIconName(icon);
		doAddTemplate(template, type, updates);
	    return template;
	 }
	 public TemplateBinder addTemplate(Binder localBinderParent, InputStream indoc, boolean replace) 
	 	throws AccessControlException, DocumentException {
		 SAXReader xIn = new SAXReader(false);
		 Document doc = xIn.read(indoc);
		 return addTemplate(localBinderParent, doc, replace);
	 }
		//add top level template
	 public TemplateBinder addTemplate(Binder localBinderParent, Document doc, boolean replace) {
		 if (localBinderParent == null) {
			 //This is a zone wide template
			 checkAccess(TemplateOperation.manageTemplate);
		 } else {
			 //This is a local template, check access to the folder
			 getBinderModule().checkAccess(localBinderParent, BinderOperation.manageConfiguration);
		 }
		 Element config = doc.getRootElement();
		 //check name
		 String name = (String)XmlUtils.getCustomAttribute(config, ObjectKeys.XTAG_BINDER_NAME);
		 if (Validator.isNull(name)) {
			 name = (String)XmlUtils.getCustomAttribute(config, ObjectKeys.XTAG_TEMPLATE_TITLE);
			 if (Validator.isNull(name)) {
				 throw new IllegalArgumentException(NLT.get("general.required.name"));
			 }
		 }
		 String internalId = config.attributeValue(ObjectKeys.XTAG_ATTRIBUTE_INTERNALID);
		 if (Validator.isNotNull(internalId)) {
			 if (localBinderParent != null) {
				 throw new NotSupportedException("errorcode.notsupported.cannotImportGlobalTemplate");
			 }
			 try {
			 //	see if it exists
				 Binder binder = getCoreDao().loadReservedBinder(internalId, RequestContextHolder.getRequestContext().getZoneId());
				 if (binder instanceof TemplateBinder) {
					 //if it exists, delete it
					 if (replace) getBinderModule().deleteBinder(binder.getId());
					 else throw new NotSupportedException("errorcode.notsupported.duplicateTemplateName", new Object[]{internalId});
				 } else {
					 throw new ConfigurationException("Reserved binder exists with same internal id");
				 }
			 } catch (NoBinderByTheNameException nb ) {
				 //	okay doesn't exists
			 }; 
		 } else {
			 List<TemplateBinder> binders = getCoreDao().loadObjects(TemplateBinder.class, new FilterControls(ObjectKeys.FIELD_BINDER_NAME, name), RequestContextHolder.getRequestContext().getZoneId());
			 if (!binders.isEmpty()) {
				 if (localBinderParent != null) {
					 //Make sure these templates really belong to this binder
					 for (TemplateBinder tb : binders) {
						 if (!localBinderParent.getId().equals(tb.getTemplateOwningBinderId())) {
							 throw new NotSupportedException("errorcode.notsupported.cannotImportLocalTemplate", new Object[]{name});
						 }
					 }
				 }
				 if (replace) {
					 getBinderModule().deleteBinder(binders.get(0).getId());
				 } else {
					 throw new NotSupportedException("errorcode.notsupported.duplicateTemplateName", new Object[]{name});
				 }
			 }
		 }
		 TemplateBinder template = new TemplateBinder();
		 //only top level needs a name
		 template.setName(name);
		 template.setInternalId((internalId));
		 if (localBinderParent != null) {
			 //Mark this template as local to the parent binder
			 template.setTemplateOwningBinderId(localBinderParent.getId());
		 }
		 doTemplate(template, config);
		 
		 //see if any child configs need to be copied
		 doChildTemplates(template, config);
		 
		 //need to flush, if multiple loaded in 1 transaction the binderKey may not have been
		 //flushed which could result in duplicates on the next save when loading multiple nfor updatetTemplates
		 getCoreDao().flush();
		 return template;
	 }
	 protected void doChildTemplates(TemplateBinder parent, Element config) {
		 List nodes = config.selectNodes("./" + ObjectKeys.XTAG_ELEMENT_TYPE_TEMPLATE);
		 if (nodes == null || nodes.isEmpty()) return;
		 for (int i=0; i<nodes.size(); ++i) {
			 Element element = (Element)nodes.get(i);
			 TemplateBinder child = new TemplateBinder();
			 parent.addBinder(child);
			 doTemplate(child, element);
			 //See if there are sub-binders to do under this binder
			 doChildTemplates(child, element);
		 }
	 }
	 protected void doTemplate(TemplateBinder template, Element config) {
		 Integer type = Integer.valueOf(config.attributeValue(ObjectKeys.XTAG_ATTRIBUTE_TYPE));
		 template.setLibrary(GetterUtil.get(XmlUtils.getProperty(config, ObjectKeys.XTAG_BINDER_LIBRARY), false));
		 template.setMirrored(GetterUtil.get(XmlUtils.getProperty(config, ObjectKeys.XTAG_BINDER_MIRRORED), false));
		 template.setUniqueTitles(GetterUtil.get(XmlUtils.getProperty(config, ObjectKeys.XTAG_BINDER_UNIQUETITLES), false));
		 template.setDefinitionsInherited(GetterUtil.get(XmlUtils.getProperty(config, ObjectKeys.XTAG_BINDER_INHERITDEFINITIONS), false));
		 template.setFunctionMembershipInherited(GetterUtil.get(XmlUtils.getProperty(config, ObjectKeys.XTAG_BINDER_INHERITFUNCTIONMEMBERSHIP), true));
		 template.setTeamMembershipInherited(GetterUtil.get(XmlUtils.getProperty(config, ObjectKeys.XTAG_BINDER_INHERITTEAMMEMBERS), true));
		 //get attribute from document
		 Map updates = XmlUtils.getCustomAttributes(config);
		 doAddTemplate(template, type, updates);
		 //setup after template is saved
		 if (!template.isTeamMembershipInherited()) XmlUtils.getTeamMembersFromXml(template, config, this);
		 XmlUtils.getDefinitionsFromXml(template, config, this);
		 if (!template.isFunctionMembershipInherited()) XmlUtils.getFunctionMembershipFromXml(template, config, this);
		 template.setEntryDef(template.getDefaultViewDef());
		 Element dashboardConfig = (Element)config.selectSingleNode(ObjectKeys.XTAG_ELEMENT_TYPE_DASHBOARD);
		 if (dashboardConfig != null) {
			getDashboardModule().createEntityDashboard(template.getEntityIdentifier(), dashboardConfig);
		 }
	 }
	 protected TemplateBinder doAddTemplate(TemplateBinder template, int type, Map updates) {
		 template.setZoneId(RequestContextHolder.getRequestContext().getZoneId());
		 template.setDefinitionType(type);
		 template.setCreation(new HistoryStamp(RequestContextHolder.getRequestContext().getUser()));
		 template.setModification(template.getCreation());
		 getCoreDao().save(template); //need id for custom attributes
		 if (template.isRoot()) {
			 template.setupRoot();
			 getCoreDao().flush(); //flush cause binderSortKey is null before setup and need to reserve unique key
		 }
		 EntryBuilder.updateEntry(template, updates);
		 if (Validator.isNull(template.getTitle())) template.setTitle(template.getTemplateTitle());
		 if (template.isRoot()) {
			 template.setPathName("/" + template.getTitle());
		 } else {
			 template.setPathName(template.getParentBinder().getPathName() + "/" + template.getTitle());
		 }
		 return template;
	 }

	 //clone a top level template as a child of another template
	 public TemplateBinder addTemplate(Long parentId, Long srcConfigId) {
		 checkAccess(TemplateOperation.manageTemplate);
	    TemplateBinder parentConfig = (TemplateBinder)getCoreDao().loadBinder(parentId, RequestContextHolder.getRequestContext().getZoneId());
	    TemplateBinder srcConfig = (TemplateBinder)getCoreDao().loadBinder(srcConfigId, RequestContextHolder.getRequestContext().getZoneId());
	    return addTemplate(parentConfig, srcConfig);
	 }
	 protected Binder copyBinderAttributes(Binder source, Binder destination) {
		 EntityDashboard dashboard = getCoreDao().loadEntityDashboard(source.getEntityIdentifier());
		 if (dashboard != null) {
			 EntityDashboard myDashboard = new EntityDashboard(dashboard);
			 myDashboard.setOwnerIdentifier(destination.getEntityIdentifier());
			 getCoreDao().save(myDashboard);
		 }
		 //copy all file attachments
		 getFileModule().copyFiles(source, source, destination, destination, null);
		 EntryBuilder.copyAttributes(source, destination);
		 //Set quota (if any)
		 if (getBinderModule().isBinderDiskQuotaEnabled()) {
			 BinderQuota quotaSource = getAdminModule().getBinderQuota(source);
			 BinderQuota quotaDestination = getAdminModule().getBinderQuota(destination);
			 if (quotaSource != null && quotaDestination != null) {
				 quotaDestination.setDiskQuota(quotaSource.getDiskQuota());
				 getAdminModule().setBinderQuota(destination, quotaDestination);
			 }
		 }
		 //Copy version controls, etc
		 if (source.getVersionsEnabled() != null) {
			 //Version controls are not inherited, so copy them
			 destination.setVersionsEnabled(source.getVersionsEnabled());
			 destination.setVersionsToKeep(source.getVersionsToKeep());
		 }
		 if (source.getVersionAgingEnabled() != null) {
			 destination.setVersionAgingEnabled(source.getVersionAgingEnabled());
			 destination.setVersionAgingDays(source.getVersionAgingDays());
		 }
		 destination.setMaxFileSize(source.getMaxFileSize());
		 destination.setFileEncryptionEnabled(getBinderModule().isBinderFileEncryptionEnabled(source));
		 return destination;		
	}
	 protected TemplateBinder addTemplate(TemplateBinder parentConfig, TemplateBinder srcConfig) {
		 TemplateBinder config = new TemplateBinder(srcConfig);
		 config.setCreation(new HistoryStamp(RequestContextHolder.getRequestContext().getUser()));
		 config.setModification(config.getCreation());
		 config.setOwner(null);
		 if (Validator.isNull(config.getTitle())) config.setTitle(config.getTemplateTitle());
		 config.setPathName(parentConfig.getPathName() + "/" + config.getTitle());
      	 getCoreDao().updateFileName(parentConfig, config, null, config.getTitle());
		 //get childen before adding new children incase parent and source are the same
		 List<TemplateBinder> children = new ArrayList(srcConfig.getBinders());
      	 
		 parentConfig.addBinder(config);  //setup binder_sortKey
		 getCoreDao().save(config); // generateId for binderKey needed by custom attributes
    	 copyBinderAttributes(srcConfig, config);
	     if (!config.isFunctionMembershipInherited()) {
	    	 //copy to new template
	    	 getWorkAreaFunctionMembershipManager().copyWorkAreaFunctionMemberships(config.getZoneId(), srcConfig, config);
	     }

		 for (TemplateBinder c:children) {
			 addTemplate(config, c);
		 }
		 return config;
	 }
	 protected boolean validateTemplateName(Long binderId, String name) {
		 if (Validator.isNull(name)) return false;
			try {
				TemplateBinder binder = getTemplateByName(name);
				if (binderId != null && binder.getId().equals(binderId)) return true;
			} catch (NoBinderByTheNameException nb) {
				return true;
			} catch (Exception ex) {};
		 return false;
	 }
	   public TemplateBinder addTemplateFromBinder(Binder localParentBinder, Long binderId) throws AccessControlException, WriteFilesException {
		   checkAccess(TemplateOperation.manageTemplate);
		   Long zoneId =  RequestContextHolder.getRequestContext().getZoneId();
		   Binder binder = (Binder)getCoreDao().loadBinder(binderId, zoneId);
		   TemplateBinder config = templateFromBinder(localParentBinder, null, binder);
		   return config;
		}
		protected TemplateBinder templateFromBinder(Binder localParentBinder, TemplateBinder parent, Binder binder) {
			//get binder setup
			if (binder.getDefinitionType() == null) {
				getDefinitionModule().setDefaultBinderDefinition(binder);
			}	
			TemplateBinder config = new TemplateBinder(binder);
			if (localParentBinder != null) {
				//This is a local template, set the parentBinder id
				config.setTemplateOwningBinderId(localParentBinder.getId());
			}
			if (parent == null) {
				//need a name				   
				String name = config.getName();
				if (Validator.isNull(name)) name = config.getTemplateTitle();
				int next=1;
				while (!validateTemplateName(null, name)) {
					name = name + next++;
				}
				config.setName(name);
			} else {
				config.setName(null);
			}
			config.setCreation(new HistoryStamp(RequestContextHolder.getRequestContext().getUser()));
			config.setModification(config.getCreation());
			config.setOwner(null);
			if (parent == null) {
				config.setPathName("/" + config.getTitle());				
			} else {
				config.setPathName(parent.getPathName() + "/" + config.getTitle());				
			}
			getCoreDao().save(config); //need id for binderKey
	      	if (parent != null) {
				parent.addBinder(config);
	      		getCoreDao().updateFileName(parent, config, null, config.getTitle());
	      		// Flush so that correct binderSortKey be written out for this node
	      		// before the caller continues to the next sibling of this node.
	      		// Otherwise, we will get UNIQUE KEY constraint error (bug #659137)
	      		getCoreDao().flush();
	      	} else {
	      		config.setupRoot();
	      		getCoreDao().flush();//flush cause binderSortKey is null before setup
	      	}
			copyBinderAttributes(binder, config);
			if (!config.isFunctionMembershipInherited()) {
				getWorkAreaFunctionMembershipManager().copyWorkAreaFunctionMemberships(binder.getZoneId(), binder, config);
			}
			List<Binder> children = binder.getBinders();    
			for (Binder child: children) {
				templateFromBinder(localParentBinder, config, child);	    	
			}
			return config;
			
	}
	public void modifyTemplate(Long id, Map updates) {
		checkAccess(TemplateOperation.manageTemplate);
		TemplateBinder config = getCoreDao().loadTemplate(id, RequestContextHolder.getRequestContext().getZoneId());
		if (config.isRoot() && updates.containsKey(ObjectKeys.FIELD_BINDER_NAME)) { //ignore name for others
			String name = (String)updates.get(ObjectKeys.FIELD_BINDER_NAME);
			if (Validator.isNull(name)) {
				throw new IllegalArgumentException(NLT.get("general.required.name"));
			} else if (!validateTemplateName(id, name)) {
				try {
					TemplateBinder binder = getTemplateByName(name);
					if (!binder.getId().equals(id)) {
						throw new NotSupportedException("errorcode.notsupported.duplicateTemplateName", new Object[]{name});
					}
				} catch (org.springframework.dao.IncorrectResultSizeDataAccessException in) {
					throw new NotSupportedException("errorcode.notsupported.duplicateTemplateName", new Object[]{name});
				}
			}
			config.setName(name);
		 }
		if (updates.containsKey(ObjectKeys.FIELD_TEMPLATE_TITLE)) {
			config.setTemplateTitle((String)updates.get(ObjectKeys.FIELD_TEMPLATE_TITLE));
		}
		if (updates.containsKey(ObjectKeys.FIELD_TEMPLATE_DESCRIPTION)) {
			config.setTemplateDescription((Description)updates.get(ObjectKeys.FIELD_TEMPLATE_DESCRIPTION));
		}

		ObjectBuilder.updateObject(config, updates);
	}
	public Document getTemplateAsXml(TemplateBinder binder) {
		Document doc = DocumentHelper.createDocument();
		Element element = doc.addElement(ObjectKeys.XTAG_ELEMENT_TYPE_TEMPLATE);
		getTemplateAsXml(binder, element);
		return doc;
	}
	protected void getTemplateAsXml(TemplateBinder binder, Element element) {
		Integer defType = binder.getDefinitionType();
		if (defType == null) defType = 0;
		element.addAttribute(ObjectKeys.XTAG_ATTRIBUTE_TYPE, defType.toString());
		element.addAttribute(ObjectKeys.XTAG_ATTRIBUTE_INTERNALID, binder.getInternalId());
		
		XmlUtils.addCustomAttribute(element, ObjectKeys.XTAG_TEMPLATE_TITLE, ObjectKeys.XTAG_TYPE_STRING, binder.getTemplateTitle());
		XmlUtils.addCustomAttribute(element, ObjectKeys.XTAG_TEMPLATE_DESCRIPTION, ObjectKeys.XTAG_TYPE_DESCRIPTION, binder.getTemplateDescription());
		XmlUtils.addCustomAttribute(element, ObjectKeys.XTAG_TEMPLATE_HIDDEN, ObjectKeys.XTAG_TYPE_BOOLEAN, binder.getTemplateHidden());
		XmlUtils.addCustomAttribute(element, ObjectKeys.XTAG_ENTITY_TITLE, ObjectKeys.XTAG_TYPE_STRING, binder.getTitle());
		XmlUtils.addCustomAttribute(element, ObjectKeys.XTAG_ENTITY_DESCRIPTION, ObjectKeys.XTAG_TYPE_DESCRIPTION, binder.getDescription());
		XmlUtils.addCustomAttribute(element, ObjectKeys.XTAG_ENTITY_ICONNAME, ObjectKeys.XTAG_TYPE_STRING, binder.getIconName());			
		if (binder.isRoot()) XmlUtils.addCustomAttribute(element, ObjectKeys.XTAG_BINDER_NAME, ObjectKeys.XTAG_TYPE_STRING, binder.getName());

		XmlUtils.addProperty(element, ObjectKeys.XTAG_BINDER_LIBRARY, binder.isLibrary());
		XmlUtils.addProperty(element, ObjectKeys.XTAG_BINDER_MIRRORED, binder.isMirrored());
		XmlUtils.addProperty(element, ObjectKeys.XTAG_BINDER_UNIQUETITLES, binder.isUniqueTitles());
		XmlUtils.addProperty(element, ObjectKeys.XTAG_BINDER_INHERITFUNCTIONMEMBERSHIP, binder.isFunctionMembershipInherited());
		XmlUtils.addProperty(element, ObjectKeys.XTAG_BINDER_INHERITDEFINITIONS, binder.isDefinitionsInherited());

 		// The controller for createing a new binder ignores this, but leave here anyway 
		XmlUtils.addProperty(element, ObjectKeys.XTAG_BINDER_INHERITTEAMMEMBERS, binder.isTeamMembershipInherited());
		if (!binder.isTeamMembershipInherited()) {
			//store as names, not ids
			Set<Long> ids = binder.getTeamMemberIds();
			List<UserPrincipal> members = getProfileDao().loadUserPrincipals(ids, binder.getZoneId(), true);
			for (Principal p:members) {
				XmlUtils.addProperty(element, ObjectKeys.XTAG_BINDER_TEAMMEMBER_NAME, p.getName());	    				 
			}
		}

		Set<Map.Entry> mes = binder.getCustomAttributes().entrySet();
		for (Map.Entry me: mes) {
			CustomAttribute attr = (CustomAttribute)me.getValue();
			attr.toXml(element);
		}
		
		if (!binder.isFunctionMembershipInherited()) {
			 //need to convert all ids to names??
	    	 List<WorkAreaFunctionMembership> wfms = getAdminModule().getWorkAreaFunctionMemberships(binder);
	    	 for (WorkAreaFunctionMembership fm: wfms) {
	    		 Set ids = fm.getMemberIds();
	    		 List<UserPrincipal> members = getProfileDao().loadUserPrincipals(ids, binder.getZoneId(), true);
	    		 try {
	    			 Element e = element.addElement(ObjectKeys.XTAG_ELEMENT_TYPE_FUNCTION_MEMBERSHIP);
	    			 Function function = getFunctionManager().getFunction(binder.getZoneId(), fm.getFunctionId());
	    			 XmlUtils.addProperty(e, ObjectKeys.XTAG_WA_FUNCTION_NAME, function.getName());
	    			 for (Principal p:members) {
	    				 //Not sure is their is a universal separator that could be used,..
		    			 XmlUtils.addProperty(e, ObjectKeys.XTAG_WA_MEMBER_NAME, p.getName());	    				 
	    			 }
	    			 StringBuffer idsString = new StringBuffer();
	    			 if (ids.contains(ObjectKeys.OWNER_USER_ID))
	    				 idsString.append(ObjectKeys.OWNER_USER_ID.toString() + " ");
	    			 if (ids.contains(ObjectKeys.TEAM_MEMBER_ID))
	    				 idsString.append(ObjectKeys.TEAM_MEMBER_ID.toString() + " ");
	    			 XmlUtils.addProperty(e, ObjectKeys.XTAG_WA_MEMBERS, idsString);
	    		 } catch (NoObjectByTheIdException no) {continue;} 
	    		 
	    	 }	    
		}
		List<Definition> defs = binder.getDefinitions();
		if (defs.isEmpty() || binder.isDefinitionsInherited()) {
			Definition def = binder.getEntryDef();
			if (def != null) {
				XmlUtils.addDefinitionReference(element, def);
			}
			
		} else {
			for (Definition def:defs) {
				XmlUtils.addDefinitionReference(element, def);
			}
		}
		Map<String,Definition> workflows = binder.getWorkflowAssociations();
		for (Map.Entry<String,Definition> me: workflows.entrySet()) {
			//log the name the workflow refers to
			try {
				Definition entryDef = getCoreDao().loadDefinition(me.getKey(), binder.getZoneId());
				Element wf = XmlUtils.addDefinitionReference(element, me.getValue());
				//log the definitionId the workflow refers to
				XmlUtils.addDefinitionReference(wf, entryDef);
			} catch (NoDefinitionByTheIdException nd) {};
	
		}
		
		Dashboard dashboard = getDashboardModule().getEntityDashboard(binder.getEntityIdentifier());
		if (dashboard != null) dashboard.asXml(element);
		
		List<TemplateBinder> children = binder.getBinders();
		for (TemplateBinder child:children) {
			Element childElement = element.addElement(ObjectKeys.XTAG_ELEMENT_TYPE_TEMPLATE);
			getTemplateAsXml(child, childElement);
			
		}

	}
	public TemplateBinder getTemplate(Long id) {
		//public
		TemplateBinder binder = getCoreDao().loadTemplate(id, RequestContextHolder.getRequestContext().getZoneId());
		return Utils.validateTemplateBinder(binder);
	}
	public TemplateBinder getTemplateByName(String name) {
		//public
		TemplateBinder binder =  getCoreDao().loadTemplateByName(name, RequestContextHolder.getRequestContext().getZoneId());
		return Utils.validateTemplateBinder(binder);
	}
	public List<TemplateBinder> getTemplates() {
		return getTemplates(Boolean.FALSE);
	}
	public List<TemplateBinder> getTemplates(boolean includeHiddenTemplates) {
		//world read
		List<TemplateBinder> binders = getCoreDao().loadTemplates(RequestContextHolder.getRequestContext().getZoneId());
		return Utils.validateTemplateBinders(binders, includeHiddenTemplates);
	}
	public List<TemplateBinder> getTemplates(Binder binder) {
		//world read
		List<TemplateBinder> binders = getCoreDao().loadTemplates(binder, RequestContextHolder.getRequestContext().getZoneId(), false);
		return Utils.validateTemplateBinders(binders);
	}
	public List<TemplateBinder> getTemplates(int type) {
		//world read
		List<TemplateBinder> binders = getCoreDao().loadTemplates( RequestContextHolder.getRequestContext().getZoneId(), type);
		return Utils.validateTemplateBinders(binders);
	}
	public List<TemplateBinder> getTemplates(int type, Binder binder, boolean includeAncestors) {
		return getTemplates(type, binder, includeAncestors, Boolean.FALSE);
	}
	public List<TemplateBinder> getTemplates(int type, Binder binder, boolean includeAncestors, 
			boolean includeHiddenTemplates) {
		List<TemplateBinder> binders = getCoreDao().loadTemplates(binder, RequestContextHolder.getRequestContext().getZoneId(), type, includeAncestors);
		return Utils.validateTemplateBinders(binders, includeHiddenTemplates);
	}
	//no transaction - Adding the top binder can lead to optimisitic lock exceptions.
	//In order to reduce the risk, we try to shorten the transaction time by managing it ourselves
	public Binder addBinder(final Long configId, final Long parentBinderId, final String title, final String name) 
			throws AccessControlException {
		return addBinder(configId, parentBinderId, title, name, null, null);
	}
	//no transaction - Adding the top binder can lead to optimisitic lock exceptions.
	//In order to reduce the risk, we try to shorten the transaction time by managing it ourselves
	public Binder addBinder(final Long configId, final Long parentBinderId, final String title, final String name, final Map overrideInputData, final Map options) 
			throws AccessControlException {
		//The first add is independent of the others.  In this case the transaction is short 
		//and managed by processors.  
		
		TemplateBinder cfg = getCoreDao().loadTemplate(configId, RequestContextHolder.getRequestContext().getZoneId());
		Binder parent = getCoreDao().loadBinder(parentBinderId, RequestContextHolder.getRequestContext().getZoneId());
		Map ctx = new HashMap();
		if(options != null)
			ctx.putAll(options);
		//force a lock so contention on the sortKey is reduced
		ctx.put(ObjectKeys.INPUT_OPTION_FORCE_LOCK, Boolean.TRUE);
		final Binder top = addBinderInternal(cfg, parent, title, name, overrideInputData, ctx);
		ctx.put(ObjectKeys.INPUT_OPTION_NO_INDEX, Boolean.TRUE); //don't bother indexing, until copyBinderAttributes done
		if (top != null) {
			//now that we have registered the sortKey in the parent binder, we use a longer transaction to complete 
			//it - there shouldn't be any contention here since the binder is new and doesn't need to reference its parent
			getTransactionTemplate().execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
			        //need to reload in case addFolder/workspace used retry loop where session cache is flushed by exception
			        TemplateBinder cfg = getCoreDao().loadTemplate(configId, RequestContextHolder.getRequestContext().getZoneId());
					addBinderPart2(cfg, top);
					return null;
				}
			});
		}
		IndexSynchronizationManager.applyChanges(); //get them committed, binders are
		return top;

	}
	
	private void addBinderPart2(TemplateBinder cfg, Binder top) {
		addBinderFinish(cfg, top);
		//after children are added, resolve relative selections
		List<Binder>binders = new ArrayList();
		binders.add(top);
		while (!binders.isEmpty()) {
			Binder b = binders.remove(0);
			binders.addAll(b.getBinders());
			try {
				EntityDashboard dashboard = getCoreDao().loadEntityDashboard(b.getEntityIdentifier());
				if (dashboard != null) {
					DashboardHelper.resolveRelativeBinders(b.getBinders(), dashboard);
				}
			} catch (Exception ex) {
				//at this point just log errors  index has already been updated
				//	if throw errors, rollback will take effect and must manualy remove from index
				logger.error("Error adding dashboard " + ex.getLocalizedMessage());
			}
		}
	}

	protected void addBinderFinish(TemplateBinder cfg, Binder binder) throws AccessControlException {
		   Map props = cfg.getProperties();
		   if (props != null) binder.setProperties(props);
		   copyBinderAttributes(cfg, binder);
		   //first flush updates, addBinder might do a refresh which overwrites changes
		   getCoreDao().flush();
		   List<TemplateBinder> children = cfg.getBinders();   
		   Map ctx = new HashMap();
		   ctx.put(ObjectKeys.INPUT_OPTION_NO_INDEX, Boolean.TRUE); //don't bother indexing, until copyBinderAttributes done
		   for (TemplateBinder child: children) {
			   if (Utils.validateDefinition(child.getEntryDef(), null)) {
				   //This binder type is allowed, so go add it (and its children)
				   Binder childBinder = addBinderInternal(child, binder, NLT.getDef(child.getTitle()), null, null, null);	
				   addBinderFinish(child, childBinder);
			   }
		   }
		   //finally index binder
		   loadBinderProcessor(binder).indexBinder(binder, false, false, Collections.EMPTY_LIST);

	}

	protected Binder addBinderInternal(TemplateBinder cfg, Binder parentBinder, String title, String name, Map overrideInputData, Map ctx) throws AccessControlException {
	   Long zoneId =  RequestContextHolder.getRequestContext().getZoneId();
	   Binder binder;
	   Definition def = cfg.getDefaultViewDef();
	   if (def == null) {
		   Integer cfgType = cfg.getDefinitionType();
		   if (cfgType == null) cfgType = 0;
		   def = getDefinitionModule().addDefaultDefinition(cfgType);
	   }
	   Map fileItems = new HashMap();
	   Map entryData = new HashMap();
	   InputDataAccessor inputData = new MapInputData(entryData);
	   if (Validator.isNull(title)) title = NLT.getDef(cfg.getTitle());
	   if (Validator.isNull(title)) title = NLT.getDef(cfg.getTemplateTitle());
	   entryData.put(ObjectKeys.FIELD_ENTITY_TITLE, title);
	   String description=null;
	   if (cfg.getDescription() != null) description = NLT.getDef(cfg.getDescription().getText());
	   if (Validator.isNotNull(description)) entryData.put(ObjectKeys.FIELD_ENTITY_DESCRIPTION, description);
	   entryData.put(ObjectKeys.FIELD_BINDER_LIBRARY, Boolean.toString(cfg.isLibrary()));
	   boolean mirrored = cfg.isMirrored();
	   if((EntityType.folder == parentBinder.getEntityType()) && parentBinder.isMirrored()) {
		   mirrored = true;
		   entryData.put(ObjectKeys.FIELD_BINDER_RESOURCE_DRIVER_NAME, parentBinder.getResourceDriverName());
	   }
	   entryData.put(ObjectKeys.FIELD_BINDER_MIRRORED, Boolean.toString(mirrored));
	   entryData.put(ObjectKeys.FIELD_BINDER_UNIQUETITLES, Boolean.toString(cfg.isUniqueTitles()));
	   entryData.put(ObjectKeys.FIELD_BINDER_INHERITTEAMMEMBERS, Boolean.toString(cfg.isTeamMembershipInherited()));
	   //if not null, use icon from template.  Otherwise try icon from definition when binder is created.
	   if (Validator.isNotNull(cfg.getIconName())) entryData.put(ObjectKeys.FIELD_ENTITY_ICONNAME, cfg.getIconName());
	   if (Validator.isNotNull(name)) entryData.put(ObjectKeys.FIELD_BINDER_NAME, name);
	   if (!cfg.isDefinitionsInherited()) {
		   entryData.put(ObjectKeys.INPUT_FIELD_DEFINITIONS, cfg.getDefinitions());
		   entryData.put(ObjectKeys.INPUT_FIELD_WORKFLOWASSOCIATIONS, cfg.getWorkflowAssociations());
	   }
	   if (!cfg.isFunctionMembershipInherited()) {
			entryData.put(ObjectKeys.INPUT_FIELD_FUNCTIONMEMBERSHIPS, getAdminModule().getWorkAreaFunctionMemberships(cfg));
	   }	    	
	   // Override the template-supplied static settings with dynamically-supplied runtime settings.
	   if(overrideInputData != null)
		   entryData.putAll(overrideInputData);
	   //get binder created
	   try {
			binder = getCoreDao().loadBinder(getBinderModule().addBinder(parentBinder.getId(), def.getId(), inputData, fileItems, ctx).getId(), zoneId);
	   } catch (WriteFilesException wf) {
		   //don't fail, but log it
  			logger.error("Error creating binder from template: ", wf);
  			binder = getCoreDao().loadBinder(wf.getEntityId(), zoneId);
	   } catch (WriteEntryDataException e) {
		   //don't fail, but log it
 			logger.error("Error creating binder from template: ", e);
 			binder = null;
	   }
	   return binder;
	}
	/**
   	 * Use operation so we can keep the logic out of application
	 * and easisly change the required rights
	 */
   	public boolean testAccess(TemplateOperation operation) {
   		try {
   			checkAccess(operation);
   			return true;
   		} catch (AccessControlException ac) {
   			return false;
   		}
   	}
   	public void checkAccess(TemplateOperation operation) {
   		switch (operation) {
   		case manageTemplate:
   			getAccessControlManager().checkOperation(getCoreDao().loadZoneConfig(RequestContextHolder.getRequestContext().getZoneId()), WorkAreaOperation.ZONE_ADMINISTRATION);
   			break;
   		default:
   			throw new NotSupportedException(operation.toString(), "checkAccess");

   		}
   	}
   	
   	//Check if this binder can be copied into a template
   	public boolean checkIfBinderValidForTemplate(Binder binder, String[] errors) {
		List<Binder>binders = new ArrayList();
		binders.add(binder);
		while (!binders.isEmpty()) {
			Binder b = binders.remove(0);
			binders.addAll(b.getBinders());
			//See if this binder uses any local definitions
			List<Definition> definitions = binder.getViewDefinitions();
			definitions.addAll(binder.getEntryDefinitions());
			definitions.addAll(binder.getWorkflowDefinitions());
			for (Definition def : definitions) {
				if (def.getBinderId() != -1) {
					//This is a local definition (local to some binder). These are not allowed
					errors[0] = NLT.get("error.template.cannotUseLocalDefinitions");
					return false;
				}
			}
		}
   		return true;
   	}
   	
	@Override
	public List<Binder> _addNetFolderBindersInSync(final Long templateId,
			final Long parentBinderId, final List<String> titleList, final List<String> nameList,
			final List<Map> overrideInputDataList, final List<Map> optionsList)
			throws AccessControlException, WriteFilesException {
		
		final ArrayList<Binder> result = new ArrayList<Binder>(titleList.size());
		
		//The first add is independent of the others.  In this case the transaction is short 
		//and managed by processors.  
		
		TemplateBinder cfg = getCoreDao().loadTemplate(templateId, RequestContextHolder.getRequestContext().getZoneId());
		Binder parent = getCoreDao().loadBinder(parentBinderId, RequestContextHolder.getRequestContext().getZoneId());
		
		Map ctx;
		Binder top;
		for(int i = 0; i < titleList.size(); i++) {
			ctx = optionsList.get(i);
			if(ctx == null)
				ctx = new HashMap();
			//force a lock so contention on the sortKey is reduced
			ctx.put(ObjectKeys.INPUT_OPTION_FORCE_LOCK, Boolean.TRUE);
			top = addBinderInternal(cfg, parent, titleList.get(i), nameList.get(i), overrideInputDataList.get(i), ctx);
			if(top != null)
				result.set(i, top);
		}
		
		//now that we have registered the sortKey in the parent binder, we use a longer transaction to complete 
		//it - there shouldn't be any contention here since the binder is new and doesn't need to reference its parent
		getTransactionTemplate().execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
		        //need to reload in case addFolder/workspace used retry loop where session cache is flushed by exception
		        TemplateBinder cfg = getCoreDao().loadTemplate(templateId, RequestContextHolder.getRequestContext().getZoneId());
				for(Binder top : result) {
					if(top != null)
						addBinderPart2(cfg, top);
				}
				return null;
			}
		});
		
		return result;
	}
	
}
