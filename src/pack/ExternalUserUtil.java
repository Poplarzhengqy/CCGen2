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
package org.kablink.teaming.extuser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kablink.teaming.InternalException;
import org.kablink.teaming.asmodule.zonecontext.ZoneContextHolder;
import org.kablink.teaming.dao.CoreDao;
import org.kablink.teaming.dao.ProfileDao;
import org.kablink.teaming.dao.util.ShareItemSelectSpec;
import org.kablink.teaming.domain.NoUserByTheNameException;
import org.kablink.teaming.domain.NoWorkspaceByTheNameException;
import org.kablink.teaming.domain.OpenIDProvider;
import org.kablink.teaming.domain.ShareItem;
import org.kablink.teaming.domain.User;
import org.kablink.teaming.module.sharing.SharingModule;
import org.kablink.teaming.module.zone.ZoneModule;
import org.kablink.teaming.spring.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.kablink.teaming.util.SpringContextUtil;
import org.kablink.util.StringUtil;
import org.kablink.util.Validator;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author jong
 *
 */
public class ExternalUserUtil {
	
	private static Log logger = LogFactory.getLog(ExternalUserUtil.class);

	public static final String QUERY_FIELD_NAME_EXTERNAL_USER_ENCODED_TOKEN = "euet";
	
	public static final String SESSION_KEY_FOR_OPENID_PROVIDER_NAME = ExternalUserUtil.class.getSimpleName() + "_openidprovidername";
	
	public static final String OPENID_PROVIDER_NAME_GOOGLE = "google";
	public static final String OPENID_PROVIDER_NAME_YAHOO = "yahoo";
	
	private static final String DELIM = ".";
	
	public static String encodeUserTokenWithNewSeed(User user) {
		user.reseedExtProvSeed();
		updateUser(user);
		return Long.toHexString(user.getId().longValue()) + DELIM + user.computeExtProvHash();
	}
	
	public static Map<String, String> getQueryParamsFromUrl(String url) {   
	    if(url == null)
	    	return new HashMap<String, String>();
		int index = url.indexOf('?');
		if(index < 0)
			return new HashMap<String, String>();
		String query = url.substring(index+1);
		return getQueryParamsFromQueryString(query);
	}  
	
	public static Map<String, String> getQueryParamsFromQueryString(String query) {
	    Map<String, String> map = new HashMap<String, String>();
	    if(query == null)
	    	return map;
	    String[] params = StringUtil.split(query, "&");  
	    for (String param : params) {  
	    	String[] elem = StringUtil.split(param, "=");
	        if(elem.length == 2)
	        	map.put(elem[0], elem[1]);  
	    }  
	    return map;  
	}
	
	public static void handleResponseToConfirmationAfterSuccessfulLocalLogin(String userToken) {
		if(Validator.isNotNull(userToken)) {
			String zoneName = getZoneModule().getZoneNameByVirtualHost(ZoneContextHolder.getServerName());
			Long userId = ExternalUserUtil.getUserId(userToken);
			// Load the user object by the ID value encoded in the token. 
			User user = findExternalUserById(userId, zoneName);
			// If still here, the ID value encoded in the token refers to a valid user account.
			// Validate the token against the current state of the account to make sure that the client didn't forge the token/link.
			validateDigest(user, ExternalUserUtil.getPrivateDigest(userToken));
			// If still here, the digest validation was successful.
			if(User.ExtProvState.credentialed == user.getExtProvState()) {
				// The user is responding to the confirmation previously sent out.
				// Mark the user object as successfully provisioned and confirmed.
				markAsVerified(user);
			}
		}
	}
	
