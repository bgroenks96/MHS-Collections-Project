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

package org.madeirahs.editor.net;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import org.madeirahs.editor.main.*;
import org.madeirahs.shared.provider.*;

/**
 * Provides static fields and methods for initializing and managing FTP
 * internally. This class handles the obtaining login/username information from
 * the user, as well as creating an FTPProvider available for public use via
 * <code>getProvider()</code>.
 * 
 * Additionally, this classes handles reading most of the configuration/save files utilized
 * by the editor in the 'sysdata' directory.
 * 
 * @author Brian Groenke
 * 
 */
public class ServerFTP {

	public static final String ROOT_DIR = "/madeirahs.org/collection/",
			RSC_DIR = "rsc/", SUB_DIR = "submissions/", DB_DIR = "database/",
			DB_ARCHIVE_DIR = DB_DIR + "archives/", BIN_DIR = "bin/";

	/**
	 * No other field in this class may end in the characters "Dir"; this identifier is used
	 * to abstractly create and read the config file values.
	 */
	public static String rootDir = ROOT_DIR,
			rscDir = RSC_DIR, subDir = SUB_DIR, dbDir = DB_DIR,
			dbArchiveDir = DB_ARCHIVE_DIR, binDir = BIN_DIR;

	private static final String 
	/**
	 * Comment or remark character.  Line will be skipped when parsing.
	 */
	CONFIG_REM = "#", 
	CONFIG_VER = CONFIG_REM + "config-v.001", CONFIG_SEP = ":";

	static final int SHIFT_THRESHOLD = 24;
	static final File LOGIN_SAVE = new File(AppSupport.SYS_DIR + File.separator
			+ "login"), SERVER_SAVE = new File(AppSupport.SYS_DIR + File.separator
					+ "server"), ADV_CONFIG_SAVE = new File(AppSupport.SYS_DIR + File.separator + "xconfig");
	@Deprecated
	static final File NAME_SAVE = new File(AppSupport.SYS_DIR + File.separator
			+ "usr");
	static String ftpServerUrl = "ftp.madeirahs.org";
	static FTPProvider prov;
	static volatile boolean login;

	private static JDialog window;
	private static JTextField userInput;
	private static JPasswordField passInput;
	private static JProgressBar prog;

