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
package org.kablink.teaming.gwt.client.widgets;

import org.kablink.teaming.gwt.client.event.ActivityStreamEnterEvent;
import org.kablink.teaming.gwt.client.event.AdministrationUpgradeCheckEvent;
import org.kablink.teaming.gwt.client.event.ChangeContextEvent;
import org.kablink.teaming.gwt.client.event.ContextChangedEvent;
import org.kablink.teaming.gwt.client.event.EventHelper;
import org.kablink.teaming.gwt.client.event.LoginEvent;
import org.kablink.teaming.gwt.client.event.MastheadHideEvent;
import org.kablink.teaming.gwt.client.event.MastheadShowEvent;
import org.kablink.teaming.gwt.client.event.SizeChangedEvent;
import org.kablink.teaming.gwt.client.event.TeamingEvents;
import org.kablink.teaming.gwt.client.GwtBrandingDataExt;
import org.kablink.teaming.gwt.client.GwtTeaming;
import org.kablink.teaming.gwt.client.RequestInfo;
import org.kablink.teaming.gwt.client.GwtBrandingData;
import org.kablink.teaming.gwt.client.mainmenu.GlobalSearchComposite;
import org.kablink.teaming.gwt.client.rpc.shared.GetBinderBrandingCmd;
import org.kablink.teaming.gwt.client.rpc.shared.GetSiteAdminUrlCmd;
import org.kablink.teaming.gwt.client.rpc.shared.GetSiteBrandingCmd;
import org.kablink.teaming.gwt.client.rpc.shared.GetSystemBinderPermalinkCmd;
import org.kablink.teaming.gwt.client.rpc.shared.GetSystemBinderPermalinkCmd.SystemBinderType;
import org.kablink.teaming.gwt.client.rpc.shared.StringRpcResponseData;
import org.kablink.teaming.gwt.client.rpc.shared.VibeRpcResponse;
import org.kablink.teaming.gwt.client.util.ActivityStreamDataType;
import org.kablink.teaming.gwt.client.util.ActivityStreamInfo;
import org.kablink.teaming.gwt.client.util.ActivityStreamInfo.ActivityStream;
import org.kablink.teaming.gwt.client.util.BinderInfo;
import org.kablink.teaming.gwt.client.util.GwtClientHelper;
import org.kablink.teaming.gwt.client.util.OnSelectBinderInfo;
import org.kablink.teaming.gwt.client.util.OnSelectBinderInfo.Instigator;
import org.kablink.teaming.gwt.client.widgets.FilrActionsCtrl.FilrActionType;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * This widget will display the MastHead
 * 
 * @author jwootton@Novell.com
 */
