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
package org.kablink.teaming;

/**
 * Defines symbols that the core is aware of.
 * 
 * @author Jong Kim
 */
public interface ObjectKeys {
	
    //Default name for the product
	public static final String PRODUCT_NAME_DEFAULT="Kablink";
	public static final String PRODUCT_TITLE_DEFAULT="Kablink";
	public static final String PRODUCT_EDITION_DEFAULT="OpenSource";
	public static final String PRODUCT_CONFERENCING_NAME_DEFAULT="Conference";
	public static final String PRODUCT_CONFERENCING_TITLE_DEFAULT="Kablink Conference";
	
	//Upgrade version
	public static final String PRODUCT_UPGRADE_VERSION="3.1";

	//Original zoneId for V1 zones
	public static final String DEFAULT_ZONE_ID_FOR_V1="1";
	
	//Default account name for admin and guest
	public static final String ADMIN = "admin";
	public static final String GUEST = "guest";
	
	//Directory name for static files
	public static final String STATIC_DIR_PROPERTY = "release.static.dir";
	public static final String STATIC_DIR = "static";
	//Directory name for files that need to have their last modify date validated
	public static final String VALIDATE_DIR = "validate";
	
	//Reserved template and definition names
	public static final String DEFAULT_TEMPLATE_NAME_BLOG="_folder_blog";
	public static final String DEFAULT_TEMPLATE_NAME_MINIBLOG="_folder_miniblog";
	public static final String DEFAULT_TEMPLATE_NAME_PHOTO="_folder_photo";
	public static final String DEFAULT_TEMPLATE_NAME_WIKI="_folder_wiki";
	public static final String DEFAULT_TEMPLATE_NAME_LIBRARY = "_folder_library";
	public static final String DEFAULT_TEMPLATE_NAME_MIRRORED_FILE = "_folder_mirrored_file";
		
	// reserved internalIds for Definitions (not necessarily databaseId cause of multi-zone support)
	//Used only to locate a minimual set of definitions if something isn't configured correctly
	public static final String DEFAULT_FOLDER_DEF="402883b90cc53079010cc539bf260001";
	public static final String DEFAULT_FOLDER_ENTRY_DEF="402883b90cc53079010cc539bf260002";
	public static final String DEFAULT_WORKSPACE_DEF="402883b90cc53079010cc539bf260005";
	public static final String DEFAULT_USER_WORKSPACE_DEF="402883b90cc53079010cc539bf26000b";
	public static final String DEFAULT_EXTERNAL_USER_WORKSPACE_DEF="402883b90cc53079010cc539bf26000e";
	public static final String DEFAULT_PROFILES_DEF="402883b90cc53079010cc539bf260006";
	public static final String DEFAULT_USER_DEF="402883b90cc53079010cc539bf260007";
	public static final String DEFAULT_GROUP_DEF="402883b90cc53079010cc539bf260008";
	public static final String DEFAULT_APPLICATION_DEF="402883b90cc53079010cc539bf26000c";
	public static final String DEFAULT_APPLICATION_GROUP_DEF="402883b90cc53079010cc539bf26000d";
	public static final String DEFAULT_ENTRY_MINIBLOG_DEF="402883b51c48633a011c4866de050002";
	public static final String DEFAULT_ENTRY_GUESTBOOK_DEF="402883c6115753d8011157811ae1000e";
	public static final String DEFAULT_WELCOME_WORKSPACE_DEF="402883b51cbdbdc2011cbdfe2857000b";
	public static final String DEFAULT_MIRRORED_FILE_ENTRY_DEF="402883a41edab1d5011edabc90aa0031";
	public static final String DEFAULT_MIRRORED_FILE_FOLDER_DEF="402883a41edab1d5011edac394df0032";
	public static final String DEFAULT_LIBRARY_ENTRY_DEF="402883c6115753d8011157829367000f";
	public static final String DEFAULT_LIBRARY_FOLDER_DEF="402883c6115753d8011157619e350009";
	public static final String DEFAULT_MIRRORED_FILR_FILE_ENTRY_DEF="402883a41edab1d5011edac394df0033";
	public static final String DEFAULT_MIRRORED_FILR_FILE_FOLDER_DEF="402883a41edab1d5011edac394df0034";

	//reserved internalIds for templates.
	//presence is used to distinquish system templates from customer templates
	public static final String DEFAULT_FOLDER_CONFIG="402883b90d0de1f3010d0df5582b0001";
	public static final String DEFAULT_FOLDER_BLOG_CONFIG="402883b90d0de1f3010d0df5582b000c";
	public static final String DEFAULT_FOLDER_MINIBLOG_CONFIG="402883b90d0de1f3010d0df5582b001c";
	public static final String DEFAULT_FOLDER_WIKI_CONFIG="402883b90d0de1f3010d0df5582b000d";
	public static final String DEFAULT_FOLDER_CALENDAR_CONFIG="402883b90d0de1f3010d0df5582b000e";
	public static final String DEFAULT_FOLDER_GUESTBOOK_CONFIG="402883b90d0de1f3010d0df5582b000f";
	public static final String DEFAULT_FOLDER_PHOTO_CONFIG="402883b90d0de1f3010d0df5582b0010";
	public static final String DEFAULT_FOLDER_LIBRARY_CONFIG="402883b90d0de1f3010d0df5582b0011";
	public static final String DEFAULT_FOLDER_MIRRORED_FILE_CONFIG="402883b90d0de1f3010d0df5582b0018";
	public static final String DEFAULT_FOLDER_TASK_CONFIG="402883b90cc53079010cc539bf260009";
	public static final String DEFAULT_FOLDER_MILESTONE_CONFIG="402883b90cc53079010cc539bf260011";
	public static final String DEFAULT_FOLDER_SURVEY_CONFIG="402883b90cc53079010cc539bf260010";
	public static final String DEFAULT_WORKSPACE_CONFIG="402883b90d0de1f3010d0df5582b0002";
	public static final String DEFAULT_USER_WORKSPACE_CONFIG="402883b90d0de1f3010d0df5582b0004";
	public static final String DEFAULT_EXTERNAL_USER_WORKSPACE_CONFIG="402883b90d0de1f3010d0df5582c0007";
	public static final String DEFAULT_TEAM_WORKSPACE_CONFIG="402883c1129b1f8101129b28bc620004";
	public static final String DEFAULT_PROJECT_WORKSPACE_CONFIG="402883c1129b1f8101129b28bc620005";
	public static final String DEFAULT_DISCUSSIONS_WORKSPACE_CONFIG="402880e619c6250f0119c62794b9000c";
	public static final String DEFAULT_WORKSPACE_WELCOME_CONFIG="402883b90d0de1f3010d0df5582b0016";
	public static final String DEFAULT_FOLDER_FILR_ROOT_CONFIG="402883b90d0de1f3010d0df5582b0032";
	public static final String DEFAULT_FOLDER_FILR_HOME_FOLDER_CONFIG="402883b90d0de1f3010d0df5582b0040";
	
