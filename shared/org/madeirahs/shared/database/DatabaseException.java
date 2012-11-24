package org.madeirahs.shared.database;

import java.io.*;

public class DatabaseException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5408133549949325831L;

	public DatabaseException() {
		super();
	}

	public DatabaseException(String message) {
		super(message);
	}

	public DatabaseException(Throwable cause) {
		super(cause);
	}

	public DatabaseException(String message, Throwable cause) {
		super(message, cause);
	}

}
