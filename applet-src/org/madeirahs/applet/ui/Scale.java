/*
 *  The MHS-Collections Project applet contains the projects primary front-end
 *  code deployed on the website for use by end-users.
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

public class Scale {
	
	public static final Dimension DEFAULT = new Dimension(1920, 1080);
	
	private static double rx = 1.0, ry = 1.0;
	private static boolean needScaling;
	
	static {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		if(screen.width < DEFAULT.width) {
			rx = (double) screen.width / (double) DEFAULT.width;
			needScaling = true;
		}
		
		if(screen.height < DEFAULT.height) {
			ry = (double) screen.height / (double) DEFAULT.height;
			needScaling = true;
		}
	}
	
	public static int sx(int x) {
		return (int) Math.round(x * rx);
	}
	
	public static int sy(int y) {
		return (int) Math.round(y * ry);
	}
	
	public static boolean isScalingNeeded() {
		return needScaling;
	}
}
