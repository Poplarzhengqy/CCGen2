/**
 * Copyright (c) 1998-2011 Novell, Inc. and its licensors. All rights reserved.
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
 * (c) 1998-2011 Novell, Inc. All Rights Reserved.
 * 
 * Attribution Information:
 * Attribution Copyright Notice: Copyright (c) 1998-2011 Novell, Inc. All Rights Reserved.
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
package org.kablink.teaming.module.zone.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.kablink.teaming.NoObjectByTheIdException;
import org.kablink.teaming.ObjectKeys;
import org.kablink.teaming.context.request.RequestContext;
import org.kablink.teaming.context.request.RequestContextHolder;
import org.kablink.teaming.context.request.RequestContextUtil;
import org.kablink.teaming.dao.util.FilterControls;
import org.kablink.teaming.dao.util.ObjectControls;
import org.kablink.teaming.dao.util.OrderBy;
import org.kablink.teaming.dao.util.SFQuery;
import org.kablink.teaming.domain.ApplicationGroup;
import org.kablink.teaming.domain.Binder;
import org.kablink.teaming.domain.Definition;
import org.kablink.teaming.domain.Group;
import org.kablink.teaming.domain.HistoryStamp;
import org.kablink.teaming.domain.IdentityInfo;
import org.kablink.teaming.domain.LdapConnectionConfig;
import org.kablink.teaming.domain.NoBinderByTheNameException;
import org.kablink.teaming.domain.NoGroupByTheNameException;
import org.kablink.teaming.domain.NoUserByTheNameException;
import org.kablink.teaming.domain.PostingDef;
import org.kablink.teaming.domain.Principal;
import org.kablink.teaming.domain.ProfileBinder;
import org.kablink.teaming.domain.Subscription;
import org.kablink.teaming.domain.User;
import org.kablink.teaming.domain.WorkflowHistory;
import org.kablink.teaming.domain.WorkflowStateHistory;
import org.kablink.teaming.domain.Workspace;
import org.kablink.teaming.domain.ZoneConfig;
import org.kablink.teaming.domain.ZoneInfo;
import org.kablink.teaming.extension.ZoneClassManager;
import org.kablink.teaming.jobs.ScheduleInfo;
import org.kablink.teaming.jobs.ZoneSchedule;
import org.kablink.teaming.module.admin.AdminModule;
import org.kablink.teaming.module.binder.BinderModule;
import org.kablink.teaming.module.definition.DefinitionModule;
import org.kablink.teaming.module.definition.DefinitionUtils;
import org.kablink.teaming.module.impl.CommonDependencyInjection;
import org.kablink.teaming.module.ldap.LdapModule;
import org.kablink.teaming.module.ldap.LdapSchedule;
import org.kablink.teaming.module.profile.ProfileModule;
import org.kablink.teaming.module.template.TemplateModule;
import org.kablink.teaming.module.zone.ZoneException;
import org.kablink.teaming.module.zone.ZoneModule;
import org.kablink.teaming.search.IndexSynchronizationManager;
import org.kablink.teaming.security.function.Function;
import org.kablink.teaming.security.function.WorkArea;
import org.kablink.teaming.security.function.WorkAreaFunctionMembership;
import org.kablink.teaming.security.function.WorkAreaOperation;
import org.kablink.teaming.security.function.WorkAreaOperation.RightSet;
import org.kablink.teaming.security.impl.AccessControlManagerImpl;
import org.kablink.teaming.util.NLT;
import org.kablink.teaming.util.ReflectHelper;
import org.kablink.teaming.util.SPropsUtil;
import org.kablink.teaming.util.SZoneConfig;
import org.kablink.teaming.util.SessionUtil;
import org.kablink.teaming.util.LocaleUtils;
import org.kablink.teaming.util.Utils;
import org.kablink.teaming.util.cache.DefinitionCache;
import org.kablink.util.Validator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@SuppressWarnings({ "unchecked", "unused", "deprecation" })
public abstract class AbstractZoneModule extends CommonDependencyInjection implements ZoneModule,InitializingBean {
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
    protected TemplateModule templateModule;
    protected TemplateModule getTemplateModule() {
    	return templateModule;
    }
    public void setTemplateModule(TemplateModule templateModule) {
    	this.templateModule = templateModule;
    }

	protected AdminModule adminModule;
	/**
	 * Setup by spring
	 * @param adminModule
	 */
	public void setAdminModule(AdminModule adminModule) {
		this.adminModule = adminModule;
	}
	protected AdminModule getAdminModule() {
		return adminModule;
	}
	private TransactionTemplate transactionTemplate;
    protected TransactionTemplate getTransactionTemplate() {
		return transactionTemplate;
	}
	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}
	private ProfileModule profileModule;	
	public void setProfileModule(ProfileModule profileModule) {
		this.profileModule = profileModule;
	}
	protected ProfileModule getProfileModule() {
		return profileModule;
	}
	private BinderModule binderModule;	
	public void setBinderModule(BinderModule binderModule) {
		this.binderModule = binderModule;
	}
	protected BinderModule getBinderModule() {
		return binderModule;
	}
	private LdapModule ldapModule;
	public void setLdapModule(LdapModule ldapModule) {
		this.ldapModule = ldapModule;
	}
	protected LdapModule getLdapModule() {
		return ldapModule;
	}
	protected List<ZoneSchedule> startupModules;
	public void setScheduleModules(List modules) {
		startupModules = modules;		
	}
	protected ZoneClassManager zoneClassManager;
	protected ZoneClassManager getZoneClassManager() {
		return zoneClassManager;
	}
	public void setZoneClassManager(ZoneClassManager zoneClassManager) {
		this.zoneClassManager = zoneClassManager;
	}
	
	/**
     * Called after bean is initialized.  
     * Check on zones
     */
 	public void afterPropertiesSet() {
 		// Do nothing
 	}
	
 	public void initZones() {
		boolean closeSession = false;
		if (!SessionUtil.sessionActive()) {
			SessionUtil.sessionStartup();	
			closeSession = true;
		}
		try {
			final List<Workspace> companies = getTopWorkspacesFromEachZone();
			final String zoneName = SZoneConfig.getDefaultZoneName();

			if (companies.size() == 0) {
				addZone(zoneName, null);
 			} else {
        		for (int i=0; i<companies.size(); ++i) {
        			final Workspace zone = (Workspace)companies.get(i);
        			try {
	    				getTransactionTemplate().execute(new TransactionCallback() {
	    					public Object doInTransaction(TransactionStatus status) {
	    						upgradeZoneTx(zone);
	    						return null;
	    					}
	    				});
        			}
        			catch(Exception e) {
        				logger.warn("Failed to upgrade zone " + zone.getZoneId(), e);
        			}
        		}
				//make sure zone is setup correctly
				getTransactionTemplate().execute(new TransactionCallback() {
		        	public Object doInTransaction(TransactionStatus status) {
		        		for (int i=0; i<companies.size(); ++i) {
		        			Workspace zone = (Workspace)companies.get(i);
		        			try {
			        			if (zone.isDeleted()) continue;
			        			validateZoneTx(zone);
		        			}
		        			catch(Exception e) {
		        				logger.warn("Failed to validate zone " + zone.getZoneId(), e);
		        			}
		    	        }
			        	return null;
		        	}
				});
    			for (ZoneSchedule zoneM:startupModules) {
	        		for (int i=0; i<companies.size(); ++i) {
	        			Workspace zone = (Workspace)companies.get(i);
	        			try {
	        				zoneM.startScheduledJobs(zone);
	        			}
	        			catch(Exception e) {
	        				logger.warn("Failed to start scheduled jobs for zone " + zone.getZoneId(), e);
	        			}
	        		}
    			}
 			
			}
		} finally {
			if (closeSession) SessionUtil.sessionStop();
		}

		// At this point we must flush out any indexing changes that might have occurred
		// before clearing the context.
		IndexSynchronizationManager.applyChanges();
		
 		RequestContextHolder.clear();
 		
 		DefinitionCache.clear();
 	}
 	
	public Long getZoneIdByZoneName(String zoneName) {
		Workspace top = getCoreDao().findTopWorkspace(zoneName);
		return top.getId();
	}
	public ZoneConfig getZoneConfig(Long zoneId) throws ZoneException {
		return getCoreDao().loadZoneConfig(zoneId);
	}
 	// Must be running inside a transaction set up by the caller 
	private ZoneConfig addZoneConfigTx(Workspace zone) {
		// Make sure there is a ZoneConfig; new for v2
		ZoneConfig zoneConfig = new ZoneConfig(zone.getId());
		if (SPropsUtil.getBoolean("smtp.service.enable")) zoneConfig.getMailConfig().setSimpleUrlPostingEnabled(true);		
		//keep current if was deleted
		ScheduleInfo notify = getAdminModule().getNotificationSchedule();
		zoneConfig.getMailConfig().setSendMailEnabled(notify.isEnabled());
		//remove old feature if not being used
		if (getAdminModule().getPostings().isEmpty() && SPropsUtil.getBoolean("mail.posting.offWhenEmpty", true)) {
			zoneConfig.getMailConfig().setPostingEnabled(false);
		} else { 
			zoneConfig.getMailConfig().setPostingEnabled(true);
		}
		getCoreDao().save(zoneConfig);
		return zoneConfig;
	}
 	protected void upgradeZoneTx(Workspace top) {
		String superName = SZoneConfig.getAdminUserName(top.getName());
 		//	get super user from config file - must exist or throws and error
 		User superU = getProfileDao().findUserByNameDeadOrAlive(superName, top.getName());
 		RequestContextUtil.setThreadContext(superU).resolve();
 		
 		ZoneConfig zoneConfig;
 		Integer version=0;  //used to mimic pre-boulder upgradeVersion on ws
 		try {
 			zoneConfig = getZoneConfig(top.getId());
 			version = zoneConfig.getUpgradeVersion();
 		} catch (NoObjectByTheIdException zx) {
			// Make sure there is a ZoneConfig; new for v2
 			zoneConfig = addZoneConfigTx(top);
 		}
 		if ((version == null) || version.intValue() <= 1) {
 			//TODO: setZoneId as non=null, only do based on version
			getCoreDao().executeUpdate("update org.kablink.teaming.security.function.Function set zoneWide=false where zoneWide is null"); 
			
			getCoreDao().executeUpdate("update org.kablink.teaming.domain.AuditTrail set zoneId=" + top.getId() + 
				" where zoneId is null");
			getCoreDao().executeUpdate("update org.kablink.teaming.domain.Tag set zoneId=" + top.getId() + 
				" where zoneId is null");
			getCoreDao().executeUpdate("update org.kablink.teaming.domain.WorkflowState set zoneId=" + top.getId() + 
				" where zoneId is null");
			getCoreDao().executeUpdate("update org.kablink.teaming.domain.Event set zoneId=" + top.getId() + 
				" where zoneId is null");
			getCoreDao().executeUpdate("update org.kablink.teaming.domain.Visits set zoneId=" + top.getId() + 
				" where zoneId is null");
			getCoreDao().executeUpdate("update org.kablink.teaming.domain.Subscription set zoneId=" + top.getId() + 
				" where zoneId is null");
			getCoreDao().executeUpdate("update org.kablink.teaming.domain.LibraryEntry set zoneId=" + top.getId() + 
				" where zoneId is null");
			getCoreDao().executeUpdate("update org.kablink.teaming.domain.Dashboard set zoneId=" + top.getId() + 
				" where zoneId is null");
			getCoreDao().executeUpdate("update org.kablink.teaming.domain.Attachment set zoneId=" + top.getId() + 
				" where zoneId is null");
			getCoreDao().executeUpdate("update org.kablink.teaming.domain.SeenMap set zoneId=" + top.getId() + 
				" where zoneId is null");
			getCoreDao().executeUpdate("update org.kablink.teaming.domain.WorkflowResponse set zoneId=" + top.getId() + 
				" where zoneId is null");
			getCoreDao().executeUpdate("update org.kablink.teaming.domain.UserProperties set zoneId=" + top.getId() + 
				" where zoneId is null");
			getCoreDao().executeUpdate("update org.kablink.teaming.domain.FolderEntry set zoneId=" + top.getId() + 
				" where zoneId is null");
			getCoreDao().executeUpdate("update org.kablink.teaming.domain.CustomAttribute set zoneId=" + top.getId() + 
				" where zoneId is null");
			getCoreDao().executeUpdate("update org.kablink.teaming.domain.PostingDef set zoneId=" + top.getId() + 
				" where zoneId is null");
			getCoreDao().executeUpdate("update org.kablink.teaming.domain.Rating set zoneId=" + top.getId() + 
				" where zoneId is null");
			getCoreDao().executeUpdate("update org.kablink.teaming.domain.TemplateBinder set name=templateTitle where parentBinder is null and (name is null or name='')");
			getCoreDao().executeUpdate("update org.kablink.teaming.domain.FolderEntry set subscribed=false where subscribed is null");
			getCoreDao().executeUpdate("update org.kablink.teaming.domain.FolderEntry set subscribed=true where id in (select id.entityId from org.kablink.teaming.domain.Subscription where id.entityType=6)");
			//update jbpm
			getCoreDao().executeUpdate("update org.jbpm.instantiation.Delegation set className='org.kablink.teaming.module.workflow.EnterExitEvent' " +
					"where className='com.sitescape.team.module.workflow.EnterExitEvent'"); 
			getCoreDao().executeUpdate("update org.jbpm.instantiation.Delegation set className='org.kablink.teaming.module.workflow.DecisionAction' " +
				"where className='com.sitescape.team.module.workflow.DecisionAction'"); 
			getCoreDao().executeUpdate("update org.jbpm.instantiation.Delegation set className='org.kablink.teaming.module.workflow.TimerAction' " +
				"where className='com.sitescape.team.module.workflow.TimerAction'"); 
			getCoreDao().executeUpdate("update org.jbpm.instantiation.Delegation set className='org.kablink.teaming.module.workflow.Notify' " +
				"where className='com.sitescape.team.module.workflow.Notify'"); 
			//fix up any duplicate definitions
			fixUpDuplicateDefinitions(top);
			//getCoreDao().executeUpdate("update org.kablink.teaming.domain.Definition set binderId=-1 where binderId is null");

			//add new reserved functions
			List ids = new ArrayList();
			//add users who currently have siteAdmin to the new functions
			WorkAreaOperation siteAdmin = WorkAreaOperation.getInstance("siteAdministration");
			List<WorkAreaFunctionMembership>wfms = getWorkAreaFunctionMembershipManager().findWorkAreaFunctionMembershipsByOperation(top.getId(), top, siteAdmin);
			for (WorkAreaFunctionMembership wfm:wfms) {
				ids.addAll(wfm.getMemberIds());				
			}
			addGlobalFunctions(zoneConfig);
			//Remove old site_admin right
			//now remove the old right
			List<Function>fns = getFunctionManager().findFunctions(top.getId(), siteAdmin);
			for (Function fn:fns) {
				fn.removeOperation(siteAdmin);
			}
			//remove from system
			WorkAreaOperation.deleteInstance(siteAdmin.getName());
			List<PostingDef> postings = getCoreDao().loadObjects(org.kablink.teaming.domain.PostingDef.class , null, top.getId());
			for (PostingDef post:postings) {
				post.updateCredentials();
			}

			//get
			//fixup user emails
	 		SFQuery query=null;
	 		List batch = new ArrayList();
	 		// Load processor class
	 		try {
	 			Class processorClass = ReflectHelper.classForName(Principal.class.getName());
	 			Field fld = processorClass.getDeclaredField("emailAddress");
	 			fld.setAccessible(true);
	 			query = getProfileDao().queryAllPrincipals(new FilterControls(), top.getId());
	      		while (query.hasNext()) {
	       			int count=0;
	       			batch.clear();
	       			// process 1000 entries
	       			while (query.hasNext() && (count < 1000)) {
	       				Object obj = query.next();
	       				if (obj instanceof Object[])
	       					obj = ((Object [])obj)[0];
	       				Principal principal = (Principal)obj;
	       		        String email = (String)fld.get(principal);
	       		        if (Validator.isNotNull(email)) {
	       					principal.setEmailAddress(Principal.PRIMARY_EMAIL, email);
	          		        fld.set(principal, null);
	       		        }
	       				++count;
	       				batch.add(principal);
	       			}
	       			//flush updates
	       			getCoreDao().flush();
	       			//flush cache
	       			for (int i=0; i<batch.size(); ++i) {
	       				getCoreDao().evict(batch.get(i));
	       			}
	      		}
	   			//flush updates
	   			getCoreDao().flush();
	   			//flush cache
	   			for (int i=0; i<batch.size(); ++i) {
	   				getCoreDao().evict(batch.get(i));
	   			}
	
			} catch (ClassNotFoundException cn) {
	 			//this cannot happen, can it?
				logger.error(cn);
			} catch (NoSuchFieldException nf) {
	 			//this cannot happen, can it?
				logger.error(nf);
			} catch (IllegalAccessException ia) {
	 			//this cannot happen, can it?
				logger.error(ia);
	 		} finally {
	 			if (query != null) query.close();
	 			query=null;
	 		}
			
	
			//fixup styles
	 		try {
				Class processorClass = ReflectHelper.classForName(Subscription.class.getName());
				Field fld = processorClass.getDeclaredField("style");
				fld.setAccessible(true);
				String [] styles = new String[] {Principal.PRIMARY_EMAIL};
	 			query = getCoreDao().queryObjects(new ObjectControls(Subscription.class), null, top.getId());
	      		while (query.hasNext()) {
	       			int count=0;
	       			batch.clear();
	       			// process 1000 entries
	       			while (query.hasNext() && (count < 1000)) {
	       				Object obj = query.next();
	       				if (obj instanceof Object[])
	       					obj = ((Object [])obj)[0];
	       				Subscription sub = (Subscription)obj;
	     				int style = fld.getInt(sub);
	       				if (style != 0) {
	           				fld.setInt(sub, 0);
	       					sub.addStyle(style, styles);
	       				}
	       				++count;
	       				batch.add(sub);
	       			}
	       			//flush updates
	       			getCoreDao().flush();
	       			//flush cache
	       			for (int i=0; i<batch.size(); ++i) {
	       				getCoreDao().evict(batch.get(i));
	       			}
	      		}
	   			//flush updates
	   			getCoreDao().flush();
	   			//flush cache
	   			for (int i=0; i<batch.size(); ++i) {
	   				getCoreDao().evict(batch.get(i));
	   			}
	
			} catch (ClassNotFoundException cn) {
	 			//this cannot happen, can it?
				logger.error(cn);
			} catch (NoSuchFieldException nf) {
	 			//this cannot happen, can it?
				logger.error(nf);
			} catch (IllegalAccessException ia) {
	 			//this cannot happen, can it?
				logger.error(ia);
			} finally {
	 			if (query != null) query.close();
	 			query=null;
	 		}
			//move old workflowStateHistory rows to new table
	 		try {
	 			query = getCoreDao().queryObjects(new ObjectControls(WorkflowStateHistory.class), null, top.getId());
	      		while (query.hasNext()) {
	       			int count=0;
	       			batch.clear();
	       			// process 1000 entries
	       			while (query.hasNext() && (count < 1000)) {
	       				Object obj = query.next();
	       				if (obj instanceof Object[])
	       					obj = ((Object [])obj)[0];
	       				WorkflowStateHistory old = (WorkflowStateHistory)obj;
	       				WorkflowHistory wfHistory = new WorkflowHistory(old);
	       				getCoreDao().replicate(wfHistory);
	       				++count;
	       				batch.add(old);
	       			}
	       			//flush updates
	       			getCoreDao().flush();
	       			//flush cache
       				getCoreDao().evict(batch);
	      		}
	   			//flush updates
	   			getCoreDao().flush();
	   			//flush cache
  				getCoreDao().evict(batch);
	
			} finally {
	 			if (query != null) query.close();
	 			query=null;
	 		}
			//remove the old history
			getCoreDao().executeUpdate("delete org.kablink.teaming.domain.WorkflowStateHistory where zoneId=" + top.getId());

			//create schedule first time through
			ScheduleInfo notify = getAdminModule().getNotificationSchedule();
	 		notify.getSchedule().setDaily(true);
	 		notify.getSchedule().setHours("0");
	 		notify.getSchedule().setMinutes("15");
	 		notify.setEnabled(true);
			zoneConfig.getMailConfig().setSendMailEnabled(true);
			getAdminModule().setMailConfigAndSchedules(zoneConfig.getMailConfig(), notify, null);
			
			//Turn on file version aging job
			ScheduleInfo info = getAdminModule().getFileVersionAgingSchedule();
			getAdminModule().setFileVersionAgingSchedule(info);

			
			//If not configured yet,  check old config
			if (getCoreDao().loadObjects(LdapConnectionConfig.class, null, top.getId()).isEmpty()) {				
				LdapSchedule schedule = getLdapModule().getLdapSchedule();
				LdapSchedule.LegacyLdapConfig oldConfig = new LdapSchedule.LegacyLdapConfig(schedule.getScheduleInfo().getDetails());
				//make sure was configured already
				if (schedule.isEnabled() || Validator.isNotNull(oldConfig.getUserUrl())) {
					//upgrade
					String url = oldConfig.getUserUrl();
					String userDn = "";
					int pos = url.lastIndexOf('/');
					if (pos > 8) {
						userDn = url.substring(pos+1, url.length());
						url = url.substring(0, url.lastIndexOf('/'));
					}
					String groupDn = oldConfig.getGroupsBasedn();
					if (Validator.isNull(groupDn)) groupDn = userDn;
					List<LdapConnectionConfig.SearchInfo> userSearch = new ArrayList();
					userSearch.add(new LdapConnectionConfig.SearchInfo(userDn, SZoneConfig.getString("ldapConfiguration/userFilter"), true));						

					List<LdapConnectionConfig.SearchInfo> groupSearch = new ArrayList();
					groupSearch.add(new LdapConnectionConfig.SearchInfo(groupDn, SZoneConfig.getString("ldapConfiguration/groupFilter"), true));
					String userId = oldConfig.getUserIdMapping();
					if (Validator.isNull(userId)) userId="uid";
					LdapConnectionConfig connection = new LdapConnectionConfig(url, userId, 
							oldConfig.getUserMappings(), userSearch, groupSearch, oldConfig.getUserPrincipal(), oldConfig.getUserCredential(), null );
					connection.setPosition(0);
					connection.setZoneId(top.getId());
					getCoreDao().save(connection);
				}
			}
			//Initialize the mail server settings
			if (!SPropsUtil.getBoolean("smtp.service.enable")) {
				zoneConfig.getMailConfig().setSimpleUrlPostingEnabled(true);
			} else {
				zoneConfig.getMailConfig().setSimpleUrlPostingEnabled(false);
			}
 		}
		//make sure zoneConfig upto date
 		
		//Always do the following items
		//Get any new definitions and templates
		getAdminModule().updateDefaultDefinitions(top.getId(), true);
		getTemplateModule().updateDefaultTemplates(RequestContextHolder.getRequestContext().getZoneId(), false);
		
		//In general, make sure all of the global functions are there
		//This call only adds roles that weren't there already
		addGlobalFunctions(zoneConfig);

		if (version.intValue() <= 2 || zoneConfig.getUpgradeVersion() < ZoneConfig.ZONE_LATEST_VERSION) {
			zoneConfig.setUpgradeVersion(ZoneConfig.ZONE_LATEST_VERSION);
			// Whenever the zoneConfig version changes, there may be
			// tasks the admin needs to perform, based on the version.
			resetZoneUpgradeTasks(version.intValue(), superU.getId(), top.getId());
		}
		if (version.intValue() <= 2) {
 			//Change the definition of the top workspace to become the welcome page
 			List definitions = new ArrayList();
 			Map workflowAssociations = new HashMap();
 			Definition def = getDefinitionModule().getDefinitionByReservedId(ObjectKeys.DEFAULT_WELCOME_WORKSPACE_DEF);
 			definitions.add(def.getId());
			getBinderModule().setDefinitions(top.getId(), definitions, workflowAssociations);
   		}
		if (version.intValue() <= 3) {
			//add new reserved functions
			List ids = new ArrayList();
			//add users who currently have siteAdmin to the new functions
			WorkAreaOperation siteAdmin = WorkAreaOperation.getInstance("siteAdministration");
			List<WorkAreaFunctionMembership>wfms = getWorkAreaFunctionMembershipManager().findWorkAreaFunctionMembershipsByOperation(top.getId(), top, siteAdmin);
			for (WorkAreaFunctionMembership wfm:wfms) {
				ids.addAll(wfm.getMemberIds());				
			}
			addGlobalFunctions(zoneConfig);
		}
		if (version.intValue() <= 5) {
			Function readRole = addEntryReadRole(top);
			Function readReplyRole = addEntryReadReplyRole(top);
			Function writeRole = addEntryWriteRole(top);
			Function deleteRole = addEntryDeleteRole(top);
			Function changeAclRole = addEntryChangeAclRole(top);
		}
		if (version.intValue() <= 6) {
			//Add the new "Create Entry ACL" related right to the standard roles
			List<Function>fns = getFunctionManager().findFunctions(top.getId());
			for (Function fn:fns) {
				if (ObjectKeys.ROLE_TITLE_PARTICIPANT.equals(fn.getName())) {
					fn.addOperation(WorkAreaOperation.CREATOR_CREATE_ENTRY_ACLS);
				}
				if (ObjectKeys.ROLE_TITLE_TEAM_MEMBER.equals(fn.getName())) {
					fn.addOperation(WorkAreaOperation.CREATOR_CREATE_ENTRY_ACLS);
				}
				if (ObjectKeys.ROLE_TITLE_BINDER_ADMIN.equals(fn.getName())) {
					fn.addOperation(WorkAreaOperation.CREATOR_CREATE_ENTRY_ACLS);
					fn.addOperation(WorkAreaOperation.CREATE_ENTRY_ACLS);
				}
			}
		}
		if(version.intValue() <= 7) {
			User synchAgent = getSynchronizationAgent(zoneConfig.getZoneId());
			if(synchAgent != null)
				setTokenRequesterInitialMembership(zoneConfig, synchAgent);
		}
		
		if(version.intValue() <= 8) {
			setupInitialOpenIDProviderList();
		}
		
		if(version.intValue() <= 10) {
			//No longer used
		}
		
		if(version.intValue() <= 11) {
			correctFilrRoles(zoneConfig);
		}
		
		if (version.intValue() <= 12) {
			if (Utils.checkIfFilr()) {
				//In Filr, we must reset all of the definitions and templates and definitions automatically
				//But this is only done when needed (i.e., update the version if another change is made)
				getAdminModule().updateDefaultDefinitions(top.getId(), false);
				getTemplateModule().updateDefaultTemplates(top.getId(), true);
				getProfileModule().setUserProperty(superU.getId(), ObjectKeys.USER_PROPERTY_UPGRADE_DEFINITIONS, "true");
				getProfileModule().setUserProperty(superU.getId(), ObjectKeys.USER_PROPERTY_UPGRADE_TEMPLATES, "true");
			}
		}
		
  	}
 	
 	private void correctFilrRoles(ZoneConfig zoneConfig) {
		Function function;
		List<Function> functions = getFunctionManager().findFunctions(zoneConfig.getZoneId());
		Map<String,Function> functionInternalIds = new HashMap<String,Function>();
		for (int i = 0; i < functions.size(); i++) {
			function = (Function)functions.get(i);
			if (function.getInternalId() != null) 
				functionInternalIds.put(function.getInternalId(), function);
		}
		
		Function filrViewerRole = functionInternalIds.get(ObjectKeys.FUNCTION_FILR_VIEWER_INTERNALID);
		if(filrViewerRole != null) {
			filrViewerRole.setName(ObjectKeys.ROLE_TITLE_FILR_VIEWER);
			filrViewerRole.getOperations().clear();
			fillFilrRoleViewer(filrViewerRole);
			getFunctionManager().updateFunction(filrViewerRole);
			
		}
		
		Function filrEditorRole = functionInternalIds.get(ObjectKeys.FUNCTION_FILR_EDITOR_INTERNALID);
		if(filrEditorRole != null) {
			filrEditorRole.setName(ObjectKeys.ROLE_TITLE_FILR_EDITOR);
			filrEditorRole.getOperations().clear();
			fillFilrRoleEditor(filrEditorRole);
			getFunctionManager().updateFunction(filrEditorRole);
			
		}
		
		Function filrContributorRole = functionInternalIds.get(ObjectKeys.FUNCTION_FILR_CONTRIBUTOR_INTERNALID);
		if(filrContributorRole != null) {
			filrContributorRole.setName(ObjectKeys.ROLE_TITLE_FILR_CONTRIBUTOR);
			filrContributorRole.getOperations().clear();
			fillFilrRoleContributor(filrContributorRole);
			getFunctionManager().updateFunction(filrContributorRole);
			
		}
 	}
 	
 	/**
 	 * Fix up duplicate definitions.  1.0 allowed definitions with the same name
 	 * we need to find any definitions with the same name and rename those that are duplicates.
 	 * 
 	 * @param top
 	 */
 	private void fixUpDuplicateDefinitions(Workspace top) {
		
 		String NoNameTitle = "N0_NAME";

 		
		OrderBy order = new OrderBy();
		order.addColumn("name");
		FilterControls filter = new FilterControls();
		filter.setOrderBy(order);
		filter.setZoneCheck(false);
		
 		List<Definition> defs = getCoreDao().loadDefinitions(filter, top.getZoneId());
 		List batch = new ArrayList();

 		String prevName = null;
		int dupCnt = 1;
		int noNameCnt = 1;
		for ( Definition def : defs )
		{
			String name = def.getName();
			if(name == null) {
				def.setName(NoNameTitle+"_"+noNameCnt);
				noNameCnt++;
				
				if(def.getBinderId() == null ) {
					def.setBinderId(new Long(-1));
				}
				
				continue;
			}
			
			if(name.equalsIgnoreCase(prevName)){
				dupCnt+=1;
			} else {
				dupCnt = 1;
			}
			
			if(dupCnt > 1)
			{
				def.setName(name+"_"+dupCnt);
				getCoreDao().update(def);
			}
			
			if(def.getBinderId() == null ) {
				def.setBinderId(new Long(-1));
			}

			prevName = name;
		}
		
		//flush updates
		getCoreDao().flush();
		//flush cache
		for (int i=0; i<batch.size(); ++i) {
			getCoreDao().evict(batch.get(i));
		}
 	}

 	// Must be running inside a transaction set up by the caller 
 	protected void validateZoneTx(Workspace zone) {
 		String superName = SZoneConfig.getAdminUserName(zone.getName());
		//	get super user from config file - must exist or throws and error
		User superU = getProfileDao().findUserByName(superName, zone.getName());
		RequestContextUtil.setThreadContext(superU).resolve();
		if (!ObjectKeys.SUPER_USER_INTERNALID.equals(superU.getInternalId())) {
			superU.setInternalId(ObjectKeys.SUPER_USER_INTERNALID);
			//force update
			getCoreDao().merge(superU);	
			getProfileModule().indexEntry(superU);
		}
		ZoneConfig zoneConfig=null;
 		try {
 			zoneConfig = getZoneConfig(zone.getId());
 		} catch (NoObjectByTheIdException zx) {
			// Make sure there is a ZoneConfig; new for v2
 			zoneConfig = addZoneConfigTx(zone);
 		}
		getZoneClassManager().initialize();

		//make sure only one
		getCoreDao().executeUpdate(
				"update org.kablink.teaming.domain.User set internalId=null where " +
				"internalId='" + ObjectKeys.SUPER_USER_INTERNALID + 
				"' and zoneId=" + zone.getId() + 
				" and not id=" + superU.getId());
		//adds user to profileDao cache
		superU = getProfileDao().getReservedUser(ObjectKeys.SUPER_USER_INTERNALID, zone.getId());
		//make sure background user exists
		try {
			getProfileDao().getReservedUser(ObjectKeys.JOB_PROCESSOR_INTERNALID, zone.getId());
		} catch (NoUserByTheNameException nu) {
			//need to add it
			User u = addJobProcessor(superU.getParentBinder(), new HistoryStamp(superU));
			//updates cache
			getProfileDao().getReservedUser(ObjectKeys.JOB_PROCESSOR_INTERNALID, zone.getId());
			getProfileModule().indexEntry(u);
		}
		//make sure posting agent exists
		try {
			getProfileDao().getReservedUser(ObjectKeys.ANONYMOUS_POSTING_USER_INTERNALID, zone.getId());
		} catch (NoUserByTheNameException nu) {
			//need to add it
			User u = addPosting(superU.getParentBinder(), new HistoryStamp(superU));
			//updates cache
			getProfileDao().getReservedUser(ObjectKeys.ANONYMOUS_POSTING_USER_INTERNALID, zone.getId());
			getProfileModule().indexEntry(u);
		}
		//make sure synchronization agent exists
		if(getSynchronizationAgent(zone.getId()) == null) {
			//need to add it
			User u = addSynchronizationAgent(superU.getParentBinder(), new HistoryStamp(superU));
			setTokenRequesterInitialMembership(zoneConfig, u);
			//updates cache
			getProfileDao().getReservedUser(ObjectKeys.SYNCHRONIZATION_AGENT_INTERNALID, zone.getId());
			getProfileModule().indexEntry(u);
		}
		//make sure file sync agent exists
		if(getFileSyncAgent(zone.getId()) == null) {
			//need to add it
			User u = addFileSyncAgent(superU.getParentBinder(), new HistoryStamp(superU));
			//updates cache
			getProfileDao().getReservedUser(ObjectKeys.FILE_SYNC_AGENT_INTERNALID, zone.getId());
			getProfileModule().indexEntry(u);
		}
		//make sure guest exists
		User guest=null;
		try {
			Binder parent = superU.getParentBinder();
			String guestName= SZoneConfig.getString(parent.getRoot().getName(), "property[@name='guestUser']", ObjectKeys.GUEST);
			guest = getProfileDao().findUserByName(guestName, zone.getId());
			
			if(guest !=null ){
				String internalId = guest.getInternalId();
				// Make sure this isn't the reserved Guest User
				if(!ObjectKeys.GUEST_USER_INTERNALID.equals(internalId))
				{
					Map options = new HashMap();
					options.put(ObjectKeys.INPUT_OPTION_DELETE_USER_WORKSPACE, true);

			    		Long id = guest.getId();
			    		try {
			    			String name = null;
			    			getProfileModule().deleteEntry(id, options);
			    		} catch (Exception ex) {
			    			logger.error(ex);//logError(NLT.get("errorcode.ldap.delete", new Object[]{id.toString()}), ex);
			    		}
				}
			}
			
			// now see if we find the reserved Guest User
			guest = getProfileDao().getReservedUser(ObjectKeys.GUEST_USER_INTERNALID, zone.getId());
			boolean guestChanged = false;
			// Make sure guest has password.
			if(guest.getPassword() == null) {
				guest.setPassword(guest.getName());
				guestChanged = true;
			}
			// Make sure guest is categorized as external
			IdentityInfo ii = guest.getIdentityInfo();
			if(ii.isInternal()) {
				ii.setInternal(false);
				guest.setIdentityInfo(ii);
				guestChanged = true;
			}
			if(guestChanged) {
				getCoreDao().merge(guest);
			}
		} catch (NoUserByTheNameException nu) {
			//need to add it
			guest = addGuest(superU.getParentBinder(), new HistoryStamp(superU));
			//	updates cache
			getProfileDao().getReservedUser(ObjectKeys.GUEST_USER_INTERNALID, zone.getId());
			getProfileModule().indexEntry(guest);
		}
		if (guest.getWorkspaceId() == null) {
			Workspace guestWs = addGuestWorkspace(guest);
			getAdminModule().setWorkAreaOwner(guestWs, superU.getId(), true);
			addVisitorRoleToGuestWorkspace(guestWs, zone.getId());
		} else {
			Workspace guestWs = (Workspace)getBinderModule().getBinder(guest.getWorkspaceId());
			addVisitorRoleToGuestWorkspace(guestWs, zone.getId());
		}
		//make sure allUsers exists
		try {
			getProfileDao().getReservedGroup(ObjectKeys.ALL_USERS_GROUP_INTERNALID, zone.getId());
		} catch (NoGroupByTheNameException nu) {
			//need to add it
			Group g = addAllUserGroup(superU.getParentBinder(), new HistoryStamp(superU));
			//	updates cache
			getProfileDao().getReservedGroup(ObjectKeys.ALL_USERS_GROUP_INTERNALID, zone.getId());
			getProfileModule().indexEntry(g);
		}
		//make sure allExtUsers exists
		try {
			Group allExtUsers = getProfileDao().getReservedGroup(ObjectKeys.ALL_EXT_USERS_GROUP_INTERNALID, zone.getId());
			// make this this group is categorized as external
			IdentityInfo ii = allExtUsers.getIdentityInfo();
			if(ii.isInternal()) {
				ii.setInternal(false);
				allExtUsers.setIdentityInfo(ii);
				getCoreDao().merge(allExtUsers);
			}
		} catch (NoGroupByTheNameException nu) {
			//need to add it
			Group g = addAllExtUserGroup(superU.getParentBinder(), new HistoryStamp(superU));
			//	updates cache
			getProfileDao().getReservedGroup(ObjectKeys.ALL_EXT_USERS_GROUP_INTERNALID, zone.getId());
			getProfileModule().indexEntry(g);
		}
		//make sure allApplications exists
		try {
			getProfileDao().getReservedApplicationGroup(ObjectKeys.ALL_APPLICATIONS_GROUP_INTERNALID, zone.getId());
		} catch (NoGroupByTheNameException nu) {
			//need to add it
			ApplicationGroup g = addAllApplicationGroup(superU.getParentBinder(), new HistoryStamp(superU));
			//	updates cache
			getProfileDao().getReservedApplicationGroup(ObjectKeys.ALL_APPLICATIONS_GROUP_INTERNALID, zone.getId());
			getProfileModule().indexEntry(g);
		}
		//turn off or on depending on ssf.props value
		if (!SPropsUtil.getBoolean("smtp.service.enable")) {
			zoneConfig.getMailConfig().setSimpleUrlPostingEnabled(false);
			getAdminModule().setMailConfigAndSchedules(zoneConfig.getMailConfig(), null, null);
		}
		if (zoneConfig.getMailConfig().isPostingEnabled()) {
			if (SPropsUtil.getBoolean("mail.posting.offWhenEmpty", true) && 
				getAdminModule().getPostings().isEmpty()) {
			//remove old feature if not being used
			zoneConfig.getMailConfig().setPostingEnabled(false);
			getAdminModule().setMailConfigAndSchedules(zoneConfig.getMailConfig(), null, null);
			}
		} else if (!SPropsUtil.getBoolean("mail.posting.offWhenEmpty", true)) {
			zoneConfig.getMailConfig().setPostingEnabled(true);
		}
		//Turn on file version aging job
		ScheduleInfo info = getAdminModule().getFileVersionAgingSchedule();
		getAdminModule().setFileVersionAgingSchedule(info);

		//Enable/Disable access control rights
		if (!SPropsUtil.getBoolean("accessControl.viewBinderTitle.enabled", false)) {
			WorkAreaOperation.deleteInstance("viewBinderTitle");
		}
		
		//Make sure the Net Folders workspace exists
		Binder netFoldersBinder = null;
		try {
			netFoldersBinder = getCoreDao().loadReservedBinder(ObjectKeys.NET_FOLDERS_ROOT_INTERNALID, zone.getId());
		} catch(NoBinderByTheNameException e) {}
		if (netFoldersBinder == null) {
			//The Net Folders workspace doesn't exist, so create it
			Workspace top = getCoreDao().findTopWorkspace(zone.getName());
			HistoryStamp stamp = new HistoryStamp(superU);
			Workspace netFoldersRoot = addNetFoldersRoot(top, stamp);
		}
		//Make sure resource driver map is initialized
		getResourceDriverManager().resetResourceDriverList();

	}

 	// Must be running inside a transaction set up by the caller
 	protected Workspace addZoneTx(String zoneName, String zoneAdminName, String virtualHost) {
       		Workspace top = new Workspace();
    		top.setName(zoneName);
    		//temporary until have read id
    		top.setZoneId(new Long(-1));
    		top.setTitle(NLT.get("administration.initial.workspace.title", new Object[] {zoneName}, zoneName));
    		top.setPathName("/"+top.getTitle());
    		top.setInternalId(ObjectKeys.TOP_WORKSPACE_INTERNALID);
    		top.setTeamMembershipInherited(false);
    		top.setFunctionMembershipInherited(false);
    		top.setDefinitionsInherited(false);
    		//generate id for top and profiles
    		getCoreDao().save(top);
    		top.setZoneId(top.getId());
    		top.setupRoot();
    		
 
    		// some piece of code needs zone id in the context
    		RequestContextHolder.getRequestContext().setZoneId(top.getId());   		
	
    		ProfileBinder profiles = addPersonalRoot(top);
    		profiles.setFunctionMembershipInherited(false);

    		//build user
    		User user = new User(new IdentityInfo());
    		user.setName(zoneAdminName);
    		user.setPassword(zoneAdminName);
    		user.setLastName(zoneAdminName);
    		user.setForeignName(zoneAdminName);
    		user.setZoneId(top.getId());
    		user.setInternalId(ObjectKeys.SUPER_USER_INTERNALID);
    		user.setParentBinder(profiles);
    		String language = LocaleUtils.getLocaleLanguage();
    		String country = LocaleUtils.getLocaleCountry();
    		if (!language.equals("")) {
    			Locale locale = null;
    			if (!country.equals("")) locale = new Locale(language, country);
    			else locale = new Locale(language);
    			user.setLocale(locale);
    		}
    		getCoreDao().save(user);
    		//indexing and other modules needs the user
    		RequestContextHolder.getRequestContext().setUser(user).resolve();
    		
       		//Reset the top folder title to localized title now that the admin user is set up
    		top.setTitle(NLT.get("administration.initial.workspace.title", new Object[] {zoneName}, zoneName));
    		top.setPathName("/"+top.getTitle());
    		getCoreDao().save(top);

    		//Reset the profiles binder title, too
    		profiles.setTitle(NLT.get("administration.initial.profile.title", "Personal"));
    		profiles.setPathName(top.getPathName() + "/" + profiles.getTitle());
    		getCoreDao().save(profiles);

    		//set zone info after context is set
			ZoneConfig zoneConfig = addZoneConfigTx(top);
			getZoneClassManager().initialize();
			HistoryStamp stamp = new HistoryStamp(user);
    		//add reserved group for use in import templates
    		Group group = addAllUserGroup(profiles, stamp);
    		Group extGroup = addAllExtUserGroup(profiles, stamp);
    		ApplicationGroup applicationGroup = addAllApplicationGroup(profiles, stamp);
	
    		Function readRole = addEntryReadRole(top);
    		Function readReplyRole = addEntryReadReplyRole(top);
    		Function writeRole = addEntryWriteRole(top);
    		Function deleteRole = addEntryDeleteRole(top);
    		Function changeAclRole = addEntryChangeAclRole(top);
    		Function visitorsRole = addVisitorsRole(top);
    		Function participantsRole = addParticipantsRole(top);
    		Function guestParticipantRole = addGuestParticipantRole(top);
    		Function teamMemberRole = addTeamMemberRole(top);
    		Function binderRole = addBinderRole(top);
    		Function teamWsRole = addTeamWorkspaceRole(top);

    		//make sure allusers group and roles are defined, may be referenced by templates
    		getAdminModule().updateDefaultDefinitions(top.getId(), false);
    		getTemplateModule().updateDefaultTemplates(top.getId(), true);
 			Definition def = getDefinitionModule().getDefinitionByReservedId(ObjectKeys.DEFAULT_WELCOME_WORKSPACE_DEF);
    		//fill in config for top
			top.setEntryDef(def);
 			List<Definition>defs = top.getDefinitions();
 			defs.add(def);
 			//Update after import of definitions
    		getDefinitionModule().setDefaultBinderDefinition(profiles);
    		getDefinitionModule().setDefaultEntryDefinition(user);
    		//fill in config for profiles
    		defs = profiles.getDefinitions();
    		defs.add(profiles.getEntryDef());
    		defs.add(getCoreDao().loadDefinition(user.getEntryDefId(), RequestContextHolder.getRequestContext().getZoneId()));
    		    			        		
    		//fill in timestampes
    		top.setCreation(stamp);
    		top.setModification(stamp);
    		profiles.setCreation(stamp);
    		profiles.setModification(stamp);
    		user.setCreation(stamp);
    		user.setModification(stamp);
    		
    		//Mark that this user was created in the current version and therefore all upgrade tasks were done
    		getBinderModule().setProperty(top.getId(), ObjectKeys.BINDER_PROPERTY_UPGRADE_VERSION, ObjectKeys.PRODUCT_UPGRADE_VERSION);
    		getProfileModule().setUserProperty(user.getId(), ObjectKeys.USER_PROPERTY_UPGRADE_DEFINITIONS, "true");
    		getProfileModule().setUserProperty(user.getId(), ObjectKeys.USER_PROPERTY_UPGRADE_TEMPLATES, "true");
    		getProfileModule().setUserProperty(user.getId(), ObjectKeys.USER_PROPERTY_UPGRADE_SEARCH_INDEX, "true");
    		
    		//flush these changes, other reads may re-load
    		getCoreDao().flush();
	
    		addPosting(profiles, stamp);
    		addJobProcessor(profiles, stamp); 
    		addSynchronizationAgent(profiles, stamp);
    		addFileSyncAgent(profiles, stamp);
    		addGuest(profiles, stamp); 
    		Workspace globalRoot = addGlobalRoot(top, stamp);		
    		Workspace netFoldersRoot = addNetFoldersRoot(top, stamp);		
    		Workspace teamRoot = addTeamRoot(top, stamp);
    		teamRoot.setFunctionMembershipInherited(false);
    		
    		
    		//setup allUsers & allApplications access
    		List members = new ArrayList();
    		members.add(group.getId());
    		members.add(applicationGroup.getId());
    		//all users and all applications visitors at top
    		addMembership(top, visitorsRole, top, members);
    		// all users participants at top
    		members.remove(applicationGroup.getId());
    		addMembership(top, participantsRole, top, members);
    		// all users visitors at profiles
    		addMembership(top, visitorsRole, profiles, members);
    		// all users participants at teamroot
    		addMembership(top, participantsRole, teamRoot, members);
    		// all users createWs  at teamroot
    		addMembership(top, teamWsRole, teamRoot, members);
    		//add members to participants
    		members.clear();
    		members.add(ObjectKeys.TEAM_MEMBER_ID);
    		// all team members have team member role at top
    		addMembership(top, teamMemberRole, top, members);
    		// all team members have team member role at teamroot
    		addMembership(top, teamMemberRole, teamRoot, members);
    		
    		members.clear();
    		members.add(ObjectKeys.OWNER_USER_ID);
    		addMembership(top, binderRole, top, members);
    		addMembership(top, binderRole, profiles, members);
    		addMembership(top, binderRole, teamRoot, members);
	
    		members.clear();
    		members.add(user.getId());
    		addGlobalFunctions(zoneConfig);
    		
    		//all applications limited to participant for zone
    		setApplicationGlobalRoles(zoneConfig, applicationGroup, participantsRole);

    		//use module instead of processor directly so index synchronziation works correctly
    		//index flushes entries from session - don't make changes without reload
       		getBinderModule().indexTree(top.getId());
    		//this will force the Ids to be cached 
    		getProfileDao().getReservedGroup(ObjectKeys.ALL_USERS_GROUP_INTERNALID, top.getId());
    		getProfileDao().getReservedUser(ObjectKeys.ANONYMOUS_POSTING_USER_INTERNALID, top.getId());
    		//reload user as side effect after index flush
    		user = getProfileDao().getReservedUser(ObjectKeys.SUPER_USER_INTERNALID, top.getId());
    		getProfileDao().getReservedUser(ObjectKeys.JOB_PROCESSOR_INTERNALID, top.getId());
    		getProfileDao().getReservedUser(ObjectKeys.SYNCHRONIZATION_AGENT_INTERNALID, top.getId());
    		getProfileDao().getReservedUser(ObjectKeys.FILE_SYNC_AGENT_INTERNALID, top.getId());
    		getProfileDao().getReservedApplicationGroup(ObjectKeys.ALL_APPLICATIONS_GROUP_INTERNALID, top.getId());

    		ScheduleInfo info = getAdminModule().getNotificationSchedule();
   			info.getSchedule().setDaily(true);
   			info.getSchedule().setHours("0");
   			info.getSchedule().setMinutes("15");
   			info.setEnabled(true);
			zoneConfig.getMailConfig().setSendMailEnabled(true);
			getAdminModule().setMailConfigAndSchedules(zoneConfig.getMailConfig(), info, null); 
			
			//Turn on file version aging job
			info = getAdminModule().getFileVersionAgingSchedule();
			getAdminModule().setFileVersionAgingSchedule(info);
			
			setupInitialOpenIDProviderList();

			// If we are running Filr reset the templates.
			if ( Utils.checkIfFilr() )
			{
				getTemplateModule().updateDefaultTemplates( top.getId(), true );
			}

    		return top;
 	}
 	
	protected Long addZone(final String name, final String virtualHost) {
		final String adminName = SZoneConfig.getAdminUserName(name);
		RequestContext oldCtx = RequestContextHolder.getRequestContext();
		RequestContextUtil.setThreadContext(name, adminName);
		try {
			Workspace zone = null;
			IndexSynchronizationManager.setForceSequential();
			try {		
	  	        zone =  (Workspace) getTransactionTemplate().execute(new TransactionCallback() {
		        	public Object doInTransaction(TransactionStatus status) {
		    			IndexSynchronizationManager.begin();
	
		        		Workspace zone = addZoneTx(name, adminName, virtualHost);
		        			        		
		    			User guest = getProfileDao().getReservedUser(ObjectKeys.GUEST_USER_INTERNALID, zone.getId());
		    			Workspace guestWs = addGuestWorkspace(guest);
		        		//now change owner to admin
		        		getAdminModule().setWorkAreaOwner(guestWs, zone.getOwnerId() ,true);
		        		addVisitorRoleToGuestWorkspace(guestWs, zone.getId());
		        		//do now, with request context set - won't have one if here on zone startup
		        		IndexSynchronizationManager.applyChanges();
		    		
		        		return zone;
		        	}
		        });
			}
			finally {
				IndexSynchronizationManager.clearForceSequential();				
			}
  	        //do outside of transaction, so commited.
  	        //otherwise jobs may start and fail cause data not saved.
        	for (ZoneSchedule zoneM:startupModules) {
				zoneM.startScheduledJobs(zone);
			}
        	return zone.getId();
		} finally  {
			//leave new context for indexing
			RequestContextHolder.setRequestContext(oldCtx);
		}

	}
	
	/**
	 * Remove a zone deleteing all its contents.
	 * @param zoneName
	 */
	protected abstract boolean removeZone(String zoneName);

    private Group addAllUserGroup(Binder parent, HistoryStamp stamp) {
		//build allUsers group
		Group group = new Group(new IdentityInfo());
		group.setName("allUsers");
		group.setForeignName(group.getName());
		group.setTitle(NLT.get("administration.initial.group.alluser.title", group.getName()));
		group.setZoneId(parent.getZoneId());
		group.setParentBinder(parent);
		group.setInternalId(ObjectKeys.ALL_USERS_GROUP_INTERNALID);
		getDefinitionModule().setDefaultEntryDefinition(group);
		getCoreDao().save(group);
		group.setCreation(stamp);
		group.setModification(stamp);
		return group;
	}
    private Group addAllExtUserGroup(Binder parent, HistoryStamp stamp) {
		//build allExtUsers group
		Group group = new Group(new IdentityInfo(false, false, true, false));
		group.setName("allExtUsers");
		group.setForeignName(group.getName());
		group.setTitle(NLT.get("administration.initial.group.allextuser.title", group.getName()));
		group.setZoneId(parent.getZoneId());
		group.setParentBinder(parent);
		group.setInternalId(ObjectKeys.ALL_EXT_USERS_GROUP_INTERNALID);
		getDefinitionModule().setDefaultEntryDefinition(group);
		getCoreDao().save(group);
		group.setCreation(stamp);
		group.setModification(stamp);
		return group;
	}
    private ApplicationGroup addAllApplicationGroup(Binder parent, HistoryStamp stamp) {
		//build allApplications group
		ApplicationGroup group = new ApplicationGroup();
		group.setName("allApplications");
		group.setForeignName(group.getName());
		group.setTitle(NLT.get("administration.initial.applicationgroup.allapplication.title", group.getName()));
		group.setZoneId(parent.getZoneId());
		group.setParentBinder(parent);
		group.setInternalId(ObjectKeys.ALL_APPLICATIONS_GROUP_INTERNALID);
		getDefinitionModule().setDefaultEntryDefinition(group);
		getCoreDao().save(group);
		group.setCreation(stamp);
		group.setModification(stamp);
		return group;
	}
	private User addReservedUser(Binder parent, HistoryStamp stamp, String name, String password, String title, String id, IdentityInfo identityInfo) {
		
		if(identityInfo == null)
			identityInfo = new IdentityInfo();
		User user = new User(identityInfo);
		user.setName(name);
		if(password != null) // optional field
			user.setPassword(password);
		user.setForeignName(name);
		user.setLastName(title);
		user.setZoneId(parent.getZoneId());
		user.setParentBinder(parent);
		user.setInternalId(id);
		getDefinitionModule().setDefaultEntryDefinition(user);
		user.setCreation(stamp);
		user.setModification(stamp);
		String language = LocaleUtils.getLocaleLanguage();
		String country = LocaleUtils.getLocaleCountry();
		if (!language.equals("")) {
			Locale locale = null;
			if (!country.equals("")) locale = new Locale(language, country);
			else locale = new Locale(language);
			user.setLocale(locale);
		}
		getCoreDao().save(user);
		return user;
	}
	private User addPosting(Binder parent, HistoryStamp stamp) {
		return addReservedUser(parent, stamp, "_postingAgent", null, NLT.get("administration.initial.postingAgent.title"), ObjectKeys.ANONYMOUS_POSTING_USER_INTERNALID, null);
	}
	private User addJobProcessor(Binder parent, HistoryStamp stamp) {
		return addReservedUser(parent, stamp, "_jobProcessingAgent", null, NLT.get("administration.initial.jobProcessor.title"), ObjectKeys.JOB_PROCESSOR_INTERNALID, null);
	}
	private User addSynchronizationAgent(Binder parent, HistoryStamp stamp) {
		return addReservedUser(parent, stamp, "_synchronizationAgent", null, NLT.get("administration.initial.synchronizationAgent.title"), ObjectKeys.SYNCHRONIZATION_AGENT_INTERNALID, null);
	}
	private User addFileSyncAgent(Binder parent, HistoryStamp stamp) {
		return addReservedUser(parent, stamp, "_fileSyncAgent", null, NLT.get("administration.initial.fileSyncAgent.title"), ObjectKeys.FILE_SYNC_AGENT_INTERNALID, null);
	}
	private User addGuest(Binder parent, HistoryStamp stamp) {
		String guestName= SZoneConfig.getString(parent.getRoot().getName(), "property[@name='guestUser']", ObjectKeys.GUEST);
		return addReservedUser(parent, stamp, guestName, guestName, NLT.get("administration.initial.guestTitle"), ObjectKeys.GUEST_USER_INTERNALID, new IdentityInfo(false, false, true, false));
		
	}
	private Workspace addTeamRoot(Workspace top, HistoryStamp stamp) {
		Workspace team = new Workspace();
		team.setCreation(stamp);
		team.setModification(stamp);
		team.setName("_teams");
		team.setTitle(NLT.get("administration.initial.team.title", "Teams"));
		team.setPathName(top.getPathName() + "/" + team.getTitle());
		team.setZoneId(top.getId());
		team.setInternalId(ObjectKeys.TEAM_ROOT_INTERNALID);
		team.setIconName("/icons/team_workspace.gif");
		List<Definition> defs = getCoreDao().loadDefinitions(new FilterControls("type", Definition.WORKSPACE_VIEW), top.getId());
		//find the definition for a root team space
		Definition teamDef=null;
		for (Definition d:defs) {
			String viewType = DefinitionUtils.getViewType(d.getDefinition());
			if (!Definition.VIEW_STYLE_TEAM_ROOT.equals(viewType)) continue;
			teamDef = d;
			break;
		}
		if (teamDef == null) getDefinitionModule().setDefaultBinderDefinition(team);
		else team.setEntryDef(teamDef);
		top.addBinder(team);
		team.setDefinitionsInherited(false);
		defs = team.getDefinitions();
		defs.add(team.getEntryDef());
		//generate id for top and profiles
		getCoreDao().save(team);
		getCoreDao().updateFileName(top, team, null, team.getTitle());
		return team;
		
	}
	private Workspace addGlobalRoot(Workspace top, HistoryStamp stamp) {
		Workspace global = new Workspace();
		global.setCreation(stamp);
		global.setModification(stamp);
		
		global.setName("_global");
		global.setTitle(NLT.get("administration.initial.global.title", "Global"));
		global.setPathName(top.getPathName() + "/" + global.getTitle());
		global.setZoneId(top.getId());
		global.setInternalId(ObjectKeys.GLOBAL_ROOT_INTERNALID);
		global.setIconName("/icons/workspace.gif");
		getDefinitionModule().setDefaultBinderDefinition(global);
		top.addBinder(global);
		global.setDefinitionsInherited(false);
		List defs = global.getDefinitions();
		defs.add(global.getEntryDef());
		
		//generate id for top and profiles
		getCoreDao().save(global);
		getCoreDao().updateFileName(top, global, null, global.getTitle());
		return global;
	}
	private Workspace addNetFoldersRoot(Workspace top, HistoryStamp stamp) {
		Workspace netFolders = new Workspace();
		netFolders.setCreation(stamp);
		netFolders.setModification(stamp);
		
		netFolders.setName("_net_folders");
		netFolders.setTitle(NLT.get("administration.initial.netFolders.title", "Net Folders"));
		netFolders.setPathName(top.getPathName() + "/" + netFolders.getTitle());
		netFolders.setZoneId(top.getId());
		netFolders.setInternalId(ObjectKeys.NET_FOLDERS_ROOT_INTERNALID);
		netFolders.setIconName("/icons/workspace.gif");
		getDefinitionModule().setDefaultBinderDefinition(netFolders);
		top.addBinder(netFolders);
		netFolders.setDefinitionsInherited(false);
		List defs = netFolders.getDefinitions();
		defs.add(netFolders.getEntryDef());
		
		//generate id for top and profiles
		getCoreDao().save(netFolders);
		getCoreDao().updateFileName(top, netFolders, null, netFolders.getTitle());
		return netFolders;
	}
	private ProfileBinder addPersonalRoot(Workspace top) {
		ProfileBinder profiles = new ProfileBinder();
		profiles.setName("_profiles");
		profiles.setTitle(NLT.get("administration.initial.profile.title", "Personal"));
		profiles.setPathName(top.getPathName() + "/" + profiles.getTitle());
		profiles.setZoneId(top.getId());
		profiles.setInternalId(ObjectKeys.PROFILE_ROOT_INTERNALID);
		profiles.setIconName("/icons/workspace_personal.gif");
		top.addBinder(profiles);
		profiles.setDefinitionsInherited(false);
		//generate id for top and profiles
		getCoreDao().save(profiles);
		getCoreDao().updateFileName(top, profiles, null, profiles.getTitle());
		return profiles;
	}
	private Function addVisitorsRole(Workspace top) {
		Function function = new Function();
		function.setZoneId(top.getId());
		function.setName(ObjectKeys.ROLE_TITLE_VISITOR);
		function.setScope(ObjectKeys.ROLE_TYPE_BINDER);

		function.addOperation(WorkAreaOperation.READ_ENTRIES);
		function.addOperation(WorkAreaOperation.ADD_REPLIES);
		
		//generate functionId
		getFunctionManager().addFunction(function);		
		return function;
	}
	private Function addParticipantsRole(Workspace top) {
		Function function = new Function();
		function.setZoneId(top.getId());
		function.setName(ObjectKeys.ROLE_TITLE_PARTICIPANT);
		function.setScope(ObjectKeys.ROLE_TYPE_BINDER);

		function.addOperation(WorkAreaOperation.READ_ENTRIES);
		function.addOperation(WorkAreaOperation.CREATE_ENTRIES);
		function.addOperation(WorkAreaOperation.CREATOR_MODIFY);
		function.addOperation(WorkAreaOperation.CREATOR_DELETE);
		function.addOperation(WorkAreaOperation.ADD_REPLIES);
		function.addOperation(WorkAreaOperation.CREATOR_CREATE_ENTRY_ACLS);
//		function.addOperation(WorkAreaOperation.USER_SEE_ALL);
		
		//generate functionId
		getFunctionManager().addFunction(function);
		return function;
	}

	private Function addGuestParticipantRole(Workspace top) {
		Function function = new Function();
		function.setZoneId(top.getId());
		function.setName(ObjectKeys.ROLE_TITLE_GUEST_PARTICIPANT);
		function.setScope(ObjectKeys.ROLE_TYPE_BINDER);

		function.addOperation(WorkAreaOperation.READ_ENTRIES);
		function.addOperation(WorkAreaOperation.CREATE_ENTRIES);
		function.addOperation(WorkAreaOperation.ADD_REPLIES);
		
		//generate functionId
		getFunctionManager().addFunction(function);
		return function;
	}

	private Function addViewBinderTitleRole(Long zoneId) {
		Function function = getFunctionManager().findFunctionByName(zoneId, ObjectKeys.ROLE_TITLE_VIEW_BINDER_TITLE);
		if (function != null) {
			function.setInternalId(ObjectKeys.FUNCTION_VIEW_BINDER_TITLE_INTERNALID);
			getFunctionManager().updateFunction(function);
		} else {
			function = new Function();
			function.setZoneId(zoneId);
			function.setName(ObjectKeys.ROLE_TITLE_VIEW_BINDER_TITLE);
			function.setScope(ObjectKeys.ROLE_TYPE_BINDER);
			function.setInternalId(ObjectKeys.FUNCTION_VIEW_BINDER_TITLE_INTERNALID);
			function.addOperation(WorkAreaOperation.VIEW_BINDER_TITLE);
			//generate functionId
			getFunctionManager().addFunction(function);
		}
		return function;
	}

	private Function addTeamMemberRole(Workspace top) {
		Function function = new Function();
		function.setZoneId(top.getId());
		function.setName(ObjectKeys.ROLE_TITLE_TEAM_MEMBER);
		function.setScope(ObjectKeys.ROLE_TYPE_BINDER);
		function.addOperation(WorkAreaOperation.READ_ENTRIES);
		function.addOperation(WorkAreaOperation.CREATE_ENTRIES);
		function.addOperation(WorkAreaOperation.CREATOR_MODIFY);
		function.addOperation(WorkAreaOperation.CREATOR_DELETE);
		function.addOperation(WorkAreaOperation.ADD_REPLIES);
		function.addOperation(WorkAreaOperation.ADD_COMMUNITY_TAGS);
		//function.addOperation(WorkAreaOperation.CREATE_FOLDERS);  //Turned off to lessen the default power of a team member
		//function.addOperation(WorkAreaOperation.CREATE_WORKSPACES);
		//function.addOperation(WorkAreaOperation.DELETE_ENTRIES);
		//function.addOperation(WorkAreaOperation.MODIFY_ENTRIES);
		function.addOperation(WorkAreaOperation.GENERATE_REPORTS);
		function.addOperation(WorkAreaOperation.CREATOR_CREATE_ENTRY_ACLS);

		//generate functionId
		getFunctionManager().addFunction(function);
		return function;
	}
	private Function addBinderRole(Workspace top) {
		Function function = new Function();
		function.setZoneId(top.getId());
		function.setName(ObjectKeys.ROLE_TITLE_BINDER_ADMIN);
		function.setScope(ObjectKeys.ROLE_TYPE_BINDER);
		//add all them remove a few
		for (Iterator iter=WorkAreaOperation.getWorkAreaOperations(); iter.hasNext();) {
			function.addOperation((WorkAreaOperation)iter.next());			
		}
//		function.removeOperation(WorkAreaOperation.USER_SEE_COMMUNITY);
		
		//generate functionId
		getFunctionManager().addFunction(function);
		return function;
	}
