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

package org.madeirahs.editor.main;

import java.util.prefs.*;

/**
 * Uses the Java preferences API to store user settings.  The caller is responsible for calling the 'put' commands
 * on the fetched Preferences node.  After data is stored, a call to sync() and save() should follow.
 * @author Brian Groenke
 *
 */
public final class Settings {

	public static final String NODE = "mhseditor", USER_KEY = "user", ARCHIVE_LIMIT_KEY = "archiveLimit", UPDATE_CHECK_KEY = "updateCheck",
			UPDATE_INTERVAL_KEY = "updateInterval", INIT_LOGIN_KEY = "initLogin";

	public static String usr;
	public static int archiveLimit;
	public static boolean updateCheck, initLogin;
	public static long interval;
	
	static {
		sync();
	}

	public static void save() {
		Preferences user = Preferences.userRoot();
		Preferences prefs = user.node(NODE);
		try {
			prefs.flush();
			prefs.sync();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
	
	public static Preferences prefs() {
		return Preferences.userRoot().node(NODE);
	}
	
	public static void sync() {
		Preferences prefs = Preferences.userRoot().node(NODE);
		usr = prefs.get(USER_KEY, null);
		archiveLimit = prefs.getInt(ARCHIVE_LIMIT_KEY, -1);
		updateCheck = prefs.getBoolean(UPDATE_CHECK_KEY, true);
		interval = prefs.getLong(UPDATE_INTERVAL_KEY, Long.MIN_VALUE);
		initLogin = prefs.getBoolean(INIT_LOGIN_KEY, true);
	}
	
	public enum UpdateInterval {
		
		ALWAYS(Long.MIN_VALUE), DAILY((long) (8.64 * Math.pow(10, 7))), WEEKLY((long) (6.048 * Math.pow(10,8))),
		MONTHLY((long) (2.628 * Math.pow(10,9)));
		
		private long millis;
		
		UpdateInterval(long millis) {
			this.millis = millis;
		}
		
		public long getMillis() {
			return millis;
		}
		
		public static UpdateInterval getByMillis(long millis) {
			for(UpdateInterval udintr:values())
				if(udintr.millis == millis)
					return udintr;
			return null;
		}
	}
}
