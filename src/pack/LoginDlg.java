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
package org.kablink.teaming.gwt.client.widgets;

import java.util.ArrayList;

import org.kablink.teaming.gwt.client.GwtBrandingData;
import org.kablink.teaming.gwt.client.GwtLoginInfo;
import org.kablink.teaming.gwt.client.GwtMainPage;
import org.kablink.teaming.gwt.client.GwtOpenIDAuthenticationProvider;
import org.kablink.teaming.gwt.client.GwtSelfRegistrationInfo;
import org.kablink.teaming.gwt.client.GwtTeaming;
import org.kablink.teaming.gwt.client.GwtTeamingMessages;
import org.kablink.teaming.gwt.client.rpc.shared.BooleanRpcResponseData;
import org.kablink.teaming.gwt.client.rpc.shared.CompleteExternalUserSelfRegistrationCmd;
import org.kablink.teaming.gwt.client.rpc.shared.GetLoginInfoCmd;
import org.kablink.teaming.gwt.client.rpc.shared.VibeRpcResponse;
import org.kablink.teaming.gwt.client.service.GwtRpcServiceAsync;
import org.kablink.teaming.gwt.client.util.GwtClientHelper;
import org.kablink.teaming.gwt.client.util.HttpRequestInfo;
import org.kablink.teaming.gwt.client.widgets.DlgBox;
import org.kablink.teaming.gwt.client.widgets.ForgottenPwdDlg.ForgottenPwdDlgClient;
import org.kablink.teaming.gwt.client.widgets.ModifyNetFolderDlg.ModifyNetFolderDlgClient;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;


/**
 * 
 * @author jwootton
 *
 */
@SuppressWarnings("unused")
public class LoginDlg extends DlgBox
{
	private FlowPanel m_mainPanel = null;
	private FormPanel m_formPanel = null;
	private FlowPanel m_hiddenInputPanel = null;
	private FlowPanel m_headerPanel = null;
	private String m_caption = null;
	private Element m_userIdLabelElement = null;
	private TextBox m_userIdTxtBox = null;
	private Element m_pwdLabelElement = null;
	private PasswordTextBox m_pwdTxtBox = null;
	private Button m_okBtn;
	private Button m_cancelBtn = null;
	private Label m_loginFailedMsg = null;
	private Label m_authenticatingMsg = null;
	private InlineLabel m_forgotPwdLink = null;
	private InlineLabel m_selfRegLink = null;
	private CheckBox m_useOpenIdCkbox = null;
	private FlowPanel m_openIdProvidersPanel = null;
	private Hidden m_openIdProviderInput = null;
	private TextBox m_firstNameTxtBox;
	private TextBox m_lastNameTxtBox;
	private PasswordTextBox m_pwd1TxtBox;
	private PasswordTextBox m_pwd2TxtBox;
	private FlowPanel m_selfRegPanel;
	private Button m_registerBtn;
	private ForgottenPwdDlg m_forgottenPwdDlg = null;

	private String m_loginUrl = null;
	private String m_springSecurityRedirect = null;	// This values tells Teaming what url to go to after the user authenticates.
	private GwtSelfRegistrationInfo m_selfRegInfo = null;
	private boolean m_requestedLoginInfo = false;
	private LoginStatus m_loginStatus;
	private String m_openIdProviderUrl;
	private boolean m_useOpenIdAuth = false;
	private boolean m_initialized = false;

	/**
	 * 
	 */
	public enum LoginStatus
	{
		AuthenticationFailed,
		RegistrationRequired,
		PromptForLogin
	}
	
	/**
	 * 
	 */
	private class LoginFormPanel extends FormPanel
	{
		/**
		 * 
		 */
		public LoginFormPanel( Element formElement )
		{
			super( formElement );
		}
	}

	/**
	 * 
	 */
	private class OpenIDAuthProviderImg extends Image
	{
		private GwtOpenIDAuthenticationProvider m_provider;
		
		/**
		 * 
		 */
		public OpenIDAuthProviderImg( GwtOpenIDAuthenticationProvider provider )
		{
			super();
			
			ImageResource imgResource;
			String title;
			
			m_provider = provider;
			if ( provider != null )
			{
				switch ( provider.getType() )
				{
				case AOL:
					imgResource = GwtTeaming.getImageBundle().openIdAuthProvider_aol();
					title = GwtTeaming.getMessages().loginDlg_AuthProviderAol();
					break;
				
				case GOOGLE:
					imgResource = GwtTeaming.getImageBundle().openIdAuthProvider_google();
					title = GwtTeaming.getMessages().loginDlg_AuthProviderGoogle();
					break;
				
				case MYOPENID:
					imgResource = GwtTeaming.getImageBundle().openIdAuthProvider_myopenid();
					title = GwtTeaming.getMessages().loginDlg_AuthProviderMyOpenId();
					break;
					
				case VERISIGN:
					imgResource = GwtTeaming.getImageBundle().openIdAuthProvider_verisign();
					title = GwtTeaming.getMessages().loginDlg_AuthProviderVerisign();
					break;
					
				case YAHOO:
					imgResource = GwtTeaming.getImageBundle().openIdAuthProvider_yahoo();
					title = GwtTeaming.getMessages().loginDlg_AuthProviderYahoo();
					break;
					
				case UNKNOWN:
				default:
					title = GwtTeaming.getMessages().loginDlg_AuthProviderUnknown();
					imgResource = GwtTeaming.getImageBundle().openIdAuthProvider_unknown();
					break;
				}
				
				setResource( imgResource );
				setTitle( title );
			}
		}
		
		/**
		 * 
		 */
		public GwtOpenIDAuthenticationProvider getProvider()
		{
			return m_provider;
		}
	}
	
