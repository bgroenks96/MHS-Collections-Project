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
import java.util.*;
import java.util.zip.*;

import javax.swing.*;
import javax.swing.event.*;

import org.madeirahs.editor.main.*;
import org.madeirahs.editor.net.*;
import org.madeirahs.shared.database.*;
import org.madeirahs.shared.misc.*;
import org.madeirahs.shared.provider.*;

public class ManageUI extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3582560499724897259L;

	private static final int TEXT_FIELD_LENGTH = 30;
	private static final Dimension ACTION_BUTTON_SIZE = new Dimension(120, 30);

	ManageUI inst = this;
	JTextField usr, server, passcode;
	JButton conn, save, create, restore, remove, adv;
	JPanel serverpl, passcodepl, usrpl, actions, backupOpts;
	Box fields, backups, localpan, archivepan;
	JList local, archives;
	JScrollPane ascroll, lscroll;

	private HashMap<Date, File> localMap = new HashMap<Date, File>();
	private HashMap<Date, String> archiveMap = new HashMap<Date, String>();

	/**
	 * Creates the dialog and all of its UI components.
	 * @param parent
	 * @param title
	 */
	public ManageUI(Frame parent, String title) {
		super(parent, title);
		usr = new JTextField(TEXT_FIELD_LENGTH);
		passcode = new JPasswordField(TEXT_FIELD_LENGTH);
		server = new JTextField(TEXT_FIELD_LENGTH);
		server.setText(ServerFTP.getFtpServerUrl());
		usrpl = new JPanel();
		((FlowLayout)usrpl.getLayout()).setAlignment(FlowLayout.LEFT);
		usrpl.add(usr);
		serverpl = new JPanel();
		((FlowLayout)serverpl.getLayout()).setAlignment(FlowLayout.LEFT);
		serverpl.add(server);
		passcodepl = new JPanel();
		((FlowLayout)passcodepl.getLayout()).setAlignment(FlowLayout.LEFT);
		passcodepl.add(passcode);
		fields = Box.createVerticalBox();

		JPanel ullh = new JPanel();
		((FlowLayout)ullh.getLayout()).setAlignment(FlowLayout.LEFT);
		ullh.add(new JLabel("Login Username"));
		fields.add(ullh);
		fields.add(usrpl);
		JPanel plh = new JPanel();
		((FlowLayout)plh.getLayout()).setAlignment(FlowLayout.LEFT);
		plh.add(new JLabel("Login Password"));
		fields.add(plh);
		fields.add(passcodepl);
		JPanel slh = new JPanel();
		((FlowLayout)slh.getLayout()).setAlignment(FlowLayout.LEFT);
		slh.add(new JLabel("Server Address"));
		fields.add(slh);

		fields.add(serverpl);

		actions = new JPanel();
		((FlowLayout)actions.getLayout()).setAlignment(FlowLayout.LEFT);
		FTPProvider ftp = ServerFTP.getProvider();
		conn = new JButton((ftp != null && ftp.isAvailable()) ? "Disconnect":"Connect");
		conn.setPreferredSize(ACTION_BUTTON_SIZE);
		conn.addActionListener(new ConnectListener());
		actions.add(conn);
		save = new JButton("Save");
		save.setPreferredSize(ACTION_BUTTON_SIZE);
		save.addActionListener(new SaveListener());
		actions.add(save);
		adv = new JButton("Advanced");
		adv.setPreferredSize(ACTION_BUTTON_SIZE);
		adv.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				AdvSettingsUI xui = new AdvSettingsUI(inst, "Advanced Server Interface Settings");
				xui.setVisible(true);
			}

		});
		actions.add(adv);
		fields.add(actions);

		backups = Box.createHorizontalBox();
		ComponentAdapter resize = new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				Dimension size = backups.getSize();
				local.setMinimumSize(new Dimension(size.width / 2 - 10, size.height / 2));
				archives.setMinimumSize(new Dimension(size.width / 2 - 10, size.height / 2));
				backups.validate();
			}


		};
		backups.addComponentListener(resize);
		local = new JList();
		local.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(local.getSelectedIndex() < 0)
					return;
				archives.clearSelection();
			}

		});
		archives = new JList();
		archives.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(archives.getSelectedIndex() < 0)
					return;
				local.clearSelection();
			}

		});
		localpan = Box.createVerticalBox();
		localpan.add(new JLabel("Local Backups"));
		lscroll = new JScrollPane(local);
		lscroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		localpan.add(lscroll);
		archivepan = Box.createVerticalBox();
		archivepan.add(new JLabel("Archives"));
		ascroll = new JScrollPane(archives);
		ascroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		archivepan.add(ascroll);

		backups.add(localpan);
		backups.add(archivepan);

		backupOpts = new JPanel();
		((FlowLayout)backupOpts.getLayout()).setAlignment(FlowLayout.LEFT);
		create = new JButton("Create Local");
		create.setPreferredSize(ACTION_BUTTON_SIZE);
		create.addActionListener(new CreateListener());
		backupOpts.add(create);
		restore = new JButton("Restore");
		restore.setPreferredSize(ACTION_BUTTON_SIZE);
		restore.addActionListener(new RestoreListener());
		backupOpts.add(restore);
		remove = new JButton("Delete");
		remove.setPreferredSize(ACTION_BUTTON_SIZE);
		remove.addActionListener(new RemoveListener());
		backupOpts.add(remove);

		add(BorderLayout.NORTH, fields);
		add(BorderLayout.CENTER, backups);
		add(BorderLayout.SOUTH, backupOpts);
		setSize(600, 600);
		setLocationRelativeTo(parent);

		try {
			loadInfo();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				updateLists();
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						inst.validate();
					}

				});
			}

		}).start();
	}

	/**
	 * Load, sort and render the values/mappings for both lists on the UI.
	 */
	private void updateLists() {
		File bdir = AppSupport.BACKUP_DIR;
		File[] files = bdir.listFiles();
		for(File f:files) {
			String name = f.getName();
			if(!name.endsWith(Database.DB_SUFFIX))
				continue;
			String timestr = name.substring(0, name.indexOf(Database.DB_SUFFIX));
			long millis = Long.parseLong(timestr);
			Date timestamp = new Date(millis);
			localMap.put(timestamp, f);
		}

		TopDownDateSorter sorter = new TopDownDateSorter();

		Date[] lda = localMap.keySet().toArray(new Date[localMap.size()]);
		Arrays.sort(lda, sorter);
		local.setListData(lda);

		FTPProvider ftp = ServerFTP.getProvider();
		if(ftp == null || !ftp.isAvailable()) {
			JOptionPane.showMessageDialog(inst, "Unable to retrieve database archives: FTP provider offline", "Provider Not Available", JOptionPane.WARNING_MESSAGE);
			return;
		}

		String[] archiveNames = ftp.listNames(ServerFTP.dbArchiveDir);
		for(String s:archiveNames) {
			String name = s.substring(s.lastIndexOf('/') + 1);
			String timestr = name.substring(0, name.indexOf(Database.DB_ARCHIVE_NAME_SEP));
			long millis = Long.parseLong(timestr);
			Date timestamp = new Date(millis);
			archiveMap.put(timestamp, s);
		}

		Date[] ada = archiveMap.keySet().toArray(new Date[archiveMap.size()]);
		Arrays.sort(ada, sorter);
		archives.setListData(ada);
	}

	/**
	 * Load login info from the encrypted data file via ServerFTP.readLoginInfo
	 * @throws IOException
	 */
	private void loadInfo() throws IOException {
		String[] info = ServerFTP.readLoginInfo();
		usr.setText(info[0]);
		passcode.setText(info[1]);
	}

	/**
	 * Listener for connect button
	 * @author Brian Groenke
	 *
	 */
	private class ConnectListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			final FTPProvider ftp = ServerFTP.getProvider();
			if(ftp != null && ftp.isAvailable()) {
				Threads.execute(new Runnable() {

					@Override
					public void run() {
						conn.setEnabled(false);
						inst.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						try {
							ftp.disconnect();
							conn.setText("Connect");
							JOptionPane.showMessageDialog(inst, "Logged out successfully.");
						} catch (IOException e1) {
							e1.printStackTrace();
							JOptionPane.showMessageDialog(inst, "Failed to disconnect:\n"+e1.toString(), "I/O Error", JOptionPane.ERROR_MESSAGE);
						} finally {
							conn.setEnabled(true);
							inst.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						}
					}

				});
			} else if(ftp != null) {
				Threads.execute(new Runnable() {

					@Override
					public void run() {
						conn.setEnabled(false);
						inst.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						conn.repaint();
						try {
							if(usr.getText().isEmpty() || passcode.getText().isEmpty())
								ftp.reconnect(server.getText());
							else
								ftp.reconnect(server.getText(), usr.getText(), passcode.getText());
							ftp.setWorkingDir(ServerFTP.ROOT_DIR);
							conn.setText("Disconnect");
							JOptionPane.showMessageDialog(inst, "Successfully logged in to FTP server @ " + server.getText());
						} catch (IOException e1) {
							e1.printStackTrace();
							JOptionPane.showMessageDialog(inst, "Failed to connect:\n"+e1.toString(), "I/O Error", JOptionPane.ERROR_MESSAGE);
						} finally {
							conn.setEnabled(true);
							inst.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						}
					}

				});
			} else {
				Threads.execute(new Runnable() {

					@Override
					public void run() {
						conn.setEnabled(false);
						inst.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						conn.repaint();
						try {
							ServerFTP.login(false);
							conn.setText("Disconnect");
							JOptionPane.showMessageDialog(inst, "Successfully logged in to FTP server @ " + server.getText());
						} catch (IOException e2) {
							e2.printStackTrace();
							JOptionPane.showMessageDialog(inst, "Failed to connect:\n"+e2.toString(), "I/O Error", JOptionPane.ERROR_MESSAGE);
							return;
						} finally {
							conn.setEnabled(true);
							inst.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						}
					}
					
				});
			}
		}

	}

	/**
	 * Listener for save button
	 * @author Brian Groenke
	 *
	 */
	private class SaveListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				ServerFTP.saveLoginInfo(usr.getText(), passcode.getText());
				ServerFTP.saveNewAddress(server.getText());
				JOptionPane.showMessageDialog(inst, "Successfully saved server info");
			} catch (IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(inst, "Failed to save server info:\n"+e1.toString(), "I/O Error", JOptionPane.ERROR_MESSAGE);
			}

		}

	}

	/**
	 * Listener for create button
	 * @author Brian Groenke
	 *
	 */
	private class CreateListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Threads.execute(new Runnable() {

				@Override
				public void run() {
					ProgressMonitor prog = new ProgressMonitor(inst, "Creating local backup...", "Retrieving database...", 0, 101);
					prog.setMillisToPopup(0);
					try {
						Database db = Database.getInstance(ServerFTP.dbDir, ServerFTP.getProvider(), prog);
						prog.setNote("Writing to local system...");
						db.writeLocal(AppSupport.BACKUP_DIR.getPath());
						try {
							Thread.sleep(300); // pause so the user can see the message
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						prog.close();
						updateLists();
						inst.validate();
						JOptionPane.showMessageDialog(inst, "Backup created successfully");
					} catch (ClassCastException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					}
				}

			});
		}

	}

	/**
	 * Listener for restore button
	 * @author Brian Groenke
	 *
	 */
	private class RestoreListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			int reply = JOptionPane.showConfirmDialog(inst, "Restoring the database will overwrite the existing store file with the backup.  " +
					"This action cannot be undone.\nPress OK to continue.", "Confirm Database Restore", JOptionPane.OK_CANCEL_OPTION);
			if(reply == JOptionPane.CANCEL_OPTION)
				return;
			Threads.execute(new Runnable() {

				@Override
				public void run() {
					Object[] larr = local.getSelectedValues();
					Object[] aarr = archives.getSelectedValues();
					if(larr.length == 1) {
						Date str = (Date) larr[0];
						File f = localMap.get(str);
						if(f == null || !f.exists())
							return;
						try {
							ProgressMonitor prog = new ProgressMonitor(inst, "Restoring Database", "Reading store file...", 0, 101);
							prog.setMillisToPopup(0);
							prog.setMillisToDecideToPopup(0);
							ZipInputStream zipin = new ZipInputStream(new FileInputStream(f));
							ZipEntry ze = zipin.getNextEntry();
							if(!ze.getName().endsWith(Database.DB_ENTRY_SUFFIX)) {
								zipin.close();
								return;
							}
							ObjectInputStream objIn = new ObjectInputStream(new MonitoredInStream(zipin, prog, f.length()));
							Database db = (Database) objIn.readObject();
							objIn.close();
							db.sync(ServerFTP.dbDir, ServerFTP.dbArchiveDir, ServerFTP.getProvider(), Settings.usr, prog);
							prog.close();
							JOptionPane.showMessageDialog(inst, "Successfully restored database");
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						} catch (ClassNotFoundException e1) {
							e1.printStackTrace();
						}
					} else if(aarr.length == 1) {
						Date str = (Date) aarr[0];
						String path = archiveMap.get(str);
						FTPProvider prov = ServerFTP.getProvider();
						if(path == null || !prov.exists(path))
							return;
						ProgressMonitor prog = new ProgressMonitor(inst, "Restoring Database", "Please wait...", 0, 101);
						prog.setMillisToDecideToPopup(0);
						prog.setMillisToPopup(0);
						try {
							ZipInputStream zipin = new ZipInputStream(prov.getInputStream(path));
							ZipEntry ze = zipin.getNextEntry();
							if(!ze.getName().endsWith(Database.DB_ENTRY_SUFFIX))
								throw(new DatabaseException("found invalid entry: " + ze.getName()));
							ObjectInputStream objIn = new ObjectInputStream(new MonitoredInStream(zipin, prog, prov.sizeOf(path)));
							Database db = (Database) objIn.readObject();
							objIn.close();
							db.sync(ServerFTP.dbDir, ServerFTP.dbArchiveDir, ServerFTP.getProvider(), Settings.usr, prog);
							prog.close();
							JOptionPane.showMessageDialog(inst, "Successfully restored database");
						} catch (ClassCastException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}

					}

					updateLists();
					validate();
				}

			});
		}

	}

	/**
	 * Listener for remove button
	 * @author Brian Groenke
	 *
	 */
	private class RemoveListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Object[] larr = local.getSelectedValues();
			Object[] aarr = archives.getSelectedValues();
			if(larr.length > 0) {
				int reply = JOptionPane.showConfirmDialog(inst, "Delete " + larr.length + " backup(s) from the system?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
				if(reply == JOptionPane.NO_OPTION)
					return;
				for(Object o:larr) {
					Date str = (Date) o;
					File f = localMap.get(str);
					f.delete();
					localMap.remove(str);
				}
			} else if(aarr.length > 0) {
				int reply = JOptionPane.showConfirmDialog(inst, "Delete " + aarr.length + " backup(s) from the server?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
				if(reply == JOptionPane.NO_OPTION)
					return;
				FTPProvider prov = ServerFTP.getProvider();
				for(Object o:aarr) {
					Date str = (Date) o;
					String path = archiveMap.get(str);
					prov.delete(path);
					archiveMap.remove(str);
				}
			}

			updateLists();
			validate();
		}

	}

	private class AdvSettingsUI extends JDialog {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7155319065889800645L;

		AdvSettingsUI inst;
		JTextField root, bin, db, sub, dba;
		JButton save, cancel;

		public AdvSettingsUI(JDialog parent, String title) {
			super(parent, title);
			inst = this;
			Box main = Box.createVerticalBox();
			JPanel lp1 = new JPanel();
			((FlowLayout)lp1.getLayout()).setAlignment(FlowLayout.LEFT);
			lp1.add(new JLabel("Root directory"));
			root = new JTextField(TEXT_FIELD_LENGTH);
			JPanel fp1 = new JPanel();
			fp1.add(root);
			main.add(lp1);
			main.add(fp1);

			JPanel lp2 = new JPanel();
			((FlowLayout)lp2.getLayout()).setAlignment(FlowLayout.LEFT);
			lp2.add(new JLabel("Bin (relative to root)"));
			bin = new JTextField(TEXT_FIELD_LENGTH);
			JPanel fp2 = new JPanel();
			fp2.add(bin);
			main.add(lp2);
			main.add(fp2);

			JPanel lp3 = new JPanel();
			((FlowLayout)lp3.getLayout()).setAlignment(FlowLayout.LEFT);
			lp3.add(new JLabel("Database (relative to root)"));
			db = new JTextField(TEXT_FIELD_LENGTH);
			JPanel fp3 = new JPanel();
			fp3.add(db);
			main.add(lp3);
			main.add(fp3);

			JPanel lp4 = new JPanel();
			((FlowLayout)lp4.getLayout()).setAlignment(FlowLayout.LEFT);
			lp4.add(new JLabel("Database Archives (relative to root)"));
			dba = new JTextField(TEXT_FIELD_LENGTH);
			JPanel fp4 = new JPanel();
			fp4.add(dba);
			main.add(lp4);
			main.add(fp4);

			JPanel lp5 = new JPanel();
			((FlowLayout)lp5.getLayout()).setAlignment(FlowLayout.LEFT);
			lp5.add(new JLabel("Submissions (relative to root)"));
			sub = new JTextField(TEXT_FIELD_LENGTH);
			JPanel fp5 = new JPanel();
			fp5.add(sub);
			main.add(lp5);
			main.add(fp5);

			JPanel buttons = new JPanel();
			save = new JButton("Save");
			save.setPreferredSize(ACTION_BUTTON_SIZE);
			save.addActionListener(new SaveListener());
			cancel = new JButton("Cancel");
			cancel.setPreferredSize(ACTION_BUTTON_SIZE);
			cancel.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					inst.dispose();
				}
			});
			buttons.add(save);
			buttons.add(cancel);
			main.add(buttons);

			init();

			setContentPane(main);
			pack();
			setLocationRelativeTo(parent);
		}

		private void init() {
			root.setText(ServerFTP.rootDir);
			bin.setText(ServerFTP.binDir);
			db.setText(ServerFTP.dbDir);
			dba.setText(ServerFTP.dbArchiveDir);
			sub.setText(ServerFTP.subDir);
		}

		private class SaveListener implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent e) {
				int reply = JOptionPane.showConfirmDialog(inst, "Incorrect specification of the server file structure will cause many components to fail.\n" +
						"Are you sure you wish to edit these configurations?", "Confirm Config Save", JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
				if(reply == JOptionPane.CANCEL_OPTION)
					return;
				ServerFTP.rootDir = root.getText();
				ServerFTP.binDir = bin.getText();
				ServerFTP.dbDir = db.getText();
				ServerFTP.dbArchiveDir = dba.getText();
				ServerFTP.subDir = sub.getText();
				try {
					ServerFTP.writeConfig();
				} catch (Exception e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(inst, "An error occurred while saving the config:\n"+e1.toString(), "Save Failure", JOptionPane.ERROR_MESSAGE);
				}

				inst.dispose();
			}

		}
	}

	/**
	 * Returns later dates as being less than earlier dates.
	 * @author Brian Groenke
	 *
	 */
	private class TopDownDateSorter implements Comparator<Date> {

		@Override
		public int compare(Date o1, Date o2) {
			if(o1.after(o2))
				return -1;
			else if (o1.before(o2))
				return 1;
			else
				return 0;
		}

	}

}
