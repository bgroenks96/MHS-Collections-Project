/*
 *  The MHS-Collections Project editor is intended for use by Historical Society members
 *  to edit, review and upload artifact information.
 *  Copyright © 2012-  Madeira Historical Society (developed by Brian Groenke)
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

import java.io.*;
import java.text.*;

import javax.swing.*;

import org.madeirahs.editor.net.*;
import org.madeirahs.editor.ui.*;
import org.madeirahs.shared.database.*;

public class Launcher {
	
	public static int a = 0;

	/**
	 * @param args
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 * @throws DuplicateArtifactException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws FileNotFoundException,
	IOException, ClassNotFoundException, DuplicateArtifactException,
	InterruptedException {
		
		/*
		ServerFTP.login(); 
		System.out.println("Fetching Database...");
		Database db = Database.getInstance(ServerFTP.DB_DIR,
				ServerFTP.getProvider(), null); 
		System.out.println((db.getSize() > 0)? "Database downloaded":"Database created"); 
		if(db.getSize() == 0) {
			System.out.println("Generating database contents..."); 
			for(int i=0;i<500;i++) { 
				Artifact a = Artifact.createGenericArtifact();
				a.accNum = "a" + i; 
				db.add(a); 
			} 
			System.out.println("Complete");
		}
		System.out.println("Beginning sync...");
		db.sync(ServerFTP.DB_DIR,
				ServerFTP.DB_ARCHIVE_DIR, ServerFTP.getProvider(), null);
		System.out.println("Sync successful"); 
		System.out.println("<exit>");
		System.exit(0);
		*/
		MemoryManagement.init();
		AppSupport.checkStorageDirs();
		ServerFTP.login();
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				MainUI gui = new MainUI();
				try {
					gui.createGUI("MHS-Collections Editor");
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		});
	}
}
