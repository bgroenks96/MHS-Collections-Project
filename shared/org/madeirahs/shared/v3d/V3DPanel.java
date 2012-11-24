/*
 *  The MHS-Collections Project shared library is intended for use by both the applet
 *  and editor software in the interest of code consistency.
 *  Copyright © 2012-  Madeira Historical Society (developed by Brian Groenke)
 *
 *  This library is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.madeirahs.shared.v3d;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.ref.SoftReference;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.madeirahs.shared.Artifact;
import org.madeirahs.shared.provider.DataProvider;

public class V3DPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4244754298124493583L;

	private static final int FRAME_UP = 0, FRAME_DOWN = 1, FRAME_LEFT = 2,
			FRAME_RIGHT = 3;

	public DataProvider provider;

	private V3DBundle bundle;
	private String[] rscNames;
	private SoftReference<BufferedImage[]> rscs;
	private int width, height;
	private V3DHandler handler;
	private ImageLoader loader = new ImageLoader();
	private volatile boolean v3d, loaded;
	private int currFrame = V3DBundle.FRONT;
	private V3DPanel instance;

	public V3DPanel(DataProvider provider) {
		this.provider = provider;
		handler = new V3DHandler();
		addMouseListener(handler);
		addMouseMotionListener(handler);
		instance = this;
	}

	public V3DPanel(V3DBundle bundle, DataProvider provider) {
		this(provider);
		this.bundle = bundle;
		if (bundle != null) {
			rscNames = bundle.getFinalImageArray();
			v3d = true;
		}
	}

	public V3DPanel(Artifact a, DataProvider provider) {
		this(a.bundle, provider);
		if (!a.is3DSupported()) {
			if (a.filenames != null && a.filenames.length > 0) {
				rscNames = a.filenames;
			}
			v3d = false;
		}
	}

	long last = 0;
	int count = 0;
	int max = 3;
	StringBuilder sb = new StringBuilder("Loading image data");

	@Override
	public void paintComponent(Graphics g) {
		width = getWidth();
		height = getHeight();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		if (!loaded && (rscNames != null && rscNames.length > 0)
				&& !loader.failed) {
			if (!loader.loading) {
				new Thread(loader).start();
				new Thread(new Runnable() {

					@Override
					public void run() {
						while (!loaded) {
							repaint();
						}
					}

				}).start();
			}

			if (last == 0) {
				last = System.currentTimeMillis();
			}

			if (System.currentTimeMillis() - last > 500) {
				if (count >= max) {
					count = 0;
					sb.delete(sb.length() - (max), sb.length());
				} else {
					count++;
					sb.append('.');
				}
				last = System.currentTimeMillis();
			}

			int cx = getWidth() / 2;
			int cy = getHeight() / 2;

			g.setFont(new Font("Arial", Font.BOLD, 18));
			FontMetrics fm = g.getFontMetrics();
			Rectangle2D rect = fm.getStringBounds(sb.toString(), g);
			g.setColor(Color.BLACK);
			g.drawString(sb.toString(), cx - (int) (rect.getWidth() / 2), cy
					- (int) (rect.getHeight() / 2));
		} else if (loaded) {
			BufferedImage currimg = null;
			BufferedImage[] rscs = this.rscs.get();
			if (v3d) {
				switch (currFrame) {
				case V3DBundle.TOP:
					currimg = rscs[V3DBundle.TOP];
					break;
				case V3DBundle.BOTTOM:
					currimg = rscs[V3DBundle.BOTTOM];
					break;
				case V3DBundle.LEFT:
					currimg = rscs[V3DBundle.LEFT];
					break;
				case V3DBundle.RIGHT:
					currimg = rscs[V3DBundle.RIGHT];
					break;
				case V3DBundle.FRONT:
					currimg = rscs[V3DBundle.FRONT];
					break;
				case V3DBundle.BACK:
					currimg = rscs[V3DBundle.BACK];
				}
			} else {
				if (currFrame < rscs.length && currFrame >= 0) {
					currimg = rscs[currFrame];
				}
			}

			if (currimg != null) {
				int x, y;
				/*
				 * boolean aspectX = currimg.getWidth() >= currimg.getHeight();
				 * x = aspectX ? 0 : (width - currimg.getWidth()) / 2; y =
				 * aspectX ? (height - currimg.getHeight()) / 2 : 0;
				 */
				x = (width - currimg.getWidth()) / 2;
				y = (height - currimg.getHeight()) / 2;
				g.drawImage(currimg, x, y, currimg.getWidth(),
						currimg.getHeight(), null);
			}
		}
	}

	public void setImageData(String[] rscNames) {
		v3d = false;
		this.rscNames = rscNames;
		this.bundle = null;
		loader.reset();
		if (rscNames != null && rscNames.length > 0) {
			repaint();
		}
	}

	public void setImageData(V3DBundle bundle) {
		v3d = true;
		this.bundle = bundle;
		this.rscNames = bundle.getFinalImageArray();
		loader.reset();
		if (rscNames != null && rscNames.length > 0) {
			repaint();
		}
	}

	public void setImageDataFromArtifact(Artifact a) {
		if (a.is3DSupported()) {
			setImageData(a.bundle);
		} else {
			setImageData(a.filenames);
		}
	}

	/**
	 * Advances to the next image if in multi-image mode (does nothing if V3D or
	 * single-image).
	 */
	public void nextFrame() {
		if (!v3d) {
			updateFrame(FRAME_RIGHT);
			repaint();
		}
	}

	/**
	 * Regresses to the previous image if in multi-image mode (does nothing if
	 * V3D or single-image).
	 */
	public void prevFrame() {
		if (!v3d) {
			updateFrame(FRAME_LEFT);
			repaint();
		}
	}

	private void updateFrame(int dir) {
		System.out.println(v3d);
		int prevFrame = currFrame;
		if (v3d && loaded) {
			switch (dir) {

			case FRAME_UP:
				if (currFrame == V3DBundle.BOTTOM || currFrame == V3DBundle.TOP) {
					currFrame = (currFrame == V3DBundle.TOP) ? V3DBundle.BACK
							: V3DBundle.FRONT;
				} else {
					currFrame = V3DBundle.TOP;
				}
				break;
			case FRAME_DOWN:
				if (currFrame == V3DBundle.BOTTOM || currFrame == V3DBundle.TOP) {
					currFrame = (currFrame == V3DBundle.TOP) ? V3DBundle.FRONT
							: V3DBundle.BACK;
				} else {
					currFrame = V3DBundle.BOTTOM;
				}
				break;
			case FRAME_LEFT:
				if (currFrame == V3DBundle.BOTTOM || currFrame == V3DBundle.TOP) {
					currFrame = V3DBundle.LEFT;
				} else {
					currFrame -= (currFrame > 0) ? 1 : -3;
				}
				break;
			case FRAME_RIGHT:
				if (currFrame == V3DBundle.BOTTOM || currFrame == V3DBundle.TOP) {
					currFrame = V3DBundle.RIGHT;
				} else {
					currFrame += (currFrame < 3) ? 1 : -3;
				}
			}
		} else if (loaded) {
			switch (dir) {

			case FRAME_RIGHT:
				if (currFrame < (rscs.get().length - 1)) {
					currFrame++;
				} else {
					currFrame = 0;
				}
				break;
			case FRAME_LEFT:
				if (currFrame > 0) {
					currFrame--;
				} else {
					currFrame = rscs.get().length - 1;
				}
			}
		}

		if (v3d) {
			/*
			 * Revert the frame change if the top or bottom view isn't supported
			 * by this bundle. N.B. This isn't a good implementation. The method
			 * up to this point doesn't check for this... so it has to be done
			 * here as an "oh shit" maneuver...
			 */
			switch (currFrame) {
			case V3DBundle.TOP:
				if (bundle.getBundleType() != V3DBundle.BUNDLE_TYPE_360
						&& bundle.getBundleType() != V3DBundle.BUNDLE_TYPE_TOP) {
					currFrame = prevFrame;
				}
				break;
			case V3DBundle.BOTTOM:
				if (bundle.getBundleType() != V3DBundle.BUNDLE_TYPE_360
						&& bundle.getBundleType() != V3DBundle.BUNDLE_TYPE_BOTTOM) {
					currFrame = prevFrame;
				}
			}
		}

		repaint();
	}

	/**
	 * Tracks mouse movement and issues ordered events based on what direction
	 * the mouse is being dragged in. The default sensitivity value is 100.
	 * 
	 * @author Brian Groenke
	 * 
	 */
	private class V3DHandler extends MouseAdapter {

		volatile int sens = 100; // default sensitivity is 100 pixels

		int fx, fy, deltaX, deltaY;
		boolean init;

		@Override
		public void mouseDragged(MouseEvent e) {
			if (init && v3d) {
				deltaX = e.getX() - fx;
				deltaY = e.getY() - fy;

				if (deltaX > sens) {
					updateFrame(FRAME_LEFT);
					resetAxisX(e);
				} else if (deltaX < -sens) {
					updateFrame(FRAME_RIGHT);
					resetAxisX(e);
				}

				if (deltaY > sens) {
					updateFrame(FRAME_UP);
					resetAxisY(e);
				} else if (deltaY < -sens) {
					updateFrame(FRAME_DOWN);
					resetAxisY(e);
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (!v3d) {
				updateFrame(FRAME_RIGHT);
				return;
			}
			init = true;
			fx = e.getX();
			fy = e.getY();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			resetAxisX(e);
			resetAxisY(e);
			init = false;
		}

		/**
		 * Set the amount of pixels the mouse drag should cover before the panel
		 * responds.
		 * 
		 * @param sensitivity
		 */
		public void setSensitivity(int sensitivity) {
			if (sensitivity > 0) {
				sens = sensitivity;
			}
		}

		private void resetAxisX(MouseEvent e) {
			fx = e.getX();
			deltaX = 0;
		}

		private void resetAxisY(MouseEvent e) {
			fy = e.getY();
			deltaY = 0;
		}
	}

	/**
	 * Responsible for loading the images in V3DPanel from the given
	 * DataProvider.
	 * 
	 * @author Brian Groenke
	 * 
	 */
	private class ImageLoader implements Runnable {

		volatile boolean failed, loading = false;

		@Override
		public void run() {
			if (loaded) {
				loaded = false;
			}
			loading = true;
			if (rscs != null) {
				rscs.clear();
			}
			rscs = new SoftReference<BufferedImage[]>(
					new BufferedImage[rscNames.length]); // an attempt to prevent any kind of memory leak
			                                             // in loaded image data - the effects of this aren't certain.
			SoftReference<BufferedImage[]> ref = rscs;
			BufferedImage[] rscs = ref.get();
			if (failed) {
				failed = false;
			}
			if (v3d) {
				if (rscs.length != 6 && rscs.length != 4) {
					throw (new IndexOutOfBoundsException(
							"Illegal V3D resource count: " + rscs.length));
				}
				try {
					System.out.println("Reading image data...");
					if (!provider.isAvailable()) {
						failed = true;
						loaded = false;
						loading = false;
						JOptionPane.showMessageDialog(instance,
								provider.getProtocolName()
										+ " provider was not available.",
								"Failed to load image data",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					rscs[V3DBundle.FRONT] = provider
							.loadImage(rscNames[V3DBundle.FRONT]);
					rscs[V3DBundle.RIGHT] = provider
							.loadImage(rscNames[V3DBundle.RIGHT]);
					rscs[V3DBundle.BACK] = provider
							.loadImage(rscNames[V3DBundle.BACK]);
					rscs[V3DBundle.LEFT] = provider
							.loadImage(rscNames[V3DBundle.LEFT]);
					if (rscs.length == 6) {
						rscs[V3DBundle.BOTTOM] = provider
								.loadImage(rscNames[V3DBundle.BOTTOM]);
						rscs[V3DBundle.TOP] = provider
								.loadImage(rscNames[V3DBundle.TOP]);
					}
					for (int i = 0; i < rscs.length; i++) {
						System.out.println("Configuring image " + (i+1) + " of " + rscs.length);
						if (rscs[i] == null) {
							continue;
						}
						int wt, ht;
						 /*
						 * If the image is too big for the panel, scale it to maximum
						 * possible size (keeping aspect ratio)
						 */
						if ((wt = rscs[i].getWidth()) > width | (ht = rscs[i].getHeight()) > height) {
							double ratio = (double) wt / (double) ht;
							Dimension newSize;
							if (width >= height) {
								int nw = (int) Math.round(height * ratio);
								newSize = new Dimension(nw, height);
							} else {
								int nh = (int) Math.round(width * ratio);
								newSize = new Dimension(width, nh);
							}
							BufferedImage scaled = new BufferedImage(
									newSize.width, newSize.height,
									BufferedImage.TYPE_INT_ARGB);
							scaled.getGraphics()
									.drawImage(
											rscs[i].getScaledInstance(
													newSize.width,
													newSize.height,
													Image.SCALE_SMOOTH), 0, 0,
											newSize.width, newSize.height, null);
							rscs[i] = scaled;
						}
					}
					System.out.println("done");
					loaded = true;
					repaint();
				} catch (IOException e) {
					e.printStackTrace();
					failed = true;
					loaded = false;
					JOptionPane.showMessageDialog(instance,
							"An I/O error occurred:\n" + e.toString(),
							"Failed to load image data",
							JOptionPane.ERROR_MESSAGE);
				} finally {
					loading = false;
				}
			} else {
				// NOTE that the repetitive nature of this if/else scheme is not ideal and should be candidate
				// for future improvement.
				System.out.println("Reading image data...");
				if (!provider.isAvailable()) {
					failed = true;
					loaded = false;
					loading = false;
					JOptionPane.showMessageDialog(instance,
							provider.getProtocolName()
									+ " provider was not available.",
							"Failed to load image data",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				for (int i = 0; i < rscs.length; i++) {
					System.out.println("Configuring image " + (i+1) + " of " + rscs.length);
					try {
						rscs[i] = provider.loadImage(rscNames[i]);
						if (rscs[i] == null) {
							continue;
						}
					} catch (IOException e) {
						e.printStackTrace();
						e.printStackTrace();
						failed = true;
						loaded = false;
						JOptionPane.showMessageDialog(instance,
								"An I/O error occurred:\n" + e.toString(),
								"Failed to load image data",
								JOptionPane.ERROR_MESSAGE);
						loading = false;
						return;
					}
					int wt, ht;
					 /*
					 * If the image is too big for the panel, scale it to maximum
					 * possible size (keeping aspect ratio)
					 */
					if ((wt = rscs[i].getWidth()) > width | (ht = rscs[i].getHeight()) > height) {
						double ratio = (double) wt / (double) ht;
						Dimension newSize;
						if (width >= height) {
							int nw = (int) Math.round(height * ratio);
							newSize = new Dimension(nw, height);
						} else {
							int nh = (int) Math.round(width * ratio);
							newSize = new Dimension(width, nh);
						}
						BufferedImage scaled = new BufferedImage(newSize.width,
								newSize.height, BufferedImage.TYPE_INT_ARGB);
						scaled.getGraphics().drawImage(
								rscs[i].getScaledInstance(newSize.width,
										newSize.height, Image.SCALE_SMOOTH), 0,
								0, newSize.width, newSize.height, null);
						rscs[i] = scaled;
					}
				}
				System.out.println("done");
				loading = false;
				loaded = true;
				repaint();
			}
		}

		public void reset() {
			loaded = false;
			loading = false;
			failed = false;
			currFrame = 0;
		}
	}
}