	/*
	 * Class constructor.
	 * 
	 * Note that the class constructor is private to facilitate code
	 * splitting.  All instantiations of this object must be done
	 * through its createAsync().
	 */
	private LoginDlg(
		boolean autoHide,
		boolean modal,
		int xPos,
		int yPos,
		Object properties,
		String loginUrl,	// The url we should use when we post the request to log in.
		String springSecurityRedirect )
	{
		super( autoHide, modal, xPos, yPos );
	
		String headerText;

		m_loginUrl = loginUrl;
		m_springSecurityRedirect = springSecurityRedirect;
		
		// Create the header, content and footer of this dialog box.
		if ( GwtTeaming.m_requestInfo.isLicenseFilr() )
			headerText = GwtTeaming.getMessages().loginDlgNovellFilrHeader();
		else if ( GwtTeaming.m_requestInfo.isNovellTeaming() )
			headerText = GwtTeaming.getMessages().loginDlgNovellHeader();
		else
			headerText = GwtTeaming.getMessages().loginDlgKablinkHeader();
		
		createAllDlgContent( headerText, null, null, properties ); 
	}// end LoginDlg()
	

	/**
	 * 
	 */
	private void debugAlert( String msg )
	{
		//Window.alert( msg );
	}
	
	/**
	 * Clear all the content from the dialog and start fresh.
	 */
	public void clearContent()
	{
	}// end clearContent()
	
	
	/**
	 * Create all the controls that make up the dialog box.
	 */
	@Override
	public Panel createContent( Object props )
	{
		// Hide the close image in the upper-right-hand corner
		hideCloseImg();
		
		m_mainPanel = new FlowPanel();
		
		// The appropriate ui will be created by showDlg()
		
		init( props );

		return m_mainPanel;
	}// end createContent()
	
	
	/*
	 * Override the createFooter() method so we can control what buttons are in the footer.
	 */
	@Override
	public FlowPanel createFooter()
	{
		return null;
	}// end createFooter()

	/**
	 * 
	 */
	private void createFormPanel()
	{
		Element formElement;

		if ( m_formPanel != null )
			return;
		
		// Get the <form ...> element that was created by GwtMainPage.jsp
		formElement = Document.get().getElementById( "loginFormId" );
		m_formPanel = new LoginFormPanel( formElement );
		m_formPanel.setVisible( true );
		
		m_formPanel.addSubmitHandler( new FormPanel.SubmitHandler()
		{
			/**
			 * 
			 */
			@Override
			public void onSubmit( SubmitEvent event )
			{
				// Hide the "login failed" message.
				hideLoginFailedMsg();
				
				// Show the the "authenticating..." message.
				showAuthenticatingMsg();
				
				// Are we using OpenID to authenticate
				if ( getUseOpenIDAuthentication() )
				{
					// Yes
					m_formPanel.setAction( "/ssf/j_spring_openid_security_check" );		
					m_formPanel.getElement().setAttribute( "name", "oidf" );
				}
			}
		});

		// Create a panel where hidden inputs will live.
		m_hiddenInputPanel = new FlowPanel();
		m_formPanel.add( m_hiddenInputPanel );
		
		// Add a hidden input for the "spring security redirect" which tells Teaming what page to go
		// to after the user authenticates.
		if ( m_springSecurityRedirect != null && m_springSecurityRedirect.length() > 0 )
		{
			Hidden hiddenInput;
			
			hiddenInput = new Hidden();
			hiddenInput.setName( "spring-security-redirect" );
			hiddenInput.setValue( m_springSecurityRedirect );
			m_hiddenInputPanel.add( hiddenInput );
		}

		// Add a hidden input that will hold the url of the selected OpenID authentication provider
		{
			m_openIdProviderInput = new Hidden();
			m_openIdProviderInput.setName( "openid_identifier" );
			m_openIdProviderInput.setValue( "" );
			m_hiddenInputPanel.add( m_openIdProviderInput );
		}
	}
	
	/**
	 * 
	 */
	private void createHeaderNow()
	{
		MastHead mastHead;
		boolean useDefaultImg = true;
		String imgUrl = null;
		
		// Get the branding data.
		mastHead = GwtTeaming.getMainPage().getMastHead();
		if ( mastHead != null )
		{
			GwtBrandingData brandingData;
			
			brandingData = mastHead.getSiteBrandingData();
			if ( brandingData != null )
			{
				String imgName;
				
				// Get the name of the image to use.
				imgName = brandingData.getLoginDlgImageName();
				
				// Do we have an image name to use
				if ( imgName != null && imgName.length() > 0 )
				{
					// Yes
					// Is the branding image name "__default teaming image__"?
					if ( imgName.equalsIgnoreCase( BrandingPanel.DEFAULT_TEAMING_IMAGE ) )
					{
						// Yes
						useDefaultImg = true;
					}
					// Is the branding image name "__no image__"?
					else if ( imgName.equalsIgnoreCase( BrandingPanel.NO_IMAGE ) )
					{
						// Yes
						useDefaultImg = false;
					}
					else
					{
						imgUrl = brandingData.getLoginDlgImageUrl();
						if ( imgUrl != null && imgUrl.length() > 0 )
							useDefaultImg = false;
					}
				}
			}
		}
	
		if ( useDefaultImg )
		{
			if ( GwtTeaming.m_requestInfo.isLicenseFilr() )
			{
				// Create a Novell Filr image that will be used in case there is no branding.
				imgUrl = GwtMainPage.m_requestInfo.getImagesPath() + "pics/Login/novell_filr_graphic.png";
			}
			else if ( GwtTeaming.m_requestInfo.isNovellTeaming() )
			{
				// Create a Novell Teaming image that will be used in case there is no branding.
				imgUrl = GwtMainPage.m_requestInfo.getImagesPath() + "pics/Login/novell_graphic.png";
			}
			else
			{
				// Create a Kablink Teaming image that will be used in case there is no branding.
				imgUrl = GwtMainPage.m_requestInfo.getImagesPath() + "pics/Login/kablink_graphic.png";
			}
		}
		
		// Should we add an image?
		if ( imgUrl != null )
		{
			Image img = null;

			// Yes
			img = new Image( imgUrl );
			m_headerPanel.add( img );
		}
	}
	
