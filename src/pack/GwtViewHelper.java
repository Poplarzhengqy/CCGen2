/**
 * Copyright (c) 1998-2013 Novell, Inc. and its licensors. All rights reserved.
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
 * (c) 1998-2013 Novell, Inc. All Rights Reserved.
 * 
 * Attribution Information:
 * Attribution Copyright Notice: Copyright (c) 1998-2013 Novell, Inc. All Rights Reserved.
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
package org.kablink.teaming.gwt.server.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import javax.portlet.PortletRequest;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.ImageIcon;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import org.kablink.teaming.BinderQuotaException;
import org.kablink.teaming.DataQuotaException;
import org.kablink.teaming.FileSizeLimitException;
import org.kablink.teaming.IllegalCharacterInNameException;
import org.kablink.teaming.ObjectKeys;
import org.kablink.teaming.comparator.StringComparator;
import org.kablink.teaming.context.request.RequestContextHolder;
import org.kablink.teaming.dao.CoreDao;
import org.kablink.teaming.dao.util.ShareItemSelectSpec;
import org.kablink.teaming.domain.Binder;
import org.kablink.teaming.domain.CustomAttribute;
import org.kablink.teaming.domain.DefinableEntity;
import org.kablink.teaming.domain.Definition;
import org.kablink.teaming.domain.EntityIdentifier;
import org.kablink.teaming.domain.EntityIdentifier.EntityType;
import org.kablink.teaming.domain.Description;
import org.kablink.teaming.domain.FileAttachment;
import org.kablink.teaming.domain.Folder;
import org.kablink.teaming.domain.FolderEntry;
import org.kablink.teaming.domain.Group;
import org.kablink.teaming.domain.GroupPrincipal;
import org.kablink.teaming.domain.HistoryStamp;
import org.kablink.teaming.domain.IdentityInfo;
import org.kablink.teaming.domain.Principal;
import org.kablink.teaming.domain.ReservedByAnotherUserException;
import org.kablink.teaming.domain.ResourceDriverConfig;
import org.kablink.teaming.domain.SeenMap;
import org.kablink.teaming.domain.ShareItem;
import org.kablink.teaming.domain.ShareItem.RecipientType;
import org.kablink.teaming.domain.TitleException;
import org.kablink.teaming.domain.User;
import org.kablink.teaming.domain.UserPrincipal;
import org.kablink.teaming.domain.UserProperties;
import org.kablink.teaming.domain.Workspace;
import org.kablink.teaming.domain.ZoneConfig;
import org.kablink.teaming.fi.connection.ResourceDriver;
import org.kablink.teaming.gwt.client.binderviews.folderdata.DescriptionHtml;
import org.kablink.teaming.gwt.client.binderviews.folderdata.FileBlob;
import org.kablink.teaming.gwt.client.binderviews.folderdata.FolderColumn;
import org.kablink.teaming.gwt.client.binderviews.folderdata.FolderRow;
import org.kablink.teaming.gwt.client.binderviews.folderdata.GuestInfo;
import org.kablink.teaming.gwt.client.GwtTeamingException;
import org.kablink.teaming.gwt.client.presence.GwtPresenceInfo;
import org.kablink.teaming.gwt.client.rpc.shared.AvatarInfoRpcResponseData;
import org.kablink.teaming.gwt.client.rpc.shared.BinderDescriptionRpcResponseData;
import org.kablink.teaming.gwt.client.rpc.shared.BooleanRpcResponseData;
import org.kablink.teaming.gwt.client.rpc.shared.ColumnWidthsRpcResponseData;
import org.kablink.teaming.gwt.client.rpc.shared.CreateFolderRpcResponseData;
import org.kablink.teaming.gwt.client.rpc.shared.EntryTypesRpcResponseData;
import org.kablink.teaming.gwt.client.rpc.shared.EntryTypesRpcResponseData.EntryType;
import org.kablink.teaming.gwt.client.rpc.shared.ErrorListRpcResponseData;
import org.kablink.teaming.gwt.client.rpc.shared.ErrorListRpcResponseData.ErrorInfo;
import org.kablink.teaming.gwt.client.rpc.shared.FileConflictsInfoRpcResponseData;
import org.kablink.teaming.gwt.client.rpc.shared.FileConflictsInfoRpcResponseData.DisplayInfo;
import org.kablink.teaming.gwt.client.rpc.shared.FolderColumnsRpcResponseData;
import org.kablink.teaming.gwt.client.rpc.shared.FolderDisplayDataRpcResponseData;
import org.kablink.teaming.gwt.client.rpc.shared.BinderFiltersRpcResponseData;
import org.kablink.teaming.gwt.client.rpc.shared.FolderRowsRpcResponseData;
import org.kablink.teaming.gwt.client.rpc.shared.JspHtmlRpcResponseData;
import org.kablink.teaming.gwt.client.rpc.shared.ProfileEntryInfoRpcResponseData;
import org.kablink.teaming.gwt.client.rpc.shared.SharedViewStateRpcResponseData;
import org.kablink.teaming.gwt.client.rpc.shared.StringRpcResponseData;
import org.kablink.teaming.gwt.client.rpc.shared.UserPropertiesRpcResponseData;
import org.kablink.teaming.gwt.client.rpc.shared.UserPropertiesRpcResponseData.AccountInfo;
import org.kablink.teaming.gwt.client.rpc.shared.UserPropertiesRpcResponseData.HomeInfo;
import org.kablink.teaming.gwt.client.rpc.shared.UserPropertiesRpcResponseData.NetFoldersInfo;
import org.kablink.teaming.gwt.client.rpc.shared.UserPropertiesRpcResponseData.QuotaInfo;
import org.kablink.teaming.gwt.client.rpc.shared.UserSharingRightsInfoRpcResponseData;
import org.kablink.teaming.gwt.client.rpc.shared.ValidateUploadsRpcResponseData;
import org.kablink.teaming.gwt.client.rpc.shared.VibeJspHtmlType;
import org.kablink.teaming.gwt.client.rpc.shared.ViewFolderEntryInfoRpcResponseData;
import org.kablink.teaming.gwt.client.rpc.shared.WhoHasAccessInfoRpcResponseData;
import org.kablink.teaming.gwt.client.rpc.shared.WhoHasAccessInfoRpcResponseData.AccessInfo;
import org.kablink.teaming.gwt.client.util.AssignmentInfo;
import org.kablink.teaming.gwt.client.util.AssignmentInfo.AssigneeType;
import org.kablink.teaming.gwt.client.util.BinderFilter;
import org.kablink.teaming.gwt.client.util.BinderIconSize;
import org.kablink.teaming.gwt.client.util.BinderInfo;
import org.kablink.teaming.gwt.client.util.BinderType;
import org.kablink.teaming.gwt.client.util.CollectionType;
import org.kablink.teaming.gwt.client.util.CommentsInfo;
import org.kablink.teaming.gwt.client.util.EmailAddressInfo;
import org.kablink.teaming.gwt.client.util.EntryEventInfo;
import org.kablink.teaming.gwt.client.util.EntityId;
import org.kablink.teaming.gwt.client.util.EntryLinkInfo;
import org.kablink.teaming.gwt.client.util.EntryTitleInfo;
import org.kablink.teaming.gwt.client.util.FolderEntryDetails;
import org.kablink.teaming.gwt.client.util.SharedViewState;
import org.kablink.teaming.gwt.client.util.TagInfo;
import org.kablink.teaming.gwt.client.util.UserType;
import org.kablink.teaming.gwt.client.util.FolderEntryDetails.UserInfo;
import org.kablink.teaming.gwt.client.util.FolderType;
import org.kablink.teaming.gwt.client.util.GwtClientHelper;
import org.kablink.teaming.gwt.client.util.ManageUsersState;
import org.kablink.teaming.gwt.client.util.ShareAccessInfo;
import org.kablink.teaming.gwt.client.util.ShareDateInfo;
import org.kablink.teaming.gwt.client.util.ShareExpirationInfo;
import org.kablink.teaming.gwt.client.util.ShareMessageInfo;
import org.kablink.teaming.gwt.client.util.ShareStringValue;
import org.kablink.teaming.gwt.server.util.GwtPerShareInfo.PerShareInfoComparator;
import org.kablink.teaming.gwt.server.util.GwtSharedMeItem.SharedMeEntriesMapComparator;
import org.kablink.teaming.gwt.client.util.PrincipalInfo;
import org.kablink.teaming.gwt.client.util.ShareRights;
import org.kablink.teaming.gwt.client.util.TaskFolderInfo;
import org.kablink.teaming.gwt.client.util.UploadInfo;
import org.kablink.teaming.gwt.client.util.ViewFileInfo;
import org.kablink.teaming.gwt.client.util.ViewFolderEntryInfo;
import org.kablink.teaming.gwt.client.util.ViewType;
import org.kablink.teaming.gwt.client.util.WorkspaceType;
import org.kablink.teaming.gwt.client.util.ViewInfo;
import org.kablink.teaming.module.admin.AdminModule;
import org.kablink.teaming.module.admin.AdminModule.AdminOperation;
import org.kablink.teaming.module.binder.BinderModule;
import org.kablink.teaming.module.binder.BinderModule.BinderOperation;
import org.kablink.teaming.module.binder.impl.WriteEntryDataException;
import org.kablink.teaming.module.file.WriteFilesException;
import org.kablink.teaming.module.folder.FilesLockedByOtherUsersException;
import org.kablink.teaming.module.folder.FolderModule;
import org.kablink.teaming.module.folder.FolderModule.FolderOperation;
import org.kablink.teaming.module.license.LicenseChecker;
import org.kablink.teaming.module.profile.ProfileModule;
import org.kablink.teaming.module.profile.ProfileModule.ProfileOperation;
import org.kablink.teaming.module.report.ReportModule;
import org.kablink.teaming.module.shared.EmptyInputData;
import org.kablink.teaming.module.shared.FolderUtils;
import org.kablink.teaming.module.shared.InputDataAccessor;
import org.kablink.teaming.module.shared.MapInputData;
import org.kablink.teaming.module.sharing.SharingModule;
import org.kablink.teaming.portlet.binder.AccessControlController;
import org.kablink.teaming.portletadapter.AdaptedPortletURL;
import org.kablink.teaming.portletadapter.portlet.RenderRequestImpl;
import org.kablink.teaming.portletadapter.portlet.RenderResponseImpl;
import org.kablink.teaming.portletadapter.support.AdaptedPortlets;
import org.kablink.teaming.portletadapter.support.KeyNames;
import org.kablink.teaming.portletadapter.support.PortletInfo;
import org.kablink.teaming.runas.RunasCallback;
import org.kablink.teaming.runas.RunasTemplate;
import org.kablink.teaming.search.SearchUtils;
import org.kablink.teaming.security.AccessControlException;
import org.kablink.teaming.security.function.WorkArea;
import org.kablink.teaming.security.function.WorkAreaOperation;
import org.kablink.teaming.security.runwith.RunWithCallback;
import org.kablink.teaming.security.runwith.RunWithTemplate;
import org.kablink.teaming.ssfs.util.SsfsUtil;
import org.kablink.teaming.task.TaskHelper;
import org.kablink.teaming.task.TaskHelper.FilterType;
import org.kablink.teaming.util.AllModulesInjected;
import org.kablink.teaming.util.DateComparer;
import org.kablink.teaming.util.FileIconsHelper;
import org.kablink.teaming.util.IconSize;
import org.kablink.teaming.util.LongIdUtil;
import org.kablink.teaming.util.NLT;
import org.kablink.teaming.util.ResolveIds;
import org.kablink.teaming.util.SPropsUtil;
import org.kablink.teaming.util.SimpleProfiler;
import org.kablink.teaming.util.SpringContextUtil;
import org.kablink.teaming.util.TempFileUtil;
import org.kablink.teaming.util.Utils;
import org.kablink.teaming.web.WebKeys;
import org.kablink.teaming.web.tree.DomTreeBuilder;
import org.kablink.teaming.web.tree.SearchTreeHelper;
import org.kablink.teaming.web.tree.WsDomTreeBuilder;
import org.kablink.teaming.web.util.BinderHelper;
import org.kablink.teaming.web.util.DashboardHelper;
import org.kablink.teaming.web.util.DefinitionHelper;
import org.kablink.teaming.web.util.EventHelper;
import org.kablink.teaming.web.util.GwtUIHelper;
import org.kablink.teaming.web.util.ListFolderHelper;
import org.kablink.teaming.web.util.ListUtil;
import org.kablink.teaming.web.util.MarkupUtil;
import org.kablink.teaming.web.util.MiscUtil;
import org.kablink.teaming.web.util.PermaLinkUtil;
import org.kablink.teaming.web.util.Toolbar;
import org.kablink.teaming.web.util.TrashHelper;
import org.kablink.teaming.web.util.WebHelper;
import org.kablink.teaming.web.util.ListFolderHelper.ModeType;
import org.kablink.teaming.web.util.TrashHelper.TrashEntry;
import org.kablink.teaming.web.util.TrashHelper.TrashResponse;
import org.kablink.util.StringUtil;
import org.kablink.util.Validator;
import org.kablink.util.search.Constants;
import org.kablink.util.search.Criteria;
import org.kablink.util.search.Order;
import org.springframework.util.FileCopyUtils;

import static org.kablink.util.search.Restrictions.like;

/**
 * Helper methods for the GWT binder views.
 *
 * @author drfoster@novell.com
 */
public class GwtViewHelper {
	protected static Log m_logger = LogFactory.getLog(GwtViewHelper.class);
	
	// Attribute names used for items related to milestones.
	public static final String RESPONSIBLE_GROUPS_MILESTONE_ENTRY_ATTRIBUTE_NAME	= "responsible_groups";
	public static final String RESPONSIBLE_MILESTONE_ENTRY_ATTRIBUTE_NAME			= "responsible";
	public static final String RESPONSIBLE_TEAMS_MILESTONE_ENTRY_ATTRIBUTE_NAME		= "responsible_teams";

	// Attribute names used to store things in the session cache.
	private static final String CACHED_UPLOAD_FILE				= "uploadFile";
	private static final String CACHED_VIEW_PINNED_ENTRIES_BASE = "viewPinnedEntries_";
	private static final String CACHED_VIEW_SHARED_FILES_BASE	= "viewSharedFiles_";
	
	// Base for the keys used to store shared view state in the session
	// cache.
	private static final String CACHED_SHARED_VIEW_SHOW_HIDDEN_BASE		= "sharedViewShowHidden_";
	private static final String CACHED_SHARED_VIEW_SHOW_NON_HIDDEN_BASE	= "sharedViewShowNonHidden_";

	// Used in various file size calculations, ...
	private final static long MEGABYTES = (1024l * 1024l);
	
	/*
	 * Inner class used to compare two AccessInfo's.
	 * 
	 * Two AccessInfo's are sorted by their name.
	 */
	private static class AccessInfoComparator implements Comparator<AccessInfo> {
		/**
		 * Constructor method.
		 */
		public AccessInfoComparator() {
			// Initialize the super class.
			super();
		}

		/**
		 * Compares two AccessInfo objects.
		 * 
		 * Implements the Comparator.compare() method.
		 * 
		 * @param ai1
		 * @param ai2
		 * 
		 * @return
		 */
		@Override
		public int compare(AccessInfo ai1, AccessInfo ai2) {
			return MiscUtil.safeSColatedCompare(ai1.getName(), ai2.getName());
		}
	}

	/*
	 * Inner class used to track the target binder's for entries and
	 * binders given the ID of an initial target binder.
	 * 
	 * The logic here is appropriate for copy/move operations where
	 * folder and/or entries are binder copied/moved to a target.  If
	 * that target is a user's 'My Files Storage' folder, the entries
	 * should be targeted there, but folders should be targeted to the
	 * containing workspace.  Conversely, if the target is a user's
	 * workspace, the folders should be targeted there, but entries
	 * should be targeted to the contained 'My Files Storage' folder.
	 */
	private static class TargetIds {
		private Long	m_binderTargetId;	// Target binder ID for binders being copied/moved.
		private Long	m_entryTargetId;	// Target binder ID for entries being copied/moved.

		/**
		 * Constructor method.
		 * 
		 * @param bs
		 * @param targetBinderId
		 */
		public TargetIds(AllModulesInjected bs, Long targetBinderId) {
			// Initialize the super class...
			super();

			// ...and store the initial target binder as both the
			// ...binder and entry target.
			setBinderTargetId(targetBinderId);
			setEntryTargetId( targetBinderId);
			
			// Is the initial target binder a 'My Files Storage'
			// folder?
			Binder targetBinder = bs.getBinderModule().getBinderWithoutAccessCheck(targetBinderId);
			if (BinderHelper.isBinderMyFilesStorage(targetBinder)) {
				// Yes! Then the binder target should be that folder's
				// parent workspace.
				setBinderTargetId(targetBinder.getParentBinder().getId());
			}
			
			// No, the initial target binder isn't a 'My FIles Storage'
			// folder.  Is it a user's workspace?
			else if (BinderHelper.isBinderUserWorkspace(targetBinder)) {
				// Yes!  Can we find any 'My Files Storage' folders
				// within that?
				List<Long> mfIDs = SearchUtils.getMyFilesFolderIds(bs, targetBinderId);
				if (MiscUtil.hasItems(mfIDs)) {
					// Yes!  Use the first one as the target for
					// entries.
					setEntryTargetId(mfIDs.get(0));
				}
			}
		}

		/**
		 * Get'er methods.
		 * 
		 * @return
		 */
		public Long getBinderTargetId() {return m_binderTargetId;}
		public Long getEntryTargetId()  {return m_entryTargetId; }
		
		/**
		 * Set'er methods.
		 * 
		 * @param
		 */
		public void setBinderTargetId(Long binderTargetId) {m_binderTargetId = binderTargetId;}
		public void setEntryTargetId( Long entryTargetId)  {m_entryTargetId  = entryTargetId; }
	}
	
	/*
	 * Class constructor that prevents this class from being
	 * instantiated.
	 */
	private GwtViewHelper() {
		// Nothing to do.
	}

	/**
	 * Aborts any file upload in progress.
	 * 
	 * @param bs
	 * @param request
	 * @param folderInfo
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static BooleanRpcResponseData abortFileUpload(AllModulesInjected bs, HttpServletRequest request, BinderInfo folderInfo) throws GwtTeamingException {
		try {
			// Do we have an upload filename cached in the session?
			HttpSession session = WebHelper.getRequiredSession(request);
			String fileName = ((String) session.getAttribute(CACHED_UPLOAD_FILE));
			if (MiscUtil.hasString(fileName)) {
				// Yes!  Remove it...
				session.removeAttribute(CACHED_UPLOAD_FILE);
				try {
					// ...and if we can access the temporary file for
					// ...it...
					File tempFile = TempFileUtil.getTempFileByName(fileName);
					if (null != tempFile) {
						// ...delete that.
						tempFile.delete();
					}
				}
				
				catch (Throwable t) {
					// Ignore.
				}
			}
			return new BooleanRpcResponseData(true);
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.abortFileUpload( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}

	/*
	 * If there's an attribute value, constructs a ProfileAttribute for
	 * it and adds it to a ProfileEntryInfoRpcResponseData object.
	 */
	private static void addProfileAttribute(ProfileEntryInfoRpcResponseData paData, String attributeName, String attributeValue) {
		if (MiscUtil.hasString(attributeValue)) {
			paData.addProfileAttribute(attributeName, new ProfileEntryInfoRpcResponseData.ProfileAttribute(NLT.get("__" + attributeName), attributeValue));
		}
	}

	/**
	 * Creates a new folder in the given binder based on a folder
	 * definition ID and the name of the new folder.
	 * 
	 * @param bs
	 * @param request
	 * @param binderId
	 * @param folderTemplateId
	 * @param folderName
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static CreateFolderRpcResponseData addNewFolder(AllModulesInjected bs, HttpServletRequest request, Long binderId, Long folderTemplateId, String folderName) throws GwtTeamingException {
		try {
			// Allocate a response we can return.
			CreateFolderRpcResponseData reply = new CreateFolderRpcResponseData(new ArrayList<ErrorInfo>());

			try {
				// Can we create the new folder?
				final BinderModule bm = bs.getBinderModule();
				final Long newId = bs.getTemplateModule().addBinder(folderTemplateId, binderId, folderName, null).getId();
				if (bm.getBinder(newId) != null) {
					
					reply.setFolderId(newId);
					reply.setFolderName(folderName);
					
					RunWithTemplate.runWith(new RunWithCallback() {
						@Override
						public Object runWith() {
							bm.setTeamMembershipInherited(newId, true);			
							return null;
						}
					}, new WorkAreaOperation[]{WorkAreaOperation.BINDER_ADMINISTRATION}, null);
				}
			}

			catch (Exception e) {
				// No!  Add an error  to the error list.
				String messageKey;
				if      (e instanceof AccessControlException)          messageKey = "addNewFolderError.AccssControlException";
				else if (e instanceof IllegalCharacterInNameException) messageKey = "addNewFolderError.IllegalCharacterInNameException";
				else if (e instanceof WriteFilesException)             messageKey = "addNewFolderError.WriteFilesException";
				else if (e instanceof TitleException)		           messageKey = "addNewFolderError.TitleException";
				else                                                   messageKey = "addNewFolderError.OtherException";
				reply.addError(NLT.get(messageKey, new String[]{folderName}));
			}

			// If we get here, reply refers to an
			// ErrorListRpcResponseData containing any errors we
			// encountered.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.addNewFolder( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}

	/*
	 * Adds a quick filter, if present, to a Criteria object.
	 */
	private static void addQuickFilterToCriteria(String quickFilter, Criteria crit) {
		// Do we have a quick filter?
		if (null != quickFilter) {
			quickFilter = quickFilter.trim();
			if (0 < quickFilter.length()) {
				// Yes!  Add it to the Criteria.
				if (!(quickFilter.endsWith("*"))) {
					quickFilter += "*";
				}
				crit.add(like(Constants.TITLE_FIELD, quickFilter));
			}
		}
	}
	
	/*
	 * Returns an entry map that represents no entries available.
	 */
	@SuppressWarnings("unchecked")
	private static Map buildEmptyEntryMap() {
		Map reply = new HashMap();
		reply.put(ObjectKeys.SEARCH_ENTRIES,     new ArrayList<Map>());
		reply.put(ObjectKeys.SEARCH_COUNT_TOTAL, new Integer(0)      );
		return reply;
	}
	
	/*
	 * Constructs and returns a UserInfo object using a HistoryStamp.
	 */
	private static UserInfo buildFolderEntryUser(AllModulesInjected bs, HttpServletRequest request, HistoryStamp hs) {
		// If we don't have a HistoryStamp...
		if (null == hs) {
			// ...there is no user.  Return null.
			return null;
		}
		
		// Create the UserInfo to return...
		UserInfo reply = new UserInfo();

		// ...set the Date when the user performed the action...
		reply.setDate(GwtServerHelper.getDateTimeString(hs.getDate(), DateFormat.MEDIUM, DateFormat.SHORT));
		
		// ...and set the PrincipalInfo about how performed the action.
		reply.setPrincipalInfo(getPIFromPId(bs, request, hs.getPrincipal().getId()));
		
		// If we get here, reply refers to the UserInfo that describes
		// the user from the given HistoryStamp.  Return it.
		return reply;
	}

	/*
	 * Returns a JSP file name, ensuring it has a .jsp extension.
	 */
	private static String buildJspName(String baseJsp) {
		// If we weren't given base name...
		if (null == baseJsp) {
			// ...return null.
			return null;
		}

		// If the base name only contains white space...
		baseJsp = baseJsp.trim();
		if (0 == baseJsp.length()) {
			// ...return a 0 length string.
			return baseJsp;
		}

		// If the trimmed base name already ends with .jsp...
		if (baseJsp.toLowerCase().endsWith(".jsp")) {
			// ...simply return it.
			return baseJsp;
		}

		// Otherwise, return the base name with .jsp appended.
		return (baseJsp + ".jsp");
	}
	
	/*
	 * Builds a model Map for running a report JSP.
	 */
	private static Map<String, Object> buildReportModelMap(Map<String, Object> baseModel) {
		// Construct the base Map...
		Map<String, Object> reply = new HashMap<String, Object>();
		if (null != baseModel) {
			reply.putAll(baseModel);
		}
		reply.put(WebKeys.URL_GWT_REPORT, Boolean.TRUE);

		// ...and add the default start/end dates.
		GregorianCalendar cal = new GregorianCalendar();
		cal.add(Calendar.MONTH, -1);
		Date startDate = cal.getTime();
		Date endDate = new Date();
		reply.put(WebKeys.REPORT_START_DATE, startDate);
		reply.put(WebKeys.REPORT_END_DATE, endDate);

		// If we get here, reply refers to the model Map for running a
		// report.  Return it.
		return reply;
	}
	
	/*
	 * Returns an entry map that represents a List<GwtSharedMeItem>.
	 */
	@SuppressWarnings("unchecked")
	private static Map buildSearchMapFromSharedMeList(AllModulesInjected bs, List<GwtSharedMeItem> shareItems, boolean sortDescend, String sortBy) {
		List<Map> searchEntries = new ArrayList<Map>();
		for (GwtSharedMeItem si:  shareItems) {
			// Create an entry Map for this GwtSharedMeItem.
			Map entryMap = new HashMap();
			searchEntries.add(entryMap);

			// Are we processing an entry?
			DefinableEntity	entity = si.getEntity();
			Description entityDesc = entity.getDescription();
			if (null != entityDesc) {
				entryMap.put(Constants.DESC_FIELD,        entityDesc.getText());
				entryMap.put(Constants.DESC_FORMAT_FIELD, entityDesc.getFormat());
			}
			entryMap.put(Constants.DOCID_FIELD,  String.valueOf(entity.getId()));
			entryMap.put(Constants.ENTITY_FIELD, entity.getEntityType().name());
			entryMap.put(Constants.TITLE_FIELD,  entity.getTitle());
			String binderIdField;
			if (entity instanceof FolderEntry) {
				// Yes!  Can we find a filename for this entity?
				String fName = GwtServerHelper.getFileEntrysFilename(bs, ((FolderEntry) entity), false);
	        	if (MiscUtil.hasString(fName)) {
	        		// Yes!  Store it in the Map.
					entryMap.put(Constants.FILENAME_FIELD, fName);
	        	}

		        // Store the total replies, last activity and
		        // modification date for this entry in the Map.
		        FolderEntry fe = ((FolderEntry) entity);
		        entryMap.put(Constants.TOTALREPLYCOUNT_FIELD,   String.valueOf(fe.getTotalReplyCount()));
		        entryMap.put(Constants.LASTACTIVITY_FIELD,      fe.getLastActivity()                   );
		        entryMap.put(Constants.MODIFICATION_DATE_FIELD, fe.getModification()                   );
				
				// Store the entry's parent binder's ID in the Map.
				binderIdField = Constants.BINDER_ID_FIELD;
			}
			
			else {
				// No, we aren't processing an entry!  It must be a
				// binder!  Store the binder's path and modification
				// date...
				Binder binder = ((Binder) entity);
				entryMap.put(Constants.ENTITY_PATH,             binder.getPathName()    );
		        entryMap.put(Constants.MODIFICATION_DATE_FIELD, binder.getModification());
		        
				// ...and store its parent's ID in the Map.
				binderIdField = Constants.BINDERS_PARENT_ID_FIELD;
			}
			
			// Store the binder ID in the Map.
			entryMap.put(
				binderIdField,
				String.valueOf(entity.getParentBinder().getId()));

			// If the entity has a family...
			String family = si.getEntityFamily();
			if (MiscUtil.hasString(family)) {
				// ...store it in the entry map.
				entryMap.put(Constants.FAMILY_FIELD, family);
			}
		}
		
		// Finally, apply the sorting to the search entries...
		searchEntries = postProcessSharedMeMap(
			bs,
			searchEntries,
			shareItems,
			sortDescend,
			sortBy);
		
		// ...and use them to construct the search results Map and
		// ...return that.
		Map reply = new HashMap();
		reply.put(ObjectKeys.SEARCH_ENTRIES,                 searchEntries        );
		reply.put(ObjectKeys.SEARCH_COUNT_TOTAL, new Integer(searchEntries.size()));
		return reply;
	}

	/*
	 * Constructs a ViewFileInfo from a FileAttachment known to have a
	 * filename.
	 */
	private static ViewFileInfo buildViewFileInfo(HttpServletRequest request, FolderEntry fe, FileAttachment fa) {
		// Is the entry a file entry with an attachment known to have a
		// filename?
		ViewFileInfo reply = null;
		if (null != fa) {
			// Yes!  Do we support viewing that type of file as HTML?
			String fName = fa.getFileItem().getName();
			boolean supportsViewAsHtml = SsfsUtil.supportsViewAsHtml(fName);
			if (!supportsViewAsHtml) {
				int pPos = fName.lastIndexOf('.');
				if (0 < pPos) {
					supportsViewAsHtml = fName.substring(pPos).toLowerCase().equals(".pdf");
				}
			}
    		if (supportsViewAsHtml) {
				try {
	        		// Yes!  Generate a ViewFileInfo for it.
					reply = new ViewFileInfo();
					reply.setFileId(     fa.getId());
					reply.setEntityId(   new EntityId(fe.getParentFolder().getId(), fe.getId(), EntityId.FOLDER_ENTRY));
					reply.setFileTime(   String.valueOf(fa.getModification().getDate().getTime()));
					reply.setViewFileUrl(GwtServerHelper.getViewFileUrl(request, reply));
				}
				catch (GwtTeamingException ex) {
					reply = null;
				}
    		}
    	}
		
		// If we get here, reply refers to the ViewFileInfo for the
		// attachment if it can be viewed as HTML or null.  Return it.
		return reply;
	}
	
	/*
	 * Constructs and returns a ViewFolderEntryInfo that wraps the
	 * given binderId/entryId pair.
	 */
	@SuppressWarnings("unchecked")
	private static ViewFolderEntryInfo buildViewFolderEntryInfo(AllModulesInjected bs, HttpServletRequest request, Long binderId, Long entryId) {
		SimpleProfiler.start("GwtViewHelper.buildViewFolderEntryInfo()");
		try {
			// Create the ViewFolderEntryInfo to return...
			ViewFolderEntryInfo	reply  = new ViewFolderEntryInfo(binderId, entryId);
			
			// ...set the user's selected view style... 
			String viewStyle = GwtServerHelper.getPersonalPreferences(bs, request).getDisplayStyle();
			if (!(MiscUtil.hasString(viewStyle))) {
				viewStyle = ObjectKeys.USER_DISPLAY_STYLE_NEWPAGE;
			}
			reply.setViewStyle(viewStyle);
	
			// ...if the user has a position for the dialog saved...
			UserProperties	userProperties = bs.getProfileModule().getUserProperties(GwtServerHelper.getCurrentUserId());
			if (null != userProperties) {
				Map upMap = userProperties.getProperties();
				if (null != upMap) {
					String packedPosition = ((String) upMap.get(ObjectKeys.USER_PROPERTY_FOLDER_ENTRY_DLG_POSITION));
					if (MiscUtil.hasString(packedPosition)) {
						String[] position = StringUtil.unpack(packedPosition);
						if ((null != position) && (4 == position.length)) {
							// ...set it into the reply...
							reply.setX( Integer.parseInt(position[0]));
							reply.setY( Integer.parseInt(position[1]));
							reply.setCX(-1);	// Integer.parseInt(position[2]));		// Saved height and width are ignored until we support...
							reply.setCY(-1);	// Integer.parseInt(position[3]));		// ...the user actually adjusting the size of the dialog.
						}
					}
				}
			}
			
			// ...set the entry's family... 
			FolderEntry fe    = bs.getFolderModule().getEntry(binderId, entryId);
			FolderEntry feTop = fe.getTopEntry();
			if (null == feTop) {
				feTop = fe;
			}
			reply.setFamily(GwtServerHelper.getFolderEntityFamily(bs, feTop));
	
			// ...set the entry's title... 
			String feTitle = fe.getTitle();
			if (!(MiscUtil.hasString(feTitle))) {
				feTitle = ("--" + NLT.get("entry.noTitle") + "--");
			}
			reply.setTitle(feTitle);
			
			// If we get here, reply refers to the ViewFolderEntryInfo
			// for the user to view the entry.  Return it.
			return reply;
		}
		
		finally {
			SimpleProfiler.stop("GwtViewHelper.buildViewFolderEntryInfo()");
		}
	}

	/*
	 * Extracts the ID's of the entry contributors and adds them to the
	 * contributor ID's list if they're not already there. 
	 */
	@SuppressWarnings("unchecked")
	private static void collectContributorIds(Map entryMap, List<Long> contributorIds) {
		ListUtil.addLongToListLongIfUnique(contributorIds, getLongFromMap(entryMap, Constants.CREATORID_FIELD)     );
		ListUtil.addLongToListLongIfUnique(contributorIds, getLongFromMap(entryMap, Constants.MODIFICATIONID_FIELD));
	}
	
