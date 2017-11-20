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
package org.kablink.teaming.web.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.kablink.teaming.ObjectKeys;
import org.kablink.teaming.context.request.RequestContextHolder;
import org.kablink.teaming.dao.CoreDao;
import org.kablink.teaming.domain.Binder;
import org.kablink.teaming.domain.EntityIdentifier;
import org.kablink.teaming.domain.Folder;
import org.kablink.teaming.domain.FolderEntry;
import org.kablink.teaming.domain.NoBinderByTheIdException;
import org.kablink.teaming.domain.User;
import org.kablink.teaming.domain.Workspace;
import org.kablink.teaming.module.admin.AdminModule.AdminOperation;
import org.kablink.teaming.module.binder.BinderModule;
import org.kablink.teaming.module.binder.BinderModule.BinderOperation;
import org.kablink.teaming.module.folder.FolderModule;
import org.kablink.teaming.module.profile.ProfileModule;
import org.kablink.teaming.portletadapter.AdaptedPortletURL;
import org.kablink.teaming.search.SearchUtils;
import org.kablink.teaming.security.AccessControlException;
import org.kablink.teaming.util.AllModulesInjected;
import org.kablink.teaming.util.NLT;
import org.kablink.teaming.util.ReleaseInfo;
import org.kablink.teaming.util.SPropsUtil;
import org.kablink.teaming.util.SpringContextUtil;
import org.kablink.teaming.util.Utils;
import org.kablink.teaming.web.WebKeys;
import org.kablink.teaming.web.util.MiscUtil;
import org.kablink.teaming.web.util.Toolbar;
import org.kablink.teaming.web.util.WebHelper;
import org.kablink.util.BrowserSniffer;
import org.springframework.web.portlet.ModelAndView;

/**
 * Helper methods for the GWT UI code.
 * 
 * @author drfoster@novell.com
 */
public class GwtUIHelper {
	protected static Log m_logger = LogFactory.getLog(GwtUIHelper.class);

	// Used to write a flag to the session cache regarding the state
	// of the GWT UI.
	private final static String GWT_UI_ENABLED_FLAG = "gwtUIEnabled";
	
	// String used to recognize an '&' formatted URL vs. a '/'
	// formatted permalink URL.
	private final static String AMPERSAND_FORMAT_MARKER = "a/do?";
	
	// The keys into the session cache to store the GWT UI top ranked
	// people and places beans.
	public final static String CACHED_TOP_RANKED_PEOPLE_KEY	= "gwt-ui-topRankedPeople";
	public final static String CACHED_TOP_RANKED_PLACES_KEY	= "gwt-ui-topRankedPlaces";
	
	// Values used to communicate the Vibe product that's running.
	//
	// *** WARNING *** WARNING *** WARNING *** WARNING ***
	// ***
	// *** These integer values are used as the numeric representation
	// *** of the enumeration values in VibeProduct.java.
	// ***
	// *** WARNING *** WARNING *** WARNING *** WARNING ***
	public final static int VIBE_PRODUCT_OTHER		= 0;
	public final static int VIBE_PRODUCT_GW			= 1;	// GroupWise integration.
	public final static int VIBE_PRODUCT_KABLINK	= 2;	// Kablink Vibe.
	public final static int VIBE_PRODUCT_NOVELL		= 3;	// Novell  Vibe (outside of GroupWise integration.)

	// Used as a qualifier in a toolbar to indicate the value maps to a
	// GWT UI TeamingEvents value.
	public final static String GWTUI_TEAMING_EVENT = "GwtUI.TeamingEvent";
	
	// When caching a toolbar containing an AdaptedPortletURL, we
	// must also store the URL in string form for the GWT UI to use.
	// If we don't, an NPE is generated when we try to convert it. 
	public final static String URLFIXUP_PATCH = ".urlAsString";

	// The following are used to control how workspace trees are
	// sorted and displayed.  The value meanings are:
	//    0 -> First Middle Last	(uses the Binder's plain  title)
	//    1 -> Last, First Middle	(uses the Binder's search title)
	// Any other value or undefined uses the default of 0.  Note that
	// the traditional UI would equate to 1 for this.
	private static       int     TREE_TITLE_FORMAT         = (-1);
	private static final int     TREE_TITLE_FORMAT_DEFAULT = 0;
	private static final String  TREE_TITLE_FORMAT_KEY     = "wsTree.titleFormat";

	/**
	 * Inner class used by addTrackBinderToToolbar() to assist in
	 * building the toolbar items to support tracking.
	 */
	public static class TrackInfo {
		public String m_event;
		public String m_resourceKey;
		public String m_tbName;
		
		TrackInfo(String tbName, String event, String resourceKey) {
			m_tbName      = tbName;
			m_event       = event;
			m_resourceKey = resourceKey;
		}
	}
	
	/*
	 * Adds an about item to the toolbar.
	 */
	@SuppressWarnings("unchecked")
	private static void addAboutToToolbar(AllModulesInjected bs, RenderRequest request, Map model, Toolbar tb) {
		// Construct an About... toolbar item.
		HashMap qualifiers = new HashMap();
		qualifiers.put("onClick", "ss_showAbout( 'ss_aboutBoxDiv' ); return false;");
		tb.addToolbarMenu(
			"about",
			NLT.get("misc.about"),
			"#",
			qualifiers);
	}
	
	/*
	 * Adds a branding item to the toolbar.
	 */
	@SuppressWarnings("unchecked")
	private static void addBrandingToToolbar(AllModulesInjected bs, RenderRequest request, Map model, Binder binder, Toolbar tb) {
		// If we don't have a binder...
		if (null == binder) {
			// ...it can't be branded.
			return;
		}

		// Does the user have rights to brand this binder?
		if (bs.getBinderModule().testAccess(binder, BinderOperation.modifyBinder)) {
			// Yes!  Add the appropriate toolbar item. 
			String menuKey = "toolbar.menu.brand.";
			if      (binder instanceof Workspace) menuKey += "workspace";
			else if (binder instanceof Folder)    menuKey += "folder";
			else                                  return;
			
			addEventToToolbar(
				tb,
				"branding",
				"EDIT_CURRENT_BINDER_BRANDING",
				NLT.get(menuKey),
				"#",
				null);
		}
	}
	
	/*
	 * Adds a clipboard item to the toolbar.
	 */
	@SuppressWarnings("unchecked")
	private static void addClipboardToToolbar(AllModulesInjected bs, RenderRequest request, Map model, Binder binder, Toolbar tb) {
		// If we're not showing the clipboard...
		Boolean showClipboard = ((Boolean) model.get(WebKeys.TOOLBAR_CLIPBOARD_SHOW));
		if ((null == showClipboard) || (!showClipboard)) {
			// ...bail.
			return;
		}
		
		// If we don't have any contributor IDs...
		String contributorIds = ((String) model.get(WebKeys.TOOLBAR_CLIPBOARD_IDS_AS_JS_STRING));
		if (null == contributorIds) {
			// ...default to an empty list.
			contributorIds = "";
		}

		// Finally, generate the clipboard toolbar menu item.
		Map qualifiers = new HashMap();
		String onClickJS = "ss_muster.showForm('ss_muster_users', [" + contributorIds + "], '" + String.valueOf(binder.getId()) + "');";
		qualifiers.put("onclick", onClickJS);
		tb.addToolbarMenu(
			"clipboard",
			NLT.get("toolbar.menu.clipboard"),
			"#",
			qualifiers);
	}
	