	//reserved internalIds for binders
	public static final String PROFILE_ROOT_INTERNALID="402883b90d0de1f3010d0df5582b0005";
	public static final String TOP_WORKSPACE_INTERNALID="402883b90d0de1f3010d0df5582b0007";
	public static final String TEAM_ROOT_INTERNALID="402883b90d0de1f3010d0df5582b0009";
	public static final String GLOBAL_ROOT_INTERNALID="402883b90d0de1f3010d0df5582b000a";
	public static final String NET_FOLDERS_ROOT_INTERNALID="402883b90d0de1f3010d0df5582b001a";
	//reserverd internalids for alluser group
	public static final String ALL_USERS_GROUP_INTERNALID="402883b90d0de1f3010d0df5582b0006";
	//reserverd internalids for allextuser group
	public static final String ALL_EXT_USERS_GROUP_INTERNALID="402883b90d0de1f3010d0df5582b0022";
	//reserverd internalId user posting agent
	public static final String ANONYMOUS_POSTING_USER_INTERNALID="402883b90d0de1f3010d0df5582b0008";
	//super user bypasses all acl and operation checks
	public static final String SUPER_USER_INTERNALID="402883b90d0de1f3010d0df5582b000b";
	//id to run background jobs under - also acts as a super user but cannot loggin as this person
	public static final String JOB_PROCESSOR_INTERNALID="402883b90d0de1f3010d0df5582b0012";
	//id to run synchronization agent (teaming connector) under
	public static final String SYNCHRONIZATION_AGENT_INTERNALID="402883b90d0de1f3010d0df5582b001d";
	//id to run file sync agent (for mirrored folder) under
	public static final String FILE_SYNC_AGENT_INTERNALID="402883b90d0de1f3010d0df5582b001e";
	//shared guest account
	public static final String GUEST_USER_INTERNALID="402883b90d0de1f3010d0df5582b0013";
	//reserverd internalids for allapplication group
	public static final String ALL_APPLICATIONS_GROUP_INTERNALID="402883b90d0de1f3010d0df5582b0014";
	//reserved internaId for _zoneAdministration pseudo role 
	public static final String FUNCTION_SITE_ADMIN_INTERNALID="402883b90d0de1f3010d0df5582b0015";
	//reserved id for _enableGuestAccess pseudo role  
	public static final String FUNCTION_ADD_GUEST_ACCESS_INTERNALID="402883b90d0de1f3010d0df5582b0017";
	//reserved id for _tokenRequester pseudo role  
	public static final String FUNCTION_TOKEN_REQUESTER_INTERNALID="402883b90d0de1f3010d0df5582b0019";
	//reserved id for _onlySeeGroupMembers pseudo role  
	public static final String FUNCTION_ONLY_SEE_GROUP_MEMBERS_INTERNALID="402883b90d0de1f3010d0df5582b0020";
	//reserved id for _overrideOnlySeeGroupMembers pseudo role  
	public static final String FUNCTION_OVERRIDE_ONLY_SEE_GROUP_MEMBERS_INTERNALID="402883b90d0de1f3010d0df5582b0021";
	//reserved id for _manageResourceDrivers pseudo role  
	public static final String FUNCTION_MANAGE_RESOURCE_DRIVERS_INTERNALID="402883b90d0de1f3010d0df5582b0022";
	//reserved id for _createFileSpaces pseudo role  
	public static final String FUNCTION_CREATE_FILESPACES_INTERNALID="402883b90d0de1f3010d0df5582b0023";
	//reserved id for _enableSharing pseudo role  
	public static final String FUNCTION_ENABLE_INTERNAL_SHARING_INTERNALID="402883b90d0de1f3010d0df5582b0024";
	//reserved id for _enableExternalSharing pseudo role 
	public static final String FUNCTION_ENABLE_EXTERNAL_SHARING_INTERNALID="402883b90d0de1f3010d0df5582b0025";
	//reserved id for _enablePublicSharing pseudo role
	public static final String FUNCTION_ENABLE_PUBLIC_SHARING_INTERNALID="402883b90d0de1f3010d0df5582b0034";
	//reserved id for _enableForwardSharing pseudo role
	public static final String FUNCTION_ENABLE_FORWARD_SHARING_INTERNALID="402883b90d0de1f3010d0df5582b0038";
	//reserved id for _allowSharingInternal pseudo role  
	public static final String FUNCTION_ALLOW_SHARING_INTERNAL_INTERNALID="402883b90d0de1f3010d0df5582b0035";
	//reserved id for _allowSharingExternal pseudo role 
	public static final String FUNCTION_ALLOW_SHARING_EXTERNAL_INTERNALID="402883b90d0de1f3010d0df5582b0036";
	//reserved id for _allowSharingPublic pseudo role
	public static final String FUNCTION_ALLOW_SHARING_PUBLIC_INTERNALID="402883b90d0de1f3010d0df5582b0037";
	//reserved id for _allowSharingForward pseudo role
	public static final String FUNCTION_ALLOW_SHARING_FORWARD_INTERNALID="402883b90d0de1f3010d0df5582b0039";
	//reserved id for _allowAccessNetFolder pseudo role
	public static final String FUNCTION_ALLOW_ACCESS_NET_FOLDER_INTERNALID="402883b90d0de1f3010d0df5582b0042";
	//reserved id for _allowSharingAllInternal pseudo role  
	public static final String FUNCTION_ENABLE_SHARING_ALL_INTERNAL_INTERNALID="402883b90d0de1f3010d0df5582b0043";
	//reserved id for _allowSharingAllExternal pseudo role 
	public static final String FUNCTION_ENABLE_SHARING_ALL_EXTERNAL_INTERNALID="402883b90d0de1f3010d0df5582b0044";

	//reserved id for View Binder Title role
	public static final String FUNCTION_VIEW_BINDER_TITLE_INTERNALID="402883b90d0de1f3010d0df5582b0033";

	//reserved id for Filr file read role
	public static final String FUNCTION_FILR_FILE_READ_INTERNALID="402883b90d0de1f3010d0df5582b0031";
	//reserved id for Filr file write role
	public static final String FUNCTION_FILR_FILE_WRITE_INTERNALID="402883b90d0de1f3010d0df5582b0026";
	//reserved id for Filr file owner role
	public static final String FUNCTION_FILR_FILE_OWNER_INTERNALID="402883b90d0de1f3010d0df5582b0027";
	//reserved id for Filr folder read role
	public static final String FUNCTION_FILR_VIEWER_INTERNALID="402883b90d0de1f3010d0df5582b0028";
	//reserved id for Filr folder write role
	public static final String FUNCTION_FILR_EDITOR_INTERNALID="402883b90d0de1f3010d0df5582b0029";
	//reserved id for Filr folder owner role
	public static final String FUNCTION_FILR_CONTRIBUTOR_INTERNALID="402883b90d0de1f3010d0df5582b0030";

	//reserved id put in acls/membership to indicate owner of object has access
	//search engine stores the ownerId of the entity in place of this
	public static final Long OWNER_USER_ID = Long.valueOf(-1);
	//reserved id put in acls/membership to indicate team members have access
	//search engine stores the actual members of the team in place of this
	public static final Long TEAM_MEMBER_ID = Long.valueOf(-2);
    // Reserved id used by the REST API for the "My Files" virtual binder
	public static final Long MY_FILES_ID = Long.valueOf(-100);
    // Reserved id used by the REST API for the "Shared With Me" virtual binder
	public static final Long SHARED_WITH_ME_ID = Long.valueOf(-101);
    // Reserved id used by the REST API for the "Shared By Me" virtual binder
	public static final Long SHARED_BY_ME_ID = Long.valueOf(-102);
    // Reserved id used by the REST API for the "Net Folders" virtual binder
	public static final Long NET_FOLDERS_ID = Long.valueOf(-103);
	//Use -1 to reserve binderId field.  Shouldn't be generated as real binderId. Used for global definitions cause mysql won't enforce the namekey unique
	//constraint if field is null.  Also used for global userProperties, cause null cannot be used in key field.
	public static final Long RESERVED_BINDER_ID=Long.valueOf(-1);
    //custom property prefix from ssf.properties
	public static final String CUSTOM_PROPERTY_PREFIX="custom.";

	//keys for data stored in request session
	public static final String SESSION_USERPROPERTIES="userProperties";
	public static final String SESSION_TABS="userTabs";
	public static final String SESSION_CLIPBOARD="userClipboard";
	public static final String SESSION_SAVE_LOCATION_ID="saveLocationId";
	public static final String SESSION_TEAMING_LIVE_TRACKED_BINDER_IDS="teamingLiveTrackedBinderIds";
	public static final String SESSION_TEAMING_LIVE_TRACKED_TYPE="teamingLiveTrackedType";
	public static final String SESSION_ACL_CACHE="aclCache";
	public static final String SESSION_ACL_CACHE_USER_ID="aclCacheUserId";
	public static final int SESSION_ACL_CACHE_TIMEOUT=60*1000; //Only use cache if time is within 1 minute

    //map keys returned from modules
    public static final String BINDER="binder";
    public static final String SEARCH_ENTRIES="search_entries";
    public static final String FULL_ENTRIES="database_entries";
    public static final String COMMUNITY_ENTITY_TAGS="community_entries_tags";
    public static final String PERSONAL_ENTITY_TAGS="personal_entries_tags";
    public static final String FOLDER_ENTRY_ANCESTORS="folderEntryAncestors";
    public static final String FOLDER_ENTRY_DESCENDANTS="folderEntryDescendants";
    public static final String FOLDER_ENTRY="folderEntry";
    public static final String SEARCH_COUNT_TOTAL="searchCountTotal";
    public static final String TOTAL_SEARCH_COUNT="totalSearchCount";
    public static final String TOTAL_SEARCH_RECORDS_RETURNED="totalSearchRecordsReturned";
    public static final String FILES_FROM_APPLET_FOR_BINDER="ss_attachFile";
    public static final String FILES_FROM_APPLET_FOLDER_INFO_FOR_BINDER="filesFromAppletFolderInfo";
    public static final String WORKFLOW_QUESTION_TEXT="workflow_questionText";
    public static final String WORKFLOW_QUESTION_EVERYONE_MUST_RESPOND="workflow_questionEveryoneMustRespond";
    public static final String WORKFLOW_QUESTION_RESPONSES="workflow_questionResponses";
    public static final String WORKFLOW_QUESTION_RESPONSE_COUNT="workflow_questionResponseCount";
    public static final String WORKFLOW_QUESTION_RESPONDERS="workflow_questionResponders";
    public static final String WORKFLOW_QUESTION_RESPONSE_RESPONDERS="workflow_questionReponseResponders";
 