	public static void handleResponseToInvitationOrConfirmation(HttpSession session, String url) 
			throws ExternalUserRespondingToInvitationException, InternalException {
		Map<String,String> queryParams = ExternalUserUtil.getQueryParamsFromUrl(url);
		String token = queryParams.get(ExternalUserUtil.QUERY_FIELD_NAME_EXTERNAL_USER_ENCODED_TOKEN);
		if(Validator.isNotNull(token)) {
			String zoneName = getZoneModule().getZoneNameByVirtualHost(ZoneContextHolder.getServerName());
			Long userId = ExternalUserUtil.getUserId(token);
			// Load the user object by the ID value encoded in the token. 
			User user = findExternalUserById(userId, zoneName);
			// If still here, the ID value encoded in the token refers to a valid user account.
			// Validate the token against the current state of the account to make sure that the client didn't forge the token/link.
			validateDigest(user, ExternalUserUtil.getPrivateDigest(token));
			// If still here, the digest validation was successful.
			if(User.ExtProvState.initial == user.getExtProvState()) {
				// The user is responding to the invitation previously sent out.
				OpenIDProvider openidProvider = ExternalUserUtil.getAllowedOpenIDProviderGivenEmailAddress(user.getName());				
				// Create an exception object to be used as more like a normal status code
				ExternalUserRespondingToInvitationException exc = new ExternalUserRespondingToInvitationException(user, (openidProvider==null)? null:openidProvider.getName(), url);
				// Unfortunately, the spring security framework won't store this exception into the session even though
				// it correctly catches it and triggers redirect to the login page. So we must put it in ourselves.
				session.setAttribute( AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY, exc);
				// Store in the session the original access url so that we can redirect the user to correct entity
				// after successful authentication with OpenID, in the case the user chooses to authenticate via 
				// OpenID rather than going through the self-provisioning steps.
    			session.setAttribute(SavedRequestAwareAuthenticationSuccessHandler.FILR_REDIRECT_AFTER_SUCCESSFUL_LOGIN, url);
				// Throwing this exception is NOT an indication of an error. Rather, this signals
				// GWT layer to proceed to the next step in the normal flow.
				throw exc;
			}
			else if(User.ExtProvState.credentialed == user.getExtProvState()) {
				// The user is responding to the confirmation previously sent out.
				// In this case, in order to verify the user's identity and finalize on the whole
				// self-provisioning process for the user, we need to ask the user to log in using
				// the credential previously supplied by the person. That's how we verify the person
				// and make sure that the person clicking on the confirmation link is the same
				// person that supplied the credential.
				// In this case, there's no need to present OpenID login dialog.
				// Note that at this particular point in time it's possible that the user has
				// already logged in using OpenID and hence has a valid interactive session.
				// Once the user logs in again using local authentication, then the old session
				// will be disposed of and new session will be created.
				
				// Create an exception object to be used as more like a normal status code
				ExternalUserRespondingToVerificationException exc = new ExternalUserRespondingToVerificationException(user, url);
				// Unfortunately, the spring security framework won't store this exception into the session even though
				// it correctly catches it and triggers redirect to the login page. So we must put it in ourselves.
				session.setAttribute( AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY, exc);
				// Store in the session the original access url so that we can redirect the user to correct entity
				// after successful authentication with OpenID, in the case the user chooses to authenticate via 
				// OpenID rather than going through the self-provisioning steps.
    			session.setAttribute(SavedRequestAwareAuthenticationSuccessHandler.FILR_REDIRECT_AFTER_SUCCESSFUL_LOGIN, url);
				// Throwing this exception is NOT an indication of an error. Rather, this signals
				// GWT layer to proceed to the next step in the normal flow.
				throw exc;
			}
		}
	}
	
	public static void markAsCredentialed(User user) {
		user.setExtProvState(User.ExtProvState.credentialed);
		updateUser(user);
	}
	
	public static void markAsVerified(User user) {
		user.getIdentityInfo().setFromLocal(true);
		user.setExtProvState(User.ExtProvState.verified);
		updateUser(user);
	}
	
	public static void markAsBoundToOpenid(User user) {
		user.getIdentityInfo().setFromOpenid(true);
		updateUser(user);
	}
	
	public static String replaceTokenInUrl(String url, String newToken) {
		if(url == null)
			return null;
		Map<String, String> params = getQueryParamsFromUrl(url);
		String token = params.get(QUERY_FIELD_NAME_EXTERNAL_USER_ENCODED_TOKEN);
		if(Validator.isNotNull(token)) {
			// Old token exists in the url. Replace it with new token.
			return url.replace(QUERY_FIELD_NAME_EXTERNAL_USER_ENCODED_TOKEN + "=" + token, QUERY_FIELD_NAME_EXTERNAL_USER_ENCODED_TOKEN + "=" + newToken);
		}
		else {
			// The link doesn't contain token. No replacement possible.
			return url;
		}
	}
	
	public static String removeTokenFromUrl(String url) {
		if(url == null)
			return null;
		Map<String, String> params = getQueryParamsFromUrl(url);
		String token = params.get(QUERY_FIELD_NAME_EXTERNAL_USER_ENCODED_TOKEN);
		if(Validator.isNotNull(token))
			// Replace the token param with something bogus but innocent/unharmful.
			return url.replace(QUERY_FIELD_NAME_EXTERNAL_USER_ENCODED_TOKEN + "=" + token, "1=1");
		else
			return url;
	}
	