	/*
	 * When initially built, the AssignmentInfo's in the folder rows
	 * only contain the assignee IDs.  We need to complete them with
	 * each assignee's title, ...
	 */
	private static void completeAIs(AllModulesInjected bs, HttpServletRequest request, List<FolderRow> folderRows) {
		// If we don't have any FolderRows's to complete...
		if (!(MiscUtil.hasItems(folderRows))) {
			// ..bail.
			return;
		}

		// Allocate List<Long>'s to track the assignees that need to be
		// completed.
		List<Long> principalIds = new ArrayList<Long>();
		List<Long> teamIds      = new ArrayList<Long>();

		// Scan the List<FolderRow>'s.
		for (FolderRow fr:  folderRows) {
			// Scan this FolderRow's individual assignees tracking each
			// unique ID.
			for (AssignmentInfo ai:  getAIListFromFR(fr, TaskHelper.ASSIGNMENT_TASK_ENTRY_ATTRIBUTE_NAME)) {
				ListUtil.addLongToListLongIfUnique((ai.getAssigneeType().isTeam() ? teamIds : principalIds), ai.getId());
			}
			for (AssignmentInfo ai:  getAIListFromFR(fr, EventHelper.ASSIGNMENT_CALENDAR_ENTRY_ATTRIBUTE_NAME)) {
				ListUtil.addLongToListLongIfUnique((ai.getAssigneeType().isTeam() ? teamIds : principalIds), ai.getId());
			}
			for (AssignmentInfo ai:  getAIListFromFR(fr, RESPONSIBLE_MILESTONE_ENTRY_ATTRIBUTE_NAME)) {
				ListUtil.addLongToListLongIfUnique((ai.getAssigneeType().isTeam() ? teamIds : principalIds), ai.getId());
			}
			
			// Scan this FolderRow's group assignees tracking each
			// unique ID.
			for (AssignmentInfo ai:  getAIListFromFR(fr, TaskHelper.ASSIGNMENT_GROUPS_TASK_ENTRY_ATTRIBUTE_NAME)) {
				ListUtil.addLongToListLongIfUnique(principalIds, ai.getId());
			}
			for (AssignmentInfo ai:  getAIListFromFR(fr, EventHelper.ASSIGNMENT_GROUPS_CALENDAR_ENTRY_ATTRIBUTE_NAME)) {
				ListUtil.addLongToListLongIfUnique(principalIds, ai.getId());
			}
			for (AssignmentInfo ai:  getAIListFromFR(fr, RESPONSIBLE_GROUPS_MILESTONE_ENTRY_ATTRIBUTE_NAME)) {
				ListUtil.addLongToListLongIfUnique(principalIds, ai.getId());
			}
			
			// Scan this FolderRow's team assignees tracking each
			// unique ID.
			for (AssignmentInfo ai:  getAIListFromFR(fr, TaskHelper.ASSIGNMENT_TEAMS_TASK_ENTRY_ATTRIBUTE_NAME)) {
				ListUtil.addLongToListLongIfUnique(teamIds, ai.getId());
			}
			for (AssignmentInfo ai:  getAIListFromFR(fr, EventHelper.ASSIGNMENT_TEAMS_CALENDAR_ENTRY_ATTRIBUTE_NAME)) {
				ListUtil.addLongToListLongIfUnique(teamIds, ai.getId());
			}
			for (AssignmentInfo ai:  getAIListFromFR(fr, RESPONSIBLE_TEAMS_MILESTONE_ENTRY_ATTRIBUTE_NAME)) {
				ListUtil.addLongToListLongIfUnique(teamIds, ai.getId());
			}
			
			// Scan this FolderRow's shared by/with's tracking each unique
			// ID.
			for (AssignmentInfo ai:  getAIListFromFR(fr, FolderColumn.COLUMN_SHARE_SHARED_BY)) {
				ListUtil.addLongToListLongIfUnique(principalIds, ai.getId());
			}
			for (AssignmentInfo ai:  getAIListFromFR(fr, FolderColumn.COLUMN_SHARE_SHARED_WITH)) {
				ListUtil.addLongToListLongIfUnique(principalIds, ai.getId());
			}
		}

		// If we don't have any assignees to complete...
		boolean hasPrincipals = (!(principalIds.isEmpty()));
		boolean hasTeams      = (!(teamIds.isEmpty()));		
		if ((!hasPrincipals) && (!hasTeams)) {
			// ...bail.
			return;
		}

		// Construct Maps, mapping the IDs to their titles, membership
		// counts, ...
		Map<Long, String>			avatarUrls        = new HashMap<Long, String>();
		Map<Long, String>			principalEMAs     = new HashMap<Long, String>();
		Map<Long, String>			principalTitles   = new HashMap<Long, String>();
		Map<Long, Integer>			groupCounts       = new HashMap<Long, Integer>();
		Map<Long, GwtPresenceInfo>	userPresence      = new HashMap<Long, GwtPresenceInfo>();
		Map<Long, Long>				presenceUserWSIds = new HashMap<Long, Long>();
		Map<Long, String>			teamTitles        = new HashMap<Long, String>();
		Map<Long, Integer>			teamCounts        = new HashMap<Long, Integer>();
		GwtEventHelper.readEventStuffFromDB(
			// Uses these...
			bs,
			request,
			principalIds,
			teamIds,

			// ...to complete these.
			principalEMAs,
			principalTitles,
			groupCounts,
			userPresence,
			presenceUserWSIds,
			
			teamTitles,
			teamCounts,
			
			avatarUrls);

		// Scan the List<FolderRow>'s again...
		for (FolderRow fr:  folderRows) {
			// ...this time, fixing the assignee lists.
			fixupAIs(     getAIListFromFR(fr, TaskHelper.ASSIGNMENT_TASK_ENTRY_ATTRIBUTE_NAME),             principalTitles, userPresence, presenceUserWSIds, avatarUrls);
			fixupAIGroups(getAIListFromFR(fr, TaskHelper.ASSIGNMENT_TASK_ENTRY_ATTRIBUTE_NAME),             principalTitles, groupCounts                                );
			fixupAITeams( getAIListFromFR(fr, TaskHelper.ASSIGNMENT_TASK_ENTRY_ATTRIBUTE_NAME),             teamTitles,      teamCounts                                 );
			fixupAIs(     getAIListFromFR(fr, EventHelper.ASSIGNMENT_CALENDAR_ENTRY_ATTRIBUTE_NAME),        principalTitles, userPresence, presenceUserWSIds, avatarUrls);
			fixupAIGroups(getAIListFromFR(fr, EventHelper.ASSIGNMENT_CALENDAR_ENTRY_ATTRIBUTE_NAME),        principalTitles, groupCounts                                );
			fixupAITeams( getAIListFromFR(fr, EventHelper.ASSIGNMENT_CALENDAR_ENTRY_ATTRIBUTE_NAME),        teamTitles,      teamCounts                                 );
			fixupAIs(     getAIListFromFR(fr, RESPONSIBLE_MILESTONE_ENTRY_ATTRIBUTE_NAME),                  principalTitles, userPresence, presenceUserWSIds, avatarUrls);
			fixupAIGroups(getAIListFromFR(fr, RESPONSIBLE_MILESTONE_ENTRY_ATTRIBUTE_NAME),                  principalTitles, groupCounts                                );
			fixupAITeams( getAIListFromFR(fr, RESPONSIBLE_MILESTONE_ENTRY_ATTRIBUTE_NAME),                  teamTitles,      teamCounts                                 );
			
			fixupAIGroups(getAIListFromFR(fr, TaskHelper.ASSIGNMENT_GROUPS_TASK_ENTRY_ATTRIBUTE_NAME),      principalTitles, groupCounts                                );
			fixupAIGroups(getAIListFromFR(fr, EventHelper.ASSIGNMENT_GROUPS_CALENDAR_ENTRY_ATTRIBUTE_NAME), principalTitles, groupCounts                                );
			fixupAIGroups(getAIListFromFR(fr, RESPONSIBLE_GROUPS_MILESTONE_ENTRY_ATTRIBUTE_NAME),           principalTitles, groupCounts                                );
			
			fixupAITeams( getAIListFromFR(fr, TaskHelper.ASSIGNMENT_TEAMS_TASK_ENTRY_ATTRIBUTE_NAME),       teamTitles,      teamCounts                                 );
			fixupAITeams( getAIListFromFR(fr, EventHelper.ASSIGNMENT_TEAMS_CALENDAR_ENTRY_ATTRIBUTE_NAME),  teamTitles,      teamCounts                                 );
			fixupAITeams( getAIListFromFR(fr, RESPONSIBLE_TEAMS_MILESTONE_ENTRY_ATTRIBUTE_NAME),            teamTitles,      teamCounts                                 );
			
			fixupAIs(      getAIListFromFR(fr, FolderColumn.COLUMN_SHARE_SHARED_BY),                         principalTitles, userPresence, presenceUserWSIds, avatarUrls);
			fixupAIs(      getAIListFromFR(fr, FolderColumn.COLUMN_SHARE_SHARED_WITH),                       principalTitles, userPresence, presenceUserWSIds, avatarUrls);
			fixupAIGroups( getAIListFromFR(fr, FolderColumn.COLUMN_SHARE_SHARED_WITH),                       principalTitles, groupCounts                                );
			fixupAIPublics(getAIListFromFR(fr, FolderColumn.COLUMN_SHARE_SHARED_BY)                                                                                      );
			fixupAIPublics(getAIListFromFR(fr, FolderColumn.COLUMN_SHARE_SHARED_WITH)                                                                                    );
			fixupAITeams(  getAIListFromFR(fr, FolderColumn.COLUMN_SHARE_SHARED_WITH),                       teamTitles,      teamCounts                                 );
		}		

		// Finally, one last scan through the List<FolderRow>'s...
		Comparator<AssignmentInfo> comparator = new GwtServerHelper.AssignmentInfoComparator(true);
		for (FolderRow fr:  folderRows) {
			// ...this time, to sort the assignee lists.
			Collections.sort(getAIListFromFR(fr, TaskHelper.ASSIGNMENT_TASK_ENTRY_ATTRIBUTE_NAME),             comparator);
			Collections.sort(getAIListFromFR(fr, EventHelper.ASSIGNMENT_CALENDAR_ENTRY_ATTRIBUTE_NAME),        comparator);
			Collections.sort(getAIListFromFR(fr, RESPONSIBLE_MILESTONE_ENTRY_ATTRIBUTE_NAME),                  comparator);
			
			Collections.sort(getAIListFromFR(fr, TaskHelper.ASSIGNMENT_GROUPS_TASK_ENTRY_ATTRIBUTE_NAME),      comparator);
			Collections.sort(getAIListFromFR(fr, EventHelper.ASSIGNMENT_GROUPS_CALENDAR_ENTRY_ATTRIBUTE_NAME), comparator);
			Collections.sort(getAIListFromFR(fr, RESPONSIBLE_GROUPS_MILESTONE_ENTRY_ATTRIBUTE_NAME),           comparator);
			
			Collections.sort(getAIListFromFR(fr, TaskHelper.ASSIGNMENT_TEAMS_TASK_ENTRY_ATTRIBUTE_NAME),       comparator);
			Collections.sort(getAIListFromFR(fr, EventHelper.ASSIGNMENT_TEAMS_CALENDAR_ENTRY_ATTRIBUTE_NAME),  comparator);
			Collections.sort(getAIListFromFR(fr, RESPONSIBLE_TEAMS_MILESTONE_ENTRY_ATTRIBUTE_NAME),            comparator);
		}
	}

	/*
	 * If the file's data can be viewed as an image, returns a URL to
	 * reference in an <IMG> tag.  Otherwise, returns null.
	 */
	private static boolean contentIsImage(AllModulesInjected bs, HttpServletRequest request, FolderEntry fe, FileAttachment fa) {
		boolean reply = false;
		try {
			// Can we convert the file's data to an image?
			InputStream	inputStream = bs.getFileModule().readFile(fe.getParentBinder(), fe, fa);
			byte[]		inputData   = FileCopyUtils.copyToByteArray(inputStream);
			ImageIcon	imageIcon   = new ImageIcon(inputData);
			
			reply =
				((null != imageIcon)                 &&
				 (0     < imageIcon.getIconHeight()) &&
				 (0     < imageIcon.getIconWidth()));
		}
		
		catch (Exception ex) {
			// Any exception we handle as though the file can't be
			// displayed as an image. 
			reply = false;
		}
		
		// If we get here, reply is true if the content can be viewed
		// as an image and false otherwise.  Return it.
		return reply;
	}
	
	/**
	 * Change the entry types for a collection of entries.
	 *
	 * @param bs
	 * @param request
	 * @param defId
	 * @param entityIds
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static ErrorListRpcResponseData changeEntryTypes(AllModulesInjected bs, HttpServletRequest request, String defId, List<EntityId> entityIds) throws GwtTeamingException {
		try {
			// Allocate an error list response we can return.
			ErrorListRpcResponseData reply = new ErrorListRpcResponseData(new ArrayList<ErrorInfo>());
			
			// Were we given the IDs of any entries to change their
			// entry types and the entry type to change them to?
			if (MiscUtil.hasItems(entityIds) && MiscUtil.hasString(defId)) {
				// Yes!  Scan them.
				FolderModule fm = bs.getFolderModule();
				for (EntityId entityId:  entityIds) {
					// If this entity is a binder...
					if (entityId.isBinder()) {
						// ...skip it.
						continue;
					}
					
					try {
						// Can we change this entry's entry type?
						fm.changeEntryType(entityId.getEntityId(), defId);
					}
					catch (Exception e) {
						// No!  Add an error  to the error list.
						String entryTitle = GwtServerHelper.getEntityTitle(bs, entityId);
						String messageKey;
						if (e instanceof AccessControlException) messageKey = "changeEntryTypeError.AccssControlException";
						else                                     messageKey = "changeEntryTypeError.OtherException";
						reply.addError(NLT.get(messageKey, new String[]{entryTitle}));
					}
				}
			}

			// If we get here, reply refers to an
			// ErrorListRpcResponseData containing any errors we
			// encountered.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.changeEntryTypes( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}

	/*
	 * Converts a List<ShareItem> into a List<GwtShareMeItem>
	 * representing the 'Shared by Me' items.
	 */
	private static List<GwtSharedMeItem> convertItemListToByMeList(AllModulesInjected bs, HttpServletRequest request, List<ShareItem> shareItems, Long userId, String sortBy, boolean sortDescend) throws Exception {
		// Allocate a List<GwtSharedMeItem> to hold the converted
		// List<ShareItem> information.
		List<GwtSharedMeItem> reply = new ArrayList<GwtSharedMeItem>();
		
		// If we don't have any share items to convert...
		if (!(MiscUtil.hasItems(shareItems))) {
			// ...return the empty reply list.
			return reply;
		}
		
		// Get the state of the shared view.
		SharedViewState svs;
		try                  {svs = getSharedViewState(bs, request, CollectionType.SHARED_BY_ME).getSharedViewState();}
		catch (Exception ex) {svs = new SharedViewState(true, false);                                                 }

		// Scan the share items.
		boolean			sharedFiles = getUserViewSharedFiles(request, CollectionType.SHARED_BY_ME);
		SharingModule	sm          = bs.getSharingModule();
		for (ShareItem si:  shareItems) {
			// Is this share item not the latest share for the entity?
			if (!(si.isLatest())) {
				// Yes!  Skip it.
				continue;
			}

			// Can we access the shared entity?
			DefinableEntity	siEntity;
			try {
				siEntity = sm.getSharedEntity(si);
			}
			catch (Exception e) {
				// No!  If it was because of an access control
				// exception...
				if (e instanceof AccessControlException) {
					// ...we'll simply skip it.  This can happen if
					// ...somebody revokes the users right to an
					// ...item they previously shared.
					siEntity = null;
				}
				
				else {
					// ...otherwise, propagate the exception.
					throw e;
				}
			}
			if (null == siEntity) {
				// No!  Skip it
				continue;
			}

			// Is this share item's entity in the trash?
			if (GwtServerHelper.isEntityPreDeleted(siEntity)) {
				// Yes!  Skip it.
				continue;
			}
			
			// Are we showing everything?
			boolean showHidden     = svs.isShowHidden();
			boolean showNonHidden  = svs.isShowNonHidden();
			boolean isEntityHidden = isSharedEntityHidden(bs, CollectionType.SHARED_BY_ME, siEntity);
			if ((!showHidden) || (!showNonHidden)) {
				// No!  Are we supposed to show entities in this hide
				// state?
				boolean showIt = ((isEntityHidden && showHidden) || ((!isEntityHidden) && showNonHidden));
				if (!showIt) {
					// Yes!  Skip it.
					continue;
				}
			}

			// Is this entity other than a file entity while we're only
			// showing files in the collection?
			String siEntityFamily = GwtServerHelper.getFolderEntityFamily(bs, siEntity);
			if (sharedFiles && (!(GwtServerHelper.isFamilyFile(siEntityFamily)))) {
				// Yes!  Skip it.
				continue;
			}

			// Is this an additional share item on an existing
			// GwtSharedMeItem?
			GwtSharedMeItem meItem = GwtSharedMeItem.findShareMeInList(siEntity, reply);
			if (null == meItem) {
				// No!  Create a new GwtSharedMeItem for it...
				meItem = new GwtSharedMeItem(
					isEntityHidden,		// true -> The entity is hidden.  false -> It isn't.
					siEntity,			// The entity being shared.
					siEntityFamily);	// The family of the entity.
				
				// ...and add it to the reply list.
				reply.add(meItem);
			}

			// Add information about this share item as a
			String recipientTitle;
			if (si.getIsPartOfPublicShare())
			     recipientTitle = NLT.get("share.recipientType.title.public");
			else recipientTitle = getRecipientTitle(bs, si.getRecipientType(), si.getRecipientId());
			meItem.addPerShareInfo(
				si,
				recipientTitle,
				getRecipientTitle(bs, RecipientType.user, si.getSharerId()));
		}

		// Sort the GwtPerShareInfo's attached to the
		// List<GwtSharedMeItem> we're going to return.
		PerShareInfoComparator.sortPerShareInfoLists(bs, CollectionType.SHARED_BY_ME, reply, sortBy, sortDescend);
		
		// If we get here, reply refers to the List<GwtSharedMeItem>
		// built from condensing the List<ShareItem>.  Return it.
		return reply;
	}
	
	/*
	 * Converts a List<ShareItem> into a List<GwtShareMeItem>
	 * representing the 'Shared with Me' items.
	 */
	private static List<GwtSharedMeItem> convertItemListToWithMeList(AllModulesInjected bs, HttpServletRequest request, List<ShareItem> shareItems, Long userId, List<Long> teams, List<Long> groups, String sortBy, boolean sortDescend) {
		// Allocate a List<GwtSharedMeItem> to hold the converted
		// List<ShareItem> information.
		List<GwtSharedMeItem> reply = new ArrayList<GwtSharedMeItem>();
		
		// If we don't have any share items to convert...
		if (!(MiscUtil.hasItems(shareItems))) {
			// ...return the empty reply list.
			return reply;
		}

		// Get the state of the shared view.
		SharedViewState svs;
		try                  {svs = getSharedViewState(bs, request, CollectionType.SHARED_WITH_ME).getSharedViewState();}
		catch (Exception ex) {svs = new SharedViewState(true, false);                                                   }

		// Scan the share items.
		boolean			sharedFiles = getUserViewSharedFiles(request, CollectionType.SHARED_WITH_ME);
		SharingModule	sm          = bs.getSharingModule();
		for (ShareItem si:  shareItems) {
			// Is this share item expired or not the latest share for
			// the entity?
			if (si.isExpired() || (!(si.isLatest()))) {
				// Yes!  Skip it.
				continue;
			}

			// Did user somehow share the item with themselves?
			if (si.getSharerId().equals(userId)) {
				// Yes!  Skip it.
				continue;
			}
			
			// Is this share item's entity in the trash?
			DefinableEntity siEntity = sm.getSharedEntity(si);
			if (GwtServerHelper.isEntityPreDeleted(siEntity)) {
				// Yes!  Skip it.
				continue;
			}

			// Are we showing everything?
			boolean showHidden     = svs.isShowHidden();
			boolean showNonHidden  = svs.isShowNonHidden();
			boolean isEntityHidden = isSharedEntityHidden(bs, CollectionType.SHARED_WITH_ME, siEntity);
			if ((!showHidden) || (!showNonHidden)) {
				// No!  Are we supposed to show entities in this hide
				// state?
				boolean showIt = ((isEntityHidden && showHidden) || ((!isEntityHidden) && showNonHidden));
				if (!showIt) {
					// Yes!  Skip it.
					continue;
				}
			}

			// Is this entity other than a file entity while we're only
			// showing files in the collection?
			String siEntityFamily = GwtServerHelper.getFolderEntityFamily(bs, siEntity);
			if (sharedFiles && (!(GwtServerHelper.isFamilyFile(siEntityFamily)))) {
				// Yes!  Skip it.
				continue;
			}
			
			// Create a new GwtSharedMeItem?
			GwtSharedMeItem meItem = GwtSharedMeItem.findShareMeInList(siEntity, reply);
			boolean newMeItem = (null == meItem);
			if (newMeItem) {
				meItem = new GwtSharedMeItem(
					isEntityHidden,		// true -> The entity is hidden.  false -> It isn't.
					siEntity,			// The entity being shared.
					siEntityFamily);	// The family of the entity.
			}

			// Is this share directed to this user, one of the user's
			// groups or one of the user's teams?  Well use the item
			// (break) if it does and skip it (continue) if it doesn't.
			Long rId = si.getRecipientId();
			switch (si.getRecipientType()) {
			case user:   if (userId.equals(  rId)) break; continue;	// Checks the user...
			case group:  if (groups.contains(rId)) break; continue;	// ...check the user's groups...
			case team:   if (teams.contains( rId)) break; continue;	// ...and check the user's teams.
			default:                                      continue;
			}
					
			// The share recipient belongs with this user!  Add the
			// information about it to the GwtSharedMeItem.
			String recipientTitle;
			if (si.getIsPartOfPublicShare())
			     recipientTitle = NLT.get("share.recipientType.title.public");
			else recipientTitle = getRecipientTitle(bs, si.getRecipientType(), si.getRecipientId());
			meItem.addPerShareInfo(
				si,
				recipientTitle,
				getRecipientTitle(bs, RecipientType.user, si.getSharerId()));

			// Has the GwtSharedMeItem actually been shared with
			// the current user?
			if (meItem.isShared() && newMeItem) {
				// Yes!  Add it to the reply
				// List<GwtSharedMeItem> we're building to return.
				reply.add(meItem);
			}
		}
		
		// Sort the GwtPerShareInfo's attached to the
		// List<GwtSharedMeItem> we're going to return.
		PerShareInfoComparator.sortPerShareInfoLists(bs, CollectionType.SHARED_WITH_ME, reply, sortBy, sortDescend);
		
		// If we get here, reply refers to the List<GwtSharedMeItem>
		// built from condensing the List<ShareItem>.  Return it.
		return reply;
	}
	
	/**
	 * Copies the entries.
	 * 
	 * @param bs
	 * @param request
	 * @param targetFolderId
	 * @param entityIds
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static ErrorListRpcResponseData copyEntries(AllModulesInjected bs, HttpServletRequest request, Long targetFolderId, List<EntityId> entityIds) throws GwtTeamingException {
		try {
			// Allocate an error list response we can return.
			ErrorListRpcResponseData reply = new ErrorListRpcResponseData(new ArrayList<ErrorInfo>());
			
			// Were we given the IDs of any entries to copy?
			if (MiscUtil.hasItems(entityIds)) {
				// Yes!  Decide on the actual target for the copy...
				TargetIds tis = new TargetIds(bs, targetFolderId);
				
				// ...and scan the entries to be copied.
				BinderModule bm = bs.getBinderModule();
				FolderModule fm = bs.getFolderModule();
				for (EntityId entityId:  entityIds) {
					try {
						// Can we copy this entity?
						if (entityId.isBinder()) {
							bm.copyBinder(entityId.getEntityId(), tis.getBinderTargetId(), true, null);
						}
						else {
							FolderEntry copiedFE = fm.copyEntry( entityId.getBinderId(), entityId.getEntityId(), tis.getEntryTargetId(),  null, null);
							if (null == copiedFE) {
								throw new TitleException("");
							}
						}
					}

					catch (Exception e) {
						// No!  Add an error  to the error list.
						String entryTitle = GwtServerHelper.getEntityTitle(bs, entityId);
						String messageKey;
						if      (e instanceof AccessControlException) messageKey = "copyEntryError.AccssControlException";
						else if (e instanceof TitleException)         messageKey = "copyEntryError.TitleException";
						else                                          messageKey = "copyEntryError.OtherException";
						reply.addError(NLT.get(messageKey, new String[]{entryTitle}));
					}
				}
			}

			// If we get here, reply refers to an
			// ErrorListRpcResponseData containing any errors we
			// encountered.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.copyEntries( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}
	
	/*
	 * Writes debug information about a file blob to the system log.
	 */
	private static void debugTraceBlob(FileBlob fileBlob, String methodName, String traceHead, String traceTail, boolean lastBlob) {
		if (m_logger.isDebugEnabled()) {
			String	dump  = (traceHead + ":  '" + fileBlob.getFileName() + "' (fSize:" + fileBlob.getFileSize() + ", bStart:" + fileBlob.getBlobStart() + ", bSize:" + fileBlob.getBlobSize() + ", last:" + lastBlob + ", md5Hash:" + fileBlob.getBlobMD5Hash() + ", uploadId:" + fileBlob.getUploadId() + ")");
			boolean hasTail = MiscUtil.hasString(traceTail);
			dump = ("GwtViewHelper." + methodName + "( " + dump + " )" + (hasTail ? ":  " + traceTail : ""));
			String data = fileBlob.getBlobData();
			dump += ("\n\nData Uploaded:  " + ((null == data) ? 0 : data.length()) + (fileBlob.isBlobBase64Encoded() ? " base64 encoded" : "") + " bytes."); 
			m_logger.debug(dump);
		}
	}
	
	private static void debugTraceBlob(FileBlob fileBlob, String methodName, String traceHead, boolean lastBlob) {
		// Always use the initial form of the method.
		debugTraceBlob(fileBlob, methodName, traceHead, null, lastBlob);
	}

	/**
	 * Delete user workspaces.
	 * 
	 * @param bs
	 * @param request
	 * @param userIds
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static ErrorListRpcResponseData deleteUserWorkspaces(AllModulesInjected bs, HttpServletRequest request, List<Long> userIds) throws GwtTeamingException {
		try {
			// Allocate an error list response we can return.
			ErrorListRpcResponseData reply = new ErrorListRpcResponseData(new ArrayList<ErrorInfo>());

			// Were we given the IDs of any users to delete?
			Long currentUserId = GwtServerHelper.getCurrentUserId(); 
			if (MiscUtil.hasItems(userIds)) {
				// Yes!  Scan them.
				boolean isOtherUserAccessRestricted = Utils.canUserOnlySeeCommonGroupMembers();
				for (Long userId:  userIds) {
					// Can we resolve the user being delete?
					User user = getResolvedUser(userId);
					if (null != user) {
						// Yes!  Is it the user that's logged in?
						if (user.getId().equals(currentUserId)) {
							// Yes!  They can't delete their own
							// workspace.  Ignore it.
							reply.addError(NLT.get("deleteUserWorkspaceError.self"));
							continue;
						}

						// Is it a reserved user?
						if (user.isReserved()) {
							// Yes!  They can't do that.  Ignore it.
							String userTitle = GwtServerHelper.getUserTitle(bs.getProfileModule(), isOtherUserAccessRestricted, String.valueOf(userId), ((null == user) ? "" : user.getTitle()));
							reply.addError(NLT.get("deleteUserWorkspaceError.reserved", new String[]{userTitle}));
							continue;
						}
					}
					
					try {
						// Does this user have a workspace ID?
						Long wsId = user.getWorkspaceId();
						if (null != wsId) {
							// Yes!  Delete the workspace.
							TrashHelper.preDeleteBinder(bs, wsId);
						}
					}

					catch (Exception e) {
						// No!  Add an error  to the error list.
						String userTitle = GwtServerHelper.getUserTitle(bs.getProfileModule(), isOtherUserAccessRestricted, String.valueOf(userId), ((null == user) ? "" : user.getTitle()));
						String messageKey;
						if      (e instanceof AccessControlException) messageKey = "deleteUserWorkspaceError.AccssControlException";
						else                                          messageKey = "deleteUserWorkspaceError.OtherException";
						reply.addError(NLT.get(messageKey, new String[]{userTitle}));
					}
				}
			}

			// If we get here, reply refers to an
			// ErrorListRpcResponseData containing any errors we
			// encountered.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.deleteUserWorkspaces( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}
	
	/**
	 * Disables the users.
	 * 
	 * @param bs
	 * @param request
	 * @param userIds
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static ErrorListRpcResponseData disableUsers(AllModulesInjected bs, HttpServletRequest request, List<Long> userIds) throws GwtTeamingException {
		try {
			// Allocate an error list response we can return.
			ErrorListRpcResponseData reply = new ErrorListRpcResponseData(new ArrayList<ErrorInfo>());

			// Were we given the IDs of any users to disable?
			Long currentUserId = GwtServerHelper.getCurrentUserId(); 
			if (MiscUtil.hasItems(userIds)) {
				// Yes!  Scan them.
				boolean isOtherUserAccessRestricted = Utils.canUserOnlySeeCommonGroupMembers();
				for (Long userId:  userIds) {
					// Can we resolve the user being disabled?
					User	user      = getResolvedUser(userId);
					String	userTitle = GwtServerHelper.getUserTitle(bs.getProfileModule(), isOtherUserAccessRestricted, String.valueOf(userId), ((null == user) ? "" : user.getTitle()));
					if (null != user) {
						// Yes!  Is it the user that's logged in?
						if (user.getId().equals(currentUserId)) {
							// Yes!  They can't disable themselves.
							// Ignore it.
							reply.addError(NLT.get("disableUserError.self"));
							continue;
						}

						// Is it a reserved user?
						if (user.isReserved()) {
							// Yes!  They can't do that.  Ignore it.
							reply.addError(NLT.get("disableUserError.reserved", new String[]{userTitle}));
							continue;
						}
						
						// Was this user provisioned from an LDAP
						// source?
						if (user.getIdentityInfo().isFromLdap()) {
							// Yes!  They can't be disabled this way.
							// Ignore it.
							reply.addError(NLT.get("disableUserError.fromLdap", new String[]{userTitle}));
							continue;
						}
					}
					
					try {
						// Can we disable this user?
						bs.getProfileModule().disableEntry(userId, true);	// true -> Disable.
					}

					catch (Exception e) {
						// No!  Add an error  to the error list.
						String messageKey;
						if (e instanceof AccessControlException) messageKey = "disableUserError.AccssControlException";
						else                                     messageKey = "disableUserError.OtherException";
						reply.addError(NLT.get(messageKey, new String[]{userTitle}));
					}
				}
			}

			// If we get here, reply refers to an
			// ErrorListRpcResponseData containing any errors we
			// encountered.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.disableUsers( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}
	
	/*
	 * Dumps the contents of a ViewInfo object.
	 */
	private static void dumpViewInfo(ViewInfo vi) {
		// If debug tracing isn't enabled...
		if (!(m_logger.isDebugEnabled())) {
			// ...bail.
			return;
		}

		// If we weren't given a ViewInfo to dump...
		if (null == vi) {
			// ...trace that fact and bail.
			m_logger.debug("...dumpViewInfo( null ):  No ViewInfo to dump.");
			return;
		}
		
		ViewType vt = vi.getViewType();
		m_logger.debug("...dumpViewInfo( " + vt.name() + " )");
		switch (vt) {
		case BINDER:
			BinderInfo bi = vi.getBinderInfo();
			BinderType bt = bi.getBinderType();
			m_logger.debug(".....dumpViewInfo( BINDER ):  " + bt.name());
			switch (bt) {
			case COLLECTION:
				m_logger.debug("........dumpViewInfo( BINDER:COLLECTION  ):  " + bi.getCollectionType().name());
				break;
				
			case FOLDER:
				m_logger.debug("........dumpViewInfo( BINDER:FOLDER      ):  " + bi.getFolderType().name());
				break;
				
			case WORKSPACE:
				m_logger.debug("........dumpViewInfo( BINDER:WORKSPACE   ):  " + bi.getWorkspaceType().name());
				break;
			
			case OTHER:
				m_logger.debug("........dumpViewInfo( BINDER:OTHER       )");
				break;
				
			default:
				m_logger.debug("........dumpViewInfo( BINDER:Not Handled ):  This BinderType is not implemented by the dumper.");
				break;
			}
			
			m_logger.debug("........dumpViewInfo( BINDER:Id         ):  " + bi.getBinderId());
			m_logger.debug("........dumpViewInfo( BINDER:Title      ):  " + bi.getBinderTitle());
			m_logger.debug("........dumpViewInfo( BINDER:EntityType ):  " + bi.getEntityType());
			
			break;
			
		case ADD_BINDER:
		case ADD_FOLDER_ENTRY:
		case ADD_PROFILE_ENTRY:
		case ADVANCED_SEARCH:
		case BUILD_FILTER:
		case VIEW_PROFILE_ENTRY:
		case OTHER:
			break;
			
		default:
			m_logger.debug("......dumpViewInfo( Not Handled ):  This ViewType is not implemented by the dumper.");
			break;
		}
	}

	/**
	 * Enables the users.
	 * 
	 * @param bs
	 * @param request
	 * @param userIds
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static ErrorListRpcResponseData enableUsers(AllModulesInjected bs, HttpServletRequest request, List<Long> userIds) throws GwtTeamingException {
		try {
			// Allocate an error list response we can return.
			ErrorListRpcResponseData reply = new ErrorListRpcResponseData(new ArrayList<ErrorInfo>());

			// Were we given the IDs of any users to enable?
			if (MiscUtil.hasItems(userIds)) {
				// Yes!  Scan them.
				boolean isOtherUserAccessRestricted = Utils.canUserOnlySeeCommonGroupMembers();
				for (Long userId:  userIds) {
					// Can we resolve the user being disabled?
					User	user      = getResolvedUser(userId);
					String	userTitle = GwtServerHelper.getUserTitle(bs.getProfileModule(), isOtherUserAccessRestricted, String.valueOf(userId), ((null == user) ? "" : user.getTitle()));
					if (null != user) {
						// Yes!  Was this user provisioned from an LDAP
						// source?
						if (user.getIdentityInfo().isFromLdap()) {
							// Yes!  They can't be enabled this way.
							// Ignore it.
							reply.addError(NLT.get("enableUserError.fromLdap", new String[]{userTitle}));
							continue;
						}
					}
					
					try {
						// Can we enable this user?
						bs.getProfileModule().disableEntry(userId, false);	// false -> Enable.
					}

					catch (Exception e) {
						// No!  Add an error  to the error list.
						String messageKey;
						if (e instanceof AccessControlException) messageKey = "enableUserError.AccssControlException";
						else                                     messageKey = "enableUserError.OtherException";
						reply.addError(NLT.get(messageKey, new String[]{userTitle}));
					}
				}
			}

			// If we get here, reply refers to an
			// ErrorListRpcResponseData containing any errors we
			// encountered.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.enableUsers( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}
	
	/*
	 * Checks for assignment column search key in the folder columns.
	 * If there's a column for it, simply returns.  If there's not a
	 * column for it, checks the entry map for assignments and if any
	 * are found, adds them to the assignment list.
	 */
	@SuppressWarnings("unchecked")
	private static void factorInAssignments(Map entryMap, List<FolderColumn> folderColumns, String csk, AssigneeType assigneeType, List<AssignmentInfo> assignmentList) {
		// Scan the columns.
		for (FolderColumn fc:  folderColumns) {
			// Is this column for the given search key?
			if (fc.getColumnSearchKey().equals(csk)) {
				// Yes!  Then we don't handle it's assignments
				// separately.  Bail.
				return;
			}
		}
		
		// Are there any assignments for the given column search key?
		List<AssignmentInfo> addList = GwtEventHelper.getAssignmentInfoListFromEntryMap(entryMap, csk, assigneeType);
		if (MiscUtil.hasItems(addList)) {
			// Yes!  Copy them into the assignment list we were given.
			for (AssignmentInfo ai:  addList) {
				assignmentList.add(ai);
			}
		}
	}
	
	/*
	 * Checks for the group assignment corresponding to the individual
	 * column search key in the folder columns.  If there's a column
	 * for it, simply returns.  If there's not a column for it, checks
	 * the entry map for assignments and if any are found, adds them to
	 * the assignment list.
	 */
	@SuppressWarnings("unchecked")
	private static void factorInGroupAssignments(Map entryMap, List<FolderColumn> folderColumns, String csk, List<AssignmentInfo> assignmentList) {
		// Can we determine the group assignment attribute?
		String groupCSK;
		if (csk.equals(TaskHelper.ASSIGNMENT_TASK_ENTRY_ATTRIBUTE_NAME))           groupCSK = TaskHelper.ASSIGNMENT_GROUPS_TASK_ENTRY_ATTRIBUTE_NAME;
		else if (csk.equals(EventHelper.ASSIGNMENT_CALENDAR_ENTRY_ATTRIBUTE_NAME)) groupCSK = EventHelper.ASSIGNMENT_GROUPS_CALENDAR_ENTRY_ATTRIBUTE_NAME;
		else if (csk.equals(RESPONSIBLE_MILESTONE_ENTRY_ATTRIBUTE_NAME))           groupCSK = RESPONSIBLE_GROUPS_MILESTONE_ENTRY_ATTRIBUTE_NAME;
		else                                                                       groupCSK = null;
		if (null != groupCSK) {
			// Yes!  Factor in any group assignments using it.
			factorInAssignments(entryMap, folderColumns, groupCSK, AssigneeType.GROUP, assignmentList);
		}
	}
	
	/*
	 * Checks for the team assignment corresponding to the individual
	 * column search key in the folder columns.  If there's a column
	 * for it, simply returns.  If there's not a column for it, checks
	 * the entry map for assignments and if any are found, adds them to
	 * the assignment list.
	 */
	@SuppressWarnings("unchecked")
	private static void factorInTeamAssignments(Map entryMap, List<FolderColumn> folderColumns, String csk, List<AssignmentInfo> assignmentList) {
		// Can we determine the team assignment attribute?
		String teamCSK;
		if (csk.equals(TaskHelper.ASSIGNMENT_TASK_ENTRY_ATTRIBUTE_NAME))           teamCSK = TaskHelper.ASSIGNMENT_TEAMS_TASK_ENTRY_ATTRIBUTE_NAME;
		else if (csk.equals(EventHelper.ASSIGNMENT_CALENDAR_ENTRY_ATTRIBUTE_NAME)) teamCSK = EventHelper.ASSIGNMENT_TEAMS_CALENDAR_ENTRY_ATTRIBUTE_NAME;
		else if (csk.equals(RESPONSIBLE_MILESTONE_ENTRY_ATTRIBUTE_NAME))           teamCSK = RESPONSIBLE_TEAMS_MILESTONE_ENTRY_ATTRIBUTE_NAME;
		else                                                                       teamCSK = null;
		if (null != teamCSK) {
			// Yes!  Factor in any team assignments using it.
			factorInAssignments(entryMap, folderColumns, teamCSK, AssigneeType.TEAM, assignmentList);
		}
	}

	/*
	 * Applies a quick filter to a List<FolderRow> of
	 * 'Shared by/with Me' rows.  A List<FolderRow> of the the
	 * FolderRow's from the input list that matches the filter is
	 * returned.
	 */
	public static List<FolderRow> filterSharedMeFolderRows(List<FolderColumn> folderColumns, List<FolderRow> folderRows, String quickFilter) {
		// Do we have a string to filter with and some FolderRow's to
		// be filtered?
		if (null != quickFilter) {
			quickFilter = quickFilter.trim().toLowerCase();
		}
		if (MiscUtil.hasString(quickFilter) && MiscUtil.hasItems(folderRows)) {
			// Yes!  Yes!  Scan the rows.
			List<FolderRow> reply = new ArrayList<FolderRow>();
			for (FolderRow fr:  folderRows) {
				// Scan the columns.
				for (FolderColumn fc:  folderColumns) {
					// What column is this?
					String cName = fc.getColumnName();
					if (FolderColumn.isColumnTitle(cName)) {
						// The title column!  If the title contains the
						// quick filter...
						EntryTitleInfo eti = fr.getColumnValueAsEntryTitle(fc);
						if (null != eti) {
							if (valueContainsQuickFilter(eti.getTitle(), quickFilter)) {
								// ...add it to the reply list.
								reply.add(fr);
								break;
							}
						}
					}
						
					else if (FolderColumn.isColumnSharedBy(cName) || FolderColumn.isColumnSharedWith(cName)) {
						// A shared by/with column!  Are there any
						// values for that?
						List<AssignmentInfo> aiList = fr.getColumnValueAsAssignmentInfos(fc);
						if (MiscUtil.hasItems(aiList)) {
							// Yes!  Scan them...
							boolean found = false;
							for (AssignmentInfo ai:  aiList) {
								// ...if this value contains the quick
								// ...filter...
								if (valueContainsQuickFilter(ai.getTitle(), quickFilter)) {
									// ...add it to the reply list.
									reply.add(fr);
									found = true;
									break;
								}
							}
							
							// Once we move the row to the reply...
							if (found) {
								// ...stop scanning the columns.
								break;
							}
						}
					}
					
					else if (FolderColumn.isColumnShareMessage(cName)) {
						// The share message column!  Are there any
						// values for that?
						List<ShareStringValue> svList = fr.getColumnValueAsShareMessageInfos(fc);
						if (MiscUtil.hasItems(svList)) {
							// Yes!  Scan them...
							boolean found = false;
							for (ShareStringValue sv:  svList) {
								// ...if this value contains the quick
								// ...filter...
								if (valueContainsQuickFilter(sv.getValue(), quickFilter)) {
									// ...add it to the reply list.
									reply.add(fr);
									found = true;
									break;
								}
							}
							
							// Once we move the row to the reply...
							if (found) {
								// ...stop scanning the columns.
								break;
							}
						}
					}
				}
			}
			folderRows = reply;
		}
		
		// If we get here, searchEntries refers to the filtered list of
		// entry maps.  Return it. 
		return folderRows;
	}