    //map keys from definition module
    public static final String DEFINITION_ENTRY_DATA="entryData";
    public static final String DEFINITION_FILE_DATA="fileData";
    public static final String DEFINITION_ERRORS="errors";
    public static final String DEFINITION_TEAM_TYPE="team";
	public static final String DEFINITION_WORKSPACE_REMOTE_APPLICATION="workspaceRemoteApp";
	public static final String DEFINITION_FOLDER_REMOTE_APPLICATION="folderRemoteApp";
	public static final String DEFINITION_ENTRY_REMOTE_APPLICATION="entryRemoteApp";	
    
    //Default role types (scope)
    public static final String ROLE_TYPE_ZONE="zone";
    public static final String ROLE_TYPE_BINDER="binder";
    public static final String ROLE_TYPE_ENTRY="entry";
    public static final String ROLE_TYPE_FILR="filr";
    
    //Default role titles
    public static final String ROLE_TITLE_READ="__role.read";
    public static final String ROLE_TITLE_READ_REPLY="__role.readReply";
    public static final String ROLE_TITLE_WRITE="__role.write";
    public static final String ROLE_TITLE_DELETE="__role.delete";
    public static final String ROLE_TITLE_CHANGE_ACL="__role.changeAcl";
    public static final String ROLE_TITLE_VISITOR="__role.visitor";
    public static final String ROLE_TITLE_PARTICIPANT="__role.participant";
    public static final String ROLE_TITLE_GUEST_PARTICIPANT="__role.guestParticipant";
    public static final String ROLE_TITLE_VIEW_BINDER_TITLE="__role.viewBinderTitle";
    public static final String ROLE_TITLE_TEAM_MEMBER="__role.teamMember";
    public static final String ROLE_TITLE_BINDER_ADMIN="__role.binderAdmin";
    public static final String ROLE_TITLE_WORKSPACE_CREATOR="__role.workspaceCreator";
    public static final String ROLE_TITLE_SITE_ADMIN="__role.siteAdmin"; //Depreciated
    public static final String ROLE_TITLE_FILR_VIEWER="__role.FilrViewer"; 
    public static final String ROLE_TITLE_FILR_EDITOR="__role.FilrEditor"; 
    public static final String ROLE_TITLE_FILR_CONTRIBUTOR="__role.FilrContributor"; 
    
    //reserved pseudo roles. Only have 1 right each and cannot be edited by user.
    //Used to simulate a group of users without actually using the group facility.
    //Allows access checking to work consistently
    public static final String ROLE_ZONE_ADMINISTRATION="__role.zoneAdministration";
    public static final String ROLE_ADD_GUEST_ACCESS="__role.addGuestAccess";
    public static final String ROLE_TOKEN_REQUESTER="__role.tokenRequester";
    public static final String ROLE_ONLY_SEE_GROUP_MEMBERS="__role.onlySeeGroupMembers";
    public static final String ROLE_OVERRIDE_ONLY_SEE_GROUP_MEMBERS="__role.overrideOnlySeeGroupMembers";
    public static final String ROLE_MANAGE_RESOURCE_DRIVERS="__role.manageResourceDrivers";
    public static final String ROLE_CREATE_FILESPACES="__role.createFilespaces";
    public static final String ROLE_ENABLE_SHARING_INTERNAL="__role.enableSharing";
    public static final String ROLE_ENABLE_SHARING_EXTERNAL="__role.enableSharingExternal";
    public static final String ROLE_ENABLE_SHARING_PUBLIC="__role.enableSharingPublic";
    public static final String ROLE_ENABLE_SHARING_FORWARD="__role.enableSharingForward";
    public static final String ROLE_ENABLE_SHARING_ALL_INTERNAL="__role.enableSharingAllInternal";
    public static final String ROLE_ENABLE_SHARING_ALL_EXTERNAL="__role.enableSharingAllExternal";
    public static final String ROLE_ALLOW_SHARING_INTERNAL="__role.allowSharing";
    public static final String ROLE_ALLOW_SHARING_EXTERNAL="__role.allowSharingExternal";
    public static final String ROLE_ALLOW_SHARING_PUBLIC="__role.allowSharingPublic";
    public static final String ROLE_ALLOW_SHARING_FORWARD="__role.allowSharingForward";
    public static final String ROLE_ALLOW_ACCESS_NET_FOLDER="__role.allowAccessNetFolder";

    //map keys from admin/sendMail
    public static final String SENDMAIL_ERRORS="errors";
    public static final String SENDMAIL_DISTRIBUTION="distributionList";
    public static final String SENDMAIL_STATUS="status";
    public static final String SENDMAIL_STATUS_SENT="sent";
    public static final String SENDMAIL_STATUS_FAILED="failed";
    public static final String SENDMAIL_STATUS_SCHEDULED="scheduled";
    	
    
    public static final int LISTING_MAX_PAGE_SIZE = 100;
    public static final int MAX_TAG_LENGTH = 60;
    public static final int MAX_NUMBER_OF_FAVORITES = 500;
    public static final int MAX_BINDER_ENTRIES_RESULTS = 2000;
    public static final int MAX_EXPANDED_BINDERS = 2500;
    public static final long SEEN_TIMEOUT_DAYS = (long)30;  //entries older than 30 days are marked seen
    public static final long SEEN_MAP_TIMEOUT = (long)SEEN_TIMEOUT_DAYS*24*60*60*1000;  //older than 30 days return seen
    public static final int MAX_FILE_SIZE_DECIMAL_PLACES = 8;
    
    public static final String BINDER_PROPERTY_FILTERS = "binderFilters";
    public static final String BINDER_PROPERTY_FOLDER_COLUMNS = "folderColumns";
    public static final String BINDER_PROPERTY_FOLDER_COLUMN_SORT_ORDER = "folderColumnSortOrder";
    public static final String BINDER_PROPERTY_FOLDER_COLUMN_TITLES = "folderColumnTitles";
    public static final String BINDER_PROPERTY_MYFILES_DIR = "myFilesDir";
    public static final String BINDER_PROPERTY_TEAM_MEMBERS = "teamMembers";
    public static final String BINDER_PROPERTY_WIKI_HOMEPAGE = "wikiHomepage";
    public static final String BINDER_PROPERTY_UPGRADE_VERSION = "binderUpgradeVersion";
    public static final String BINDER_PROPERTY_TASK_LINKAGE = "taskLinkage";
    public static final String BINDER_PROPERTY_TASK_ADDED = "taskAdded";
    public static final String BINDER_PROPERTY_TASK_CHANGE = "taskChange";
    public static final String BINDER_PROPERTY_TASK_ID = "taskId";
    public static final String BINDER_PROPERTY_TASK_MODIFIED = "taskModified";
    public static final String BINDER_PROPERTY_TASK_EXPAND_GRAPHS = "taskExpandGraphs";
    
    // Global properties
    public static final String GLOBAL_PROPERTY_DEFAULT_LOCALE = "defaultLocale";
    public static final String GLOBAL_PROPERTY_DEFAULT_TIME_ZONE = "defaultTimeZone";

    public static final String CALENDAR_MODE_TYPE_PHYSICAL = "physical";
    public static final String CALENDAR_MODE_TYPE_VIRTUAL = "virtual";
    public static final String CALENDAR_MODE_TYPE_MY_EVENTS = "myEvents";

    public static final String CONFIG_PROPERTY_REPOSITORY = "repository";
    public static final String CONFIG_PROPERTY_REPOSITORIES = "repositories";

    public static final String DASHBOARD_COMPONENT_BLOG_SUMMARY = "blog";
    public static final String DASHBOARD_COMPONENT_CALENDAR_SUMMARY = "calendar";
    public static final String DASHBOARD_COMPONENT_GUESTBOOK_SUMMARY = "guestbook";
    public static final String DASHBOARD_COMPONENT_TASK_SUMMARY = "task";
    public static final String DASHBOARD_COMPONENT_BUDDY_LIST = "buddyList";
    public static final String DASHBOARD_COMPONENT_TEAM_MEMBERS_LIST = "teamMembersList";
    public static final String DASHBOARD_COMPONENT_FOLDER = "folder";
    public static final String DASHBOARD_COMPONENT_SEARCH = "search";
    public static final String DASHBOARD_COMPONENT_GALLERY = "gallery";    
    public static final String DASHBOARD_COMPONENT_WIKI_SUMMARY = "wiki";    
    public static final String DASHBOARD_COMPONENT_WORKSPACE_TREE = "workspaceTree";
    public static final String DASHBOARD_COMPONENT_REMOTE_APPLICATION = "remoteApplication";
    public static final String DASHBOARD_COMPONENT_HTML = "html";
    public static final String DASHBOARD_COMPONENT_IFRAME = "iframe";
    
    public static final String FOLDER_ENTRY_TO_BE_SHOWN = "folder_entry_to_be_shown";
    public static final String FOLDER_MODE_TYPE = "folderModeType";
    
