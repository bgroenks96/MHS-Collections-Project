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

package org.madeirahs.shared.database;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import javax.swing.*;

import org.madeirahs.editor.ui.*;
import org.madeirahs.shared.*;
import org.madeirahs.shared.Artifact.StringField;
import org.madeirahs.shared.Artifact.TimeField;
import org.madeirahs.shared.misc.*;
import org.madeirahs.shared.provider.*;
import org.madeirahs.shared.time.*;

/**
 * Shared class representing the artifact database for the MHS-Collections project. Database
 * provides all the means necessary for creating, editing and searching the database.
 * 
 * @author Brian Groenke
 * 
 */
public class Database implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1996081993674943784L;

	public static String DB_SUFFIX = ".store", DATABASE = "database" + DB_SUFFIX,
			DB_ENTRY_SUFFIX = ".database", DB_TMP_STORE = "tmpstore", DB_ARCHIVE_NAME_SEP = "_", UNIQUE_ID_FLAG = "#%%#";

	private static int DL_BUFF_SIZE = 5120, DB_STORE_COMPRESSION_LEVEL = 9; // 0-9; 9 being max
																			// compression

	private Artifact[] data;
	private long timestamp = -1;
	private String user;
	private transient Object dataLock = new Object();

	protected Database() {
		data = new Artifact[0];
	}

	/**
	 * Downloads, extracts, and creates an instance of Database currently published on the FTP server.
	 * @param loc
	 *            the relative directory location of the Database file.
	 * @param prov DataProvider able to locate the database store file.
	 * @param prog ProgressMonitor that will be given progress updates.  May be null.
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws ClassCastException
	 */
	public static Database getInstance(String loc, DataProvider prov,
			ProgressMonitor prog) throws IOException, ClassNotFoundException,
			ClassCastException {
		if(!prov.isAvailable())
			throw(new IOException("provider not available"));
		long size = prov.sizeOf(loc + "/" + DATABASE);
		InputStream in = prov.getInputStream(loc + "/" + DATABASE);
		if (in == null) {
			return new Database();
		}
		if (prog != null) {
			prog.setNote("Downloading database store-file...");
			in = new MonitoredInStream(in, prog, size);
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		BufferedInputStream buffin = new BufferedInputStream(in);
		byte[] buff = new byte[DL_BUFF_SIZE];
		int len = 0;
		while ((len = buffin.read(buff)) >= 0) {
			bos.write(buff, 0, len);
		}
		bos.flush();
		buffin.close();
		if (prog != null) {
			prog.setNote("Extracting database...");
		}
		ZipInputStream zipin = new ZipInputStream(new ByteArrayInputStream(
				bos.toByteArray()));
		ZipEntry dbentry = zipin.getNextEntry();
		if (!dbentry.getName().endsWith(DB_ENTRY_SUFFIX)) {
			zipin.close();
			throw (new DatabaseException("found invalid entry: " + dbentry.getName()));
		}
		@SuppressWarnings("resource")
		InputStream instream = (prog != null) ? new MonitoredInStream(zipin,
				prog, (dbentry.getSize() > 0) ? dbentry.getSize():bos.size()) : zipin;
		ObjectInputStream objIn = new ObjectInputStream(instream);
		Database loaded = (Database) objIn.readObject();
		objIn.close();
		Arrays.sort(loaded.data, new ArtifactComparator());
		return loaded;
	}

	/**
	 * Synchronize the provider with this Database. The object is serialized and added both to the
	 * archives and currently active database object. The data is written to a temporary "partial"
	 * file which is then renamed to the actual file upon completion.<br/>
	 * <br/>
	 * N.B: Forward slashes ('/') are automatically added between directories and output files. It
	 * is the responsibility of the caller to prevent occurrences of double or triple slashes ("//")
	 * if the provider won't tolerate this.
	 * 
	 * @param loc
	 *            the path of the archive directory relative to the DataProvider's working directory
	 *            (or in whatever fashion it will recognize it).
	 * @param archiveDir
	 *            the relative path of the archives directory from the given DataProvider.
	 * @param prov
	 *            the DataProvider to which data will be written.
	 * @throws IOException
	 *             if the DataProvider fails or returns null streams.
	 */
	public void sync(String loc, String archiveDir, DataProvider prov, String usr,
			ProgressMonitor prog) throws IOException {
		if(!prov.isAvailable())
			throw(new IOException("provider not available"));
		long currTime = System.currentTimeMillis();
		this.timestamp = currTime;
		this.user = usr;
		if(this.user != null && this.user.isEmpty())
			this.user = null;
		OutputStream a = prov.getOutputStream(loc + "/" + currTime + DB_TMP_STORE
				+ UploadUI.PAR_EXT);
		if (a == null) {
			throw (new IOException("provider returned null output stream"));
		}

		if (prog != null) {
			prog.setNote("Compressing database... Please wait");
		}
		ByteArrayOutputStream storeStream = new ByteArrayOutputStream();
		ZipOutputStream zipout = new ZipOutputStream(
				(prog != null) ? new MonitoredOutStream(storeStream, prog,
						Integer.MAX_VALUE) : storeStream);
		zipout.setLevel(DB_STORE_COMPRESSION_LEVEL);
		ZipEntry dbentry = new ZipEntry(currTime + DB_ENTRY_SUFFIX);
		zipout.putNextEntry(dbentry);
		ObjectOutputStream objOut = new ObjectOutputStream(zipout);
		objOut.writeObject(this);
		dbentry.setSize(storeStream.size());
		objOut.close();
		if (prog != null) {
			prog.setNote("Uploading database store-file...");
		}
		OutputStream curr = (prog != null) ? new MonitoredOutStream(
				new BufferedOutputStream(a), prog, storeStream.size())
				: new BufferedOutputStream(a);
		ByteArrayInputStream fromStore = new ByteArrayInputStream(
				storeStream.toByteArray());
		byte[] buff = new byte[DL_BUFF_SIZE];
		int len = 0;
		while ((len = fromStore.read(buff)) > 0) {
			curr.write(buff, 0, len);
		}

		curr.close();

		prov.rename(loc + "/" + currTime + DB_TMP_STORE + UploadUI.PAR_EXT, loc
				+ "/" + DATABASE);

		// ------ Write to archives -------//
		fromStore.reset(); // reset byte-array stream's buffer pos
		OutputStream b = prov.getOutputStream(archiveDir + "/" + currTime + DB_ARCHIVE_NAME_SEP
				+ DATABASE);
		if (b == null) {
			throw (new IOException("provider returned null output stream"));
		}
		if (prog != null) {
			prog.setNote("Archiving database store-file...");
		}
		OutputStream archives = (prog != null) ? new MonitoredOutStream(
				new BufferedOutputStream(b), prog, storeStream.size())
				: new BufferedOutputStream(b);
		buff = new byte[DL_BUFF_SIZE];
		while ((len = fromStore.read(buff)) > 0) {
			archives.write(buff, 0, len);
		}

		archives.close();
	}
	
	/**
	 * Writes this Database to a store file at the given location.
	 * @param dir
	 * @throws IOException
	 */
	public void writeLocal(String dir) throws IOException {
		long currTime = System.currentTimeMillis();
		
		ByteArrayOutputStream storeStream = new ByteArrayOutputStream();
		ZipOutputStream zipout = new ZipOutputStream(storeStream);
		zipout.setLevel(DB_STORE_COMPRESSION_LEVEL);
		zipout.putNextEntry(new ZipEntry(currTime + DB_ENTRY_SUFFIX));
		ObjectOutputStream objOut = new ObjectOutputStream(zipout);
		objOut.writeObject(this);
		objOut.close();
		
		FileOutputStream fileOut = new FileOutputStream(dir + File.separator + currTime + DB_SUFFIX);
		ByteArrayInputStream storeIn = new ByteArrayInputStream(storeStream.toByteArray());
		byte[] buff = new byte[DL_BUFF_SIZE];
		int len = 0;
		while((len=storeIn.read(buff)) > 0) {
			fileOut.write(buff, 0, len);
		}
		fileOut.close();
		storeIn.close();
	}

	/**
	 * Implicitly overridden method that the JVM looks for when deserializing.  Instantiates the lock Object.
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		dataLock = new Object();
	}

	/**
	 * Returns the time in millis that the Database was uploaded to the server (last
	 * synced/updated).
	 * 
	 * @return the last upload/sync time in System.currentTimeMillis() timebase or -1 if never
	 *         uploaded.
	 */
	public long getTimestamp() {
		return timestamp;
	}
	
	/**
	 * Returns the last recorded user who committed changes to the database.
	 * @return the username as a String or null if unknown
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Adds a new Artifact to the database. Note that the server database-store will not be updated
	 * until <code>sync</code> is called.
	 * 
	 * @param e
	 *            the Artifact to add
	 * @throws DuplicateArtifactException
	 *             if the artifact already exists in the database. This exception should be caught
	 *             and processed accordingly.
	 * @see #addAndOverwrite(Artifact)
	 */
	public synchronized void add(Artifact e) throws DuplicateArtifactException {
		_add(e, false);
	}

	/**
	 * Adds a new Artifact to the database or overwrites an existing one. No exception will be
	 * thrown if an Artifact with the same accession number already exists, but the data position
	 * will be quietly reassigned. <br/>
	 * <br/>
	 * <b>Note: It is highly recommended that you call <code>add</code> before using this method.
	 * This verifies that you are aware the Artifact already exists and gives you a chance to notify
	 * the user, if applicable.</b>
	 * 
	 * @param e
	 *            the Artifact to add or overwrite
	 * @see #add(Artifact)
	 */
	public synchronized void addAndOverwrite(Artifact e) {
		try {
			_add(e, true);
		} catch (DuplicateArtifactException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * <Internal Method>
	 * Actually performs action of synchronizing on the object lock and putting the Artifact into the Database.
	 * @param e
	 * @param forceOverwrite
	 * @throws DuplicateArtifactException
	 */
	private synchronized void _add(Artifact e, boolean forceOverwrite)
			throws DuplicateArtifactException {
		if (e == null) {
			return;
		}

		int ind = contains(e);
		if (ind >= 0) {
			if (forceOverwrite) {
				synchronized (dataLock) {
					data[ind] = e;
					return;
				}
			} else {
				throw (new DuplicateArtifactException(
						"Artifact already exists in Database"));
			}
		}

		synchronized (dataLock) {
			data = Arrays.copyOf(data, data.length + 1);
			data[data.length - 1] = e;
		}
	}

	/**
	 * Removes the given Artifact (or any artifact with the same accession number) from the
	 * database.
	 * 
	 * @param e
	 *            the Artifact to remove.
	 * @return true if successfully removed, false otherwise.
	 */
	public synchronized boolean remove(Artifact e) {
		if (e == null) {
			return false;
		}

		synchronized (dataLock) {
			int pos = contains(e);
			if (pos >= 0) {
				data[pos] = null;
				Arrays.sort(data, new PushNullComparator<Artifact>());
				data = Arrays.copyOf(data, data.length - 1);
				Arrays.sort(data, new ArtifactComparator());
				return true;
			}

			return false;
		}
	}

	/**
	 * Uses a binary search algorithm to find the specified Artifact in the database. The value
	 * returned will be < 0 if nothing was found.
	 * 
	 * @param e
	 * @return a value >= 0 that represents the Artifact's position in the database array or < 0 if
	 *         no match was found.
	 */
	public synchronized int contains(Artifact e) {
		int pos = -1;
		if (e == null) {
			return pos;
		}
		synchronized (dataLock) {
			ArtifactComparator comp = new ArtifactComparator();
			Artifact[] wcpy = Arrays.copyOf(data, data.length);
			Arrays.sort(wcpy, comp);
			pos = Arrays.binarySearch(wcpy, e, comp);
		}

		return pos;
	}

	/**
	 * Returns an array that is a direct copy of the Database's Artifact array.
	 * 
	 * @return
	 */
	public synchronized Artifact[] getData() {
		Artifact[] copy = new Artifact[data.length];
		synchronized (dataLock) {
			System.arraycopy(data, 0, copy, 0, data.length);
		}
		return copy;
	}

	/**
	 * @return the size of the database's Artifact array (int value).
	 */
	public int getSize() {
		return data.length;
	}

	/**
	 * Search the database for the specified time attribute of the given Artifact.
	 * @param field denotes which time-related Artifact attribute to obtain.
	 * @param query the Artifact to be used as the query; only the specified field will be evaluated
	 * @return an array of relevant search results.
	 */
	public Artifact[] searchByField(TimeField field, Artifact query) {
		Artifact[] found = null;
		String fieldName = field.getFieldName();
		synchronized (dataLock) {
			// Create a map so we can identify the orignal Artifacts after
			// searching the values.
			HashMap<TimeSpec, Artifact> mapvals = new HashMap<TimeSpec, Artifact>();
			for (Artifact a : data) {
				mapvals.put(timefield(a, fieldName), a);
			}

			TimeSpec qspec = timefield(query, fieldName);
			TimeValueCrawler tvc = new TimeValueCrawler(qspec);
			TimeSpec[] specs = mapvals.keySet().toArray(
					new TimeSpec[mapvals.size()]);
			Arrays.sort(specs, tvc);
			found = new Artifact[specs.length];

			// Copy into the result array top-down so that most relevant results
			// are at front of array.
			// Irrelevant results are weeded out by the call to isRelevant()
			int fc = 0;
			for (int i = specs.length - 1; i >= 0; i--) {
				if (tvc.isRelevant(specs[i])) {
					found[fc] = mapvals.get(specs[i]);
					fc++;
				}
			}
			found = Arrays.copyOf(found, fc);
		}
		return found;
	}
	
	/**
	 * Search the database for the specified time attribute of the given Artifact, using
	 * the String representation of the time instead of absolute time comparison.  This search method will
	 * use the StringCrawler (like {@link searchByField(StringField, Artifact)}) to find results that are relevant
	 * to the time representation.
	 * @param field denotes which time-related Artifact attribute to obtain.
	 * @param query the Artifact to be used as the query; only the specified field will be evaluated
	 * @return an array of relevant search results.
	 * @see #StringCrawler #searchByField(TimeField, Artifact) #searchByField(StringField, Artifact)
	 */
	public Artifact[] searchByFieldHybrid(TimeField field, Artifact query) {
		Artifact[] found = null;
		String fieldName = field.getFieldName();
		synchronized (dataLock) {
			// Create a map so we can identify the orignal Artifacts after
			// searching the values.
			HashMap<String, Artifact> mapvals = new HashMap<String, Artifact>();
			for (Artifact a : data) {
				mapvals.put(timefield(a, fieldName).toString(), a);
			}
			
			TimeSpec qspec = timefield(query, fieldName);
			StringCrawler strc = new StringCrawler(qspec.toString());
			String[] specs = mapvals.keySet().toArray(
					new String[mapvals.size()]);
			Arrays.sort(specs, strc);
			found = new Artifact[specs.length];
			
			// Copy into the result array top-down so that most relevant results
			// are at front of array.
			// Irrelevant results are weeded out by the call to isRelevant()
			int fc = 0;
			for (int i = specs.length - 1; i >= 0; i--) {
				if (strc.isRelevant(specs[i])) {
					found[fc] = mapvals.get(specs[i]);
					fc++;
				}
			}
			found = Arrays.copyOf(found, fc);
		}
		return found;
	}

	/**
	 * Search the database for the specified String attribute of the given Artifact.
	 * @param field denotes which Artifact String attribute to obtain.
	 * @param query the Artifact to be used as the query; only the specified field will be evaluated
	 * @return
	 */
	// Yes I copy/pasted again.
	public Artifact[] searchByField(StringField field, Artifact query) {
		Artifact[] found = null;
		String fieldName = field.getFieldName();
		synchronized (dataLock) {
			// Create a map so we can identify the original Artifacts after
			// searching the values.
			HashMap<String, Artifact> mapvals = new HashMap<String, Artifact>();
			for (int i = 0; i < data.length;i++) {
				Artifact a = data[i];
				mapvals.put(strfield(a, fieldName) + UNIQUE_ID_FLAG + i, a);
			}

			String qstr = strfield(query, fieldName);
			StringCrawler tvc = new StringCrawler(qstr);
			String[] strs = mapvals.keySet()
					.toArray(new String[mapvals.size()]);
			Arrays.sort(strs, tvc);
			found = new Artifact[strs.length];

			// Copy into the result array top-down so that most relevant results
			// are at front of array.
			// Irrelevant results are weeded out by the call to isRelevant()
			int fc = 0;
			for (int i = strs.length - 1; i >= 0; i--) {
				if (tvc.isRelevant(strs[i])) {
					found[fc] = mapvals.get(strs[i]);
					fc++;
				}
			}
			found = Arrays.copyOf(found, fc);
		}
		
		return found;
	}

	/**
	 * <Internal Method> Convenience method to fetch field value from object.
	 * 
	 * @param a
	 * @param name
	 *            name of the field
	 * @return value of the field
	 */
	private TimeSpec timefield(Artifact a, String name) {
		TimeSpec ts = null;
		try {
			ts = (TimeSpec) a.getClass().getField(name).get(a);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return ts;
	}

	/**
	 * <Internal Method> Convenience method to fetch field value from object.
	 * 
	 * @param a
	 * @param name
	 *            name of the field
	 * @return value of the field
	 */
	private String strfield(Artifact a, String name) {
		String str = null;
		try {
			str = (String) a.getClass().getField(name).get(a);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return str;
	}

	/**
	 * Compares Artifact objects by accession number. No other fields are checked.
	 * 
	 * @author Brian Groenke
	 * 
	 */
	private static class ArtifactComparator implements Comparator<Artifact> {

		@Override
		public int compare(Artifact o1, Artifact o2) {
			return o1.accNum.compareTo(o2.accNum);
		}

	}

	/**
	 * Provides a String evaluation and 2v1 comparison algorithm for finding relevant String based search results.
	 * @author Brian Groenke
	 *
	 */
	private static class StringCrawler extends DatabaseCrawler<String> {

		private final String[] COMMON = new String[] { "the", "a", "of", "an",
				"and", "but" };

		protected StringCrawler(String a) {
			super(a);
			Arrays.sort(COMMON);
		}

		/**
		 * Uses <code>_compare(String)</code> to see which String has a greater number of
		 * patterns in the query.
		 */
		@Override
		public int compare(String a, String b) {
			a = a.substring(0, a.indexOf(UNIQUE_ID_FLAG));
			b = b.substring(0, b.indexOf(UNIQUE_ID_FLAG));
			int aptn = 0, bptn = 0;
			aptn = _compare(a);
			bptn = _compare(b);
			
			if (aptn > bptn) {
				return 1;
			} else if (aptn == bptn) {
				return 0;
			} else {
				return -1;
			}
		}
		
		@Override
		public boolean isRelevant(String a) {
			a = a.substring(0, a.indexOf(UNIQUE_ID_FLAG));
			int aptn = _compare(a);
			return aptn >= 1;
		}
		
		/**
		 * Search algorithm that compares the given String value to the query.
		 * The query is split up by whitespace into separate words.  Then <code>indexOf</code> is
		 * called repeatedly on <code>a</code> to find each occurrence.  The total number of recorded
		 * occurrences for each part of the query is returned.
		 * @param a
		 * @return
		 */
		private int _compare(String a) {
			String query = removeDoubleWhitespace(this.query.toLowerCase());
			a = removeDoubleWhitespace(a.toLowerCase());

			int aptn = 0;
			String[] qpts = query.split("\\s+");
			for(String s:qpts) {
				if(Arrays.binarySearch(COMMON, s) >= 0)
					continue;
				int next = -1;
				while((next=a.indexOf(s, next + 1)) >= 0) {
					aptn++;
				}
			}
			
			return aptn;
		}

		/**
		 * Removes double whitespace from the String to prevent the algorithm from getting messed up.
		 * @param str
		 * @return
		 */
		private String removeDoubleWhitespace(String str) {
			StringBuilder sb = new StringBuilder();
			boolean lsw = false;
			for (char c : str.toCharArray()) {
				if (!lsw) {
					sb.append(c);
				}
				if (Character.isWhitespace(c) && (c != '\n' && c != '\r')) {
					lsw = true;
				} else if (lsw) {
					lsw = false;
					sb.append(c);
				}
			}

			return sb.toString();
		}

	}

	/**
	 * Provides a simple method for comparing time related aspects of Artifacts and searching the Database for relevant results.
	 * @author Brian Groenke
	 *
	 */
	private static class TimeValueCrawler extends DatabaseCrawler<TimeSpec> {

		protected TimeValueCrawler(TimeSpec a) {
			super(a);
		}

		@Override
		public int compare(TimeSpec a, TimeSpec b) {
			long adiff = a.getStartDate().compareTo(query.getStartDate())
					+ a.getEndDate().compareTo(query.getEndDate());
			long bdiff = b.getStartDate().compareTo(query.getStartDate())
					+ b.getEndDate().compareTo(query.getEndDate());
			if (adiff == bdiff) {
				return 0;
			} else if (adiff < bdiff) {
				return 1;
			} else {
				return -1;
			}
		}

		@Override
		public boolean isRelevant(TimeSpec a) {
			return a.contains(query);
		}
	}
}
