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