    public static final String MASHUP_ATTR_COLS = "cols";
    public static final String MASHUP_ATTR_COL_WIDTH = "colWidth";
    public static final String MASHUP_ATTR_COL_WIDTHS = "colWidths";
    public static final String MASHUP_ATTR_CUSTOM_JSP_NAME = "customJsp";
    public static final String MASHUP_ATTR_CUSTOM_JSP_PATH_TYPE = "pathType";
    public static final String MASHUP_ATTR_CUSTOM_JSP_PATH_TYPE_CUSTOM_JSP = "custom_jsp";
    public static final String MASHUP_ATTR_CUSTOM_JSP_PATH_TYPE_EXTENSION = "extension";
    public static final String MASHUP_ATTR_DATA = "data";
    public static final String MASHUP_ATTR_ENTRY_ID = "entryId";
    public static final String MASHUP_ATTR_ENTRIES_TO_SHOW = "entriesToShow";
    public static final String MASHUP_ATTR_FOLDER_ID = "folderId";
    public static final String MASHUP_ATTR_BINDER_ID = "binderId";
    public static final String MASHUP_ATTR_GRAPHIC = "graphic";
    public static final String MASHUP_ATTR_TITLE = "title";
    public static final String MASHUP_ATTR_URL = "url";
    public static final String MASHUP_ATTR_ENHANCED_VIEW_JSP_NAME = "jspName";
    public static final String MASHUP_ATTR_ENHANCED_VIEW_MY_CALENDAR_EVENTS = "landing_page_my_calendar_events.jsp";
    public static final String MASHUP_ATTR_ENHANCED_VIEW_MY_TASKS = "landing_page_my_tasks.jsp";
    public static final String MASHUP_ATTR_ZONE_UUID = "zoneUUID";
    public static final String MASHUP_TYPE_CUSTOM_JSP = "customJsp";
    public static final String MASHUP_TYPE_ENHANCED_VIEW = "enhancedView";
    public static final String MASHUP_TYPE_ENTRY = "entry";
    public static final String MASHUP_TYPE_FOLDER = "folder";
    public static final String MASHUP_TYPE_GRAPHIC = "graphic";
    public static final String MASHUP_TYPE_HTML = "html";
    public static final String MASHUP_TYPE_IFRAME = "iframe";
    public static final String MASHUP_TYPE_BINDER_URL = "binderUrl";
    public static final String MASHUP_TYPE_ENTRY_URL = "entryUrl";
    public static final String MASHUP_TYPE_TABLE = "table";
    public static final String MASHUP_TYPE_TABLE_START = "tableStart";
    public static final String MASHUP_TYPE_TABLE_END = "tableEnd";
    public static final String MASHUP_TYPE_TABLE_END_DELETE = "tableEnd_delete";
    public static final String MASHUP_VIEW_TYPE_MY_CALENDAR = "MyCalendar";
    public static final String MASHUP_VIEW_TYPE_MY_TASKS = "MyTasks";
    public static final String MASHUP_TYPE_TABLE_COL = "tableCol";
    public static final String MASHUP_TYPE_LIST = "list";
    public static final String MASHUP_TYPE_LIST_START = "listStart";
    public static final String MASHUP_TYPE_LIST_END = "listEnd";
    public static final String MASHUP_TYPE_LIST_END_DELETE = "listEnd_delete";
    
    public static final String RELEVANCE_DASHBOARD_OVERVIEW = "overview";
    public static final String RELEVANCE_DASHBOARD_PROFILE = "profile";
    public static final String RELEVANCE_DASHBOARD_TRACKED_ITEMS = "trackedItems";
    public static final String RELEVANCE_DASHBOARD_WHATS_NEW = "whatsNew";
    public static final String RELEVANCE_DASHBOARD_TASKS_VIEW_DEFAULT = "2weeks";
    public static final String RELEVANCE_DASHBOARD_WHATS_NEW_VIEW_DEFAULT = "teams";
    public static final String RELEVANCE_DASHBOARD_WHATS_NEW_VIEW_DEFAULT_GUEST = "site";
    public static final String RELEVANCE_DASHBOARD_WHATS_NEW_VIEW_TEAMS = "teams";
    public static final String RELEVANCE_DASHBOARD_WHATS_NEW_VIEW_TRACKED = "tracked";
    public static final String RELEVANCE_DASHBOARD_WHATS_NEW_VIEW_SITE = "site";
    public static final String RELEVANCE_DASHBOARD_TASKS_AND_CALENDARS = "tasks_and_calendars";
    public static final String RELEVANCE_DASHBOARD_ACTIVITIES = "activities";
    public static final String RELEVANCE_DASHBOARD_MINIBLOGS = "miniblogs";
    public static final String RELEVANCE_DASHBOARD_FILESPACES = "filespaces";
    public static final String RELEVANCE_TRACKED_BINDERS = "trackedBinders";
    public static final String RELEVANCE_TRACKED_CALENDARS = "trackedCalendars";
    public static final String RELEVANCE_TRACKED_PEOPLE = "trackedPeople";
    
    public static final String RELEVANCE_PAGE_ENTRIES_VIEWED = "entriesViewed";
    public static final String RELEVANCE_PAGE_NEW_SITE = "newSite";
    public static final String RELEVANCE_PAGE_NEW_TEAMS = "newTeams";
    public static final String RELEVANCE_PAGE_NEW_TRACKED = "newTracked";
    public static final String RELEVANCE_PAGE_ACTIVITIES = "activities";
    public static final String RELEVANCE_PAGE_MINIBLOGS = "miniblogs";
    public static final String RELEVANCE_PAGE_DOCS = "docs";
    public static final String RELEVANCE_PAGE_HOT = "hot";
    public static final String RELEVANCE_PAGE_SHARED = "shared";
    public static final String RELEVANCE_PAGE_TASKS = "tasks";
    public static final String RELEVANCE_PAGE_VISITORS = "visitors";
    
    public static final String SEARCH_CASE_SENSITIVE = "case_sensitive";
    public static final String SEARCH_CONTEXT = "context";
    public static final String SEARCH_CONTEXT_TYPE_COLLECTION = "collection";
    public static final String SEARCH_CONTEXT_TYPE_BINDER = "binder";
    public static final String SEARCH_CONTEXT_TYPE_ENTRY = "entry";
    public static final String SEARCH_CONTEXT_TYPE_THER = "other";
    public static final String SEARCH_CONTEXT_COLLECTION = "contextCollection";
    public static final String SEARCH_CONTEXT_BINDER_ID = "contextBinderId";
    public static final String SEARCH_CONTEXT_ENTRY_ID = "contextEntryId";
    public static final String SEARCH_OFFSET = "offset";
    public static final String SEARCH_MAX_HITS = "maxHits";
    public static final String SEARCH_USER_MAX_HITS = "userMaxHits";
    public static final String SEARCH_USER_OFFSET = "userOffset";
    public static final String SEARCH_INCLUDE_NESTED_BINDERS = "includeNestedBinders";
    public static final String SEARCH_SCOPE = "scope";
    public static final String SEARCH_SCOPE_ALL = "all";
    public static final String SEARCH_SCOPE_MY_FILES = "myFiles";
    public static final String SEARCH_SCOPE_NET_FOLDERS = "netFolders";
    public static final String SEARCH_SCOPE_SHARED_WITH_ME = "sharedWithMe";
    public static final String SEARCH_SCOPE_CURRENT = "current";
    public static final int SEARCH_MAX_HITS_DEFAULT = 10;
    public static final int SEARCH_MAX_HITS_SUB_BINDERS = 1000;
    public static final int SEARCH_MAX_HITS_FOLDER_ENTRIES = 1000;
    public static final int SEARCH_MAX_HITS_FOLDER_ENTRIES_WIKI = 10000;
    public static final int SEARCH_MAX_HITS_FOLDER_ENTRIES_EXPORT = 100000;
    public static final int SEARCH_MAX_HITS_ALL_USERS = 1000;
    public static final int SEARCH_MAX_HITS_LIMIT = 1000000;
    public static final int SEARCH_RESULTS_TO_CREATE_STATISTICS = 200;
    public static final String SEARCH_SEARCH_FILTER = "searchFilter";
    public static final String SEARCH_SEARCH_DYNAMIC_FILTER = "searchDynamicFilter";
    public static final String SEARCH_SORT_BY = "sortBy";
    public static final String SEARCH_SORT_DESCEND = "sortDescend";
    public static final String SEARCH_SORT_BY_SECONDARY = "sortBySecondary";
    public static final String SEARCH_SORT_DESCEND_SECONDARY = "sortDescendSecondary";
    public static final String SEARCH_SORT_NONE = "sortNone";
    public static final String SEARCH_SORT_BY_RELEVANCE = "sortByRelevance";
    public static final String SEARCH_SORT_BY_DATE = "sortByDate";
    public static final String SEARCH_SORT_BY_RATING = "sortByRating";
    public static final String SEARCH_SORT_BY_REPLY_COUNT = "sortByReplyCount";
    public static final String SEARCH_START_DATE = "startDate";
    public static final String SEARCH_END_DATE = "endDate";
    public static final String SEARCH_FILTER_AND = "searchFilterAnd";
    public static final String SEARCH_FILTER_OR = "searchFilterOr";
    public static final String SEARCH_CRITERIA_AND = "searchCriteriaAnd";
    public static final String SEARCH_CRITERIA_OR = "searchCriteriaOr";
    public static final String SEARCH_DASHBOARD_CURRENT_BINDER_ID = "searchDashboardBinderId";
    public static final String PAGE_ENTRIES_PER_PAGE = "entriesPerPage";
    public static final String SEARCH_PAGE_ENTRIES_PER_PAGE = "entriesPerSearchPage";
    public static final String SEARCH_YEAR_MONTH = "yearMonth";
    public static final String SEARCH_EVENT_DAYS = "eventDays";
    public static final String SEARCH_COMMUNITY_TAG = "searchCommunityTag";
    public static final String SEARCH_PERSONAL_TAG = "searchPersonalTag";
    public static final String SEARCH_ANCESTRY = "ancestry";
    public static final String SEARCH_TITLE = "searchTitle";
    public static final String SEARCH_PRE_DELETED = "__preDeleted";
    public static final String SEARCH_LASTACTIVITY_DATE_START = "lastActivityDateStart";
    public static final String SEARCH_LASTACTIVITY_DATE_END = "lastActivityDateEnd";
    public static final String SEARCH_CREATION_DATE_START = "creationDateStart";
    public static final String SEARCH_CREATION_DATE_END = "creationDateEnd";
    public static final String SEARCH_IS_PERSON = "searchIsPerson";
    public static final String SEARCH_IS_INTERNAL = "searchIsInternal";
    public static final String SEARCH_IS_EXTERNAL = "searchIsExternal";
    public static final String SEARCH_IS_DISABLED_USERS = "searchIsDisabledUsers";
    public static final String SEARCH_IS_ENABLED_USERS  = "searchIsEnabledUsers";
    