	/**
	 * Override the createHeader() method because we need to make it nicer.
	 */
	@Override
	public Panel createHeader( String caption )
	{
		m_caption = caption;
		m_headerPanel = new FlowPanel();
		
		return m_headerPanel;
	}// end createHeader()
	
	/**
	 * Create the controls needed for OpenID authentication
	 */
	private void createOpenIdControls( ArrayList<GwtOpenIDAuthenticationProvider> listOfProviders )
	{
		// Have we already created a "Use OpenID Authentication" checkbox?
		if ( m_useOpenIdCkbox == null )
		{
			FlowPanel panel;
			ValueChangeHandler<java.lang.Boolean> valueChangeHandler;

			// No
			// Add a "Use OpenID Authentication" checkbox.
			m_useOpenIdCkbox = new CheckBox( GwtTeaming.getMessages().loginDlgUseOpenId() );
			panel = new FlowPanel();
			panel.addStyleName( "loginDlg_useOpenIdPanel" );
			panel.add( m_useOpenIdCkbox );
			m_mainPanel.insert( panel, 0 );
			
			valueChangeHandler = new ValueChangeHandler<java.lang.Boolean>()
			{
				@Override
				public void onValueChange( ValueChangeEvent<java.lang.Boolean> event )
				{
					Scheduler.ScheduledCommand cmd;
					
					cmd = new Scheduler.ScheduledCommand()
					{
						@Override
						public void execute() 
						{
							danceOpenIdControls();
						}
					};
					Scheduler.get().scheduleDeferred( cmd );
				}
			};
			m_useOpenIdCkbox.addValueChangeHandler( valueChangeHandler );

			// Create a panel that holds all of the openid authentication providers
			{
				// Add a panel that will hold all of the OpenID authentication providers.
				m_openIdProvidersPanel = new FlowPanel();
				m_openIdProvidersPanel.addStyleName( "loginDlg_openIdProvidersPanel" );
				m_mainPanel.insert( m_openIdProvidersPanel, 1 );
				
				if ( listOfProviders != null && listOfProviders.size() > 0 )
				{
					FlexTable table;
					Label pleaseSelectLabel;
					int row;
					int i;
					
					pleaseSelectLabel = new Label( GwtTeaming.getMessages().loginDlgSelectAuthProviderLabel() );
					pleaseSelectLabel.addStyleName( "loginDlg_pleaseSelectProviderLabel" );
					m_openIdProvidersPanel.add( pleaseSelectLabel );
					
					table = new FlexTable();
					m_openIdProvidersPanel.add( table );
					
					row = 0;
					for (i = 0; i < listOfProviders.size(); ++i)
					{
						OpenIDAuthProviderImg img;
						GwtOpenIDAuthenticationProvider provider;
						int col;
						
						provider = listOfProviders.get( i );
						img = new OpenIDAuthProviderImg( provider );
						img.addStyleName( "loginDlg_openIdProviderImg" );
						
						img.addClickHandler( new ClickHandler() 
						{
							@Override
							public void onClick( ClickEvent event )
							{
								Scheduler.ScheduledCommand cmd;
								OpenIDAuthProviderImg img;
								final GwtOpenIDAuthenticationProvider selectedProvider;
								
								img = (OpenIDAuthProviderImg) event.getSource();
								selectedProvider = img.getProvider();
								
								cmd = new Scheduler.ScheduledCommand()
								{
									@Override
									public void execute() 
									{
										handleOpenIDAuthProviderSelected( selectedProvider );
									}
								};
								Scheduler.get().scheduleDeferred( cmd );
							}
						} );
						
						col = i % 3;
						table.setWidget( row, col, img );
						if ( col == 2 )
							++row;
						
					}// end for()
				}
			}
		}
	}

	/**
	 * Hide/show controls on the dialog based on the given login info
	 */
	private void danceDlg( GwtLoginInfo loginInfo )
	{
		debugAlert( "in danceDlg" );
		m_selfRegInfo = loginInfo.getSelfRegistrationInfo();
		
		// Hide or show the self registration controls.
		updateSelfRegistrationControls( m_selfRegInfo );

		// Turn auto complete on/off.
		if ( loginInfo.getAllowAutoComplete() == true )
			DOM.removeElementAttribute( m_formPanel.getElement(), "autocomplete" );
		else
			DOM.setElementAttribute( m_formPanel.getElement(), "autocomplete", "off" );
		
		// Is OpenID authentication available?
		if ( loginInfo.getAllowOpenIdAuthentication() )
		{
			debugAlert( "openid auth is allowed" );
			// Yes
			// Create the controls needed for openid authentication.
			createOpenIdControls( loginInfo.getOpenIDAuthenticationProviders() );
			
			// Show/hide the openid controls
			danceOpenIdControls();
		}
	}
	
	/**
	 * Show/hide the controls used with OpenId authentication
	 */
	private void danceOpenIdControls()
	{
		if ( m_useOpenIdCkbox != null )
		{
			// Is the "Use OpenId Authentication" checkbox checked.
			if ( m_useOpenIdCkbox.getValue() )
			{
				// Yes
				m_openIdProvidersPanel.setVisible( true );
				
				m_userIdLabelElement.getStyle().setDisplay( Display.NONE );
				m_userIdTxtBox.setVisible( false );
				
				m_pwdLabelElement.getStyle().setDisplay( Display.NONE );
				m_pwdTxtBox.setVisible( false );
			}
			else
			{
				// No
				m_openIdProvidersPanel.setVisible( false );
				
				m_userIdLabelElement.getStyle().setDisplay( Display.BLOCK );
				m_userIdTxtBox.setVisible( true );

				m_pwdLabelElement.getStyle().setDisplay( Display.BLOCK );
				m_pwdTxtBox.setVisible( true );
			}
		}
	}
	
	/**
	 * 
	 */
	@Override
	public Object getDataFromDlg()
	{
		// Nothing to do.
		return new Object();
	}// end getDataFromDlg()
	