	/*
	 * Adds a configure columns item to the toolbar.
	 */
	@SuppressWarnings("unchecked")
	private static void addConfigureColumnsToToolbar(AllModulesInjected bs, RenderRequest request, Map model, Binder binder, Toolbar tb) {
		// In the current context, can the user configure columns?
		// Note:  The check here follows the same logic as implemented
		// in sidebar_configure_columns.jsp.
		String viewType = ((String) model.get(WebKeys.VIEW_TYPE));
		if (MiscUtil.hasString(viewType)) {
			if (viewType.equalsIgnoreCase("folder") ||
					viewType.equalsIgnoreCase("table") ||
					viewType.equalsIgnoreCase("file")) {
				// Yes!  Construct a Configure Columns toolbar item.
				AdaptedPortletURL url = new AdaptedPortletURL(request, "ss_forum", true);
				url.setParameter(WebKeys.ACTION, "__ajax_request");
				url.setParameter(WebKeys.URL_OPERATION, "configure_folder_columns");
				url.setParameter(WebKeys.URL_BINDER_ID, String.valueOf(binder.getId()));
				HashMap qualifiers = new HashMap();
				qualifiers.put("onClick", "ss_createPopupDiv(this, 'ss_folder_column_menu');return false;");
				tb.addToolbarMenu(
					"configureColumns",
					NLT.get("misc.configureColumns"),
					url.toString(),
					qualifiers);
			}
		}
	}
	
	/*
	 * Adds a send email item to the toolbar.
	 */
	@SuppressWarnings("unchecked")
	private static void addSendEmailToToolbar(AllModulesInjected bs, RenderRequest request, Map model, Binder binder, Toolbar tb) {
		// If we don't have a URL...
		String url = ((String) model.get(WebKeys.TOOLBAR_SENDMAIL_URL));
		if (!(MiscUtil.hasString(url))) {
			// ...we don't add anything to the toolbar.
			return;
		}

		// If we don't have any contributor IDs to send to...
		String[] contributorIds = ((String[]) model.get(WebKeys.TOOLBAR_SENDMAIL_IDS));
		if (null == contributorIds) {
			// ...default to an empty list.
			contributorIds = new String[0];
		}
		int contributorsCount = contributorIds.length;
				
		// If we don't have any contributors...
		Boolean post;
		if (0 == contributorsCount) {
			// ...we don't have to worry about the post flag.
			post = Boolean.FALSE;
		}
		else {
			// Otherwise, if we weren't passed a post flag...
			post = ((Boolean) model.get(WebKeys.TOOLBAR_SENDMAIL_POST));
			if (null == post) {
				// ...default it to true since there were contributors.
				post = Boolean.TRUE;
			}
		}
		
		// Generate the qualifiers to run a URL in a popup window...
		Map qualifiers = new HashMap();
		qualifiers.put("popup",       "true");
		qualifiers.put("popupWidth",  "600");
		qualifiers.put("popupHeight", "600");
		if (post) {
			// ...allowing for any contributors...
			String contributors = "";
			for (int i = 0 ; i < contributorsCount; i += 1) {
				if (0 < i) contributors += ",";
				contributors += String.valueOf(contributorIds[i]);
			}
			qualifiers.put("popup.fromForm",            "true");
			qualifiers.put("popup.hiddenInput.count",   "1");
			qualifiers.put("popup.hiddenInput.0.name",  WebKeys.USER_IDS_TO_ADD);
			qualifiers.put("popup.hiddenInput.0.value", contributors);
		}
		
		// ...and add it as a toolbar menu item.
		tb.addToolbarMenu(
			"sendEmail",
			NLT.get("toolbar.menu.sendMail"),
			url,
			qualifiers);
	}
	
	/*
	 * Adds a share this binder item to the toolbar.
	 */
	@SuppressWarnings("unchecked")
	private static void addShareBinderToToolbar(AllModulesInjected bs, RenderRequest request, Map model, Binder binder, Toolbar tb) {
		// Generate the qualifiers to run a URL in a popup window...
		Map qualifiers = new HashMap();
		qualifiers.put("popup",       "true");
		qualifiers.put("popupWidth",  "550");
		qualifiers.put("popupHeight", "750");
		
		// ...generate the URL to share the binder...
		AdaptedPortletURL url = new AdaptedPortletURL(request, "ss_forum", true);
		url.setParameter(WebKeys.ACTION, "__ajax_relevance");
		url.setParameter(WebKeys.URL_OPERATION, "share_this_binder");
		url.setParameter(WebKeys.URL_BINDER_ID, String.valueOf(binder.getId()));
		
		// ...and add it as a toolbar menu item.
		tb.addToolbarMenu(
			"share",
			NLT.get(
				buildRelevanceKey(
					binder,
					"relevance.shareThis")),
			url.toString(),
			qualifiers);
	}
	
	/*
	 * Adds a teaming event a toolbar.
	 */
	@SuppressWarnings("unchecked")
	private static void addEventToToolbar(Toolbar tb, String menuName, String teamingEvent, String title, String url, Map qualifiers) {
		// Add the teaming event to the qualifiers...
		if (null == qualifiers) {
			qualifiers = new HashMap();
		}
		
		if (MiscUtil.hasString(teamingEvent)) {
			qualifiers.put(GWTUI_TEAMING_EVENT, teamingEvent);
		}
		
		// ...and add it as a toolbar menu item.
		tb.addToolbarMenu(
			menuName,
			((null == title) ? "" : title),
			((null == url)   ? "" : url),
			qualifiers);
	}
	
	private static void addEventToToolbar(Toolbar tb, String menuName, String teamingEvent, String title) {
		addEventToToolbar(tb, menuName, teamingEvent, title, null, null);
	}
	
	/*
	 * Adds a track this binder item to the toolbar.
	 *
	 * Cases handled:
	 *    User workspace:  Tracked     / Person:  Tracked.
	 *    User workspace:  Tracked     / Person:  Not Tracked.
	 *    Workspace:       Tracked.
	 *    Calendar:        Tracked.
	 *    Folder:          Tracked.
	 *    
	 *    User workspace:  Not Tracked / Person:  Tracked.
	 *    User workspace:  Not Tracked / Person:  Not Tracked.
	 *    Workspace:       Not Tracked.
	 *    Calendar:        Not Tracked.
	 *    Folder:          Not Tracked.
	 */
	@SuppressWarnings("unchecked")
	private static void addTrackBinderToToolbar(AllModulesInjected bs, RenderRequest request, Map model, Binder binder, Toolbar tb) {
		// Get a List<TrackInfo> that describes what track operations
		// we'll need in the menu.
		List<TrackInfo> tiList = getTrackInfoList(bs, binder);

		// Scan the TrackInfo objects we generated...
		for (TrackInfo ti:  tiList) {
			// ...adding a toolbar item for each.
			addEventToToolbar(tb, ti.m_tbName, ti.m_event, NLT.get(ti.m_resourceKey));
		}
	}