    //Search Result Identifier
    public static final String SEARCH_RESULTS_DISPLAY="ss_searchResultListing";

    //file repository
    public static final String FI_ADAPTER = "fiAdapter";
	public static final String PI_SYNCH_TO_SOURCE = "_synchToSource";
	
	//Resource Drivers
	public static final String RESOURCE_DRIVER_READ_ONLY = "readonly";
	public static final String RESOURCE_DRIVER_SYNCH_TOP_DELETE = "synchTopDelete";
	public static final String RESOURCE_DRIVER_PUT_REQUIRES_CONTENT_LENGTH = "putRequiresContentLength";
	public static final String RESOURCE_DRIVER_ALLOW_SELF_SIGNED_CERTIFICATE = "allowSelfSignedCertificate";
	public static final String RESOURCE_DRIVER_HOST_URL = "hostUrl";
	public static final String RESOURCE_DRIVER_ACCOUNT_NAME = "accountName";
	public static final String RESOURCE_DRIVER_PASSWORD = "password";
	public static final String RESOURCE_DRIVER_SHARE_NAME = "shareName";
	public static final String RESOURCE_DRIVER_SERVER_NAME = "serverName";
	public static final String RESOURCE_DRIVER_SERVER_IP = "serverIP";
	public static final String RESOURCE_DRIVER_VOLUME = "volume";
	public static final String RESOURCE_DRIVER_MODIFIED_ON = "modifiedOn";
	
    //User and Principal objects
    public static final int USER_STATUS_DATABASE_FIELD_LENGTH = 256;

    //User properties
    public static final String USER_DISPLAY_STYLE_DEFAULT = "newpage";
    public static final String USER_DISPLAY_STYLE_ACCESSIBLE = "accessible";
    public static final String USER_DISPLAY_STYLE_IFRAME = "iframe";
    public static final String USER_DISPLAY_STYLE_NEWPAGE = "newpage";
    public static final String USER_DISPLAY_STYLE_POPUP = "popup";
    public static final String USER_DISPLAY_STYLE_VERTICAL = "vertical";
    public static final String USER_PROPERTY_ALLOW_ADHOC_FOLDERS = "allowAdhocFolders";
    public static final String USER_PROPERTY_CALENDAR_VIEWMODE = "calendarViewMode";
    public static final String USER_PROPERTY_DASHBOARD_GLOBAL = "dashboard_global";
    public static final String USER_PROPERTY_DASHBOARD_SHOW_ALL = "dashboard_show_all";
    public static final String USER_PROPERTY_DEBUG = "debugMode";
    public static final String USER_PROPERTY_DEFAULT_HOME_PAGE = "defaultHomePage";
    public static final String USER_PROPERTY_DISPLAY_STYLE = "displayStyle";
    public static final String USER_PROPERTY_DISPLAY_DEFINITION = "displayDefinition";
    public static final String USER_PROPERTY_FILR_ADMIN_TASKS = "filrAdminTasks";
    public static final String USER_PROPERTY_HELP_CPANEL_SHOW = "help_cpanel_show";
    public static final String USER_PROPERTY_RELEVANCE_MAP = "relevanceMap";
    public static final String USER_PROPERTY_RELEVANCE_TAB = "relevanceTab";
    public static final String USER_PROPERTY_RELEVANCE_TAB_TASKS_TYPE = "relevanceTabTasksType";
    public static final String USER_PROPERTY_RELEVANCE_TAB_WHATS_NEW_TYPE = "relevanceTabWhatsNewType";
    public static final String USER_PROPERTY_SEARCH_FILTERS_V1 = "searchFilters";//obsolete
    public static final String USER_PROPERTY_SEARCH_FILTERS = "searchFilterMap";
    public static final String USER_PROPERTY_USER_FILTER = "userFilter";
    public static final String USER_PROPERTY_USER_FILTERS = "userFilters";
    public static final String USER_PROPERTY_USER_FILTER_GLOBAL = "global";
    public static final String USER_PROPERTY_USER_FILTER_PERSONAL = "personal";
    public static final String USER_PROPERTY_USER_FILTER_SCOPE = "userFilterScope";
    public static final String USER_PROPERTY_APPCONFIGS = "userAppConfigs";
    public static final String USER_PROPERTY_FAVORITES = "userFavorites";
    public static final String USER_PROPERTY_SHOW_DESKTOP_APP_DOWNLOAD = "showDesktopAppDownload";
    public static final String USER_PROPERTY_FOLDER_COLUMNS = "userFolderColumns";
    public static final String USER_PROPERTY_FOLDER_ENTRY_DLG_POSITION = "userFolderEntryDlgPosition";
    public static final String USER_PROPERTY_BINDER_SHOW_ACCESSORIES = "userBinderShowAccessories";
    public static final String USER_PROPERTY_DESKTOP_APP_ACCESS_FILR = "userDesktopApp_AccessFilr";
    public static final String USER_PROPERTY_DESKTOP_APP_CACHE_PWD = "userDesktopApp_CachePwd";
    public static final String USER_PROPERTY_FOLDER_COLUMN_SORT_ORDER = "userFolderColumnSortOrder";
    public static final String USER_PROPERTY_FOLDER_COLUMN_TITLES = "userFolderColumnTitles";
    public static final String USER_PROPERTY_MOBILE_APPS_ACCESS_FILR = "userMobileApps_AccessFilr";
    public static final String USER_PROPERTY_MOBILE_APPS_CACHE_CONTENT = "userMobileApps_CacheContent";
    public static final String USER_PROPERTY_MOBILE_APPS_CACHE_PWD = "userMobileApps_CachePwd";
    public static final String USER_PROPERTY_MOBILE_APPS_PLAY_WITH_OTHER_APPS = "userMobileApps_PlayWithOtherApps";
    public static final String USER_PROPERTY_MOBILE_BINDER_IDS = "userMobileBinderIds";
    public static final String USER_PROPERTY_PERMALINK_URL = "userPermalinkUrl";
    public static final String USER_PROPERTY_PINNED_ENTRIES = "userPinnedEntries";
    public static final String USER_PROPERTY_PORTAL_URL = "userPortalUrl";
    public static final String USER_PROPERTY_REGION_VIEW = "regionView";
    public static final String USER_PROPERTY_SEARCH_RESULTS_FOLDER_COLUMNS = "userSearchResultsFolderColumns";
    public static final String USER_PROPERTY_SEARCH_RESULTS_COLUMN_POSITIONS="searchResultsColumnPositions";
    public static final String USER_PROPERTY_SAVED_SEARCH_QUERIES="userSearchQueries";
    public static final String USER_PROPERTY_SIDEBAR_PANEL_PREFIX="sidebarPanelShow_";
    public static final String USER_PROPERTY_SIDEBAR_VISIBILITY="sidebarVisibility";
    public static final String USER_PROPERTY_BUSINESS_CARD_PREFIX="businessCardShow_";
    public static final String USER_PROPERTY_CALENDAR_FIRST_DAY_OF_WEEK="calendarFirstDayOfWeek";
    public static final String USER_PROPERTY_CALENDAR_WORK_DAY_START="calendarWorkDayStart";
    public static final String USER_PROPERTY_TAG_SORT_ORDER = "tagSortOrder";
	public static final String USER_PROPERTY_TEAMING_LIVE_WHATS_NEW_TYPE = "teaming_live_whats_new_type";
    public static final String USER_PROPERTY_TUTORIAL_PANEL_STATE = "userTutorialPanelState";
    public static final String USER_PROPERTY_UPGRADE_DEFINITIONS = "upgradeDefinitions";
    public static final String USER_PROPERTY_UPGRADE_TEMPLATES = "upgradeTemplates";
    public static final String USER_PROPERTY_UPGRADE_SEARCH_INDEX = "upgradeSearchIndex";
    public static final String USER_PROPERTY_WHATS_NEW_SHOW_SETTING = "whatsNewShowSetting";
    public static final String USER_PROPERTY_EXPANDED_BINDERS_LIST = "expandedBindersList";
    public static final String USER_PROPERTY_DEFAULT_ACTIVITY_STREAM = "defaultActivityStream";
    public static final String USER_PROPERTY_COLLAPSE_SUBTASKS = "collapsedSubtasks";
    public static final String USER_PROPERTY_COLUMN_WIDTHS = "columnWidths";
    public static final String USER_THEME_DEFAULT="default";
	public static final String USER_PROPERTY_TABS="userTabs";
    