	/**
	 * 
	 */
	private Long getExtUserId()
	{
		return GwtTeaming.getMainPage().getLoginExternalUserId();
	}
	
	/**
	 * 
	 */
	private String getFirstName()
	{
		if ( m_firstNameTxtBox != null )
			return m_firstNameTxtBox.getValue();
		
		return null;
	}
	
	/**
	 *  
	 */
	@Override
	public FocusWidget getFocusWidget()
	{
		return m_userIdTxtBox;
	}// end getFocusWidget()
	
	
	/**
	 * 
	 */
	private String getLastName()
	{
		if ( m_lastNameTxtBox != null )
			return m_lastNameTxtBox.getValue();
		
		return null;
	}
	
	/**
	 * Issue an ajax request to get login info from the server.
	 */
	private void getLoginInfoFromServer()
	{
		GetLoginInfoCmd cmd;
		
		debugAlert( "in getLoginInfoFromServer() m_requestedLoginInfo: " + m_requestedLoginInfo );
		if ( m_requestedLoginInfo == false )
		{
			AsyncCallback<VibeRpcResponse> rpcCallback;
			
			m_requestedLoginInfo = true;

			rpcCallback = new AsyncCallback<VibeRpcResponse>()
			{
				/**
				 * 
				 */
				@Override
				public void onFailure( Throwable t )
				{
					// Don't call GwtClientHelper.handleGwtRPCFailure() like we would normally do.  If the
					// session has expired, handleGwtRPCFailure() will invoke this login dialog again
					// and we will be in an infinite loop.
					// GwtClientHelper.handleGwtRPCFailure(
					//	t,
					//	GwtTeaming.getMessages().rpcFailure_GetSelfRegInfo());
					debugAlert( "In m_rpcGetLoginInfoCallback / onFailure()" );
				}// end onFailure()
		
				/**
				 * 
				 * @param result
				 */
				@Override
				public void onSuccess( VibeRpcResponse response )
				{
					debugAlert( "in m_rpcGetLoginInfoCallback / onSuccess()" );
					if ( response.getResponseData() != null && response.getResponseData() instanceof GwtLoginInfo )
					{
						final GwtLoginInfo loginInfo;

						debugAlert( "response data is not null" );
						loginInfo = (GwtLoginInfo) response.getResponseData();
						
						danceDlg( loginInfo );
					}
				}// end onSuccess()
			};
			
			// Issue an ajax request to get login information
			debugAlert( "about to execute GetLoginInfoCmd" );
			cmd = new GetLoginInfoCmd();
			GwtClientHelper.executeCommand( cmd, rpcCallback );
		}
	}
	
	/**
	 * 
	 */
	private String getPwd1()
	{
		if ( m_pwd1TxtBox != null )
			return m_pwd1TxtBox.getValue();
		
		return null;
	}
	
	/**
	 * 
	 */
	private String getPwd2()
	{
		if ( m_pwd2TxtBox != null )
			return m_pwd2TxtBox.getValue();
		
		return null;
	}
	
	/**
	 * This method gets called when the user clicks on the Register button
	 */
	private void handleClickOnRegisterBtn()
	{
		CompleteExternalUserSelfRegistrationCmd cmd;
		AsyncCallback<VibeRpcResponse> rpcCallback;
		
		rpcCallback = new AsyncCallback<VibeRpcResponse>()
		{
			/**
			 * 
			 */
			@Override
			public void onFailure( Throwable t )
			{
				// Don't call GwtClientHelper.handleGwtRPCFailure() like we would normally do.  If the
				// session has expired, handleGwtRPCFailure() will invoke this login dialog again
				// and we will be in an infinite loop.
				// GwtClientHelper.handleGwtRPCFailure(
				//	t,
				//	GwtTeaming.getMessages().rpcFailure_GetSelfRegInfo());
				debugAlert( "In CompleteExternalUserSelfRegistrationCmd / onFailure()" );
			}// end onFailure()
	
			/**
			 * 
			 * @param result
			 */
			@Override
			public void onSuccess( VibeRpcResponse response )
			{
				debugAlert( "In CompleteExternalUserSelfRegistrationCmd / onSuccess()" );
				if ( response.getResponseData() != null && response.getResponseData() instanceof BooleanRpcResponseData )
				{
					BooleanRpcResponseData responseData;
					
					responseData = (BooleanRpcResponseData) response.getResponseData();
					if ( responseData.getBooleanValue() == true )
					{
						ScheduledCommand cmd;
						
						cmd = new ScheduledCommand()
						{
							@Override
							public void execute()
							{
								showExternalUserRegConfirmation();
							}
						};
						Scheduler.get().scheduleDeferred( cmd );
					}
					else
						Window.alert( GwtTeaming.getMessages().loginDlg_externalUserSelfRegFailed() );
				}
			}
		};
		
		// Did the user fill out all the necessary information
		if ( isExternalUserSelfRegDataValid() == false )
		{
			// No, just bail.  The user will have already been told what's wrong.
			return;
		}
		
		cmd = new CompleteExternalUserSelfRegistrationCmd(
														getExtUserId(),
														getFirstName(),
														getLastName(),
														getPwd1(),
														GwtTeaming.getMainPage().getLoginInvitationUrl() );
		GwtClientHelper.executeCommand( cmd, rpcCallback );
	}
	
	/**
	 * Submit the login form using the url of the selecte openid authentication provider.
	 */
	private void handleOpenIDAuthProviderSelected( GwtOpenIDAuthenticationProvider provider )
	{
		if ( provider != null )
		{
			m_openIdProviderInput.setValue( provider.getUrl() );
			m_formPanel.submit();
		}
	}
	
