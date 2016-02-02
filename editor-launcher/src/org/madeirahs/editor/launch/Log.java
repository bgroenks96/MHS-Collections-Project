/*
 *  The MHS-Collections Project editor is intended for use by Historical Society members
 *  to edit, review and upload artifact information.
 *  Copyright (c) 2012-2016 Madeira Historical Society (developed by Brian Groenke)
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

package org.madeirahs.editor.launch;

import java.io.*;
import java.util.*;

public class Log implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2682254354788030260L;
	
	public Calendar time;
	public ArrayList<String> updatedPkgs = new ArrayList<String>();
	public ArrayList<Exception> exceptions = new ArrayList<Exception>();

}