	/*
	 * Adds a trash item to the toolbar.
	 */
	@SuppressWarnings("unchecked")
	private static void addTrashToToolbar(AllModulesInjected bs, RenderRequest request, Map model, Binder binder, Toolbar tb) {
		// Construct a permalink URL to the trash for the current binder...
		String binderPermalink = PermaLinkUtil.getPermalink(request, binder);
		String trashPermalink = getTrashPermalink(binderPermalink);
		
		// ...and add t to the toolbar.
		addEventToToolbar(
			tb,
			"trash",
			"GOTO_PERMALINK_URL",
			NLT.get("toolbar.menu.trash"),
			trashPermalink,
			null);
	}
	
	/*
	 * Adds a mobile ui item to the toolbar.
	 */
	@SuppressWarnings("unchecked")
	private static void addMobileUiToToolbar(AllModulesInjected bs, RenderRequest request, Map model, Binder binder, Toolbar tb) {
		//Is this a mobile device?
		HttpServletRequest req = WebHelper.getHttpServletRequest(request);
		String userAgents = org.kablink.teaming.util.SPropsUtil.getString("mobile.userAgents", "");
		String tabletUserAgents = org.kablink.teaming.util.SPropsUtil.getString("tablet.userAgentRegexp", "");
		Boolean testForAndroid = org.kablink.teaming.util.SPropsUtil.getBoolean("tablet.useDefaultTestForAndroidTablets", false);
		if (BrowserSniffer.is_mobile(req, userAgents) && !BrowserSniffer.is_tablet(req, tabletUserAgents, testForAndroid)) {
			// Construct a URL to go to the mobile ui
			AdaptedPortletURL url = new AdaptedPortletURL(request, "ss_forum", true, true);
			url.setParameter(WebKeys.ACTION, WebKeys.ACTION_MOBILE_AJAX);
			url.setParameter(WebKeys.URL_OPERATION, WebKeys.OPERATION_MOBILE_SHOW_MOBILE_UI);
			// ...and add t to the toolbar.
			addEventToToolbar(
				tb,
				"mobileUI",
				"GOTO_PERMALINK_URL",
				NLT.get("toolbar.menu.mobileUI"),
				url.toString(),
				null);
		}
	}
	
	/**
	 * Appends a parameter to to a URL.
	 * 
	 * @param urlString
	 * @param pName
	 * @param pValue
	 * 
	 * @return
	 */
	public static String appendUrlParam(String urlString, String pName, String pValue) {
		String param;
		boolean useAmpersand = (0 < urlString.indexOf(AMPERSAND_FORMAT_MARKER));
		if (useAmpersand)
			 param = ("&" + pName + "=" + pValue);
		else param = ("/" + pName + "/" + pValue);
		if (0 > urlString.indexOf(param)) {
			urlString += param;
		}
		return urlString;
	}
	
	/**
	 * Builds the GWT miscellaneous toolbar for a binder.
	 *
	 * @param bs
	 * @param request
	 * @param user
	 * @param binder
	 * @param model
	 * @param qualifiers
	 * @param gwtMiscToolbar
	 */
	@SuppressWarnings("unchecked")
	public static void buildGwtMiscToolbar(AllModulesInjected bs, RenderRequest request, Binder binder, Map model, Toolbar gwtMiscToolbar) {
		// We only add the GWT miscellaneous toolbar items if the GWT
		// UI is active.  Is it?
		if (isGwtUIActive(request)) {
			// Yes!  Add an about toolbar item.
			addAboutToToolbar(bs, request, model, gwtMiscToolbar);
			
			// Do we have a binder and are we running as other than
			// guest?
			if ((null != binder) && (!(isCurrentUserGuest()))) {
				// Yes!  Is the binder we're on other than the profiles
				// container?
				addBrandingToToolbar(bs, request, model, binder, gwtMiscToolbar);
				if (EntityIdentifier.EntityType.profiles != binder.getEntityType()) {
					// Yes!  Add the various binder based toolbar
					// items.
					addClipboardToToolbar(       bs, request, model, binder, gwtMiscToolbar);
					addConfigureColumnsToToolbar(bs, request, model, binder, gwtMiscToolbar);
					addSendEmailToToolbar(       bs, request, model, binder, gwtMiscToolbar);
					addShareBinderToToolbar(     bs, request, model, binder, gwtMiscToolbar);
					addTrackBinderToToolbar(     bs, request, model, binder, gwtMiscToolbar);
					addTrashToToolbar(           bs, request, model, binder, gwtMiscToolbar);
					addMobileUiToToolbar(        bs, request, model, binder, gwtMiscToolbar);
				}
			}
		}
	}

	/**
	 * Generates the appropriate resource key for the given binder.
	 * 
	 * Note:  The logic for which key is based on the logic from
	 *        sidebar_track2.jsp.
	 *        
	 * @param binder
	 * @param keyBase
	 * 
	 * @return
	 */
	public static String buildRelevanceKey(Binder binder, String keyBase) {
		String relevanceKey = keyBase;
		switch (binder.getEntityType()) {
		case workspace:
			relevanceKey += "Workspace";
			break;
			
		case folder:
			String dFamily = "";
			Element familyProperty = ((Element) binder.getDefaultViewDef().getDefinition().getRootElement().selectSingleNode("//properties/property[@name='family']"));
			if (familyProperty != null) {
				dFamily = familyProperty.attributeValue("value", "");
				if (null == dFamily) {
					dFamily = "";
				}
			}
			if (dFamily.equalsIgnoreCase("calendar")) relevanceKey += "Calendar";
			else                                      relevanceKey += "Folder";
		}
		
		return relevanceKey;
	}
	
	/**
	 * When in GWT UI mode, extracts the toolbar beans from the model
	 * and stores them in the session cache.
	 * 
	 * @param pRequest
	 * @param mv
	 */
	public static ModelAndView cacheToolbarBeans(PortletRequest pRequest, ModelAndView mv) {
		return cacheToolbarBeans(WebHelper.getHttpServletRequest(pRequest), mv);
	}

	/**
	 * When in GWT UI mode, extracts the toolbar beans from the model
	 * and stores them in the session cache.
	 * 
	 * @param hRequest
	 * @param mv
	 */
	public static ModelAndView cacheToolbarBeans(HttpServletRequest hRequest, ModelAndView mv) {
		cacheToolbarBeansImpl(hRequest, ((null == mv) ? null : mv.getModel()));
		return mv;
	}

	/*
	 * When in GWT UI mode, extracts the toolbar beans from the model
	 * and stores them in the session cache.
	 */
	@SuppressWarnings("unchecked")
	private static void cacheToolbarBeansImpl(HttpServletRequest hRequest, Map model) {
		// If we don't have an HttpServletRequest...
		if (null == hRequest) {
			// ...we can't cache anything.  Bail.
			return;
		}

		// Clear any previous stuff we may have cached.
		HttpSession hSession = WebHelper.getRequiredSession(hRequest);
		hSession.removeAttribute(CACHED_TOP_RANKED_PEOPLE_KEY);
		hSession.removeAttribute(CACHED_TOP_RANKED_PLACES_KEY);

		// If we're not in GWT UI mode...
		if (!(isGwtUIActive(hRequest))) {
			// ...we don't cache anything.  Bail.
			return;
		}

		// If we don't have a model or the model data is empty...
		if ((null == model) || (0 == model.size())) {
			// ...we can't cache anything.  Bail.
			return;
		}

		// Finally, cache the other stuff we store in the session.
		hSession.setAttribute(CACHED_TOP_RANKED_PEOPLE_KEY, new GwtUISessionData(model.get(WebKeys.FOLDER_ENTRYPEOPLE)));
		hSession.setAttribute(CACHED_TOP_RANKED_PLACES_KEY, new GwtUISessionData(model.get(WebKeys.FOLDER_ENTRYPLACES)));
	}