	/**
	 * Return true of the accessory panel should be visible on the
	 * given binder and false otherwise.
	 * 
	 * @param bs
	 * @param request
	 * @param binderId
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	@SuppressWarnings("unchecked")
	public static Boolean getAccessoryStatus(AllModulesInjected bs, HttpServletRequest request, Long binderId) throws GwtTeamingException {
		try {
			Binder			binder               = bs.getBinderModule().getBinder(binderId);
			User			user                 = GwtServerHelper.getCurrentUser();
			UserProperties	userFolderProperties = bs.getProfileModule().getUserProperties(user.getId(), binderId);
			
			// Has the user saved the status of the accessories panel
			// on this binder?
			Boolean accessoryStatus = ((Boolean) userFolderProperties.getProperty(ObjectKeys.USER_PROPERTY_BINDER_SHOW_ACCESSORIES));
			if (null == accessoryStatus) {
				// No!  If they have any accessories defined, we show
				// the panel.
				UserProperties	userProperties = bs.getProfileModule().getUserProperties(user.getId());
				Map<String, Object> model = new HashMap<String,Object>();
				DashboardHelper.getDashboardMap(binder, userProperties.getProperties(), model);
				Map ssDashboard = ((Map) model.get(WebKeys.DASHBOARD));
				accessoryStatus = DashboardHelper.checkIfAnyContentExists(ssDashboard);

				// Save this status so we don't have to read the
				// accessories again.
				saveAccessoryStatus(bs, request, binderId, accessoryStatus);
			}
			
			// If we get here, accessoryStatus contains true if we
			// should show the accessory panel and false otherwise.
			// Return it.
			return accessoryStatus;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.getAccessoryStatus( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}
	
	/*
	 * Returns the days duration value if only days were found. 
	 * Otherwise returns -1.
	 */
	@SuppressWarnings("unchecked")
	private static int getDurDaysFromEntryMap(String colName, Map entryMap) {
		int days = (-1);
		int seconds = MiscUtil.safeSToInt(((String) entryMap.get(colName + "#Duration#S")), (-1));
		int minutes = MiscUtil.safeSToInt(((String) entryMap.get(colName + "#Duration#M")),  (-1));
		int hours   = MiscUtil.safeSToInt(((String) entryMap.get(colName + "#Duration#H")),  (-1));
		int weeks   = MiscUtil.safeSToInt(((String) entryMap.get(colName + "#Duration#W")),  (-1));
		if ((0 >= seconds) && (0 >= minutes) && (0 >= hours) && (0 >= weeks)) {
			days = MiscUtil.safeSToInt(((String) entryMap.get(colName + "#Duration#D")),  (-1));
		}		
		return days;
	}
	
	/*
	 * Fixes up the group assignees in an List<AssignmentInfo>'s.
	 */
	private static void fixupAIGroups(List<AssignmentInfo> aiGroupsList, Map<Long, String> principalTitles, Map<Long, Integer> groupCounts) {
		// If don't have a list to fixup...
		if (!(MiscUtil.hasItems(aiGroupsList))) {
			// ...bail.
			return;
		}
		
		// The removeList is used to handle cases where an ID could
		// not be resolved (e.g., an 'Assigned To' group has been
		// deleted.)
		List<AssignmentInfo> removeList = new ArrayList<AssignmentInfo>();
		
		// Scan this AssignmentInfo's group assignees...
		for (AssignmentInfo ai:  aiGroupsList) {
			// ...skipping those that aren't really groups...
			if (!(ai.getAssigneeType().isGroup())) {
				continue;
			}
			
			// ...and setting each one's title and membership count.
			if (GwtEventHelper.setAssignmentInfoTitle(  ai, principalTitles)) {
				GwtEventHelper.setAssignmentInfoMembers(ai, groupCounts     );
				ai.setPresenceDude("pics/group_icon_small.png");
			}
			else {
				removeList.add(ai);
			}
		}
		GwtServerHelper.removeUnresolvedAssignees(aiGroupsList, removeList);
	}
	
	/*
	 * Fixes up the team assignees in an List<AssignmentInfo>'s.
	 */
	private static void fixupAITeams(List<AssignmentInfo> aiTeamsList, Map<Long, String> teamTitles, Map<Long, Integer> teamCounts) {
		// If don't have a list to fixup...
		if (!(MiscUtil.hasItems(aiTeamsList))) {
			// ...bail.
			return;
		}
		
		// The removeList is used to handle cases where an ID could
		// not be resolved (e.g., an 'Assigned To' team has been
		// deleted.)
		List<AssignmentInfo> removeList = new ArrayList<AssignmentInfo>();
		
		// Scan this AssignmentInfo's team assignees...
		for (AssignmentInfo ai:  aiTeamsList) {
			// ...skipping those that aren't really teams...
			if (!(ai.getAssigneeType().isTeam())) {
				continue;
			}
			
			// ...and setting each one's title and membership count.
			if (GwtEventHelper.setAssignmentInfoTitle(  ai, teamTitles)) {
				GwtEventHelper.setAssignmentInfoMembers(ai, teamCounts );
				ai.setPresenceDude("pics/team_16.png");
			}
			else {
				removeList.add(ai);
			}
		}
		GwtServerHelper.removeUnresolvedAssignees(aiTeamsList, removeList);
	}
	
	/*
	 * Fixes up the public assignees in an List<AssignmentInfo>'s.
	 */
	private static void fixupAIPublics(List<AssignmentInfo> aiTeamsList) {
		// If don't have a list to fixup...
		if (!(MiscUtil.hasItems(aiTeamsList))) {
			// ...bail.
			return;
		}
		
		// Scan this AssignmentInfo's team assignees...
		for (AssignmentInfo ai:  aiTeamsList) {
			// ...skipping those that aren't really public...
			if (!(ai.getAssigneeType().isPublic())) {
				continue;
			}
			
			// ...and setting each one's title and hover.
			ai.setTitle(NLT.get("share.recipientType.title.public"));
			ai.setHover(NLT.get("share.recipientType.hover.public"));
		}
	}
	
	/*
	 * Fixes up the individual assignees in an List<AssignmentInfo>'s.
	 */
	private static void fixupAIs(List<AssignmentInfo> aiList, Map<Long, String> principalTitles, Map<Long, GwtPresenceInfo> userPresence, Map<Long, Long> presenceUserWSIds, Map<Long, String> avatarUrls) {
		// If don't have a list to fixup...
		if (!(MiscUtil.hasItems(aiList))) {
			// ...bail.
			return;
		}
		
		// The removeList is used to handle cases where an ID could
		// not be resolved (e.g., an 'Assigned To' user has been
		// deleted.)
		List<AssignmentInfo> removeList = new ArrayList<AssignmentInfo>();
		
		// Scan this AssignmentInfo's individual assignees...
		for (AssignmentInfo ai:  aiList) {
			// ...skipping those that aren't really individuals...
			if (!(ai.getAssigneeType().isIndividual())) {
				continue;
			}
			
			// ...and setting each one's title.
			if (GwtEventHelper.setAssignmentInfoTitle(           ai, principalTitles )) {
				GwtEventHelper.setAssignmentInfoPresence(        ai, userPresence     );
				GwtEventHelper.setAssignmentInfoPresenceUserWSId(ai, presenceUserWSIds);
				GwtEventHelper.setAssignmentInfoAvatarUrl(       ai, avatarUrls       );
			}
			else {
				removeList.add(ai);
			}
		}
		GwtServerHelper.removeUnresolvedAssignees(aiList, removeList);
	}
	
	/*
	 * Walks the List<FolderColumn>'s setting the search and sort keys
	 * appropriately for each.
	 * 
	 * Note:  The algorithm used in this method for columns whose names
	 *    contain multiple parts was reverse engineered from that used
	 *    by folder_view_common2.jsp or view_trash.jsp.
	 */
	private static void fixupFCs(List<FolderColumn> fcList, boolean isTrash, boolean isCollection) {
		// We need to handle the columns that were added for
		// collections.
		for (FolderColumn fc:  fcList) {
			String colName = fc.getColumnName();
			if      (colName.equals("author"))           {fc.setColumnSearchKey(Constants.PRINCIPAL_FIELD);              fc.setColumnSortKey(Constants.CREATOR_TITLE_FIELD);    }
			else if (colName.equals("comments"))         {fc.setColumnSearchKey(Constants.TOTALREPLYCOUNT_FIELD);                                                               }
			else if (colName.equals("date"))             {fc.setColumnSearchKey(Constants.MODIFICATION_DATE_FIELD);                                                             }
			else if (colName.equals("description"))      {fc.setColumnSearchKey(Constants.DESC_FIELD);                                                                          }
			else if (colName.equals("descriptionHtml"))  {fc.setColumnSearchKey(Constants.DESC_FIELD);                                                                          }
			else if (colName.equals("download"))         {fc.setColumnSearchKey(Constants.FILENAME_FIELD);                                                                      }
			else if (colName.equals("dueDate"))          {fc.setColumnSearchKey(Constants.DUE_DATE_FIELD);                                                                      }
			else if (colName.equals("emailAddress"))     {fc.setColumnSearchKey(Constants.EMAIL_FIELD);                                                                         }
			else if (colName.equals("family"))           {fc.setColumnSearchKey(Constants.FAMILY_FIELD);                 fc.setColumnSortable(false);                           }
			else if (colName.equals("fullName"))         {fc.setColumnSearchKey(Constants.PRINCIPAL_FIELD);              fc.setColumnSortKey(Constants.SORT_TITLE_FIELD);       }
			else if (colName.equals("guest"))            {fc.setColumnSearchKey(Constants.PRINCIPAL_FIELD);              fc.setColumnSortKey(Constants.CREATOR_TITLE_FIELD);    }
			else if (colName.equals("html"))             {fc.setColumnSearchKey(Constants.FILE_ID_FIELD);                                                                       }
			else if (colName.equals("location"))         {fc.setColumnSearchKey(Constants.PRE_DELETED_FIELD);                                                                   }
			else if (colName.equals("loginId"))          {fc.setColumnSearchKey(Constants.LOGINNAME_FIELD);                                                                     }
			else if (colName.equals("netfolder_access")) {fc.setColumnSearchKey(FolderColumn.COLUMN_NETFOLDER_ACCESS);   fc.setColumnSortable(false);                           }
			else if (colName.equals("number"))           {fc.setColumnSearchKey(Constants.DOCNUMBER_FIELD);              fc.setColumnSortKey(Constants.SORTNUMBER_FIELD);       }
			else if (colName.equals("rating"))           {fc.setColumnSearchKey(Constants.RATING_FIELD);                                                                        }
			else if (colName.equals("responsible"))      {fc.setColumnSearchKey(Constants.RESPONSIBLE_FIELD);                                                                   }
			else if (colName.equals("size"))             {fc.setColumnSearchKey(Constants.FILE_SIZE_FIELD);                                                                     }
			else if (colName.equals("share_access"))     {fc.setColumnSearchKey(FolderColumn.COLUMN_SHARE_ACCESS);                                                              }
			else if (colName.equals("share_date"))       {fc.setColumnSearchKey(FolderColumn.COLUMN_SHARE_DATE);                                                                }
			else if (colName.equals("share_expiration")) {fc.setColumnSearchKey(FolderColumn.COLUMN_SHARE_EXPIRATION);                                                          }
			else if (colName.equals("share_message"))    {fc.setColumnSearchKey(FolderColumn.COLUMN_SHARE_MESSAGE);                                                             }
			else if (colName.equals("share_sharedBy"))   {fc.setColumnSearchKey(FolderColumn.COLUMN_SHARE_SHARED_BY);                                                           }
			else if (colName.equals("share_sharedWith")) {fc.setColumnSearchKey(FolderColumn.COLUMN_SHARE_SHARED_WITH);                                                         }
			else if (colName.equals("state"))            {fc.setColumnSearchKey(Constants.WORKFLOW_STATE_CAPTION_FIELD); fc.setColumnSortKey(Constants.WORKFLOW_STATE_FIELD);   }
			else if (colName.equals("status"))           {fc.setColumnSearchKey(Constants.STATUS_FIELD);                                                                        }
			else if (colName.equals("tasks"))            {fc.setColumnSearchKey(Constants.TASKS_FIELD);                                                                         }
			else if (colName.equals("title"))            {fc.setColumnSearchKey(Constants.TITLE_FIELD);                  fc.setColumnSortKey(Constants.SORT_TITLE_FIELD);       }
			else if (colName.equals("userType"))         {fc.setColumnSearchKey(Constants.IDENTITY_INTERNAL_FIELD);      fc.setColumnSortKey(Constants.IDENTITY_INTERNAL_FIELD);}
			else {
				// Does the column name contain multiple parts wrapped
				// in a single value?
				String defId      = null;
				String eleType    = null;
				String eleName    = null;
				String eleCaption = null;
				if (colName.contains(",")) {
					String[] temp = colName.split(",");
					if (4 <= temp.length) {
						defId   = temp[0];
						eleType = temp[1];
						eleName = temp[2];
						
						// Since the caption may have commas in it, we
						// need to get everything past the third comma.
						eleCaption = colName.substring(colName.indexOf(",")+1);
						eleCaption = eleCaption.substring(eleCaption.indexOf(",")+1);
						eleCaption = eleCaption.substring(eleCaption.indexOf(",")+1);
					}
				}
				if (MiscUtil.hasString(defId)) {
					// Yes!  Update the FolderColumn components based
					// on the information extracted from the field.
					fc.setColumnDefId(  defId  );
					fc.setColumnType(   eleType);
					fc.setColumnEleName(eleName);
					
					if (!(MiscUtil.hasString(fc.getColumnTitle()))) {
						fc.setColumnTitle(eleCaption);
					}
					
					String eleSortName;
					if      (eleType.equals("selectbox") || eleType.equals("radio"))  eleSortName = ("_caption_" + eleName);
					else if (eleType.equals("text")      || eleType.equals("hidden")) eleSortName = ("_sort_"    + eleName);
					else if (eleType.equals("event"))                                 eleSortName = (eleName + "#LogicalStartDate");
					else                                                              eleSortName = eleName;
					fc.setColumnSortKey(eleSortName);
				} 
				
				else {
					// No, the name doesn't have multiple parts wrapped
					// in a single value!  Just use the name for the
					// search and sort keys.
					fc.setColumnSearchKey(colName);
					fc.setColumnEleName(  colName);	//If not a custom attribute, the element name is the colName
				}
			}

			// If we're dealing with a trash view...
			if (isTrash) {
				// ...some of the fields are managed using a different search key. 
				if      (colName.equals("author"))   {fc.setColumnSearchKey(Constants.PRE_DELETED_BY_ID_FIELD); fc.setColumnSortKey("");}
				else if (colName.equals("date"))     {fc.setColumnSearchKey(Constants.PRE_DELETED_WHEN_FIELD);  fc.setColumnSortKey("");}
				else if (colName.equals("location")) {fc.setColumnSearchKey(Constants.PRE_DELETED_FROM_FIELD);  fc.setColumnSortKey("");}
			}
		}
	}
	
	/*
	 * Scans the List<FolderRow> and sets the access rights for the
	 * current user for each row.
	 */
	private static void fixupFRs(AllModulesInjected bs, HttpServletRequest request, List<FolderRow> frList) {
		// If we don't have any FolderRow's to complete...
		if (!(MiscUtil.hasItems(frList))) {
			// ..bail.
			return;
		}

		// Collect the entity IDs of the rows from the List<FolderRow>.
		List<Long> entryIds  = new ArrayList<Long>();
		List<Long> binderIds = new ArrayList<Long>();
		for (FolderRow fr:  frList) {
			Long id = fr.getEntityId().getEntityId();
			if (fr.isBinder())
			     binderIds.add(id);
			else entryIds.add( id);
		}
		
		try {
			// Read the FolderEntry's for the rows...
			FolderModule fm = bs.getFolderModule();
			SortedSet<FolderEntry> entries = fm.getEntries(entryIds);
			
			// ...mapping each FolderEntry to its ID.
			Map<Long, FolderEntry> entryMap = new HashMap<Long, FolderEntry>();
			for (FolderEntry entry: entries) {
				entryMap.put(entry.getId(), entry);
			}

			// Scan the List<FolderRow> again.
			FolderColumn commentsCol = new FolderColumn("comments");
			for (FolderRow fr:  frList) {
				// Skipping any binders.
				if (fr.isBinder()) {
					continue;
				}
				
				// Do we have the FolderEntry for this row?
				FolderEntry entry = entryMap.get(fr.getEntityId().getEntityId());
				if (null != entry) {
					// Yes!  Store the user's rights to that
					// FolderEntry.
					fr.setCanModify(fm.testAccess(entry, FolderOperation.modifyEntry   ));
					fr.setCanPurge( fm.testAccess(entry, FolderOperation.deleteEntry   ));
					fr.setCanTrash( fm.testAccess(entry, FolderOperation.preDeleteEntry));
					fr.setCanShare( GwtShareHelper.isEntitySharable(bs, entry          ));
					
					// If the user can't add replies to this entry...
					if (!(fm.testAccess(entry, FolderOperation.addReply))) {
						// ...and we have a CommentsInfo for it...
						CommentsInfo ci = fr.getColumnValueAsComments(commentsCol);
						if (null != ci) {
							// ...update its can add replies field
							// ...accordingly.
							ci.setCanAddReplies(false);
						}
					}
				}
			}

			// Read the Binder's for the rows (including those intermediate sub-binders that might be inaccessible)...
			BinderModule bm = bs.getBinderModule();
			SortedSet<Binder> binders = bm.getBinders(binderIds, Boolean.FALSE);

			// ...mapping each Binder to its ID.
			Map<Long, Binder> binderMap = new HashMap<Long, Binder>();
			for (Binder binder:  binders) {
				binderMap.put(binder.getId(), binder);
			}

			// Scan the List<FolderRow> again.
			for (FolderRow fr:  frList) {
				// Skipping any entries.
				if (!(fr.isBinder())) {
					continue;
				}

				// Do we have the Binder for this row?
				Binder binder = binderMap.get(fr.getEntityId().getEntityId());
				if (null != binder) {
					// Yes!  Store its icon names...
					fr.setBinderIcon(binder.getIconName(),                BinderIconSize.SMALL );
					fr.setBinderIcon(binder.getIconName(IconSize.MEDIUM), BinderIconSize.MEDIUM);
					fr.setBinderIcon(binder.getIconName(IconSize.LARGE ), BinderIconSize.LARGE );
					
					// ...store a BinderInfo for it...
					fr.setBinderInfo(GwtServerHelper.getBinderInfo(request, bs, binder));

					// ...and the user's rights to that Binder.
					fr.setCanModify(bm.testAccess(binder, BinderOperation.modifyBinder   ));
					fr.setCanPurge( bm.testAccess(binder, BinderOperation.deleteBinder   ));
					fr.setCanTrash( bm.testAccess(binder, BinderOperation.preDeleteBinder));
					fr.setCanShare( GwtShareHelper.isEntitySharable(bs, binder           ));
				}
			}
		}
		
		catch (Exception ex) {/* Ignored. */}
	}

	/*
	 * Returns a non-null List<AssignmentInfo> from a folder row for a
	 * given attribute.
	 */
	private static List<AssignmentInfo> getAIListFromFR(FolderRow fr, String attrName) {
		Map<String, List<AssignmentInfo>> aiMap = fr.getRowAssigneeInfoListsMap();
		List<AssignmentInfo> reply = aiMap.get(attrName.toLowerCase());
		return ((null == reply) ? new ArrayList<AssignmentInfo>() : reply);
	}

	/*
	 * Returns a non-null List<AssignmentInfo> build from a
	 * sharer's ID.
	 */
	private static List<AssignmentInfo> getAIListFromRecipients(List<GwtPerShareInfo> perShareInfos) {
		// Allocate a List<AssignmentInfo> to return.
		List<AssignmentInfo> reply = new ArrayList<AssignmentInfo>();

		// Scan the shares...
		for (GwtPerShareInfo psi:  perShareInfos) {
			// ...creating an AssignmentInfo for each recipient.
			AssigneeType assigneeType;
			if (psi.isRecipientPublic()) {
				assigneeType = AssigneeType.PUBLIC;
			}
			else {
				switch (psi.getRecipientType()) {
				default:     assigneeType = null;                    continue;
				case user:   assigneeType = AssigneeType.INDIVIDUAL; break;
				case group:  assigneeType = AssigneeType.GROUP;      break;
				case team:   assigneeType = AssigneeType.TEAM;       break;
				}
			}
			AssignmentInfo ai = AssignmentInfo.construct(
				psi.getRecipientId(),
				assigneeType);
			reply.add(ai);
		}

		// If we get here, reply refers to the List<AssignmentInfo>
		// corresponding to the given List<GwtPerShareInfo>.  Return
		// it.
		return reply;
	}
	
	/*
	 * Returns a non-null List<AssignmentInfo> build from a
	 * sharer's ID.
	 */
	private static List<AssignmentInfo> getAIListFromSharers(List<GwtPerShareInfo> perShareInfos) {
		// Allocate a List<AssignmentInfo> to return.
		List<AssignmentInfo> reply = new ArrayList<AssignmentInfo>();

		// Scan the shares...
		for (GwtPerShareInfo psi:  perShareInfos) {
			// ...creating an AssignmentInfo for each sharer.
			AssignmentInfo ai = AssignmentInfo.construct(
				psi.getSharerId(),
				AssigneeType.INDIVIDUAL);
			reply.add(ai);
		}

		// If we get here, reply refers to the List<AssignmentInfo>
		// corresponding to the given sharer ID.  Return it.
		return reply;
	}
	
