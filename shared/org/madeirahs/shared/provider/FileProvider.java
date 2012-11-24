/*
 *  The MHS-Collections Project shared library is intended for use by both the applet
 *  and editor software in the interest of code consistency.
 *  Copyright © 2012-  Madeira Historical Society (developed by Brian Groenke)
 *
 *  This library is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.madeirahs.shared.provider;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class FileProvider implements DataProvider {

	private String home, wdir;
	private Map<String, File> listings = new HashMap<String, File>();

	public FileProvider() {
		wdir = File.separator;
		home = System.getProperty("user.home");
	}

	@Override
	public String getWorkingDir() {
		return wdir;
	}

	@Override
	public String getHome() {
		return home;
	}

	@Override
	public String getProtocolName() {
		return "file";
	}

	@Override
	public void setWorkingDir(String wkdir) {
		this.wdir = wkdir;
	}

	@Override
	public boolean isAvailable() {
		File file = new File(wdir);
		if (file.canRead() && file.canWrite() && file.canExecute()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public InputStream getInputStream(String fileName) throws IOException {
		return new FileInputStream(
				(listings.get(fileName) != null) ? listings.get(fileName)
						: new File(wdir + File.separator + fileName));
	}

	@Override
	public OutputStream getOutputStream(String fileName) throws IOException {
		return new FileOutputStream(
				(listings.get(fileName) != null) ? listings.get(fileName)
						: new File(wdir + File.separator + fileName));
	}

	@Override
	public BufferedImage loadImage(String fileName) throws IOException {
		BufferedImage img = null;
		File f = listings.get(fileName);
		if (f == null) {
			f = new File(wdir + File.separator + fileName);
		}
		if (f.exists()) {
			img = ImageIO.read(f);
		}
		return img;
	}

	/**
	 * Plays audio at the specified file location (uses getInputStream to obtain
	 * it).
	 * 
	 * @return a javax.sound.sampled.Clip object to control playback.
	 */
	@Override
	public Object playAudio(String audioAddr) throws LineUnavailableException,
			IOException, UnsupportedAudioFileException {
		Clip clip = AudioSystem.getClip();
		clip.open(AudioSystem.getAudioInputStream(getInputStream(audioAddr)));
		clip.start();
		return clip;
	}

	public void putListing(String filename, File file) {
		listings.put(filename, file);
	}

	public void removeListing(String filename) {
		listings.remove(filename);
	}

	public void putAll(Map<String, File> m) {
		listings.putAll(m);
	}

	public void clearListings() {
		listings.clear();
	}

	/**
	 * 
	 * @param listing
	 * @return The File object representing the file name stored in the
	 *         Provider's memory.
	 */
	public File getLocalValueOf(String listing) {
		return listings.get(listing);
	}

	@Override
	public boolean exists(String fileName) {
		return (listings.get(fileName) != null) ? listings.get(fileName)
				.exists() : new File(wdir + File.separator + fileName).exists();
	}

	@Override
	public long sizeOf(String fileName) {
		long size = -1;
		File sto = listings.get(fileName);
		if (sto != null) {
			size = sto.length();
		} else {
			File f = new File(wdir + File.separator + fileName);
			size = f.exists() ? f.length() : -1;
		}
		return size;
	}

	/**
	 * @param fileName
	 *            when using FileProvider, the first parameter (the current file
	 *            name) can be supplied as just a name if a registered listing.
	 */
	@Override
	public void rename(String fileName, String newTarget) throws IOException,
			UnsupportedOperationException {
		File f = listings.get(fileName);
		if (f == null) {
			f = new File(wdir + File.separator + fileName);
		}
		if (!f.exists()) {
			throw (new FileNotFoundException(fileName));
		}
		f.renameTo(new File(wdir + File.separator + newTarget));
	}

	@Override
	public boolean delete(String fileName) {
		return (listings.get(fileName) != null) ? listings.get(fileName)
				.exists() : new File(wdir + File.separator + fileName).delete();
	}

	// FIXME This may cause problems later if the listings map becomes out of
	// sync with the file system.
}