	/**
	 * 
	 */
	private void hideAuthenticatingMsg()
	{
		if ( m_authenticatingMsg != null )
			m_authenticatingMsg.setVisible( false );
	}// end hideAuthenticatingMsg()

	
	/**
	 * 
	 */
	private void hideLoginFailedMsg()
	{
		if ( m_loginFailedMsg != null )
			m_loginFailedMsg.setVisible( false );
	}// end hideLoginFailedMsg()
	
	
	/**
	 * Initialize the controls in the dialog with the values from the given object.
	 * Currently there is nothing to initialize.
	 */
	public void init( Object props )
	{
	}// end init()

	
	/**
	 * Did the user fill out all the necessary data for external user self registration
	 */
	private boolean isExternalUserSelfRegDataValid()
	{
		String name;
		String pwd1;
		String pwd2;
		
		name = getFirstName();
		if ( name == null || name.length() == 0 )
		{
			Window.alert( GwtTeaming.getMessages().loginDlg_firstNameRequired() );
			m_firstNameTxtBox.setFocus( true );
			return false;
		}
		
		name = getLastName();
		if ( name == null || name.length() == 0 )
		{
			Window.alert( GwtTeaming.getMessages().loginDlg_lastNameRequired() );
			m_lastNameTxtBox.setFocus( true );
			return false;
		}
		
		pwd1 = getPwd1();
		if ( pwd1 == null || pwd1.length() == 0 )
		{
			Window.alert( GwtTeaming.getMessages().loginDlg_pwdRequired() );
			m_pwd1TxtBox.setFocus( true );
			return false;
		}
		
		pwd2 = getPwd2();
		if ( pwd1.equalsIgnoreCase( pwd2 ) == false )
		{
			Window.alert( GwtTeaming.getMessages().loginDlg_pwdDoNotMatch() );
			m_pwd2TxtBox.setFocus( true );
			return false;
		}
		
		// If we get here, everything is good.
		return true;
	}
	/*
	 * This method gets called when the user clicks on a button in the footer.
	 */
	@Override
	public void onClick( ClickEvent event )
	{
		Object	source;
		
		// Get the object that was clicked on.
		source = event.getSource();
		
		if ( source == m_cancelBtn )
		{
			hide();
			return;
		}
	}// end onClick()
	

	/**
	 * Should we allow the user to cancel the login dialog.
	 */
	private void setAllowCancel( boolean allowCancel )
	{
		if ( m_cancelBtn != null )
			m_cancelBtn.setVisible( allowCancel );
	}// end setAllowCancel()
	

	/**
	 * 
	 */
	public void showDlg( boolean allowCancel, LoginStatus loginStatus )
	{
		debugAlert( "In LoginDlg.showDlg()" );
		
		m_loginStatus = loginStatus;

		if ( m_initialized == false )
		{
			switch ( loginStatus )
			{
			case AuthenticationFailed:
				showRegularLoginUI();
			    showLoginFailedMsg();
			    break;
				
			case RegistrationRequired:
				showExternalUserRegistrationUI();
				break;
				
			case PromptForLogin:
				showRegularLoginUI();
				hideLoginFailedMsg();
				break;
			}
		}

		setAllowCancel( allowCancel );

		// Issue an ajax request to get self registration info and a list of open id providers
		if ( loginStatus != LoginStatus.RegistrationRequired )
			getLoginInfoFromServer();
		
		// We need to wait until the masthead has read the branding data
		if ( m_initialized == false )
		{
			Timer timer;
			
			timer = new Timer()
			{
				@Override
				public void run()
				{
					m_initialized = true;
					createHeaderNow();
					center();
				}
			};
			
			timer.schedule( 250 );
		}
		else
		{
			show();
		}
	}
	
	/**
	 * 
	 */
	private void showAuthenticatingMsg()
	{
		if ( m_authenticatingMsg != null )
			m_authenticatingMsg.setVisible( true );
	}// end showAuthenticatingMsg()

	/**
	 * Show the message when the external user has completed the registration
	 */
	private void showExternalUserRegConfirmation()
	{
		FlowPanel panel;
		FlexTable table;
		FlexCellFormatter cellFormatter;
		InlineLabel label;
		Image img;
		
		m_mainPanel.clear();
		
		panel = new FlowPanel();
		panel.addStyleName( "loginDlg_confirmationPanel" );
		
		table = new FlexTable();
		cellFormatter = table.getFlexCellFormatter();
		panel.add( table );
		
		// Add an image
		img = new Image( GwtTeaming.getImageBundle().emailConfirmation() );
		img.addStyleName( "loginDlg_confirmationImg" );
		table.setWidget( 0, 0, img );
		cellFormatter.setVerticalAlignment( 0, 0, HasVerticalAlignment.ALIGN_TOP );
		
		// Add a message telling the user to check their email.
		label = new InlineLabel( GwtTeaming.getMessages().loginDlg_ConfirmationText() );
		label.addStyleName( "loginDlg_confirmationHint" );
		table.setWidget( 0, 1, label );
		
		m_registerBtn.setVisible( false );
		
		m_mainPanel.add( panel );
	}
	
