/*
 *  The MHS-Collections Project
 *  Copyright © 2012  Madeira Historical Society (developed by Brian Groenke)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  Note: This class was borrowed from the Groenke Commons Java API with full permission from the author.
 */
package org.madeirahs.applet.ui;

import java.awt.*;

import org.madeirahs.applet.*;
import org.madeirahs.shared.*;
import org.madeirahs.shared.v3d.V3DPanel.LoaderCallback;

/**
 * @author Brian Groenke
 *
 */
public class ArtifactPageView extends PageView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4953054120903676925L;
	
	private CollectionsApplet cntxt;

	/**
	 * 
	 */
	public ArtifactPageView(final ArtifactView view, CollectionsApplet context) {
		this.cntxt = context;
		navMsg = "Viewing: " + view.elem.title + " [" + ((view.elem.is3DSupported()) ? "V3D: ":"") + "Loading " +
	        view.elem.filenames.length + " image(s)]";
		setLayout(new BorderLayout());
		setOpaque(false);
		add(BorderLayout.CENTER, view);
		view.addLoaderCallback(new LoaderCallback() {

			@Override
			public void onFinish(boolean success) {
				if(success) {
					navMsg = "Viewing: " + view.elem.title + " [" + ((view.elem.is3DSupported()) ? "V3D":"No V3D") + "]";
				} else
					navMsg = "Error: Failed to load images";
				view.removeLoaderCallback(this);
				cntxt.getNavBar().setMessage(navMsg);
			}
			
		});
	}

}