	/**
	 * Reads information for rendering a binder's description in a
	 * BinderDescriptionRpcResponseData object.
	 * 
	 * @param bs
	 * @param request
	 * @param binderId
	 * 
	 * @return
	 */
	public static BinderDescriptionRpcResponseData getBinderDescription(AllModulesInjected bs, HttpServletRequest request, Long binderId) throws GwtTeamingException {
		try {
			// Access the BinderInfo for the binder's description
			// information...
			BinderInfo binderInfo = GwtServerHelper.getBinderInfo(bs, request, String.valueOf(binderId));

			// ...and use it to construct and return a
			// ...BinderDescriptionRpcResponseData object.
			return
				new BinderDescriptionRpcResponseData(
					binderInfo.getBinderDesc(),
					binderInfo.isBinderDescHTML(),
					binderInfo.isBinderDescExpanded());
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.getBinderDescription( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}

	/**
	 * Reads the current user's filters for a binder and returns them
	 * as a BinderFiltersRpcResponseData.
	 * 
	 * The algorithm used in this method was reverse engineered from
	 * that used by BinderHelper.getSearchFilter() and
	 * view_forum_user_filters.jsp.
	 * 
	 * @param bs
	 * @param request
	 * @param binderId
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static BinderFiltersRpcResponseData getBinderFilters(AllModulesInjected bs, HttpServletRequest request, Long binderId) throws GwtTeamingException {
		try {
			Binder				binder = bs.getBinderModule().getBinderWithoutAccessCheck(binderId);  //Not giving any secrets away, so do it fast
			List<BinderFilter>	ffList = new ArrayList<BinderFilter>();
			User				user   = GwtServerHelper.getCurrentUser();

			// Are there any global filters defined?
			TreeMap<String, String>	filterMap = new TreeMap<String, String>(new StringComparator(user.getLocale()));
			Map searchFilters = ((Map) binder.getProperty(ObjectKeys.BINDER_PROPERTY_FILTERS));
			if (MiscUtil.hasItems(searchFilters)) {
				// Yes!  Add them to the sort map.
				Set<String> keySet = searchFilters.keySet();
				for (Iterator<String> ksIT = keySet.iterator(); ksIT.hasNext(); ) {
					filterMap.put(ksIT.next(), ObjectKeys.USER_PROPERTY_USER_FILTER_GLOBAL);
				}
			}
			
			// Does the user have any personal filters defined?
			UserProperties userBinderProperties = bs.getProfileModule().getUserProperties(user.getId(), binderId);
			searchFilters = ((Map) userBinderProperties.getProperty(ObjectKeys.USER_PROPERTY_SEARCH_FILTERS));
			if (MiscUtil.hasItems(searchFilters)) {
				// Yes!  Add them to the sort map.
				Set<String> keySet = searchFilters.keySet();
				for (Iterator<String> ksIT = keySet.iterator(); ksIT.hasNext(); ) {
					filterMap.put(ksIT.next(), ObjectKeys.USER_PROPERTY_USER_FILTER_PERSONAL);
				}
			}

			// Based on the binder type, define the appropriate action
			// to use to view this type of binder.
			String viewAction;
			String binderType = binder.getEntityType().name();
			if      (binderType.equals(EntityIdentifier.EntityType.folder.name()))    viewAction = WebKeys.ACTION_VIEW_FOLDER_LISTING;
			else if (binderType.equals(EntityIdentifier.EntityType.workspace.name())) viewAction = WebKeys.ACTION_VIEW_WS_LISTING;
			else if (binderType.equals(EntityIdentifier.EntityType.profiles.name()))  viewAction = WebKeys.ACTION_VIEW_PROFILE_LISTING;
			else {
				throw new IllegalArgumentException("Unknown binderType" + binderType);
			}
			
			// Did we find any filters?
			AdaptedPortletURL url;
			if (!(filterMap.isEmpty())) {
				// ...scan the sorted set...
				Set<String> keySet = filterMap.keySet();
				for (Iterator<String> ksIT = keySet.iterator(); ksIT.hasNext(); ) {
					// ...and add a BinderFilter for each to the list.
					String filterName  = ksIT.next();
					String filterScope = filterMap.get(filterName);
					url = new AdaptedPortletURL(request, "ss_forum", true);
					url.setParameter(WebKeys.ACTION,            viewAction               );
					url.setParameter(WebKeys.URL_BINDER_ID,     binder.getId().toString());
					url.setParameter(WebKeys.URL_OPERATION,     WebKeys.URL_SELECT_FILTER);
					url.setParameter(WebKeys.URL_OPERATION2,    filterScope              );
					url.setParameter(WebKeys.URL_SELECT_FILTER, filterName               );
					String filterAddUrl = url.toString();
					
					url = new AdaptedPortletURL(request, "ss_forum", true);
					url.setParameter(WebKeys.ACTION,           viewAction               );
					url.setParameter(WebKeys.URL_BINDER_ID,    binder.getId().toString());
					url.setParameter(WebKeys.URL_OPERATION,    WebKeys.URL_CLEAR_FILTER );
					url.setParameter(WebKeys.URL_OPERATION2,   filterScope              );
					url.setParameter(WebKeys.URL_CLEAR_FILTER, filterName               );
					String filterClearUrl = url.toString();
					
					ffList.add(
						new BinderFilter(
							filterName,
							filterScope,
							filterAddUrl,
							filterClearUrl));
				}
			}
			
			// Use the data we obtained to create a
			// BinderFiltersRpcResponseData.
			BinderFiltersRpcResponseData reply = new BinderFiltersRpcResponseData(ffList);
			
			// Store the current filters, if any, that the user
			// currently has selected on this binder.
			reply.setCurrentFilters(GwtServerHelper.getCurrentUserFilters(userBinderProperties, true));

			// Store a URL to turn off filtering on the binder.
			url = new AdaptedPortletURL(request, "ss_forum", true);
			url.setParameter(WebKeys.ACTION,            viewAction               );
			url.setParameter(WebKeys.URL_BINDER_ID,     binder.getId().toString());
			url.setParameter(WebKeys.URL_OPERATION,     WebKeys.URL_SELECT_FILTER);
			url.setParameter(WebKeys.URL_SELECT_FILTER, ""                       );
			reply.setFiltersOffUrl(url.toString());
			
			// Store a URL to edit the filters on the binder.
			url = new AdaptedPortletURL(request, "ss_forum", true);
			url.setParameter(WebKeys.ACTION,          WebKeys.ACTION_BUILD_FILTER);
			url.setParameter(WebKeys.URL_BINDER_ID,   binder.getId().toString()  );
			url.setParameter(WebKeys.URL_BINDER_TYPE, binderType                 );
			reply.setFilterEditUrl(url.toString());
			
			// Finally, return the BinderFiltersRpcResponseData we just
			// constructed.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.getBinderFilters( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}

	/**
	 * Returns an AvatarInfoRpcResponseData object containing the
	 * information about a binder owner's avatar.
	 * 
	 * @param bs
	 * @param request
	 * @param binderId
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static AvatarInfoRpcResponseData getBinderOwnerAvatarInfo(AllModulesInjected bs, HttpServletRequest request, Long binderId) throws GwtTeamingException {
		try {
			// Construct a GuestInfo from the binder's owner...
			Binder binder = bs.getBinderModule().getBinder(Long.valueOf(binderId), Boolean.TRUE);
			Principal p = binder.getCreation().getPrincipal(); //creator is user
			GuestInfo gi = getGuestInfoFromPrincipal(bs, request, Utils.fixProxy(p));
			
			// ...and use that to construct an AvatarInfoRpcResponseData.
			AvatarInfoRpcResponseData reply = new AvatarInfoRpcResponseData(gi);

			// If we get here, reply refers to the
			// AvatarInfoRpcResponseData for the binder's owner.
			// Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.getBinderOwnerAvatarInfo( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}
	
	/**
	 * Reads the current user's region state for a binder and returns
	 * it as a StringRpcResponseData.
	 * 
	 * @param bs
	 * @param request
	 * @param binderId
	 * @param regionId
	 * 
	 * @return
	 */
	public static StringRpcResponseData getBinderRegionState(AllModulesInjected bs, HttpServletRequest request, Long binderId, String regionId) throws GwtTeamingException {
		try {
			// Does the user have this region state defined?
			UserProperties userBinderProperties = bs.getProfileModule().getUserProperties(GwtServerHelper.getCurrentUserId(), binderId);
			String regionState = ((String) userBinderProperties.getProperty(ObjectKeys.USER_PROPERTY_REGION_VIEW + "." + regionId));

			// Use the data we obtained to create a
			// StringRpcResponseData and return it.
			return new StringRpcResponseData(MiscUtil.hasString(regionState) ? regionState : "expanded");
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.getBinderRegionState( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}

	/*
	 * Returns a String[] of the names of the columns to display in a
	 * given collection type.
	 */
	private static String[] getCollectionColumnNames(CollectionType ct) {
		String[] reply;
		switch (ct) {
		case MY_FILES:       reply = pruneColumnNames(ct, "title", "comments", "size",   "date"                                                                     ); break;
		case NET_FOLDERS:    reply = pruneColumnNames(ct, "title", "date",     "netfolder_access", "descriptionHtml"                                                ); break;
		case SHARED_BY_ME:   reply = pruneColumnNames(ct, "title", "comments", "share_sharedWith", "share_date", "share_expiration", "share_access", "share_message"); break;
		case SHARED_WITH_ME: reply = pruneColumnNames(ct, "title", "comments", "share_sharedBy",   "share_date", "share_expiration", "share_access", "share_message"); break;
		default:             reply = new String[0];                                                                                                                    break;
		}
		return reply;
	}

	/*
	 * Returns the entries for the given collection.
	 */
	@SuppressWarnings("unchecked")
	private static Map getCollectionEntries(AllModulesInjected bs, HttpServletRequest request, Binder binder, String quickFilter, Map options, CollectionType ct, List<GwtSharedMeItem> shareItems) {
		// Construct the search Criteria...
		Criteria crit;
		switch (ct) {
		default:
		case MY_FILES:
			// Use the common criteria builder for My Files.
            crit = SearchUtils.getMyFilesSearchCriteria(bs, binder.getId());
			break;

		case NET_FOLDERS:
			// Create the criteria for top level mirrored file folders
			// that have been configured.
			crit = SearchUtils.getNetFoldersSearchCriteria(bs, false);	// false -> Don't default to the top workspace.
			
			// Factor in any quick filter we've got.
			addQuickFilterToCriteria(quickFilter, crit);

			// Add in the sort information...
			boolean sortAscend = (!(GwtUIHelper.getOptionBoolean(options, ObjectKeys.SEARCH_SORT_DESCEND, false                   )));
			String  sortBy     =    GwtUIHelper.getOptionString( options, ObjectKeys.SEARCH_SORT_BY,      Constants.SORT_TITLE_FIELD);
			crit.addOrder(new Order(Constants.ENTITY_FIELD, sortAscend));
			crit.addOrder(new Order(sortBy,                 sortAscend));
			
			// ...and issue the query and return the entries.
			Binder nfBinder = SearchUtils.getNetFoldersRootBinder();
			return
				bs.getBinderModule().searchFolderOneLevelWithInferredAccess(
					crit,
					Constants.SEARCH_MODE_SELF_CONTAINED_ONLY,
					GwtUIHelper.getOptionInt(options, ObjectKeys.SEARCH_OFFSET,   0),
					GwtUIHelper.getOptionInt(options, ObjectKeys.SEARCH_MAX_HITS, ObjectKeys.SEARCH_MAX_HITS_SUB_BINDERS),
					nfBinder);
			
		case SHARED_BY_ME:
		case SHARED_WITH_ME:
			// Do we have any shares to analyze?
			if (!(MiscUtil.hasItems(shareItems))) {
				// No!  Bail.
				return buildEmptyEntryMap();
			}
			
			// Scan the items that have been shared by/with the current
			// user...
			List<String>	sharedBinderIds = new ArrayList<String>();
			List<String>	sharedEntryIds  = new ArrayList<String>();
			for (GwtSharedMeItem si:  shareItems) {
				// ...tracking each as a binder or entry.
				DefinableEntity	entity   = si.getEntity();
				String			entityId = String.valueOf(entity.getId());
				if (entity.getEntityType().equals(EntityType.folderEntry))
				     sharedEntryIds.add( entityId);
				else sharedBinderIds.add(entityId);
			}
			
			// Do we have any binders or entries that have been shared
			// by/with the current user?
			boolean hasSharedBinders = (!(sharedBinderIds.isEmpty()));
			boolean hasSharedEntries = (!(sharedEntryIds.isEmpty()));
			if ((!hasSharedBinders) && (!hasSharedEntries)) {
				// No!  Bail.
				return buildEmptyEntryMap();
			}
			
			return
				buildSearchMapFromSharedMeList(
					bs,
					shareItems,
					GwtUIHelper.getOptionBoolean(options, ObjectKeys.SEARCH_SORT_DESCEND, false),
					GwtUIHelper.getOptionString( options, ObjectKeys.SEARCH_SORT_BY,      Constants.SORT_TITLE_FIELD));
		}

		// Factor in any quick filter we've got.
		addQuickFilterToCriteria(quickFilter, crit);

		// Add in the sort information...
		boolean sortAscend = (!(GwtUIHelper.getOptionBoolean(options, ObjectKeys.SEARCH_SORT_DESCEND, false                   )));
		String  sortBy     =    GwtUIHelper.getOptionString( options, ObjectKeys.SEARCH_SORT_BY,      Constants.SORT_TITLE_FIELD);
		crit.addOrder(new Order(Constants.ENTITY_FIELD, sortAscend));
		crit.addOrder(new Order(sortBy,                 sortAscend));
		
		// ...and issue the query and return the entries.
		return
			bs.getBinderModule().executeSearchQuery(
				crit,
				Constants.SEARCH_MODE_NORMAL,
				GwtUIHelper.getOptionInt(options, ObjectKeys.SEARCH_OFFSET,   0),
				GwtUIHelper.getOptionInt(options, ObjectKeys.SEARCH_MAX_HITS, ObjectKeys.SEARCH_MAX_HITS_SUB_BINDERS));
	}
	
	/*
	 * Returns a LinkedHashMap of the column names from a String[]
	 * of them.
	 */
	@SuppressWarnings("unchecked")
	private static Map getColumnsLHMFromAS(String[] columnNames) {
		Map reply = new LinkedHashMap();
		for (String columnName:  columnNames) {
			reply.put(columnName, columnName);
		}
		return reply;
	}

	/**
	 * Returns the column widths for a user on a folder.
	 * 
	 * @param bs
	 * @param request
	 * @param folderInfo
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	@SuppressWarnings("unchecked")
	public static ColumnWidthsRpcResponseData getColumnWidths(AllModulesInjected bs, HttpServletRequest request, BinderInfo folderInfo) throws GwtTeamingException {
		try {
			// Read the column widths stored in the folder properties...
			User			user                 = GwtServerHelper.getCurrentUser();
			UserProperties	userFolderProperties = bs.getProfileModule().getUserProperties(user.getId(), folderInfo.getBinderIdAsLong());
			String          propKey              = (ObjectKeys.USER_PROPERTY_COLUMN_WIDTHS + (folderInfo.isBinderTrash() ? ".Trash" : ""));
			Map<String, String> columnWidths = ((Map<String, String>) userFolderProperties.getProperty(propKey));
			if ((null != columnWidths) && columnWidths.isEmpty()) {
				columnWidths = null;
			}
			
			// ...and return a ColumnWidthsRpcResponseData containing
			// ...them.
			return new ColumnWidthsRpcResponseData(columnWidths);
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.getColumnWidths( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}

	/*
	 * Use Spring to access a CoreDao object. 
	 */
	private static CoreDao getCoreDao() {
		return ((CoreDao) SpringContextUtil.getBean("coreDao"));
	}
	
	/**
	 * Returns the entry description from a search results Map.
	 * 
	 * @param httpReq
	 * @param entryMap
	 * @param de
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String getEntryDescriptionFromMap(HttpServletRequest httpReq, Map entryMap, DefinableEntity de) {
		String reply = GwtServerHelper.getStringFromEntryMap(entryMap, Constants.DESC_FIELD);
		if (MiscUtil.hasString(reply)) {
			if (null == de)
			     reply = MarkupUtil.markupStringReplacement(null, null, httpReq, null, entryMap, reply, WebKeys.MARKUP_VIEW, false);
			else reply = MarkupUtil.markupStringReplacement(null, null, httpReq, null, de,       reply, WebKeys.MARKUP_VIEW       );
			reply = MarkupUtil.markupSectionsReplacement(reply);
		}
		return reply;
	}
	
	@SuppressWarnings("unchecked")
	public static String getEntryDescriptionFromMap(HttpServletRequest httpReq, Map entryMap) {
		// Always use the initial form of the method.
		return getEntryDescriptionFromMap(httpReq, entryMap, null);	// null -> No DefinableEntity.
	}

	/**
	 * Returns the collected entry types defined for a collection of binders.
	 *
	 * @param bs
	 * @param request
	 * @param entityId
	 * @param binderIds
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static EntryTypesRpcResponseData getEntryTypes(AllModulesInjected bs, HttpServletRequest request, EntityId entityId, List<Long> binderIds) throws GwtTeamingException {
		try {
			// Allocate an EntryTypesRpcResponseData to track the entry
			// types for the requested binders.
			EntryTypesRpcResponseData reply = new EntryTypesRpcResponseData();
			
			// Scan the binders whose entry types are being requested.
			for (Long binderId:  binderIds) {
				// Scan this binder's entry definitions...
				SortedMap<String, Definition> binderDefs = DefinitionHelper.getAvailableDefinitions(binderId, Definition.FOLDER_ENTRY);
				for (String defKey:  binderDefs.keySet()) {
					// ...adding an EntryType for each unique one to
					// ...the reply.
					Definition binderDef = binderDefs.get(defKey);
					String defId = binderDef.getId();
					if (!(reply.isEntryTypeInList(defId))) {
						boolean localDef = ((-1) != binderDef.getBinderId());
						EntryType et = new EntryType(defId, defKey, localDef);
						reply.addEntryType(et);
					}
				}
			}

			// Was the entry type of a specific entry requested?
			if (null != entityId) {
				// Yes!  Get its definition ID...
				FolderEntry fe = bs.getFolderModule().getEntry(entityId.getBinderId(), entityId.getEntityId());
				String feDefId = fe.getEntryDefId();
				
				// ...can the definition IDs we found...
				for (EntryType et:  reply.getEntryTypes()) {
					// ...and when one matches...
					if (feDefId.equals(et.getDefId())) {
						// ...use its EntryType for the requested entry.
						reply.setBaseEntryType( et           );
						reply.setBaseEntryTitle(fe.getTitle());
						break;
					}
				}
			}

			// If we get here, reply refers to the
			// EntryTypesRpcResponseData of the entry types for the
			// requested binders.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.getEntryTypes( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}

	/**
	 * Returns a FileConflictsInfoRpcResponseData object containing
	 * information for rendering conflicts information in a dialog.
	 * 
	 * @param bs
	 * @param request
	 * @param folderInfo
	 * @param fileConflicts
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static FileConflictsInfoRpcResponseData getFileConflictsInfo(AllModulesInjected bs, HttpServletRequest request, BinderInfo folderInfo, List<UploadInfo> fileConflicts) throws GwtTeamingException {
		try {
			// Allocate a FileConflictsInfoRpcResponseData to return
			// the information in.
			FileConflictsInfoRpcResponseData reply = new FileConflictsInfoRpcResponseData();

			// Add a DisplayInfo for the folder to the reply.
			Folder folder = bs.getFolderModule().getFolder(folderInfo.getBinderIdAsLong());
			String name = folder.getTitle();
			DisplayInfo di = new DisplayInfo(
				(MiscUtil.hasString(name) ? name : ("--" + NLT.get("entry.noTitle") + "--")),
				folder.getPathName(),
				folder.getIconName(IconSize.MEDIUM));
			reply.setFolderDisplay(di);

			// If we have some file conflicts...
			if (MiscUtil.hasItems(fileConflicts)) {
				// ...scan them...
				for (UploadInfo fileConflict:  fileConflicts) {
					// ...for for those that represent a file...
					if (fileConflict.isFile()) {
						// ...add a DisplayInfo for them to the reply.
						di = new DisplayInfo(
							fileConflict.getName(),
							"",	// Don't need a path for files.
							FileIconsHelper.getFileIconFromFileName(
								fileConflict.getName(),
								IconSize.SMALL));
						reply.addFileConflictDisplay(di);
					}
				}
			}
			
			// If we get here, reply refers to the
			// FileConflictsInfoRpcResponseData with the information
			// for running the conflicts dialog.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.getFileConflictsInfo( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
		
	}

	/*
	 * Returns a String[] of the names of the columns to display in a
	 * given folder type.
	 * 
	 * Uses the default as setup in folder_column_defaults.jsp.
	 */
	private static String[] getFolderColumnNames(FolderType ft) {
		String[] reply;
		switch (ft) {
		case FILE:       reply = pruneColumnNames(ft, "title", "comments",    "size",     "state",  "author", "date"           ); break;
		case GUESTBOOK:  reply = pruneColumnNames(ft, "guest", "title",       "date",     "descriptionHtml"                    ); break;
		case MILESTONE:  reply = pruneColumnNames(ft, "title", "responsible", "tasks",    "status", "dueDate"                  ); break;
		case MINIBLOG:   reply = pruneColumnNames(ft, "title", "description"                                                   ); break;
		case SURVEY:     reply = pruneColumnNames(ft, "title", "author",      "dueDate"                                        ); break;
		default:         reply = pruneColumnNames(ft, "number", "title",      "comments", "state",  "author", "date",  "rating"); break;
		}
		return reply;
	}
	
	/**
	 * Reads the current user's columns for a folder and returns them
	 * as a FolderColumnsRpcResponseData.
	 * 
	 * The algorithm used in this method was reverse engineered from
	 * that used by folder_view_common2.jsp or view_trash.jsp.
	 * 
	 * @param bs
	 * @param request
	 * @param folderInfo
	 * 
	 * @return
	 */
	public static FolderColumnsRpcResponseData getFolderColumns(AllModulesInjected bs, HttpServletRequest request, BinderInfo folderInfo) throws GwtTeamingException {
		return getFolderColumns(bs, request, folderInfo, Boolean.FALSE);
	}
	
	@SuppressWarnings("unchecked")
	public static FolderColumnsRpcResponseData getFolderColumns(AllModulesInjected bs, HttpServletRequest request, BinderInfo folderInfo, Boolean includeConfigurationInfo) throws GwtTeamingException {
		try {
			Long			folderId             = folderInfo.getBinderIdAsLong();
			Binder			binder               = bs.getBinderModule().getBinder(folderId);
			Folder			folder               = ((binder instanceof Folder) ? ((Folder) binder) : null);
			User			user                 = GwtServerHelper.getCurrentUser();
			UserProperties	userFolderProperties = bs.getProfileModule().getUserProperties(user.getId(), folderId);
			
			Map    columnNames;
			Map    columnTitles      = null;
			String columnOrderString = null;
			List columnsAll = new ArrayList();

			// Are we showing the trash on this folder?
			String baseNameKey;
			boolean isCollection = folderInfo.isBinderCollection();
			boolean isTrash      = folderInfo.isBinderTrash();
			if (isTrash) {
				// Yes!  The columns in a trash view are not
				// configurable.  Use the default trash columns.
				baseNameKey = "trash.column.";
				columnNames = getColumnsLHMFromAS(TrashHelper.trashColumns);
			}

			// No, we aren't showing the trash on this folder!  Are we
			// looking at the root profiles binder? 
			else if (folderInfo.isBinderProfilesRootWS()) {
				// Yes!
				baseNameKey = "profiles.column.";
				if (folderInfo.isBinderProfilesRootWSManagement())
				     columnNames = getColumnsLHMFromAS(new String[]{"fullName", "userType", "emailAddress", "loginId"});
				else columnNames = getColumnsLHMFromAS(new String[]{"fullName",             "emailAddress", "loginId"});
			}
			
			// No, we aren't showing the root profiles binder
			// either!  Are we viewing a collection?
			else if (isCollection) {
				// Yes!  Generate the base key to use for accessing
				// column name in the string resource...
				baseNameKey = "collections.column.";
				CollectionType	collectionType = folderInfo.getCollectionType();
				switch (collectionType) {
				default:
				case MY_FILES:        baseNameKey += "myfiles.";      break;
				case NET_FOLDERS:     baseNameKey += "netfolders.";   break;
				case SHARED_BY_ME:    baseNameKey += "sharedByMe.";   break;
				case SHARED_WITH_ME:  baseNameKey += "sharedWithMe."; break;
				}
				
				// ...and generate the column names for the collection.
				columnNames =
					getColumnsLHMFromAS(
						getCollectionColumnNames(
							collectionType));
			}
			
			else {
				// No, we aren't viewing a collection either!  If we
				// weren't given a folder...
				if (null == folder) {
					// ...we can't do anything with it.
            		throw
            			new GwtTeamingException(
            				GwtTeamingException.ExceptionType.FOLDER_EXPECTED,
            				"GwtViewHelper.getFolderColumns( *Internal Error* ):  The ID could not be resolved to a folder.");
				}
				
				// Are there user defined columns on this folder?
				FolderType folderType = folderInfo.getFolderType();
				switch (folderType) {
				case GUESTBOOK:  baseNameKey = "guestbook.column."; break;
				case MILESTONE:  baseNameKey = "milestone.";        break;
				case MINIBLOG:   baseNameKey = "miniblog.column.";  break;
				case SURVEY:     baseNameKey = "survey.";           break;
				default:         baseNameKey = "folder.column.";    break;
				}
				columnNames = ((Map) userFolderProperties.getProperty(ObjectKeys.USER_PROPERTY_FOLDER_COLUMNS));
				if (null == columnNames) {
					// No!  Are there defaults stored on the binder?
					columnNames = ((Map) folder.getProperty(ObjectKeys.BINDER_PROPERTY_FOLDER_COLUMNS));
					if (null == columnNames) {
						// No!  Generate a list of default column names
						// for the folder.
						columnNames =
							getColumnsLHMFromAS(
								getFolderColumnNames(
									folderType));
					}
					
					else {
						// Yes, there are defaults from the binder!
						// Read and names and sort order from there as
						// well.
						columnTitles      = ((Map)    folder.getProperty(ObjectKeys.BINDER_PROPERTY_FOLDER_COLUMN_TITLES    ));
						columnOrderString = ((String) folder.getProperty(ObjectKeys.BINDER_PROPERTY_FOLDER_COLUMN_SORT_ORDER));
					}
				}
				
				else {
					// Yes, there are user defined columns on the
					// folder!  Read and names and sort order from
					// there as well.
					columnTitles      = ((Map)    userFolderProperties.getProperty(ObjectKeys.USER_PROPERTY_FOLDER_COLUMN_TITLES    ));
					columnOrderString = ((String) userFolderProperties.getProperty(ObjectKeys.USER_PROPERTY_FOLDER_COLUMN_SORT_ORDER));
				}
			}

			// If we don't have any column names...
			if (null == columnTitles) {
				// ...just use an empty map.
				columnTitles = new HashMap();
			}
			
			// If we don't have any column sort order...
			if (!(MiscUtil.hasString(columnOrderString))) {
				// ...define one based on the column names.
				Set<String> keySet = columnNames.keySet();
				boolean firstCol = true;
				StringBuffer sb = new StringBuffer("");
				for (Iterator<String> ksIT = keySet.iterator(); ksIT.hasNext(); ) {
					if (!firstCol) {
						sb.append("|");
					}
					sb.append(ksIT.next());
					firstCol = false;
				}
				columnOrderString = sb.toString();
			}

			// Finally, generate a List<String> from the raw column
			// order string...
			List<String> columnSortOrder = new ArrayList<String>();
			String[] sortOrder = columnOrderString.split("\\|");
			for (String columnName:  sortOrder) {
				if (MiscUtil.hasString(columnName)) {
					columnSortOrder.add(columnName);
				}
			}
			
			// ...and ensure all the columns are accounted for in it.
			Set<String> keySet = columnNames.keySet();
			for (Iterator<String> ksIT = keySet.iterator(); ksIT.hasNext(); ) {
				String columnName = ksIT.next();
				if (!(columnSortOrder.contains(columnName))) {
					columnSortOrder.add(columnName);
				}
			}

			// If we get here, we've got all the data we need to define
			// the List<FolderColumn> for this folder.  Allocate the
			// list that we can fill from that data.
			List<FolderColumn> fcList = new ArrayList<FolderColumn>();
			List<FolderColumn> fcListAll = new ArrayList<FolderColumn>();
			for (String colName:  columnSortOrder) {
				if (!columnsAll.contains(colName)) {
					FolderColumn fc = new FolderColumn(colName);
					if (colName.contains(",")) {
						String[] cnParts = colName.split(",");
						fc.setColumnDefId(cnParts[0]);
						fc.setColumnType(cnParts[1]);
						fc.setColumnEleName(cnParts[2]);
						String caption = colName.substring(colName.indexOf(",")+1);
						caption = caption.substring(caption.indexOf(",")+1);
						caption = caption.substring(caption.indexOf(",")+1);
						fc.setColumnDefaultTitle(caption);
					}
					// Is this column is not to be shown, skip it.
					// No!  Skip it.
					String columnValue = ((String) columnNames.get(colName));
					if (!(MiscUtil.hasString(columnValue)))
					     fc.setColumnShown(Boolean.FALSE);
					else fc.setColumnShown(Boolean.TRUE );
	
					// Is there a custom title for this column?
					String colTitle = ((String) columnTitles.get(colName));
					String colTitleDefault = "";
					if (!MiscUtil.hasString(fc.getColumnDefId())) {
						colTitleDefault = NLT.get(
							(baseNameKey + colName),	// Key to find the resource.
							colName,					// Default if not defined.
							true);						// true -> Silent.  Don't generate an error if undefined.
					} else {
						colTitleDefault = fc.getColumnDefaultTitle();
					}
					fc.setColumnDefaultTitle(colTitleDefault);
					if (!(MiscUtil.hasString(colTitle))) {
						// There is no custom title,  use the default.
						colTitle = colTitleDefault;
					} else {
						fc.setColumnCustomTitle(colTitle);
					}
					fc.setColumnTitle(colTitle);
	
					// Add a FolderColumn for this to the list we're
					// going to return.
					if (fc.isColumnShown()) {
						// This column is being shown.
						fcList.add(fc);
					}
					fcListAll.add(fc);
					columnsAll.add(colName);
				}
			}

			// Walk the List<FolderColumn>'s performing fixups on each
			// as necessary.
			fixupFCs(fcList, isTrash, isCollection);
			

			if (includeConfigurationInfo && (!isTrash) && (!isCollection)) {
				//Build a list of all possible columns
				Map<String,Definition> entryDefs = DefinitionHelper.getEntryDefsAsMap(((Folder) bs.getBinderModule().getBinder(folderId)));
				for (Definition def :  entryDefs.values()) {
					@SuppressWarnings("unused")
					Document defDoc = def.getDefinition();
					Map<String,Map> elementData = bs.getDefinitionModule().getEntryDefinitionElements(def.getId());
					for (Map.Entry me : elementData.entrySet()) {
						String eleName = (String)me.getKey();
						String type = (String)((Map)me.getValue()).get("type");
						String caption = (String)((Map)me.getValue()).get("caption");
						String colName = def.getId()+","+type+","+eleName+","+caption;
						if (!columnsAll.contains(colName)) {
							if (type.equals(    "selectbox"        ) ||
									type.equals("radio"            ) ||
									type.equals("checkbox"         ) ||
									type.equals("date"             ) ||
									type.equals("date_time"        ) ||
									type.equals("event"            ) ||
									type.equals("text"             ) ||
									type.equals("number"           ) ||
									type.equals("url"              ) ||
									type.equals("hidden"           ) ||
									type.equals("user_list"        ) ||
									type.equals("userListSelectbox")) {
								FolderColumn fc = new FolderColumn(colName);
								fc.setColumnDefId(def.getId());
								fc.setColumnType(type);
								// Is this column to be shown?
								String columnValue = ((String) columnNames.get(colName));
								if (!(MiscUtil.hasString(columnValue)))
								     fc.setColumnShown(Boolean.FALSE);
								else fc.setColumnShown(Boolean.TRUE );
			
								// Is there a custom title for this column?
								String colTitleDefault = (String)((Map)me.getValue()).get("caption");
								if (!(MiscUtil.hasString(fc.getColumnDefId()))) {
									colTitleDefault = NLT.get(
										(baseNameKey + colName),	// Key to find the resource.
										colTitleDefault,			// Default if not defined.
										true);						// true -> Silent.  Don't generate an error if undefined.
								}
								fc.setColumnDefaultTitle(colTitleDefault);
								String colTitle = (String) columnTitles.get(colName);
								if (!(MiscUtil.hasString(colTitle)))
								     // There is no custom title,  use the default.
								     colTitle = colTitleDefault;
								else fc.setColumnCustomTitle(colTitle);
								fc.setColumnTitle(colTitle);
			
								// Add a FolderColumn for this to the
								// list of all columns if it isn't
								// already there.
								fcListAll.add(fc);
								columnsAll.add(colName);
							}
						}
					}
				}
			}

			// Finally, use the data we obtained to create a
			// FolderColumnsRpcResponseData and return that. 
			return new FolderColumnsRpcResponseData(fcList, fcListAll);
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.getFolderColumns( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}

	/**
	 * Reads the current user's display data for a folder and returns
	 * them as a FolderDisplayDataRpcResponseData.
	 * 
	 * @param bs
	 * @param request
	 * @param folderInfo
	 * 
	 * @return
	 */
	public static FolderDisplayDataRpcResponseData getFolderDisplayData(AllModulesInjected bs, HttpServletRequest request, BinderInfo folderInfo) throws GwtTeamingException {
		try {
			Long			folderId             = folderInfo.getBinderIdAsLong();
			User			user                 = GwtServerHelper.getCurrentUser();
			Long			userId               = user.getId();
			UserProperties	userProperties       = bs.getProfileModule().getUserProperties(userId);
			UserProperties	userFolderProperties = bs.getProfileModule().getUserProperties(userId, folderId);
			
			// Allow for collection sort information being stored on
			// the same binder.
			String propSortBy      = ObjectKeys.SEARCH_SORT_BY;
			String propSortDescend = ObjectKeys.SEARCH_SORT_DESCEND;
			if (folderInfo.isBinderCollection()) {
				String cName     = ("." + String.valueOf(folderInfo.getCollectionType().ordinal()));
				propSortBy      += cName;
				propSortDescend += cName;
			}
			
			// How should the folder be sorted?
			String	sortBy = ((String) userFolderProperties.getProperty(propSortBy));
			boolean sortDescend;
			if (MiscUtil.hasString(sortBy)) {
				String sortDescendS = ((String) userFolderProperties.getProperty(propSortDescend));
				sortDescend = (("true").equalsIgnoreCase(sortDescendS));
			}
			else {
				sortDescend = false;
				if (folderInfo.isBinderProfilesRootWS() || folderInfo.isBinderCollection()) {
					sortBy = Constants.SORT_TITLE_FIELD;
				}
				else {
					switch (folderInfo.getFolderType()) {
					case FILE:
					case MILESTONE:
					case MINIBLOG:
					case SURVEY:     sortBy = Constants.SORT_TITLE_FIELD;                     break;
					case TASK:       sortBy = Constants.SORT_ORDER_FIELD;                     break;
					case GUESTBOOK:  sortBy = Constants.CREATOR_TITLE_FIELD;                  break;
					default:         sortBy = Constants.SORTNUMBER_FIELD; sortDescend = true; break;
					}
				}
			}

			// How many entries per page should the folder display?
			int pageSize;
			try                  {pageSize = Integer.parseInt(MiscUtil.entriesPerPage(userProperties));}
			catch (Exception ex) {pageSize = 25;                                                       }
			
			// Has the user defined any column widths on this folder?
			ColumnWidthsRpcResponseData cwData = getColumnWidths(bs, request, folderInfo);

			// What do we know about pinning of entries on this folder?
			boolean folderSupportsPinning = getFolderSupportsPinning(folderInfo);
			boolean viewPinnedEntries     = (folderSupportsPinning && getUserViewPinnedEntries(request, folderId));

			// What do we know about the view state on the
			// 'Shared by/with Me' collections?
			boolean viewSharedFiles;
			CollectionType collectionType = folderInfo.getCollectionType();
			switch (collectionType) {
			case SHARED_BY_ME:
			case SHARED_WITH_ME:  viewSharedFiles = getUserViewSharedFiles(request, collectionType); break;
			default:              viewSharedFiles = false;                                           break;
			}

			// Does the current user own this folder?
			boolean folderOwnedByCurrentUser; 
			try {
				Folder f = bs.getFolderModule().getFolder(folderId);
				folderOwnedByCurrentUser = f.getOwnerId().equals(userId);
			}
			catch (Exception ex) {
				folderOwnedByCurrentUser = false;
			}

			// Finally, use the data we obtained to create a
			// FolderDisplayDataRpcResponseData and return that. 
			return
				new FolderDisplayDataRpcResponseData(
					sortBy,
					sortDescend,
					pageSize,
					cwData.getColumnWidths(),
					folderSupportsPinning,
					viewPinnedEntries,
					viewSharedFiles,
					folderOwnedByCurrentUser);
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.getFolderDisplayData( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}
	
	/**
	 * Constructs and returns a FolderEntryDetails that wraps the given
	 * entityId.
	 * 
	 * @param bs
	 * @param request
	 * @param entityId
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static FolderEntryDetails getFolderEntryDetails(AllModulesInjected bs, HttpServletRequest request, EntityId entityId) throws GwtTeamingException {
		SimpleProfiler.start("GwtViewHelper.getFolderEntryDetails()");
		try {
			// Create the ViewFolderEntryInfo to return...
			Long				userId = GwtServerHelper.getCurrentUserId();
			FolderEntryDetails	reply  = new FolderEntryDetails(entityId);
			
			// ...set whether the user has seen this entry...
			Long			folderId = entityId.getBinderId();
			FolderModule	fm       = bs.getFolderModule();
			FolderEntry 	fe       = fm.getEntry(folderId, entityId.getEntityId());
			SeenMap			seenMap  = bs.getProfileModule().getUserSeenMap(userId);
			reply.setSeen(seenMap.checkIfSeen(fe));
			
			// ...set the entry's family and path... 
			FolderEntry feTop = fe.getTopEntry();
			boolean		isTop = (null == feTop);
			if (isTop) {
				feTop = fe;
			}
			reply.setTop(   isTop                                           );
			reply.setFamily(GwtServerHelper.getFolderEntityFamily(bs, feTop));
			reply.setPath(  feTop.getParentBinder().getPathName()           );
	
			// ...set the entry's creator...
			HistoryStamp cStamp = fe.getCreation();
			reply.setCreator(buildFolderEntryUser(bs, request, cStamp));
			
			// ...set the entry's modifier...
			HistoryStamp mStamp = fe.getModification();
			reply.setModifier(buildFolderEntryUser(bs, request, mStamp));
			reply.setModifierIsCreator(cStamp.getPrincipal().getId().equals(mStamp.getPrincipal().getId()));
			
			// ...set the entry's locker...
			HistoryStamp lStamp = fe.getReservation();
			reply.setLocker(buildFolderEntryUser(bs, request, lStamp));
			reply.setLockedByLoggedInUser((null != lStamp) && lStamp.getPrincipal().getId().equals(userId));

			// ...set the contributor's to this entry...
			reply.setContributors(ListFolderHelper.collectContributorIds(fe));
			
			// ...set the entry's title... 
			String feTitle = fe.getTitle();
			if (!(MiscUtil.hasString(feTitle))) {
				feTitle = ("--" + NLT.get("entry.noTitle") + "--");
			}
			reply.setTitle(feTitle);
			
			// ...set information about the entry's comments...
			CommentsInfo ci = new CommentsInfo(
				entityId,
				feTitle,
				(isTop                      ?
					fe.getTotalReplyCount() :	// For top level entries, we show all the replies.
					fe.getReplyCount()),		// For replies themselves, we only show their direct replies.
				fm.testAccess(fe, FolderOperation.addReply));
			reply.setComments(ci);

			// ...if this is a comment...
			if (!isTop) {
				// ...store a List<ViewFolderEntryInfo> for the bread
				// ...crumb links leading up to it...
				List<ViewFolderEntryInfo> peBCList = new ArrayList<ViewFolderEntryInfo>();
				FolderEntry pe = fe.getParentEntry();
				while (null != pe) {
					peBCList.add(0, buildViewFolderEntryInfo(bs, request, folderId, pe.getId()));
					pe = pe.getParentEntry();
				}
				reply.setCommentBreadCrumbs(peBCList);
			}
	
			// ...if this is a file entry with a filename...
			FileAttachment faForEntryIcon;
			FileAttachment fa = GwtServerHelper.getFileEntrysFileAttachment(bs, fe, isTop);
			if (null != fa) {
				// ...store it for using for the entry icon...
				faForEntryIcon = fa;
				
				// ...and set the ViewFileInfo for an HTML view of the
				// ...file if it supports it...
				ViewFileInfo vfi = buildViewFileInfo(request, fe, fa);
				if (null == vfi)
				     reply.setContentIsImage(contentIsImage(bs, request, fe, fa));
				else reply.setHtmlView(vfi);
				
				reply.setDownloadUrl(
					GwtServerHelper.getDownloadFileUrl(
						request,
						bs,
						fe.getParentBinder().getId(),
						fe.getId())); 
			}
			
			else {
				// ...if the entry itself didn't have a file for an
				// ...entry icon, try its top most parent...
				faForEntryIcon = GwtServerHelper.getFileEntrysFileAttachment(bs, feTop);
			}

			// ...if we have a FileAttachment with a file to use for
			// ...the entry icon...
			if (null != faForEntryIcon) {
				// ...store the icons for that file...
				String fName = faForEntryIcon.getFileItem().getName();
				reply.setEntryIcon(FileIconsHelper.getFileIconFromFileName(fName, mapBISToIS(BinderIconSize.SMALL)),  BinderIconSize.SMALL );
				reply.setEntryIcon(FileIconsHelper.getFileIconFromFileName(fName, mapBISToIS(BinderIconSize.MEDIUM)), BinderIconSize.MEDIUM);
				reply.setEntryIcon(FileIconsHelper.getFileIconFromFileName(fName, mapBISToIS(BinderIconSize.LARGE)),  BinderIconSize.LARGE );
			}
	
			// ...set the entry's description...
			Description	feDesc = fe.getDescription();
			if (null != feDesc) {
				boolean feDescHtml = (Description.FORMAT_HTML == feDesc.getFormat());
				reply.setDescIsHtml(feDescHtml              );
				reply.setDescTxt(   feDesc.getStrippedText());
				
				String desc = feDesc.getText();
				if (MiscUtil.hasString(desc)) {
					desc = MarkupUtil.markupStringReplacement(
						null,
						null,
						request,
						null,
						fe,
						desc,
						WebKeys.MARKUP_VIEW);
				}
				reply.setDesc(desc);
			}
			else {
				reply.setDescIsHtml(true);
				reply.setDesc(      ""  );
				reply.setDescTxt(   ""  );
			}
			
			// ...and finally, set the view's toolbar items.
			reply.setToolbarItems(GwtMenuHelper.getViewEntryToolbarItems(bs, request, fe));
			
			// If we get here, reply refers to the ViewFolderEntryInfo
			// for the user to view the entry.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.getFolderEntryDetails( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
		
		finally {
			SimpleProfiler.stop("GwtViewHelper.getFolderEntryDetails()");
		}
	}

	/**
	 * Reads the row data from a folder and returns it as a
	 * FolderRowsRpcResponseData.
	 * 
	 * @param bs
	 * @param request
	 * @param folderInfo
	 * @param folderColumns
	 * @param start
	 * @param length
	 * @param quickFilter
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static FolderRowsRpcResponseData getFolderRows(AllModulesInjected bs, HttpServletRequest request, BinderInfo folderInfo, List<FolderColumn> folderColumns, int start, int length, String quickFilter) throws GwtTeamingException {
		try {
			// Is this a binder the user can view?
			if (!(GwtServerHelper.canUserViewBinder(bs, folderInfo))) {
				// No!  Return an empty set of rows.
				return
					new FolderRowsRpcResponseData(
						new ArrayList<FolderRow>(),	// FolderRows.
						0,							// Start index.
						0,							// Total count.
						new ArrayList<Long>());		// Contributor IDs.
			}
			
			// Access the binder/folder.
			Long	folderId = folderInfo.getBinderIdAsLong();
			Binder	binder   = bs.getBinderModule().getBinder(folderId);
			Folder	folder   = ((binder instanceof Folder) ? ((Folder) binder) : null);
			boolean	isFolder = (null != folder);
			
			// If we're reading from a mirrored file folder...
			if (FolderType.MIRROREDFILE == folderInfo.getFolderType()) {
				// ...whose driver is not configured...
				String rdn = binder.getResourceDriverName();
				if (!(MiscUtil.hasString(rdn))) {
					// ...we don't read anything.
					return
						new FolderRowsRpcResponseData(
							new ArrayList<FolderRow>(),
							start,
							0,
							new ArrayList<Long>());
				}
			}
			
			// What type of folder are we dealing with?
			boolean isGuestbook      = false;
			boolean isMilestone      = false;
			boolean isSurvey         = false;
			boolean isProfilesRootWS = folderInfo.isBinderProfilesRootWS();
			boolean isTrash          = folderInfo.isBinderTrash();
			switch (folderInfo.getFolderType()) {
			case GUESTBOOK:  isGuestbook = true; break;
			case MILESTONE:  isMilestone = true; break;
			case SURVEY:     isSurvey    = true; break;
			}

			// Access any other information we need to read the data.
			User			user                 = GwtServerHelper.getCurrentUser();
			UserProperties	userFolderProperties = bs.getProfileModule().getUserProperties(user.getId(), folderId);
			SeenMap			seenMap              = bs.getProfileModule().getUserSeenMap(null);

			// Setup the current search filter the user has selected
			// on the folder.
			Map options;
			if (isFolder)
			     options = getFolderSearchFilter(bs, folder, userFolderProperties, null);
			else options = new HashMap();
			GwtServerHelper.addQuickFilterToSearch(options, quickFilter, folderInfo.isBinderProfilesRootWS());
			options.put(ObjectKeys.SEARCH_OFFSET,   start );
			options.put(ObjectKeys.SEARCH_MAX_HITS, length);

			// Are we populating the profiles root binder?
			boolean isManageUsers = false;
			if (isProfilesRootWS) {
				// Yes!  Is it for the manage users feature of the
				// administration console?
				isManageUsers = folderInfo.isBinderProfilesRootWSManagement();
				if (isManageUsers) {
					// Yes!  If the filters are such that we wouldn't
					// get any results...
					ManageUsersState mus = GwtServerHelper.getManageUsersState(bs, request).getManageUsersState();
					boolean disabled = mus.isShowDisabled();
					boolean enabled  = mus.isShowEnabled();
					boolean external = mus.isShowExternal();
					boolean internal = mus.isShowInternal();
					if (((!external) && (!internal)) ||
						((!enabled)  && (!disabled))) {
						// ...simply return an empty list.
						return
							new FolderRowsRpcResponseData(
								new ArrayList<FolderRow>(),
								start,
								0,
								new ArrayList<Long>());
						
					}
					
					// Apply the internal/external filtering...
					if      (internal && external) /* Default includes both.*/ ;
					else if (internal)             options.put(ObjectKeys.SEARCH_IS_INTERNAL,       Boolean.TRUE);
					else if (external)             options.put(ObjectKeys.SEARCH_IS_EXTERNAL,       Boolean.TRUE);

					// ...and apply the enabled/disabled filtering.
					if      (enabled  && disabled) /* Default includes both. */ ;
					else if (enabled)              options.put(ObjectKeys.SEARCH_IS_ENABLED_USERS,  Boolean.TRUE);
					else if (disabled)             options.put(ObjectKeys.SEARCH_IS_DISABLED_USERS, Boolean.TRUE);
				}
				
				else {
					// No, it isn't for the manage users feature of the
					// administration console!  Eliminate the
					// non-person, external and disabled users.
					options.put(ObjectKeys.SEARCH_IS_PERSON,        Boolean.TRUE);
					options.put(ObjectKeys.SEARCH_IS_INTERNAL,      Boolean.TRUE);
					options.put(ObjectKeys.SEARCH_IS_ENABLED_USERS, Boolean.TRUE);
				}
			}

			// Factor in the user's sorting selection.
			FolderDisplayDataRpcResponseData fdd = getFolderDisplayData(bs, request, folderInfo);
			String	sortBy      = fdd.getFolderSortBy();
			boolean	sortDescend = fdd.getFolderSortDescend();
			options.put(ObjectKeys.SEARCH_SORT_BY,      sortBy     );
			options.put(ObjectKeys.SEARCH_SORT_DESCEND, sortDescend);

			// What do we know about pinning of entries on this folder?
			boolean folderSupportsPinning = getFolderSupportsPinning(folderInfo);
			boolean viewPinnedEntries     = (folderSupportsPinning && getUserViewPinnedEntries(request, folderId));
			
			// Does this folder support pinning?
			List<Map>  pinnedEntrySearchMaps;
			List<Long> pinnedEntryIds = new ArrayList<Long>();
			if (folderSupportsPinning) {
				// Yes!  Are there any entries pinned in the folder?
				pinnedEntrySearchMaps = getPinnedEntries(
					bs,
					folder,
					userFolderProperties,
					pinnedEntryIds,
					viewPinnedEntries);
			}
			else {
				// No, this folder doesn't support pinning!  We don't
				// have (or need) the pinned entry maps.
				pinnedEntrySearchMaps = null;
			}

			// If we're working with a 'Shared by/with Me' collection,
			// get the shared items.
			CollectionType			collectionType           = folderInfo.getCollectionType();
			boolean					isCollection             = folderInfo.isBinderCollection();
			boolean					isCollectionSharedByMe   = (isCollection && CollectionType.SHARED_BY_ME.equals(  collectionType));
			boolean					isCollectionSharedWithMe = (isCollection && CollectionType.SHARED_WITH_ME.equals(collectionType));
			List<GwtSharedMeItem> 	shareItems;
			if      (isCollectionSharedByMe)   shareItems = getSharedByMeItems(  bs, request, sortBy, sortDescend);
			else if (isCollectionSharedWithMe) shareItems = getSharedWithMeItems(bs, request, sortBy, sortDescend);
			else                               shareItems = null;

			// Is the user currently viewing pinned entries?
			List<Map>  searchEntries;
			int        totalRecords;
			if (viewPinnedEntries) {
				// Yes!  Use the pinned entries as the search entries.
				searchEntries = pinnedEntrySearchMaps;
				totalRecords  = searchEntries.size();
			}
			
			else {
				// No, the user isn't currently viewing pinned entries!
				// Read the entries based on a search.
				Map searchResults;
				if      (isTrash)          searchResults = TrashHelper.getTrashEntries(bs, binder, options);
				else if (isProfilesRootWS) searchResults = getUserEntries(      bs, request, binder, quickFilter, options                            );
				else if (isCollection)     searchResults = getCollectionEntries(bs, request, binder, quickFilter, options, collectionType, shareItems);
				else {
					options.put(ObjectKeys.SEARCH_INCLUDE_NESTED_BINDERS, Boolean.TRUE);
					options.put(ObjectKeys.SEARCH_SORT_BY,                Constants.ENTITY_FIELD);
					options.put(ObjectKeys.SEARCH_SORT_DESCEND,           sortDescend           );
					options.put(ObjectKeys.SEARCH_SORT_BY_SECONDARY,      fdd.getFolderSortBy() );
					options.put(ObjectKeys.SEARCH_SORT_DESCEND_SECONDARY, sortDescend           );
					searchResults = bs.getFolderModule().getEntries(folderId, options);
				}
				searchEntries = ((List<Map>) searchResults.get(ObjectKeys.SEARCH_ENTRIES    ));
				totalRecords  = ((Integer)   searchResults.get(ObjectKeys.SEARCH_COUNT_TOTAL)).intValue();
			}

			// Scan the entries we read.
			boolean         addedAssignments = false;
			List<FolderRow> folderRows       = new ArrayList<FolderRow>();
			List<Long>      contributorIds   = new ArrayList<Long>();
			for (Map entryMap:  searchEntries) {
				// Is this an entry or folder?
				String  entityType          = GwtServerHelper.getStringFromEntryMap(entryMap, Constants.ENTITY_FIELD);
				boolean isEntityFolderEntry = EntityType.folderEntry.name().equals(entityType);
				
				String locationBinderId = GwtServerHelper.getStringFromEntryMap(entryMap, Constants.BINDER_ID_FIELD);
				if (!(MiscUtil.hasString(locationBinderId))) {
					locationBinderId = GwtServerHelper.getStringFromEntryMap(entryMap, Constants.BINDERS_PARENT_ID_FIELD);
				}
				
				// Have we already process this entry's ID?
				String   docIdS   = GwtServerHelper.getStringFromEntryMap(entryMap, Constants.DOCID_FIELD);
				Long     docId    = Long.parseLong(docIdS);
				EntityId entityId = new EntityId(Long.parseLong(locationBinderId), docId, entityType);
				if ((!isCollectionSharedWithMe) && isEntityInList(entityId, folderRows)) {
					// Yes!  Skip it now.  Note that we may have
					// duplicates because of pinning.
					continue;
				}
				
				// Extract the contributors from this entry.
				collectContributorIds(entryMap, contributorIds);
				
				// Create a FolderRow for each entry.
				FolderRow fr = new FolderRow(entityId, folderColumns);
				if (isEntityFolderEntry && pinnedEntryIds.contains(entityId.getEntityId())) {
					fr.setPinned(true);
				}
				
				// Scan the columns.
				for (FolderColumn fc:  folderColumns) {
					// Is this a custom column?
					if (fc.isCustomColumn()) {
						// Yes!  Generate a value for it.
						setValueForCustomColumn(bs, entryMap, fr, fc);
					}
					
					else {
						// No, this isn't a custom column!
						String cn      = fc.getColumnName();
						String csk     = fc.getColumnSearchKey();
						Object emValue = GwtServerHelper.getValueFromEntryMap(entryMap, csk);
						if ((null == emValue) && csk.equals(Constants.MODIFICATION_DATE_FIELD)) {
							emValue = GwtServerHelper.getValueFromEntryMap(entryMap, Constants.CREATION_DATE_FIELD);
							if (null != emValue) {
								csk = Constants.CREATION_DATE_FIELD;
							}
						}
						
						// Are we working on a 'Shared by/with Me'
						// collection?
						GwtSharedMeItem smItem;
						if (isCollectionSharedByMe || isCollectionSharedWithMe) {
							// Yes!  Find the GwtSharedMeItem for this row.
							smItem = GwtSharedMeItem.findShareMeInList(
								docId,
								entityType,
								shareItems);
							
							// Is this the sharedBy/With column?
							if (csk.equalsIgnoreCase(FolderColumn.COLUMN_SHARE_SHARED_BY) ||
								csk.equalsIgnoreCase(FolderColumn.COLUMN_SHARE_SHARED_WITH)) {
								// Yes!  Build a
								// List<AssignmentInfo> for this row...
								List<AssignmentInfo> aiList;
								if (null == smItem) {
									aiList = new ArrayList<AssignmentInfo>();
								}
								else {
									if (csk.equalsIgnoreCase(FolderColumn.COLUMN_SHARE_SHARED_BY))
									     aiList = getAIListFromSharers(   smItem.getPerShareInfos());
									else aiList = getAIListFromRecipients(smItem.getPerShareInfos());
								}
								addedAssignments = (!(aiList.isEmpty()));
								fr.setColumnValue_AssignmentInfos(fc, aiList);
	
								// ...and continue with the next
								// ...column.
								continue;
							}
							
							// No, this isn't the sharedBy/With column!
							// Is it the sharedMessage column?
							else if (csk.equalsIgnoreCase(FolderColumn.COLUMN_SHARE_MESSAGE)) {
								// Yes!  Build a
								// List<ShareMessageInfo> for this
								// row...
								List<ShareMessageInfo> smiList;
								if (null == smItem)
								     smiList = new ArrayList<ShareMessageInfo>();
								else smiList = getShareMessageListFromShares(smItem.getPerShareInfos());
								fr.setColumnValue_ShareMessageInfos(fc, smiList);
								
								// ...and continue with the next
								// ...column.
								continue;
							}
							
							// No, this isn't the sharedMessage column
							// either!  Is it the sharedDate column?
							else if (csk.equalsIgnoreCase(FolderColumn.COLUMN_SHARE_DATE)) {
								// Yes!  Build a
								// List<ShareDateInfo> for this row...
								List<ShareDateInfo> sdiList;
								if (null == smItem)
								     sdiList = new ArrayList<ShareDateInfo>();
								else sdiList = getShareDateListFromShares(smItem.getPerShareInfos());
								fr.setColumnValue_ShareDateInfos(fc, sdiList);
								
								// ...and continue with the next
								// ...column.
								continue;
							}
							
							// No, this isn't the sharedDate column
							// either!  Is it the sharedExpiration
							// column?
							else if (csk.equalsIgnoreCase(FolderColumn.COLUMN_SHARE_EXPIRATION)) {
								// Yes!  Build a
								// List<ShareExpirationInfo> for this
								// row...
								List<ShareExpirationInfo> seiList;
								if (null == smItem)
								     seiList = new ArrayList<ShareExpirationInfo>();
								else seiList = getShareExpirationListFromShares(smItem.getPerShareInfos());
								fr.setColumnValue_ShareExpirationInfos(fc, seiList);
								
								// ...and continue with the next
								// ...column.
								continue;
							}
							
							// No, this isn't the sharedExpiration
							// column either!  Is it the sharedAccess
							// column?
							else if (csk.equalsIgnoreCase(FolderColumn.COLUMN_SHARE_ACCESS)) {
								// Yes!  Build a
								// List<ShareAccessInfo> for this
								// row...
								List<ShareAccessInfo> saiList;
								if (null == smItem)
								     saiList = new ArrayList<ShareAccessInfo>();
								else saiList = getShareAccessListFromShares(smItem.getPerShareInfos());
								fr.setColumnValue_ShareAccessInfos(fc, saiList);
								
								// ...and continue with the next
								// ...column.
								continue;
							}
						}
						
						else {
							// No, we aren't working on a
							// 'Shared by/with Me' collection!
							smItem = null;
						}
						
						GuestInfo     gi = null;
						PrincipalInfo pi = null;
						if (emValue instanceof Principal) {
							// Yes!  Are we looking at the 'guest'
							// column in a guest book folder?
							Principal p = ((Principal) emValue);
							if (isGuestbook && cn.equals("guest")) {
								// Yes!  If the entity is a folder
								// entry...
								if (isEntityFolderEntry) {
									// ...use the principal to generate
									// ...a GuestInfo for the column...
									gi = getGuestInfoFromPrincipal(bs, request, p);
									fr.setColumnValue(fc, gi);
								}
								else {
									// ...otherwise, don't store a
									// ...value for the column.
									fr.setColumnValue(fc, "");
								}
							}
							
							else {
								// No, we aren't looking at the 
								// guest' column in a guest book
								// folder!  If we can create a
								// PrincipalInfo for the principal...
								pi = getPIFromPId(bs, request, p.getId());
								if (null != pi) {
									// ...store it directly.
									if (pi.isUserPerson() || isManageUsers || (!(Utils.checkIfFilr()))) {
										fr.setColumnValue(fc, pi);
									}
								}
							}
						}
					
						if ((null == pi) && (null == gi)) {
							// No!  Does the column contain assignment
							// information?
							if (AssignmentInfo.isColumnAssigneeInfo(csk)) {
								// Yes!  Read its
								// List<AssignmentInfo>'s.
								AssigneeType ait = AssignmentInfo.getColumnAssigneeType(csk);
								List<AssignmentInfo> assignmentList = GwtEventHelper.getAssignmentInfoListFromEntryMap(entryMap, csk, ait);
								
								// Is this column for an individual
								// assignee?
								if (ait.isIndividual()) {
									// Yes!  If we don't have columns
									// for group or team assignments,
									// factor those in as well.
									factorInGroupAssignments(entryMap, folderColumns, csk, assignmentList);
									factorInTeamAssignments( entryMap, folderColumns, csk, assignmentList);
								}
								
								// Add the column data to the list.
								addedAssignments = true;
								fr.setColumnValue_AssignmentInfos(fc, assignmentList);
							}
							
							// No, the column doesn't contain
							// assignment information either!  Does
							// it contain a collection of task folders?
							else if (csk.equals("tasks")) {
								// Yes!  Create a List<TaskFolderInfo>
								// from the IDs it contains and set
								// that as the column value.
								List<TaskFolderInfo> taskFolderList = GwtServerHelper.getTaskFolderInfoListFromEntryMap(bs, request, entryMap, csk);
								fr.setColumnValue_TaskFolderInfos(fc, taskFolderList);
							}
							
							// No, the column doesn't contain a
							// collection of task folders either!
							// Does it contain an email address?
							else if (csk.equals("emailAddress")) {
								// Yes!  Construct an EmailAddressInfo
								// from the entry map.
								EmailAddressInfo emai = GwtServerHelper.getEmailAddressInfoFromEntryMap(bs, entryMap);
								fr.setColumnValue(fc, emai);
							}
							
							else {
								// No, the column doesn't contain an
								// email address either!  Extract its
								// String value.
								String value = GwtServerHelper.getStringFromEntryMapValue(
									emValue,
									DateFormat.MEDIUM,
									DateFormat.SHORT);
								
								// Are we working on a title field?
								if (csk.equals(Constants.TITLE_FIELD)) {
									// Yes!  Construct an
									// EntryTitleInfo for it.
									EntryTitleInfo  eti = new EntryTitleInfo();
									eti.setHidden((null != smItem) && smItem.isHidden());
									eti.setSeen(isEntityFolderEntry ? seenMap.checkIfSeen(entryMap) : true);
									eti.setTrash(isTrash);
									eti.setTitle(MiscUtil.hasString(value) ? value : ("--" + NLT.get("entry.noTitle") + "--"));
									eti.setEntityId(entityId);
									String description = getEntryDescriptionFromMap(request, entryMap, ((null == smItem) ? null : smItem.getEntity()));
									if (MiscUtil.hasString(description)) {
										eti.setDescription(description);
										String descriptionFormat = GwtServerHelper.getStringFromEntryMap(entryMap, Constants.DESC_FORMAT_FIELD);
										eti.setDescriptionIsHtml(MiscUtil.hasString(descriptionFormat) && descriptionFormat.equals(String.valueOf(Description.FORMAT_HTML)));
									}
									else if (!isEntityFolderEntry) {
										description = GwtServerHelper.getStringFromEntryMap(entryMap, Constants.ENTITY_PATH);
										if (MiscUtil.hasString(description)) {
											eti.setDescription(      description);
											eti.setDescriptionIsHtml(false      );
										}
									}
									if (isEntityFolderEntry && GwtServerHelper.isFamilyFile(GwtServerHelper.getStringFromEntryMap(entryMap, Constants.FAMILY_FIELD))) {
										String fName = GwtServerHelper.getStringFromEntryMap(entryMap, Constants.FILENAME_FIELD);
										if (MiscUtil.hasString(fName)) {
											eti.setFile(true);
											eti.setFileDownloadUrl(
												GwtServerHelper.getDownloadFileUrl(
													request,
													bs,
													entityId.getBinderId(),
													entityId.getEntityId()));
											eti.setFileIcon(
												FileIconsHelper.getFileIconFromFileName(
													fName,
													mapBISToIS(
														BinderIconSize.getListViewIconSize())));
										}
									}
									fr.setColumnValue(fc, eti);
								}
								
								// No, we aren't working on a title
								// field!  Are we working on a file ID
								// field?
								else if (csk.equals(Constants.FILE_ID_FIELD)) {
									// Yes!  Do we have a single file
									// ID?
									if ((!(MiscUtil.hasString(value))) || ((-1) != value.indexOf(','))) {
										// No!  Ignore the value.
										value = null;
									}
									
									else {
										// Yes, we have a single file
										// ID!  Do we have a file path
										// that we support viewing of?
										String relativeFilePath = GwtServerHelper.getStringFromEntryMap(entryMap, Constants.FILENAME_FIELD);
										if ((!(MiscUtil.hasString(relativeFilePath))) || (!(SsfsUtil.supportsViewAsHtml(relativeFilePath)))) {
											// No!  Ignore the value.
											value = null;
										}
									}
									
									// Do we have a file ID to work
									// with?
									if (MiscUtil.hasString(value)) {
										// Yes!  Construct a
										// ViewFileInfo for it.
										ViewFileInfo vfi = new ViewFileInfo();
										vfi.setFileId(     value);
										vfi.setEntityId(   entityId);
										vfi.setFileTime(   GwtServerHelper.getStringFromEntryMap(entryMap, Constants.FILE_TIME_FIELD));
										vfi.setViewFileUrl(GwtServerHelper.getViewFileUrl(request, vfi));
										fr.setColumnValue(fc, vfi);
									}
								}

								// No, we aren't working on a file ID
								// field either!  Are we working on an
								// HTML description field?
								else if (csk.equals(Constants.DESC_FIELD) && cn.equals("descriptionHtml")) {
									// Yes!  Check if the description
									// is in HTML format and store it.
									String descFmt = GwtServerHelper. getStringFromEntryMap(entryMap, Constants.DESC_FORMAT_FIELD);
									boolean isHtml = ((null != descFmt) && "1".equals(descFmt));
									fr.setColumnValue(fc, new DescriptionHtml(value, isHtml));
								}
								
								// No, we aren't working on an HTML
								// description field either!  Are we
								// working on a family specification
								// field?
								else if (csk.equals(Constants.FAMILY_FIELD)) {
									// Yes!  Do we have a value for the
									// column?
									if (MiscUtil.hasString(value)) {
										// Yes!  Load any localized
										// name we might have for it.
										String nltKeyBase;
										if (entityType.equals(EntityType.folderEntry.name()))
										     nltKeyBase = "__entry_";
										else nltKeyBase = "__folder_";
										value = NLT.get((nltKeyBase + value), value);
									}
									
									// Use what ever String value we
									// arrived at.
									fr.setColumnValue(fc, (null == (value) ? "" : value));
								}
								
								// No, we aren't working on a family
								// specification field either!  Are we
								// working on a comments count field?
								else if (csk.equals(Constants.TOTALREPLYCOUNT_FIELD)) {
									// Yes!  Store a CommentsInfo for
									// it.
									String commentCount = GwtServerHelper.getStringFromEntryMap(entryMap, Constants.TOTALREPLYCOUNT_FIELD);
									if (!(MiscUtil.hasString(commentCount))) {
										commentCount = "0";
									}
									String entityTitle = GwtServerHelper.getStringFromEntryMap(entryMap, Constants.TITLE_FIELD);
									if (!(MiscUtil.hasString(entityTitle))) {
										entityTitle = ("--" + NLT.get("entry.noTitle") + "--");
									}
									fr.setColumnValue(
										fc,
										new CommentsInfo(
											entityId,
											entityTitle,
											Integer.parseInt(commentCount)));
								}
								
								// No, we aren't working a comments
								// count field either!  Are we working
								// on the internal/external flag of a
								// user?
								else if (csk.equals(Constants.IDENTITY_INTERNAL_FIELD)) {
									// Yes!  Store a user type for it.
									fr.setColumnValue(fc, getUserType(bs, request, entityId));
								}
								
								else {
									// No, we aren't working on a
									// comments count field either!
									// Are we working on a field whose
									// value is a Date?
									if (emValue instanceof Date) {
										// Yes!  Is that Date overdue?
										if (DateComparer.isOverdue((Date) emValue)) {
											// Yes!  Mark that column
											// as being an overdue
											// date, and if this a
											// due date...
											if (csk.equals(Constants.DUE_DATE_FIELD)) {
												// ...that's not...
												// ...completed...
												String status = GwtServerHelper.getStringFromEntryMap(entryMap, Constants.STATUS_FIELD);
												boolean completed = (MiscUtil.hasString(status) && status.equals("completed"));
												if (!completed) {
													// ...show it as
													// ...being
													// ...overdue.
													fr.setColumnOverdueDate(fc, Boolean.TRUE);
													if      (isSurvey)    value += (" " + NLT.get("survey.overdue"   ));
													else if (isMilestone) value += (" " + NLT.get("milestone.overdue"));
												}
											}
										}
										fr.setColumnValue(fc, (null == (value) ? "" : value));
									}
									
									// No, we aren't working on a Date
									// field!  Are we working on a file
									// size field?
									else if (csk.equals(Constants.FILE_SIZE_FIELD)) {
										// Yes!  Trim any leading 0's
										// from the value.
										if (isEntityFolderEntry) {
											value = trimFileSize(value);
											if (MiscUtil.hasString(value)) {
												value += " KB";
											}
										}
										else {
											value = "";
										}
									}

									// No, we aren't working on a file
									// size field!  Are we working on
									// the status field of a milestone?
									else if (csk.equals(Constants.STATUS_FIELD) && isMilestone) {
										// Yes!  Do we have a status
										// value for it?
										if (MiscUtil.hasString(value)) {
											// Yes!  Pull its localized
											// string from the
											// resources.
											value = NLT.get(("__milestone_status_" + value), value);
										}
									}
									
									// Use what ever String value we
									// arrived at.
									fr.setColumnValue(fc, (null == (value) ? "" : value));
								}
							}
						}
					}
				}
				
				// Add the FolderRow we just built to the
				// List<FolderRow> of them.
				folderRows.add(fr);
			}

			// Did we add any rows with assignment information?
			if (addedAssignments) {
				// Yes!  We need to complete the definition of the
				// AssignmentInfo objects.
				//
				// When initially built, the AssignmentInfo's in the
				// List<AssignmentInfo>'s only contain the assignee
				// IDs.  We need to complete them with each assignee's
				// title, ...
				completeAIs(bs, request, folderRows);
			}
			
			// Walk the List<FolderRow>'s performing any remaining
			// fixups on each as necessary.
			fixupFRs(bs, request, folderRows);

			// Is the user viewing pinned entries?
			if (viewPinnedEntries) {
				// Yes!  Then we need to sort the rows using the user's
				// current sort criteria.
				Comparator<FolderRow> comparator =
					new FolderRowComparator(
						fdd.getFolderSortBy(),
						fdd.getFolderSortDescend(),
						folderColumns);
				
				Collections.sort(folderRows, comparator);
			}

			// If we have a quick filter and we processing a
			// 'Shared by/with Me' collection and we have some rows...
			if (MiscUtil.hasString(quickFilter) && (isCollectionSharedByMe || isCollectionSharedWithMe) && (!(folderRows.isEmpty()))) {
				// ...we need to apply the quick filter to the
				// ...List<FolderRow>.
				folderRows = filterSharedMeFolderRows(
					folderColumns,
					folderRows,
					quickFilter);
				totalRecords = folderRows.size();
			}
			
			// Finally, return the List<FolderRow> wrapped in a
			// FolderRowsRpcResponseData.
			return
				new FolderRowsRpcResponseData(
					folderRows,
					start,
					totalRecords,
					contributorIds);
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.getFolderRows( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}

	/*
	 * Constructs and returns a GuestInfo from a Principal.
	 */
	private static GuestInfo getGuestInfoFromPrincipal(AllModulesInjected bs, HttpServletRequest request, Principal p) {
		Long pId = p.getId();
		GuestInfo reply = new GuestInfo(
			pId,
			p.getTitle(),
			PermaLinkUtil.getUserPermalink(request, String.valueOf(pId)));
		reply.setAvatarUrl(GwtServerHelper.getUserAvatarUrl(bs, request, p));
		reply.setEmailAddress(      p.getEmailAddress()      );
		reply.setMobileEmailAddress(p.getMobileEmailAddress());
		reply.setTextEmailAddress(  p.getTxtEmailAddress()   );
		if (p instanceof User) {
			reply.setPhone(((User) p).getPhone());
		}
		return reply;
	}

	/*
	 * Extracts a Long value from a entry Map.
	 */
	@SuppressWarnings("unchecked")
	public static Long getLongFromMap(Map entryMap, String key) {
		Object v = entryMap.get(key);
		Long reply;
		if      (v instanceof String) reply = Long.parseLong((String) v);
		else if (v instanceof Long)   reply = ((Long) v);
		else                          reply = -1L;
		return reply;
	}

	/**
	 * Returns a ViewFolderEntryInfoRpcResponseData corresponding to
	 * the previous/next folder entry to the given EntityId.
	 * 
	 * @param bs
	 * @param request
	 * @param entityId
	 * @param previous
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	@SuppressWarnings("unchecked")
	public static ViewFolderEntryInfoRpcResponseData getNextPreviousFolderInfo(AllModulesInjected bs, HttpServletRequest request, EntityId entityId, boolean previous) throws GwtTeamingException {
		try {
			// Allocate a ViewFolderEntryInfoRpcResponseData we can
			// return.
			ViewFolderEntryInfoRpcResponseData reply = new ViewFolderEntryInfoRpcResponseData();

			// Setup an options Map with the sorting information
			// current in effect for the folder.
			Long								folderId   = entityId.getBinderId();
			BinderInfo							folderInfo = GwtServerHelper.getBinderInfo(bs, request, folderId  );
			FolderDisplayDataRpcResponseData	fdd        = getFolderDisplayData(         bs, request, folderInfo);
			Map options = new HashMap();
			options.put(ObjectKeys.SEARCH_SORT_BY,                     fdd.getFolderSortBy()      );
			options.put(ObjectKeys.SEARCH_SORT_DESCEND, String.valueOf(fdd.getFolderSortDescend()));
			
			// Can we get the ID of the previous/next folder entry?
			Folder	folder        = bs.getFolderModule().getFolder(folderId);
			Long	targetEntryId = BinderHelper.getNextPrevEntry(bs, folder, entityId.getEntityId(), (!previous), options);
			if (null != targetEntryId) {
				// Yes!  Create a ViewFolderEntryInfo for it and store
				// it in the reply.
				reply.setViewFolderEntryInfo(
					buildViewFolderEntryInfo(
						bs,
						request,
						folderId,
						targetEntryId));
			}
			
			// If we get here, reply refers to the
			// ViewFolderEntryInfoRpcResponseData for the previous/next
			// folder entry.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.getNextPreviousFolderInfo( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}
	
	/*
	 * Given a Principal's ID read from an entry map, returns an
	 * equivalent PrincipalInfo object.
	 */
	@SuppressWarnings("unchecked")
	private static PrincipalInfo getPIFromPId(AllModulesInjected bs, HttpServletRequest request, Long pId) {
		// Can we resolve the ID to an actual Principal object?
		PrincipalInfo reply = null;
		List<Long> principalIds = new ArrayList<Long>();
		principalIds.add(pId);
		List principals = null;
		try {principals = ResolveIds.getPrincipals(principalIds, false);}
		catch (Exception ex) {/* Ignored. */}
		if (MiscUtil.hasItems(principals)) {
			for (Object o:  principals) {
				// Yes!  Is it a User?
				Principal p = ((Principal) o);
				boolean isUser = (p instanceof UserPrincipal);
				if (isUser) {
					// Yes!  Construct the rest of the PrincipalInfo
					// required.
					pId   = p.getId();
					reply = PrincipalInfo.construct(pId);
					reply.setTitle(p.getTitle());
					User      user          = ((User) p);
					Workspace userWS        = GwtServerHelper.getUserWorkspace(user);
					boolean   userHasWS     = (null != userWS);
					boolean   userWSInTrash = (userHasWS && userWS.isPreDeleted());
					reply.setUserDisabled( user.isDisabled());
					reply.setUserHasWS(    userHasWS        );
					reply.setUserPerson(   user.isPerson()  );
					reply.setUserWSInTrash(userWSInTrash    );
					reply.setViewProfileEntryUrl(getViewProfileEntryUrl(bs, request, pId));
					reply.setPresenceUserWSId(user.getWorkspaceId());
					reply.setAvatarUrl(GwtServerHelper.getUserAvatarUrl(bs, request, user));
					
					// Setup an appropriate GwtPresenceInfo for the
					// Vibe environment?
					GwtPresenceInfo presenceInfo;
					if (GwtServerHelper.isPresenceEnabled())
					     presenceInfo = GwtServerHelper.getPresenceInfo(user);
					else presenceInfo = null;
					if (null == presenceInfo) {
						presenceInfo = GwtServerHelper.getPresenceInfoDefault();
					}
					if (null != presenceInfo) {
						reply.setPresence(presenceInfo);
						reply.setPresenceDude(GwtServerHelper.getPresenceDude(presenceInfo));
					}
				}
				
				// There can only ever be one ID.
				break;
			}
		}
		
		// If we get here, reply refers to the PrincipalInfo object
		// for the principal ID we received or is null.  Return it.
		return reply;
	}
	
	/*
	 * Returns a map containing the search filter to use to read the
	 * rows from a folder.
	 */
	@SuppressWarnings("unchecked")
	private static Map getFolderSearchFilter(AllModulesInjected bs, Binder binder, UserProperties userFolderProperties, String searchTitle) {
		Map result = new HashMap();
		GwtServerHelper.addSearchFiltersToOptions(bs, binder, userFolderProperties, true, result);
		if (MiscUtil.hasString(searchTitle)) {
			result.put(ObjectKeys.SEARCH_TITLE, searchTitle);
		}
		return result;
	}

	/*
	 * Returns true if a folder supports pinning and folder otherwise.
	 */
	private static boolean getFolderSupportsPinning(BinderInfo folderInfo) {
		boolean reply;
		switch (folderInfo.getFolderType()) {
		default:          reply = false; break;
		case DISCUSSION:  reply = true;  break;
		}
		return reply;
	}
	
	/**
	 * Returns the HTML from the executing a JSP.
	 * 
	 * @param bs
	 * @param request
	 * @param response
	 * @param servletContext
	 * @param jspType
	 * @param model
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static JspHtmlRpcResponseData getJspHtml(AllModulesInjected bs, HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, VibeJspHtmlType jspType, Map<String, Object> model) throws GwtTeamingException {
		String html    = "";
		String jspPath = null;
		
		try {
			// The following are the supported JSP types.
			AdminModule   am = bs.getAdminModule();
			BinderModule  bm = bs.getBinderModule();
			ProfileModule pm = bs.getProfileModule();
			switch (jspType) {
			case ACCESSORY_PANEL: {
				try {
					// Build request and render objects needed to build
					// the toolbar.
					String		portletName = "ss_forum";
					PortletInfo	portletInfo = ((PortletInfo) AdaptedPortlets.getPortletInfo(portletName));
					
					RenderRequestImpl renderReq = new RenderRequestImpl(request, portletInfo, AdaptedPortlets.getPortletContext());
					
					Map<String, Object> params = new HashMap<String, Object>();
					params.put(KeyNames.PORTLET_URL_PORTLET_NAME, new String[]{portletName});
					renderReq.setRenderParameters(params);
					
					RenderResponseImpl renderRes = new RenderResponseImpl(renderReq, response, portletName);
					String charEncoding = SPropsUtil.getString("web.char.encoding", "UTF-8");
					renderRes.setContentType("text/html; charset=" + charEncoding);
					renderReq.defineObjects(portletInfo.getPortletConfig(), renderRes);
					
					renderReq.setAttribute(PortletRequest.LIFECYCLE_PHASE, PortletRequest.RENDER_PHASE);

					// Display the whole accessory panel.
					User user = RequestContextHolder.getRequestContext().getUser();
					String s_binderId = (String) model.get("binderId");
					Binder binder = bm.getBinder(Long.valueOf(s_binderId));

					UserProperties userProperties = new UserProperties(user.getId());
					Map userProps = new HashMap();
		    		if (null != userProperties.getProperties()) {
		    			userProps = userProperties.getProperties();
		    		}

		    		if (null != user) {
		    			userProperties = pm.getUserProperties(user.getId());
		    		}
		    		
					// Build the 'Add Accessory' toolbar.
		    		Map<String,Object> panelModel = new HashMap<String,Object>();
					DashboardHelper.getDashboardMap(binder, userProps, panelModel);
					Toolbar dashboardToolbar = new Toolbar();
					BinderHelper.buildDashboardToolbar(renderReq, renderRes, bs, binder, dashboardToolbar, panelModel);

					// Set up the beans used by the jsp.
					panelModel.put(WebKeys.BINDER_ID, binder.getId());
					panelModel.put(WebKeys.BINDER, binder);
					panelModel.put(WebKeys.USER_PROPERTIES, userProps);
					panelModel.put(WebKeys.SNIPPET, true);	// Signal that <html> and <head> should not be output.
					panelModel.put(WebKeys.DASHBOARD_TOOLBAR, dashboardToolbar.getToolbar());
					
					jspPath = "definition_elements/view_dashboard_canvas.jsp";
					html = GwtServerHelper.executeJsp(bs, request, response, servletContext, jspPath, panelModel);
					break;
				}
				
				catch(Exception e) {
					if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
					     m_logger.debug("GwtViewHelper.getJspHtml( SOURCE EXCEPTION ):  ", e);
					}
					
					// Return an error back to the user.
					String[] args = new String[]{e.getMessage()};
					html = NLT.get("errorcode.dashboardComponentViewFailure", args);
				}
			}
			
			case ACCESSORY: {
				try {
					// Set up bean used by the dashboard component (aka
					// accessory).
					User	user        = RequestContextHolder.getRequestContext().getUser();
					String	s_binderId  = ((String) model.get("binderId"));
					Binder	binder      = bm.getBinder(Long.valueOf(s_binderId));
					String	componentId = ((String) model.get("ssComponentId"));
					String	scope       = componentId.split("_")[0];

					UserProperties	userProperties = new UserProperties(user.getId());
					Map				userProps      = new HashMap();
		    		if (null != userProperties.getProperties()) {
		    			userProps = userProperties.getProperties();
		    		}
		    		if (null != user) {
		    			userProperties = pm.getUserProperties(user.getId());
		    		}
		    		if ((null != componentId) && (!(componentId.equals("")))) {
			    		Map<String,Object> componentModel = new HashMap<String,Object>();
						DashboardHelper.getDashboardMap(binder, userProps, componentModel, scope, componentId, false);
						Map<String,Object> componentDashboard = (Map<String,Object>) componentModel.get("ssDashboard");
						componentDashboard.put("ssComponentId", componentId);
						componentModel.put(WebKeys.BINDER_ID, binder.getId());
						componentModel.put(WebKeys.BINDER, binder);
						jspPath = "definition_elements/view_dashboard_component.jsp";
						html = GwtServerHelper.executeJsp(bs, request, response, servletContext, jspPath, componentModel);
		    		}
					break;
				}
				
				catch(Exception e) {
					if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
					     m_logger.debug("GwtViewHelper.getJspHtml( SOURCE EXCEPTION ):  ", e);
					}
					// Return an error back to the user.
					String[] args = new String[]{e.getMessage()};
					html = NLT.get("errorcode.dashboardComponentViewFailure", args);
				}
			}
			
			case ADMIN_REPORT_CHANGELOG: {
				// Create a model Map for the change log report JSP
				// page...
				Map<String, Object> changeLogModel = buildReportModelMap(model);
	    		
	    		// ...and run the JSP to produce the target HTML.
				html = GwtServerHelper.executeJsp(
					bs,
					request,
					response,
					servletContext,
					buildJspName(WebKeys.VIEW_ADMIN_CHANGELOG),
					changeLogModel);

				break;
			}
				
			case ADMIN_REPORT_CREDITS: {
				// Create a model Map for the credits JSP page...
				Map<String, Object> creditsModel = buildReportModelMap(model);
	    		
	    		// ...and run the JSP to produce the target HTML.
				html = GwtServerHelper.executeJsp(
					bs,
					request,
					response,
					servletContext,
					buildJspName(WebKeys.VIEW_ADMIN_CREDITS),
					creditsModel);
				
				break;
			}

			case ADMIN_REPORT_DATA_QUOTA_EXCEEDED:
			case ADMIN_REPORT_DATA_QUOTA_HIGHWATER_EXCEEDED:
			case ADMIN_REPORT_DISK_USAGE: {
				// Create a model Map for the report's JSP page...
				Map<String, Object> reportModel = buildReportModelMap(model);
				
				// ...add the specific beans required by these report
				// ...pages...
				Map aclMap = BinderHelper.getAccessControlMapBean(reportModel);
				aclMap.put("generateQuotaReport", am.testAccess(AdminOperation.report));

				// ...decided on the appropriate JSP view for the
				// ...report...
				String jspView;
				switch (jspType) {
				default:
				case ADMIN_REPORT_DISK_USAGE:                     jspView = WebKeys.VIEW_QUOTA_REPORT;                    break;
				case ADMIN_REPORT_DATA_QUOTA_EXCEEDED:            jspView = WebKeys.VIEW_QUOTA_EXCEEDED_REPORT;           break;
				case ADMIN_REPORT_DATA_QUOTA_HIGHWATER_EXCEEDED:  jspView = WebKeys.VIEW_QUOTA_HIGHWATER_EXCEEDED_REPORT; break;
				}
	    		
	    		// ...and run that JSP to produce the target HTML.
				html = GwtServerHelper.executeJsp(
					bs,
					request,
					response,
					servletContext,
					buildJspName(jspView),
					reportModel);
				
				break;
			}
			
			case ADMIN_REPORT_XSS: {
				// Create a model Map for the XSS JSP page...
				Map<String, Object> xssModel = buildReportModelMap(model);

				// ...add the specific beans required by the XSS
				// ...page...
	    		Map accessControlMap = BinderHelper.getAccessControlMapBean(xssModel);
	    		accessControlMap.put("generateReport", am.testAccess(AdminOperation.report));
	    		
	    		Document pTree = DocumentHelper.createDocument();
	        	Element rootElement = pTree.addElement(DomTreeBuilder.NODE_ROOT);
	        	Document wsTree = bm.getDomBinderTree(RequestContextHolder.getRequestContext().getZoneId(), new WsDomTreeBuilder(null, true, bs, new SearchTreeHelper()), 1);
	        	rootElement.appendAttributes(wsTree.getRootElement());
	        	rootElement.appendContent(wsTree.getRootElement());
	     		xssModel.put(WebKeys.WORKSPACE_DOM_TREE_BINDER_ID, RequestContextHolder.getRequestContext().getZoneId().toString());
	     		xssModel.put(WebKeys.WORKSPACE_DOM_TREE, pTree);		
	    		
	    		// ...and run the JSP to produce the target HTML.
				html = GwtServerHelper.executeJsp(
					bs,
					request,
					response,
					servletContext,
					buildJspName(WebKeys.VIEW_XSS_REPORT),
					xssModel);
				
				break;
			}
			
			default: 
				// Log an error that we encountered an unhandled command.
				m_logger.error("JspHtmlRpcResponseData( Unknown jsp type ):  " + jspType.name());
				break;
			}
			
			return new JspHtmlRpcResponseData(html, model);
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.getJspHtml( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}

	/*
	 * Returns a date/time stamp of the last time the user logged in or
	 * null if it can't be determined.
	 */
	private static Date getLastUserLogin(AllModulesInjected bs, Long userId) {
		Date reply = null;
		
		try {
			// Can we get a login report for this user?
			Set<Long> memberIds = new HashSet<Long>();
			memberIds.add(userId);
			List<Map<String, Object>> loginReport = bs.getReportModule().generateLoginReport(
				new Date(0),	// Report start.
				new Date(),		// Report end.
				WebKeys.URL_REPORT_OPTION_TYPE_SHORT,
				ReportModule.LAST_LOGIN_SORT,
				ReportModule.LOGIN_DATE_SORT,
				memberIds);
			if (MiscUtil.hasItems(loginReport)) {
				// Yes!  Does it contain their last login?
				Map<String, Object> loginMap = loginReport.get(0);
				String lastLogin = ((String) loginMap.get(ReportModule.LAST_LOGIN));	// Example:  '2012-12-17 02:24:01 PM'
				if (MiscUtil.hasString(lastLogin)) {
					// Yes!  Parse and return it.
					SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");
					reply = fmt.parse(lastLogin);
				}
			}
		}
		catch (ParseException e) {
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.getLastUserLogin( SOURCE EXCEPTION ):  ", e);
			}
			reply = null;
		}

		// If we get here, reply refers to a date/time stamp of the
		// last time the user logged in or is null if it couldn't be
		// determined.  Return it.
		return reply;
	}

	/*
	 * Get the parent container's DN.  May be null.
	 * 
	 * Copied the logic for this from
	 * LdapModuleImpl.getParentContainerDn().
	 */
	private static String getParentContainerDN(String baseDN) {
		// If we weren't given a base DN to parse...
		if (null == baseDN) {
			// ...return null.
			return null;
		}
		
		// Find the first ','
		int index = baseDN.indexOf( ',' );
		if ((0 < index) && ((index + 1) < baseDN.length())) {
			// Does the parent DN start with 'ou=' or 'o='?
			String parentDN = baseDN.substring(index + 1);
			if (parentDN.startsWith("ou=") || parentDN.startsWith("o=")) {
				// Yes!  Return it.
				return parentDN;
			}
		}

		// If we get here, we couldn't determine the parent DN.  Return
		// null.
		return null;
	}
	
	/*
	 * Returns a List<Long> of the entity ID's of the entries that are
	 * pinned in the given folder. 
	 */
	@SuppressWarnings("unchecked")
	private static List<Map> getPinnedEntries(AllModulesInjected bs, Folder folder, UserProperties userFolderProperties, List<Long> pinnedEntryIds, boolean returnEntries) {
		// Allocate a List<Map> for the search results for the entries
		// pinned in this folder.
		List<Map> pinnedEntrySearchMaps = new ArrayList<Map>();

		// Are there any pinned entries stored in the user's folder
		// properties on this folder?
		Map properties = userFolderProperties.getProperties();
		String pinnedEntries;
		if ((null != properties) && properties.containsKey(ObjectKeys.USER_PROPERTY_PINNED_ENTRIES))
		     pinnedEntries = ((String) properties.get(ObjectKeys.USER_PROPERTY_PINNED_ENTRIES));
		else pinnedEntries = null;
		if (MiscUtil.hasString(pinnedEntries)) {
			// Yes!  Parse them converting the String ID's to Long's.
			if (pinnedEntries.lastIndexOf(",") == (pinnedEntries.length() - 1)) { 
				pinnedEntries = pinnedEntries.substring(0, (pinnedEntries.length() - 1));
			}
			String[] peArray = pinnedEntries.split(",");
			List<Long> peSet = new ArrayList();
			for (int i = 0; i < peArray.length; i += 1) {
				String pe = peArray[i];
				if (MiscUtil.hasString(pe)) {
					peSet.add(Long.valueOf(pe));
				}
			}

			// Scan the pinned entries.
			FolderModule fm = bs.getFolderModule();
			List<org.apache.lucene.document.Document> pinnedFolderEntriesList = new ArrayList<org.apache.lucene.document.Document>();
			SortedSet<FolderEntry> pinnedFolderEntriesSet = fm.getEntries(peSet);
			for (FolderEntry entry:  pinnedFolderEntriesSet) {
				// Is this entry still viable in this folder?
				if (!(entry.isPreDeleted()) && entry.getParentBinder().equals(folder)) {
					// Yes!  Track its ID.
					pinnedEntryIds.add(entry.getId());

					// Are we returning the entries too?
					if (returnEntries) {
						// Yes!  Save its indexDoc.
						org.apache.lucene.document.Document indexDoc = fm.buildIndexDocumentFromEntry(entry.getParentBinder(), entry, null);
						pinnedFolderEntriesList.add(indexDoc);
					}
				}
			}

			// Are we returning the entries too?
			if (returnEntries) {
				// Construct search Map's from the indexDoc's for the
				// pinned entries.
				pinnedEntrySearchMaps = org.kablink.teaming.module.shared.SearchUtils.getSearchEntries(pinnedFolderEntriesList);
				bs.getFolderModule().getEntryPrincipals(pinnedEntrySearchMaps);
			}
		}
		
		// If we get here, pinnedEntrySearchMaps refers to a List<Map>
		// search Map's for the pinned entries.  Return it.
		return pinnedEntrySearchMaps;
	}

	/**
	 * Returns a ProfileEntryInfoRpcRequestData containing information
	 * about a user's profile.
	 * 
	 * @param bs
	 * @param request
	 * @param userId
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	@SuppressWarnings("unchecked")
	public static ProfileEntryInfoRpcResponseData getProfileEntryInfo(AllModulesInjected bs, HttpServletRequest request, Long userId) throws GwtTeamingException {
		try {
			// Allocate an profile entry info response we can return.
			ProfileEntryInfoRpcResponseData reply = new ProfileEntryInfoRpcResponseData();

			// Can we access the user in question?
			List<String> userIdList = new ArrayList<String>();
			String userIdS = String.valueOf(userId);
			userIdList.add(userIdS);
			List resolvedList = ResolveIds.getPrincipals(userIdList, false);
			if (MiscUtil.hasItems(resolvedList)) {
				// Yes!  Extract the profile information we need to
				// display.
				User user = ((User) resolvedList.get(0));
				addProfileAttribute(reply, "title",        user.getTitle());
				addProfileAttribute(reply, "jobTitle",     GwtProfileHelper.getJobTitle(user));
				addProfileAttribute(reply, "emailAddress", user.getEmailAddress());
				addProfileAttribute(reply, "phone",        user.getPhone());
				addProfileAttribute(reply, "timeZone",     user.getTimeZone().getDisplayName());
				addProfileAttribute(reply, "locale",       user.getLocale().getDisplayName());
				if (!(Utils.checkIfFilr())) {
					addProfileAttribute(reply, "mobileEmailAddress", user.getMobileEmailAddress());
					addProfileAttribute(reply, "txtEmailAddress",    user.getTxtEmailAddress());
				}

				// Does the user have an 'About Me' defined?
				CustomAttribute ca = user.getCustomAttribute("aboutMe");
				if (null != ca) {
					// Yes!  Does it have a value?
					Object		aboutMeO = ca.getValue();
					Description	aboutMeDesc;
					if      (aboutMeO instanceof String)      aboutMeDesc = new Description((String)aboutMeO);
					else if (aboutMeO instanceof Description) aboutMeDesc = ((Description) ca.getValue());
					else                                      aboutMeDesc = null;
					if (null != aboutMeDesc ) {
						// Yes!  Replace the mark up...
						String aboutMeHtml = MarkupUtil.markupStringReplacement(null, null, request, null, user, aboutMeDesc.getText(), WebKeys.MARKUP_VIEW);
						if (null != aboutMeHtml){
							// Added a length of one to skip over a
							// return characters that are in somehow in
							// the value of the attribute.
							if (1 < aboutMeHtml.length()){
								// ...and store anything left in the
								// ...reply.
								reply.setAboutMeHtml(aboutMeHtml);
							}
						}
					}
				}
				
				// Store the URL for the user's avatar, if they have
				// one.
				reply.setAvatarUrl(GwtServerHelper.getUserAvatarUrl(bs, request, user));

				// Does the current user have rights to modify users?
				ProfileModule pm = bs.getProfileModule();
				String profilesWSIdS = String.valueOf(pm.getProfileBinderId());
				if (pm.testAccess(user, ProfileOperation.modifyEntry)) {
					// Yes!  Store the modify URL for this user.
					AdaptedPortletURL url = new AdaptedPortletURL(request, "ss_forum", true);
					url.setParameter(WebKeys.ACTION,         WebKeys.ACTION_MODIFY_PROFILE_ENTRY);
					url.setParameter(WebKeys.URL_BINDER_ID,  profilesWSIdS                      );
					url.setParameter(WebKeys.URL_ENTRY_ID,   userIdS                            );
					reply.setModifyUrl(url.toString());
				}
				
				// Does the current user have rights to delete users
				// and is this other than a reserved user?
				if (pm.testAccess(user, ProfileOperation.deleteEntry) && (!(user.isReserved()))) {
					// Yes!  Store the delete URL for this user.
					AdaptedPortletURL url = new AdaptedPortletURL(request, "ss_forum", true);
					url.setParameter(WebKeys.ACTION,         WebKeys.ACTION_MODIFY_PROFILE_ENTRY);
					url.setParameter(WebKeys.URL_OPERATION,  WebKeys.OPERATION_DELETE           );
					url.setParameter(WebKeys.URL_BINDER_ID,  profilesWSIdS                      );
					url.setParameter(WebKeys.URL_ENTRY_ID,   userIdS                            );
					reply.setDeleteUrl(url.toString());
				}
			}

			// If we get here, reply refers to an
			// ProfileEntryInfoRpcResponseData containing the user's
			// profile information.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.getProfileEntryInfo( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}
	
	/*
	 * Returns a Map<String, String> for the query parameters from a
	 * URL.
	 */
	private static Map<String, String> getQueryParameters(String url) {
		URI uri;
		try {
			// Can we parse the URL?
			uri = new URI(url);
		} catch (URISyntaxException e) {
			// No!  Log the error and bail.
			m_logger.error("GwtViewHelper.getQueryParameters( URL Parsing Exception ):  ", e);
			return null;
		}
		
		// Allocate a Map<String, String> to return the parameters.
		Map<String, String> reply = new HashMap<String, String>();

		// Does the URL contain a query?
		String query = uri.getQuery();
		if (null != query) {
			// Yes!  Split it at the &'s.
			String[] parameters = query.split("&");
			
			// Scan the parameters.
			for (String p:  parameters) {
				// Does this parameter contain an '='?
				int eq = p.indexOf('=');
				if ((-1) != eq) {
					// Yes!  Does it have a name part?
					String name = p.substring(0, eq);
					if (MiscUtil.hasString(name)) {
						// Yes!  Add it to the map.
						reply.put(name.toLowerCase(), p.substring(eq + 1));
					}
				}
			}
		}
		
		else {
			// No, the URL didn't contain a query!  Does it contain a
			// path?
			String path = uri.getPath();
			if (null != path) {
				// Yes!  Split it at the /'s.
				String[] parameters = path.split("/");
				
				// Scan the parameters by 2's.
				int c = (parameters.length - 1);
				for (int i = 0; i < c; i += 2) {
					// Does this parameter have a name?
					String name = parameters[i];
					if (MiscUtil.hasString(name)) {
						// Yes!  Add it to the map.
						reply.put(name.toLowerCase(), parameters[i + 1]);
					}
				}
			}
		}
		
		// If we get here, reply refers to a Map<String, String> of the
		// parameters from the URL.  Return it.
		return reply;
	}

	/*
	 * Searches a Map<String, String> for a named parameter containing
	 * a Boolean value.
	 * 
	 * If the value can't be found or it can't be parsed, null is
	 * returned. 
	 */
	private static boolean getQueryParameterBoolean(Map<String, String> nvMap, String name) {
		String v = getQueryParameterString(nvMap, name);
		if (0 < v.length()) {
			boolean reply;
			try                 {reply = Boolean.parseBoolean(v);}
			catch (Exception e) {reply = false;                  }
			return reply;
		}		
		return false;
	}
	
	/*
	 * Searches a Map<String, String> for a named parameter containing
	 * a Boolean value.
	 * 
	 * If the value can't be found or it can't be parsed, null is
	 * returned. 
	 */
	@SuppressWarnings("unused")
	private static int getQueryParameterInt(Map<String, String> nvMap, String name) {
		String v = getQueryParameterString(nvMap, name);
		if (0 < v.length()) {
			int reply;
			try                 {reply = Integer.parseInt(v);}
			catch (Exception e) {reply = (-1);               }
			return reply;
		}		
		return (-1);
	}
	
	/*
	 * Searches a Map<String, String> for a named parameter containing
	 * a Long value.
	 * 
	 * If the value can't be found or it can't be parsed, null is
	 * returned. 
	 */
	private static Long getQueryParameterLong(Map<String, String> nvMap, String name) {
		String v = getQueryParameterString(nvMap, name);
		if (0 < v.length()) {
			Long reply;
			try                 {reply = Long.parseLong(v);}
			catch (Exception e) {reply = null;             }
			return reply;
		}		
		return null;
	}
	
	/*
	 * Searches a Map<String, String> for a named parameter.
	 * 
	 * If the value can't be found, null is returned. 
	 */
	private static String getQueryParameterString(Map<String, String> nvMap, String name) {
		String reply;
		if (MiscUtil.hasItems(nvMap))
		     reply = nvMap.get(name.toLowerCase());
		else reply = null;
		return ((null == reply) ? "" : reply);
	}

	/*
	 * Returns the title to use for comparisons of a sharer or
	 * share recipient.
	 */
	@SuppressWarnings("unchecked")
	private static String getRecipientTitle(AllModulesInjected bs, RecipientType rt, Long sId) {
		String reply = "";
		try {
			switch (rt) {
			case user:
			case group:
				// Resolve the user/group...
				List<Long> principalIds = new ArrayList<Long>();
				principalIds.add(sId);
				List<Principal> principals = ResolveIds.getPrincipals(principalIds, false);
				if (MiscUtil.hasItems(principalIds)) {
					// ...and return its title.
					reply = ((Principal) principals.get(0)).getTitle();
				}
				break;
				
			case team:
				// Return the title of the binder.
				Binder binder = bs.getBinderModule().getBinder(sId);
				reply = binder.getTitle();
				break;
			}
		}
		catch (Exception ex) {/* Ignore. */}
		return reply;
	}
	
	/*
	 * Resolves, if possible, a user ID to a User object.
	 */
	@SuppressWarnings("unchecked")
	private static User getResolvedUser(Long userId) {
		User user = null;
		String userIdS = String.valueOf(userId);
		List<String> userIdList = new ArrayList<String>();
		userIdList.add(userIdS);
		List resolvedList = ResolveIds.getPrincipals(userIdList, false);
		if (MiscUtil.hasItems(resolvedList)) {
			Object o = resolvedList.get(0);
			if (o instanceof User) {
				user = ((User) o);
			}
		}
		return user;
	}

	/*
	 * Returns a List<GwtSharedMeItem> of the items shared by the
	 * current user.
	 */
	private static List<GwtSharedMeItem> getSharedByMeItems(AllModulesInjected bs, HttpServletRequest request, String sortBy, boolean sortDescend) throws Exception {
		// Construct a list containing just this user's ID...
		Long		userId = GwtServerHelper.getCurrentUserId();
		List<Long>	users  = new ArrayList<Long>();
		users.add(userId);

		// ...get the List<ShareItem> of those things shared by the
		// ...user...
		ShareItemSelectSpec	spec = new ShareItemSelectSpec();
		spec.setSharerIds(users);
		List<ShareItem> shareItems = bs.getSharingModule().getShareItems(spec);

		// ...and finally, convert the List<ShareItem> into a
		// ...List<GwtShareMeItem> and return that.
		List<GwtSharedMeItem> siList = convertItemListToByMeList(bs, request, shareItems, userId, sortBy, sortDescend);
		return siList;
	}
	
	/*
	 * Returns a List<GwtSharedMeItem> of the items shared with the
	 * current user.
	 */
	private static List<GwtSharedMeItem> getSharedWithMeItems(AllModulesInjected bs, HttpServletRequest request, String sortBy, boolean sortDescend) {
		// Construct a list containing just this user's ID...
		Long		userId = GwtServerHelper.getCurrentUserId();
		List<Long>	users  = new ArrayList<Long>();
		users.add(userId);

		// ...get ID's of the groups and teams the user is a member
		// ...of...
		List<Long>	groups = GwtServerHelper.getGroupIds(request, bs, userId);
		List<Long>	teams  = GwtServerHelper.getTeamIds( request, bs, userId);
		
		// ...get the List<ShareItem> of those things shared with the
		// ...user...
		ShareItemSelectSpec	spec = new ShareItemSelectSpec();
		spec.setRecipients(users, groups, teams);
		List<ShareItem> shareItems = bs.getSharingModule().getShareItems(spec);

		// ...and finally, convert the List<ShareItem> into a
		// ...List<GwtShareMeItem> and return that.
		List<GwtSharedMeItem> siList = convertItemListToWithMeList(bs, request, shareItems, userId, teams, groups, sortBy, sortDescend);
		return siList;
	}
	
	/*
	 * Returns a non-null List<ShareAccessInfo> build from a share
	 * list.
	 */
	private static List<ShareAccessInfo> getShareAccessListFromShares(List<GwtPerShareInfo> perShareInfos) {
		// Allocate a List<ShareAccessInfo> to return.
		List<ShareAccessInfo> reply = new ArrayList<ShareAccessInfo>();

		// Scan the shares...
		for (GwtPerShareInfo psi:  perShareInfos) {
			// ...creating a ShareAccessInfo for each share.
			String access;
			ShareRights	rights = psi.getRights();
			if (null == rights) {
				access = "";
			}
			else {
				switch (rights.getAccessRights()) {
				default:
				case UNKNOWN:      access = "";                                        break;
				case CONTRIBUTOR:  access = NLT.get("collections.access.contributor"); break;
				case EDITOR:       access = NLT.get("collections.access.editor");      break;
				case VIEWER:       access = NLT.get("collections.access.viewer");      break;
				}
			}
			if (rights.getCanShareForward()) {
				if (0 < access.length()) {
					access += " + ";
				}
				access += NLT.get("collections.access.sharer");
			}
			reply.add(new ShareAccessInfo(access));
		}

		// If we get here, reply refers to the List<ShareAccessInfo>
		// corresponding to the share list.  Return it.
		return reply;
	}
	
	/*
	 * Returns a non-null List<ShareDateInfo> build from a share
	 * list.
	 */
	private static List<ShareDateInfo> getShareDateListFromShares(List<GwtPerShareInfo> perShareInfos) {
		// Allocate a List<ShareDateInfo> to return.
		List<ShareDateInfo> reply = new ArrayList<ShareDateInfo>();

		// Scan the shares...
		for (GwtPerShareInfo psi:  perShareInfos) {
			// ...creating an ShareDateInfo for each share.
			Date	shareDate  = psi.getShareDate();
			String	dateString = ((null == shareDate) ? "" : GwtServerHelper.getDateTimeString(shareDate, DateFormat.MEDIUM, DateFormat.SHORT));
			reply.add(new ShareDateInfo(shareDate, dateString));
		}

		// If we get here, reply refers to the List<ShareDateInfo>
		// corresponding to the share list.  Return it.
		return reply;
	}
	
	/*
	 * Returns a non-null List<ShareExpirationInfo> build from a share
	 * list.
	 */
	private static List<ShareExpirationInfo> getShareExpirationListFromShares(List<GwtPerShareInfo> perShareInfos) {
		// Allocate a List<ShareExpirationInfo> to return.
		List<ShareExpirationInfo> reply = new ArrayList<ShareExpirationInfo>();

		// Scan the shares...
		for (GwtPerShareInfo psi:  perShareInfos) {
			// ...creating an ShareExpirationInfo for each share.
			boolean	isExpired        = psi.isRightsExpired();
			Date	expirationDate   = psi.getRightsExpire();
			String	expirationString = ((null == expirationDate) ? "" : GwtServerHelper.getDateTimeString(expirationDate, DateFormat.MEDIUM, DateFormat.SHORT));
			ShareExpirationInfo	sei = new ShareExpirationInfo(expirationDate, isExpired, expirationString);
			if (isExpired) {
				sei.setAddedStyle("vibe-dataTableShareStringValue-expired");
			}
			reply.add(sei);
		}

		// If we get here, reply refers to the List<ShareExpirationInfo>
		// corresponding to the share list.  Return it.
		return reply;
	}
	
	/**
	 * Returns a SharedViewStateRpcResponseData object
	 * containing the information for shared views.
	 * 
	 * @param bs
	 * @param request
	 * @param ct
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static SharedViewStateRpcResponseData getSharedViewState(AllModulesInjected bs, HttpServletRequest request, CollectionType ct) throws GwtTeamingException {
		try {
			HttpSession hSession = GwtServerHelper.getCurrentHttpSession();
			Boolean showHidden    = ((Boolean) hSession.getAttribute(CACHED_SHARED_VIEW_SHOW_HIDDEN_BASE     + ct.name()));
			Boolean showNonHidden = ((Boolean) hSession.getAttribute(CACHED_SHARED_VIEW_SHOW_NON_HIDDEN_BASE + ct.name()));

			// Construct the SharedViewStateRpcResponseData
			// object to return.
			SharedViewStateRpcResponseData reply =
				new SharedViewStateRpcResponseData(
					((null == showNonHidden) || showNonHidden),	// Default is true.
					((null != showHidden)    && showHidden));	// Default is false.

			// If we get here, reply refers to the
			// SharedViewStateRpcResponseData object
			// containing the information about shared views.  Return
			// it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.getSharedViewState( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}

	/*
	 * Returns a non-null List<ShareMessageInfo> build from a share
	 * list.
	 */
	private static List<ShareMessageInfo> getShareMessageListFromShares(List<GwtPerShareInfo> perShareInfos) {
		// Allocate a List<ShareMessageInfo> to return.
		List<ShareMessageInfo> reply = new ArrayList<ShareMessageInfo>();

		// Scan the shares...
		for (GwtPerShareInfo psi:  perShareInfos) {
			// ...creating an ShareMessageInfo for each share.
			reply.add(new ShareMessageInfo(psi.getComment()));
		}

		// If we get here, reply refers to the List<ShareMessageInfo>
		// corresponding to the share list.  Return it.
		return reply;
	}

	/**
	 * Returns a StringRpcResponseData containing the URL for viewing
	 * the trash on the given BinderInfo.
	 * 
	 * @param bs
	 * @param request
	 * @param bi
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static StringRpcResponseData getTrashUrl(AllModulesInjected bs, HttpServletRequest request, BinderInfo bi) throws GwtTeamingException {
		try {
			// Construct the URL for viewing the trash on this BinderInfo...
			Binder binder    = bs.getBinderModule().getBinderWithoutAccessCheck(bi.getBinderIdAsLong());
			String binderUrl = PermaLinkUtil.getPermalink(request, binder);
			String trashUrl  = GwtUIHelper.getTrashPermalink(binderUrl);
			
			// ...and wrap it in a StringRpcResponseData.
			StringRpcResponseData reply = new StringRpcResponseData(trashUrl);
			
			// If we get here, reply refers to the
			// StringRpcResponseData object containing the URL for
			// viewing the trash on the given BinderInfo.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.getTrashUrl( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}
	
	/*
	 * Returns a Map of the search results for users based on the
	 * criteria in the options Map.
	 */
	@SuppressWarnings("unchecked")
	private static Map getUserEntries(AllModulesInjected bs, HttpServletRequest request, Binder binder, String quickFilter, Map options) {
		return bs.getProfileModule().getUsers(options);
	}
	
	/**
	 * Returns a UserPropertiesRpcRequestData containing information
	 * managing a user.
	 * 
	 * @param bs
	 * @param request
	 * @param userId
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	@SuppressWarnings("unchecked")
	public static UserPropertiesRpcResponseData getUserProperties(final AllModulesInjected bs, final HttpServletRequest request, final Long userId) throws GwtTeamingException {
		try {
			// Construct a user properties response containing the
			// user's profile that we can return...
			final UserPropertiesRpcResponseData reply = new UserPropertiesRpcResponseData(
				getProfileEntryInfo(
					bs,
					request,
					userId));

			// ...add information about what the user's account...
			ProfileModule	pm     = bs.getProfileModule();
			final User		user   = ((User) pm.getEntry(userId));
			IdentityInfo	userII = user.getIdentityInfo();
			AccountInfo		ai     = new AccountInfo();
			reply.setAccountInfo(ai);
			ai.setLoginId(user.getName());
			ai.setFromOpenId(userII.isFromOpenid());
			ai.setUserType(getUserType(user));

			// ...if the user's from LDAP...
			if (userII.isFromLdap()) {
				// ...add their LDAP DN and eDirectory container, if
				// ...available...
				String ldapDN = user.getForeignName();
				ai.setLdapDN(                            ldapDN );
				ai.setEDirContainer(getParentContainerDN(ldapDN));
			}

			// ...if we can determine the last time the user logged
			// ...in...
			Date lastLogin = getLastUserLogin(bs, userId);
			if (null != lastLogin) {
				// ...add that to the reply.
				ai.setLastLogin(GwtServerHelper.getDateTimeString(lastLogin));
			}
			
			// ...add whether the user has adHoc folder access...
			boolean hasAdHocFolders = ((!user.isShared()) && user.getIdentityInfo().isInternal() && user.isPerson());
			boolean perUserAdHoc    = false;
			if (hasAdHocFolders) {
				hasAdHocFolders = (!(GwtServerHelper.useHomeAsMyFiles(bs, user)));
				perUserAdHoc    = (null != pm.getUserProperties(userId).getProperty(ObjectKeys.USER_PROPERTY_ALLOW_ADHOC_FOLDERS));
			}
			ai.setHasAdHocFolders(hasAdHocFolders);
			ai.setPerUserAdHoc(   perUserAdHoc   );

			// ...add the user's current workspace sharing rights...
			List<Long> userIds = new ArrayList<Long>();
			userIds.add(userId);
			UserSharingRightsInfoRpcResponseData sharingRights = GwtServerHelper.getUserSharingRightsInfo(bs, request, userIds);
			reply.setSharingRights(sharingRights.getUserRights(userId));

			// ...if quotas are enabled...
			final AdminModule am = bs.getAdminModule();
			if (am.isQuotaEnabled()) {
				QuotaInfo qi = new QuotaInfo();
				if (am.testAccess(AdminOperation.manageFunction)) {
					// ...add the manage quotas URL if the user has
					// ...rights to manage them...
					AdaptedPortletURL url = new AdaptedPortletURL(request, "ss_forum", false);
					url.setParameter(WebKeys.ACTION, WebKeys.ACTION_MANAGE_QUOTAS);
					qi.setManageQuotasUrl(url.toString());
				}
				
				long userQuota = user.getDiskQuota();
				if (0 == userQuota) {
					userQuota = user.getMaxGroupsQuota();
					if (0 == userQuota) {
						ZoneConfig zc = getCoreDao().loadZoneConfig(RequestContextHolder.getRequestContext().getZoneId());
						userQuota = zc.getDiskQuotaUserDefault();
						if (0 < userQuota) {
							qi.setZoneQuota(true);	// true -> The user quota came from the default zone assignment.
						}
					}
					else {
						qi.setGroupQuota(true);	// true -> The user quota came from a group assignment.
					}
				}
				
				// ...add information about their quota...
				qi.setUserQuota(userQuota);
				reply.setQuotaInfo(qi);
			}

			// ...add a NetFolderInfo...
			final NetFoldersInfo nfi = new NetFoldersInfo();
			reply.setNetFoldersInfo(nfi);
			if (am.testAccess(AdminOperation.manageFunction)) {
				// ...including whether the user can manage net
				// ...folders...
				try {
					Binder netFoldersParentBinder = SearchUtils.getNetFoldersRootBinder();
					nfi.setCanManageNetFolders(
						(null != netFoldersParentBinder) &&
						LicenseChecker.isAuthorizedByLicense("com.novell.teaming.module.folder.MirroredFolder") &&
						bs.getBinderModule().testAccess(netFoldersParentBinder, BinderOperation.modifyBinder));
				}
				catch (Exception ex) {
					// Ignore.  If we can't access it, the user can't
					// manage net folders.
				}
			}

			// ...some of the information required has to be done as
			// ...the target user...
			RunasTemplate.runas(
				new RunasCallback() {
					@Override
					public Object doAs() {
						// ...if the user has a home folder...
						Long homeId = GwtServerHelper.getHomeFolderId(bs, user);
						if (null != homeId) {
							String			rootPath = null;
							Folder			home     = bs.getFolderModule().getFolder(homeId);
							ResourceDriver	rd       = home.getResourceDriver();
							if (null != rd) {
								String dc = rd.getClass().getName();
								if (MiscUtil.hasString(dc) && dc.equals("com.novell.teaming.fi.connection.file.FileResourceDriver")) {
									rootPath = rd.getRootPath();
								}
								else {
									ResourceDriverConfig rdConfig = rd.getConfig();
									if (null != rdConfig) {
										rootPath = rdConfig.getRootPath();
									}
								}
							}

							// ...add information about the home folder...
							HomeInfo hi = new HomeInfo();
							hi.setId(          homeId                );
							hi.setRelativePath(home.getResourcePath());
							hi.setRootPath(    rootPath              );
							reply.setHomeInfo(hi);
						}

						Map			nfSearch = getCollectionEntries(bs, request, null, null, new HashMap(), CollectionType.NET_FOLDERS, null);
						List<Map>	nfList   = ((List<Map>) nfSearch.get(ObjectKeys.SEARCH_ENTRIES));
						if (MiscUtil.hasItems(nfList)) {
							Long nfBinderId = SearchUtils.getNetFoldersRootBinder().getId();
							for (Map nfMap:  nfList) {
								// ...adding an EntryTitleInfo for each
								// ...to the reply.
								Long			docId  = Long.parseLong(GwtServerHelper.getStringFromEntryMap(nfMap, Constants.DOCID_FIELD));
								String			title  = GwtServerHelper.getStringFromEntryMap(nfMap, Constants.TITLE_FIELD);
								EntryTitleInfo	eti = new EntryTitleInfo();
								eti.setSeen(true);
								eti.setTitle(MiscUtil.hasString(title) ? title : ("--" + NLT.get("entry.noTitle") + "--"));
								eti.setEntityId(new EntityId(nfBinderId, docId, EntityId.FOLDER));
								String description = getEntryDescriptionFromMap(request, nfMap);
								if (MiscUtil.hasString(description)) {
									eti.setDescription(description);
									String descriptionFormat = GwtServerHelper.getStringFromEntryMap(nfMap, Constants.DESC_FORMAT_FIELD);
									eti.setDescriptionIsHtml(MiscUtil.hasString(descriptionFormat) && descriptionFormat.equals(String.valueOf(Description.FORMAT_HTML)));
								}
								else {
									description = GwtServerHelper.getStringFromEntryMap(nfMap, Constants.ENTITY_PATH);
									if (MiscUtil.hasString(description)) {
										eti.setDescription(      description);
										eti.setDescriptionIsHtml(false      );
									}
								}
								nfi.addNetFolder(eti);
							}
						}
						return null;	// Not used.  Doesn't matter what we return.
					}
				},
				WebHelper.getRequiredZoneName(request),
				userId);
			
			// If we get here, reply refers to a
			// UserPropertiesRpcResponseData containing the properties
			// for managing the user.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.getUserProperties( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}

	/*
	 * Given a User, returns the type of that user.
	 */
	private static UserType getUserType(User user) {
		// Are they an internal user?
		UserType reply;
		IdentityInfo ui = user.getIdentityInfo();
		if (ui.isInternal()) {
			// Yes!  Are they from LDAP?
			if (ui.isFromLdap()) {
				// Yes!
				reply = UserType.INTERNAL_LDAP;
			}
			else {
				// No, they're not from LDAP!  Is it a person?
				if (user.isPerson()) {
					// Yes!
					if (user.isReserved() && user.getInternalId().equalsIgnoreCase(ObjectKeys.SUPER_USER_INTERNALID))
					     reply = UserType.INTERNAL_PERSON_ADMIN;
					else reply = UserType.INTERNAL_PERSON_OTHERS;
				}
				else {
					// No, it's not a person!
					reply = UserType.INTERNAL_SYSTEM;
				}
			}
		}
		else {
			// No, it's not an internal user!  Is it the Guest user?
			if (user.isShared())
			     reply = UserType.EXTERNAL_GUEST;
			else reply = UserType.EXTERNAL_OTHERS;
		}
		
		// If we get here, reply refers to the UserType of the User.
		// Return it.
		return reply;
	}
	
	/*
	 * Given an EntityId that describes a user, returns the type of
	 * user.
	 */
	private static UserType getUserType(AllModulesInjected bs, HttpServletRequest request, EntityId entityId) {
		UserType reply = UserType.UNKNOWN;
		
		try {
			// Can we access the User?
			Principal p = bs.getProfileModule().getEntry(entityId.getEntityId());
			if ((null != p) && (p instanceof User)) {
				// Yes!  What type are they?
				reply = getUserType(((User) p));
			}
		}
		
		catch (Exception ex) {
			// Log the exception.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.getUserType( SOURCE EXCEPTION ):  ", ex);
			}
		}
		
		// If we get here, reply refers to the UserType of the
		// EntityId.  Return it.
		return reply;
	}
	
	/**
	 * Returns true if the user is viewing pinned entries on a given
	 * folder and false otherwise.
	 * 
	 * @param request
	 * @param folderId
	 * 
	 * @return
	 */
	public static boolean getUserViewPinnedEntries(HttpServletRequest request, Long folderId) {
		HttpSession session = WebHelper.getRequiredSession(request);
		Boolean viewPinnedEntries = ((Boolean) session.getAttribute(CACHED_VIEW_PINNED_ENTRIES_BASE + folderId));
		return ((null != viewPinnedEntries) && viewPinnedEntries);
	}
	
	/**
	 * Returns true if the user is viewing shared files (vs. all
	 * entries) on a given collection and false otherwise.
	 * 
	 * @param request
	 * @param collectionType
	 * 
	 * @return
	 */
	public static boolean getUserViewSharedFiles(HttpServletRequest request, CollectionType collectionType) {
		HttpSession session = WebHelper.getRequiredSession(request);
		Boolean viewSharedFiles = ((Boolean) session.getAttribute(CACHED_VIEW_SHARED_FILES_BASE + collectionType.ordinal()));
		return ((null == viewSharedFiles) || viewSharedFiles);
	}
	
	/**
	 * Returns a ViewInfo used to control folder views based on a URL.
	 * 
	 * @param bs
	 * @param request
	 * @param url
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException 
	 */
	@SuppressWarnings("unchecked")
	public static ViewInfo getViewInfo(AllModulesInjected bs, HttpServletRequest request, String url) throws GwtTeamingException {
		// Trace the URL we're working with.
		m_logger.debug("GwtViewHelper.getViewInfo():  " + url);

		// Can we parse the URL?
		Map<String, String> nvMap = getQueryParameters(url);
		if (!(MiscUtil.hasItems(nvMap))) {
			// No!  Then we can't get a ViewInfo.
			m_logger.debug("GwtViewHelper.getViewInfo():  1:Could not determine a view.");
			return null;
		}

		// Construct a ViewInfo we can setup with the information for
		// viewing this URL.
		ViewInfo vi = new ViewInfo();
		vi.setBaseBinderId(getQueryParameterLong(nvMap, WebKeys.URL_BINDER_ID));

		// Is there an operation on the URL?
		String op = getQueryParameterString(nvMap, WebKeys.URL_OPERATION);
		if (null == op) op = "";
		
		// What action is the URL requesting?
		String action = getQueryParameterString(nvMap, WebKeys.URL_ACTION).toLowerCase();		
		if (action.equals(WebKeys.ACTION_VIEW_PERMALINK)) {
			// A view on a permalink!  What type of entity is being
			// viewed?
			String entityType = getQueryParameterString(nvMap, WebKeys.URL_ENTITY_TYPE).toLowerCase();
			if (entityType.equals(EntityType.user.name())) {
				// A user!  Can we access the user?
				Long entryId = getQueryParameterLong(nvMap, WebKeys.URL_ENTRY_ID);			
				if (!(initVIFromUser(request, bs, GwtServerHelper.getUserFromId(bs, entryId), vi))) {
					m_logger.debug("GwtViewHelper.getViewInfo():  2:Could not determine a view.");
					return null;
				}
			}
			else {
				boolean isProfiles = entityType.equals(EntityType.profiles.name());
				if (entityType.equals(EntityType.folder.name()) || entityType.equals(EntityType.workspace.name()) || isProfiles) {
					// A folder, workspace or the profiles binder!  Is
					// this a request to show team members on this binder?
					if ((!isProfiles) && op.equals(WebKeys.OPERATION_SHOW_TEAM_MEMBERS)) {
						// Yes!  Simply mark the ViewInfo as such.
						vi.setViewType(ViewType.ADD_FOLDER_ENTRY);
					}
					
					// Setup a binder view based on the binder ID.
					else if (!(initVIFromBinderId(request, bs, nvMap, WebKeys.URL_BINDER_ID, vi))) {
						m_logger.debug("GwtViewHelper.getViewInfo():  3:Could not determine a view.");
						return null;
					}
				}
			}
		}
		
		else if (action.equals(WebKeys.ACTION_VIEW_WS_LISTING)     ||
		         action.equals(WebKeys.ACTION_VIEW_FOLDER_LISTING) ||
		         action.equals(WebKeys.ACTION_VIEW_PROFILE_LISTING)) {
			// A view workspace, folder or profiles listing!  Is this a
			// request to show team members on this binder?
			if (op.equals(WebKeys.OPERATION_SHOW_TEAM_MEMBERS)) {
				// Yes!  Simply mark the ViewInfo as such.
				vi.setViewType(ViewType.ADD_FOLDER_ENTRY);
			}
			
			else {
				// No, it's not a request to show team members!  Are we
				// viewing a folder while changing its view?
				boolean			viewFolderListing = action.equals(WebKeys.ACTION_VIEW_FOLDER_LISTING);
				ProfileModule	pm                = bs.getProfileModule();
				User			user              = GwtServerHelper.getCurrentUser();
				Long			userId            = user.getId();
				if (viewFolderListing && op.equals(WebKeys.OPERATION_SET_DISPLAY_DEFINITION)) {
					// Yes!  Do we have both the view definition and
					// binder ID's?
					String defId    = getQueryParameterString(nvMap, WebKeys.URL_VALUE    );
					Long   binderId = getQueryParameterLong(  nvMap, WebKeys.URL_BINDER_ID);
					if (MiscUtil.hasString(defId) && (null != binderId)) {
						// Yes!  Put the view into affect.
						pm.setUserProperty(userId, binderId, ObjectKeys.USER_PROPERTY_DISPLAY_DEFINITION, defId);
					}
				}
				
				// Setup a binder view based on the binder ID.
				if (!(initVIFromBinderId(request, bs, nvMap, WebKeys.URL_BINDER_ID, vi))) {
					m_logger.debug("GwtViewHelper.getViewInfo():  4:Could not determine a view.");
					return null;
				}
	
				// Is this a view folder listing?
				BinderInfo	viBI     = vi.getBinderInfo();
				Long		binderId = viBI.getBinderIdAsLong();
				if (viewFolderListing) {
					// Yes!  Does it also contain changes to the folder
					// sorting?
					String sortBy       = getQueryParameterString(nvMap, WebKeys.FOLDER_SORT_BY);		
					String sortDescendS = getQueryParameterString(nvMap, WebKeys.FOLDER_SORT_DESCEND);
					if (MiscUtil.hasString(sortBy) && MiscUtil.hasString(sortDescendS)) {
						// Yes!  Apply the sort changes.
						Boolean sortDescend = Boolean.parseBoolean(sortDescendS);
						GwtServerHelper.saveFolderSort(
							bs,
							viBI,
							sortBy,
							(!sortDescend));
					}
					
					// If the request contain changes to the task
					// filtering...
					String taskFilterType = getQueryParameterString(nvMap, WebKeys.TASK_FILTER_TYPE);
					if (MiscUtil.hasString(taskFilterType)) {
						// ...put them into effect.
						TaskHelper.setTaskFilterType(bs, userId, binderId, FilterType.valueOf(taskFilterType));
					}
					String folderModeType = getQueryParameterString(nvMap, WebKeys.FOLDER_MODE_TYPE);
					if (MiscUtil.hasString(folderModeType)) {
						// ...put them into effect.
						ListFolderHelper.setFolderModeType(bs, userId, binderId, ModeType.valueOf(folderModeType));
					}
					
					// Is the URL to view a specific entry within the
					// folder?
					String  entryViewStyle = getQueryParameterString(nvMap, WebKeys.URL_ENTRY_VIEW_STYLE);
					String  entryId        = getQueryParameterString(nvMap, WebKeys.URL_ENTRY_ID        );
					if (MiscUtil.hasString(entryViewStyle) && MiscUtil.hasString(entryId)) {
						// Yes!  If we're not ignoring the entry
						// view...
						boolean entryViewIgnore = GwtClientHelper.hasString(getQueryParameterString(nvMap, WebKeys.URL_ENTRY_VIEW_IGNORE));
						if (!entryViewIgnore) {
							// ...adjust the ViewInfo accordingly.
							vi.setViewType(ViewType.BINDER_WITH_ENTRY_VIEW);
							vi.setEntryViewUrl(
								GwtServerHelper.getViewFolderEntryUrl(
									bs,
									request,
									binderId,
									Long.parseLong(entryId),
									isQueryParamSet(nvMap, WebKeys.URL_INVOKE_SHARE,     "1"),
									isQueryParamSet(nvMap, WebKeys.URL_INVOKE_SUBSCRIBE, "1")));
							vi.setInvokeShare(    false);	// We'll invoke any share     on the entry, not the binder.
							vi.setInvokeSubscribe(false);	// We'll invoke any subscribe on the entry, not the binder.
						}

						// Is the folder this entry is located in
						// accessible to the user?
						BinderInfo bi = vi.getBinderInfo();
						if (!(bi.isBinderAccessible())) {
							// No!  Set the entry view's underlying
							// context to the user's Shared With Me
							// view.
							bi = GwtServerHelper.getBinderInfo(bs, request, user.getWorkspaceId());
							bi.setWorkspaceType( WorkspaceType.NOT_A_WORKSPACE);
							bi.setFolderType(    FolderType.NOT_A_FOLDER      );
							bi.setBinderType(    BinderType.COLLECTION        );
							bi.setCollectionType(CollectionType.SHARED_WITH_ME);
							vi.setBinderInfo(    bi                           );
						}
					}
				}
				
				// Does it contain a filter operation?
				if (op.equals(WebKeys.URL_SELECT_FILTER)) {
					// Yes, a filter selection!  Apply the selection.
					String filterName = MiscUtil.replace(getQueryParameterString(nvMap, WebKeys.URL_SELECT_FILTER), "+", " ");
					String op2 = getQueryParameterString(nvMap, WebKeys.URL_OPERATION2);
					String filterScope;
					if (MiscUtil.hasString(op2) && op2.equals(ObjectKeys.USER_PROPERTY_USER_FILTER_GLOBAL))
					     filterScope = ObjectKeys.USER_PROPERTY_USER_FILTER_GLOBAL;
					else filterScope = ObjectKeys.USER_PROPERTY_USER_FILTER_PERSONAL;
					pm.setUserProperty(userId, binderId, ObjectKeys.USER_PROPERTY_USER_FILTER,       filterName );
					pm.setUserProperty(userId, binderId, ObjectKeys.USER_PROPERTY_USER_FILTER_SCOPE, filterScope);
	
					UserProperties userBinderProperties = bs.getProfileModule().getUserProperties(GwtServerHelper.getCurrentUserId(), binderId);
					List<String> currentFilters = ((List<String>) userBinderProperties.getProperty(ObjectKeys.USER_PROPERTY_USER_FILTERS));
					if (null == currentFilters) {
						currentFilters = new ArrayList<String>();
					}
					if (MiscUtil.hasString(filterName)) {
						String filterSpec = BinderFilter.buildFilterSpec(filterName, filterScope);
						if (!(currentFilters.contains(filterSpec))) {
							currentFilters.add(filterSpec);
						}
					}
					else {
						currentFilters.clear();
					}
					pm.setUserProperty(userId, binderId, ObjectKeys.USER_PROPERTY_USER_FILTERS, currentFilters);
				}
				
				if (op.equals(WebKeys.URL_CLEAR_FILTER)) {
					// Yes, a filter clear!  Apply the selection.
					String op2 = getQueryParameterString(nvMap, WebKeys.URL_OPERATION2);
					String filterScope;
					if (MiscUtil.hasString(op2) && op2.equals(ObjectKeys.USER_PROPERTY_USER_FILTER_GLOBAL))
					     filterScope = ObjectKeys.USER_PROPERTY_USER_FILTER_GLOBAL;
					else filterScope = ObjectKeys.USER_PROPERTY_USER_FILTER_PERSONAL;
					pm.setUserProperty(userId, binderId, ObjectKeys.USER_PROPERTY_USER_FILTER,       ""         );
					pm.setUserProperty(userId, binderId, ObjectKeys.USER_PROPERTY_USER_FILTER_SCOPE, filterScope);
	
					String filterName = MiscUtil.replace(getQueryParameterString(nvMap, WebKeys.URL_CLEAR_FILTER), "+", " ");
					String filterSpec = BinderFilter.buildFilterSpec(filterName, filterScope);
					UserProperties userBinderProperties = bs.getProfileModule().getUserProperties(GwtServerHelper.getCurrentUserId(), binderId);
					List<String> currentFilters = ((List<String>) userBinderProperties.getProperty(ObjectKeys.USER_PROPERTY_USER_FILTERS));
					if (null == currentFilters) {
						currentFilters = new ArrayList<String>();
					}
					currentFilters.remove(filterSpec);
					pm.setUserProperty(userId, binderId, ObjectKeys.USER_PROPERTY_USER_FILTERS, currentFilters);
				}
			}
		}

		else if (action.equals(WebKeys.ACTION_ADD_FOLDER_ENTRY)) {
			// An add folder entry!  Simply mark the ViewInfo as such.
			vi.setViewType(ViewType.ADD_FOLDER_ENTRY);
		}
		
		else if (action.equals(WebKeys.ACTION_ADD_BINDER)) {
			// An add binder!  Simply mark the ViewInfo as such.
			vi.setViewType(ViewType.ADD_BINDER);
		}
		
		else if (action.equals(WebKeys.ACTION_ADD_PROFILE_ENTRY)) {
			// An add profile entry!  Simply mark the ViewInfo as such.
			vi.setViewType(ViewType.ADD_PROFILE_ENTRY);
		}
		
		else if (action.equals(WebKeys.ACTION_ADVANCED_SEARCH)) {
			// An advanced search!  Simply mark the ViewInfo as such.
			vi.setViewType(ViewType.ADVANCED_SEARCH);
		}
		
		else if (action.equals(WebKeys.ACTION_BUILD_FILTER)) {
			// A build filter!  Simply mark the ViewInfo as such.
			vi.setViewType(ViewType.BUILD_FILTER);
		}
		
		else if (action.equals(WebKeys.ACTION_MODIFY_BINDER)) {
			// A modify binder!  Simply mark the ViewInfo as such.
			vi.setViewType(ViewType.MODIFY_BINDER);
		}
		
		else if (action.equals(WebKeys.ACTION_VIEW_PROFILE_ENTRY)) {
			// A view profile entry!  Simply mark the ViewInfo as such.
			vi.setViewType(ViewType.VIEW_PROFILE_ENTRY);
		}
		
		else if (action.equals(WebKeys.ACTION_VIEW_FOLDER_ENTRY)) {
			// A view folder entry!  Construct a ViewFolderEntryInfo
			// for it...
			Long				binderId = getQueryParameterLong(nvMap, WebKeys.URL_BINDER_ID);
			Long				entryId  = getQueryParameterLong(nvMap, WebKeys.URL_ENTRY_ID );
			ViewFolderEntryInfo	vfei     = buildViewFolderEntryInfo(bs, request, binderId, entryId);
			
			// ...mark the ViewInfo as such...
			vi.setViewFolderEntryInfo(vfei                 );
			vi.setViewType(           ViewType.FOLDER_ENTRY);
			
			// ...if we're supposed to invoke the share dialog on this
			// ...entry...
			boolean invokeShare = isQueryParamSet(nvMap, WebKeys.URL_INVOKE_SHARE, "1");
			if (invokeShare) {
				// ...mark the ViewInfo accordingly...
				FolderEntry fe = GwtUIHelper.getEntrySafely(bs.getFolderModule(), binderId, entryId);
				vi.setInvokeShare((null != fe) && GwtShareHelper.isEntitySharable(bs, fe));
			}
			
			// ...if we're supposed to invoke the subscribe dialog on
			// ...this entry...
			boolean invokeSubscribe = isQueryParamSet(nvMap, WebKeys.URL_INVOKE_SUBSCRIBE, "1");
			if (invokeSubscribe) {
				// ...mark the ViewInfo accordingly.
				vi.setInvokeSubscribe(true);
			}
		}
		
		// If we get here reply refers to the BinderInfo requested or
		// is null.  Return it.
		if (m_logger.isDebugEnabled()) {
			dumpViewInfo(vi);
		}
		return vi;
	}

	/*
	 * Returns the URL to use to view a user's profile entry.
	 */
	private static String getViewProfileEntryUrl(AllModulesInjected bs, HttpServletRequest request, Long userId) {
		AdaptedPortletURL url = new AdaptedPortletURL(request, "ss_forum", true);
		url.setParameter(WebKeys.URL_BINDER_ID,        String.valueOf(bs.getProfileModule().getProfileBinderId()));
		url.setParameter(WebKeys.URL_ACTION,           WebKeys.ACTION_VIEW_PROFILE_ENTRY                         );
		url.setParameter(WebKeys.URL_ENTRY_VIEW_STYLE, WebKeys.URL_ENTRY_VIEW_STYLE_FULL                         );
		url.setParameter(WebKeys.URL_NEW_TAB,          "1"                                                       );
		url.setParameter(WebKeys.URL_ENTRY_ID,         String.valueOf(userId)                                    );
		return url.toString();
	}

	/**
	 * Returns a WhoHasAccessInfoRcpResponseData object containing the
	 * names of the users, groups and teams that have access to an
	 * entity.
	 * 
	 * @param bs
	 * @param request
	 * @param entityId
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	@SuppressWarnings("unchecked")
	public static WhoHasAccessInfoRpcResponseData getWhoHasAccess(AllModulesInjected bs, HttpServletRequest request, EntityId entityId) throws GwtTeamingException {
		try {
			// Allocate an WhoHasAccessInfoRpcResponseData to track the entry
			// types for the requested binders.
			WhoHasAccessInfoRpcResponseData reply = new WhoHasAccessInfoRpcResponseData();
			
			// Is the entity a binder?
			WorkArea workArea;
			if (entityId.isBinder()) {
				// Yes!  We use it directly as the work are. 
				Binder binder = bs.getBinderModule().getBinderWithoutAccessCheck(entityId.getEntityId());
				reply.setEntityTitle(     binder.getTitle()                                         );
				reply.setEntityIcon(      binder.getIconName(),                BinderIconSize.SMALL );
				reply.setEntityIcon(      binder.getIconName(IconSize.MEDIUM), BinderIconSize.MEDIUM);
				reply.setEntityIcon(      binder.getIconName(IconSize.LARGE ), BinderIconSize.LARGE );
				reply.setEntityHomeFolder(binder.isHomeDir()                                        );
				workArea = binder;
			}
			
			else {
				// No, the entity isn't a binder, it must be a folder
				// entry!  Access the top entry in the chain (in case
				// this is a comment, ...)
				FolderEntry	fe = bs.getFolderModule().getEntry(entityId.getBinderId(), entityId.getEntityId());
				reply.setEntityTitle(fe.getTitle());
				FolderEntry	feTop = fe.getTopEntry();
				if (null != feTop) {
					fe = feTop;
				}

				// Is this file entry we can determine a filename for?
				String fName = GwtServerHelper.getFileEntrysFilename(bs, fe);
				if (MiscUtil.hasString(fName)) {
					// Yes!  Store the appropriate icons for it.
					reply.setEntityIcon(FileIconsHelper.getFileIconFromFileName(fName, mapBISToIS(BinderIconSize.SMALL)),  BinderIconSize.SMALL );
					reply.setEntityIcon(FileIconsHelper.getFileIconFromFileName(fName, mapBISToIS(BinderIconSize.MEDIUM)), BinderIconSize.MEDIUM);
					reply.setEntityIcon(FileIconsHelper.getFileIconFromFileName(fName, mapBISToIS(BinderIconSize.LARGE)),  BinderIconSize.LARGE );
				}
				
				// If the entry has its own ACLs, we use it as the work
				// area, otherwise we use the containing binder.
				if (fe.hasEntryAcl())
				     workArea = fe;
				else workArea = bs.getBinderModule().getBinderWithoutAccessCheck(entityId.getBinderId());
			}

			// Get the access control information for the work are.
			Map model = new HashMap();
			model.put(WebKeys.ACCESS_HONOR_INHERITANCE, Boolean.TRUE);
			AccessControlController.setupAccess(bs, workArea, model);
			List groups = ((List) model.get(WebKeys.ACCESS_SORTED_GROUPS));
			List users  = ((List) model.get(WebKeys.ACCESS_SORTED_USERS ));
			
			// If there any groups with access...
			if (MiscUtil.hasItems(groups)) {
				// ...scan them...
				for (Object gO:  groups) {
					// ...and add an AccessInfo for each to the reply.
					Group group = ((Group) gO);
					reply.addGroup(new AccessInfo(group.getId(), group.getTitle()));
				}
			}
			
			// If there are any users with access...
			if (MiscUtil.hasItems(users)) {
				// ...scan them...
				for (Object uO:  users) {
					// ...and add an AccessInfo for each to the reply.
					User user = ((User) uO);
					reply.addUser(
						new AccessInfo(
							user.getId(),
							user.getTitle(),
							"",
							GwtServerHelper.getUserAvatarUrl(
								bs,
								request,
								user)));
				}
			}

			// Has this entity been shared?
			ShareItemSelectSpec	spec = new ShareItemSelectSpec();
			spec.setSharedEntityIdentifier(GwtShareHelper.getEntityIdentifierFromEntityId(entityId));
			spec.setAccountForInheritance(true);
			List<ShareItem> shareItems = bs.getSharingModule().getShareItems(spec);
			if (MiscUtil.hasItems(shareItems)) {
				// Yes!  Scan the shares.
				List<Long> shareGroupIds = new ArrayList<Long>();
				List<Long> shareTeamIds  = new ArrayList<Long>();
				List<Long> shareUserIds  = new ArrayList<Long>();
				for (ShareItem si:  shareItems) {
					// If this share is expired or obsolete...
					if (si.isExpired() || (!(si.isLatest()))) {
						// ...skip it.
						continue;
					}

					// Add the recipient ID to the appropriate list.
					Long recipientId = si.getRecipientId();
					switch (si.getRecipientType()) {
					case group:  shareGroupIds.add(recipientId); break;
					case team:   shareTeamIds.add( recipientId); break;
					case user:   shareUserIds.add( recipientId); break;
					}
				}

				// Are there any users that the item was shared with?
				if (!(shareUserIds.isEmpty())) {
					// Yes!  Can we resolve them?
					List shareUsers = ResolveIds.getPrincipals(shareUserIds, true);
					if (MiscUtil.hasItems(shareUsers)) {
						// Yes!  Scan them...
						for (Object u:  shareUsers) {
							if (u instanceof UserPrincipal) {
								// ...and add an AccessInfo for each to
								// ...the reply.
								User shareUser = ((User) u);
								reply.addUser(
									new AccessInfo(
										shareUser.getId(),
										shareUser.getTitle(),
										"",
										GwtServerHelper.getUserAvatarUrl(
											bs,
											request,
											shareUser)));
							}
						}
					}
				}
				
				// Are there any groups that the item was shared with?
				if (!(shareGroupIds.isEmpty())) {
					// Yes!  Can we resolve them?
					List shareGroups = ResolveIds.getPrincipals(shareGroupIds, true);
					if (MiscUtil.hasItems(shareGroups)) {
						// Yes!  Scan them...
						for (Object g:  shareGroups) {
							if (g instanceof GroupPrincipal) {
								// ...and add an AccessInfo for each to
								// ...the reply.
								Group shareGroup = ((Group) g);
								reply.addGroup(new AccessInfo(shareGroup.getId(), shareGroup.getTitle()));
							}
						}
					}
				}
				
				// Are there any teams that the item was shared with?
				if (!(shareTeamIds.isEmpty())) {
					// Yes!  Scan them.
					for (Long teamId:  shareTeamIds) {
						// Does this team have any members?
						Set<Long> teamMemberIds = GwtServerHelper.getTeamMemberIds(bs, teamId, false);
						if (MiscUtil.hasItems(teamMemberIds)) {
							// Yes!  Can we resolve them?
							List sharePrincipals = ResolveIds.getPrincipals(teamMemberIds);
							if (MiscUtil.hasItems(sharePrincipals)) {
								// Yes!  Scan them...
								for (Object p:  sharePrincipals) {
									// ...and for any users...
									if (p instanceof UserPrincipal) {
										// ...and add an AccessInfo for
										// ...each to the reply.
										User shareUser = ((User) p);
										reply.addUser(
											new AccessInfo(
												shareUser.getId(),
												shareUser.getTitle(),
												"",
												GwtServerHelper.getUserAvatarUrl(
													bs,
													request,
													shareUser)));
									}
									
									// ...and for any groups...
									else if (p instanceof GroupPrincipal) {
										// ...and add an AccessInfo for
										// ...each to the reply.
										Group shareGroup = ((Group) p);
										reply.addGroup(new AccessInfo(shareGroup.getId(), shareGroup.getTitle()));
									}
								}
							}
						}
					}
				}
			}

			// Sort the List<AccessInfo>'s in the object we're
			// returning.
			Comparator<AccessInfo> aiComparator = new AccessInfoComparator();
			Collections.sort(reply.getGroups(), aiComparator);
			Collections.sort(reply.getUsers(),  aiComparator);

			// If we get here, reply refers to the
			// WhoHasAccessInfoRpcResponseData of the entry types for
			// the requested binders.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.getWhoHasAccess( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}
	
	/**
	 * Marks the selected shared entities as being hidden.
	 * 
	 * @param bs
	 * @param request
	 * @param ct
	 * @param entityIds
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static Boolean hideShares(AllModulesInjected bs, HttpServletRequest request, CollectionType ct, List<EntityId> entityIds) throws GwtTeamingException {
		try {
			// Are there any entities to hide?
			if (MiscUtil.hasItems(entityIds)) {
				// Yes!  Scan them...
				SharingModule sm = bs.getSharingModule();
                boolean isSharedWithMe = ct.equals(CollectionType.SHARED_WITH_ME);
                List<EntityIdentifier> ids = new ArrayList<EntityIdentifier>();
				for (EntityId eid:  entityIds) {
                    ids.add(toEntityIdentifier(eid));
				}
                sm.hideSharedEntitiesForCurrentUser(ids, isSharedWithMe);
			}
			
			// If we get here, the hide was successful.  Return true.
			return Boolean.TRUE;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.hideShares( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}
	
	/*
	 * Initializes a ViewInfo based on a binder ID.
	 * 
	 * Returns true if the ViewInfo was initialized and false
	 * otherwise.
	 */
	private static boolean initVIFromBinderId(HttpServletRequest request, AllModulesInjected bs, Map<String, String> nvMap, String binderIdName, ViewInfo vi) {
		// Initialize as a binder based on the user's workspace.
		Long binderId = getQueryParameterLong(nvMap, binderIdName);
		BinderInfo bi = GwtServerHelper.getBinderInfo(bs, request, String.valueOf(binderId));
		if (null == bi) {
			return false;
		}
		
		// Is the binder a workspace?
		User user = GwtServerHelper.getCurrentUser();
		vi.setViewType(ViewType.BINDER);
		if (bi.isBinderWorkspace()) {
			// Yes!  Does it have the showCollection parameter?
			String showCollection = getQueryParameterString(nvMap, WebKeys.URL_SHOW_COLLECTION);
			if (MiscUtil.hasString(showCollection)) {
				// Yes!  If it's other than My Files or it's My Files
				// and we aren't mapping it to their Home, setup a
				// BinderInfo for the collection.  Otherwise, setup a
				// BinderInfo for their Home. 
				Long homeId;
				CollectionType ct = CollectionType.getEnum(showCollection);
				if (CollectionType.MY_FILES.equals(ct) && SearchUtils.useHomeAsMyFiles(bs))
				     homeId = SearchUtils.getHomeFolderId(bs);
				else homeId = null;
				if (null == homeId)
				     bi = GwtServerHelper.buildCollectionBI(ct, user.getWorkspaceId());
				else bi = GwtServerHelper.getBinderInfo(bs, request, String.valueOf(homeId));
			}
		}

		// No, the binder isn't a workspace!  Is it a folder?
		else if (bi.isBinderFolder()) {
			// Yes!  Is it a 'My Files Storage' folder?
			Binder binder = bs.getBinderModule().getBinderWithoutAccessCheck(binderId);
			if ((null != binder) && BinderHelper.isBinderMyFilesStorage(binder)) {
				// Yes!  Is it the current user's?
				Binder userWS          = binder.getParentBinder();
				Long   currentUserWSId = GwtServerHelper.getCurrentUser().getWorkspaceId();
				if (currentUserWSId.equals(userWS.getId())) {
					// Yes!  Are we supposed to redirect requests by
					// the owner of a 'My Files Storage' folder to
					// their 'My Files' collection instead?
					if (SPropsUtil.getBoolean("redirect.owner.myFilesStorage", true)) {
						// Yes!  Redirect.
						bi = GwtServerHelper.buildCollectionBI(
							CollectionType.MY_FILES,
							currentUserWSId);
					}
				}
				else  {
					// No, this isn't the current user's workspace!  It
					// must be somebody else's.  Are we supposed to
					// redirect requests to somebody else's 'My Files
					// Storage' folder to their profile instead?
					if (SPropsUtil.getBoolean("redirect.other.myFilesStorage", true)) {
						// Yes!  Redirect.
						vi.setViewType(ViewType.BINDER);
						vi.setOverrideUrl(GwtProfileHelper.getUserProfileUrl(request, userWS));
						bi = GwtServerHelper.getBinderInfo(bs, request, userWS.getId());
					}
				}
			}
		}
		
		// Store any BinderInfo change we made in the ViewInfo.
		vi.setBinderInfo(bi);

		// Are we showing the trash on a this binder?
		boolean showTrash = getQueryParameterBoolean(nvMap, WebKeys.URL_SHOW_TRASH);
		if (showTrash) {
			// Yes!  Update the folder/workspace type
			// accordingly.
			if      (bi.isBinderFolder())    bi.setFolderType(   FolderType.TRASH   );
			else if (bi.isBinderWorkspace()) bi.setWorkspaceType(WorkspaceType.TRASH);
		}

		// Are we supposed to invoke the share dialog on this binder?
		if (isQueryParamSet(nvMap, WebKeys.URL_INVOKE_SHARE, "1")) {
			// Yes!  Mark the ViewInfo accordingly.
			Binder binder = GwtUIHelper.getBinderSafely(bs.getBinderModule(), binderId);
			vi.setInvokeShare((null != binder) && GwtShareHelper.isEntitySharable(bs, binder));
		}
		
		// Are we supposed to invoke the subscribe dialog on this binder?
		if (isQueryParamSet(nvMap, WebKeys.URL_INVOKE_SUBSCRIBE, "1")) {
			// Yes!  Mark the ViewInfo accordingly.
			vi.setInvokeSubscribe(true);
		}

		// Finally, return true since if we get here, the view has been
		// initialized.
		return true;
	}
	
	/*
	 * Initializes a ViewInfo based on a User.
	 * 
	 * Returns true if the ViewInfo was initialized and false
	 * otherwise.
	 */
	private static boolean initVIFromUser(HttpServletRequest request, AllModulesInjected bs, User user, ViewInfo vi) {
		// Were we given a User object to initialize from?
		if (null == user) {
			// No!  Bail.
			return false;
		}

		// Initialize as a binder based on the user's workspace.
		BinderInfo bi = GwtServerHelper.getBinderInfo(bs, request, String.valueOf(user.getWorkspaceId()));
		if (null == bi) {
			return false;
		}
		vi.setViewType(ViewType.BINDER);
		vi.setBinderInfo(bi);
		
		return true;
	}

	/*
	 * Returns true if a FolderRow with the specified entity ID is in
	 * a List<FolderRow> or false otherwise.
	 */
	private static boolean isEntityInList(EntityId entityId, List<FolderRow> folderRows) {
		// Scan the List<FolderRow>.
		for (FolderRow fr:  folderRows) {
			// Do entry entity ID's and entity type's match?
			EntityId rowEID = fr.getEntityId();
			if (rowEID.getBinderId().equals(  entityId.getBinderId()) &&
				rowEID.getEntityId().equals(  entityId.getEntityId()) &&
				rowEID.getEntityType().equals(entityId.getEntityType())) {
				// Yes!  Then we found it.  Return true.
				return true;
			}
		}
		
		// If we get here, we couldn't find the entry in question.
		// Return false.
		return false;
	}

	/*
	 * Returns true if a query parameter is set to a specific value and
	 * false otherwise.
	 */
	private static boolean isQueryParamSet(Map<String, String> nvMap, String name, String value) {
		String qp = getQueryParameterString(nvMap, name);
		return (MiscUtil.hasString(qp) && qp.trim().equalsIgnoreCase(value.trim()));
	}

	/*
	 * Returns true if a DefinableEnity is tagged as a hidden share and
	 * false otherwise.
	 */
	private static boolean isSharedEntityHidden(AllModulesInjected bs, CollectionType ct, DefinableEntity siEntity) {
		// Does the entity have any personal tags defined on it?
		boolean isEntry = siEntity.getEntityType().equals(EntityType.folderEntry);
		ArrayList<TagInfo> entityTags;
		if (isEntry)
		     entityTags = GwtServerHelper.getEntryTags( bs, ((FolderEntry) siEntity), false, true);
		else entityTags = GwtServerHelper.getBinderTags(bs, ((Binder)      siEntity), false, true);
		if (MiscUtil.hasItems(entityTags)) {
			// Yes!  Scan them.
			for (TagInfo ti:  entityTags) {
				// Is this tag marking this entry as being hidden?
				boolean isHidden;
				if (ct.equals(CollectionType.SHARED_BY_ME))
				     isHidden = ti.isHiddenSharedByTag();
				else isHidden = ti.isHiddenSharedWithTag();
				if (isHidden) {
					// Yes!  Return true, that's all we're looking for.
					return true;
				}
			}
		}
		
		// If we get here, the entity is not marked as being hidden.
		// Return false.
		return false;
	}
	
	/**
	 * Locks the entries.
	 * 
	 * @param bs
	 * @param request
	 * @param entityIds
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static ErrorListRpcResponseData lockEntries(AllModulesInjected bs, HttpServletRequest request, List<EntityId> entityIds) throws GwtTeamingException {
		try {
			// Allocate an error list response we can return.
			ErrorListRpcResponseData reply = new ErrorListRpcResponseData(new ArrayList<ErrorInfo>());

			// Were we given the IDs of any entries to lock?
			if (MiscUtil.hasItems(entityIds)) {
				// Yes!  Scan them.
				for (EntityId entityId:  entityIds) {
					// If the entity is a binder...
					if (entityId.isBinder()) {
						// ...skip it.
						continue;
					}
					
					try {
						// Can we lock this entry?
						bs.getFolderModule().reserveEntry(entityId.getBinderId(), entityId.getEntityId());
					}

					catch (Exception e) {
						// No!  Add an error  to the error list.
						String entryTitle = GwtServerHelper.getEntityTitle(bs, entityId);
						String messageKey;
						if      (e instanceof AccessControlException)           messageKey = "lockEntryError.AccssControlException";
						else if (e instanceof ReservedByAnotherUserException)   messageKey = "lockEntryError.ReservedByAnotherUserException";
						else if (e instanceof FilesLockedByOtherUsersException) messageKey = "lockEntryError.FilesLockedByOtherUsersException";
						else                                                    messageKey = "lockEntryError.OtherException";
						reply.addError(NLT.get(messageKey, new String[]{entryTitle}));
					}
				}
			}

			// If we get here, reply refers to an
			// ErrorListRpcResponseData containing any errors we
			// encountered.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.lockEntries( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}

	/**
	 * Maps a BinderIconSize enumeration value to its equivalent
	 * IconSize.
	 * 
	 * @param bis
	 * 
	 * @return
	 */
	public static IconSize mapBISToIS(BinderIconSize bis) {
		IconSize reply;
		switch (bis) {
		case SMALL:   reply = IconSize.SMALL;     break;
		case MEDIUM:  reply = IconSize.MEDIUM;    break;
		case LARGE:   reply = IconSize.LARGE;     break;
		default:      reply = IconSize.UNDEFINED; break;
		}
		return reply;
	}

	/**
	 * Moves the entries.
	 * 
	 * @param bs
	 * @param request
	 * @param targetFolderId
	 * @param entityIds
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static ErrorListRpcResponseData moveEntries(AllModulesInjected bs, HttpServletRequest request, Long targetFolderId, List<EntityId> entityIds) throws GwtTeamingException {
		try {
			// Allocate an error list response we can return.
			ErrorListRpcResponseData reply = new ErrorListRpcResponseData(new ArrayList<ErrorInfo>());

			// Were we given the IDs of any entries to move?
			if (MiscUtil.hasItems(entityIds)) {
				// Yes!  Decide on the actual target for the move...
				TargetIds tis = new TargetIds(bs, targetFolderId);
				
				// ...and scan the entries to be moved.
				BinderModule bm = bs.getBinderModule();
				FolderModule fm = bs.getFolderModule();
				for (EntityId entityId:  entityIds) {
					try {
						// Can we move this entity?
						if (entityId.isBinder())
						     bm.moveBinder(                       entityId.getEntityId(), tis.getBinderTargetId(),      null);
						else fm.moveEntry(entityId.getBinderId(), entityId.getEntityId(), tis.getEntryTargetId(), null, null);
					}

					catch (Exception e) {
						// No!  Add an error  to the error list.
						String entryTitle = GwtServerHelper.getEntityTitle(bs, entityId);
						String messageKey;
						if      (e instanceof AccessControlException) messageKey = "moveEntryError.AccssControlException";
						else if (e instanceof TitleException)         messageKey = "moveEntryError.TitleException";
						else                                          messageKey = "moveEntryError.OtherException";
						reply.addError(NLT.get(messageKey, new String[]{entryTitle}));
					}
				}
			}

			// If we get here, reply refers to an
			// ErrorListRpcResponseData containing any errors we
			// encountered.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.moveEntries( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}

	/*
	 * Performs any post processing on the search entries being
	 * returned for a 'Shared by/with Me' collection view.
	 */
	@SuppressWarnings("unchecked")
	private static List<Map> postProcessSharedMeMap(AllModulesInjected bs, List<Map> searchEntries, List<GwtSharedMeItem> shareItems, boolean sortDescend, String sortBy) {
		// Do we have any search entries to process?
		if (MiscUtil.hasItems(searchEntries)) {
			// Yes!  Sort the list, based on the the information we
			// have for sorting it.
			Collections.sort(
				searchEntries,
				new SharedMeEntriesMapComparator(
					shareItems,
					sortBy,
					sortDescend));
		}

		// If we get here, searchEntries refers to the post processed
		// search entries Map.  Return it. 
		return searchEntries;
	}
	
	/*
	 * Given a list of column names, return those columns applicable to
	 * the collection type for the current license mode, ...
	 */
	private static String[] pruneColumnNames(CollectionType collection, String ... columnList) {
		// Scan the columns.
		List<String> columns = new ArrayList<String>();
		for (String column:  columnList) {
			// Add the column to the list we'll return.
			columns.add(column);
		}
		
		// Return a String[] of the columns that are applicable for the
		// current license, ... in the current collection type.
		return columns.toArray(new String[0]);
	}
	
	/*
	 * Given a list of column names, returns those column applicable to
	 * the folder type for the current license mode, ...
	 */
	private static String[] pruneColumnNames(FolderType folder, String ... columnList) {
		// If we're not in Filr mode...
		if (!(Utils.checkIfFilr())) {
			// ...we always use all the columns.
			return columnList;
		}

		// Scan the columns.
		List<String> columns = new ArrayList<String>();
		for (String column:  columnList) {
			// What type of folder are we working on?
			switch (folder) {
			case FILE:
				// File!  We show all but state.
				if (column.equals("state")) {
					continue;
				}
				break;
				
			case MIRROREDFILE:
				// MirroredFile!  We show all but state.
				if (column.equals("state")) {
					continue;
				}
				break;
				
			default:
				// For all others, we show all the columns.
				break;
			}
			
			// Add the column to the list we'll return.
			columns.add(column);
		}
		
		// Return a String[] of the columns that are applicable for the
		// current license, ... in the current folder type.
		return columns.toArray(new String[0]);
	}
	
	/**
	 * Purge users and their workspaces.
	 * 
	 * @param bs
	 * @param request
	 * @param userIds
	 * @param purgeMirrored
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	@SuppressWarnings("unchecked")
	public static ErrorListRpcResponseData purgeUsers(AllModulesInjected bs, HttpServletRequest request, List<Long> userIds, boolean purgeMirrored) throws GwtTeamingException {
		try {
			// Allocate an error list response we can return.
			ErrorListRpcResponseData reply = new ErrorListRpcResponseData(new ArrayList<ErrorInfo>());

			// Were we given the IDs of any users to purge?
			Long currentUserId = GwtServerHelper.getCurrentUserId(); 
			if (MiscUtil.hasItems(userIds)) {
				// Yes!  Scan them.
				boolean isOtherUserAccessRestricted = Utils.canUserOnlySeeCommonGroupMembers();
				for (Long userId:  userIds) {
					// Can we resolve the user being purge?
					User user = getResolvedUser(userId);
					if (null != user) {
						// Yes!  Is it the user that's logged in?
						if (user.getId().equals(currentUserId)) {
							// Yes!  They can't purge their own
							// user object or workspace.  Ignore it.
							reply.addError(NLT.get("purgeUserError.self"));
							continue;
						}

						// Is it a reserved user?
						if (user.isReserved()) {
							// Yes!  They can't do that.  Ignore it.
							String userTitle = GwtServerHelper.getUserTitle(bs.getProfileModule(), isOtherUserAccessRestricted, String.valueOf(userId), ((null == user) ? "" : user.getTitle()));
							reply.addError(NLT.get("purgeUserError.reserved", new String[]{userTitle}));
							continue;
						}
					}
					
					try {
						// Does this user have a workspace ID?
						Long wsId = user.getWorkspaceId();
						if (null != wsId) {
							// Yes!  Purge the workspace...
							bs.getBinderModule().deleteBinder(wsId, purgeMirrored, null);
						}
						
						// ...and purge the user.
						Map options = new HashMap();
						options.put(ObjectKeys.INPUT_OPTION_DELETE_USER_WORKSPACE, false);
						bs.getProfileModule().deleteEntry(userId, options);
					}

					catch (Exception e) {
						// No!  Add an error  to the error list.
						String userTitle = GwtServerHelper.getUserTitle(bs.getProfileModule(), isOtherUserAccessRestricted, String.valueOf(userId), ((null == user) ? "" : user.getTitle()));
						String messageKey;
						if      (e instanceof AccessControlException) messageKey = "purgeUserError.AccssControlException";
						else                                          messageKey = "purgeUserError.OtherException";
						reply.addError(NLT.get(messageKey, new String[]{userTitle}));
					}
				}
			}

			// If we get here, reply refers to an
			// ErrorListRpcResponseData containing any errors we
			// encountered.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.purgeUsers( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}
	
	/**
	 * Purge user workspaces.
	 * 
	 * @param bs
	 * @param request
	 * @param userIds
	 * @param purgeMirrored
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static ErrorListRpcResponseData purgeUserWorkspaces(AllModulesInjected bs, HttpServletRequest request, List<Long> userIds, boolean purgeMirrored) throws GwtTeamingException {
		try {
			// Allocate an error list response we can return.
			ErrorListRpcResponseData reply = new ErrorListRpcResponseData(new ArrayList<ErrorInfo>());

			// Were we given the IDs of any users to purge?
			Long currentUserId = GwtServerHelper.getCurrentUserId(); 
			if (MiscUtil.hasItems(userIds)) {
				// Yes!  Scan them.
				boolean isOtherUserAccessRestricted = Utils.canUserOnlySeeCommonGroupMembers();
				for (Long userId:  userIds) {
					// Can we resolve the user being purge?
					User user = getResolvedUser(userId);
					if (null != user) {
						// Yes!  Is it the user that's logged in?
						if (user.getId().equals(currentUserId)) {
							// Yes!  They can't purge their own
							// workspace.  Ignore it.
							reply.addError(NLT.get("purgeUserWorkspaceError.self"));
							continue;
						}

						// Is it a reserved user?
						if (user.isReserved()) {
							// Yes!  They can't do that.  Ignore it.
							String userTitle = GwtServerHelper.getUserTitle(bs.getProfileModule(), isOtherUserAccessRestricted, String.valueOf(userId), ((null == user) ? "" : user.getTitle()));
							reply.addError(NLT.get("purgeUserWorkspaceError.reserved", new String[]{userTitle}));
							continue;
						}
					}
					
					try {
						// Does this user have a workspace ID?
						Long wsId = user.getWorkspaceId();
						if (null != wsId) {
							// Yes!  Purge the workspace.
							bs.getBinderModule().deleteBinder(wsId, purgeMirrored, null);
						}
					}

					catch (Exception e) {
						// No!  Add an error  to the error list.
						String userTitle = GwtServerHelper.getUserTitle(bs.getProfileModule(), isOtherUserAccessRestricted, String.valueOf(userId), ((null == user) ? "" : user.getTitle()));
						String messageKey;
						if      (e instanceof AccessControlException) messageKey = "purgeUserWorkspaceError.AccssControlException";
						else                                          messageKey = "purgeUserWorkspaceError.OtherException";
						reply.addError(NLT.get(messageKey, new String[]{userTitle}));
					}
				}
			}

			// If we get here, reply refers to an
			// ErrorListRpcResponseData containing any errors we
			// encountered.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.purgeUserWorkspaces( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}

	/**
	 * Renames an entity.
	 * 
	 * @param bs
	 * @param request
	 * @param eid
	 * @param entityName
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	@SuppressWarnings("unchecked")
	public static StringRpcResponseData renameEntity(AllModulesInjected bs, HttpServletRequest request, EntityId eid, String entityName) throws GwtTeamingException {
		try {
			// Allocate a string response we can return any error in.
			StringRpcResponseData reply = new StringRpcResponseData();

			// Are we renaming a binder?
			if (eid.isBinder()) {
				// Yes!  Can we perform the rename?
				HashMap rdMap = new HashMap();
				rdMap.put(ObjectKeys.FIELD_ENTITY_TITLE, entityName);
				MapInputData mid = new MapInputData(rdMap);
				try {
					bs.getBinderModule().modifyBinder(
						eid.getEntityId(),
						mid,			// Input data.
						new HashMap(),	// No file items.
						new HashSet(),	// No delete attachments.
						null);			// No options.
				}
				
				catch (Exception e) {
					// No!  Return the reason why in the string response.
					String messageKey;
					if      (e instanceof AccessControlException)          messageKey = "renameEntityError.AccssControlException.";
					else if (e instanceof IllegalCharacterInNameException) messageKey = "renameEntityError.IllegalCharacterInNameException.";
					else if (e instanceof WriteFilesException)             messageKey = "renameEntityError.WriteFilesException.";
					else if (e instanceof TitleException)		           messageKey = "renameEntityError.TitleException.";
					else                                                   messageKey = "renameEntityError.OtherException.";
					if (eid.isFolder())
					     messageKey += "folder";
					else messageKey += "workspace";
					reply.setStringValue(NLT.get(messageKey, new String[]{entityName}));
				}
			}
			
			else {
				// No, we aren't renaming a binder!
				//
				// Note:  The logic I use here to rename the file
				//        and/or entry was copied from
				//        org.kablink.teaming.webdav.FileResource.renameFile()
				// 
				// Access the entry and attachment we're renaming.
				FolderModule   fm = bs.getFolderModule();
				FolderEntry    fe = fm.getEntry(eid.getBinderId(), eid.getEntityId());
				FileAttachment fa = GwtServerHelper.getFileEntrysFileAttachment(bs, fe, true);
				
				// If the current entry title is identical to the name
				// of the file, it's reasonable to change the title to
				// match the new name as well.  Do we have to rename
				// the entry?
				InputDataAccessor inputData;				
				if ((null == fa) || fa.getFileItem().getName().equals(fe.getTitle())) {
					// Yes!  Setup the appropriate input data.
					Map data = new HashMap();
					data.put(ObjectKeys.FIELD_ENTITY_TITLE, entityName);
					inputData = new MapInputData(data);
				}
				else {
					// No, we don't have to rename the entry!
					inputData = new EmptyInputData();
				}

				// Setup the appropriate  file rename.
				Map<FileAttachment,String> renamesTo;
				if (null == fa) {
					renamesTo = null;
				}
				else {
					renamesTo = new HashMap<FileAttachment,String>();
					renamesTo.put(fa, entityName);
				}

				// Has the user already seen this entry?
				SeenMap seenMap = bs.getProfileModule().getUserSeenMap(null);
				boolean feSeen  = seenMap.checkIfSeen(fe);
				
				// Can we perform the rename of the entry and/or file?
				try {
					// If the name is bogus...
					if (Validator.containsPathCharacters(entityName)) {
						// ...throw an appropriate exception.
						throw new IllegalCharacterInNameException("errorcode.title.pathCharacters", new Object[]{entityName});
					}

					fm.modifyEntry(
						fe.getParentBinder().getId(),	//
						fe.getId(),						//
						inputData,						//
						null,							// null -> No file items.
						null,							// null -> No delete attachments.
						renamesTo,						//
						null);							// null -> No options.
					
					// If the user saw the entry before we modified it...
					if (feSeen) {
						// ...retain that seen state.
						bs.getProfileModule().setSeen(null, fe);
					}
				}
				
				catch (Exception e) {
					// No!  Return the reason why in the string response.
					String messageKey;
					if      (e instanceof AccessControlException)          messageKey = "renameEntityError.AccssControlException.file";
					else if (e instanceof IllegalCharacterInNameException) messageKey = "renameEntityError.IllegalCharacterInNameException.file";
					else if (e instanceof ReservedByAnotherUserException)  messageKey = "renameEntityError.ReservedByAnotherUserException.file";
					else if (e instanceof WriteFilesException)             messageKey = "renameEntityError.WriteFilesException.file";
					else if (e instanceof WriteEntryDataException)         messageKey = "renameEntityError.WriteEntryDataException.file";
					else                                                   messageKey = "renameEntityError.OtherException.file";
					reply.setStringValue(NLT.get(messageKey, new String[]{entityName}));
				}
			}

			// If we get here, reply refers to a
			// StringRpcResponseData containing any error we
			// encountered.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.renameEntity( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}
	
	/**
	 * Unlocks the entries.
	 * 
	 * @param bs
	 * @param request
	 * @param entityIds
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static ErrorListRpcResponseData unlockEntries(AllModulesInjected bs, HttpServletRequest request, List<EntityId> entityIds) throws GwtTeamingException {
		try {
			// Allocate an error list response we can return.
			ErrorListRpcResponseData reply = new ErrorListRpcResponseData(new ArrayList<ErrorInfo>());

			// Were we given the IDs of any entries to unlock?
			if (MiscUtil.hasItems(entityIds)) {
				// Yes!  Scan them.
				for (EntityId entityId:  entityIds) {
					// If this entity is a binder...
					if (entityId.isBinder()) {
						// ...skip it.
						continue;
					}
					
					try {
						// Can we unlock this entry?
						bs.getFolderModule().unreserveEntry(entityId.getBinderId(), entityId.getEntityId());
					}

					catch (Exception e) {
						// No!  Add an error  to the error list.
						String entryTitle = GwtServerHelper.getEntityTitle(bs, entityId);
						String messageKey;
						if      (e instanceof AccessControlException)         messageKey = "unlockEntryError.AccssControlException";
						else if (e instanceof ReservedByAnotherUserException) messageKey = "unlockEntryError.ReservedByAnotherUserException";
						else                                                  messageKey = "unlockEntryError.OtherException";
						reply.addError(NLT.get(messageKey, new String[]{entryTitle}));
					}
				}
			}

			// If we get here, reply refers to an
			// ErrorListRpcResponseData containing any errors we
			// encountered.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.unlockEntries( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}
	
	/**
	 * Saves whether the accessory panel should be visible on the
	 * given binder.
	 * 
	 * @param bs
	 * @param request
	 * @param binderId
	 * @param showAccessoryPanel
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static Boolean saveAccessoryStatus(AllModulesInjected bs, HttpServletRequest request, Long binderId, boolean showAccessoryPanel) throws GwtTeamingException {
		try {
			// Save the accessory status...
			bs.getProfileModule().setUserProperty(
				GwtServerHelper.getCurrentUserId(),
				binderId,
				ObjectKeys.USER_PROPERTY_BINDER_SHOW_ACCESSORIES,
				new Boolean(showAccessoryPanel));
			
			// ...and return true.
			return Boolean.TRUE;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.saveAccessoryStatus( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}
	
	/**
	 * Stores a region state for the current user on a binder.
	 * 
	 * @param bs
	 * @param request
	 * @param binderId
	 * @param regionId
	 * @param regionState
	 * 
	 * @return
	 */
	public static BooleanRpcResponseData saveBinderRegionState(AllModulesInjected bs, HttpServletRequest request, Long binderId, String regionId, String regionState) throws GwtTeamingException {
		try {
			// Store the new region state and return true.
			bs.getProfileModule().setUserProperty(GwtServerHelper.getCurrentUserId(), binderId, ObjectKeys.USER_PROPERTY_REGION_VIEW + "." + regionId, regionState);
			return new BooleanRpcResponseData(Boolean.TRUE);
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.saveBinderRegionState( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}

	/**
	 * Stores the column widths for a user on a folder.
	 * 
	 * @param bs
	 * @param request
	 * @param folderId
	 * @param columnWidths
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static BooleanRpcResponseData saveColumnWidths(AllModulesInjected bs, HttpServletRequest request, BinderInfo folderInfo, Map<String, String> columnWidths) throws GwtTeamingException {
		try {
			// Store the column widths...
			String propKey = ObjectKeys.USER_PROPERTY_COLUMN_WIDTHS;
			if (folderInfo.isBinderTrash()) {
				propKey += ".Trash";
			}
			bs.getProfileModule().setUserProperty(
				GwtServerHelper.getCurrentUserId(),
				folderInfo.getBinderIdAsLong(),
				propKey,
				columnWidths);
			
			// ...and return true.
			return new BooleanRpcResponseData(Boolean.TRUE);
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.saveColumnWidths( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}

	/**
	 * Saves the position of the View Details dialog on a FolderEntry
	 * to the user's properties.
	 * 
	 * @param bs
	 * @param request
	 * @param x
	 * @param y
	 * @param cx
	 * @param cy
	 * 
	 * @throws GwtTeamingException
	 */
	public static void saveFolderEntryDlgPosition(AllModulesInjected bs, HttpServletRequest request, int x, int y, int cx, int cy) throws GwtTeamingException {
		try {
			// Is this the guest user?
			User	user    = GwtServerHelper.getCurrentUser();
			boolean	isGuest = ObjectKeys.GUEST_USER_INTERNALID.equals(user.getInternalId());
			if (isGuest) {
				// Yes!  Then we don't save the dialog's position.
				return;
			}

			// Pack the dialog position values into a string...
			String packedPosition = StringUtil.pack(new String[]{
				String.valueOf(x),	
				String.valueOf(y),	
				String.valueOf(cx),	
				String.valueOf(cy),	
			});

			// ...and store them in the user's properties.
			bs.getProfileModule().setUserProperty(
				user.getId(),
				ObjectKeys.USER_PROPERTY_FOLDER_ENTRY_DLG_POSITION,
				packedPosition);
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.saveFolderEntryDlgPosition( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}
	
	/*
	 * Generates a value for a custom column in a row.
	 * 
	 * The algorithm used in this method was reverse engineered from
	 * that used by folder_view_common2.jsp or view_trash.jsp.
	 */
	@SuppressWarnings("unchecked")
	private static void setValueForCustomColumn(AllModulesInjected bs, Map entryMap, FolderRow fr, FolderColumn fc) throws ParseException {
		try {
			// If we don't have a column element name...
			String colEleName = fc.getColumnEleName();
			if (!MiscUtil.hasString(colEleName)) {
				// ...just render it as an empty string.
				fr.setColumnValue(fc, "");
				return;
			}

			// Do we have a value or event for this column?
			Object colValue = entryMap.get(colEleName);
			String colType = fc.getColumnType();
			if (null == colType) colType = "";
			if ((null != colValue) || colType.equals("event")) {
				// Yes!  Handles those that are a simple list of values.
				Definition entryDef = bs.getDefinitionModule().getDefinition(fc.getColumnDefId());
				if (colType.equals("selectbox")           ||
					    colType.equals("radio")           ||
					    colType.equals("checkbox")        ||
					    colType.equals("text")            ||
					    colType.equals("entryAttributes") ||
					    colType.equals("hidden")          ||
					    colType.equals("number")) {
					String strValue = DefinitionHelper.getCaptionsFromValues(entryDef, colEleName, colValue.toString());
					fr.setColumnValue(fc, strValue);
				}

				// Handle those that are a URL.
				else if (colType.equals("url")) {
					// Can we find the definition of the column?
		         	Element colElement = ((Element) DefinitionHelper.findAttribute(colEleName, entryDef.getDefinition()));
		         	if (null == colElement) {
		         		// No!  Just render the column as an empty string.
						fr.setColumnValue(fc, "");
		         	}
		         	
		         	else {
						// Yes, we found the definition of the column!
		         		// Extract what we need to render it.
			         	String linkText = DefinitionHelper.getItemProperty(colElement, "linkText");
			         	String target   = DefinitionHelper.getItemProperty(colElement, "target"  );
			         	
			         	String  strValue   = DefinitionHelper.getCaptionsFromValues(entryDef, colEleName, colValue.toString());
			         	if (!(MiscUtil.hasString(linkText))) linkText = strValue;
			         	if ((null == target) || target.equals("false")) target = "";
			         	if ((null != target) && target.equals("true" )) target = "_blank";
			         	
			         	// Construct an EntryLinkInfo for the data.
			         	EntryLinkInfo linkValue = new EntryLinkInfo(strValue, target, linkText);
						fr.setColumnValue(fc, linkValue);
		         	}
				}

				// Handle date stamps.
				else if (colType.equals("date")) {
					if (null != colValue) {
						String tdStamp = ((String) colValue);
					    String year    = tdStamp.substring(0, 4);
						String month   = tdStamp.substring(4, 6);
						String day     = tdStamp.substring(6, 8);
						String strValue;
						if (8 < tdStamp.length()) {
							String time = tdStamp.substring(8);
							SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd:HHmm");
							Date date = formatter.parse(year + "-" + month + "-" + day + ":" + time);
							strValue = GwtServerHelper.getDateString(date, DateFormat.SHORT);
						}
						else {
							SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
							Date date = formatter.parse(year + "-" + month + "-" + day);
							strValue = GwtServerHelper.getDateString(date, DateFormat.SHORT);
						}
						fr.setColumnValue(fc, strValue);
					}
				}

				// Handle time/date stamps.
				else if (colType.equals("date_time")) {
					if (null != colValue) {
						String tdStamp = ((String) colValue);
					    String year    = tdStamp.substring(0, 4);
						String month   = tdStamp.substring(4, 6);
						String day     = tdStamp.substring(6, 8);
						String strValue;
						if (8 < tdStamp.length()) {
							String time = tdStamp.substring(8, 12);
							SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd:HHmm");
							Date date = formatter.parse(year + "-" + month + "-" + day + ":" + time);
							strValue = GwtServerHelper.getDateTimeString(date, DateFormat.SHORT, DateFormat.SHORT);
						}
						else {
							SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
							Date date = formatter.parse(year + "-" + month + "-" + day);
							strValue = GwtServerHelper.getDateString(date, DateFormat.SHORT);
						}
						fr.setColumnValue(fc, strValue);
					}
				}

				// Handle events.
				else if (colType.equals("event")) {
					String tzId = ((String) entryMap.get(colEleName + "#TimeZoneID"));
					boolean showTimes = false;
					Date logicalEnd   = ((Date) entryMap.get(colEleName + "#LogicalEndDate"));
					Date logicalStart = ((Date) entryMap.get(colEleName + "#LogicalStartDate"));
					if ((null != logicalStart) && (null != logicalEnd)) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
						if (sdf.format(logicalStart).equals(sdf.format(logicalEnd))) {
							// The two dates are the same, so show the
							// time field.
							showTimes = true;
						}
					}
					
					String logicalEndS   = null;
					String logicalEtartS = null;
					boolean allDayEvent  = (null == tzId);
					if (allDayEvent) {
						//All day event.
						if (null != logicalStart) logicalEtartS = GwtServerHelper.getDateString(logicalStart, DateFormat.SHORT);
						if (null != logicalEnd)   logicalEndS   = GwtServerHelper.getDateString(logicalEnd,   DateFormat.SHORT);
					}
					
					else {
						// Regular event.
						if (null != logicalStart) {
							if (showTimes)
							     logicalEtartS = GwtServerHelper.getDateTimeString(logicalStart, DateFormat.SHORT, DateFormat.SHORT);
							else logicalEtartS = GwtServerHelper.getDateString(    logicalStart, DateFormat.SHORT                  );
							
						}
						if (null != logicalEnd) {
							if (showTimes)
							     logicalEndS = GwtServerHelper.getDateTimeString(logicalEnd, DateFormat.SHORT, DateFormat.SHORT);
							else logicalEndS = GwtServerHelper.getDateString(    logicalEnd, DateFormat.SHORT                  );
						}
					}
					
					EntryEventInfo eventValue = new EntryEventInfo(allDayEvent, logicalEtartS, logicalEndS);
					Date actualEnd   = ((Date) entryMap.get(colEleName + "#EndDate"));
					int  durDays     = getDurDaysFromEntryMap(colEleName, entryMap);
					boolean daysOnly = ((null == actualEnd) && (0 < durDays));
					if (daysOnly) {
						// Duration only.
						eventValue.setDurationDaysOnly(true   );
						eventValue.setDurationDays(    durDays);
					}
		         	
					// Construct an EntryEventInfo for the data.
					fr.setColumnValue(fc, eventValue);
				}

				// Handle lists of users.
				else if (colType.equals("user_list") || colType.equals("userListSelectbox")) {
					String       idList     = colValue.toString();
					Set          ids        = LongIdUtil.getIdsAsLongSet(idList, ","  );
					List         principals = ResolveIds.getPrincipals(  ids,    false);
					StringBuffer strValue   = new StringBuffer("");
					boolean      firstP     = true;
					for (Object pO:  principals) {
						Principal p = ((Principal) pO);
						if (!firstP) {
							strValue.append(", ");
						}
						strValue.append(p.getTitle());
						firstP = false;
					}
					fr.setColumnValue(fc, strValue.toString());
				}
			}
		}
		
		catch (Exception ex) {
			// Log the exception...
			m_logger.debug("GwtViewHelper.setValueForCustomColumn( EXCEPTION ):  ", ex);
			m_logger.debug("...Element:  " + fc.getColumnEleName());
			m_logger.debug("...Column:  "  + fc.getColumnName());
			m_logger.debug("...Row:  "     + fr.getEntityId().getEntityId());

			// ...and store something for the column to display.
			fr.setColumnValue(
				fc,
				(GwtUIHelper.isVibeUiDebug()         ?
					("Exception:  " + ex.toString()) :
					""));
		}
	}

	/**
	 * Stores the values from a SharedViewState object in the session
	 * cache.
	 * 
	 * @param bs
	 * @param request
	 * @param ct
	 * @param svs
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static Boolean saveSharedViewState(AllModulesInjected bs, HttpServletRequest request, CollectionType ct, SharedViewState svs) throws GwtTeamingException {
		try {
			// Store/remove the values from the cache and return true.
			HttpSession hSession = GwtServerHelper.getCurrentHttpSession();
			if (svs.isShowHidden())    hSession.setAttribute(   CACHED_SHARED_VIEW_SHOW_HIDDEN_BASE     + ct.name(), Boolean.TRUE); else hSession.removeAttribute(CACHED_SHARED_VIEW_SHOW_HIDDEN_BASE     + ct.name()               );
			if (svs.isShowNonHidden()) hSession.removeAttribute(CACHED_SHARED_VIEW_SHOW_NON_HIDDEN_BASE + ct.name()              ); else hSession.setAttribute(   CACHED_SHARED_VIEW_SHOW_NON_HIDDEN_BASE + ct.name(), Boolean.FALSE);
			return Boolean.TRUE;
		}
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.saveSharedViewState( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}		
	}

	/**
	 * Stores whether the user is viewing pinned entries on a given
	 * folder.
	 * 
	 * @param request
	 * @param folderId
	 * @param viewingPinnedEntries
	 */
	public static void saveUserViewPinnedEntries(HttpServletRequest request, Long folderId, boolean viewingPinnedEntries) {
		HttpSession session = WebHelper.getRequiredSession(request);
		session.setAttribute((CACHED_VIEW_PINNED_ENTRIES_BASE + folderId), new Boolean(viewingPinnedEntries));
	}
	
	/**
	 * Stores whether the user is viewing shared files on a given
	 * collection.
	 * 
	 * @param request
	 * @param collectionType
	 * @param viewingSharedFiles
	 */
	public static void saveUserViewSharedFiles(HttpServletRequest request, CollectionType collectionType, boolean viewingSharedFiles) {
		HttpSession session = WebHelper.getRequiredSession(request);
		session.setAttribute((CACHED_VIEW_SHARED_FILES_BASE + collectionType.ordinal()), new Boolean(viewingSharedFiles));
	}
	
	/**
	 * Marks the selected shared entities as no longer being hidden.
	 * 
	 * @param bs
	 * @param request
	 * @param ct
	 * @param entityIds
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static Boolean showShares(AllModulesInjected bs, HttpServletRequest request, CollectionType ct, List<EntityId> entityIds) throws GwtTeamingException {
		try {
			// Are there any entities to show?
			if (MiscUtil.hasItems(entityIds)) {
				// Yes!  Scan them.
                SharingModule sm = bs.getSharingModule();
				boolean isSharedWithMe = ct.equals(CollectionType.SHARED_WITH_ME);
                List<EntityIdentifier> ids = new ArrayList<EntityIdentifier>();
                for (EntityId eid:  entityIds) {
                    ids.add(toEntityIdentifier(eid));
                }
                sm.unhideSharedEntitiesForCurrentUser(ids, isSharedWithMe);
			}
			
			// If we get here, the show was successful.  Return true.
			return Boolean.TRUE;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.showShares( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}
	
	/**
	 * Called to purge all the entries in the trash.
	 * 
	 * @param bs
	 * @param reqeust
	 * @param binderId
	 * @param purgeMirroredSources
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static StringRpcResponseData trashPurgeAll(AllModulesInjected bs, HttpServletRequest reqeust, Long binderId, boolean purgeMirroredSources) throws GwtTeamingException {
		try {
			// Allocate an error list response we can return.
			StringRpcResponseData reply = new StringRpcResponseData();

			// Purge the entries in the trash...
			TrashEntry[] trashEntries = TrashHelper.getAllTrashEntries(bs, binderId);
			TrashResponse tr = TrashHelper.purgeSelectedEntries(bs, trashEntries, purgeMirroredSources);
			if (tr.isError() || tr.m_rd.hasRenames()) {
				// ...and return any messages we get in response.
				reply.setStringValue(tr.getTrashMessage(bs));
			}

			// If we get here, reply refers to a StringRpcResponseData
			// containing any errors we encountered.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.trashPurgeAll( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}
	
	/**
	 * Called to purge the selected entries in the trash.
	 * 
	 * @param bs
	 * @param reqeust
	 * @param binderId
	 * @param purgeMirroredSources
	 * @param trashSelectionData
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static StringRpcResponseData trashPurgeSelectedEntries(AllModulesInjected bs, HttpServletRequest reqeust, Long binderId, boolean purgeMirroredSources, List<String> trashSelectionData) throws GwtTeamingException {
		try {
			// Allocate an error list response we can return.
			StringRpcResponseData reply = new StringRpcResponseData();

			// Do we have any selections to purge?
			int count = ((null == trashSelectionData) ? 0 : trashSelectionData.size());
			if (0 < count) {
				// Yes!  Convert them to a TrashEntry[]...
				TrashEntry[] trashEntries = new TrashEntry[count];
				for (int i = 0; i < count; i += 1) {
					trashEntries[i] = new TrashEntry(trashSelectionData.get(i));
				}
				
				// ...purge those...
				TrashResponse tr = TrashHelper.purgeSelectedEntries(bs, trashEntries, purgeMirroredSources);
				if (tr.isError() || tr.m_rd.hasRenames()) {
					// ...and return any messages we get in response.
					reply.setStringValue(tr.getTrashMessage(bs));
				}
			}
			
			// If we get here, reply refers to a StringRpcResponseData
			// containing any errors we encountered.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.trashPurgeSelectedEntries( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}
	
	/**
	 * Called to restore all the entries in the trash.
	 * 
	 * @param bs
	 * @param reqeust
	 * @param binderId
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static StringRpcResponseData trashRestoreAll(AllModulesInjected bs, HttpServletRequest reqeust, Long binderId) throws GwtTeamingException {
		try {
			// Allocate an error list response we can return.
			StringRpcResponseData reply = new StringRpcResponseData();

			// Restore the entries in the trash...
			TrashEntry[] trashEntries = TrashHelper.getAllTrashEntries(bs, binderId);
			TrashResponse tr = TrashHelper.restoreSelectedEntries(bs, trashEntries);
			if (tr.isError() || tr.m_rd.hasRenames()) {
				// ...and return any messages we get in response.
				reply.setStringValue(tr.getTrashMessage(bs));
			}

			// If we get here, reply refers to a StringRpcResponseData
			// containing any errors we encountered.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.trashRestoreAll( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}
	
	/**
	 * Called to restore the selected entries in the trash.
	 * 
	 * @param bs
	 * @param reqeust
	 * @param binderId
	 * @param trashSelectionData
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static StringRpcResponseData trashRestoreSelectedEntries(AllModulesInjected bs, HttpServletRequest reqeust, Long binderId, List<String> trashSelectionData) throws GwtTeamingException {
		try {
			// Allocate an error list response we can return.
			StringRpcResponseData reply = new StringRpcResponseData();

			// Do we have any selections to restore?
			int count = ((null == trashSelectionData) ? 0 : trashSelectionData.size());
			if (0 < count) {
				// Yes!  Convert them to a TrashEntry[]...
				TrashEntry[] trashEntries = new TrashEntry[count];
				for (int i = 0; i < count; i += 1) {
					trashEntries[i] = new TrashEntry(trashSelectionData.get(i));
				}
				
				// ...restore those...
				TrashResponse tr = TrashHelper.restoreSelectedEntries(bs, trashEntries);
				if (tr.isError() || tr.m_rd.hasRenames()) {
					// ...and return any messages we get in response.
					reply.setStringValue(tr.getTrashMessage(bs));
				}
			}
			
			// If we get here, reply refers to a StringRpcResponseData
			// containing any errors we encountered.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.trashRestoreSelectedEntries( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}
	
	/*
	 * Strips the leading 0's and any trying decimal information off a
	 * String value and returns it.
	 */
	private static String trimFileSize(String value) {
		if (null != value) {
	        while (value.startsWith("0")) {
	        	value = value.substring(1, value.length());
	        }
	        value = trimFollowing(value, ".");	// Cleans any decimal...
	        value = trimFollowing(value, ",");	// information.
	        if (value.equals("")) {
	        	value = "0";
	        }
		}
		return value;
	}

	/*
	 * Strips everything in a string followingThis.
	 */
	private static String trimFollowing(String value, String followingThis) {
		if (null != value) {
	        int dPos = value.indexOf(followingThis);
	        while ((-1) != dPos) {
	        	value = value.substring(0, dPos);
		        dPos = value.indexOf(followingThis);
	        }
		}
		return value;
	}

	/**
	 * Uploads a file blob.
	 * 
	 * If the blob is the last one for the file, the file entry is
	 * created.  Otherwise, the blob is cached while we await
	 * additional blobs for it.
	 *
	 * @param bs
	 * @param request
	 * @param folderInfo
	 * @param fileBlob
	 * @param lastBlob
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static StringRpcResponseData uploadFileBlob(AllModulesInjected bs, HttpServletRequest request, BinderInfo folderInfo, FileBlob fileBlob, boolean lastBlob) throws GwtTeamingException {
		try {
			// Trace what we read to the log.
			debugTraceBlob(fileBlob, "uploadFileBlob", "Uploaded", lastBlob);

			// Is this the first blob of a file?
			HttpSession session = WebHelper.getRequiredSession(request);
			boolean firstBlob = (0l == fileBlob.getBlobStart());
			File tempFile;
			String uploadFName = (CACHED_UPLOAD_FILE + "." + String.valueOf(GwtServerHelper.getCurrentUserId()) + "." + String.valueOf(fileBlob.getUploadId()) + ".");
			if (firstBlob) {
				// Yes!  Create a new temporary file for it and store
				// the file handle in the session cache.  The format of
				// the prefix used is:  'uploadFile.<userId>.<timestamp>.'
				tempFile = TempFileUtil.createTempFile(uploadFName);
				if (!lastBlob) {
					session.setAttribute(uploadFName, tempFile.getName());
				}
			}
			
			else {
				// No, this isn't the first blob of a file!  Access the
				// temporary file from the handle stored in the session
				// cache.
				tempFile = TempFileUtil.getTempFileByName((String) session.getAttribute(uploadFName));
				if (lastBlob) {
					session.removeAttribute(uploadFName);
				}
			}

			// Does the MD5 hash calculated on the blob we just
			// received match the MD5 hash that came with it? 
			StringRpcResponseData	reply   = null;
			String					md5Hash = MiscUtil.getMD5Hash(fileBlob.getBlobData());
			if (!(md5Hash.equals(fileBlob.getBlobMD5Hash()))) {
				// No!  Then the data is corrupt.  Return the error to
				// the user.
				reply = new StringRpcResponseData();
				reply.setStringValue(NLT.get("binder.add.files.html5.upload.corrupt"));
				try {tempFile.delete();}
				catch (Throwable t) {/* Ignored. */}
			}
			
			else {
				try {
					// Yes!  The MD5 hashes match!  Can we write the
					// data from this blob to the file?
					FileOutputStream fo = new FileOutputStream(tempFile, (!firstBlob));
					byte[] blobData = fileBlob.getBlobData().getBytes();
					if (fileBlob.isBlobBase64Encoded()) {
						blobData = Base64.decodeBase64(blobData);
					}
					fo.write(blobData);
					fo.close();
				}
				
				catch (Exception e) {
					// No!  Return the error to the user.
					reply = new StringRpcResponseData();
					reply.setStringValue(NLT.get("binder.add.files.html5.upload.error", new String[]{e.getLocalizedMessage()}));
					try {tempFile.delete();}
					catch (Throwable t) {/* Ignored. */}
				}
			}

			// Did we just successfully write the last blob of the file
			// to the temporary file we use to cache it while we stream
			// it to the server?
			if ((null == reply) && lastBlob) {
				// Yes!  We need to create the entry for the file in
				// the target folder.
				FolderModule	fm     = bs.getFolderModule();
				ProfileModule	pm     = bs.getProfileModule();
    	    	Folder			folder = fm.getFolder(folderInfo.getBinderIdAsLong());
    	    	FileInputStream fi     = new FileInputStream(tempFile);
    	    	try {
        	    	// If there's an existing entry...
    				String fileName = fileBlob.getFileName();
        	    	FolderEntry existingEntry = fm.getLibraryFolderEntryByFileName(folder, fileName);
    	    		if (null != existingEntry) {
    	    			// ...we modify it...
        	    		FolderUtils.modifyLibraryEntry(existingEntry, fileName, fi, null, null, null, true);
        				pm.setSeen(null, existingEntry);
        	    	}
    	    		
    	    		else {
    	    			// ...otherwise, we create a new one.
        	    		FolderEntry fe = FolderUtils.createLibraryEntry(folder, fileName, fi, null, null, true);
        				pm.setSeen(null, fe);
        	    	}
    	    	}
    	    	
    	    	catch (Exception ex) {
    	    		String errMsg;
	    	    	if      (ex instanceof AccessControlException) errMsg = NLT.get("entry.duplicateFileInLibrary2"           );
	    	    	else if (ex instanceof BinderQuotaException)   errMsg = NLT.get("entry.uploadError.binderQuotaException"  );
	    	    	else if (ex instanceof DataQuotaException)     errMsg = NLT.get("entry.uploadError.dataQuotaException"    );
	    	    	else if (ex instanceof FileSizeLimitException) errMsg = NLT.get("entry.uploadError.fileSizeLimitException");
					else if (ex instanceof WriteFilesException)    errMsg = NLT.get("entry.uploadError.writeFilesException", new String[]{ex.getLocalizedMessage()});
	    	    	else                                           errMsg = NLT.get("entry.uploadError.unknownError",        new String[]{ex.getLocalizedMessage()});
    				reply = new StringRpcResponseData();
    				reply.setStringValue(errMsg);
    	    	}
    	    	
    	    	finally {
    	    		// If we have an input stream open...
    	    		if (null != fi) {
    	    			// ...close it...
    	    			fi.close();
    	    			fi = null;
    	    		}
    	    		
    	    		// ...and delete the temporary file.
    	    		try {tempFile.delete();}
    	    		catch (Throwable t) {/* Ignore. */}
    	    	}
			}

			// Return an empty string response or the one we
			// constructed containing any error we encountered during
			// the upload.
			return ((null == reply) ? new StringRpcResponseData() : reply);
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.uploadFileBlob( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}

	/**
	 * Validates that the user can upload the files/folders in
	 * List<UploadInfo> of things pending upload.
	 * 
	 * @param bs
	 * @param request
	 * @param folderInfo
	 * @param uploads
	 * 
	 * @return
	 * 
	 * @throws GwtTeamingException
	 */
	public static ValidateUploadsRpcResponseData validateUploads(AllModulesInjected bs, HttpServletRequest request, BinderInfo folderInfo, List<UploadInfo> uploads) throws GwtTeamingException {
		try {
			// Allocate validation response we can return.
			ValidateUploadsRpcResponseData reply = new ValidateUploadsRpcResponseData(new ArrayList<ErrorInfo>());

			// We're we given anything to validate?
			if (MiscUtil.hasItems(uploads)) {
				// Yes!  Access the objects we need to perform the
				// analysis.
				AdminModule		am                = bs.getAdminModule();
				BinderModule	bm                = bs.getBinderModule();
				FolderModule	fm                = bs.getFolderModule();
				Folder			folder            = fm.getFolder(folderInfo.getBinderIdAsLong());
				Long			userFileSizeLimit = am.getUserFileSizeLimit();
				
				// What do we need to check?
				boolean	checkBinderQuotas      = bm.isBinderDiskQuotaEnabled();
				boolean	checkUserQuotas        = am.isQuotaEnabled();
				boolean	checkUserFileSizeLimit = ((null != userFileSizeLimit) && (0 < userFileSizeLimit));
				if (checkUserFileSizeLimit) {
					userFileSizeLimit = (userFileSizeLimit * MEGABYTES);
				}
				
				// Do we need to worry about quotas?
				if (checkBinderQuotas || checkUserQuotas || checkUserFileSizeLimit) {
					// Yes!  Scan the UploadInfo's.
					long totalSize = 0l;
					for (UploadInfo upload:  uploads) {
						// Is this upload a file?
						if (upload.isFile()) {
							// Yes!  Does its size exceed the user's
							// file size limit?
							long size = upload.getSize();
							if (checkUserFileSizeLimit && (size > userFileSizeLimit)) {
								// Yes!  Add an appropriate error the
								// reply.
								reply.addError(NLT.get("validateUploadError.quotaExceeded.file", new String[]{upload.getName(), String.valueOf(userFileSizeLimit)}));
							}
							
							// Add this file's size to the running
							// total.
							totalSize += size;
						}
					}

					// Will the total size of all the files being
					// uploaded exceed this binder's remaining quota?
					if (checkBinderQuotas && (!(bm.isBinderDiskQuotaOk(folder, totalSize)))) {
						// Yes!  Add an appropriate error the reply.
						reply.addError(NLT.get("validateUploadError.quotaExceeded.folder", new String[]{String.valueOf(bm.getMinBinderQuotaLeft(folder) / MEGABYTES)}));
					}

					// Do we need to check the total upload size against
					// this user's quota?
					User user = GwtServerHelper.getCurrentUser();
					long userQuota = user.getDiskQuota();
					if (0 == userQuota) {
						userQuota = user.getMaxGroupsQuota();
						if (0 == userQuota) {
							ZoneConfig zc = getCoreDao().loadZoneConfig(RequestContextHolder.getRequestContext().getZoneId());
							userQuota = zc.getDiskQuotaUserDefault();
						}
					}
					long userQuotaMB       = (userQuota * MEGABYTES);
					Long userDiskSpaceUsed = user.getDiskSpaceUsed();
					if ((0l < userQuota) && (null != userDiskSpaceUsed)) {
						// Yes!  Does it exceed the user's quota?
						if ((totalSize + userDiskSpaceUsed) > userQuotaMB) {
							// Yes!  Add an appropriate error the
							// reply.
							reply.addError(NLT.get("validateUploadError.quotaExceeded.user", new String[]{String.valueOf(userQuota)}));
						}
					}

					// If we detected any quota errors...
					if (reply.hasErrors()) {
						// ...we'll stop with the analysis and return
						// ...what we've got.
						return reply;
					}
				}

				// Scan the UploadInfo's again.
				for (UploadInfo upload:  uploads) {
					// Is this a file upload?
					String name = upload.getName();
					if (upload.isFile()) {
						// Yes!  Does it contain a valid name?
						if (Validator.containsPathCharacters(name)) {
							reply.addError(NLT.get("validateUploadError.invalidName.file", new String[]{name}));
						}
					}
					
					else {
						// No, it must be a folder upload!  Does it
						// contain a valid name?
						if (!(BinderHelper.isBinderNameLegal(name))) {
							reply.addError(NLT.get("validateUploadError.invalidName.folder", new String[]{name}));
						}
					}
				}
				
				// Scan the UploadInfo's again.
				for (UploadInfo upload:  uploads) {
					// Is this upload a file?
					if (!(upload.isFile())) {
						// No!  Skip it.
						continue;
					}
					
					// Does the folder contain an entry with this name?
					String uploadFName = upload.getName();
					FolderEntry fe = fm.getLibraryFolderEntryByFileName((Folder) folder, uploadFName);
					if (null != fe) {
						// Yes!  Track it as a duplicate.
						reply.addDuplicate(upload);
						continue;
					}
					
					// Scan the files attached to the folder.
					for (FileAttachment fa:  folder.getFileAttachments()) {
						// Does this attachment's file exist and is it a match?
						if (fa.getFileExists() && fa.getFileItem().getName().equals(uploadFName)) {
							// Yes!  Track it as a duplicate.
							reply.addDuplicate(upload);
							break;
						}
					}
				}
			}
			
			// If we get here, reply refers to an
			// ValidateUploadsRpcResponseData containing any errors and
			// information duplicates that we encountered.  Return it.
			return reply;
		}
		
		catch (Exception e) {
			// Convert the exception to a GwtTeamingException and throw
			// that.
			if ((!(GwtServerHelper.m_logger.isDebugEnabled())) && m_logger.isDebugEnabled()) {
			     m_logger.debug("GwtViewHelper.validateUploads( SOURCE EXCEPTION ):  ", e);
			}
			throw GwtServerHelper.getGwtTeamingException(e);
		}
	}
	
	/**
	 * Returns true if a string values contains a quick filter and
	 * false otherwise.
	 * 
	 * @param value
	 * @param quickFilter
	 * 
	 * @return
	 */
	public static boolean valueContainsQuickFilter(String value, String quickFilter) {
		if (null != value) {
			value = value.trim().toLowerCase();
		}
		
		if (MiscUtil.hasString(value))
		     return value.contains(quickFilter);
		else return false;
	}

    public static EntityIdentifier toEntityIdentifier(EntityId eid) {
        if (eid.isEntry()) {
            return new EntityIdentifier(eid.getEntityId(), EntityIdentifier.EntityType.folderEntry);
        } else if (eid.isFolder()) {
            return new EntityIdentifier(eid.getEntityId(), EntityIdentifier.EntityType.folder);
        } else if (eid.isWorkspace()) {
            return new EntityIdentifier(eid.getEntityId(), EntityIdentifier.EntityType.workspace);
        }
        throw new UnsupportedOperationException("Can't convert EntityId to EntityIdentifier.  Unknown entity type: " + eid.getEntityType());
    }


}
