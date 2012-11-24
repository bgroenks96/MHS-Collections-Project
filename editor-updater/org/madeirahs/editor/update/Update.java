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

package org.madeirahs.editor.update;

import java.awt.Desktop;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Update {

	public static final String LAUNCHER_URL = "http://www.madeirahs.org/collection/bin/launcher.jar", TMP_FILE = "loc.tmp";

	/**
	 * @param args
	 * @throws UnsupportedLookAndFeelException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, FileNotFoundException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		BufferedReader br = new BufferedReader(new FileReader(new File(System.getProperty("java.io.tmpdir") + TMP_FILE)));
		String loc = null;
		try {
			loc = br.readLine();
			br.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		new File(System.getProperty("java.io.tmpdir")+TMP_FILE).deleteOnExit();
		File out = new File(loc);
		System.out.println("Using " + out + " as output file");
		File tmp = new File(System.getProperty("java.io.tmpdir") + File.separator + "tmp_" + out.getName());

		try {
			URL dlurl = new URL(LAUNCHER_URL);
			ProgressMonitorInputStream in = new ProgressMonitorInputStream(null, "Downloading updated launcher...", dlurl.openStream());
			in.getProgressMonitor().setMillisToDecideToPopup(0);
			in.getProgressMonitor().setMillisToPopup(500);
			BufferedOutputStream buffout = new BufferedOutputStream(new FileOutputStream(tmp));
			byte[] buff = new byte[8192];
			int len = 0;
			while((len = in.read(buff)) > 0) {
				buffout.write(buff, 0, len);
			}
			buffout.close();
			in.close();
			
			out.delete();
			tmp.renameTo(out);
			
			Desktop.getDesktop().open(out);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail(e.toString());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.toString());
		} finally {
			tmp.delete();
		}
	}

	static void fail(String message) {
		JOptionPane.showMessageDialog(null, message, "Fatal Error - Unable to Run Updater", JOptionPane.ERROR_MESSAGE);
		System.exit(-1);
	}

}
