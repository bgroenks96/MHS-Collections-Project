/*
 *  The MHS-Collections Project editor is intended for use by Historical Society members
 *  to edit, review and upload artifact information.
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
 */

package org.madeirahs.editor.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import org.madeirahs.editor.main.*;
import org.madeirahs.editor.net.*;
import org.madeirahs.shared.*;
import org.madeirahs.shared.misc.*;
import org.madeirahs.shared.provider.*;

/**
 * Handles I/O and displays a UI for uploading artifact data to the server. The
 * upload process begins once <code>start()</code> is called. After that the
 * UploadUI object is no longer usable. <code>start()</code> may only be called
 * once, and any subsequent calls will be ignored.
 * 
 * @author Brian Groenke
 * 
 */
public class UploadUI {

	public static final String PAR_EXT = ".par",
			UPLOAD_ICON = AppSupport.JAR_PKG_UI + "/upload.png";
	public static final int RSC_CLEVEL = 9;

	private DataProvider prov;
	private FTPProvider ftp;
	private ProgressDialog prog;
	private Artifact a;
	private String[] fileArrCopy;
	private boolean finis;

	/**
	 * Create a new UploadUI. The UI will not be shown until you call
	 * <code>start()</code>.
	 * 
	 * @param prov
	 *            the DataProvider from which resources will be loaded
	 * @param parentComponent
	 * @param artifact
	 * @param origs
	 * @throws IOException 
	 */
	public UploadUI(DataProvider prov, Frame parentComponent,
			Artifact artifact, String[] origs) throws IOException {
		prog = new ProgressDialog(parentComponent, "Upload Service",
				"Uploading Artifact");
		this.prov = prov;
		this.a = artifact;
		ftp = ServerFTP.getProvider();
		if(!ftp.isAvailable())
			throw (new IOException("provider not available"));
		/*
		if (origs != null) {
			for (String s : origs) {
				int ind = Arrays.binarySearch(artifact.filenames, s);
				if (ind < 0) {
					ftp.delete(s);
				}
			}
		}
		*/
		init();
	}

	public void start() {
		if (!finis) {
			prog.setVisible(true);
			upload();
		}
	}

	/**
	 * Checks all the FTP server directories and renames all of the Artifact's
	 * filenames to have the resource directory preceding them.
	 */
	protected void init() {
		ServerFTP.checkDirs();
		fileArrCopy = Arrays.copyOf(a.filenames, a.filenames.length);
		String[] fn = a.filenames;
		for (int i = 0; i < fn.length; i++) {
			String[] pts = fn[i].split("/");
			fn[i] = pts[pts.length - 1];
			fn[i] = ServerFTP.rscDir + fn[i];
		}
	}

