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
package org.kablink.teaming.gwt.client.binderviews;

import org.kablink.teaming.gwt.client.GwtTeaming;
import org.kablink.teaming.gwt.client.GwtTeamingDataTableImageBundle;
import org.kablink.teaming.gwt.client.GwtTeamingFilrImageBundle;
import org.kablink.teaming.gwt.client.GwtTeamingMessages;
import org.kablink.teaming.gwt.client.util.FolderEntryDetails;
import org.kablink.teaming.gwt.client.util.GwtClientHelper;
import org.kablink.teaming.gwt.client.util.ViewFileInfo;
import org.kablink.teaming.gwt.client.widgets.VibeFlowPanel;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Image;

/**
 * Class that holds the folder entry viewer document area.
 * 
 * @author drfoster@novell.com
 */
@SuppressWarnings("unused")
public class FolderEntryDocument extends VibeFlowPanel {
	private FolderEntryCallback				m_fec;			// Callback to the folder entry composite.
	private FolderEntryDetails				m_fed;			// The full details about the folder entry being viewed.
	private Frame							m_htmlFrame;	// The <IFRAME> containing rendered HTML when it's available from the file.
	private GwtTeamingDataTableImageBundle	m_images;		// Access to Vibe's images.
	private GwtTeamingFilrImageBundle		m_filrImages;	// Access to Filr's images.
	private GwtTeamingMessages				m_messages;		// Access to Vibe's messages.

	private final static int	NO_VSCROLL_ADJUST		= 20;
	private final static String VIEW_DOCUMENT_FRAME_ID	= "ss_iframe_fileview_GWT";
	
	public FolderEntryDocument(FolderEntryCallback fec, FolderEntryDetails fed) {
		// Initialize the super class...
		super();
		
		// ...store the parameters...
		m_fec = fec;
		m_fed = fed;
		
		// ...initialize the data members requiring it...
		m_filrImages = GwtTeaming.getFilrImageBundle();
		m_images     = GwtTeaming.getDataTableImageBundle();
		m_messages   = GwtTeaming.getMessages();
		
		// ...and construct the document's content.
		createContent();
	}
	
	/*
	 * Creates the document's content.
	 */
	private void createContent() {
		// Do we have HTML to render for the document?
		String contentSpecificStyles;
		ViewFileInfo vfi = m_fed.getHtmlView();
		String htmlUrl = ((null != vfi) ? vfi.getViewFileUrl() : "");
		if (GwtClientHelper.hasString(htmlUrl)) {
			// Yes!  Create an <IFRAME> for to render into...
			m_htmlFrame = new Frame();
			m_htmlFrame.addStyleName("vibe-feView-documentFrame");
			Element dfE = m_htmlFrame.getElement(); 
			dfE.setId(VIEW_DOCUMENT_FRAME_ID);
			dfE.setAttribute("frameBorder",  "0");
			dfE.setAttribute("scrolling", "auto");
			m_htmlFrame.setUrl(  htmlUrl    );
			m_htmlFrame.setTitle(m_fed.getTitle());
			add(m_htmlFrame);
			
			// ...and add the HTML specific content style.
			contentSpecificStyles = "vibe-feView-documentPanelHtml";
		}
		
		else {
			// No, we don't have HTML to render for the document!  Do
			// we have a URL we can use as an <IMG> for the file?
			String imgUrl = m_fed.getDownloadUrl();
			String imgStyle;
			if (m_fed.isContentImage() && GwtClientHelper.hasString(imgUrl)) {
				// Yes!  We'll display that with an appropriate style.
				imgStyle = "vibe-feView-documentContentImage";
				
				// ...and add the image specific content style.
				contentSpecificStyles = "vibe-feView-documentPanelImage";
			}
			
			else {
				// No, we don't have the URL for an image
				// representation of the file's data either!  Add the
				// unknown image...
				imgUrl   = m_images.unknown().getSafeUri().asString();
				imgStyle = "vibe-feView-documentUnknownImage";
				
				// ...and add the unknown specific content style.
				contentSpecificStyles = "vibe-feView-documentPanelUnknown";
			}

			// Add the appropriate image. 
			Image i = GwtClientHelper.buildImage(imgUrl);
			i.addStyleName(imgStyle);
			add(i);
			
		}
		
		// Add the panel's styles...
		addStyleName("vibe-feView-documentPanel");
		addStyleName(contentSpecificStyles);
		
		// ...and tell the composite that we're ready.
		m_fec.viewComponentReady();
	}
	
	/**
	 * Called to set the panel's height.
	 */
	@Override
	public void setHeight(String height) {
		// If we've got an <IFRAME> containing HTML...
		String width  = ((getOffsetWidth() - NO_VSCROLL_ADJUST) + "px");
		if (null != m_htmlFrame) {
			// ...set it's size...
			m_htmlFrame.setHeight(height);
			m_htmlFrame.setWidth( width );
		}

		// ...and pass the height to the super class.
		super.setHeight(height);
	}
}