	/**
	 * Return the 'AdHoc folder' setting from the given user's
	 * properties.
	 * 
	 * @param ami
	 * @param userId
	 * 
	 * @return
	 */
	public static Boolean getAdhocFolderSettingFromUser(AllModulesInjected ami, Long userId) {
		return SearchUtils.getAdhocFolderSettingFromUser(ami, userId);
	}

	/**
	 * Return the 'AdHoc folder' setting from the zone.
	 * 
	 * @param ami,
	 * 
	 * @return
	 */
	public static Boolean getAdhocFolderSettingFromZone(AllModulesInjected ami) {
		return SearchUtils.getAdhocFolderSettingFromZone(ami);
	}
	
	/**
	 * Return the effective 'AdHoc folder' setting from the given user.
	 * We will look in the user's properties first for a value.  If one
	 * is not found we will get the setting from the zone.
	 * 
	 * @param ami
	 * @param user
	 * 
	 * @return
	 */
	public static Boolean getEffectiveAdhocFolderSetting(AllModulesInjected ami, User user) {
		return SearchUtils.getEffectiveAdhocFolderSetting(ami, user);
	}
	
	/**
	 * Returns a Binder from it's ID guarding against any exceptions.
	 * If an exception is caught, null is returned. 
	 * 
	 * @param bm
	 * @param binderId
	 * 
	 * @return
	 */
	public static Binder getBinderSafely(BinderModule bm, Long binderId) {
		Binder reply;
		try {
			reply = bm.getBinder(binderId);
			if ((null != reply) && (reply.isDeleted() || isBinderPreDeleted(reply))) {
				reply = null;
			}
		}
		catch (Exception e) {
			m_logger.debug("GwtUIHelper.getBinderSafely(Binder could not be accessed - EXCEPTION:  " + e.getMessage() + ")");
			reply = null;
		}
		return reply;
	}
	
	public static Binder getBinderSafely(BinderModule bm, String binderId) {
		// Always use the initial form of this method.
		return getBinderSafely(bm, Long.parseLong(binderId));
	}
	
	/**
	 * Returns a Binder from it's ID guarding against all but access
	 * control and no binder exceptions.  If another exception is
	 * caught, null is returned.
	 * 
	 * @param bm
	 * @param binderId
	 * 
	 * @return
	 */
	public static Binder getBinderSafely2(BinderModule bm, Long binderId) throws AccessControlException, NoBinderByTheIdException {
		Binder reply;
		try {
			reply = bm.getBinder(binderId);
			if ((null != reply) && (reply.isDeleted() || isBinderPreDeleted(reply))) {
				reply = null;
			}
		}
		catch (Exception e) {
			if (e instanceof AccessControlException) {
				throw ((AccessControlException) e);
			}
			
			else if (e instanceof NoBinderByTheIdException) {
				throw ((NoBinderByTheIdException) e);
			}
			
			else {
				m_logger.debug("GwtUIHelper.getBinderSafely2(Binder could not be accessed - EXCEPTION:  " + e.getMessage() + ")");
				reply = null;
			}
		}
		return reply;
	}
	
	public static Binder getBinderSafely2(BinderModule bm, String binderId) throws AccessControlException, NoBinderByTheIdException {
		// Always use the initial form of this method.
		return getBinderSafely2(bm, Long.parseLong(binderId));
	}
	
	/**
	 * Returns a FolderEntry from it's ID guarding against any
	 * exceptions.  If an exception is caught, null is returned.
	 * 
	 * @param fm
	 * @param binderId
	 * @param entryId
	 * 
	 * @return
	 */
	public static FolderEntry getEntrySafely(FolderModule fm, Long binderId, Long entryId) {
		FolderEntry reply;
		try {
			reply = fm.getEntry(binderId, entryId);
			if ((null != reply) && (reply.isDeleted() || reply.isPreDeleted())) {
				reply = null;
			}
		}
		catch (Exception e) {
			m_logger.debug("GwtUIHelper.getEntrySafely(FolderEntry could not be accessed - EXCEPTION:  " + e.getMessage() + ")");
			reply = null;
		}
		return reply;
	}
	
	public static FolderEntry getEntrySafely(FolderModule fm, Long entryId) {
		// Always use the initial form of the method.
		return getEntrySafely(fm, null, entryId);
	}
	
	public static FolderEntry getEntrySafely(FolderModule fm, String entryId) {
		// Always use the initial form of the method.
		return getEntrySafely(fm, null, Long.parseLong(entryId));
	}
	
	public static FolderEntry getEntrySafely(FolderModule fm, String binderId, String entryId) {
		// Always use the initial form of the method.
		return getEntrySafely(fm, Long.parseLong(binderId), Long.parseLong(entryId));
	}
		
	/**
	 * Returns a FolderEntry from it's ID guarding against all but
	 * access control and no binder exceptions.  If another exception
	 * is caught, null is returned.
	 * 
	 * @param fm
	 * @param binderId
	 * @param entryId
	 * 
	 * @return
	 */
	public static FolderEntry getEntrySafely2(FolderModule fm, Long binderId, Long entryId) throws AccessControlException, NoBinderByTheIdException {
		FolderEntry reply;
		try {
			reply = fm.getEntry(binderId, entryId);
			if ((null != reply) && (reply.isDeleted() || reply.isPreDeleted())) {
				reply = null;
			}
		}
		catch (Exception e) {
			if (e instanceof AccessControlException) {
				throw ((AccessControlException) e);
			}
			
			else if (e instanceof NoBinderByTheIdException) {
				throw ((NoBinderByTheIdException) e);
			}
			
			else {
				m_logger.debug("GwtUIHelper.getEntrySafely2(FolderEntry could not be accessed - EXCEPTION:  " + e.getMessage() + ")");
				reply = null;
			}
		}
		return reply;
	}
	
	public static FolderEntry getEntrySafely2(FolderModule fm, Long entryId) throws AccessControlException, NoBinderByTheIdException {
		// Always use the initial form of the method.
		return getEntrySafely2(fm, null, entryId);
	}
	
	public static FolderEntry getEntrySafely2(FolderModule fm, String entryId) throws AccessControlException, NoBinderByTheIdException {
		// Always use the initial form of the method.
		return getEntrySafely2(fm, null, Long.parseLong(entryId));
	}
	
	public static FolderEntry getEntrySafely2(FolderModule fm, String binderId, String entryId) throws AccessControlException, NoBinderByTheIdException {
		// Always use the initial form of the method.
		return getEntrySafely2(fm, Long.parseLong(binderId), Long.parseLong(entryId));
	}
		