	/**
	 * An external user needs to complete the registration process.  Show the UI needed for that.
	 */
	private void showExternalUserRegistrationUI()
	{
		String openIdProviderName;
		GwtTeamingMessages messages;
		FlowPanel panel;
		
		m_mainPanel.clear();

		messages = GwtTeaming.getMessages();
		
		createFormPanel();
		
		panel = new FlowPanel();
		panel.addStyleName( "loginDlg_RegistrationRequiredPanel" );
		m_mainPanel.add( panel );
		
		// Hide the ok and cancel buttons
		{
			Element okElement;
			Element cancelElement;
			
			okElement = Document.get().getElementById( "loginOkBtn" );
			m_okBtn = Button.wrap( okElement );
			m_okBtn.setVisible( false );
			
			cancelElement = Document.get().getElementById( "loginCancelBtn" );
			m_cancelBtn = Button.wrap( cancelElement );
			m_cancelBtn.setText( messages.cancel() );
			m_cancelBtn.addClickHandler( this );
			m_cancelBtn.setVisible( false );
		}

		// Show the Register button
		{
			Element element;
			
			element = Document.get().getElementById( "loginRegisterBtn" );
			m_registerBtn = Button.wrap( element );
			m_registerBtn.setText( messages.loginDlg_Register() );
			m_registerBtn.setVisible( true );
			
			m_registerBtn.addClickHandler( new ClickHandler()
			{
				@Override
				public void onClick( ClickEvent event )
				{
					ScheduledCommand cmd;
					
					cmd = new ScheduledCommand()
					{
						@Override
						public void execute() 
						{
							handleClickOnRegisterBtn();
						}
					};
					Scheduler.get().scheduleDeferred( cmd );
				}
			} );
		}

		// Create a panel that holds the self registration controls
		{
			FlexTable table;
			Label label;
			int row;

			m_selfRegPanel = new FlowPanel();
			m_selfRegPanel.addStyleName( "loginDlg_selfRegPanel" );
			
			// Add some instructions
			label = new Label( messages.loginDlg_ExtUserRegistrationHint() );
			label.addStyleName( "loginDlg_extUserSelfRegHint" );
			m_selfRegPanel.add( label );
			
			panel.add( m_selfRegPanel );
			
			row = 0;
			table = new FlexTable();
			
			m_selfRegPanel.add( table );
			
			// Add a read-only control for the user id
			{
				TextBox txtBox;
				
				label = new Label( messages.loginDlgUserId() );
				table.setHTML( row, 0, label.getElement().getInnerHTML() );
	
				txtBox = new TextBox();
				txtBox.setReadOnly( true );
				txtBox.setValue( GwtTeaming.getMainPage().getLoginExternalUserName() );
				txtBox.setVisibleLength( 20 );
				table.setWidget( row, 1, txtBox );
			
				++row;
			}
			
			// Add the controls for the first name
			{
				label = new Label( messages.loginDlg_FirstNameLabel() );
				table.setHTML( row, 0, label.getElement().getInnerHTML() );
	
				m_firstNameTxtBox = new TextBox();
				m_firstNameTxtBox.setVisibleLength( 20 );
				table.setWidget( row, 1, m_firstNameTxtBox );
			
				++row;
			}
			
			// Add the controls for the last name
			{
				label = new Label( messages.loginDlg_LastNameLabel() );
				table.setHTML( row, 0, label.getElement().getInnerHTML() );
	
				m_lastNameTxtBox = new TextBox();
				m_lastNameTxtBox.setVisibleLength( 20 );
				table.setWidget( row, 1, m_lastNameTxtBox );
				
				++row;
			}
			
			// Add the controls for the password
			{
				label = new InlineLabel( messages.loginDlg_PwdLabel() );
				table.setHTML( row, 0, label.getElement().getInnerHTML() );
				
				m_pwd1TxtBox = new PasswordTextBox();
				m_pwd1TxtBox.setVisibleLength( 20 );
				table.setWidget( row, 1, m_pwd1TxtBox );
				++row;
			}

			// Add the controls for the reenter password
			{
				label = new InlineLabel( messages.loginDlg_ReenterPwdLabel() );
				table.setHTML( row, 0, label.getElement().getInnerHTML() );
				
				m_pwd2TxtBox = new PasswordTextBox();
				m_pwd2TxtBox.setVisibleLength( 20 );
				table.setWidget( row, 1, m_pwd2TxtBox );
				++row;
			}
		}
		
		// Get the name of the OpenID provider the user can use to complete the registration.
		openIdProviderName = GwtTeaming.getMainPage().getLoginOpenIdProviderName();

		// Get the url of the OpenID provider
		m_openIdProviderUrl = GwtTeaming.getMainPage().getLoginOpenIdProviderUrl();
		
		// Does the user have the option of completing the registration using OpenID? 
		if ( openIdProviderName != null && openIdProviderName.length() > 0 &&
			 m_openIdProviderUrl != null && m_openIdProviderUrl.length() > 0 )
		{
			FlowPanel openIdPanel;
			FlexTable table;
			FlexCellFormatter cellFormatter;
			Label label;
			Image providerImg;

			// Yes

			// Add the word "Or"
			{
				Label orLabel;
				
				orLabel = new Label( messages.loginDlg_OrLabel() );
				orLabel.addStyleName( "loginDlg_OrText" );
				panel.add( orLabel );
			}
			
			// Create an image for the openId provider
			{
				final GwtOpenIDAuthenticationProvider provider;
				
				provider = new GwtOpenIDAuthenticationProvider();
				provider.setName( openIdProviderName );
				provider.setTitle( openIdProviderName );
				provider.setUrl( m_openIdProviderUrl );
				
				providerImg = new OpenIDAuthProviderImg( provider );
				providerImg = new Image( providerImg.getUrl() );
				providerImg.addStyleName( "loginDlg_openIdProviderImg" );
				providerImg.setPixelSize( 50, 30 );
				
				providerImg.addClickHandler( new ClickHandler() 
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
								m_useOpenIdAuth = true;
								handleOpenIDAuthProviderSelected( provider );
							}
						};
						Scheduler.get().scheduleDeferred( cmd );
					}
				} );
			}
			
			openIdPanel = new FlowPanel();
			panel.add( openIdPanel );
			
			table = new FlexTable();
			panel.add( table );
			
			label = new Label( messages.loginDlg_AuthenticateUsingOpenID( openIdProviderName ) );
			label.getElement().getStyle().setWidth( 290, Unit.PX );
			table.setWidget( 0, 0, label );
			
			table.setWidget( 0, 1, providerImg );
			cellFormatter = table.getFlexCellFormatter();
			cellFormatter.setVerticalAlignment( 0, 1, HasVerticalAlignment.ALIGN_TOP );
		}
		
		// Hide the user name and password controls that are used for regular authentication
		{
			Element element;
			TextBox txtBox;
			PasswordTextBox pwd;
			InlineLabel label;
			
			element = Document.get().getElementById( "userIdLabel" );
			if ( element != null )
			{
				label = InlineLabel.wrap( element );
				label.setVisible( false );
			}

			element = Document.get().getElementById( "j_usernameId" );
			if ( element != null )
			{
				txtBox = TextBox.wrap( element );
				txtBox.setVisible( false );
			}

			element = Document.get().getElementById( "pwdLabel" );
			if ( element != null )
			{
				label = InlineLabel.wrap( element );
				label.setVisible( false );
			}

			element = Document.get().getElementById( "j_passwordId" );
			if ( element != null )
			{
				pwd = PasswordTextBox.wrap( element );
				pwd.setVisible( false );
			}
		}

		m_mainPanel.add( m_formPanel );
	}
	
	/**
	 * Show the UI needed for regular login
	 */
	private void showRegularLoginUI()
	{
		m_mainPanel.clear();
		
		createFormPanel();
		
		// Add a row for the "user id" controls.
		{
			Element userIdElement;
			String userName;
			
			m_userIdLabelElement = Document.get().getElementById( "userIdLabel" );
			m_userIdLabelElement.setInnerText( GwtTeaming.getMessages().loginDlgUserId() );
			
			userIdElement = Document.get().getElementById( "j_usernameId" );
			m_userIdTxtBox = TextBox.wrap( userIdElement );
			
			// Do we have the user name for an external user trying to log in for the first time?
			userName = GwtTeaming.getMainPage().getLoginExternalUserName();
			if ( userName != null && userName.length() > 0 )
				m_userIdTxtBox.setValue( userName );
		}
		
		// Add a row for the "password" controls.
		{
			Element pwdTxtBoxElement;
			
			m_pwdLabelElement = Document.get().getElementById( "pwdLabel" );
			m_pwdLabelElement.setInnerText( GwtTeaming.getMessages().loginDlgPassword() );
			
			pwdTxtBoxElement = Document.get().getElementById( "j_passwordId" );
			m_pwdTxtBox = PasswordTextBox.wrap( pwdTxtBoxElement );
		}
		
		// Add a "login failed" label to the dialog.
		{
			Element loginFailedElement;
			
			loginFailedElement = Document.get().getElementById( "loginFailedMsgDiv" );
			loginFailedElement.setInnerText( GwtTeaming.getMessages().loginDlgLoginFailed() );
			m_loginFailedMsg = Label.wrap( loginFailedElement );
			m_loginFailedMsg.setVisible( false );
		}
		
		// Add a "Authenticating..." label to the dialog.
		{
			Element authenticatingElement;
			
			authenticatingElement = Document.get().getElementById( "authenticatingDiv" );
			authenticatingElement.setInnerText( GwtTeaming.getMessages().loginDlgAuthenticating() );
			m_authenticatingMsg = Label.wrap( authenticatingElement );
			m_authenticatingMsg.setVisible( false );
		}
		
		// Create the ok and cancel buttons
		{
			Element okElement;
			Element cancelElement;
			
			okElement = Document.get().getElementById( "loginOkBtn" );
			m_okBtn = Button.wrap( okElement );
			m_okBtn.setText( GwtTeaming.getMessages().login() );
			
			cancelElement = Document.get().getElementById( "loginCancelBtn" );
			m_cancelBtn = Button.wrap( cancelElement );
			m_cancelBtn.setText( GwtTeaming.getMessages().cancel() );
			m_cancelBtn.addClickHandler( this );
			m_cancelBtn.setVisible( false );
		}
		
		// Create a "I forgot my password" link.
		{
			ClickHandler clickHandler;
			MouseOverHandler mouseOverHandler;
			MouseOutHandler mouseOutHandler;
			Element forgotPwdElement;
			
			forgotPwdElement = Document.get().getElementById( "forgottenPwdSpan" );
			forgotPwdElement.setInnerText( GwtTeaming.getMessages().loginDlg_ForgottenPwd() );
			m_forgotPwdLink = InlineLabel.wrap( forgotPwdElement );
			m_forgotPwdLink.setVisible( true );
			
			// Add a clickhandler to the "I forgot my password" link.  When the user clicks on the link we
			// will invoke the "Forgotten password" dialog.
			clickHandler = new ClickHandler()
			{
				/**
				 * Clear all branding information.
				 */
				@Override
				public void onClick( ClickEvent event )
				{
					Scheduler.ScheduledCommand cmd;
					
					cmd = new Scheduler.ScheduledCommand()
					{
						@Override
						public void execute() 
						{
							invokeForgottenPwdDlg();
						}
					};
					Scheduler.get().scheduleDeferred( cmd );
				}
			};
			m_forgotPwdLink.addClickHandler( clickHandler );

			// Add a mouse-over handler
			mouseOverHandler = new MouseOverHandler()
			{
				/**
				 * 
				 */
				@Override
				public void onMouseOver( MouseOverEvent event )
				{
					Widget widget;
					
					widget = (Widget)event.getSource();
					widget.removeStyleName( "subhead-control-bg1" );
					widget.addStyleName( "subhead-control-bg2" );
				}// end onMouseOver()
			};
			m_forgotPwdLink.addMouseOverHandler( mouseOverHandler );

			// Add a mouse-out handler
			mouseOutHandler = new MouseOutHandler()
			{
				/**
				 * 
				 */
				@Override
				public void onMouseOut( MouseOutEvent event )
				{
					Widget widget;
					
					// Remove the background color we added to the anchor when the user moved the mouse over the anchor.
					widget = (Widget)event.getSource();
					widget.removeStyleName( "subhead-control-bg2" );
					widget.addStyleName( "subhead-control-bg1" );
				}// end onMouseOut()
			};
			m_forgotPwdLink.addMouseOutHandler( mouseOutHandler );
		}

		// Create a "Create new account" link that will initially be hidden.
		// We will show this link later when we get the response to our request to get
		// self registration info and self registration is allowed.
		{
			ClickHandler clickHandler;
			MouseOverHandler mouseOverHandler;
			MouseOutHandler mouseOutHandler;
			Element selfRegElement;
			
			selfRegElement = Document.get().getElementById( "createNewAccountSpan" );
			selfRegElement.setInnerText( GwtTeaming.getMessages().loginDlgCreateNewAccount() );
			m_selfRegLink = InlineLabel.wrap( selfRegElement );
			m_selfRegLink.setVisible( false );
			
			// Add a clickhandler to the "Create new account" link.  When the user clicks on the link we
			// will invoke the "Create user" page.
			clickHandler = new ClickHandler()
			{
				/**
				 * Clear all branding information.
				 */
				@Override
				public void onClick( ClickEvent event )
				{
					Scheduler.ScheduledCommand cmd;
					
					cmd = new Scheduler.ScheduledCommand()
					{
						@Override
						public void execute() 
						{
							String url;
							
							// Get the url we need to invoke the "Create User" page.
							url = m_selfRegInfo.getCreateUserUrl();
							
							// Invoke the "Create User" page in a new window.
							Window.open( url, "self_reg_create_new_account", "height=750,resizeable,scrollbars,width=750" );
						}
					};
					Scheduler.get().scheduleDeferred( cmd );
				}//end onClick()
			};
			m_selfRegLink.addClickHandler( clickHandler );
			
			// Add a mouse-over handler
			mouseOverHandler = new MouseOverHandler()
			{
				/**
				 * 
				 */
				@Override
				public void onMouseOver( MouseOverEvent event )
				{
					Widget widget;
					
					widget = (Widget)event.getSource();
					widget.removeStyleName( "subhead-control-bg1" );
					widget.addStyleName( "subhead-control-bg2" );
				}// end onMouseOver()
			};
			m_selfRegLink.addMouseOverHandler( mouseOverHandler );

			// Add a mouse-out handler
			mouseOutHandler = new MouseOutHandler()
			{
				/**
				 * 
				 */
				@Override
				public void onMouseOut( MouseOutEvent event )
				{
					Widget widget;
					
					// Remove the background color we added to the anchor when the user moved the mouse over the anchor.
					widget = (Widget)event.getSource();
					widget.removeStyleName( "subhead-control-bg2" );
					widget.addStyleName( "subhead-control-bg1" );
				}// end onMouseOut()
			};
			m_selfRegLink.addMouseOutHandler( mouseOutHandler );
		}
		
		m_mainPanel.add( m_formPanel );
	}
	
	/**
	 * 
	 */
	private void showLoginFailedMsg()
	{
		m_loginFailedMsg.setVisible( true );
	}// end hideLoginFailedMsg()
	
	/**
	 * Show or hide the controls dealing with self registration depending on the values in
	 * the given GwtSelfRegistrationInfo object.
	 */
	private void updateSelfRegistrationControls( GwtSelfRegistrationInfo selfRegInfo )
	{
		String url;
		
		// Is self registration allowed?
		url = selfRegInfo.getCreateUserUrl();
		if ( selfRegInfo.isSelfRegistrationAllowed() && url != null && url.length() > 0 )
		{
			// Yes, show the self registration button.
			m_selfRegLink.setVisible( true );
		}
		else
		{
			// No, hide the self registration button.
			m_selfRegLink.setVisible( false );
		}
	}// end updateSelfRegistrationControls()
	
	/**
	 * Return whether we should use OpenID authentication 
	 */
	private boolean getUseOpenIDAuthentication()
	{
		if ( m_useOpenIdCkbox != null && m_useOpenIdCkbox.isVisible() && m_useOpenIdCkbox.getValue() == true )
			return true;
		
		// m_useOpenIdAuth is used when we are displaying the external user self registration ui
		// and the user selected to authenticate using an OpenID provider.
		if ( m_useOpenIdAuth )
			return true;
		
		return false;
	}

	/**
	 * Invoke the "Forgotten Password" dialog
	 */
	private void invokeForgottenPwdDlg()
	{
		if ( m_forgottenPwdDlg == null )
		{
			ForgottenPwdDlg.createAsync(
										false, 
										true,
										new ForgottenPwdDlgClient()
			{			
				@Override
				public void onUnavailable()
				{
					// Nothing to do.  Error handled in asynchronous provider.
				}
				
				@Override
				public void onSuccess( final ForgottenPwdDlg fpDlg )
				{
					ScheduledCommand cmd;
					
					cmd = new ScheduledCommand()
					{
						@Override
						public void execute() 
						{
							m_forgottenPwdDlg = fpDlg;
							
							fpDlg.init();
							fpDlg.showRelativeTo( m_forgotPwdLink );
						}
					};
					Scheduler.get().scheduleDeferred( cmd );
				}
			} );
		}
		else
		{
			m_forgottenPwdDlg.init();
			m_forgottenPwdDlg.showRelativeTo( m_forgotPwdLink );
		}
	}
	
	/**
	 * Callback interface to interact with the dialog asynchronously
	 * after it loads. 
	 */
	public interface LoginDlgClient
	{
		void onSuccess( LoginDlg dlg );
		void onUnavailable();
	}
	
	/**
	 * Loads the LoginDlg split point and returns an instance
	 * of it via the callback.
	 * 
	 */
	public static void createAsync(
							final boolean autoHide,
							final boolean modal,
							final int left,
							final int top,
							final Object properties,
							final String loginUrl,
							final String springSecurityRedirect,
							final LoginDlgClient loginDlgClient )
	{
		GWT.runAsync( LoginDlg.class, new RunAsyncCallback()
		{
			@Override
			public void onFailure(Throwable reason)
			{
				Window.alert( GwtTeaming.getMessages().codeSplitFailure_LoginDlg() );
				if ( loginDlgClient != null )
				{
					loginDlgClient.onUnavailable();
				}
			}

			@Override
			public void onSuccess()
			{
				LoginDlg loginDlg;
				
				loginDlg = new LoginDlg(
									autoHide,
									modal,
									left,
									top,
									properties,
									loginUrl,
									springSecurityRedirect );
				loginDlgClient.onSuccess( loginDlg );
			}
		});
	}
}// end LoginDlg