public class MastHead extends Composite
	implements ClickHandler,
	// Event handlers implemented by this class.
		ContextChangedEvent.Handler,
		MastheadHideEvent.Handler,
		MastheadShowEvent.Handler
{
	private BrandingPanel m_siteBrandingPanel = null;
	private BrandingPanel m_binderBrandingPanel = null;
	private RequestInfo m_requestInfo = null;
	private String m_mastheadBinderId = null;
	private FlowPanel m_mainMastheadPanel = null;
	private HorizontalPanel m_globalActionsPanel = null;
	private FlowPanel m_userNamePanel;
	private InlineLabel m_loginLink = null;
	private InlineLabel m_userName = null;
	private Label m_betaLabel = null;
	private MastheadPopupMenu m_popupMenu = null;
	private UserActionsPopup m_userActionsPopup = null;
	private FilrActionsCtrl m_filrActionsCtrl;
	private String m_personalWorkspacesUrl = null;

	private GwtBrandingData m_siteBrandingData = null;
	private GwtBrandingData m_binderBrandingData = null;
	
	// m_rpcGetSiteBrandingCallback is our callback that gets called when the ajax request to get the site branding data completes.
	private AsyncCallback<VibeRpcResponse> m_rpcGetSiteBrandingCallback = null;

	// m_rpcGetBinderBrandingCallback is our callback that gets called when the ajax request to get the binder branding data completes.
	private AsyncCallback<VibeRpcResponse> m_rpcGetBinderBrandingCallback = null;
	
	// The following defines the TeamingEvents that are handled by
	// this class.  See EventHelper.registerEventHandlers() for how
	// this array is used.
	private TeamingEvents[] m_registeredEvents = new TeamingEvents[] {
		// Context events.
		TeamingEvents.CONTEXT_CHANGED,
		
		// Masthead events.
		TeamingEvents.MASTHEAD_HIDE,
		TeamingEvents.MASTHEAD_SHOW,
	};
		
	
	/**
	 * 
	 */
	public MastHead( RequestInfo requestInfo )
	{
		Scheduler.ScheduledCommand cmd;
		final boolean beta = true;
		
		// Register the events to be handled by this class.
		EventHelper.registerEventHandlers(
			GwtTeaming.getEventBus(),
			m_registeredEvents,
			this );
		
        m_requestInfo = requestInfo;
        m_mastheadBinderId = m_requestInfo.getBinderId();

		m_mainMastheadPanel = new FlowPanel();
		m_mainMastheadPanel.addStyleName( "mastHead" );
		
		// Create a branding panel that will display "site" branding if it exists.
		m_siteBrandingPanel = new BrandingPanel( requestInfo );
		m_siteBrandingPanel.setVisible( false );
		m_mainMastheadPanel.add( m_siteBrandingPanel );
		
		// Create a branding panel that will display the binder branding.
		m_binderBrandingPanel = new BrandingPanel( requestInfo );
		m_binderBrandingPanel.setVisible( false );
		m_mainMastheadPanel.add( m_binderBrandingPanel );
		
		// Create a place for the beta text to go.
		if ( beta && m_requestInfo.isLicenseFilr() == false )
		{
			String productName;
			
			if ( m_requestInfo.isLicenseFilr() )
			{
				productName = GwtTeaming.getMessages().novellFilr();
			}
			else
			{
				productName = m_requestInfo.getProductName();
			}
			
			m_betaLabel = new Label( GwtTeaming.getMessages().betaWithProduct( productName ) );
			m_betaLabel.addStyleName( "mastheadBeta" );
			m_mainMastheadPanel.add( m_betaLabel );
		}
		
		// Are we running Filr?
		if ( m_requestInfo.isLicenseFilr() )
		{
			GlobalSearchComposite globalSearchWidget;
			FlowPanel globalSearchPanel;
			
			// Yes
			// Create a link for the user to click on that will invoke "Users in the system" page
			{
				FlowPanel panel;
				FlowPanel imgPanel;
				Image img;
				
				panel = new FlowPanel();
				panel.addStyleName( "mastheadFilr_PeoplePanel" );
				
				imgPanel = new FlowPanel();
				imgPanel.addStyleName( "mastheadFilr_PeopleImgPanel" );
				img = new Image( GwtTeaming.getImageBundle().userList() );
				img.setTitle( GwtTeaming.getMessages().invokeUserListHint() );
				img.addStyleName( "mastheadFilr_PeopleImg" );
				img.addClickHandler( new ClickHandler()
				{
					@Override
					public void onClick( ClickEvent event )
					{
						Scheduler.ScheduledCommand cmd;
						
						cmd = new Scheduler.ScheduledCommand()
						{
							@Override
							public void execute()
							{
								invokeUsersPage();
							}
						};
						Scheduler.get().scheduleDeferred( cmd );
					}
				} );
				imgPanel.add( img );
				panel.add( imgPanel );
				
				m_mainMastheadPanel.add( panel );
			}
			
			// Create a link for the user to click on that will invoke "what's new"
			{
				FlowPanel panel;
				FlowPanel imgPanel;
				Image img;
				
				panel = new FlowPanel();
				panel.addStyleName( "mastheadFilr_WhatsNewPanel" );
				
				imgPanel = new FlowPanel();
				imgPanel.addStyleName( "mastheadFilr_WhatsNewImgPanel" );
				img = new Image( GwtTeaming.getImageBundle().masthead_WhatsNew() );
				img.setTitle( GwtTeaming.getMessages().whatsNew() );
				img.addStyleName( "mastheadFilr_WhatsNewImg" );
				img.addClickHandler( new ClickHandler()
				{
					@Override
					public void onClick( ClickEvent event )
					{
						Scheduler.ScheduledCommand cmd;
						
						cmd = new Scheduler.ScheduledCommand()
						{
							@Override
							public void execute()
							{
								invokeWhatsNew();
							}
						};
						Scheduler.get().scheduleDeferred( cmd );
					}
				} );
				imgPanel.add( img );
				panel.add( imgPanel );
				
				m_mainMastheadPanel.add( panel );
			}
			
			// Create a Filr Actions panel
			m_filrActionsCtrl = new FilrActionsCtrl();
			m_filrActionsCtrl.addStyleName( "mastheadFilrActionsPanel" );
			m_mainMastheadPanel.add( m_filrActionsCtrl );
			
			// Add the search control
			globalSearchPanel = new FlowPanel();
			globalSearchPanel.addStyleName( "mastheadFilrSearchComposite" );
			globalSearchWidget = new GlobalSearchComposite( false );
			globalSearchPanel.add( globalSearchWidget );
			
			m_mainMastheadPanel.add( globalSearchPanel );
		}
		
		// Create the panel that will hold the global actions such as Administration", "Logout" etc
		{
			m_globalActionsPanel = new HorizontalPanel();
			m_globalActionsPanel.addStyleName( "mastheadGlobalActionsPanel" );
			
			// Create a label that holds the logged-in user's name.
			{
				Image img;
				
				m_userNamePanel = new FlowPanel();
				m_userNamePanel.setStylePrimaryName( "mastheadUserName" );
				
				// Are we dealing with the guest user?
				if ( GwtClientHelper.isCurrentUserGuest() == false )
				{
					// No
					m_userNamePanel.addStyleName( "brandingLink" );
					m_userNamePanel.addDomHandler( this, ClickEvent.getType() );
				}
				else
				{
					// Yes
					m_userNamePanel.addStyleName( "guestLabel" );
				}
				
				m_globalActionsPanel.add( m_userNamePanel );

				String userName = requestInfo.getUserName();
				if ( !GwtClientHelper.hasString( userName ) )
				{
					userName = requestInfo.getUserLoginId();
				}
				m_userName = new InlineLabel( userName );
				m_userName.getElement().setId( "mhUserName" );
				m_userNamePanel.add( m_userName );

				if ( GwtClientHelper.isCurrentUserGuest() == false )
				{
					img = new Image( GwtTeaming.getImageBundle().mastheadActions2() );
					img.getElement().setAttribute( "align", "absmiddle" );
					m_userNamePanel.add( img );
				}
			}
			
			// Add login to the masthead.
			addLoginAction();

			// We only show the popup menu if we are not running Filr.
			if ( m_requestInfo.isLicenseFilr() == false )
			{
				// Create the actions popup menu.
				m_popupMenu = new MastheadPopupMenu( m_mastheadBinderId, m_requestInfo.isUserLoggedIn(), true, true );
				
				// Add an image for the user to click on to pop up the actions menu
				{
					ClickHandler clickHandler;
					final FlowPanel panel;
					Image img;
					
					// Add a separator
					if ( m_requestInfo.isUserLoggedIn() == false )
					{
						InlineLabel separator;
						
						separator = new InlineLabel( "|" );
						separator.addStyleName( "mastheadActionsSeparator" );
						m_globalActionsPanel.add( separator );
					}
					
					panel = new FlowPanel();
					panel.addStyleName( "mastheadMenuPanel" );
					panel.addStyleName( "brandingLink" );
					
					clickHandler = new ClickHandler()
					{
						/**
						 * 
						 */
						@Override
						public void onClick( ClickEvent event )
						{
							m_popupMenu.showRelativeToTarget( panel );
						}
					};
	
					img = new Image( GwtTeaming.getImageBundle().mastheadActions() );
					img.addClickHandler( clickHandler );
					img.getElement().setAttribute( "align", "absmiddle" );
					panel.add( img );
					
					img = new Image( GwtTeaming.getImageBundle().mastheadActions2() );
					img.addClickHandler( clickHandler );
					img.getElement().setAttribute( "align", "absmiddle" );
					panel.add( img );
					
					m_globalActionsPanel.add( panel );
				}
			}
			
			m_mainMastheadPanel.add( m_globalActionsPanel );
		}
		
		// Create the callback that will be used when we issue an ajax call to get the site branding
		m_rpcGetSiteBrandingCallback = new AsyncCallback<VibeRpcResponse>()
		{
			/**
			 * 
			 */
			@Override
			public void onFailure( Throwable t )
			{
				GwtClientHelper.handleGwtRPCFailure(
					t,
					GwtTeaming.getMessages().rpcFailure_GetBranding(),
					"Home Workspace" );
				
				//!!!Window.alert( "get site branding failed" );
			}// end onFailure()
	
			/**
			 * 
			 * @param result
			 */
			@Override
			public void onSuccess( final VibeRpcResponse response )
			{
				Scheduler.ScheduledCommand cmd;
				
				m_siteBrandingData = (GwtBrandingData) response.getResponseData();
				
				cmd = new Scheduler.ScheduledCommand()
				{
					@Override
					public void execute()
					{
						// Update the site branding panel with the branding data
						m_siteBrandingPanel.updateBrandingPanel( m_siteBrandingData );

						// Issue an ajax request to get the binder branding
						getBinderBrandingDataFromServer();
					}
				};
				Scheduler.get().scheduleDeferred( cmd );
			}// end onSuccess()
		};

		// Create the callback that will be used when we issue an ajax call to get the binder branding
		m_rpcGetBinderBrandingCallback = new AsyncCallback<VibeRpcResponse>()
		{
			/**
			 * 
			 */
			@Override
			public void onFailure( Throwable t )
			{
				GwtClientHelper.handleGwtRPCFailure(
					t,
					GwtTeaming.getMessages().rpcFailure_GetBranding(),
					m_mastheadBinderId );
				
				//!!!Window.alert( "get binder branding failed" );
			}// end onFailure()
	
			/**
			 * 
			 * @param result
			 */
			@Override
			public void onSuccess( VibeRpcResponse response )
			{
				Scheduler.ScheduledCommand cmd;
				
				m_binderBrandingData = (GwtBrandingData) response.getResponseData();

				cmd = new Scheduler.ScheduledCommand()
				{
					@Override
					public void execute()
					{
						// Update the binder branding panel with the branding data
						m_binderBrandingPanel.updateBrandingPanel( m_binderBrandingData );
						
						// Display site and binder branding based on the branding rule found in
						// the site branding.
						showBranding();
					}
				};
				Scheduler.get().scheduleDeferred( cmd );
				
			}// end onSuccess()
		};

		// Check to see if there are any upgrade actions that need to be performed.
		checkForUpgradeActions();
		
		// All composites must call initWidget() in their constructors.
		initWidget( m_mainMastheadPanel );

		// Issue an ajax request to get the site branding.
		cmd = new Scheduler.ScheduledCommand()
		{
			@Override
			public void execute()
			{
				getSiteBrandingDataFromServer();
			}
		};
		Scheduler.get().scheduleDeferred( cmd );
	}// end MastHead()


	/**
	 * Adjust the height of the branding panels
	 */
	private void adjustBrandingPanelsHeight()
	{
		final int minHeight;
		Scheduler.ScheduledCommand cmd;

		if ( m_filrActionsCtrl != null )
			minHeight = m_filrActionsCtrl.getOffsetHeight() + 12;
		else
			minHeight = 50;
		
		cmd = new Scheduler.ScheduledCommand()
		{
			@Override
			public void execute()
			{
				Scheduler.ScheduledCommand cmd2;

				if ( m_siteBrandingPanel != null )
				{
					m_siteBrandingPanel.setMinHeight( minHeight );
					m_siteBrandingPanel.adjustBrandingPanelHeight();
				}
				
				cmd2 = new Scheduler.ScheduledCommand()
				{
					@Override
					public void execute()
					{
						if ( m_binderBrandingPanel != null )
						{
							m_binderBrandingPanel.setMinHeight( minHeight );
							m_binderBrandingPanel.adjustBrandingPanelHeight();
						}
					}
				};
				Scheduler.get().scheduleDeferred( cmd2 );
			}
		};
		Scheduler.get().scheduleDeferred( cmd );
	}
	
	/**
	 * Add the "login" action to the global actions part of the masthead.
	 */
	private void addLoginAction()
	{
		Element linkElement;

		// Is the user logged in?
		if ( m_requestInfo.isUserLoggedIn() == false )
		{
			FlowPanel panel;
			
			// Add a separator
			{
				InlineLabel separator;
				
				separator = new InlineLabel( "|" );
				separator.addStyleName( "mastheadActionsSeparator" );
				m_globalActionsPanel.add( separator );
			}

			// No, add the "login" action.
			panel = new FlowPanel();
			panel.addStyleName( "brandingLink" );
			
			m_loginLink = new InlineLabel( GwtTeaming.getMessages().loginHint() );
			m_loginLink.addStyleName( "brandingLink" );
			m_loginLink.addClickHandler( this );
			m_loginLink.setTitle( GwtTeaming.getMessages().loginHint() );
			linkElement = m_loginLink.getElement();
			linkElement.setId( "mhLoginAction" );
			
			panel.add( m_loginLink );
			m_globalActionsPanel.add( panel );
		}
	}
	
	
	/**
	 * Issue an ajax request to see if the user has rights to run the "site administration" page.
	 * If they do we will check to see if there are any upgrade actions that need to be done.
	 */
	private void checkForUpgradeActions()
	{
		AsyncCallback<VibeRpcResponse> rpcCallback;

		rpcCallback = new AsyncCallback<VibeRpcResponse>()
		{
			/**
			 * 
			 */
			@Override
			public void onFailure( Throwable t )
			{
				// Note:  We don't pass a string here such as
				//   rpcFailure_GetSiteAdminUrl() because it would
				//   get displayed for guest, and all other
				//   non-admin users.  Not passing a string here
				//   allows the proper exception handling to occur
				//   but will NOT display an error to the user.
				GwtClientHelper.handleGwtRPCFailure( t );
			}
	
			/**
			 * 
			 * @param result
			 */
			@Override
			public void onSuccess( VibeRpcResponse response )
			{
				String url;
				StringRpcResponseData responseData;
				
				responseData = (StringRpcResponseData) response.getResponseData();
				url = responseData.getStringValue();
				
				// Did we get a url for the "site administration" action?
				if ( url != null && url.length() > 0 )
				{
					Scheduler.ScheduledCommand cmd;
					
					// Yes
					cmd = new Scheduler.ScheduledCommand()
					{
						/**
						 * 
						 */
						@Override
						public void execute()
						{
							// Since the user has administration rights, show them a list of
							// upgrade tasks that still need to be performed.
							// Sent event to check for tasks
							AdministrationUpgradeCheckEvent.fireOne();
						}
					};
					Scheduler.get().scheduleDeferred( cmd );
				}
			}
		};
		
		// Issue an ajax request to get the url for the "site administration" action.
		{
			GetSiteAdminUrlCmd cmd;

			cmd = new GetSiteAdminUrlCmd( getBinderId() );
			GwtClientHelper.executeCommand( cmd, rpcCallback );
		}
	}

	/**
	 * Return the binder id we are working with.
	 */
	public String getBinderId()
	{
		return m_mastheadBinderId;
	}// end getBinderId()
	
	
	/**
	 * Return the branding data we are working with.
	 */
	public GwtBrandingData getBrandingData()
	{
		return m_binderBrandingPanel.getBrandingData();
	}// end GwtBrandingData()
	
	
	/**
	 * Issue an ajax request to get the binder branding data from the server.  Our AsyncCallback
	 * will be called when this request completes.
	 */
	private void getBinderBrandingDataFromServer()
	{
		// Do we have a binder id?
		if ( m_mastheadBinderId != null && m_mastheadBinderId.length() > 0 )
		{
			GetBinderBrandingCmd cmd;
			
			// Yes, Issue an ajax request to get the branding data for the given binder.
			cmd = new GetBinderBrandingCmd( m_mastheadBinderId );
			GwtClientHelper.executeCommand( cmd, m_rpcGetBinderBrandingCallback );
		}
	}// end getBinderBrandingDataFromServer()


	/**
	 * Return the height of the masthead
	 */
	public int getHeight()
	{
		int height;
		
		height = 0;
		if ( m_siteBrandingPanel.isVisible() )
			height += m_siteBrandingPanel.getOffsetHeight();
		
		if ( m_binderBrandingPanel.isVisible() )
			height += m_binderBrandingPanel.getOffsetHeight();
		
		// Are we running Filr?
		if ( m_requestInfo.isLicenseFilr() && m_filrActionsCtrl != null )
		{
			int filrActionsCtrlHeight;
			
			filrActionsCtrlHeight = m_filrActionsCtrl.getOffsetHeight() + 10;
			if ( filrActionsCtrlHeight > height )
				height = filrActionsCtrlHeight;
		}

		return height;
	}// end getHeight()
	
	
	/**
	 * Return the site branding data.
	 */
	public GwtBrandingData getSiteBrandingData()
	{
		return m_siteBrandingData;
	}// end getSiteBrandingData()
	
	
	/**
	 * Issue an ajax request to get the site branding data from the server.  Our AsyncCallback
	 * will be called when this request completes.
	 */
	private void getSiteBrandingDataFromServer()
	{
		GetSiteBrandingCmd cmd;
		
		// Issue an ajax request to get the site branding data.
		cmd = new GetSiteBrandingCmd();
		GwtClientHelper.executeCommand( cmd, m_rpcGetSiteBrandingCallback );
	}
	
	/**
	 * Hide the logout link.  We will do this if we are running in captive mode.
	 */
	public void hideLogoutLink()
	{
		if ( m_userActionsPopup != null )
			m_userActionsPopup.hideLogoutLink();
	}

	/**
	 * 
	 */
	private void invokeUserActionsPopup()
	{
		if ( m_userActionsPopup == null )
		{
			m_userActionsPopup = new UserActionsPopup( m_userName.getText(), true, false );

			// Set the position of the popup
			{
				int left;
				int top;
				
				left = m_userNamePanel.getAbsoluteLeft() - (m_userNamePanel.getOffsetWidth() / 2);
				top = m_userNamePanel.getAbsoluteTop() + m_userNamePanel.getOffsetHeight() + 12;
				m_userActionsPopup.setPopupPosition( left, top );
			}
		}

		m_userActionsPopup.setPopupPositionAndShow( new PopupPanel.PositionCallback()
		{
			@Override
			public void setPosition(int offsetWidth, int offsetHeight)
			{
				int left;
				int top;
				
				// Align the right edge of the popup with the right edge of the user name
				left = m_userNamePanel.getAbsoluteLeft();
				left -= (offsetWidth - m_userNamePanel.getOffsetWidth());
				left -= 20;
				top = m_userNamePanel.getAbsoluteTop() + m_userNamePanel.getOffsetHeight() + 12;
				m_userActionsPopup.setPopupPosition( left, top );
				
				m_userActionsPopup.setPopupPosition( left, top );
			}
		} );
	}
	
	/**
	 * 
	 */
	private void invokeUsersPage()
	{
		// Do we have the url to the "personal workspaces" binder?
		if ( m_personalWorkspacesUrl == null )
		{
			GetSystemBinderPermalinkCmd cmd;
			AsyncCallback<VibeRpcResponse> rpcCallback;

			// No
			rpcCallback = new AsyncCallback<VibeRpcResponse>()
			{
				/**
				 * 
				 */
				@Override
				public void onFailure( Throwable t )
				{
					// Note:  We don't pass a string here such as
					//   rpcFailure_GetSiteAdminUrl() because it would
					//   get displayed for guest, and all other
					//   non-admin users.  Not passing a string here
					//   allows the proper exception handling to occur
					//   but will NOT display an error to the user.
					GwtClientHelper.handleGwtRPCFailure( t );
				}
		
				/**
				 * 
				 * @param result
				 */
				@Override
				public void onSuccess( VibeRpcResponse response )
				{
					String url;
					StringRpcResponseData responseData;
					
					responseData = (StringRpcResponseData) response.getResponseData();
					url = responseData.getStringValue();
					
					// Did we get a url for the "personal workspace"?
					if ( url != null && url.length() > 0 )
					{
						Scheduler.ScheduledCommand cmd;
						
						// Yes
						m_personalWorkspacesUrl = url;
						cmd = new Scheduler.ScheduledCommand()
						{
							/**
							 * 
							 */
							@Override
							public void execute()
							{
								invokeUsersPage();
							}
						};
						Scheduler.get().scheduleDeferred( cmd );
					}
				}
			};
			
			// Issue an ajax request to get the "personal workspaces" url
			cmd = new GetSystemBinderPermalinkCmd( SystemBinderType.PROFILE_ROOT );
			GwtClientHelper.executeCommand( cmd, rpcCallback );
		}
		else
		{
			OnSelectBinderInfo osbInfo;
			
			// Invoke the "Users" page.
			osbInfo = new OnSelectBinderInfo(
										m_personalWorkspacesUrl,
										Instigator.GOTO_CONTENT_URL );
			GwtTeaming.fireEvent( new ChangeContextEvent( osbInfo ) );
		}
	}
	
	/**
	 * Invoke the Whats New page
	 */
	private void invokeWhatsNew()
	{
		ActivityStreamInfo asi;
		ActivityStream as;
		
		FilrActionsCtrl.closeAdminConsole();

		asi = new ActivityStreamInfo();

		// Get the selected action from the FilrActionsCtrl
		if ( m_filrActionsCtrl != null )
		{
			FilrActionType filrActionType;

			filrActionType = m_filrActionsCtrl.getSelectedActionType();

			// Figure out which collection point is selected and invoke "what's new"
			// on that collection point.
			switch ( filrActionType )
			{
			case MY_FILES:
				as = ActivityStream.MY_FILES;
				break;
			
			case NET_FOLDERS:
				as = ActivityStream.NET_FOLDERS;
				break;
				
			case SHARED_BY_ME:
				as = ActivityStream.SHARED_BY_ME;
				break;
				
			case SHARED_WITH_ME:
				as = ActivityStream.SHARED_WITH_ME;
				break;
			
			case UNKNOWN:
			default:
				as = ActivityStream.SHARED_WITH_ME;
				break;
			}
		}
		else
		{
			as = ActivityStream.CURRENT_BINDER;
			//!!!asi.setBinderId( m_mastheadBinderId );
		}

		asi.setActivityStream( as );
		
		GwtTeaming.fireEvent( new ActivityStreamEnterEvent( asi, ActivityStreamDataType.OTHER ) );
	}
	
	/**
	 * This method gets called when the user clicks on something in the branding panel.
	 */
	@Override
	public void onClick( ClickEvent event )
	{
		Scheduler.ScheduledCommand cmd;
		final Widget eventSource;
		
		// Get the widget that was clicked on.
		eventSource = (Widget) event.getSource();

		cmd = new Scheduler.ScheduledCommand()
		{
			@Override
			public void execute() 
			{
				if ( eventSource == m_loginLink )
				{
					LoginEvent.fireOne();
				}
				else if ( eventSource == m_userName || eventSource == m_userNamePanel )
				{
					invokeUserActionsPopup();
				}
			}
		};
		Scheduler.get().scheduleDeferred( cmd );
	}// end onClick()
	
	
	/**
	 * Refresh the binder branding by issuing an ajax request to get the binder branding data . 
	 */
	public void refreshBinderBranding()
	{
		getBinderBrandingDataFromServer();
	}// end refreshBinderBranding()

	
	/**
	 * Refresh the site branding by issuing an ajax request to get the site branding data . 
	 */
	public void refreshSiteBranding()
	{
		getSiteBrandingDataFromServer();
	}// end refreshSiteBranding()

	
	/**
	 * Set the id of the binder the masthead is dealing with.
	 */
	public void setBinderId( String binderId )
	{
		if ( binderId != null && binderId.length() > 0 )
		{
			// Did the binder id change?
			if ( m_mastheadBinderId == null || m_mastheadBinderId.equalsIgnoreCase( binderId ) == false )
			{
				// Yes
				m_mastheadBinderId = binderId;
				
				// Issue an ajax request to get the binder branding data for the given binder.
				getBinderBrandingDataFromServer();
			}
		}
	}// end setBinderId()

	
	/**
	 * Set the font color used in the "global actions" panel and in the FilrActions widget.
	 */
	private void setMastheadFontColor()
	{
		Element element;
		Style style;
		String fontColor;
		GwtBrandingData brandingData;
		
		// If the site branding is visible get the font color from it.  Otherwise, get the font color
		// from the binder branding.
		{
			if ( m_siteBrandingPanel.isVisible() )
				brandingData = m_siteBrandingData;
			else
				brandingData = m_binderBrandingData;

			fontColor = brandingData.getFontColor();
		}
		
		// For the given branding data, set the color of the font used in the "global actions" part of the branding panel
		element = m_globalActionsPanel.getElement();
		style = element.getStyle();
			
		// Do we have a font color?
		if ( fontColor != null && fontColor.length() > 0 )
		{
			// Yes
			// Change the color of the font used to display the user's name.
			style.setColor( fontColor );
			
		}
		else
		{
			// Go back to the font color defined in the style sheet.
			style.clearColor();
		}

		// Set the font color used by the FilrActionsCtrl
		if ( m_filrActionsCtrl != null )
			m_filrActionsCtrl.setFontColor( fontColor );

		if ( m_betaLabel != null )
		{
			element = m_betaLabel.getElement();
			style = element.getStyle();

			// Do we have a font color?
			if ( fontColor != null && fontColor.length() > 0 )
			{
				// Yes
				// Change the color of the font used to display the beta text
				style.setColor( fontColor );
			}
			else
			{
				// Go back to the font color defined in the style sheet.
				style.clearColor();
			}
		}
	}
	
	
	/**
	 * 
	 */
	@Override
	public void setVisible( boolean visible )
	{
		super.setVisible( visible );

		if ( visible == true )
		{
			adjustBrandingPanelsHeight();
		}
	}
	
	
	/**
	 * Show the site and binder branding based on the branding rule found in the site branding.
	 */
	private void showBranding()
	{
		// Do we have any site branding?
		if ( m_siteBrandingData.haveBranding() == false )
		{
			// No, just show the binder branding.
			m_siteBrandingPanel.setVisible( false );
			m_binderBrandingPanel.setVisible( true );
			
		}
		else
		{
			String siteBinderId;
			String binderId;
			
			// We have site branding.
			// Did the site branding and the binder branding come from the same binder?
			siteBinderId = m_siteBrandingData.getBinderId();
			binderId = m_binderBrandingData.getBinderId();
			if ( siteBinderId.equalsIgnoreCase( binderId ) )
			{
				// Yes, just display the binder branding.
				m_siteBrandingPanel.setVisible( false );
				m_binderBrandingPanel.setVisible( true );
			}
			else
			{
				GwtBrandingDataExt.BrandingRule brandingRule;
				
				// Display site and binder branding based on the branding rule found
				// in the site branding.
				brandingRule = m_siteBrandingData.getBrandingRule();
				switch ( brandingRule )
				{
				case DISPLAY_SITE_BRANDING_ONLY:
					m_siteBrandingPanel.setVisible( true );
					m_binderBrandingPanel.setVisible( false );
					break;
					
				case DISPLAY_BOTH_SITE_AND_BINDER_BRANDING:
					m_siteBrandingPanel.setVisible( true );
					
					// Do we have binder branding?
					if ( m_binderBrandingData.haveBranding() )
					{
						// Yes
						m_binderBrandingPanel.setVisible( true );
					}
					else
					{
						// No
						m_binderBrandingPanel.setVisible( false );
					}
					break;
					
				case BINDER_BRANDING_OVERRIDES_SITE_BRANDING:
					// Do we have binder branding?
					if ( m_binderBrandingData.haveBranding() )
					{
						// Yes
						m_siteBrandingPanel.setVisible( false );
						m_binderBrandingPanel.setVisible( true );
					}
					else
					{
						m_siteBrandingPanel.setVisible( true );
						m_binderBrandingPanel.setVisible( false );
					}
					break;
					
				case BRANDING_RULE_UNDEFINED:
					m_siteBrandingPanel.setVisible( false );
					m_binderBrandingPanel.setVisible( true );
					break;
					
				default:
					Window.alert( "Unknown branding rule" );
					break;
				}// end switch()
			}
		}
		
		// Set the font color used in the mast head
		setMastheadFontColor();

		// Adjust the height of the branding panels.
		adjustBrandingPanelsHeight();
	}// end showBranding()
	
	/**
	 * Handles ContextChangedEvent's received by this class.
	 * 
	 * Implements the ContextChangedEvent.Handler.onContextChanged() method.
	 * 
	 * @param event
	 */
	@Override
	public void onContextChanged( final ContextChangedEvent event )
	{
		OnSelectBinderInfo osbInfo = event.getOnSelectBinderInfo();
		if ( GwtClientHelper.validateOSBI( osbInfo, false ))
		{
			BinderInfo bi = osbInfo.getBinderInfo();
			if ( GwtClientHelper.isBinderInfoMyFilesHome( bi ) )
			{
				bi = GwtClientHelper.buildMyFilesBinderInfo();
			}
			setBinderId( bi.getBinderId() );
		}
	}// end onContextChanged()
	
	/**
	 * Handles MastheadHideEvent's received by this class.
	 * 
	 * Implements the MastheadHideEvent.Handler.onMastheadHide() method.
	 * 
	 * @param event
	 */
	@Override
	public void onMastheadHide( MastheadHideEvent event )
	{
		setVisible( false );
		
		SizeChangedEvent.fireOne();
	}// end onMastheadHide()
	
	/**
	 * Handles MastheadShowEvent's received by this class.
	 * 
	 * Implements the MastheadShowEvent.Handler.onMastheadShow() method.
	 * 
	 * @param event
	 */
	@Override
	public void onMastheadShow( MastheadShowEvent event )
	{
		setVisible( true );
		
		SizeChangedEvent.fireOne();
	}// end onMastheadShow()	
}// end MastHead
