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
import java.util.concurrent.*;

import javax.imageio.*;

import org.apache.commons.net.ftp.*;

/**
 * Implementation of DataProvider for obtaining data streams to/from a remote
 * FTP server.
 * 
 * @author Brian Groenke
 * 
 */
public class FTPProvider implements DataProvider {
	
	private static final int SO_TIMEOUT = 0x3938700, BUFF_SIZE = 0x4000; //16kB

	private FTPClient ftp = new FTPClient();
	private String address, wkdir = "/", home = wkdir;
	private volatile boolean available, login;
	private Thread ka;
	private long lastPingTime;

	/**
	 * All operations using FTPClient (with the exception for initializers)
	 * should acquire a permit from Semaphore <code>guard</code> first. One
	 * permit is available, and it is issued on a first-in-first-out basis.
	 */
	private Semaphore guard = new Semaphore(1, true);

	private final LogoutHook EXIT_HOOK = new LogoutHook();

	/**
	 * Connects to a FTP server without attempting a login.
	 * 
	 * Note: this constructor simply calls: <code>connect(address)</code>
	 * 
	 * @param address
	 *            address of the remote FTP server to connect to.
	 * @throws SocketException
	 *             if the socket timeout could not be set.
	 * @throws IOException
	 *             if the socket could not be opened.
	 */
	public FTPProvider(String address) throws SocketException, IOException {
		init();
		connect(address);
	}

	/**
	 * Create an FTPProvider that logs into and communicates with a remote FTP
	 * server at the corresponding <code>address</code>, <code>username</code>
	 * and <code>password</code>.
	 * 
	 * Note: this constructor simply calls: <code>connect(address)</code>
	 * 
	 * @param address
	 *            the remote address to connect to.
	 * @param username
	 *            the username to login with.
	 * @param password
	 *            the password to login with.
	 * @throws LoginException
	 *             if the login was unsuccessful.
	 * @throws SocketException
	 *             if the socket timeout could not be set.
	 * @throws IOException
	 *             if the socket could not be opened.
	 */
	public FTPProvider(String address, String username, String password)
			throws LoginException, SocketException, IOException {
		init();
		connect(address, username, password);
	}

