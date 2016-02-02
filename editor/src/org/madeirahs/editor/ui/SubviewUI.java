/*
 *  The MHS-Collections Project editor is intended for use by Historical Society members
 *  to edit, review and upload artifact information.
 *  Copyright (c) 2012-2016 Madeira Historical Society (developed by Brian Groenke)
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

import javax.swing.*;
import javax.swing.border.*;

import org.apache.commons.net.ftp.*;
import org.madeirahs.editor.main.*;
import org.madeirahs.editor.net.*;
import org.madeirahs.shared.*;
import org.madeirahs.shared.database.*;
import org.madeirahs.shared.provider.*;

public class SubviewUI extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4448709123274756618L;

	private boolean changesMade;

	SubviewUI lref = this;
	MainUI parent;
	JList list;
	JPanel btns;
	JButton load, accept, remove;
	Database dbi;
	FTPProvider prov;

	final Font nf = new Font("Century", Font.BOLD, 16);
	final Font lf = new Font("Century", Font.ITALIC, 14);
	final Font sf = new Font("Century", Font.PLAIN, 12);
	final Font ssf = new Font("Terminal", Font.PLAIN, 10);

	/**
	 * Constructs a new Submissions viewing UI. It is recommended that you call
	 * this from the UI thread.
	 * 
	 * @param owner
	 * @param prov
	 */
	public SubviewUI(MainUI owner, final FTPProvider prov) {
		super(owner, "Database Submissions");
		parent = owner;
		this.prov = prov;
		final SubviewUI inst = this;
		list = new JList();
		list.setBorder(new EmptyBorder(5, 5, 5, 5));
		btns = new JPanel();
		((FlowLayout) btns.getLayout()).setAlignment(FlowLayout.RIGHT);
		load = new JButton("Load");
		load.addActionListener(new LoadListener());
		accept = new JButton("Accept");
		accept.addActionListener(new AcceptListener());
		remove = new JButton("Remove");
		remove.addActionListener(new RemoveListener());
		btns.add(load);
		btns.add(accept);
		btns.add(remove);
		add(BorderLayout.CENTER, list);
		add(BorderLayout.SOUTH, btns);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new OnCloseDialog());
		setSize(300, 300);
		setLocationRelativeTo(parent);
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					ProgressMonitor prog = new ProgressMonitor(inst,
							"Retrieving Database", "", 0, 101);
					prog.setMillisToDecideToPopup(0);
					prog.setMillisToPopup(0);
					dbi = Database.getInstance(ServerFTP.dbDir, prov, prog);
					prog.close();

					try {
						loadListData();
					} catch (IllegalServerStateException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, "Server communication error.  Please contact server admin.\n"+e.toString(), "Bad Response", JOptionPane.ERROR_MESSAGE);
					}

					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							list.validate();
						}

					});
				} catch (ClassCastException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(parent,
							"Error downloading database:\n" + e.toString(),
							"I/O Error", JOptionPane.ERROR_MESSAGE);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}

		}).start();
	}

	private void loadListData() throws IllegalServerStateException {
		if (!prov.exists(ServerFTP.subDir)) {
			throw (new IllegalServerStateException(
					"FTPProvider doesn't recognize submissions directory"));
		}

		FTPFile[] files = prov.listFiles(ServerFTP.subDir);
		String[] filenames = new String[files.length];
		for (int i = 0; i < filenames.length; i++) {
			filenames[i] = files[i].getName();
		}
		list.setListData(filenames);
	}

	private Artifact readSubmission(String str) {
		try {
			InputStream in = prov.getInputStream(ServerFTP.subDir + str);
			if (in == null) {
				JOptionPane.showMessageDialog(parent,
						"Failed to locate file on remote server", "I/O Error",
						JOptionPane.ERROR_MESSAGE);
				return null;
			}
			ObjectInputStream objIn = new ObjectInputStream(in);
			Artifact a = (Artifact) objIn.readObject();
			objIn.close();
			return a;
		} catch (IOException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(
					parent,
					"An error occurred while retrieving the file:\n"
							+ e1.toString(), "I/O Error",
							JOptionPane.ERROR_MESSAGE);
			return null;
		} catch (ClassNotFoundException e2) {
			e2.printStackTrace();
			JOptionPane.showMessageDialog(
					parent,
					"An error occurred while retrieving the file:\n"
							+ e2.toString(), "This Shouldn't Happen",
							JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	private class LoadListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					Object[] arr = list.getSelectedValues();
					if (arr.length == 0) {
						return;
					}
					Artifact a = readSubmission((String) arr[0]);
					if (a == null) {
						return;
					}
					parent.loadArtifactFromServer(a, prov);
				}

			}).start();
		}

	}

	private class AcceptListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					int reply = JOptionPane.showConfirmDialog(parent,
							"Accept submission(s) into the database?",
							"Confirm Submission", JOptionPane.YES_NO_OPTION);
					if (reply == JOptionPane.NO_OPTION) {
						return;
					}
					for(Object o:list.getSelectedValues()) {
						if (o == null) {
							return;
						}
						Artifact a = readSubmission((String) o);
						if (a == null) {
							return;
						}
						try {
							prov.delete(ServerFTP.subDir + a.accNum
									+ MainUI.ARTIFACT_EXT);
							dbi.add(a);
							if (!changesMade) {
								changesMade = true;
							}
						} catch (DuplicateArtifactException e1) {
							int resp = JOptionPane
									.showConfirmDialog(
											parent,
											"Artifact already exists in database.  Overwrite it?",
											"Confirm Overwrite",
											JOptionPane.YES_NO_OPTION);
							if (resp == JOptionPane.YES_OPTION) {
								dbi.addAndOverwrite(a);
								if (!changesMade) {
									changesMade = true;
								}
							}
						} finally {
							try {
								loadListData();
							} catch (IllegalServerStateException e) {
								e.printStackTrace();
							}
						}
					}
				}

			}).start();
		}

	}

	private class RemoveListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					int reply = JOptionPane.showConfirmDialog(parent,
							"Also remove all resources associated with submission(s)?\nNote: deletion of the submission and resource data cannot be reversed.",
							"Confirm Submission Removal", JOptionPane.YES_NO_CANCEL_OPTION);
					if (reply == JOptionPane.CANCEL_OPTION) {
						return;
					}
					for(Object o:list.getSelectedValues()) {
						if (o == null) {
							return;
						}
						Artifact a = readSubmission((String) o);
						if (a == null) {
							return;
						}
						prov.delete(ServerFTP.subDir + a.accNum
								+ MainUI.ARTIFACT_EXT);
						if(reply == JOptionPane.YES_OPTION)
							for (String s : a.filenames) {
								prov.delete(s);
							}

						try {
							loadListData();
						} catch (IllegalServerStateException e) {
							e.printStackTrace();
						}
					}
				}

			}).start();
		}

	}

	private class OnCloseDialog extends WindowAdapter {

		@Override
		public void windowClosing(WindowEvent e) {
			if (!changesMade) {
				dispose();
				return;
			}
			int reply = JOptionPane.showConfirmDialog(lref,
					"Commit changes to database?", "Confirm Database Sync",
					JOptionPane.YES_NO_CANCEL_OPTION);
			switch (reply) {
			case JOptionPane.YES_OPTION:
				new Thread(new Runnable() {

					@Override
					public void run() {
						ProgressMonitor monitor = new ProgressMonitor(
								lref,
								"Syncing database with FTP server.  Please wait...",
								"", 0, 101);
						try {
							dbi.sync(ServerFTP.dbDir,
									ServerFTP.dbArchiveDir,
									ServerFTP.getProvider(), Settings.usr, monitor);
							ServerFTP.checkArchiveLimit();
						} catch (IOException e1) {
							e1.printStackTrace();
							JOptionPane.showMessageDialog(lref,
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
