/*
 *  The MHS-Collections Project shared library is intended for use by both the applet
 *  and editor software in the interest of code consistency.
 *  Copyright (c) 2012-2016 Madeira Historical Society (developed by Brian Groenke)
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
import java.util.*;

import javax.sound.sampled.*;

/**
 * A DataProvider that uses multiple other providers to consolidate data access.
 * For example, an instance of this class could be created with a FileProvider
 * and a FTPProvider and would check both for all data queries.
 * 
 * @author Brian Groenke
 * 
 */
public class MultiProvider implements DataProvider {

	private Vector<DataProvider> providers;
	private int defOut, cmdTarget;

	/**
	 * Creates a new MultiProvider that consolidates the given DataProviders.
	 * The order they are listed as arguments or in the array dictates
	 * preference to which should be used for input in the case of multiple
	 * positive results.
	 * 
	 * @param providers
	 */
	public MultiProvider(DataProvider... providers) {
		if (providers == null) {
			throw (new IllegalArgumentException("null argumnet"));
		}
		this.providers = new Vector<DataProvider>();
		for (DataProvider dp : providers) {
			this.providers.add(dp);
		}
	}

	/**
	 * Fetches the set of DataProviders this MultiProvider uses.
	 * 
	 * @return a copy of the DataProviders array
	 */
	public DataProvider[] getProviders() {
		return providers.toArray(new DataProvider[providers.size()]);
	}

	/**
	 * Sets which provider in the set of providers given should be used for
	 * output.
	 * 
	 * @param provider
	 * @return true if successful, false otherwise
	 */
	public boolean setDefaultOutputProvider(DataProvider provider) {
		int ind;
		if ((ind = providers.indexOf(provider)) >= 0) {
			defOut = ind;
			return true;
		}
		return false;
	}

	/**
	 * Sets the provider to use for command methods in this MultiProvider. This
	 * must be set before the command is called if the target provider has
	 * changed.
	 * 
	 * @param provider
	 * @return true if successful, false otherwise
	 */
	public boolean setCommandTarget(DataProvider provider) {
		int ind;
		if ((ind = providers.indexOf(provider)) >= 0) {
			cmdTarget = ind;
			return true;
		}
		return false;
	}

	/**
	 * MultiProvider - Uses command target.
	 */
	@Override
	public String getWorkingDir() {
		return providers.get(cmdTarget).getWorkingDir();
	}

	/**
	 * MultiProvider - Uses command target.
	 */
	@Override
	public void setWorkingDir(String newRoot) throws IOException {
		providers.get(cmdTarget).setWorkingDir(newRoot);
	}

	/**
	 * MultiProvider - Uses command target.
	 */
	@Override
	public String getHome() {
		return providers.get(cmdTarget).getHome();
	}

	/**
	 * MultiProvider - Uses command target.
	 */
	@Override
	public String getProtocolName() {
		return providers.get(cmdTarget).getProtocolName();
	}

	/**
	 * MultiProvider - Uses command target.
	 */
	@Override
	public boolean isAvailable() {
		return providers.get(cmdTarget).isAvailable();
	}

	@Override
	public InputStream getInputStream(String fileName) throws IOException {
		InputStream in = null;
		for (int i = 0; i < providers.size(); i++) {
			InputStream get = providers.get(i).getInputStream(fileName);
			if (get != null) {
				in = get;
				break;
			}
		}
		return in;
	}

	@Override
	public OutputStream getOutputStream(String fileName) throws IOException {
		OutputStream out = null;
		for (int i = 0; i < providers.size(); i++) {
			OutputStream get = providers.get(i).getOutputStream(fileName);
			if (get != null) {
				out = get;
				break;
			}
		}
		return out;
	}

	@Override
	public BufferedImage loadImage(String fileName) throws IOException {
		BufferedImage img = null;
		for (int i = 0; i < providers.size(); i++) {
			BufferedImage get = providers.get(i).loadImage(fileName);
			if (get != null) {
				img = get;
				break;
			}
		}
		return img;
	}

	/**
	 * @return true if one or more providers recognize the file, false otherwise.
	 */
	@Override
	public boolean exists(String fileName) {
		for(DataProvider prov:providers) {
			if(prov.exists(fileName))
				return true;
		}
		
		return false;
	}

	/**
	 * MultiProvider - Uses command target.
	 */
	@Override
	public long sizeOf(String fileName) {
		return providers.get(cmdTarget).sizeOf(fileName);
	}

	/**
	 * MultiProvider - Uses command target.
	 */
	@Override
	public boolean rename(String fileName, String newTarget) throws IOException,
			UnsupportedOperationException {
		return providers.get(cmdTarget).rename(fileName, newTarget);
	}

	/**
	 * MultiProvider - Uses command target.
	 */
	@Override
	public Object playAudio(String audioAddr)
			throws UnsupportedOperationException, MalformedURLException,
			LineUnavailableException, IOException,
			UnsupportedAudioFileException {
		return providers.get(cmdTarget).playAudio(audioAddr);
	}

	/**
	 * MultiProvider - Uses command target.
	 * @return true if one or more returned a successful deletion, false otherwise.
	 */
	@Override
	public boolean delete(String fileName) {
		return providers.get(cmdTarget).delete(fileName);
	}
}
