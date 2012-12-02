/*
 *  The MHS-Collections Project applet contains the projects primary front-end
 *  code deployed on the website for use by end-users.
 *  Copyright © 2012  Madeira Historical Society (developed by Brian Groenke)
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

import java.util.*;

public class PageQueue {
	
	LinkedList<PageView> queue = new LinkedList<PageView>();
	int currPos = 0;
	
	public PageView getCurrent() {
		return queue.get(currPos);
	}
	
	public PageView addNew(PageView npv) {
		for(int i=currPos+1;i<queue.size();i++) {
			queue.pop();
		}
		
		queue.push(npv);
		return npv;
	}
	
	public PageView back() {
		if(currPos > 0)
			currPos--;
		return queue.get(currPos);
	}
	
	public PageView forward() {
		if(currPos < queue.size() - 1)
			currPos++;
		return queue.get(currPos);
	}
	
	public boolean isFront() {
		return currPos == queue.size() - 1;
	}
	
	public boolean isBack() {
		return currPos == 0;
	}
}
