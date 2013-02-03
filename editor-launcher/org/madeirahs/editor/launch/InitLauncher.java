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
 *  
 *  Note: This class was borrowed from the Groenke Commons Java API with full permission from the author.
 */

package org.madeirahs.editor.launch;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.jar.*;
import java.util.jar.Attributes.Name;
import java.util.prefs.*;

import javax.swing.*;

public class InitLauncher {

	/**
	 * <b>All node values MUST remain consistent with Settings class of editor package!</b>
	 */
	public static final String NODE = "mhseditor", USER_KEY = "user", ARCHIVE_LIMIT_KEY = "archiveLimit", UPDATE_CHECK_KEY = "updateCheck",
			UPDATE_INTERVAL_KEY = "updateInterval";

	public static final String LAUNCHER_JAR_NAME = "launcher.jar", JAR_NAME = "mhseditor.jar", UPDATE_JAR = "updater.jar", 
			JAR_PATH = "org/madeirahs/editor/launch/jars/", TMP_FILE = "loc.tmp",
			/* JVM heap space tweaks */HEAP_RAM_MAX = "614M",
			MAX_FREE_RATIO = "40", MIN_FREE_RATIO = "10";

	public static final File UPDATE_LOG_FILE = new File(AppSupport.SYS_DIR + File.separator + "updateLog.ser");
	public static Log log = new Log();

	public static final String LIB1 = "commons-net-3.0.1.jar",
			LIB2 = "shared-lib.jar";

	public static final String URL_BASE = "http://www.madeirahs.org/collection/bin/", JAR_URL_BASE = "jar:"+URL_BASE, JAR_SUFFIX = "!/";

	private static ProgressMonitor prog;

	public static void main(String[] args) throws ClassNotFoundException,
	InstantiationException, IllegalAccessException,
	UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		boolean update = shouldUpdate();

		if(update) {
			prog = new ProgressMonitor(null,
					"Validating software packages...", "Contacting update server... ("+URL_BASE+")", 0,
					100);
			prog.setMillisToDecideToPopup(0);
			prog.setMillisToPopup(1000);
			prog.setProgress(0);

			boolean success = checkForLauncherUpdate();

			AppSupport.checkStorageDirs();

			if(success)
				checkPackages();

			writeLog();
		}

		File appFile = new File(AppSupport.BIN_DIR + File.separator + JAR_NAME);

		String filepath = appFile.toString();
		//String filepath = "\"" + appFile + "\"";
		String[] cmdset = new String[] { "java", "-client", "-Xmx" + HEAP_RAM_MAX,
				"-XX:MaxHeapFreeRatio=" + MAX_FREE_RATIO,
				"-XX:MinHeapFreeRatio=" + MIN_FREE_RATIO, "-jar", filepath,
				System.getProperty("user.dir") };
		try {
			Process p = Runtime.getRuntime().exec(cmdset);
			filepath = null;
			cmdset = null;
			System.gc();
			System.runFinalization();
			int exitCode = p.waitFor();
			System.out.println(exitCode);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static boolean shouldUpdate() {
		if(!UPDATE_LOG_FILE.exists())
			return true;
		Preferences prefs = Preferences.userRoot().node(NODE);
		if(!prefs.getBoolean(UPDATE_CHECK_KEY, true))
			return false;
		long interval = prefs.getLong(UPDATE_INTERVAL_KEY, Long.MIN_VALUE);
		try {
			ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(UPDATE_LOG_FILE));
			Log log = (Log) objIn.readObject();
			objIn.close();
			long curr = Calendar.getInstance().getTimeInMillis();
			long last = log.time.getTimeInMillis();
			return (curr - last) > interval;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return true;
	}

	private static void writeLog() {
		log.time = Calendar.getInstance();
		try {
			UPDATE_LOG_FILE.createNewFile();
			ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(UPDATE_LOG_FILE));
			objOut.writeObject(log);
			objOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean checkForLauncherUpdate() {
		try {
			URLConnection conn = new URL(URL_BASE).openConnection();
			System.out.println("Server reply to date request: "+DateFormat.getDateInstance(DateFormat.FULL).format(new Date(conn.getDate())));
			prog.setNote("Checking for launcher updates...");
			JarURLConnection jurl = (JarURLConnection) new URL(JAR_URL_BASE + LAUNCHER_JAR_NAME + JAR_SUFFIX).openConnection();
			Manifest mf = jurl.getManifest();
			Attributes attr = mf.getMainAttributes();
			String nv = attr.getValue(Name.IMPLEMENTATION_VERSION);
			String version = attr.getValue(Name.SPECIFICATION_VERSION);
			URLClassLoader cl = (URLClassLoader) ClassLoader.getSystemClassLoader();
			URL url = cl.findResource("META-INF/MANIFEST.MF");
			Manifest mf2 = new Manifest(url.openStream());
			String currVer = mf2.getMainAttributes().getValue(Name.IMPLEMENTATION_VERSION);
			if(!nv.equalsIgnoreCase(currVer) && !prog.isCanceled())
				selfUpdate(version + "_"+nv);
		} catch(IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.toString(), "Update Check Failed", JOptionPane.ERROR_MESSAGE);
			log.exceptions.add(e);
			return false;
		}

		return true;
	}

	private static void selfUpdate(String newVersion) {
		prog.setNote("Launcher version " + newVersion + " found: preparing self-updater...");
		int reply = JOptionPane.showConfirmDialog(null, "A launcher update is available: v." + newVersion + "\nClick OK to upgrade or Cancel to skip.", "Update Available", JOptionPane.OK_CANCEL_OPTION);
		if(reply == JOptionPane.CANCEL_OPTION)
			return;
		try {
			String currPath = System.getProperty("user.dir") + File.separator + getLocalJarName(InitLauncher.class);
			PrintWriter pw = new PrintWriter(new File(System.getProperty("java.io.tmpdir") + TMP_FILE));
			pw.println(currPath);
			pw.close();
			Desktop.getDesktop().open(new File(AppSupport.BIN_DIR + File.separator + UPDATE_JAR));
			prog.close();
			log.updatedPkgs.add(currPath);
			writeLog();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e);
		}
	}