	public static boolean shouldPermitAccess(User user) {
		if(user.getIdentityInfo().isInternal())
			return true; // This code only concerns external user
		
		if(user.getIdentityInfo().isFromOpenid())
			return true; // This user has already logged into Filr successfully using OpenID at least once in the past. Keep allow access.
		
		if(user.getExtProvState() == User.ExtProvState.verified)
			return true; // This user has already completed self-provisioning. Keep allow access.
		
		if(user.getExtProvState() == User.ExtProvState.credentialed)
			return true; // This user is in the process of self-provisioning. Keep allow access.
		
		// Check if the user still has at least one unexpired entity shared with him.
		ShareItemSelectSpec spec = new ShareItemSelectSpec();
		spec.setRecipients(user.getId(), null, null);
		spec.excludeExpired();
		List<ShareItem> shareItems = getSharingModule().getShareItems(spec);
		if(shareItems != null && shareItems.size() > 0)
			return true;
		else
			return false;
	}
	
	private static Long getUserId(String encodedUserToken) {
		return Long.parseLong(encodedUserToken.substring(0, encodedUserToken.indexOf(DELIM)), 16);
	}
	
	private static String getPrivateDigest(String encodedUserToken) {
		return encodedUserToken.substring(encodedUserToken.indexOf(DELIM)+1);
	}
	
	private static OpenIDProvider getAllowedOpenIDProviderGivenEmailAddress(String emailAddress) {
    	Long zoneId = getZoneModule().getZoneIdByVirtualHost(ZoneContextHolder.getServerName());
     	
    	List<OpenIDProvider> providers = getCoreDao().findOpenIDProviders(zoneId);
    	
    	if(providers != null) {
    		for(OpenIDProvider provider:providers) {
    			// Since this method is called only when dealing with external user invitation/confirmation,
    			// some inefficiency in the processing is acceptable.
    			if(provider.getEmailRegex() != null && emailAddress.matches(provider.getEmailRegex())) {
    				return provider; // Match found
    			}
    		}
    	}
    	
    	return null;
	}
	
	private static void validateDigest(User user, String digest) {
		if(!user.computeExtProvHash().equals(digest)) {
			logger.warn("User '" + user.getName() + "' supplied invalid digest value");
			throw new UsernameNotFoundException("Invalid link");
		}
	}

	private static User findExternalUserById(Long userId, String zoneName) throws UsernameNotFoundException {
		User user;
		try {
			user = getProfileDao().loadUser(userId, zoneName);
		}
		catch (NoWorkspaceByTheNameException e) {
    		throw new UsernameNotFoundException("No user with id '" + userId + "'", e);
    	} 
		catch (NoUserByTheNameException e) {
    		throw new UsernameNotFoundException("No user with id '" + userId + "'", e);
    	}
		
    	// Only external user can get to this code path.
		if(user.getIdentityInfo().isInternal()) {
			// This shouldn't happen
			logger.error("User id '" + userId + "' represents an internal user");
    		throw new UsernameNotFoundException("No user with id '" + userId + "'");
		}
		else {
			return user;
		}
	}

	private static void updateUser(final User user) {
		getTransactionTemplate().execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(TransactionStatus status) {
				getCoreDao().update(user);
				return null;
			}
		});
	}

	private static ProfileDao getProfileDao() {
		return (ProfileDao) SpringContextUtil.getBean("profileDao");
	}
	
	private static ZoneModule getZoneModule() {
		return (ZoneModule) SpringContextUtil.getBean("zoneModule");
	}
	
	private static CoreDao getCoreDao() {
		return (CoreDao) SpringContextUtil.getBean("coreDao");
	}
	
	private static TransactionTemplate getTransactionTemplate() {
		return (TransactionTemplate) SpringContextUtil.getBean("transactionTemplate");
	}

	private static SharingModule getSharingModule() {
		return (SharingModule) SpringContextUtil.getBean("sharingModule");
	}
	
	public static void main(String[] args) throws Exception {
		long l = 209;
		String hex = Long.toHexString(l);
		System.out.println(hex);
		long l2 = Long.parseLong(hex, 16);
		System.out.println(l2);		
	}
}
