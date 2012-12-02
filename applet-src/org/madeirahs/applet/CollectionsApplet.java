package org.madeirahs.applet;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;

import javax.imageio.*;
import javax.swing.*;

import org.madeirahs.applet.ui.*;
import org.madeirahs.shared.database.*;
import org.madeirahs.shared.provider.*;

public class CollectionsApplet extends JApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1165776873247472887L;

	public static final String DATABASE_LOC = "/database/", APPLET_RSC = "/applet_rsc/", IMAGE_NAME = "miller_house.jpg";

	private static final int DEFAULT_WIDTH = 800, DEFAULT_HEIGHT = 600;
	private static final boolean TEST_MODE = false;
	private static CollectionsApplet local;

	NavBar nav;
	PageQueue pq;
	RootPanel root;
	LoadScreen load;
	Database db;

	@Override
	public void init() {
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		local = this;
		
		nav = new NavBar();
		pq = new PageQueue();
		root = new RootPanel();
		root.setLayout(new FlowLayout());
		add(BorderLayout.NORTH, nav);
		add(BorderLayout.CENTER, root);
		
		JPanel test = new JPanel();
		test.setOpaque(false);
		root.add(test, RootPanel.DEFAULT_LAYER, 0);
		
		setLoadScreen(true);
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					loadDatabase();
				} catch (ClassCastException e) {
					e.printStackTrace();
					load.setShowFailMessage(true);
				} catch (IOException e) {
					e.printStackTrace();
					load.setShowFailMessage(true);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					load.setShowFailMessage(true);
				}
			}

		}).start();
	}

	@Override
	public void start() {

	}

	@Override
	public void stop() {

	}

	@Override
	public void destroy() {

	}

	public Database getDatabase() {
		return db;
	}
	
	private void setLoadScreen(boolean show) {
		System.out.println(show);
		if(show) {
			load = new LoadScreen();
			root.add(load, RootPanel.PALETTE_LAYER, 0);
		} else
			root.remove(load);
	}

	private void loadDatabase() throws IOException, ClassCastException, ClassNotFoundException {
		URL url = this.getCodeBase();
		HTTPProvider prov = new HTTPProvider(url);
		db = Database.getInstance(DATABASE_LOC, prov, null);
		setLoadScreen(false);
	}

	private class RootPanel extends JLayeredPane {

		/**
		 * 
		 */
		private static final long serialVersionUID = 826443815825550187L;

		BufferedImage background;

		RootPanel() {
			setOpaque(false);
			try {
				background = ImageIO.read(new URL("http://www.madeirahs.org/collection/" + APPLET_RSC + IMAGE_NAME));
				if(Scale.isScalingNeeded()) {
					Image scaled = background.getScaledInstance(Scale.sx(background.getWidth()), Scale.sy(background.getHeight()), Image.SCALE_SMOOTH);
					background = new BufferedImage(scaled.getWidth(null), scaled.getHeight(null), background.getType());
					Graphics g = background.getGraphics();
					g.drawImage(scaled, 0, 0, null);
					g.dispose();
				}

				this.setMinimumSize(new Dimension(background.getWidth(), background.getHeight()));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void paintComponent(Graphics g) {
			g.drawImage(background, 0, 0, null);
			super.paintComponent(g);
		}

	}
	
	private class LoadScreen extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 826803169333478624L;
		
		String msg = "Loading Database...";
		
	    LoadScreen() {
			setOpaque(false);
			this.setPreferredSize(local.getSize());
		}
		
		@Override
		public void paintComponent(Graphics g) {
			int wt = getWidth(), ht = getHeight();
			if(wt <= 0 || ht <=0)
				return;
			g.setColor(new Color(0, 0, 0, 200));
			g.fillRect(0, 0, wt, ht);
			g.setColor(Color.WHITE);
			g.drawString(msg, wt - 200, ht - 20);
		}
		
		public void setShowFailMessage(boolean show) {
			if(show) {
				msg = "Failed to load Database.";
				repaint();
			}
		}
	}

}
