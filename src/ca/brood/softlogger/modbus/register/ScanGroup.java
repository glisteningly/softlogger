/*******************************************************************************
 * Copyright (c) 2013 Charles Hache. All rights reserved. 
 * 
 * This file is part of the softlogger project.
 * softlogger is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * softlogger is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with softlogger.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Charles Hache - initial API and implementation
 ******************************************************************************/
package ca.brood.softlogger.modbus.register;

import java.util.SortedSet;
import java.util.TreeSet;
import ca.brood.softlogger.scheduler.PeriodicSchedulable;

public class ScanGroup extends PeriodicSchedulable {
	private SortedSet<RealRegister> registers;
	
	public ScanGroup(int scanRate) {
		super(scanRate);
		registers = new TreeSet<RealRegister>();
	}
	
	public int getScanRate() {
		return this.getPeriod();
	}
	
	public void addRegister(RealRegister add) {
		registers.add(add);
	}
	
	public SortedSet<RealRegister> getRegisters() {
		return registers;
	}
	
	public String toString() {
		return "ScanGroup: TTL: "+(System.currentTimeMillis()-this.getNextRun())+" scanRate: "+this.getScanRate();
	}
	
}
