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
import java.io.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.text.*;

import org.madeirahs.editor.main.*;
import org.madeirahs.editor.net.*;
import org.madeirahs.shared.*;
import org.madeirahs.shared.Artifact.StringField;
import org.madeirahs.shared.Artifact.TimeField;
import org.madeirahs.shared.database.*;
import org.madeirahs.shared.provider.*;
import org.madeirahs.shared.time.*;

public class LoadUI extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 751248625624825979L;

	private static final String LOCAL_TAB_NAME = "Local",
			SERVER_TAB_NAME = "Server";

	private static File lastLocation;

	LoadUI inst = this;
	MainUI parent;
	JPanel buttons;
	JTabbedPane views;
	Box panels;
	JFileChooser lfiles;
	ServerView sfiles;
	JButton load, delete, cancel;
	AWTEventListener mouseInput;
	Database db;

	private boolean changesMade;

	public LoadUI(MainUI parent) {
		super(parent);
		this.parent = parent;
		setTitle("Load Artifact");
		this.addWindowListener(new OnCloseDialog());
		views = new JTabbedPane();
		lfiles = new JFileChooser(AppSupport.SAVE_DIR);
		lfiles.setFileFilter(new FileNameExtensionFilter(
				"Serialized Artifacts", "ser"));
		lfiles.setControlButtonsAreShown(false);
		sfiles = new ServerView();
		views.addTab(LOCAL_TAB_NAME, lfiles);
		views.add(SERVER_TAB_NAME, sfiles);
		views.setBorder(new EmptyBorder(5, 5, 5, 5));
		buttons = new JPanel();
		((FlowLayout) buttons.getLayout()).setAlignment(FlowLayout.RIGHT);
		load = new JButton("Load");
		load.addActionListener(new OnLoad());
		delete = new JButton("Delete");
		delete.addActionListener(new OnDelete());
		cancel = new JButton("Cancel");
		cancel.addActionListener(new OnCancel());
		buttons.add(load);
		buttons.add(delete);
		buttons.add(cancel);
		panels = new Box(BoxLayout.Y_AXIS);
		panels.add(buttons);
		add(BorderLayout.CENTER, views);
		add(BorderLayout.SOUTH, panels);
		addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				Toolkit.getDefaultToolkit().removeAWTEventListener(mouseInput);
			}

			@Override
			public void windowGainedFocus(WindowEvent e) {
				Toolkit.getDefaultToolkit().addAWTEventListener(mouseInput,
						AWTEvent.MOUSE_EVENT_MASK);
			}
		});
	}

	private void loadLocal(File f) throws FileNotFoundException, IOException,
	ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
		SavedArtifact loaded = (SavedArtifact) ois.readObject();
		parent.loadArtifactFromSystem(loaded);
		dispose();
	}

	private void loadRemote(Artifact artifact, FTPProvider prov) {
		if (artifact == null || prov == null) {
			return;
		}
		parent.loadArtifactFromServer(artifact, prov);
		dispose();
	}

	private class OnLoad implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (views.getSelectedIndex() == 0) { // Local
				File f = lfiles.getSelectedFile();
				if (f != null) {
					try {
						loadLocal(f);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(inst,
								"Error loading file:\n" + e1.toString(),
								"I/O Error", JOptionPane.ERROR_MESSAGE);
					} catch (ClassNotFoundException e1) {
						JOptionPane
						.showMessageDialog(
								inst,
								"Serialized object not of expected type org.madeirahs.shared.database.SavedArtifact\n"
										+ e1.toString(),
										"Unrecognized Class Type",
										JOptionPane.ERROR_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(inst, "Please select a file");
				}
			} else if (views.getSelectedIndex() == 1) { // Server
				if (sfiles.selected == null) {
					JOptionPane.showMessageDialog(inst,
							"Please select an Artifact");
					return;
				}

				loadRemote(sfiles.pamap.get(sfiles.selected),
						ServerFTP.getProvider());
			}
		}

	}

	class OnDelete implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (views.getSelectedIndex() == 0) { // Local
				File f = lfiles.getSelectedFile();
				if (f != null) {
					int reply = JOptionPane.showConfirmDialog(inst,
							"Delete this Artifact from the file system?",
							"Confirm Delete", JOptionPane.YES_NO_OPTION);
					if (reply == JOptionPane.NO_OPTION) {
						return;
					}
					f.delete();
					lfiles.setFileView(lfiles.getFileView());
					lfiles.validate();
					lfiles.repaint();
				} else {
					JOptionPane.showMessageDialog(parent,
							"Please select a file");
				}
			} else if (views.getSelectedIndex() == 1) { // Server
				if (sfiles.selected == null) {
					JOptionPane.showMessageDialog(parent,
							"Please select an Artifact");
					return;
				}

				int reply = JOptionPane.showConfirmDialog(parent,
						"Also remove all resources associated with this artifact?\nNote: deletion of the artifact and resource data cannot be reversed.",
						"Confirm Submission Removal", JOptionPane.YES_NO_CANCEL_OPTION);
				if (reply == JOptionPane.CANCEL_OPTION) {
					return;
				}

				Artifact a = sfiles.pamap.get(sfiles.selected);
				db.remove(a);

				FTPProvider prov = ServerFTP.getProvider();
				if(reply == JOptionPane.YES_OPTION)
					for (String s : a.filenames) {
						prov.delete(s);
					}

				sfiles.loadList();
				sfiles.validate();

				changesMade = true;
			}
		}

	}

	class OnCancel implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			inst.dispose();
		}
	}

	private class ServerView extends JPanel {

		final String[] qstr = new String[] {"Title", "Donor", "Description", "Medium", "Accession Number"},
				qtime = new String[] {"Submission Date", "Object Date"};

		Box list;
		JTextField searchField;
		JScrollPane listScroll;
		JComboBox qtype;
		JLabel dbinf;
		JPanel selected;
		HashMap<JPanel, Artifact> pamap = new HashMap<JPanel, Artifact>();
		HashMap<String, StringField> fmap1 = new HashMap<String, StringField>();
		HashMap<String, TimeField> fmap2 = new HashMap<String, TimeField>();
		private Runnable runLoader;
		private SearchChangeListener scl;

		public ServerView() {
			super(new BorderLayout());
			searchField = new JTextField(20);
			listScroll = new JScrollPane();
			initList();
			initComboBox();
			JPanel north = new JPanel(new BorderLayout());
			JPanel txthld = new JPanel();
			((FlowLayout) txthld.getLayout()).setAlignment(FlowLayout.LEFT);
			txthld.add(new JLabel("Search: "));
			txthld.add(searchField);
			txthld.add(qtype);
			north.add(BorderLayout.WEST, txthld);
			ImageIcon ricon = new ImageIcon(this.getClass().getClassLoader()
					.getResource(AppSupport.JAR_PKG_UI + "/reload.png"));
			JButton btn = new JButton(ricon);
			btn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					new Thread(runLoader).start();
				}

			});

			dbinf = new JLabel();

			north.add(BorderLayout.EAST, btn);
			add(BorderLayout.NORTH, north);
			add(BorderLayout.CENTER, listScroll);
			add(BorderLayout.SOUTH, dbinf);
			runLoader = new Runnable() {

				@Override
				public void run() {
					ProgressMonitor prog = new ProgressMonitor(inst,
							"Reloading Databse", "", 0, 101);
					try {
						db = Database.getInstance(ServerFTP.dbDir,
								ServerFTP.getProvider(), prog);
						dbinf.setText("Database last edited by \"" + ((db.getUser() == null) ? "Unknown":db.getUser()) + "\" on " + new Date(db.getTimestamp()).toString());

						scl = new SearchChangeListener(db);
						searchField.addCaretListener(scl);
					} catch (ClassCastException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(inst, "Failed to retrieve database from server:\n"+e.toString(), "I/O Error", JOptionPane.ERROR_MESSAGE);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} finally {
						prog.close();
					}
					sfiles.loadList();
				}

			};
			new Thread(runLoader).start();
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 7759938332501812139L;

		final Font nf = new Font("Century", Font.BOLD, 16);
		final Font lf = new Font("Century", Font.ITALIC, 14);
		final Font sf = new Font("Century", Font.PLAIN, 12);
		final Font ssf = new Font("Terminal", Font.PLAIN, 10);

		public void loadList() {
			initList();
			pamap.clear();

			if (db == null) {
				ProgressMonitor prog = new ProgressMonitor(inst,
						"Retrieving Database", "", 0, 101);
				try {
					db = Database.getInstance(ServerFTP.dbDir,
							ServerFTP.getProvider(), prog);
				} catch (ClassCastException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} finally {
					prog.close();
				}
			}

			if (db == null) {
				return;
			}

			Artifact[] data = db.getData();
			for (Artifact a : data) {
				JPanel pan = buildArtifactInfo(a);
				list.add(pan);
				pamap.put(pan, a);
				list.add(Box.createVerticalStrut(5));
			}

			list.add(Box.createVerticalGlue());
			list.validate();
		}

		private JPanel buildArtifactInfo(Artifact a) {
			JPanel panel = new JPanel(new BorderLayout());
			panel.setOpaque(true);
			panel.setBackground(list.getBackground());
			panel.addMouseListener(new MouseListener() {

				Color pressed = Color.GRAY, sel = Color.LIGHT_GRAY,
						hover = Color.ORANGE, prev, hprev;
				boolean inside;

				@Override
				public void mouseClicked(MouseEvent e) {
					//
				}

				@Override
				public void mousePressed(MouseEvent e) {
					prev = e.getComponent().getBackground();
					e.getComponent().setBackground(pressed);
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					e.getComponent().setBackground((inside) ? sel : hprev);
					if (selected != null) {
						selected.setBackground(hprev);
					}
					selected = (JPanel) e.getComponent();
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					hprev = e.getComponent().getBackground();
					if (e.getComponent() != selected) {
						e.getComponent().setBackground(hover);
					}
					inside = true;
				}

				@Override
				public void mouseExited(MouseEvent e) {
					if (e.getComponent() != selected) {
						e.getComponent().setBackground(hprev);
					}
					inside = false;
				}

			});
			Box west = new Box(BoxLayout.Y_AXIS);
			west.setFocusable(false);
			Box east = new Box(BoxLayout.Y_AXIS);
			east.setFocusable(false);
			JLabel title = new JLabel(a.title);
			title.setFont(lf);
			JLabel donor = new JLabel(a.donor);
			donor.setFont(sf);
			JLabel sd = new JLabel(a.subDate.toString());
			sd.setFont(sf);
			JLabel od = new JLabel(a.objDate.toString());
			od.setFont(sf);
			JLabel accNum = new JLabel(a.accNum);
			accNum.setFont(ssf);
			west.add(title);
			west.add(donor);
			west.add(accNum);
			east.add(sd);
			east.add(od);
			panel.add(BorderLayout.WEST, west);
			panel.add(BorderLayout.EAST, east);
			panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel
					.getPreferredSize().height));
			panel.validate();
			return panel;
		}

		private void initList() {
			list = new Box(BoxLayout.Y_AXIS);
			list.setOpaque(true);
			list.setBackground(Color.WHITE);
			list.setBorder(new EmptyBorder(10, 5, 5, 5));
			listScroll.setViewportView(list);
			validate();
		}

		private void initComboBox() {
			String[] combo = new String[qstr.length+qtime.length];
			for(int i=0;i<qstr.length;i++) {
				switch(i) {
				case 0:
					fmap1.put(qstr[i], StringField.TITLE);
					break;
				case 1:
					fmap1.put(qstr[i], StringField.DONOR);
					break;
				case 2:
					fmap1.put(qstr[i], StringField.DESCRIPTION);
					break;
				case 3:
					fmap1.put(qstr[i], StringField.MEDIUM);
					break;
				case 4:
					fmap1.put(qstr[i], StringField.ACCESSION_NUMBER);
				}
				combo[i] = qstr[i];
			}

			for(int i=0, ii = qstr.length;i<qtime.length;i++, ii++) {
				switch(i) {
				case 0:
					fmap2.put(qtime[i], TimeField.SUBMISSION_DATE);
					break;
				case 1:
					fmap2.put(qtime[i], TimeField.OBJECT_DATE);
				}
				combo[ii] = qtime[i];
			}

			qtype = new JComboBox(combo);
			qtype.addItemListener(new ItemListener() {
				Object lastSel = qtype.getSelectedItem();
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(!qtype.getSelectedItem().equals(lastSel)) {
						lastSel = qtype.getSelectedItem();
						scl.update(searchField.getText(), true);
					}
				}
			});
		}

		private StringField getStringType(String sel) {
			return fmap1.get(sel);
		}

		private TimeField getTimeType(String sel) {
			return fmap2.get(sel);
		}

		private class SearchChangeListener implements CaretListener {

			Database db;
			String before;

			public SearchChangeListener(Database db) {
				if(db == null)
					throw(new NullPointerException("database cannot be null"));
				this.db = db;
			}

			@Override
			public void caretUpdate(CaretEvent e) {
				update(((JTextComponent)e.getSource()).getText(), false);
			}

			public void update(String now, boolean ignoreLast) {

				if(before != null && now.isEmpty()) {
					loadList();
					before = now;
					return;
				} else if((before != null && (now.equals(before) && !ignoreLast)) || now.isEmpty()) {
					// if we haven't gotten a before value yet, the value is unchanged, or there is no entry, cancel the update.
					// ignoreLast specifies whether or not this if statement should consider the query being unchanged a reason to cancel the update.
					before = now;
					return;
				}

				before = now;

				String sel = (String) qtype.getSelectedItem();
				StringField sf = getStringType(sel);
				TimeField tf = getTimeType(sel);
				String fieldName = (sf == null) ? tf.getFieldName():sf.getFieldName();
				Object qobj = now;
				if(sf == null) {
					DateTime dt = new DateTime(Calendar.getInstance().getTime(), DateFormat.getDateInstance());
					dt.forcedValue = now;
					qobj = dt;
				}
				
				Artifact query = Artifact.createGenericArtifact();
				try {
					query.getClass().getField(fieldName).set(query, qobj);
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
				} catch (SecurityException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				} catch (NoSuchFieldException e1) {
					e1.printStackTrace();
				}

				Artifact[] results = null;
				if(sf != null)
					results = db.searchByField(sf, query);
				else if(tf != null)
					results = db.searchByFieldHybrid(tf, query);

				initList();
				pamap.clear();
				for (Artifact a : results) {
					JPanel pan = buildArtifactInfo(a);
					list.add(pan);
					pamap.put(pan, a);
					list.add(Box.createVerticalStrut(5));
				}

				list.add(Box.createVerticalGlue());
				list.validate();
			}
		}

	}

	private class OnCloseDialog extends WindowAdapter {

		@Override
		public void windowClosing(WindowEvent e) {
			if (!changesMade) {
				dispose();
				return;
			}
			int reply = JOptionPane.showConfirmDialog(inst,
					"Commit changes to database?", "Confirm Database Sync",
					JOptionPane.YES_NO_CANCEL_OPTION);
			switch (reply) {
			case JOptionPane.YES_OPTION:
				new Thread(new Runnable() {

					@Override
					public void run() {
						ProgressMonitor monitor = new ProgressMonitor(
								inst,
								"Syncing database with FTP server.  Please wait...",
								"", 0, 101);
						monitor.setMillisToDecideToPopup(0);
						monitor.setMillisToPopup(0);
						try {
							db.sync(ServerFTP.dbDir, ServerFTP.dbArchiveDir,
									ServerFTP.getProvider(), Settings.usr, monitor);
							ServerFTP.checkArchiveLimit();
						} catch (IOException e1) {
							e1.printStackTrace();
							JOptionPane.showMessageDialog(inst,
									"An error occurred while trying to sync the databse:\n"
											+ e1.toString(), "I/O Error",
											JOptionPane.ERROR_MESSAGE);
						}
						monitor.close();
					}

				}).start();

			case JOptionPane.NO_OPTION:
				dispose();
				break;
			default:

			}
		}

	}

}
