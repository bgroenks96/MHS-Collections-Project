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
 *  
 *  Note: This class was borrowed from the Groenke Commons Java API with full permission from the author.
 */

package org.madeirahs.editor.main;

import java.io.*;

/**
 * Provides several file locations on various platforms where applications can
 * freely store information/data, as well as access to the default Java tempdir.
 * 
 * @author Brian Groenke
 * @since GCJL 1.0
 */

public class AppSupport {

	private AppSupport() {
	}

	public static final File USER_DIR = new File(System.getProperty("user.dir"));
	public static final File USER_HOME = new File(
			System.getProperty("user.home"));
	public static final File WINDOWS = new File(USER_HOME + "/AppData/Roaming");
	public static final File WINDOWS_XP = new File(USER_HOME
			+ "/Application Data");
	public static final File MAC_OS_X = new File(USER_HOME
			+ "/Library/Application Support");
	public static final File LINUX = USER_HOME;
	public static final File TEMP = new File(
			System.getProperty("java.io.tmpdir"));

	// Application specific locations
	public static final String APP_DIR_STR = "MHS-Editor";
	
	public static final File APP_DIR = getAppStorage();
	public static final File BACKUP_DIR = new File(APP_DIR + File.separator
			+ "db-backup");
	public static final File SYS_DIR = new File(APP_DIR + File.separator
			+ "sysdata");
	public static final File SAVE_DIR = new File(APP_DIR + File.separator
			+ "saves");
	public static final File RSC_SAVE_DIR = new File(SAVE_DIR + File.separator
			+ "rsc-dl");

	public static final File BIN_DIR = new File(APP_DIR + File.separator
			+ "bin");

	public static final String JAR_PKG_ROOT = "org/madeirahs/editor",
			JAR_PKG_UI = JAR_PKG_ROOT + "/ui", JAR_PKG_NET = JAR_PKG_ROOT
					+ "/net", JAR_PKG_MAIN = JAR_PKG_ROOT + "/main";

	// ----

	/**
	 * Identifies the underlying OS and returns the corresponding app data
	 * storage directory as a File object.<br>
	 * This method will return the #LINUX_LOCAL directory if the underlying
	 * platform is determined to be a Linux OS.<br>
	 * If no supported OS is detected, the user home directory will be returned. This
	 * may occur on systems where the OS name property isn't recognized
	 * (Solaris, Linux variants and any other miscellaneous UNIX platforms that
	 * Java still supports).
	 * 
	 * @return the File (directory) corresponding with the local OS that can be
	 *         used for application data storage.
	 */
	public static File getSystemStorage() {
		String os = System.getProperty("os.name").toLowerCase();
		File sysloc = null;
		if (os.contains("windows")) {
			if (os.contains("xp")) {
				sysloc = WINDOWS_XP;
			} else {
				sysloc = WINDOWS;
			}
		} else if (os.contains("mac")) {
			sysloc = MAC_OS_X;
		} else if (os.contains("linux")) {
			sysloc = LINUX;
		} else {
			sysloc = USER_HOME;
		}

		return sysloc;
	}
	
	/**
	 * Gets the storage directory for the specific Application, as defined by APP_DIR_STR.
	 * APP_DIR uses this method to get the application storage File.  On Linux, the application
	 * directory is preceded by a '.'.
	 * @return
	 */
	public static File getAppStorage() {
		File sys = getSystemStorage();
		String appDir = (sys.equals(LINUX)) ? "." + APP_DIR_STR:APP_DIR_STR;
		File appStore = new File(sys + File.separator + appDir);
		return appStore;
	}

	public static void checkStorageDirs() {
		if (!APP_DIR.exists()) {
			APP_DIR.mkdir();
		}
		if (!SYS_DIR.exists() || !BACKUP_DIR.exists() || !SAVE_DIR.exists()
				|| !RSC_SAVE_DIR.exists() || !BIN_DIR.exists()) {
			SYS_DIR.mkdir();
			BACKUP_DIR.mkdir();
			SAVE_DIR.mkdir();
			RSC_SAVE_DIR.mkdir();
			BIN_DIR.mkdir();
		}
	}
}