	/**
	 * Returns an Integer based value from an options Map.  If a value
	 * for key isn't found, defInt is returned.
	 * 
	 * @param options
	 * @param key
	 * @param defInt
	 *  
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static int getOptionInt(Map options, String key, int defInt) {
		Integer obj = ((Integer) options.get(key));
		return ((null == obj) ? defInt : obj.intValue());
	}
	
	/**
	 * Returns a Boolean based value from an options Map.  If a value
	 * for key isn't found, defBool is returned. 
	 * 
	 * @param options
	 * @param key
	 * @param defBool
	 *  
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean getOptionBoolean(Map options, String key, boolean defBool) {
		Boolean obj = ((Boolean) options.get(key));
		return ((null == obj) ? defBool : obj.booleanValue());
	}
	
	/**
	 * Returns a String based value from an options Map.  If a value
	 * for key isn't found, defStr is returned. 
	 * 
	 * @param options
	 * @param key
	 * @param defStr
	 *  
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String getOptionString(Map options, String key, String defStr) {
		String obj = ((String) options.get(key));
		return (((null == obj) || (0 == obj.length())) ? defStr : obj);
	}

	/**
	 * Returns the string representation of the top most workspace ID.
	 * 
	 * If requested, when the true top workspace ID can't be accessed,
	 * the current user's workspace ID is returned.
	 * 
	 * @param bs
	 * @param defaultToUsersWS
	 * 
	 * @return
	 */
	public static String getTopWSIdSafely(AllModulesInjected bs, boolean defaultToUsersWS) {
		Long topWSId;
		
		try {
			topWSId = bs.getWorkspaceModule().getTopWorkspaceId();
		}
		catch (Exception e) {
			topWSId = null;
		}
		if ((null == topWSId) && defaultToUsersWS) {
			User user = RequestContextHolder.getRequestContext().getUser();
			topWSId = bs.getProfileModule().getEntryWorkspaceId(user.getId());
		}
		
		String reply = ((null == topWSId) ? "" : String.valueOf(topWSId.longValue()));
		return reply;
	}
	
	public static String getTopWSIdSafely(AllModulesInjected bs) {
		// Always use the initial form of the method.
		return getTopWSIdSafely(bs, true);	// true -> Default to user's WS if top WS can't be accessed.
	}

	/*
	 * Returns the referrer URL from the request.
	 * 
	 * Note that this method must NEVER return null.
	 */
	private static String getRefererUrl(PortletRequest pRequest) {
		String reply;		
		if (null == pRequest) {
			reply = "";
		}
		else {
			reply = PortletRequestUtils.getStringParameter(pRequest, WebKeys.URL_REFERER_URL, "");
			if (null == reply) {
				reply = "";
			}
		}
		return reply;
	}

	/*
	 * Returns true if the request has a referrer URL and false
	 * otherwise.
	 */
	private static boolean hasRefererUrl(PortletRequest pRequest) {
		return (0 < getRefererUrl(pRequest).length());
	}

	/**
	 * Returns a List<TrackInfo> of how a binder is being tracked.
	 * 
	 * @param bs
	 * @param binder
	 * 
	 * @return
	 */
	public static List<TrackInfo> getTrackInfoList(AllModulesInjected bs, Binder binder) {
		// Construct an ArrayList to hold TrackInfo objects that
		// describe what track operations we'll need in the menu.
		List<TrackInfo> tiList = new ArrayList<TrackInfo>();

		// What do we know about this binder?
		Long binderId = binder.getId();
		boolean binderIsWorkspace     = (binder instanceof Workspace);
		boolean binderIsCalendar      = BinderHelper.isBinderCalendar(bs,  binder);
		boolean binderIsUserWorkspace = BinderHelper.isBinderUserWorkspace(binder);
		boolean binderTracked         = BinderHelper.isBinderTracked(bs, binderId);
		boolean personTracked         = BinderHelper.isPersonTracked(bs, binderId);

		// Build TrackInfo objects for the toolbar items we need to add
		// for tracking (or untracking) whatever we're looking at.
		if (binderTracked) {
			if (personTracked) {
				// User workspace:  Tracked / Person:  Tracked.
				tiList.add(    new TrackInfo("track",       "UNTRACK_CURRENT_BINDER", "relevance.trackThisWorkspaceNot"));				
				tiList.add(    new TrackInfo("trackPerson", "UNTRACK_CURRENT_PERSON", "relevance.trackThisPersonNot"));				
			}
			else {
				if (binderIsUserWorkspace) {
					// User workspace:  Tracked / Person:  Not Tracked.
					tiList.add(new TrackInfo("track",       "UNTRACK_CURRENT_BINDER", "relevance.trackThisWorkspaceNot"));				
					tiList.add(new TrackInfo("trackPerson", "TRACK_CURRENT_BINDER",   "relevance.trackThisPerson"));				
				}
				else if (binderIsWorkspace) {
					// Workspace:  Tracked.
					tiList.add(new TrackInfo("track",       "UNTRACK_CURRENT_BINDER", "relevance.trackThisWorkspaceNot"));				
				}
				else if (binderIsCalendar) {
					// Calendar:  Tracked.
					tiList.add(new TrackInfo("track",       "UNTRACK_CURRENT_BINDER", "relevance.trackThisCalendarNot"));				
				}
				else {
					// Folder:  Tracked.
					tiList.add(new TrackInfo("track",       "UNTRACK_CURRENT_BINDER", "relevance.trackThisFolderNot"));				
				}
			}
		}
		else {
			if (personTracked) {
				// User workspace:  Not Tracked / Person:  Tracked.
				tiList.add(    new TrackInfo("track",       "TRACK_CURRENT_BINDER",   "relevance.trackThisWorkspace"));				
				tiList.add(    new TrackInfo("trackPerson", "UNTRACK_CURRENT_PERSON", "relevance.trackThisPersonNot"));				
			}
			else if (binderIsUserWorkspace) {
				// User workspace:  Not Tracked / Person:  Not Tracked.
				tiList.add(    new TrackInfo("track",       "TRACK_CURRENT_BINDER",   "relevance.trackThisPerson"));				
			}
			else if (binderIsWorkspace) {
				// Workspace:  Not Tracked.
				tiList.add(    new TrackInfo("track",       "TRACK_CURRENT_BINDER",   "relevance.trackThisWorkspace"));				
			}
			else if (binderIsCalendar) {
				// Calendar:  Not Tracked.
				tiList.add(    new TrackInfo("track",       "TRACK_CURRENT_BINDER",   "relevance.trackThisCalendar"));				
			}
			else {
				// Folder:  Not Tracked.
				tiList.add(    new TrackInfo("track",       "TRACK_CURRENT_BINDER",   "relevance.trackThisFolder"));				
			}
		}
		
		return tiList;
	}
	
	/**
	 * Returns s User from it's ID guarding against any exceptions.  If
	 * an exception is caught, null is returned.
	 * 
	 * @param pm
	 * @param userId
	 * 
	 * @return
	 */
	public static User getUserSafely(ProfileModule pm, String userId) {
		return getUserSafely(pm, Long.parseLong(userId));
	}
	
	public static User getUserSafely(ProfileModule pm, Long userId) {
		User reply;
		try {
			reply = ((User) pm.getEntry(userId));
			if ((null != reply) && reply.isDeleted()) {
				reply = null;
			}
		}
		catch (Exception e) {
			m_logger.debug("GwtUIHelper.getUserSafely(User could not be accessed - EXCEPTION:  " + e.getMessage() + ")");
			reply = null;
		}
		return reply;
	}
	
	/**
	 * Returns true if we're supposed to start with an activity stream
	 * on login or false otherwise.
	 * 
	 * @return
	 */
	public static boolean isActivityStreamOnLogin() {
		return (
			(!(Utils.checkIfFilr())) &&									// Not Filr and...
			SPropsUtil.getBoolean("activity.stream.on.login",  true));	// ...default at login is activity streams.
	}
	