	static {
		if(NAME_SAVE.exists())
			NAME_SAVE.delete();
		try {
			readConfig();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Fatal error - program will exit\n"+e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Fatal error - program will exit\n"+e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		} catch (SecurityException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Fatal error - program will exit\n"+e.toString(), "Access Denied", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Fatal error - program will exit\n"+e.toString(), "Invalid Field", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set the URL the program connects to for access to the FTP server.
	 * @param newUrl
	 * @return true if successfully reset, false otherwise (only occurs is string was empty or null).
	 */
	public static boolean setFtpServerUrl(String newUrl) {
		if(newUrl != null && !newUrl.isEmpty())
			ftpServerUrl = newUrl;
		else
			return false;
		return true;

	}

	public static String getFtpServerUrl() {
		return ftpServerUrl;
	}

	public static FTPProvider getProvider() {
		return prov;
	}

	/**
	 * Shows a dialog for the user to enter his/her desired username for this
	 * session. If requested, the information will be saved to the system as a
	 * default login.
	 */
	public static void promptForName() {
		String user = Settings.prefs().get(Settings.USER_KEY, null);
		if(user != null)
			return;
		final JDialog dialog = new JDialog();
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		Box box = Box.createVerticalBox();
		JLabel label = new JLabel(
				"Enter your username to be used for this session:");
		final JTextField input = new JTextField();
		final JCheckBox save = new JCheckBox(
				"Always use this name on this system");
		final JButton ok = new JButton("OK");
		box.add(label);
		box.add(input);
		box.add(Box.createVerticalStrut(10));
		box.add(save);
		JPanel bhld = new JPanel();
		bhld.add(ok);
		box.setBorder(new EmptyBorder(5, 5, 5, 5));
		dialog.add(BorderLayout.CENTER, box);
		dialog.add(BorderLayout.SOUTH, bhld);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setResizable(false);
		dialog.setVisible(true);
		dialog.addWindowFocusListener(new WindowFocusListener() {

			@Override
			public void windowGainedFocus(WindowEvent e) {
			}

			@Override
			public void windowLostFocus(WindowEvent e) {
				dialog.requestFocus();
			}

		});
		box.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), "onEnter");
		box.getActionMap().put("onEnter", new AbstractAction() {
			private static final long serialVersionUID = -3065977430688202574L;

			@Override
			public void actionPerformed(ActionEvent e) {
				ok.doClick();
			}
		});
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String str = input.getText();
				if (str != null && !str.isEmpty()) {
					Settings.usr = str;
				}
				if (save.isSelected()) {
					Settings.prefs().put(Settings.USER_KEY, str);
					Settings.save();
				}
				dialog.dispose();
			}
		});
	}

	/**
	 * Initiates an attempt to login to the remote FTP server. If login
	 * information is not available on the system, the user will be asked to
	 * provide it. Saved information is encrypted using the Bitshift-0C
	 * algorithm. If a FTPProvider already exists with an active connection, it
	 * will be disconnected before continuing.
	 * 
	 * @throws IOException
	 */
	public static void login() throws IOException {
		if (prov != null && prov.isAvailable()) {
			prov.disconnect();
		}
		if (!LOGIN_SAVE.exists()) {
			window = new JDialog();
			window.setTitle("FTP Server Login");
			window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			Box mb = new Box(BoxLayout.Y_AXIS);
			mb.setOpaque(true);
			mb.setBackground(new Color(0xE6EBF0));
			JPanel utfh = new JPanel(), ptfh = new JPanel();
			utfh.setOpaque(false);
			ptfh.setOpaque(false);
			JLabel l1 = new JLabel("FTP Account Username"), l2 = new JLabel(
					"FTP Account Password");
			userInput = new JTextField(20);
			passInput = new JPasswordField(20);
			utfh.add(userInput);
			ptfh.add(passInput);
			JPanel bh = new JPanel();
			bh.setOpaque(false);
			JButton login = new JButton("Login");
			login.addActionListener(new ServerLogin());
			bh.add(login);
			mb.add(l1);
			mb.add(utfh);
			mb.add(l2);
			mb.add(ptfh);
			mb.add(bh);
			mb.setBorder(new EmptyBorder(5, 5, 5, 5));
			prog = new JProgressBar();
			prog.setIndeterminate(true);
			window.add(BorderLayout.CENTER, mb);
			window.add(BorderLayout.SOUTH, prog);
			window.pack();
			window.setLocationRelativeTo(null);
			window.setFocusable(true);
			window.setFocusableWindowState(true);
			window.addWindowFocusListener(new WindowFocusListener() {

				@Override
				public void windowGainedFocus(WindowEvent e) {

				}

				@Override
				public void windowLostFocus(WindowEvent e) {
					e.getWindow().requestFocus();
				}

			});
			window.addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosing(WindowEvent e) {
					int reply = JOptionPane.showConfirmDialog(
							e.getWindow(),
							"Services will be unavailable if you do not login to the FTP server.  Skip anyway?",
							"Skip Login", JOptionPane.OK_CANCEL_OPTION);
					if (reply == JOptionPane.OK_OPTION) {
						e.getWindow().dispose();
					}
					promptForName();
				}

			});
			prog.setVisible(false);
			window.setVisible(true);
			userInput.requestFocus();
		} else {
			String[] info = readLoginInfo();
			new ServerLogin().login(ftpServerUrl, info[0], info[1]);
		}
	}

	@Deprecated
	/**
	 * No longer needed after username was made part of the Preference API based settings system.
	 * @return
	 */
	public static String readNameFile() {
		String read = null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(NAME_SAVE)));
			read = br.readLine();
			br.close();
		} catch(IOException e) {

		}

		return read;
	}

	public static void saveNewAddress(String addr) {
		try {
			PrintWriter pw = new PrintWriter(SERVER_SAVE);
			pw.println(addr);
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static String readAddress() {
		String addr = null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(SERVER_SAVE)));
			addr = br.readLine();
		} catch (FileNotFoundException e) {

		} catch (IOException e) {
			e.printStackTrace();
		}
		return addr;
	}

	/*
	 * The encryption algorithm first writes the random seed to the file so
	 * that it can be decrypted. Then, it encrypts each byte by replacing it
	 * with a long value that is equivalent to the byte shifted left n
	 * number of bits (n being a random number generated by the seed).
	 * 
	 * Decryption reverse engineers the process, shifting each long value n
	 * bits to the right and casting to a byte.
	 * 
	 * A shifting threshold of 24 bits is used, as to prevent an overflow
	 * beyond the value of a long.
	 * 
	 * It should be noted that this encryption algorithm largely inflates
	 * the file size, and thus was designed only for small files.
	 * 
	 * See the bottom of the file for a more detailed file format model.
	 */

	/**
	 * Saves and encrypts the login information using a
	 * RandomAccessFile and byte-shift encryption algorithm.
	 * 
	 * @param usr
	 *            the username to save
	 * @param pass
	 *            the password to save
	 * @throws IOException 
	 */
	public static void saveLoginInfo(String usr, String pass) throws IOException {
		LOGIN_SAVE.createNewFile();
		RandomAccessFile raf = new RandomAccessFile(LOGIN_SAVE, "rws");
		long seed = Math.round(Math.random() * Long.MAX_VALUE);
		Random gen = new Random(seed);
		raf.writeLong(seed);
		byte[] bytes = usr.getBytes(Charset.defaultCharset());
		raf.writeInt(bytes.length);
		for (byte b : bytes) {
			raf.writeLong(b << gen.nextInt(SHIFT_THRESHOLD));
		}
		bytes = pass.getBytes();
		raf.writeInt(bytes.length);
		for (byte b : bytes) {
			raf.writeLong(b << gen.nextInt(SHIFT_THRESHOLD));
		}
		raf.close();
	}

	/**
	 * Reads and decrypts the login information from the save file.
	 * @return a String array where the username is in index 0 and password in index 1 (usr = array[0], pass = array[1]).
	 * @throws IOException
	 */
	public static String[] readLoginInfo() throws IOException {
		RandomAccessFile raf = new RandomAccessFile(LOGIN_SAVE, "rws");
		long seed = raf.readLong();
		Random gen = new Random(seed);
		int ulen = raf.readInt();
		byte[] bytes = new byte[ulen];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) (raf.readLong() >> gen
			.nextInt(SHIFT_THRESHOLD));
		}
		String usr = new String(bytes, Charset.defaultCharset());
		int plen = raf.readInt();
		bytes = new byte[plen];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) (raf.readLong() >> gen
			.nextInt(SHIFT_THRESHOLD));
		}
		String pass = new String(bytes);
		raf.close();
		return new String[] {usr, pass};
	}

	/**
	 * Read the configuration file for server file structure settings.  This method uses class field get/set methods
	 * to abstractly configure the ServerFTP class public "Dir" fields without needing code to be appended if additional
	 * fields are added.
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	public static void readConfig() throws IOException, IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
		if(!ADV_CONFIG_SAVE.exists()) {
			writeConfig();
		}

		BufferedReader br = new BufferedReader(new FileReader(ADV_CONFIG_SAVE));
		String line = null;
		int lineCount = 1;
		boolean regen = false;
		while((line=br.readLine()) != null) {
			if(line.startsWith(CONFIG_REM)) {
				if(lineCount == 1) // first line of config file must specify version
					if(!line.equals(CONFIG_VER)) // if config specs have changed, mark for regeneration.
						regen = true;
				continue;
			}
			String[] pts = line.split(CONFIG_SEP);
			if(pts.length != 2)
				throw(new IOException("error in config file: illegal syntax\nline="+lineCount));
			Field f = ServerFTP.class.getField(pts[0]);
			f.set(new ServerFTP(), pts[1]);
			lineCount++;
		}

		if(regen)
			writeConfig();
	}

	/**
	 * Write the configuration file with the current values set in the class.
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static void writeConfig() throws IOException, IllegalArgumentException, IllegalAccessException {
		PrintWriter pw = new PrintWriter(ADV_CONFIG_SAVE);
		pw.println(CONFIG_VER);
		Field[] farr = ServerFTP.class.getFields();
		for(Field f:farr) {
			if(!f.getName().endsWith("Dir"))
				continue;
			pw.println(f.getName() + CONFIG_SEP + f.get(new ServerFTP()));
		}
		pw.close();
	}

	/**
	 * Check the database archives and compare the backup count to the limit set locally.
	 * If the current size of the archives exceeds the limit, all excess STORE files
	 * (starting with the oldest) will be deleted.
	 */
	public static void checkArchiveLimit() {
		int limit = Settings.archiveLimit;
		if(limit < 0)
			return;
		String[] backups = prov.listNames(dbArchiveDir);
		while(backups.length > limit) {
			String oldest = null;
			Calendar oldestTime = Calendar.getInstance();
			for(String s:backups) {
				Calendar time = prov.getLastModified(s);
				if(time.before(oldestTime)) {
					oldestTime = time;
					oldest = s;
				}
			}
			prov.delete(oldest);

			backups = prov.listNames(dbArchiveDir);
		}
	}

	/**
	 * Checks for all default directories on the server and creates them if not found.
	 */
	public static void checkDirs() {
		if (!prov.exists(rootDir)) {
			JOptionPane
			.showMessageDialog(
					null,
					"Server error: unable to locate system directory\nPlease contact the server admin.",
					"Fatal error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		if (!prov.exists(rscDir)) {
			prov.mkdir(rscDir);
		}
		if (!prov.exists(subDir)) {
			prov.mkdir(subDir);
		}
		if (!prov.exists(dbDir)) {
			prov.mkdir(dbDir);
		}
		if (!prov.exists(dbArchiveDir)) {
			prov.mkdir(dbArchiveDir);
		}
		if (!prov.exists(binDir)) {
			prov.mkdir(binDir);
		}
	}

	/**
	 * Internal class that handles the information provided by the user and
	 * makes a login attempt. This nested class also contains the code for
	 * encrypting login info.
	 * 
	 * @author Brian Groenke
	 * 
	 */
	private static class ServerLogin implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			final String usr = userInput.getText();
			@SuppressWarnings("deprecation")
			final String pass = passInput.getText();
			final JButton src = (JButton) e.getSource();
			new Thread(new Runnable() {

				@Override
				public void run() {
					src.setEnabled(false);
					prog.setVisible(true);
					login(ftpServerUrl, usr, pass);
					prog.setVisible(false);
					src.setEnabled(true);
				}

			}).start();
		}

		public boolean login(String url, String usr, String pass) {
			try {
				String addr = readAddress();
				if(addr != null && !addr.isEmpty())
					ftpServerUrl = addr;
				prov = new FTPProvider(ftpServerUrl, usr, pass);
				prov.setWorkingDir(rootDir);
				ServerFTP.checkDirs();
				if (window != null) {
					window.dispose();
					saveLoginInfo(usr, pass);
					JOptionPane
					.showMessageDialog(
							null,
							"Login successful! Credentials have been saved for auto-login.\n(Tools -> Settings to change)");
				}
				login = true;
				promptForName();
			} catch (LoginException e1) {
				JOptionPane.showMessageDialog(null, "Failed to login to "
						+ ftpServerUrl + ": " + e1.toString(),
						"Login Failed", JOptionPane.ERROR_MESSAGE);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null,
						"An error occurred:\n" + e1.toString(), "IOException",
						JOptionPane.ERROR_MESSAGE);
			}
			
			return login;
		}
	}
}

/*
 * Bitshift-0C Encryption Algorithm (author: Brian Groenke) - file format
 * structure 
 * <file-start> encryption seed (long - 64 bits) username string
 * length (int - 32 bits) username byte values (each byte is inflated to a long
 * value by left-shifting n places where 'n' is randomly generated using the
 * seed) password string length (int - 32 bits) password byte values (same
 * process as username bytes) <file-end>
 */
