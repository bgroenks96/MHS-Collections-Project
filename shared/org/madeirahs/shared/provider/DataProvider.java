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

import java.awt.image.*;
import java.io.*;
import java.net.*;

import javax.sound.sampled.*;

public interface DataProvider {

	public String getWorkingDir();

	public void setWorkingDir(String newRoot) throws IOException;

	public String getHome();

	/**
	 * Returns a readable String representation of the DataProvider's protocol
	 * type.
	 * 
	 * @return
	 */
	public String getProtocolName();

	/**
	 * Checks to see if this provider is available and ready to be used.
	 * 
	 * @return
	 */
	public boolean isAvailable();

	public InputStream getInputStream(String fileName) throws IOException;

	public OutputStream getOutputStream(String fileName) throws IOException;

	public BufferedImage loadImage(String fileName) throws IOException;

	/**
	 * Checks to see if the file exists.
	 * 
	 * @param fileName
	 * @return true if it exists or is known, false otherwise.
	 */
	public boolean exists(String fileName);

	/**
	 * Returns the size of the file or resource, if present.
	 * 
	 * @param fileName
	 *            the name of the resource as it should be recognized by the
	 *            provider (given the current configuration).
	 * @return the size (in bytes) of the file or resource. If non-existent, -1
	 *         is returned.
	 */
	public long sizeOf(String fileName);

	/**
	 * Renames the specified file to the new target location.
	 * 
	 * @param fileName
	 *            name of the file relative to the working directory
	 * @param newTarget
	 *            name of the file relative to the working directory
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 *             if the DataProvider doesn't support this operation.
	 */
	public void rename(String fileName, String newTarget) throws IOException,
			UnsupportedOperationException;
	
	/**
	 * Deletes the specified file from the provider's system.
	 * @param fileName name of the file, relative to the provider's current working directory and/or file specificaiton settings.
	 * @return true if successful, false otherwise.
	 */
	public boolean delete(String fileName);

	/**
	 * Plays audio at the specified file location, if supported. The type of
	 * Object returned depends on implementation. Read the provider's
	 * documentation for info on how it should be casted.
	 * 
	 * @param audioAddr
	 * @throws UnsupportedOperationException
	 * @throws MalformedURLException
	 * @throws LineUnavailableException
	 * @throws IOException
	 * @throws UnsupportedAudioFileException
	 */
	public Object playAudio(String audioAddr)
			throws UnsupportedOperationException, MalformedURLException,
			LineUnavailableException, IOException,
			UnsupportedAudioFileException;
}