	private static String getLocalJarName(Class<?> cls) {
		String cn = cls.getName();
		String rn = cn.replace('.', '/') + ".class";
		String path =
				ClassLoader.getSystemClassLoader().getResource(rn).getPath();
		String jpath = path.substring(0, path.lastIndexOf("!"));
		return jpath.substring(jpath.lastIndexOf("/") + 1, jpath.length()).replace("%20", " ");
	}

	private static void checkPackages() {
		final int TOT = 4;
		for (int i = 0; i < TOT; i++) {
			if(prog.isCanceled())
				break;
			String jarname = null;
			switch (i) {
			case 0:
				jarname = JAR_NAME;
				prog.setProgress(25);
				break;
			case 1:
				jarname = LIB1;
				prog.setProgress(50);
				break;
			case 2:
				jarname = LIB2;
				prog.setProgress(75);
				break;
			case 3:
				jarname = UPDATE_JAR;
				prog.setProgress(99);
			}
			try {
				prog.setNote("Verifying software version... (package " + (i+1) + " of " + TOT + ")");
				JarURLConnection jurl = (JarURLConnection) new URL(JAR_URL_BASE + jarname + JAR_SUFFIX).openConnection();
				Manifest mf = jurl.getManifest();
				Attributes attr = mf.getMainAttributes();
				String version = attr.getValue(Name.IMPLEMENTATION_VERSION);
				File jarFile = new File(AppSupport.BIN_DIR + File.separator + jarname);
				String currVer = null;
				if(jarFile.exists()) {
					JarFile jar = new JarFile(jarFile);
					Manifest mf2 = jar.getManifest();
					Attributes attr2 = mf2.getMainAttributes();
					currVer = attr2.getValue(Name.IMPLEMENTATION_VERSION);
				}
				if(!jarFile.exists() || !version.equalsIgnoreCase(currVer)) {
					prog.setNote("Updating software package... (package " + (i+1) + " of " + TOT + ")");
					InputStream in = new URL(URL_BASE + jarname).openStream();
					BufferedOutputStream buffout = new BufferedOutputStream(
							new FileOutputStream(AppSupport.BIN_DIR
									+ File.separator + jarname));
					byte[] buff = new byte[2048];
					int len = 0;
					while ((len = in.read(buff)) > 0) {
						buffout.write(buff, 0, len);
					}
					buffout.close();
					in.close();
					log.updatedPkgs.add(jarname);
				}
			} catch (IOException e) {
				e.printStackTrace();
				log.exceptions.add(e);
				alert(e);
				break;
			}
		}

		prog.close();
	}

	@SuppressWarnings("unused")
	@Deprecated
	/**
	 * @deprecated Replaced by network-based updating.
	 */
	private static void exportBinData() {
		ProgressMonitor prog = new ProgressMonitor(null,
				"Unpacking application data...", "Exporting package 1 of 3", 0,
				100);
		prog.setMillisToDecideToPopup(0);
		prog.setMillisToPopup(300);
		prog.setProgress(0);
		try {
			Thread.sleep(400);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		for (int i = 0; i < 3; i++) {
			String jarname = null;
			switch (i) {
			case 0:
				jarname = JAR_NAME;
				prog.setProgress(33);
				break;
			case 1:
				jarname = LIB1;
				prog.setProgress(66);
				prog.setNote("Exporting package 2 of 3");
				break;
			case 2:
				jarname = LIB2;
				prog.setProgress(99);
				prog.setNote("Exporting package 3 of 3");
				break;
			}
			InputStream in = ClassLoader.getSystemClassLoader()
					.getResourceAsStream(JAR_PATH + jarname);
			try {
				BufferedOutputStream buffout = new BufferedOutputStream(
						new FileOutputStream(AppSupport.BIN_DIR
								+ File.separator + jarname));
				byte[] buff = new byte[2048];
				int len = 0;
				while ((len = in.read(buff)) > 0) {
					buffout.write(buff, 0, len);
				}
				buffout.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
				fail(e);
			}
		}

		prog.close();
	}

	private static void fail(Throwable t) {
		alert(t);
		System.exit(1);
	}

	private static void alert(Throwable t) {
		JOptionPane.showMessageDialog(
				null,
				"Fatal error occurred when launching MHS-Editor:\n"
						+ t.toString(), "Launch Error",
						JOptionPane.ERROR_MESSAGE);
	}
}