	protected void upload() {
		String name = ServerFTP.subDir + a.accNum + PAR_EXT;
		String rname = ServerFTP.subDir + a.accNum + MainUI.ARTIFACT_EXT;
		ObjectOutputStream obj = null;
		try {
			if (ftp.exists(rname)) {
				int reply = JOptionPane
						.showConfirmDialog(
								null,
								"This artifact already exists in submissions.  Overwrite it?",
								"Confirm Overwrite",
								JOptionPane.OK_CANCEL_OPTION);
				if (reply == JOptionPane.OK_OPTION) {
					ftp.delete(rname);
				} else {
					return;
				}
			}
			OutputStream os = ftp.getOutputStream(name);
			if (os == null) {
				throw (new IOException("Unable to establish data streams"));
			}

			obj = new ObjectOutputStream(new OutStreamWrapper(os, Utils.sizeof(a)));
			prog.setNote("<html>Writing artifact data...<br/></html>");

			obj.writeObject(a);
			obj.close();

			prog.setNote("Evaluating resources...");
			prog.setProgress(0);
			Thread.sleep(400); // pause in the interest of clarity

			uploadResources();
			prog.setProgress(100);

			prog.setNote("Finalizing upload...");
			if (!prog.isCanceled()) {
				ftp.rename(name, rname);
				JOptionPane.showMessageDialog(null,
						"Successfully uploaded artifact.");
			}
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Upload failed: " + e.toString(), "I/O Error",
					JOptionPane.ERROR_MESSAGE);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			ftp.delete(name);
			finis = true;
			prog.dispose();
		}
	}

	private static final String PTH = ServerFTP.rscDir;
	private static final int BUFF_SIZE = 8192; // 8 kB - standard CPU/disk
												// transfer

	private void uploadResources() {
		String[] filenames = fileArrCopy;
		if (filenames == null || filenames.length == 0) {
			prog.setProgress(101);
			return;
		}

		long combo = 0;
		double total = 0;
		for (String f : filenames) {
			combo += (prov.sizeOf(f) > 0 && !ftp.exists(PTH + f)) ? prov
					.sizeOf(f) : 0;
		}

		for (String s : filenames) {
			if (!prov.exists(s) || ftp.exists(PTH + s)) {
				continue;
			}
			prog.setNote("<html>Uploading resource <br>" + s + "</html>");
			String[] pts = s.split("/");
			String prt = pts[pts.length - 1];
			prt = PTH + prt + PAR_EXT;
			try {
				BufferedOutputStream out = new BufferedOutputStream(
						ftp.getOutputStream(prt));
				BufferedInputStream in = new BufferedInputStream(
						prov.getInputStream(s));
				byte[] buff = new byte[BUFF_SIZE];
				int len;
				while ((len = in.read(buff)) >= 0 && !prog.isCanceled()) {
					out.write(buff, 0, len);
					out.flush();
					total += len;
					prog.setProgress((int) (Math.round((total / combo) * 100)));
				}

				in.close();
				out.close();

				if (prog.isCanceled()) {
					prog.setNote("Cancelling...");
					for (String str : a.filenames) {
						if (ftp.exists(str + PAR_EXT)) {
							ftp.delete(str + PAR_EXT);
						}
					}
					break;
				}

			} catch (IOException e) {
				JOptionPane.showMessageDialog(
						null,
						"Failed to upload artifact resource " + s + "\n"
								+ e.toString(), "I/O Error",
						JOptionPane.ERROR_MESSAGE);
			} finally {
				if (prog.isCanceled()) {
					return;
				}
			}
		}

		prog.setNote("Finalizing resources...");

		for (String str : a.filenames) {
			if (ftp.exists(str + PAR_EXT)) {
				try {
					ftp.rename(str + PAR_EXT, str);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Actual UI code for the upload dialog window.
	 * @author Brian Groenke
	 *
	 */
	public class ProgressDialog extends JDialog {

		/**
		 * 
		 */
		private static final long serialVersionUID = -9097422980332812916L;

		Frame parent;

		public ProgressDialog(Frame parent, String title, String taskName) {
			super(parent, title);
			progress = new JProgressBar(0, 100);
			task = new JLabel(taskName) {
				/**
				 * 
				 */
				private static final long serialVersionUID = -2149137693748682079L;

				@Override
				public void paintComponent(Graphics g) {
					setMinimumSize(new Dimension(getParent().getWidth()
							- getParent().getWidth() / 6, getHeight()));
					setMaximumSize(new Dimension(getParent().getWidth()
							- getParent().getWidth() / 6, getHeight()));
					setPreferredSize(new Dimension(getParent().getWidth()
							- getParent().getWidth() / 6, getHeight()));
					pack();
					super.paintComponent(g);
				}
			};
			task.setFont(new Font("Arial", Font.BOLD, 12));
			note = new JLabel("Initializing....") {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1592154552364858914L;

				int origHt = 0;

				@Override
				public void paintComponent(Graphics g) {
					if (origHt == 0) {
						origHt = getHeight();
					}
					setMinimumSize(new Dimension(getParent().getWidth()
							- getParent().getWidth() / 6, getHeight()));
					setMaximumSize(new Dimension(getParent().getWidth()
							- getParent().getWidth() / 6, origHt * 2));
					setPreferredSize(new Dimension(getParent().getWidth()
							- getParent().getWidth() / 6, origHt * 2));
					pack();
					super.paintComponent(g);
				}
			};
			Box box = new Box(BoxLayout.Y_AXIS);
			box.setBorder(new EmptyBorder(5, 10, 5, 5));
			box.add(Box.createVerticalStrut(10));
			JPanel tlh = new JPanel();
			((FlowLayout) tlh.getLayout()).setAlignment(FlowLayout.LEFT);
			tlh.add(task);
			box.add(tlh);
			box.add(Box.createVerticalStrut(5));
			JPanel nlh = new JPanel();
			((FlowLayout) nlh.getLayout()).setAlignment(FlowLayout.LEFT);
			nlh.add(note);
			box.add(nlh);
			box.add(Box.createVerticalStrut(10));
			JPanel ph = new JPanel();
			ph.add(progress);
			box.add(ph);
			box.add(Box.createVerticalStrut(10));
			// panel.add(box);
			ImgPanel img = new ImgPanel();
			img.setPreferredSize(new Dimension(75, 100));
			cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					cancelPressed = true;
				}

			});
			JPanel bh = new JPanel();
			bh.add(cancel);
			add(BorderLayout.WEST, img);
			add(BorderLayout.EAST, box);
			add(BorderLayout.SOUTH, bh);
			note.validate();
			note.repaint();
			pack();
			setLocationRelativeTo(parent);
			this.parent = parent;
		}

		private JLabel task, note;
		private JButton cancel;
		private JProgressBar progress;
		private volatile boolean cancelPressed;

		public void setNote(String text) {
			if (text != null) {
				note.setText(text);
			}
			validate();
			note.repaint();
		}

		public boolean isCanceled() {
			return cancelPressed;
		}

		public void setProgress(int value) {
			if (value > 100) {
				dispose();
			} else {
				progress.setValue(value);
			}
		}

		public void setIndeterminate(boolean indeterminate) {
			progress.setIndeterminate(indeterminate);
		}

		/**
		 * Special panel for displaying the upload icon.
		 * @author Brian Groenke
		 *
		 */
		private class ImgPanel extends JPanel {

			/**
			 * 
			 */
			private static final long serialVersionUID = -1006312058697452902L;

			@Override
			public void paintComponent(Graphics g) {
				double wt = getWidth();
				double ht = getHeight();
				Image img = Toolkit.getDefaultToolkit().getImage(
						ClassLoader.getSystemClassLoader().getResource(
								UPLOAD_ICON));
				double nwt = wt / 2;
				double aratio = (double) img.getWidth(null)
						/ (double) img.getHeight(null);
				double nht = aratio * nwt;
				BufferedImage newimg = new BufferedImage((int) nwt, (int) nht,
						BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2 = newimg.createGraphics();
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				g2.drawImage(img, 0, 0, newimg.getWidth(), newimg.getHeight(),
						null);
				g.drawImage(newimg, (int) (wt / 4), (int) (ht / 5), null);
			}
		}
	}

	/**
	 * Wraps any OutputStream and updates the UI with progress values.
	 * @author Brian Groenke
	 *
	 */
	private class OutStreamWrapper extends OutputStream {

		OutputStream stream;
		int total, max;

		OutStreamWrapper(OutputStream out, int max) {
			this.stream = out;
			this.max = max;
		}

		@Override
		public void write(int b) throws IOException {
			stream.write(b);
			total++;
			int p = (int) Math.round(((double) total / max) * 100);
			prog.setProgress(p);
		}

		@Override
		public void close() throws IOException {
			stream.close();
		}
	}
}