    //workflow definition names
    public static final String WORKFLOW_START_STATE = "__start_state";
    public static final String WORKFLOW_END_STATE = "__end_state";
    public static final String WORKFLOW_PARALLEL_THREAD_NAME = "name";
    public static final String WORKFLOW_PARALLEL_THREAD_START_STATE = "startState";
    public static final String WORKFLOW_PARALLEL_THREAD_END_TRANSITION = "transitionState";
    public static final String WORKFLOW_START_WORKFLOW = "startWorkflow";
    public static final String WORKFLOW_START_WORKFLOW_START = "startThis";
    public static final String WORKFLOW_START_WORKFLOW_COPY = "copyThis";
    public static final String WORKFLOW_START_WORKFLOW_NO_START = "startThisNot";
    
    //reserved input tags
    public static final String INPUT_FIELD_POSTING_FROM="__poster";
    public static final String INPUT_FIELD_ENTITY_ATTACHMENTS="ss_attachFile";
    public static final String INPUT_FIELD_GROUP_PRINCIPAL_MEMBERNAME="memberName";
    public static final String INPUT_FIELD_TAGS="_tags";
    public static final String INPUT_FIELD_ORDER_SUFFIX="__order";
	public static final String INPUT_FIELD_FUNCTIONMEMBERSHIPS="org.kablink.teaming.workareaFunctionMemberships";
	public static final String INPUT_FIELD_DEFINITIONS="org.kablink.teaming.definitions";
	public static final String INPUT_FIELD_WORKFLOWASSOCIATIONS="org.kablink.teaming.workflows";

	/********INPUT options parameters********/
	//Passed to processors to force a database lock.  Used to reduce optimistic lock exceptions
	public static final String INPUT_OPTION_FORCE_LOCK="org.kablink.teaming.options.lock";
	//default = Boolean.TRUE if INPUT_OPTION_COPY_NEW_BINDER=TRUE; Preserve old docnumbers when copy to a new folder;
	public static final String INPUT_OPTION_PRESERVE_DOCNUMBER="org.kablink.teaming.options.preserve.docnum";
	//Default=Boolean.FALSE; When deleteing a profile entry, delete the associated workspace and all source/sub binders
	public static final String INPUT_OPTION_DELETE_USER_WORKSPACE="org.kablink.teaming.options.delete.userworkspace";
	//Default=Boolean.TRUE; When deleting a user's personal workspace, should we delete the content of a mirrored folder.
	public static final String INPUT_OPTION_DELETE_MIRRORED_FOLDER_SOURCE = "org.kablink.teaming.options.delete.userworkspace.mirrored.folder.source";
	//migration flags
	public static final String INPUT_OPTION_NO_INDEX="org.kablink.teaming.options.no_indexing";
    public static final String INPUT_OPTION_NO_WORKFLOW="org.kablink.teaming.options.no_workflow_processing";
    public static final String INPUT_OPTION_CREATION_NAME="org.kablink.teaming.options.entity.creation.name";
    public static final String INPUT_OPTION_CREATION_ID="org.kablink.teaming.options.entity.creation.id";
    public static final String INPUT_OPTION_CREATION_DATE="org.kablink.teaming.options.entity.creation.timestamp";
    public static final String INPUT_OPTION_MODIFICATION_NAME="org.kablink.teaming.options.entity.modification.name";
    public static final String INPUT_OPTION_MODIFICATION_ID="org.kablink.teaming.options.entity.modification.id";
    public static final String INPUT_OPTION_MODIFICATION_DATE="org.kablink.teaming.options.entity.modification.timestamp";
    public static final String INPUT_OPTION_FORCE_WORKFLOW_STATE="org.kablink.teaming.options.entity.workflow.state";
    public static final String INPUT_OPTION_NO_MODIFICATION_DATE="org.kablink.teaming.options.no_modificationdate_processing";
    public static final String INPUT_OPTION_VALIDATION_ONLY="org.kablink.teaming.options.validation_only";
    public static final String INPUT_OPTION_REQUIRED_TITLE="org.kablink.teaming.options.required.title";
    public static final String INPUT_OPTION_OWNER_ID="org.kablink.teaming.options.entity.owner.id";
    public static final String INPUT_OPTION_SKIP_PARENT_MODTIME_UPDATE="org.kablink.teaming.options.skip.parent.modtime.update";
    
 	//Reserved data field names
    public static final String FIELD_ID="id";
    public static final String FIELD_ZONE="zoneId";
    public static final String FIELD_INTERNALID="internalId";

    public static final String FIELD_ENTITY_TITLE="title";
    public static final String FIELD_ENTITY_NORMALIZED_TITLE="normalTitle";
    public static final String FIELD_ENTITY_DESCRIPTION="description";
    public static final String FIELD_ENTITY_DESCRIPTION_FORMAT="description.format";
    public static final String FIELD_ENTITY_PARENTBINDER="parentBinder";
    public static final String FIELD_ENTITY_DEFTYPE="definitionType";
    public static final String FIELD_ENTITY_DEFID="entryDefId";
    public static final String FIELD_ENTITY_ICONNAME="iconName";
    public static final String FIELD_ENTITY_CREATE_PRINCIPAL="creation.principal";
    public static final String FIELD_ENTITY_MODIFY_PRINCIPAL="modification.principal";
    public static final String FIELD_ENTITY_DELETED="deleted";
    public static final String FIELD_ENTITY_TEMPLATE_OWNING_BINDER_ID="templateOwningBinderId";

    public static final String FIELD_FILE_ID="_fileID";
    
    public static final String FIELD_APPLICATION_POST_URL="postUrl";
    public static final String FIELD_APPLICATION_TIMEOUT="timeout";
    public static final String FIELD_APPLICATION_TRUSTED="trusted";
    public static final String FIELD_APPLICATION_MAX_IDLE_TIME="maxIdleTime";
    public static final String FIELD_APPLICATION_SAME_ADDR_POLICY="sameAddrPolicy";
    public static final String FIELD_BINDER_ALLOW_DESKTOP_APP_TO_SYNC_DATA = "allowDesktopAppToSyncData";
    public static final String FIELD_BINDER_ALLOW_MOBILE_APPS_TO_SYNC_DATA = "allowMobileAppsToSyncData";
    public static final String FIELD_BINDER_INDEX_CONTENT = "indexContent";
    public static final String FIELD_BINDER_LIBRARY="library";
    public static final String FIELD_BINDER_NAME="name";
    public static final String FIELD_BINDER_UNIQUETITLES="uniqueTitles";
	public static final String FIELD_BINDER_MIRRORED = "mirrored"; 
	public static final String FIELD_BINDER_INHERITTEAMMEMBERS = "teamMembershipInherited"; 
	public static final String FIELD_BINDER_RESOURCE_DRIVER_NAME = "resourceDriverName";
	public static final String FIELD_BINDER_RESOURCE_PATH = "resourcePath"; 
	public static final String FIELD_IS_HOME_DIR = "homeDir";
	public static final String FIELD_WS_SEARCHTITLE="searchTitle";