	/**
	 * Returns true if a Binder is preDeleted and false otherwise.
	 * 
	 * @param binder
	 *  
	 * @return
	 */
	public static boolean isBinderPreDeleted(Binder binder) {
		// If we have a Binder...
		boolean reply = false;
		if (null != binder) {
			// ...check it if it's a Folder or Workspace.
			if (binder instanceof Folder) {
				reply = ((Folder) binder).isPreDeleted();
			}
			else if (binder instanceof Workspace) {
				reply = ((Workspace) binder).isPreDeleted();
			}
		}
		
		// If we get here, reply is true if the Binder is
		// preDeleted and false otherwise.
		return reply;
	}
	
	/**
	 * Returns true if the current user is the built-in guest user and
	 * false otherwise.
	 * 
	 * @return
	 */
	public static boolean isCurrentUserGuest() {
		User user = RequestContextHolder.getRequestContext().getUser();
		return ObjectKeys.GUEST_USER_INTERNALID.equals(user.getInternalId());
	}

	/**
	 * Returns true if the GWT UI should be active and false otherwise.
	 * 
	 * @param pRequest
	 * 
	 * @return
	 */
	public static boolean isGwtUIActive(PortletRequest pRequest) {
		HttpServletRequest hRequest = WebHelper.getHttpServletRequest(pRequest);
		boolean reply = (null != pRequest);
		if (reply) {
			reply = isGwtUIActive(hRequest);
		}
		return reply;
	}

	/**
	 * Returns true if the GWT UI should be active and false otherwise.
	 * 
	 * @param hRequest
	 * 
	 * @return
	 */
	public static boolean isGwtUIActive(HttpServletRequest hRequest) {
		boolean reply = (null != hRequest);
		if (reply) {
			HttpSession hSession = WebHelper.getRequiredSession(hRequest);
			Object gwtUI = hSession.getAttribute(GWT_UI_ENABLED_FLAG);
			if (null == gwtUI) {
				reply = Boolean.TRUE;
			}
			else {
				reply = ((gwtUI instanceof Boolean) && ((Boolean) gwtUI).booleanValue());
			}
		}
		return reply;

	}

	/**
	 * Checks the session for the session captive (i.e., running under
	 * GroupWise) state.  If a value is stored, it's returned.  If a
	 * value is not stored, any captive flag in the request is used to
	 * generate one and that's stored in the session and returned.
	 * 
	 * @param pRequest
	 * 
	 * @return
	 */
	public static boolean isSessionCaptive(PortletRequest pRequest) {
		// If this is the root request into Vibe...
		if (isVibeRootRequest(pRequest)) {
			// ...we ignore it as we'll process what we need with the
			// ...URL that gets redirected to.
			return false;
		}

		// If we don't have access to the session...
		PortletSession session = WebHelper.getRequiredPortletSession(pRequest);;
		if (null == session) {
			// ...we can't do anything.
			m_logger.error("GwtUIHelper.isSessionCaptive():  Can't access session cache.");
			return false;
		}
				
		// Yes!  Do we have a session captive flag stored?
		Boolean sessionCaptive = ((Boolean) session.getAttribute(WebKeys.SESSION_CAPTIVE));
		if (null == sessionCaptive) {
			// No!  Store a session captive flag based on there
			// being a 'captive=true' setting in the request.
			String captive = PortletRequestUtils.getStringParameter(pRequest, WebKeys.URL_CAPTIVE, "false");
			sessionCaptive = ((MiscUtil.hasString(captive) && "true".equalsIgnoreCase(captive)) ? Boolean.TRUE : Boolean.FALSE);
			if (!(hasRefererUrl(pRequest))) {
				session.setAttribute(WebKeys.SESSION_CAPTIVE, sessionCaptive);
			}
		}
		
		// If we get here, sessionCaptive is true if we're in session
		// captive mode and false otherwise.  Return it.
		return sessionCaptive.booleanValue();
	}

	/**
	 * Returns true if the Vibe UI is in landing page debug mode and false
	 * otherwise.
	 * 
	 * @return
	 */
	public static boolean isVibeDebugLP() {
		return SPropsUtil.getBoolean("ssf.lp.debug.enabled", false);
	}
	
	/**
	 * Returns true if the Vibe UI is in debug mode and false
	 * otherwise.
	 * 
	 * @return
	 */
	public static boolean isVibeUiDebug() {
		return SPropsUtil.getBoolean("ssf.ui.debug.enabled", false);
	}
	
	/*
	 * Returns true if a request is a Vibe root URL or refers to a Vibe
	 * root URL and returns false otherwise.
	 */
	private static boolean isVibeRootRequest(PortletRequest pRequest) {
		// Is the base request a Vibe root URL?
		String urlParam = PortletRequestUtils.getStringParameter(pRequest, WebKeys.URL_NOVL_ROOT_FLAG, "");
		boolean reply = MiscUtil.hasString(urlParam);
		if (!(reply)) {
			urlParam = PortletRequestUtils.getStringParameter(pRequest, WebKeys.URL_VIBE_ROOT_FLAG_DEPRECATED, "");
			reply = MiscUtil.hasString(urlParam);
			if (!(reply)) {
				urlParam = PortletRequestUtils.getStringParameter(pRequest, WebKeys.URL_VIBEONPREM_ROOT_FLAG_DEPRECATED, "");
				reply = MiscUtil.hasString(urlParam);
			}
		}
		if (!(reply)) {
			// No!  Does the request refer to a Vibe root URL?
			urlParam = getRefererUrl(pRequest);
			reply = (0 < urlParam.indexOf(WebKeys.URL_NOVL_ROOT_FLAG));
			if (!reply) {
				reply = (0 < urlParam.indexOf(WebKeys.URL_VIBE_ROOT_FLAG_DEPRECATED));
				if (!reply) {
					reply = (0 < urlParam.indexOf(WebKeys.URL_VIBEONPREM_ROOT_FLAG_DEPRECATED));
				}
			}
		}
		
		// If we get here, reply is true if the request is a Vibe root
		// URL or refers to a Vibe root URL and is false otherwise.
		// Return it.
		return reply;
	}
	
	/**
	 * Takes a Binder permalink and does what's necessary to bring up
	 * the trash on that Binder.
	 * 
	 * @param binderPermalink
	 * 
	 * @return
	 */
	public static String getTrashPermalink(String binderPermalink) {
		return appendUrlParam(binderPermalink, WebKeys.URL_SHOW_TRASH, "true");
	}
	
	/**
	 * Updates stores the current GWT UI active flag in the session
	 * cache.
	 * 
	 * @param pRequest
	 * @param gwtUIActive
	 */
	public static void setGwtUIActive(PortletRequest pRequest, boolean gwtUIActive) {
		HttpServletRequest hRequest = WebHelper.getHttpServletRequest(pRequest);
		if (null != hRequest) {
			setGwtUIActive(hRequest, gwtUIActive);
		}
	}

	/**
	 * Updates stores the current GWT UI active flag in the session
	 * cache.
	 * 
	 * @param hRequest
	 * @param gwtUIActive
	 */
	public static void setGwtUIActive(HttpServletRequest hRequest, boolean gwtUIActive) {
		if (null != hRequest) {
			HttpSession hSession = WebHelper.getRequiredSession(hRequest);
			hSession.setAttribute(GWT_UI_ENABLED_FLAG, new Boolean(gwtUIActive));
		}
	}

