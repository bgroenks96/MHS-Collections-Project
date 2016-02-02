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

package org.madeirahs.editor.main;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

public class MemoryManagement {

	private MemoryManagement() {
	}

	private static final int THREAD_SLEEP_TIME = 2500,
			MAX_MEM_BYTES = 1024 * 500000;

	static volatile boolean running;

	public static void init() {
		if (!running) {
			running = true;
			Thread mThread = new Thread(new UsageMonitor());
			mThread.setName("memory_usage_monitor");
			mThread.setDaemon(true);
			mThread.start();
		}
	}

	public static void stop() {
		running = false;
	}

	private static class UsageMonitor implements Runnable {

		boolean finalize, skip;

		@Override
		public void run() {
			while (running) {
				MemoryUsage heapMem = ManagementFactory.getMemoryMXBean()
						.getHeapMemoryUsage();
				MemoryUsage nonHeap = ManagementFactory.getMemoryMXBean()
						.getNonHeapMemoryUsage();
				long totalUsage = heapMem.getUsed() + nonHeap.getUsed();
				if (totalUsage > MAX_MEM_BYTES) {
					if (!finalize && !skip) {
						System.gc();
						finalize = true;
					} else if (!skip) {
						System.runFinalization();
						System.out.println("Requested finalization");
						skip = true;
					} else {
						finalize = false;
						skip = false;
					}
				}
				Thread.yield();
				try {
					Thread.sleep(THREAD_SLEEP_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
