/*
 *  The MHS-Collections Project shared library is intended for use by both the applet
 *  and editor software in the interest of code consistency.
 *  Copyright © 2012-2013 Madeira Historical Society (developed by Brian Groenke)
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

import javax.imageio.*;
import javax.sound.sampled.*;

/**
 * Provides a convenient way to access content on a server via a standard HTTP
 * connection. Except for the initial connection to the given URL, this provider
 * doesn't hold an ongoing session. Each call to a method such as
 * <code>getInputStream</code> opens a new connection to a new URL (using the
 * root URL). <br/>
 * <br/>
 * Note: future implementations of HTTPProvider may provide a more
 * fully-featured way of communicating with HTTP servers.
 * 
 * @author Brian Groenke
 * 
 */
public class HTTPProvider implements DataProvider {

	HttpURLConnection rootConn;
	String root, wdir;
	boolean dced;

	public HTTPProvider(URL url) throws IOException {
		rootConn = (HttpURLConnection) url.openConnection();
		InputStream in = rootConn.getInputStream();
		in.close();
		root = url.toString();
		wdir = root + "/";
		Thread hook = new Thread(new Runnable() {

			@Override
			public void run() {
				if (!dced) {
					rootConn.disconnect();
				}
			}
		});
		hook.setName("HTTPProvider_disconnect_hook");
		Runtime.getRuntime().addShutdownHook(hook);
	}

	public HTTPProvider(String url) throws IOException {
		this(new URL(url));
	}

	/**
	 * "Disconnects" from the server. You shouldn't attempt any more operations
	 * after calling this method.
	 */
	public void disconnect() {
		rootConn.disconnect();
		dced = true;
	}

	@Override
	public String getWorkingDir() {
		return wdir;
	}

	@Override
	public String getHome() {
		return root;
	}

	@Override
	public String getProtocolName() {
		return "http";
	}

	@Override
	public void setWorkingDir(String wkdir) {
		wdir = root + "/" + wkdir;
	}

	/**
	 * Checks availability by opening a stream to the root URL. Upon failure,
	 * false will be returned.
	 */
	@Override
	public boolean isAvailable() {
		boolean a = false;
		try {
			InputStream in = rootConn.getInputStream();
			if (in != null) {
				a = true;
			}
			in.close();
		} catch (IOException e) {
		}

		return a;
	}

	@Override
	public InputStream getInputStream(String loc) throws IOException {
		URLConnection conn = new URL(root + "/" + loc).openConnection();
		conn.setDoInput(true);
		return conn.getInputStream();
	}

	@Override
	public BufferedImage loadImage(String loc) throws IOException {
		InputStream in = getInputStream(loc);
		BufferedImage bi = ImageIO.read(in);
		in.close();
		return bi;
	}

	/**
	 * Plays audio at the specified address and returns a
	 * javax.sound.sampled.Clip object.
	 * 
	 * @param audioAddr
	 * @return a Clip object that can be used to control playback.
	 * @throws MalformedURLException
	 * @throws LineUnavailableException
	 * @throws IOException
	 * @throws UnsupportedAudioFileException
	 */
	@Override
	public Object playAudio(String audioAddr) throws MalformedURLException,
			LineUnavailableException, IOException,
			UnsupportedAudioFileException {
		Clip clip = AudioSystem.getClip();
		clip.open(AudioSystem.getAudioInputStream(new URL(root + "/"
				+ audioAddr)));
		clip.start();
		return clip;
	}

	@Override
	public OutputStream getOutputStream(String loc) throws IOException {
		URLConnection conn = new URL(root + "/" + loc).openConnection();
		conn.setDoOutput(true);
		return conn.getOutputStream();
	}

	@Override
	public boolean exists(String fileName) {
		try {
			InputStream in = new URL(root + "/" + fileName).openConnection()
					.getInputStream();
			in.close();
			return in != null;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
		}
		return false;
	}

	/**
	 * Uses the content-Length header field to obtain length of content. The
	 * server may or may not specify the size of the file.
	 * 
	 * @return the size of the content in bytes or -1 if not available
	 */
	@Override
	public long sizeOf(String fileName) {
		try {
			return new URL(root + "/" + fileName).openConnection()
					.getContentLength();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
		}

		return -1;
	}

	@Override
	public boolean rename(String fileName, String newTarget)
			throws UnsupportedOperationException {
		throw (new UnsupportedOperationException(
				"not supported by HTTPProvider"));
	}

	/**
	 * Sends a DELETE command for the specified resource on the server using HTTP.  This is likely to fail in most cases.
	 * @return true if successful, false otherwise.
	 */
	@Override
	public boolean delete(String fileName) {
		int resp = -1;
		try {
			HttpURLConnection httpCon = (HttpURLConnection) new URL(root + "/" + fileName).openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestProperty(
			    "Content-Type", "application/x-www-form-urlencoded" );
			httpCon.setRequestMethod("DELETE");
			resp = httpCon.getResponseCode();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (resp == HttpURLConnection.HTTP_OK);
	}
}