	/**
	 * Returns the URL for the GWT UI to use to bring up help.
	 * 
	 * @return
	 */
	public static String getHelpUrl() {
		return MiscUtil.getHelpUrl("user", null, null);
	}

	/**
	 * Based on information in the request, returns the product Vibe is
	 * running as.
	 * 
	 * @param pRequest
	 * 
	 * @return
	 */
	public static int getVibeProduct(PortletRequest pRequest) {
		// If this is the root request into Vibe...
		if (isVibeRootRequest(pRequest)) {
			// ...we ignore it as we'll process what we need with the
			// ...URL that it gets redirected to.
			return VIBE_PRODUCT_OTHER;
		}
		
		// If we don't have access to the session...
		PortletSession session = WebHelper.getRequiredPortletSession(pRequest);;
		if (null == session) {
			// ...we can't do anything.
			m_logger.error("GwtUIHelper.getVibeProduct():  Can't access session cache.");
			return VIBE_PRODUCT_OTHER;
		}
				

		// If there's a product stored in the session cache...
		String sessionProduct = ((String) session.getAttribute(WebKeys.SESSION_PRODUCT));
		if (MiscUtil.hasString(sessionProduct)) {
			// ...simply return it as an integer.
			return Integer.parseInt(sessionProduct);
		}

		// Is there a 'product=...' setting in the request?
		int reply;
		boolean hasRefererUrl = hasRefererUrl(pRequest);
		boolean isKablink = (!(ReleaseInfo.isLicenseRequiredEdition())); 
		String product = PortletRequestUtils.getStringParameter(pRequest, WebKeys.URL_PRODUCT, "");
		if (MiscUtil.hasString(product)) {
			// Yes!  Do we recognize?
			if      (WebKeys.URL_PRODUCT_GW.equalsIgnoreCase(     product) && (!isKablink)) reply = VIBE_PRODUCT_GW;
			else if (WebKeys.URL_PRODUCT_KABLINK.equalsIgnoreCase(product) &&   isKablink ) reply = VIBE_PRODUCT_KABLINK;
			else if (WebKeys.URL_PRODUCT_NOVELL.equalsIgnoreCase( product) && (!isKablink)) reply = VIBE_PRODUCT_NOVELL;
			else                                                                            reply = VIBE_PRODUCT_OTHER;			
			if (VIBE_PRODUCT_OTHER != reply) {
				// Yes!  If this URL isn't going someplace else...
				if (!hasRefererUrl) {
					// ...store the product in the session cache...
					session.setAttribute(WebKeys.SESSION_PRODUCT, String.valueOf(reply));
					
					// ...and use it to determine the the captive state
					// ...and store that in the session cache.
					Boolean sessionCaptive = Boolean.valueOf(VIBE_PRODUCT_GW == reply);
					session.setAttribute(WebKeys.SESSION_CAPTIVE, sessionCaptive);
				}
				
				// If we get here, reply refers to the Vibe product
				// that we're running as.  Return it.
				return reply;
			}
		}

		// If we get here, there wasn't a 'product=...' setting in the
		// request or we didn't recognize it.  Use other factors to
		// determine the product.
		if      ((!isKablink) && isSessionCaptive(pRequest)) reply = VIBE_PRODUCT_GW;
		else if ( !isKablink)                                reply = VIBE_PRODUCT_NOVELL;
		else if (  isKablink)                                reply = VIBE_PRODUCT_KABLINK;
		else                                                 reply = VIBE_PRODUCT_OTHER;

		// If we have a known product and this URL isn't going
		// someplace else...
		if ((VIBE_PRODUCT_OTHER != reply) && (!hasRefererUrl)) {
			// ...store the product in the session cache.
			session.setAttribute(WebKeys.SESSION_PRODUCT, String.valueOf(reply));			
		}
		
		// If we get here, reply refers to the Vibe product that we're
		// running as.  Return it.
		return reply;
	}
	
	/**
	 * Save the 'AdHoc folder' setting.  If userId is not null saves
	 * the value in the user's properties.  Otherwise, saves the
	 * setting in the zone.
	 * 
	 * @param ami
	 * @param userId
	 * @param allow
	 * 
	 * @return
	 */
	public static Boolean saveAdhocFolderSetting(AllModulesInjected ami, Long userId, Boolean allow) {
		// Are we dealing with a user?
		if (null != userId) {
			// Yes!  Save the setting to their properties.
			ami.getProfileModule().setUserProperty(
				userId,
				ObjectKeys.USER_PROPERTY_ALLOW_ADHOC_FOLDERS,
				((null == allow) ?
					null         :				//     null -> Remove the setting and revert to the zone's setting.
					String.valueOf(allow)));	// non-null -> Specific value to set.
		}
		else {
			// No, we aren't running with a user!  Save as a zone
			// setting.
			ami.getAdminModule().setAdHocFoldersEnabled(
				((null == allow) ?
					Boolean.FALSE :	//     null -> Default to false.
					allow));		// non-null -> Store value directly.
		}
		
		return Boolean.TRUE;
	}
	
	/**
	 * Saves the 'AdHoc folder' settings for multiple users.
	 * 
	 * @param bs
	 * @param userIds
	 * @param allow
	 * 
	 * @return
	 */
	public static Boolean saveMultipleAdHocFolderSettings(AllModulesInjected bs, List<Long> userIds, Boolean allow) {
		// Do we have any user IDs to save from?
		if (MiscUtil.hasItems(userIds)) {
			// Yes!  Scan them...
			for (Long userId:  userIds) {
				// ...saving the allow flag for each.
				saveAdhocFolderSetting(bs, userId, allow);
			}
		}
		
		return Boolean.TRUE;
	}
	