	/**
	 * Performs a connection attempt to the specified address without sending login information.
	 * @param address
	 * @throws SocketException
	 * @throws IOException
	 */
	protected void connect(String address) throws SocketException, IOException {
		ftp.connect(address);
		int reply = ftp.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			throw (new IOException(
					"connection attempted failed with reply code: " + reply));
		}
		this.address = address;
		this.wkdir = ftp.printWorkingDirectory();
		login = true;
		initConn();
	}

	/**
	 * Attempts to login to the FTP server at the specified address.
	 * @param address
	 * @param username
	 * @param password
	 * @throws SocketException
	 * @throws IOException
	 */
	protected void connect(String address, String username, String password) throws SocketException, IOException {
		ftp.connect(address);
		int reply = ftp.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			throw (new IOException(
					"connection attempted failed with reply code: " + reply));
		}
		this.address = address;
		boolean success = ftp.login(username, password);
		if (!success) {
			ftp.disconnect();
			throw (new LoginException("invalid login"));
		} else {
			Runtime.getRuntime().addShutdownHook(EXIT_HOOK);
			this.wkdir = ftp.printWorkingDirectory();
			login = true;
			initConn();
		}
	}
	
	/**
	 * Internal initialization method called by constructors.  This is called once before
	 * any connection attempt is made.
	 * @throws SocketException
	 */
	protected void init() throws IOException {
		ftp.setBufferSize(BUFF_SIZE);
		ftp.setSendBufferSize(BUFF_SIZE);
		ftp.setReceiveBufferSize(BUFF_SIZE);
	}

	/**
	 * Internal connection initialization method called by connect methods. Subclasses should
	 * also call this method if not explicitly calling super constructor. <br/>
	 * <br/>
	 * Specification:<br/>
	 * Sets file transaction type to BINARY_FILE_TYPE, sets local passive mode
	 * and launches FTP keepAlive thread.
	 * 
	 * @throws IOException
	 */
	protected void initConn() throws IOException {
		ftp.setSoTimeout(SO_TIMEOUT);
		ftp.setTcpNoDelay(false);
		ftp.setFileTransferMode(FTPClient.BLOCK_TRANSFER_MODE);
		ftp.enterLocalPassiveMode(); // should help to avoid any issues with
		    // firewalls
		ftp.setFileType(FTP.BINARY_FILE_TYPE); // All file transfers by
		    // FTPProvider should be binary.

		ka = new Thread(new KeepAlive());
		ka.setDaemon(true);
		ka.setName("Ftp_keepAlive");
		ka.start();
	}

	/**
	 * Fetches the currently set working directory of the provider. The default
	 * is usually just '/' representing the absolute root directory of the
	 * system.
	 * 
	 * @return the current working directory
	 * @throws  
	 */
	@Override
	public String getWorkingDir() {
		return wkdir;
	}

	/**
	 * Fetches the home directory for this FTPProvider on the FTP server.
	 * 
	 * @return the set home directory
	 */
	@Override
	public String getHome() {
		return home;
	}

	/**
	 * Sets the working directory for this FTPProvider on the FTP server.
	 * 
	 * @throws IOException
	 * 
	 * @see #getWorkingDir()
	 */
	@Override
	public void setWorkingDir(String wkdir) throws IOException {
		try {
			guard.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ftp.changeWorkingDirectory(wkdir);
		guard.release();
		this.wkdir = wkdir;
	}

	@Override
	public String getProtocolName() {
		return "ftp";
	}

	/**
	 * Checks to see if this FTPProvider is connected, logged in and ready to be
	 * used for data transfers.
	 */
	@Override
	public boolean isAvailable() {
		try {
			guard.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		boolean available = ftp.isConnected() && ftp.isAvailable() && login;
		guard.release();
		return available;
	}

	/**
	 * Fetches an input stream from the specified file on the server. Returns
	 * null if the file doesn't exist, the stream can't be opened, or the server
	 * sends a negative reply code. The returned InputStream will take care of
	 * completing the transfer when you call <code>close()</code>.
	 */
	@Override
	public InputStream getInputStream(String fileName) throws IOException {
		try {
			guard.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		InputStream in = ftp.retrieveFileStream(fileName);
		guard.release();
		if (in == null) {
			return null;
		}
		return new FtpInputStream(in);
	}

	/**
	 * Fetches an output stream to the specified file on the server. Returns
	 * null if the stream can't be opened, or the server sends a negative reply
	 * code. The returned OutputStream will take care of completing the transfer
	 * when you call <code>close()</code>.
	 */
	@Override
	public OutputStream getOutputStream(String fileName) throws IOException {
		try {
			guard.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		OutputStream out = ftp.storeFileStream(fileName);
		guard.release();
		if (out == null) {
			return null;
		}
		return new FtpOutputStream(out);
	}

	/**
	 * Fetches an image with the given name on the server.
	 * @param fileName file name including location on server (relative to working dir).
	 */
	@Override
	public BufferedImage loadImage(String fileName) throws IOException {
		BufferedImage img = null;
		try {
			guard.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		InputStream in = ftp.retrieveFileStream(fileName);
		guard.release();
		if (in == null) {
			return null;
		}
		in = new BufferedInputStream(new FtpInputStream(in));
		img = ImageIO.read(in);
		in.close();
		return img;
	}

	/**
	 * Throws UnsupportedOperationException. DO NOT use with code that supports
	 * an audio-enabled DataProvider.
	 */
	@Override
	public Object playAudio(String audioAddr)
			throws UnsupportedOperationException {
		throw (new UnsupportedOperationException(
				"Audio playback is not supported by FTPProvider"));
	}

	/**
	 * Logout and disconnect from the server.  Note that all methods will most likely throw some sort of IOException
	 * until <code>reconnect</code> is called.
	 * @see #reconnect(String)
	 * @see #reconnect(String,String,String)
	 * @throws IOException
	 */
	public void disconnect() throws IOException {
		Runtime.getRuntime().removeShutdownHook(EXIT_HOOK);
		try {
			guard.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ftp.logout();
		ftp.disconnect();
		guard.release();
		login = false;
		ka.interrupt();
	}

	/**
	 * Reconnects this provider to a server.  This does nothing if called
	 * while the provider is already connected.
	 * @param address
	 * @throws SocketException
	 * @throws IOException
	 */
	public void reconnect(String address) throws SocketException, IOException {
		if(!login) {
			connect(address);
		}
	}

	/**
	 * Reconnects this provider to a server with a new login session.  This does nothing if called
	 * while the provider is already connected.
	 * @param address
	 * @param username
	 * @param password
	 * @throws SocketException
	 * @throws IOException
	 */
	public void reconnect(String address, String username, String password) throws SocketException, IOException {
		if(!login) {
			connect(address, username, password);
		}
	}

	/**
	 * Creates a directory at the specified path on the FTP server.
	 * 
	 * @param dirPath
	 *            the fully qualified name of the new directory (including all
	 *            parent directories).
	 * @return true if successful, false otherwise.
	 */
	public boolean mkdir(String dirPath) {
		try {
			guard.acquire();
			ftp.makeDirectory(dirPath);
			guard.release();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Deletes a file at the specified pathname.
	 * 
	 * @param pathname
	 *            fully qualified or relative pathname.
	 * @return true if successful, false otherwise.
	 */
	public boolean delete(String pathname) {
		try {
			guard.acquire();
			ftp.deleteFile(pathname);
			guard.release();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Attempts to remove a directory at the specified pathname.
	 * 
	 * @param pathname
	 * @return true if successful, false otherwise.
	 */
	public boolean rmdir(String pathname) {
		try {
			guard.acquire();
			ftp.removeDirectory(pathname);
			guard.release();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Returns a list of file names in the given directory.
	 * 
	 * @param dir
	 *            the directory whose contents to retrieve
	 * @return array of file name strings in the 'dir'
	 */
	public String[] listNames(String dir) {
		String[] list = null;
		try {
			guard.acquire();
			list = ftp.listNames(dir);
			guard.release();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * Returns a list of FTP files in the given directory.
	 * 
	 * @param dir
	 *            the directory whose contents to retrieve
	 * @return an FTPFile array representing the contents of 'dir'
	 */
	public FTPFile[] listFiles(String dir) {
		FTPFile[] files = null;
		try {
			guard.acquire();
			files = ftp.listFiles(dir);
			guard.release();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return files;
	}

	@Override
	public boolean exists(String pathname) {
		try {
			guard.acquire();
			String stat = ftp.getStatus(pathname);
			guard.release();
			if (stat != null) {
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Note: accuracy of results for FTP servers are neither guaranteed nor
	 * explicitly defined. The server may or may not provide the necessary file
	 * size information and furthermore may update this information at its own
	 * discretion.
	 */
	@Override
	public long sizeOf(String fileName) {
		try {
			guard.acquire();
			FTPFile f = ftp.mlistFile(fileName);
			guard.release();
			if (f == null) {
				return -1;
			} else {
				return f.getSize();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return -1;
	}

	/**
	 * Renames a file at the specified pathname.
	 * 
	 * @param fileName
	 *            path of the current file
	 * @param newTarget
	 *            new path of the file
	 * @return true if successful, false otherwise.
	 */
	@Override
	public boolean rename(String fileName, String newTarget) throws IOException {
		if (!exists(fileName)) {
			throw (new FileNotFoundException(fileName));
		}
		try {
			guard.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		boolean success = ftp.rename(fileName, newTarget);
		guard.release();
		return success;
	}

	/**
	 * Sends a command to the FTP server and records the time it takes to
	 * respond.
	 * 
	 * @return the amount of time in milliseconds the server took to respond.
	 * @throws IOException
	 */
	public long ping() throws IOException {
		synchronized (ftp) {
			try {
				guard.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			long t1 = System.currentTimeMillis();
			ftp.feat();
			long t2 = System.currentTimeMillis();
			guard.release();
			return t2 - t1;
		}
	}

	/**
	 * Obtains the last modification time of the file.
	 * @param fileName
	 * @return a Calendar object representing the last modification time of the file in the local time zone.
	 */
	public Calendar getLastModified(String fileName) {
		synchronized(ftp) {
			try {
				guard.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				FTPFile file = ftp.mlistFile(fileName);
				Calendar time = file.getTimestamp();
				time.setTimeZone(TimeZone.getDefault());
				return time;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * OutputStream returned by the <code>getOutputStream(String)</code> of this
	 * class. It takes care of extra operations needed when closing the stream.
	 * 
	 * @author Brian Groenke
	 * 
	 */
	protected class FtpOutputStream extends OutputStream {

		private OutputStream out;
		private boolean locked;

		public FtpOutputStream(OutputStream out) {
			if (out == null) {
				throw (new IllegalArgumentException("stream cannot be null"));
			}
			
			this.out = out;
		}

		@Override
		public void write(int b) throws IOException {
			if(!locked) {
				try {
					guard.acquire();
					locked = true;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			out.write(b);
		}

		@Override
		public void flush() throws IOException {
			out.flush();
		}

		@Override
		public void close() throws IOException {
			out.close();
			
			boolean fail = false;
			if (ftp == null || !ftp.completePendingCommand()) {
				fail = true;
			}
			
			guard.release();
			locked = false;
			
			if(fail)
			    throw (new IOException("failed to complete FTP transaction"));
		}

	}

	/**
	 * InputStream returned by the <code>getInputStream(String)</code> of this
	 * class. It takes care of extra operations needed when closing the stream.
	 * 
	 * @author Brian Groenke
	 * 
	 */
	protected class FtpInputStream extends InputStream {

		private InputStream in;
		private boolean locked;

		public FtpInputStream(InputStream in) {
			this.in = in;
		}

		@Override
		public int read() throws IOException {
			if(!locked) {
				try {
					guard.acquire();
					locked = true;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return in.read();
		}

		@Override
		public void close() throws IOException {
			in.close();

			boolean fail = false;
			if (ftp == null || !ftp.completePendingCommand()) {
				fail = true;
			}

			guard.release();
			locked = false;

			if(fail)
				throw (new IOException("failed to complete FTP transaction"));
		}

		@Override
		public void mark(int limit) {
			in.mark(limit);
		}

		@Override
		public boolean markSupported() {
			return in.markSupported();
		}

		@Override
		public void reset() throws IOException {
			in.reset();
		}

		@Override
		public long skip(long n) throws IOException {
			return in.skip(n);
		}

		@Override
		public int available() throws IOException {
			return in.available();
		}
	}

	private class LogoutHook extends Thread {

		public LogoutHook() {
			super(new Runnable() {

				@Override
				public void run() {
					try {
						login = false;
						ka.interrupt();
						ftp.logout();
						ftp.disconnect();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			});
		}
	}

	private class KeepAlive implements Runnable {

		public static final int INTERVAL = 0x493E0; // specified time in
		// milliseconds | currently
		// set to 5 mins (300,000ms)

		@Override
		public void run() {
			while (login) {
				try {
					ftp.setSoTimeout((int) (INTERVAL * 1.5));
					lastPingTime = ping();
					Thread.sleep(INTERVAL); // wait set amount of time - see
					// INTERVAL field above
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					// we really don't care
				}
			}
		}

	}
}