/**No longer used, since the SITE_ADMIN right only made sense on top workspace.
 * For V2, this is no longer visible.	
  private Function addAdminRole(Workspace top) {
		Function function = new Function();
		function.setZoneId(top.getId());
		function.setName(ObjectKeys.ROLE_TITLE_SITE_ADMIN);
		for (Iterator iter = WorkAreaOperation.getWorkAreaOperations(); iter.hasNext();) {
			function.addOperation(((WorkAreaOperation)iter.next()));
		}	
//		function.removeOperation(WorkAreaOperation.USER_SEE_COMMUNITY);
		//generate functionId
		getFunctionManager().addFunction(function);
		return function;
	}
	**/
	private Function addTeamWorkspaceRole(Workspace top) {
		Function function = new Function();
		function.setZoneId(top.getId());
		function.setName(ObjectKeys.ROLE_TITLE_WORKSPACE_CREATOR);
		function.setScope(ObjectKeys.ROLE_TYPE_BINDER);
		function.addOperation(WorkAreaOperation.CREATE_WORKSPACES);
		//generate functionId
		getFunctionManager().addFunction(function);
		return function;
	}

	private void addMembership(Workspace top, Function function, WorkArea workArea, List ids) {
		WorkAreaFunctionMembership ms = new WorkAreaFunctionMembership();
		ms.setWorkAreaId(workArea.getWorkAreaId());
		ms.setWorkAreaType(workArea.getWorkAreaType());
		ms.setZoneId(top.getId());
		ms.setFunctionId(function.getId());
		Set members = new HashSet(ids);
		ms.setMemberIds(members);
		getCoreDao().save(ms);
		
	}
	private Function addEntryReadRole(Workspace top) {
		Function function = new Function();
		function.setZoneId(top.getId());
		function.setName(ObjectKeys.ROLE_TITLE_READ);
		function.setScope(ObjectKeys.ROLE_TYPE_ENTRY);

		function.addOperation(WorkAreaOperation.READ_ENTRIES);
		
		//generate functionId
		getFunctionManager().addFunction(function);		
		return function;
	}
	private Function addEntryReadReplyRole(Workspace top) {
		Function function = new Function();
		function.setZoneId(top.getId());
		function.setName(ObjectKeys.ROLE_TITLE_READ_REPLY);
		function.setScope(ObjectKeys.ROLE_TYPE_ENTRY);

		function.addOperation(WorkAreaOperation.READ_ENTRIES);
		function.addOperation(WorkAreaOperation.ADD_REPLIES);
		
		//generate functionId
		getFunctionManager().addFunction(function);		
		return function;
	}
	private Function addEntryWriteRole(Workspace top) {
		Function function = new Function();
		function.setZoneId(top.getId());
		function.setName(ObjectKeys.ROLE_TITLE_WRITE);
		function.setScope(ObjectKeys.ROLE_TYPE_ENTRY);

		function.addOperation(WorkAreaOperation.READ_ENTRIES);
		function.addOperation(WorkAreaOperation.ADD_REPLIES);
		function.addOperation(WorkAreaOperation.MODIFY_ENTRIES);
		function.addOperation(WorkAreaOperation.ADD_COMMUNITY_TAGS);
		function.addOperation(WorkAreaOperation.GENERATE_REPORTS);
		
		//generate functionId
		getFunctionManager().addFunction(function);
		return function;
	}
	private Function addEntryDeleteRole(Workspace top) {
		Function function = new Function();
		function.setZoneId(top.getId());
		function.setName(ObjectKeys.ROLE_TITLE_DELETE);
		function.setScope(ObjectKeys.ROLE_TYPE_ENTRY);

		function.addOperation(WorkAreaOperation.READ_ENTRIES);
		function.addOperation(WorkAreaOperation.ADD_REPLIES);
		function.addOperation(WorkAreaOperation.MODIFY_ENTRIES);
		function.addOperation(WorkAreaOperation.DELETE_ENTRIES);
		function.addOperation(WorkAreaOperation.ADD_COMMUNITY_TAGS);
		function.addOperation(WorkAreaOperation.GENERATE_REPORTS);
		
		//generate functionId
		getFunctionManager().addFunction(function);
		return function;
	}
	private Function addEntryChangeAclRole(Workspace top) {
		Function function = new Function();
		function.setZoneId(top.getId());
		function.setName(ObjectKeys.ROLE_TITLE_CHANGE_ACL);
		function.setScope(ObjectKeys.ROLE_TYPE_ENTRY);

		function.addOperation(WorkAreaOperation.READ_ENTRIES);
		function.addOperation(WorkAreaOperation.ADD_REPLIES);
		function.addOperation(WorkAreaOperation.MODIFY_ENTRIES);
		function.addOperation(WorkAreaOperation.DELETE_ENTRIES);
		function.addOperation(WorkAreaOperation.CHANGE_ACCESS_CONTROL);
		function.addOperation(WorkAreaOperation.ADD_COMMUNITY_TAGS);
		function.addOperation(WorkAreaOperation.GENERATE_REPORTS);
		
		//generate functionId
		getFunctionManager().addFunction(function);
		return function;
	}

	private Function addFilrRoleViewer(Long zoneId) {
		// VIEWER: read only (read, open, file scan)
		Function function = new Function();
		function.setZoneId(zoneId);
		function.setName(ObjectKeys.ROLE_TITLE_FILR_VIEWER);
		function.setScope(ObjectKeys.ROLE_TYPE_FILR);
		function.setInternalId(ObjectKeys.FUNCTION_FILR_VIEWER_INTERNALID);
		fillFilrRoleViewer(function);
		getFunctionManager().addFunction(function);		
		return function;
	}
	//This routine should be kept in sync with the Viewer role definition in ShareItem
	private void fillFilrRoleViewer(Function function) {
		function.addOperation(WorkAreaOperation.READ_ENTRIES);
		function.addOperation(WorkAreaOperation.ADD_REPLIES);
	}
	
	private Function addFilrRoleEditor(Long zoneId) {
		// EDITOR: read and write (read, open, file scan, write)
		Function function = new Function();
		function.setZoneId(zoneId);
		function.setName(ObjectKeys.ROLE_TITLE_FILR_EDITOR); 
		function.setScope(ObjectKeys.ROLE_TYPE_FILR);
		function.setInternalId(ObjectKeys.FUNCTION_FILR_EDITOR_INTERNALID);
		fillFilrRoleEditor(function);
		getFunctionManager().addFunction(function);		
		return function;
	}
	//This routine should be kept in sync with the Editor role definition in ShareItem
	private void fillFilrRoleEditor(Function function) {
		function.addOperation(WorkAreaOperation.READ_ENTRIES);
		function.addOperation(WorkAreaOperation.ADD_REPLIES);
		// This is tricky. On the file system, the right to modify content of a file is separate from the right
		// to rename a file. However, traditionally on the Filr side, these two rights were combined into a single
		// right called MODIFY_ENTRIES. So we address this problem by differentiating those two modes in the 
		// application layer (as opposed to simply invoking low-level access checking manager), and if it is
		// detected that the nature of the modification is for renaming, then the right to check against is
		// escalated from MODIFY_ENTRIES to a pair of CREATE_ENTRIES and DELETE_ENTRIES, which effectively
		// requires the principal to have the CONTRIBUTOR role on that file.
		function.addOperation(WorkAreaOperation.MODIFY_ENTRIES);		
	}
	
	private Function addFilrRoleContributor(Long zoneId) {
		// CONTRIBUTOR: all rights (read, open, file scan, write, create, erase/delete, modify/rename)
		Function function = new Function();
		function.setZoneId(zoneId);
		function.setName(ObjectKeys.ROLE_TITLE_FILR_CONTRIBUTOR);
		function.setScope(ObjectKeys.ROLE_TYPE_FILR);
		function.setInternalId(ObjectKeys.FUNCTION_FILR_CONTRIBUTOR_INTERNALID);
		fillFilrRoleContributor(function);
		getFunctionManager().addFunction(function);		
		return function;
	}
	//This routine should be kept in sync with the Contributor role definition in ShareItem
	private void fillFilrRoleContributor(Function function) {
		function.addOperation(WorkAreaOperation.READ_ENTRIES);
		function.addOperation(WorkAreaOperation.ADD_REPLIES);
		function.addOperation(WorkAreaOperation.MODIFY_ENTRIES);
		function.addOperation(WorkAreaOperation.CREATE_ENTRIES);
		function.addOperation(WorkAreaOperation.CREATE_FOLDERS);		
		function.addOperation(WorkAreaOperation.DELETE_ENTRIES);
		function.addOperation(WorkAreaOperation.BINDER_ADMINISTRATION);
		function.addOperation(WorkAreaOperation.CHANGE_ACCESS_CONTROL);
		function.addOperation(WorkAreaOperation.ADD_COMMUNITY_TAGS);
		function.addOperation(WorkAreaOperation.GENERATE_REPORTS);		
	}
	
	private User getSynchronizationAgent(Long zoneId) {
		try {
			return getProfileDao().getReservedUser(ObjectKeys.SYNCHRONIZATION_AGENT_INTERNALID, zoneId);
		} catch (NoUserByTheNameException nu) {
			return null;
		}
	}
	
	private User getFileSyncAgent(Long zoneId) {
		try {
			return getProfileDao().getReservedUser(ObjectKeys.FILE_SYNC_AGENT_INTERNALID, zoneId);
		} catch (NoUserByTheNameException nu) {
			return null;
		}
	}
	
	private void setTokenRequesterInitialMembership(ZoneConfig zoneConfig, User synchAgent) {
		List<Function> functions = getFunctionManager().findFunctions(zoneConfig.getZoneId());
		Function function = null;
		for(Function f:functions) {
			if(ObjectKeys.FUNCTION_TOKEN_REQUESTER_INTERNALID.equals(f.getInternalId()) &&
					ObjectKeys.ROLE_TOKEN_REQUESTER.equals(f.getName())) {
				function = f;
				break;
			}
		}
		if(function != null) {
			WorkAreaFunctionMembership ms = getWorkAreaFunctionMembershipManager().
				getWorkAreaFunctionMembership(zoneConfig.getZoneId(), zoneConfig, function.getId());
			if(ms != null) {
				Set<Long> memberIds = ms.getMemberIds();
				if(!memberIds.contains(synchAgent.getId())) {
					memberIds.add(synchAgent.getId());
					ms.setMemberIds(memberIds);
				}
			}
		}
	}
	
	private void addGlobalFunctions(ZoneConfig zoneConfig) {
		Set<Long> members = new HashSet();
		Function function;
		List<Function> functions = getFunctionManager().findFunctions(zoneConfig.getZoneId());
		Map functionNames = new HashMap();
		Map functionInternalIds = new HashMap();
		for (int i = 0; i < functions.size(); i++) {
			function = (Function)functions.get(i);
			functionNames.put(function.getName(), function);
			if (function.getInternalId() != null) functionInternalIds.put(function.getInternalId(), function);
		}
		
		if (!functionInternalIds.containsKey(ObjectKeys.FUNCTION_SITE_ADMIN_INTERNALID) && 
				!functionNames.containsKey(ObjectKeys.ROLE_ZONE_ADMINISTRATION)) {
			function = new Function();
			function.setZoneId(zoneConfig.getZoneId());
			function.setName(ObjectKeys.ROLE_ZONE_ADMINISTRATION);
			function.setScope(ObjectKeys.ROLE_TYPE_ZONE);
			function.setInternalId(ObjectKeys.FUNCTION_SITE_ADMIN_INTERNALID);
			function.addOperation(WorkAreaOperation.ZONE_ADMINISTRATION);
			function.setZoneWide(true);
			//generate functionId
			getFunctionManager().addFunction(function);
			setGlobalWorkareaFunctionMembership(zoneConfig, function, members);
		}
		
		if (!functionInternalIds.containsKey(ObjectKeys.FUNCTION_ADD_GUEST_ACCESS_INTERNALID) && 
				!functionNames.containsKey(ObjectKeys.ROLE_ADD_GUEST_ACCESS)) {
			function = new Function();
			function.setZoneId(zoneConfig.getZoneId());
			function.setName(ObjectKeys.ROLE_ADD_GUEST_ACCESS);
			function.setScope(ObjectKeys.ROLE_TYPE_ZONE);
			function.setInternalId(ObjectKeys.FUNCTION_ADD_GUEST_ACCESS_INTERNALID);
			function.addOperation(WorkAreaOperation.ADD_GUEST_ACCESS);
			function.setZoneWide(true);
			//generate functionId
			getFunctionManager().addFunction(function);
			setGlobalWorkareaFunctionMembership(zoneConfig, function, members);
		}
		
		if (!functionInternalIds.containsKey(ObjectKeys.FUNCTION_TOKEN_REQUESTER_INTERNALID) && 
				!functionNames.containsKey(ObjectKeys.ROLE_TOKEN_REQUESTER)) {
			function = new Function();
			function.setZoneId(zoneConfig.getZoneId());
			function.setName(ObjectKeys.ROLE_TOKEN_REQUESTER);
			function.setScope(ObjectKeys.ROLE_TYPE_ZONE);
			function.setInternalId(ObjectKeys.FUNCTION_TOKEN_REQUESTER_INTERNALID);
			function.addOperation(WorkAreaOperation.TOKEN_REQUEST);
			function.setZoneWide(true);
			//generate functionId
			getFunctionManager().addFunction(function);
			Set<Long> mbrs = new HashSet();
			User synchAgent = getSynchronizationAgent(zoneConfig.getZoneId());
			if(synchAgent != null)
				mbrs.add(synchAgent.getId());
			setGlobalWorkareaFunctionMembership(zoneConfig, function, mbrs);
		}
			
		if (!functionInternalIds.containsKey(ObjectKeys.FUNCTION_ONLY_SEE_GROUP_MEMBERS_INTERNALID)) {
			function = new Function();
			function.setZoneId(zoneConfig.getZoneId());
			function.setName(ObjectKeys.ROLE_ONLY_SEE_GROUP_MEMBERS);
			function.setScope(ObjectKeys.ROLE_TYPE_ZONE);
			function.setInternalId(ObjectKeys.FUNCTION_ONLY_SEE_GROUP_MEMBERS_INTERNALID);
			function.addOperation(WorkAreaOperation.ONLY_SEE_GROUP_MEMBERS);
			function.setZoneWide(true);
			//generate functionId
			getFunctionManager().addFunction(function);
			setGlobalWorkareaFunctionMembership(zoneConfig, function, new HashSet());
		}
		
		if (!functionInternalIds.containsKey(ObjectKeys.FUNCTION_OVERRIDE_ONLY_SEE_GROUP_MEMBERS_INTERNALID)) {
			function = new Function();
			function.setZoneId(zoneConfig.getZoneId());
			function.setName(ObjectKeys.ROLE_OVERRIDE_ONLY_SEE_GROUP_MEMBERS);
			function.setScope(ObjectKeys.ROLE_TYPE_ZONE);
			function.setInternalId(ObjectKeys.FUNCTION_OVERRIDE_ONLY_SEE_GROUP_MEMBERS_INTERNALID);
			function.addOperation(WorkAreaOperation.OVERRIDE_ONLY_SEE_GROUP_MEMBERS);
			function.setZoneWide(true);
			//generate functionId
			getFunctionManager().addFunction(function);
			setGlobalWorkareaFunctionMembership(zoneConfig, function, new HashSet());
		}
		
		if (!functionInternalIds.containsKey(ObjectKeys.FUNCTION_MANAGE_RESOURCE_DRIVERS_INTERNALID)) {
			function = new Function();
			function.setZoneId(zoneConfig.getZoneId());
			function.setName(ObjectKeys.ROLE_MANAGE_RESOURCE_DRIVERS);
			function.setScope(ObjectKeys.ROLE_TYPE_ZONE);
			function.setInternalId(ObjectKeys.FUNCTION_MANAGE_RESOURCE_DRIVERS_INTERNALID);
			function.addOperation(WorkAreaOperation.MANAGE_RESOURCE_DRIVERS);
			function.setZoneWide(true);
			//generate functionId
			getFunctionManager().addFunction(function);
			setGlobalWorkareaFunctionMembership(zoneConfig, function, new HashSet());
		}
		
		if (!functionInternalIds.containsKey(ObjectKeys.FUNCTION_CREATE_FILESPACES_INTERNALID)) {
			function = new Function();
			function.setZoneId(zoneConfig.getZoneId());
			function.setName(ObjectKeys.ROLE_CREATE_FILESPACES);
			function.setScope(ObjectKeys.ROLE_TYPE_ZONE);
			function.setInternalId(ObjectKeys.FUNCTION_CREATE_FILESPACES_INTERNALID);
			function.addOperation(WorkAreaOperation.CREATE_FILESPACE);
			function.setZoneWide(true);
			//generate functionId
			getFunctionManager().addFunction(function);
			setGlobalWorkareaFunctionMembership(zoneConfig, function, new HashSet());
		}
		
		if (!functionInternalIds.containsKey(ObjectKeys.FUNCTION_ENABLE_INTERNAL_SHARING_INTERNALID)) {
			function = new Function();
			function.setZoneId(zoneConfig.getZoneId());
			function.setName(ObjectKeys.ROLE_ENABLE_SHARING_INTERNAL);
			function.setScope(ObjectKeys.ROLE_TYPE_ZONE);
			function.setInternalId(ObjectKeys.FUNCTION_ENABLE_INTERNAL_SHARING_INTERNALID);
			function.addOperation(WorkAreaOperation.ENABLE_SHARING_INTERNAL);
			function.setZoneWide(true);
			//generate functionId
			getFunctionManager().addFunction(function);
			setGlobalWorkareaFunctionMembership(zoneConfig, function, new HashSet());
		}
		
		if (!functionInternalIds.containsKey(ObjectKeys.FUNCTION_ENABLE_EXTERNAL_SHARING_INTERNALID)) {
			function = new Function();
			function.setZoneId(zoneConfig.getZoneId());
			function.setName(ObjectKeys.ROLE_ENABLE_SHARING_EXTERNAL);
			function.setScope(ObjectKeys.ROLE_TYPE_ZONE);
			function.setInternalId(ObjectKeys.FUNCTION_ENABLE_EXTERNAL_SHARING_INTERNALID);
			function.addOperation(WorkAreaOperation.ENABLE_SHARING_EXTERNAL);
			function.setZoneWide(true);
			//generate functionId
			getFunctionManager().addFunction(function);
			setGlobalWorkareaFunctionMembership(zoneConfig, function, new HashSet());
		}
		
		if (!functionInternalIds.containsKey(ObjectKeys.FUNCTION_ENABLE_PUBLIC_SHARING_INTERNALID)) {
			function = new Function();
			function.setZoneId(zoneConfig.getZoneId());
			function.setName(ObjectKeys.ROLE_ENABLE_SHARING_PUBLIC);
			function.setScope(ObjectKeys.ROLE_TYPE_ZONE);
			function.setInternalId(ObjectKeys.FUNCTION_ENABLE_PUBLIC_SHARING_INTERNALID);
			function.addOperation(WorkAreaOperation.ENABLE_SHARING_PUBLIC);
			function.setZoneWide(true);
			//generate functionId
			getFunctionManager().addFunction(function);
			setGlobalWorkareaFunctionMembership(zoneConfig, function, new HashSet());
		} else {
			//Due to earlier bug, test that this function is correctly marked as zoneWide
			function = (Function) functionInternalIds.get(ObjectKeys.FUNCTION_ENABLE_PUBLIC_SHARING_INTERNALID);
			if (!function.isZoneWide()) {
				function.setZoneWide(true);
				getFunctionManager().updateFunction(function);
			}
		}

		if (!functionInternalIds.containsKey(ObjectKeys.FUNCTION_ENABLE_FORWARD_SHARING_INTERNALID)) {
			function = new Function();
			function.setZoneId(zoneConfig.getZoneId());
			function.setName(ObjectKeys.ROLE_ENABLE_SHARING_FORWARD);
			function.setScope(ObjectKeys.ROLE_TYPE_ZONE);
			function.setInternalId(ObjectKeys.FUNCTION_ENABLE_FORWARD_SHARING_INTERNALID);
			function.addOperation(WorkAreaOperation.ENABLE_SHARING_FORWARD);
			function.setZoneWide(true);
			//generate functionId
			getFunctionManager().addFunction(function);
			setGlobalWorkareaFunctionMembership(zoneConfig, function, new HashSet());
		}
		
		if (!functionInternalIds.containsKey(ObjectKeys.FUNCTION_ALLOW_SHARING_INTERNAL_INTERNALID)) {
			function = getFunctionManager().findFunctionByName(zoneConfig.getZoneId(), ObjectKeys.ROLE_ALLOW_SHARING_INTERNAL);
			if (function != null) {
				function.setInternalId(ObjectKeys.FUNCTION_ALLOW_SHARING_INTERNAL_INTERNALID);
				getFunctionManager().updateFunction(function);
			} else {
				function = new Function();
				function.setZoneId(zoneConfig.getZoneId());
				function.setName(ObjectKeys.ROLE_ALLOW_SHARING_INTERNAL);
				function.setScope(ObjectKeys.ROLE_TYPE_BINDER);
				function.setInternalId(ObjectKeys.FUNCTION_ALLOW_SHARING_INTERNAL_INTERNALID);
				function.addOperation(WorkAreaOperation.ALLOW_SHARING_INTERNAL);
				//generate functionId
				getFunctionManager().addFunction(function);
				setGlobalWorkareaFunctionMembership(zoneConfig, function, new HashSet());
			}
		}
		
		if (!functionInternalIds.containsKey(ObjectKeys.FUNCTION_ALLOW_SHARING_EXTERNAL_INTERNALID)) {
			function = new Function();
			function.setZoneId(zoneConfig.getZoneId());
			function.setName(ObjectKeys.ROLE_ALLOW_SHARING_EXTERNAL);
			function.setScope(ObjectKeys.ROLE_TYPE_BINDER);
			function.setInternalId(ObjectKeys.FUNCTION_ALLOW_SHARING_EXTERNAL_INTERNALID);
			function.addOperation(WorkAreaOperation.ALLOW_SHARING_EXTERNAL);
			//generate functionId
			getFunctionManager().addFunction(function);
			setGlobalWorkareaFunctionMembership(zoneConfig, function, new HashSet());
		}
		
		if (!functionInternalIds.containsKey(ObjectKeys.FUNCTION_ALLOW_SHARING_PUBLIC_INTERNALID)) {
			function = new Function();
			function.setZoneId(zoneConfig.getZoneId());
			function.setName(ObjectKeys.ROLE_ALLOW_SHARING_PUBLIC);
			function.setScope(ObjectKeys.ROLE_TYPE_BINDER);
			function.setInternalId(ObjectKeys.FUNCTION_ALLOW_SHARING_PUBLIC_INTERNALID);
			function.addOperation(WorkAreaOperation.ALLOW_SHARING_PUBLIC);
			//generate functionId
			getFunctionManager().addFunction(function);
			setGlobalWorkareaFunctionMembership(zoneConfig, function, new HashSet());
		}
		
		if (!functionInternalIds.containsKey(ObjectKeys.FUNCTION_ALLOW_SHARING_FORWARD_INTERNALID)) {
			function = new Function();
			function.setZoneId(zoneConfig.getZoneId());
			function.setName(ObjectKeys.ROLE_ALLOW_SHARING_FORWARD);
			function.setScope(ObjectKeys.ROLE_TYPE_BINDER);
			function.setInternalId(ObjectKeys.FUNCTION_ALLOW_SHARING_FORWARD_INTERNALID);
			function.addOperation(WorkAreaOperation.ALLOW_SHARING_FORWARD);
			//generate functionId
			getFunctionManager().addFunction(function);
			setGlobalWorkareaFunctionMembership(zoneConfig, function, new HashSet());
		}
		
		if (!functionInternalIds.containsKey(ObjectKeys.FUNCTION_ENABLE_SHARING_ALL_INTERNAL_INTERNALID)) {
			function = new Function();
			function.setZoneId(zoneConfig.getZoneId());
			function.setName(ObjectKeys.ROLE_ENABLE_SHARING_ALL_INTERNAL);
			function.setScope(ObjectKeys.ROLE_TYPE_ZONE);
			function.setInternalId(ObjectKeys.FUNCTION_ENABLE_SHARING_ALL_INTERNAL_INTERNALID);
			function.addOperation(WorkAreaOperation.ENABLE_SHARING_ALL_INTERNAL);
			function.setZoneWide(true);
			//generate functionId
			getFunctionManager().addFunction(function);
			setGlobalWorkareaFunctionMembership(zoneConfig, function, new HashSet());
		}
		
		if (!functionInternalIds.containsKey(ObjectKeys.FUNCTION_ENABLE_SHARING_ALL_EXTERNAL_INTERNALID)) {
			function = new Function();
			function.setZoneId(zoneConfig.getZoneId());
			function.setName(ObjectKeys.ROLE_ENABLE_SHARING_ALL_EXTERNAL);
			function.setScope(ObjectKeys.ROLE_TYPE_ZONE);
			function.setInternalId(ObjectKeys.FUNCTION_ENABLE_SHARING_ALL_EXTERNAL_INTERNALID);
			function.addOperation(WorkAreaOperation.ENABLE_SHARING_ALL_EXTERNAL);
			function.setZoneWide(true);
			//generate functionId
			getFunctionManager().addFunction(function);
			setGlobalWorkareaFunctionMembership(zoneConfig, function, new HashSet());
		}
		
		if (!functionInternalIds.containsKey(ObjectKeys.FUNCTION_ALLOW_ACCESS_NET_FOLDER_INTERNALID)) {
			function = new Function();
			function.setZoneId(zoneConfig.getZoneId());
			function.setName(ObjectKeys.ROLE_ALLOW_ACCESS_NET_FOLDER);
			function.setScope(ObjectKeys.ROLE_TYPE_BINDER);
			function.setInternalId(ObjectKeys.FUNCTION_ALLOW_ACCESS_NET_FOLDER_INTERNALID);
			function.addOperation(WorkAreaOperation.ALLOW_ACCESS_NET_FOLDER);
			//generate functionId
			getFunctionManager().addFunction(function);
			setGlobalWorkareaFunctionMembership(zoneConfig, function, new HashSet());
		}
		
		if (!functionInternalIds.containsKey(ObjectKeys.FUNCTION_VIEW_BINDER_TITLE_INTERNALID)) {
			addViewBinderTitleRole(zoneConfig.getZoneId());
		}
		
		// The next calls should be deleted. These file roles were never shipped
		//TODO remove these next lines before Filr ships
		for (Function f : functions) {
			if (f.getName().equals("__role.FilrFolderRead") || 
					f.getName().equals("__role.FilrFolderWrite") || 
					f.getName().equals("__role.FilrFolderOwner") ||
					f.getName().equals("__role.FilrFileRead") || 
					f.getName().equals("__role.FilrFileWrite") || 
					f.getName().equals("__role.FilrFileOwner") ||
					f.getName().equals("__role.enableChangingAccessControl")) {
				try{
					getFunctionManager().deleteFunction(f, true);
				} catch(Exception e) {
					logger.warn("Could not delete unused Filr roles");
				}
			}
		}
		functions = getFunctionManager().findFunctions(zoneConfig.getZoneId());
		functionInternalIds = new HashMap();
		for (int i = 0; i < functions.size(); i++) {
			function = (Function)functions.get(i);
			functionNames.put(function.getName(), function);
			if (function.getInternalId() != null) functionInternalIds.put(function.getInternalId(), function);
		}
		
		if (!functionInternalIds.containsKey(ObjectKeys.FUNCTION_FILR_VIEWER_INTERNALID)) {
			addFilrRoleViewer(zoneConfig.getZoneId());
		}
		if (!functionInternalIds.containsKey(ObjectKeys.FUNCTION_FILR_EDITOR_INTERNALID)) {
			addFilrRoleEditor(zoneConfig.getZoneId());
		}
		if (!functionInternalIds.containsKey(ObjectKeys.FUNCTION_FILR_CONTRIBUTOR_INTERNALID)) {
			addFilrRoleContributor(zoneConfig.getZoneId());
		} else {
			//Make sure this role is properly configured
			Function filrOwnerFunction = (Function) functionInternalIds.get(ObjectKeys.FUNCTION_FILR_CONTRIBUTOR_INTERNALID);
			if (!filrOwnerFunction.getOperations().contains(WorkAreaOperation.CREATE_FOLDERS)) {
				//This function needs to be fixed up
				filrOwnerFunction.addOperation(WorkAreaOperation.CREATE_FOLDERS);
				getFunctionManager().updateFunction(filrOwnerFunction);
			}
		}
	}
	
	private void setGlobalWorkareaFunctionMembership(ZoneConfig zoneConfig, Function function, Set<Long> memberIds) {
		WorkAreaFunctionMembership ms = new WorkAreaFunctionMembership();
		ms.setWorkAreaId(zoneConfig.getWorkAreaId());
		ms.setWorkAreaType(zoneConfig.getWorkAreaType());
		ms.setZoneId(zoneConfig.getZoneId());
		ms.setFunctionId(function.getId());
		ms.setMemberIds(memberIds);
		getCoreDao().save(ms);		
	}

	private void setApplicationGlobalRoles(ZoneConfig zoneConfig, ApplicationGroup applicationGroup,
			Function participantsRole) {
		Set members = new HashSet();
		members.add(applicationGroup.getId());
		WorkAreaFunctionMembership ms = new WorkAreaFunctionMembership();
		ms.setWorkAreaId(zoneConfig.getWorkAreaId());
		ms.setWorkAreaType(zoneConfig.getWorkAreaType());
		ms.setZoneId(zoneConfig.getZoneId());
		ms.setFunctionId(participantsRole.getId());
		ms.setMemberIds(members);
		getCoreDao().save(ms);
	}
	
	private Workspace addGuestWorkspace(User guest) {
		AccessControlManagerImpl.temporarilyDisableAccessCheckForThisThread();
		try {
			return getProfileModule().addUserWorkspace(guest, null);
		}
		finally {
			AccessControlManagerImpl.bringAccessCheckBackToNormalForThisThread();
		}
	}
	
	private void addVisitorRoleToGuestWorkspace(Workspace guestWs, Long zoneId) {
		User g = getProfileDao().getReservedUser(ObjectKeys.GUEST_USER_INTERNALID, zoneId);
		//Let guest be a visitor to the guest workspace
		Set<Long> members = new HashSet<Long>();
		members.add(g.getId());
		//Let guest be a visitor to the guest workspace
		Function visitorsRole = getFunctionManager().findFunctionByName(zoneId, ObjectKeys.ROLE_TITLE_VISITOR);
		WorkAreaFunctionMembership wafm = getWorkAreaFunctionMembershipManager().getWorkAreaFunctionMembership(zoneId, guestWs, visitorsRole.getId());
		if (wafm == null) {
			wafm = new WorkAreaFunctionMembership();
		}
		wafm.setFunctionId(visitorsRole.getId());
		wafm.setWorkAreaId(guestWs.getWorkAreaId());
		wafm.setWorkAreaType(guestWs.getWorkAreaType());
		wafm.setZoneId(zoneId);
		wafm.setMemberIds(members);
		getCoreDao().save(wafm);
	}

	/*
	 * Depending on the version that we're upgrading from, the admin
	 * may need to perform certain tasks related to the upgrade.  The
	 * following lets us control, version to version, which tasks need
	 * to be performed.  Initially, with Evergreen, they need to do
	 * everything.
	 */
	private void resetZoneUpgradeTasks(int oldVersion, Long superUId, Long topWSId) {
		// Based on the version, decide which admin tasks need to be
		// performed.
		boolean forceDefinitionsWarning = false;
		boolean forceIndexWarning       = false;
		boolean forceTemplatesWarning   = false;
		boolean forceVersionReset       = false;
		switch (oldVersion) {
		default:
			forceDefinitionsWarning =
			forceIndexWarning       =
			forceTemplatesWarning   =
			forceVersionReset       = true;
			
			break;
		}
		
		ProfileModule pm = getProfileModule();
		if (forceDefinitionsWarning) pm.setUserProperty(           superUId, ObjectKeys.USER_PROPERTY_UPGRADE_DEFINITIONS,     null);
		if (forceIndexWarning)       pm.setUserProperty(           superUId, ObjectKeys.USER_PROPERTY_UPGRADE_SEARCH_INDEX,    null);
		if (forceTemplatesWarning)   pm.setUserProperty(           superUId, ObjectKeys.USER_PROPERTY_UPGRADE_TEMPLATES,       null);
		if (forceVersionReset)       getBinderModule().setProperty(topWSId,  ObjectKeys.BINDER_PROPERTY_UPGRADE_VERSION,       null);
	}
	
	private List<Workspace> getTopWorkspacesFromEachZone() {
		List<Workspace> companies = getCoreDao().findCompanies();
		
		List<ZoneInfo> zoneInfos = getZoneInfos();
		
		List<Workspace> results = new ArrayList<Workspace>();
		
		for(Workspace top:companies) {
			boolean found = false;
			for(ZoneInfo zoneInfo:zoneInfos) {
				if(top.getZoneId().equals(zoneInfo.getZoneId())) {
					results.add(top);
					found = true;
					break;
				}
			}
			if(!found)
				logger.warn("Zone " + top.getZoneId() + " appears to be broken. Skipping it from loading.");
		}
		
		return results;
	}

	abstract protected void setupInitialOpenIDProviderList();
	
}