	/**
	 * Sets the common GWT RequestInfo parameters required by the
	 * definition of m_requestInfo in GwtMainPage.jsp.
	 * 
	 * Items included are:
	 * - vibeUIDebug
	 * - vibeLPDebug
	 * - vibeProduct
	 * - sessionCaptive
	 * - isNovellTeaming
	 * - isFilr
	 * - isFilrAndVibe
	 * - isTinyMCECapable
	 * - tinyMCELang
	 * - productName
	 * - ss_helpUrl
	 * - topWSId
	 * - showWhatsNew
	 * - showCollection
	 * - isFormLoginAllowed
	 * 
	 * @param request
	 * @param bs
	 * @param model
	 */
	public static void setCommonRequestInfoData(PortletRequest request, AllModulesInjected bs, Map<String, Object> model) {
		// Put out the flag indicating whether the UI should be in
		// debug mode (i.e., perform extra checking, display messages,
		// ...)
		model.put(WebKeys.VIBE_UI_DEBUG, isVibeUiDebug());
		
		// Put out the flag indicating whether the landing page is in debug mode.
		model.put(WebKeys.VIBE_LP_DEBUG, isVibeDebugLP());
		
		// Put out the flag indicating which product we're running as.
		// Note that we do this first as it has the side affect of
		// setting the session captive flag products that require it.
		int vp = getVibeProduct(request);
		model.put(WebKeys.VIBE_PRODUCT, String.valueOf(vp));

		// Put out the session captive flag.
		model.put(WebKeys.SESSION_CAPTIVE, String.valueOf(isSessionCaptive(request)));
		
		// Put out the flag that tells us if we are running Novell or
		// Kablink Vibe.
		String isNovellTeaming = Boolean.toString(ReleaseInfo.isLicenseRequiredEdition());
		model.put("isNovellTeaming", isNovellTeaming);
		
		// Put out the flag that tells us if we are running Filr.
		boolean isLicenseFilr = Utils.checkIfFilr();
		model.put("isLicenseFilr", Boolean.toString(isLicenseFilr));
		
		// Put out the flag that tells us if we are running Filr and Vibe.
		boolean isLicenseFilrAndVibe = Utils.checkIfFilrAndVibe();
		model.put("isLicenseFilrAndVibe", Boolean.toString(isLicenseFilrAndVibe));
		
		// Put out the flag that tells us if we are running the Vibe UI (which also includes Kablink).
		boolean isLicenseVibe = (Utils.checkIfVibe() || Utils.checkIfKablink());
		model.put("isLicenseVibe", Boolean.toString(isLicenseVibe));
		
		// Put out the flag indicating if the user is a site
		// administrator.
		boolean isSiteAdmin = bs.getAdminModule().testAccess(AdminOperation.manageFunction);
		model.put("isSiteAdmin", Boolean.toString(isSiteAdmin));
		
		// Put out the flag that tells us if the tinyMCE editor will
		// work on the device we are running on.  Get the list of user
		// agents that the tinyMCE editor won't run on.
		HttpServletRequest hRequest = WebHelper.getHttpServletRequest(request);
		String unsupportedUserAgents = SPropsUtil.getString("TinyMCE.notSupportedUserAgents", "");
		
		// See if the tinyMCE editor is capable of running on the current device.
		boolean isCapable = BrowserSniffer.is_TinyMCECapable(hRequest, unsupportedUserAgents);
		model.put("isTinyMCECapable", Boolean.toString(isCapable));
		
		// Add the language code the tinyMCE editor will use.  It is different from the
		// language is running in in the following way.  If the user is running
		// Traditional Chinese the language code will be "tw".  If the user is running
		// Simplified Chinese the language code will be "zh".
		model.put("tinyMCELang", getTinyMCELanguage());

		// Put out the name of the product (Novell or Kablink Vibe)
		// that's running.
		String productName = ReleaseInfo.getName();
		model.put("productName", productName);
			
		// Put out the main help URL for Vibe.
		model.put(WebKeys.URL_HELPURL, getHelpUrl());

		// Put out the ID of the top Vibe workspace.
		String topWSId = getTopWSIdSafely(bs);
		model.put("topWSId", topWSId);
		
		// Put out a true/false indicator as to the state of the
		// activity streams based user interface.
		boolean showWhatsNew;
		if (isLicenseFilr) {
			showWhatsNew = false;
			
			// Does the user have rights to see other users?
			{
				Binder binder;
				boolean canSeeOtherUsers;
				CoreDao coreDao;
				
				coreDao = (CoreDao) SpringContextUtil.getBean( "coreDao" );
				binder = coreDao.loadReservedBinder(
												ObjectKeys.PROFILE_ROOT_INTERNALID,
												RequestContextHolder.getRequestContext().getZoneId() );
				canSeeOtherUsers = bs.getBinderModule().testAccess( binder, BinderOperation.readEntries );
				model.put( "canSeeOtherUsers", String.valueOf( canSeeOtherUsers ) );
			}
		}
		else {
			String showWhatsNewS = PortletRequestUtils.getStringParameter(request, WebKeys.URL_ACTIVITY_STREAMS_SHOW_SITE_WIDE, "");
			showWhatsNew         = (MiscUtil.hasString(showWhatsNewS) && showWhatsNewS.equals("1"));
		}
		model.put(WebKeys.URL_ACTIVITY_STREAMS_SHOW_SITE_WIDE, String.valueOf(showWhatsNew));

		// If there's a show collection setting...
		String showCollection = PortletRequestUtils.getStringParameter(request, WebKeys.URL_SHOW_COLLECTION, "");
		if (MiscUtil.hasString(showCollection)) {
			// ...put that out.
			model.put(WebKeys.URL_SHOW_COLLECTION, showCollection);
		}

		// Put out the flag indicating whether login is allowed via our standard login dialog.
		// Logging in via our standard login dialog will be disabled if we are running behind
		// a single-sign on product such as NAM.
		Boolean loginDisallowed;
		loginDisallowed = SPropsUtil.getBoolean("form.login.auth.disallowed", false);
		model.put(WebKeys.IS_FORM_LOGIN_ALLOWED, (!loginDisallowed));
		
		// Add the id of the "all external users" group.
		{
			Long id;
			
			id = Utils.getAllExtUsersGroupId();
			if ( id != null )
				model.put( "allExternalUsersGroupId", id.toString() );
		}
		
		// Add the id of the "all users" or "all internal users" group.
		{
			Long id;
			
			id = Utils.getAllUsersGroupId();
			if ( id != null )
				model.put( "allInternalUsersGroupId", id.toString() );
		}
		
		// Add the id of the "guest" user.
		{
			Long id;
			
			id = Utils.getGuestId( bs );
			if ( id != null )
				model.put( "guestId", id.toString() );
		}

		// Add a flag that will allow the admin to not show the "show people" link in the mast head
		{
			Boolean allowShowPeople;
		
			allowShowPeople = SPropsUtil.getBoolean( "allow.show.people", true );
			model.put( "allowShowPeople", allowShowPeople );
		}
	}
	
	/**
	 * Return the language the tinyMCE editor should use.  The language is different from the
	 * language the user is running in in the following way.  If the user is running
	 * Traditional Chinese the language code will be "tw".  If the user is running
	 * Simplified Chinese the language code will be "zh".
	 */
	public static String getTinyMCELanguage() {
		User user = RequestContextHolder.getRequestContext().getUser();
		String langCode = user.getLocale().getLanguage();
		String country = user.getLocale().getCountry();
		if ((null != country) && country.equalsIgnoreCase("tw")) {
			langCode = "tw";
		}
		else if ((null != country) && country.equalsIgnoreCase("cn")) {
			langCode = "zh";
		}
		return langCode;
	}
	
	/**
	 * Given a binder, returns the string to display for it in a
	 * workspace tree.
	 * 
	 * @param binder
	 * 
	 * @return
	 */
	public static String getTreeBinderTitle(Binder binder) {
		String reply;
		if (useSearchTitles())
		     reply = binder.getSearchTitle();
		else reply = binder.getTitle();
		return reply;
	}
	
	/**
	 * Returns the bucket size to use when displaying binders in
	 * buckets in the workspace trees.
	 * 
	 * @return
	 */
	public static boolean useSearchTitles() {
		// If we haven't read which format to display workspace tree
		// titles in yet...
		if ((-1) == TREE_TITLE_FORMAT) {
			// ...read it now.
			TREE_TITLE_FORMAT = SPropsUtil.getInt(
				TREE_TITLE_FORMAT_KEY,
				TREE_TITLE_FORMAT_DEFAULT);

			// If what we read is out of range, use the default.
			switch (TREE_TITLE_FORMAT) {
			case 0:
			case 1:                                                  break;
			default:  TREE_TITLE_FORMAT = TREE_TITLE_FORMAT_DEFAULT; break;
			}
		}
		
		// Return true if we should use search titles and false
		// otherwise.
		return (1 == TREE_TITLE_FORMAT);
	}
}
