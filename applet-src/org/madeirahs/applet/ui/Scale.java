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