    public static final String FIELD_USER_FIRSTNAME="firstName";
    public static final String FIELD_USER_MIDDLENAME="middleName";
    public static final String FIELD_USER_LASTNAME="lastName";
    public static final String FIELD_USER_DISPLAYSTYLE="displayStyle";
    public static final String FIELD_USER_EMAIL="emailAddress";
    public static final String FIELD_USER_EMAIL_TEXT="txtEmailAddress";
    public static final String FIELD_USER_EMAIL_MOBILE="mobileEmailAddress";
    public static final String FIELD_USER_EMAIL_BCC="bccEmailAddress";
    public static final String FIELD_USER_LOCALE="locale";
    public static final String FIELD_USER_MINIBLOGID="miniBlogId";
    public static final String FIELD_USER_DISKQUOTA="diskQuota";
    public static final String FIELD_CREATION_PRINCIPAL="creation_principal";
    public static final String FIELD_FILE_LENGTH="fileLength";
    public static final String FIELD_MODIFICATION_DATE="modification_date";
    public static final String FIELD_USER_DISKSPACEUSED="diskSpaceUsed";
    public static final String FIELD_USER_SKYPEID="skypeId";
    public static final String FIELD_USER_TWITTERID="twitterId";
    public static final String FIELD_USER_STATUS="status";
    public static final String FIELD_USER_TIMEZONE="timeZone";
    public static final String FIELD_USER_PASSWORD="password";
    public static final String FIELD_USER_WS_SEARCHTITLE="wsSearchTitle";
    public static final String FIELD_USER_WS_TITLE="wsTitle";
    public static final String FIELD_USER_PRINCIPAL_IDENTITY_INFO="identityInfo";
    public static final String FIELD_USER_EXT_ACCOUNT_STATE="extProvState";
    public static final String FIELD_PRINCIPAL_FOREIGNNAME="foreignName";
    public static final String FIELD_PRINCIPAL_LDAPGUID="ldapGuid";
    public static final String FIELD_PRINCIPAL_OBJECTSID = "objectSid";
    public static final String FIELD_PRINCIPAL_NAME="name";
    public static final String FIELD_PRINCIPAL_SAM_ACCOUNT_NAME = "samAccountName";
    public static final String FIELD_PRINCIPAL_TYPE="type";
    public static final String FIELD_PRINCIPAL_DISABLED="disabled";
    public static final String FIELD_GROUP_DYNAMIC = "dynamic";
    public static final String FIELD_GROUP_LDAP_QUERY = "ldapQuery";
    public static final String FIELD_GROUP_PRINCIPAL_MEMBERS="members";
    public static final String FIELD_PRINCIPAL_THEME="theme";
    public static final String FIELD_GROUP_LDAP_CONTAINER="ldapContainer";
    public static final String TASK_FIELD_ASSIGNMENT="assignment";
    public static final String TASK_FIELD_EVENT="start_end#LogicalEndDate";
    public static final String FIELD_TEMPLATE_TITLE="templateTitle";
    public static final String FIELD_TEMPLATE_DESCRIPTION="templateDescription";

    // families of entries
    public static final String FAMILY_TASK = "task";
    public static final String FAMILY_CALENDAR = "calendar";
    public static final String FAMILY_MILESTONE = "milestone";
    
    //xml tags - some used in GenerateLdapList (as strings) - keep in sync
    public static final String XTAG_ATTRIBUTE_DATABASEID="databaseId";
    public static final String XTAG_ATTRIBUTE_NAME="name";
    public static final String XTAG_ATTRIBUTE_STATE_CAPTION="stateCaption";
    public static final String XTAG_ATTRIBUTE_THREAD_CAPTION="threadCaption";
    public static final String XTAG_ATTRIBUTE_TYPE="type";
    public static final String XTAG_ATTRIBUTE_INTERNALID="internalId";
    public static final String XTAG_ATTRIBUTE_EXTENSION="extension";

    //change log element attributes
    public static final String XTAG_ATTRIBUTE_OPERATION="operation";
    public static final String XTAG_ATTRIBUTE_COMMENT="comment";
    public static final String XTAG_ATTRIBUTE_MODIFIEDBY="modifiedBy";
    public static final String XTAG_ATTRIBUTE_MODIFIEDON="modifiedOn";
    public static final String XTAG_ATTRIBUTE_LOGVERSION="logVersion";
    //types of XML elements
    public static final String XTAG_ELEMENT_TYPE_PROPERTY="property";
    public static final String XTAG_ELEMENT_TYPE_HISTORYSTAMP="historyStamp";
    public static final String XTAG_ELEMENT_TYPE_EVENT="event";
    public static final String XTAG_ELEMENT_TYPE_WORKFLOWSTATE="workflowState";
    public static final String XTAG_ELEMENT_TYPE_WORKFLOWREPONSE="workflowResponse";
    public static final String XTAG_ELEMENT_TYPE_FILEARCHIVE="fileArchive";
    public static final String XTAG_ELEMENT_TYPE_VERSIONARCHIVE="versionArchive";
    public static final String XTAG_ELEMENT_TYPE_FILEATTACHMENT="fileAttachment";
    public static final String XTAG_ELEMENT_TYPE_VERSIONATTACHMENT="versionAttachment";
    public static final String XTAG_ELEMENT_TYPE_ATTRIBUTE="attribute";
    public static final String XTAG_ELEMENT_TYPE_ATTRIBUTE_SET="attribute-set";
    public static final String XTAG_ELEMENT_TYPE_DEFINITION="definition";
    public static final String XTAG_ELEMENT_TYPE_TEMPLATE="template";
    public static final String XTAG_ELEMENT_TYPE_DASHBOARD="dashboard";
    public static final String XTAG_ELEMENT_TYPE_DASHBOARD_COMPONENT="component";
    public static final String XTAG_ELEMENT_TYPE_DASHBOARD_COMPONENT_DATA="data";
    public static final String XTAG_ELEMENT_TYPE_DASHBOARD_LAYOUT="layout";
    public static final String XTAG_ELEMENT_TYPE_FUNCTION_MEMBERSHIP="workAreaFunctionMembership";
    // values of type= attribute for element XTAG_ELEMENT_TYPE_ATTRIBUTE
    public static final String XTAG_TYPE_STRING="string";
    public static final String XTAG_TYPE_DESCRIPTION="description";
    public static final String XTAG_TYPE_COMMASEPARATED="commaSeparated";
    public static final String XTAG_TYPE_BOOLEAN="boolean";
    public static final String XTAG_TYPE_LONG="long";
    public static final String XTAG_TYPE_DATE="date";
    public static final String XTAG_TYPE_SERIALIZED="serialized";
    public static final String XTAG_TYPE_XML="xml";
    public static final String XTAG_TYPE_EVENT="event";
    public static final String XTAG_TYPE_FILE="file";
    //attributes for element XTAG_ELEMENT_TYPE_HISTORYSTAMP 
    public static final String XTAG_HISTORY_BY="author";
    public static final String XTAG_HISTORY_WHEN="when";
    //value of name= attribute on XTAG_ELEMENT_TYPE_HISTORYSTAMP 
    public static final String XTAG_ENTITY_CREATION="created";
    public static final String XTAG_ENTITY_MODIFICATION="modified";
    //values of name= attribute on XTAG_ELEMENT_TYPE_ATTRIBUTE for entities
    public static final String XTAG_ENTITY_TITLE="title";
    public static final String XTAG_ENTITY_DESCRIPTION="description";
    public static final String XTAG_ENTITY_DEFINITION_NAME="entryDefName";
    public static final String XTAG_ENTITY_ICONNAME="iconName";
    public static final String XTAG_ENTITY_ATTACHMENTS="attachments";
    public static final String XTAG_ENTITY_SHARES="shares";
    //values of name= attribute on XTAG_ELEMENT_TYPE_PROPERTY for entities   
    public static final String XTAG_ENTITY_PARENTBINDER="parentBinder";
    public static final String XTAG_ENTITY_DEFINITION="entryDef";
    //values of name= attribute on XTAG_ELEMENT_TYPE_PROPERTY for share   
    public static final String XTAG_ENTITY_SHARE="share";
    public static final String XTAG_ENTITY_SHARE_SHARER_ID="sharerId";
    public static final String XTAG_ENTITY_SHARE_RECIPIENT_TYPE="recipientType";
    public static final String XTAG_ENTITY_SHARE_RECIPIENT_ID="recipientId";
    public static final String XTAG_ENTITY_SHARE_EXPIRATION="expiration";
    public static final String XTAG_ENTITY_SHARE_ROLE="role";
    //values of name= attributes on XTAG_ELEMENT_TYPE_ATTRIBUTE for principals
    public static final String XTAG_PRINCIPAL_FOREIGNNAME="foreignName";
    public static final String XTAG_PRINCIPAL_NAME="name";
    public static final String XTAG_PRINCIPAL_DISABLED="disabled";
    //values of name= attributes on XTAG_ELEMENT_TYPE_ATTRIBUTE for user
    public static final String XTAG_USER_FIRSTNAME="firstName";
    public static final String XTAG_USER_MIDDLENAME="middleName";
    public static final String XTAG_USER_LASTNAME="lastName";
    public static final String XTAG_USER_EMAIL="emailAddress";
    public static final String XTAG_USER_EMAIL_TEXT="txtEmailAddress";
    public static final String XTAG_USER_EMAIL_MOBILE="mobileEmailAddress";
    public static final String XTAG_USER_EMAIL_BCC="bccEmailAddress";
    public static final String XTAG_USER_ZONNAME="zonName";
    public static final String XTAG_USER_ORGANIZATION="organization";
    public static final String XTAG_USER_PHONE="phone";
    public static final String XTAG_USER_MINIBLOGID="miniBlogId";
    public static final String XTAG_USER_SKYPEID="skypeId";
    public static final String XTAG_USER_TWITTERID="twitterId";
    public static final String XTAG_USER_STATUS="status";
    public static final String XTAG_USER_STATUS_DATE="statusDate";
    public static final String XTAG_USER_TIMEZONE="timeZone";
    //values of name= attributes on XTAG_ELEMENT_TYPE_PROPERTY for user
    public static final String XTAG_USER_DISPLAYSTYLE="displayStyle";
    public static final String XTAG_USER_LOCALE="locale";
    public static final String XTAG_USER_PASSWORD="password";
    public static final String XTAG_USER_DIGESTSEED="digestSeed";
    public static final String XTAG_USER_FIRST_LOGINDATE="loginDate";
    //values of name= attributes on XTAG_ELEMENT_TYPE_PROPERTY for groups
    public static final String XTAG_GROUP_MEMBERS="members";
    public static final String XTAG_GROUP_MEMBER_NAME="memberName";
    //values of name= attributes on XTAG_ELEMENT_TYPE_ATTRIBUTE for groups
    public static final String XTAG_APPLICATION_POSTURL="postUrl";
    //values of name= attributes on XTAG_ELEMENT_TYPE_PROPERTY for groups
    public static final String XTAG_APPLICATION_GROUP_MEMBERS="appMembers";

