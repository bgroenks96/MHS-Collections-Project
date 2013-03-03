/*
 *  The MHS-Collections Project applet contains the projects primary front-end
 *  code deployed on the website for use by end-users.
 *  Copyright © 2012-2013 Madeira Historical Society (developed by Brian Groenke)
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

package org.madeirahs.applet;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

import org.madeirahs.applet.ui.*;
import org.madeirahs.shared.database.*;
import org.madeirahs.shared.provider.*;

public class CollectionsApplet extends JApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1165776873247472887L;

	public static final String DATABASE_LOC = "/database/", APPLET_RSC = "/applet_rsc/", IMAGE_NAME = "miller_house.jpg";

	private static final boolean TEST_MODE = true;
	private static CollectionsApplet local;
	
	private URL url;

	NavBar nav;
	PageQueue pq;
	MainView main;
	RootPanel root;
	Database db;
	HTTPProvider prov;

	@Override
	public void init() {
		local = this;
		try {
			url = (TEST_MODE) ? new URL("http://www.madeirahs.org/collection/"):this.getCodeBase();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		// set the UI to system default if not Metal, otherwise use Nimbus.
		try {
			if(!UIManager.getSystemLookAndFeelClassName().contains("Metal"))
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			else {
				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if ("Nimbus".equals(info.getName())) {
						UIManager.setLookAndFeel(info.getClassName());
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		local = this;

		ImageIO.setUseCache(true); // use cache files when providers load images.

		try {
			nav = new NavBar(this);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		nav.setOpaque(false);
		pq = new PageQueue();
		main = new MainView(local);
		pq.addNew(main);
		root = new RootPanel();
		root.setLayout(new BorderLayout());
		root.add(BorderLayout.NORTH, nav);
		updateView(null);
		setContentPane(root);

		setLoadScreen(true, false, true, "Loading Database...");
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Object lock = root.init();
					synchronized(lock) {
						try {
							lock.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					loadDatabase();
					main.initData();
				} catch (ClassCastException e) {
					e.printStackTrace();
					setLoadScreen(true, true, false, "Failed to load Database: error in class casting");
				} catch (IOException e) {
					e.printStackTrace();
					setLoadScreen(true, true, false, "Failed to load Database: I/O error");
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					setLoadScreen(true, true, false, "Failed to load Database: Class not found");
				}
			}

		}).start();
	}

	/**
	 * 
	 * @param prev the previous Component that should be removed
	 */
	public void updateView(Component prev) {
		if(prev != null)
			root.remove(prev);
		root.add(BorderLayout.CENTER, pq.getCurrent());
		validate();
		repaint();
	}

	public PageQueue getPageQueue() {
		return pq;
	}

	public HTTPProvider getProvider() {
		return prov;
	}

	public URL getResourceURL() {
		try {
			return new URL(url + APPLET_RSC);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public NavBar getNavBar() {
		return nav;
	}

	@Override
	public void start() {

	}

	@Override
	public void stop() {

	}

	@Override
	public void destroy() {
		root.removeAll();
		db = null;
	}

	public Database getDatabase() {
		return db;
	}

	public void setLoadScreen(boolean show, boolean allowDismiss, boolean showProg, String msg) {
		root.showLoadScreen = show;
		root.loadMsg = msg;
		root.allowDismiss = allowDismiss;
		root.drawProg = showProg;
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				root.repaint();
			}

		});
	}

	private void loadDatabase() throws IOException, ClassCastException, ClassNotFoundException {
		prov = new HTTPProvider(url);
		db = Database.getInstance(DATABASE_LOC, prov, null);
		setLoadScreen(false, false, false, null);
	}

	private class RootPanel extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 826443815825550187L;

		Image loaderCurr, cache1, cache2, cache3;
		BufferedImage background;

		String loadMsg;
		boolean showLoadScreen, allowDismiss, drawProg;
		volatile boolean rscsLoaded;

		private int num = 0;
		private Timer loader = new Timer(500, null);

		RootPanel() {
			Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {

				@Override
				public void eventDispatched(AWTEvent arg0) {
					if(!showLoadScreen)
						return;
					if(allowDismiss) {
						showLoadScreen = false;
						allowDismiss = false;
						repaint();
					}

					MouseEvent me = (MouseEvent) arg0;
					me.consume();
				}

			}, AWTEvent.MOUSE_EVENT_MASK);

			Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {

				@Override
				public void eventDispatched(AWTEvent arg0) {
					if(!showLoadScreen)
						return;
					if(allowDismiss) {
						showLoadScreen = false;
						allowDismiss = false;
						repaint();
					}
					KeyEvent me = (KeyEvent) arg0;
					me.consume();
				}

			}, AWTEvent.KEY_EVENT_MASK);

			loader.setInitialDelay(0);
			loader.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if(num > 2)
						num = 0;
					switch(num) {
					case 0:
						loaderCurr = cache1;
						break;
					case 1:
						loaderCurr = cache2;
						break;
					case 2:
						loaderCurr = cache3;
					}
					num++;
					repaint();
				}
			});
		}

		volatile boolean taskStarted;

		@Override
		public void paint(Graphics g) {
			if(!rscsLoaded) {
				return;
			}

			g.drawImage(background, 0, 0, null);
			super.paint(g);
			if(showLoadScreen) {
				int wt = getWidth(), ht = getHeight();
				if(wt <= 0 || ht <=0)
					return;
				g.setColor(new Color(0, 0, 0, 200));
				g.fillRect(0, 0, wt, ht);
				g.setColor(Color.WHITE);
				if(loadMsg == null)
					loadMsg = "Loading...";
				g.setFont(new Font("Arial", Font.PLAIN, 18));
				int ty = (ht / 2) - g.getFontMetrics().getHeight();
				g.drawString(loadMsg, (int) (wt/2 - g.getFontMetrics().getStringBounds(loadMsg, g).getWidth() / 2), ty);
				if(loaderCurr != null && drawProg) {
					g.drawImage(loaderCurr, (wt/2) - loaderCurr.getWidth(null) / 2, ty + g.getFontMetrics().getHeight() + 10, null);
				}
				this.requestFocus();
				if(!loader.isRunning() && drawProg) {
					num = 0;
					loader.start();
				}
			} else if(loader.isRunning()) {
				loader.stop();
				num = 0;
				loaderCurr = null;
			}
		}

		protected Object init() {
			Object lockObj = new Object();
			if(!taskStarted) {
				LoadResources task = new LoadResources(lockObj);
				Thread t = new Thread(task);
				t.setName("resource_loader");
				t.start();
				taskStarted = true;
			}
			return lockObj;
		}

		private void drawRscLoader(final Graphics g, final Rectangle rloader, final float percent) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						Graphics2D g2d = (Graphics2D) g;
						g2d.setColor(Color.DARK_GRAY);
						int fillWidth = Math.round(rloader.width * percent);
						g2d.draw(rloader);
						g2d.fillRect(rloader.x, rloader.y, fillWidth, rloader.height);
					}

				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		private class LoadResources implements Runnable {

			Object lock;

			LoadResources(Object lock) {
				this.lock = lock;
			}

			@Override
			public void run() {
				Graphics g = getGraphics();
				Rectangle rloader = new Rectangle(0, 0, getWidth() / 3, getHeight() / 10);
				rloader.setLocation((getWidth() / 2) - (rloader.width / 2), (getHeight() / 2) - (rloader.height / 2));
				drawRscLoader(g, rloader, 0.0f);
				try {
					cache1 = ImageIO.read(new URL(url + APPLET_RSC + "loader0.png"));
					drawRscLoader(g, rloader, 0.25f);
					cache2 = ImageIO.read(new URL(url + APPLET_RSC + "loader1.png"));
					drawRscLoader(g, rloader, 0.50f);
					cache3 = ImageIO.read(new URL(url + APPLET_RSC + "loader2.png"));
					drawRscLoader(g, rloader, 0.75f);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				setOpaque(false);
				try {
					background = ImageIO.read(new URL(url + APPLET_RSC + IMAGE_NAME));
					drawRscLoader(g, rloader, 1.0f);
					Image scaled = background.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
					background = new BufferedImage(scaled.getWidth(null), scaled.getHeight(null), background.getType());
					Graphics ig = background.getGraphics();
					ig.drawImage(scaled, 0, 0, null);
					ig.dispose();

					setMinimumSize(new Dimension(background.getWidth(), background.getHeight()));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				rscsLoaded = true;
				synchronized(lock) {
					repaint();
					lock.notifyAll();
				}
			}

		}

	}
}
