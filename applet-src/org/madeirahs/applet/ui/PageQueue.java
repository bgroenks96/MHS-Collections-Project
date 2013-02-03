/*
 *  The MHS-Collections Project applet contains the projects primary front-end
 *  code deployed on the website for use by end-users.
 *  Copyright © 2012-2013 Madeira Historical Society (developed by Brian Groenke)
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
 *  
 *  Note: This class was borrowed from the Groenke Commons Java API with full permission from the author.
 */

package org.madeirahs.applet.ui;

import java.awt.*;
import java.util.*;

public class PageQueue {
	
	LinkedList<Container> queue = new LinkedList<Container>();
	int currPos = 0;
	
	public Container getCurrent() {
		return queue.get(currPos);
	}
	
	public Container getPrevious() {
		return (currPos < queue.size() - 1) ? queue.get(currPos + 1):queue.get(currPos);
	}
	
	public Container addNew(Container npv) {
		for(int i=currPos-1;i >= 0;i--) {
			queue.pop();
		}
		
		queue.push(npv);
		forward();
		return npv;
	}
	
	public Container back() {
		if(currPos < queue.size() - 1)
			currPos++;
		return queue.get(currPos);
	}
	
	public Container forward() {
		if(currPos > 0)
			currPos--;
		return queue.get(currPos);
	}
	
	public boolean isFront() {
		return currPos == queue.size() - 1;
	}
	
	public boolean isBack() {
		return currPos == 0;
	}
	
	public void clear() {
		queue.clear();
	}
}