    //values of name= attributes on XTAG_ELEMENT_TYPE_PROPERTY for folderentry
    public static final String XTAG_FOLDERENTRY_DOCNUMBER="docNumber";
    public static final String XTAG_FOLDERENTRY_TOPENTRY="topEntry";
    public static final String XTAG_FOLDERENTRY_PARENTENTRY="parentEntry";
    public static final String XTAG_FOLDERENTRY_POSTEDBY="postedBy";
    //value of name= attributes on properties of XTAG_ELEMENT_TYPE_FILEATTACHMENT 
    public static final String XTAG_FILE_NAME="fileName";
    public static final String XTAG_FILE_LENGTH="fileLength";
    public static final String XTAG_FILE_DESCRIPTION="fileDescription";
    public static final String XTAG_FILE_REPOSITORY="repository";
    public static final String XTAG_FILE_LAST_VERSION="lastVersion";
    public static final String XTAG_FILE_MAJOR_VERSION="majorVersion";
    public static final String XTAG_FILE_MINOR_VERSION="minorVersion";
    public static final String XTAG_FILE_STATUS="fileStatus";
    //value of name= attributes on properties of XTAG_ELEMENT_TYPE_VERSIONATTACHMENT 
    public static final String XTAG_FILE_PARENT="parentAttachment";
    public static final String XTAG_FILE_VERSION_NUMBER="versionNumber";
    public static final String XTAG_FILE_VERSION_NAME="versionName";
    public static final String XTAG_FILE_VERSION_COUNT="versionCount";
    public static final int XTAG_FILE_VERSION_COUNT_MAXIMUM = 20;  //This is how many version to show in the change log
    //values of attributes on element XTAG_ELEMENT_TYPE_FILEARCHIVE
    public static final String XTAG_FILE_ARCHIVE_STORE_NAME="archiveStoreName";
    //values of attributes on element XTAG_ELEMENT_TYPE_VERSIONARCHIVE
    public static final String XTAG_FILE_ARCHIVE_URI="archiveURI";
    //values of name= attribute on XTAG_ELEMENT_TYPE_PROPERTY for element XTAG_ELEMENT_TYPE_FUNCTION_MEMBERSHIP
    public static final String XTAG_WA_FUNCTION="function";
    public static final String XTAG_WA_FUNCTION_NAME="functionName";
    public static final String XTAG_WA_MEMBERS="members";
    public static final String XTAG_WA_MEMBER_NAME="memberName";

    //value of name= attribue on XTAG_ELEMENT_TYPE_ATTRIBUTE for binders
    public static final String XTAG_BINDER_NAME="name";
    public static final String XTAG_TEMPLATE_TITLE="templateTitle";
    public static final String XTAG_TEMPLATE_DESCRIPTION="templateDescription";
    public static final String XTAG_TEMPLATE_HIDDEN="templateHidden";
   //value of name= attribue on XTAG_ELEMENT_TYPE_PROPERTY for binders
    public static final String XTAG_BINDER_UNIQUETITLES="uniqueTitle";
    public static final String XTAG_BINDER_LIBRARY="library";
    public static final String XTAG_BINDER_MIRRORED="mirrored";
    public static final String XTAG_BINDER_INHERITFUNCTIONMEMBERSHIP="inheritFunctionMembership";
    public static final String XTAG_BINDER_INHERITDEFINITIONS="inheritDefinitions";
    public static final String XTAG_BINDER_INHERITTEAMMEMBERS="inheritTeamMembers";
    public static final String XTAG_BINDER_TEAMMEMBERS="teamMembers";
    public static final String XTAG_BINDER_TEAMMEMBER_NAME="teamMemberName";
     
    //value of name= attribute on XTAG_ELEMENT_TYPE_HISTORYSTAMP 
    public static final String XTAG_WF_CHANGE="workflowChange";
    //value of name= attribute XTAG_ELEMENT_TYPE_PROPERTY element XTAG_ELEMENT_TYPE_WORKFLOWSTATE
    public static final String XTAG_WFS_DEFINITION="definition";
    public static final String XTAG_WFS_TIMER="timer";
    public static final String XTAG_WFS_THREAD="thread";
    public static final String XTAG_WFS_PROCESS="process";
     
    //value of name= attribute XTAG_ELEMENT_TYPE_PROPERTY element XTAG_ELEMENT_TYPE_WORKFLOWRESPONSE
    public static final String XTAG_WFR_DEFINITION="definition";
    public static final String XTAG_WFR_RESPONDER="responder";
    public static final String XTAG_WFR_RESPONSEDATE="responseDate";
    public static final String XTAG_WFR_RESPONSE="response";
    
    //Quota related constants
    public static final	int DISKQUOTA_OK = 1;
    public static final	int DISKQUOTA_HIGHWATERMARK_EXCEEDED = 2;
    public static final	int DISKQUOTA_EXCEEDED = 3;
    public static final String PRINCIPAL_TYPE_GROUP = "group";
    public static final String PRINCIPAL_TYPE_USER = "user";
    public final static long MEGABYTES = 1024L * 1024L;
    
    //Mobile related constants
	public static final String USER_PROPERTY_MOBILE_WHATS_NEW_TYPE = "mobile_whats_new_type";
    public static final String MOBILE_WHATS_NEW_VIEW_TEAMS = "teams";
    public static final String MOBILE_WHATS_NEW_VIEW_TRACKED = "tracked";
    public static final String MOBILE_WHATS_NEW_VIEW_TRACKED_PEOPLE = "trackedPeople";
    public static final String MOBILE_WHATS_NEW_VIEW_TRACKED_PLACES = "trackedPlaces";
    public static final String MOBILE_WHATS_NEW_VIEW_FAVORITES = "favorites";
    public static final String MOBILE_WHATS_NEW_VIEW_SITE = "site";
    public static final String MOBILE_WHATS_NEW_VIEW_MICROBLOG = "microblog";
    
    //UI Styles
    public static final String LICENSE_TYPE_KABLINK = "kablink";
    public static final String LICENSE_TYPE_VIBE = "vibe";
    public static final String LICENSE_TYPE_FILR = "filr";
    public static final String LICENSE_TYPE_IPRINT = "iprint";
    
    //License options
    public static final String LICENSE_OPTION_GUEST_ACCESS = "com.novell.teaming.GuestAccess";
    public static final String LICENSE_OPTION_VIBE = "com.novell.teaming.Vibe";
    public static final String LICENSE_OPTION_FILR = "com.novell.teaming.Filr";
    public static final String LICENSE_OPTION_IPRINT = "com.novell.teaming.iPrint";
    public static final String LICENSE_OPTION_EXT_USERS = "com.novell.teaming.ExtUsers";
    
    //License Activity
    public static final int LOGIN_ACTIVITY_YEARS = 1;
	
    public static final String SEARCH_MODE = "searchMode";
    
    //Converter directories
    public static final String CONVERTER_DIR_TEXT = "";		//Content text files go where they used to go (i.e., in cachefilestore)
    public static final String CONVERTER_DIR_HTML = "converted_html_files";
    public static final String CONVERTER_DIR_IMAGE = "converted_images";

    // The following are used as the names of the personal tags added
    // to a shared entity to indicate that the user has it hidden.
    // Must match values in GWT client's TagInfo class
    public final static String	HIDDEN_SHARED_BY_TAG	= "sharedByHidden";
    public final static String	HIDDEN_SHARED_WITH_TAG	= "sharedWithHidden";
}
