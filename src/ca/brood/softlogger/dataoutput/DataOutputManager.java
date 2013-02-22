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
package ca.brood.softlogger.dataoutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import ca.brood.softlogger.modbus.Device;
import ca.brood.softlogger.modbus.register.RealRegister;
import ca.brood.softlogger.modbus.register.RegisterCollection;
import ca.brood.softlogger.scheduler.Scheduler;

public class DataOutputManager {
	private Map<OutputModule,Scheduler> schedulers;
	private Logger log;
	
	public DataOutputManager() {
		schedulers = new HashMap<OutputModule, Scheduler>();
		
		log = Logger.getLogger(DataOutputManager.class);
	}
	
	public void addOutputModule(OutputModule m) {
		Scheduler sched = new Scheduler();
		sched.setThreadName(m.getDescription());
		schedulers.put(m, sched);
	}
	
	public void refresh(ArrayList<Device> devices) {
		for (Device device : devices) {
			device.deleteAllOutputModules();
		}
		
		Map<OutputModule,Scheduler> newSchedulers = new HashMap<OutputModule, Scheduler>();
		
		for (Entry<OutputModule, Scheduler> entry : schedulers.entrySet()) {
			Scheduler sched = new Scheduler();
			sched.setThreadName(entry.getKey().getDescription());
			for (Device device : devices) {
				OutputModule newModule = entry.getKey().clone();
				//log.trace("Original: "+entry.getKey()+ " New: " + newModule);
				ArrayList<RealRegister> regg = device.getAllRegisters();
				for (RealRegister regd : regg) {
					//log.info(regd);
				}
				newModule.setRegisterCollection(new RegisterCollection(device.getAllRegisters()));
				sched.addSchedulee(newModule);
				device.addOutputModule(newModule);
			}
			newSchedulers.put(entry.getKey(), sched);
		}
		schedulers = newSchedulers;
	}
	
	public void start() {
		log.info("Starting");
		for (Scheduler scheduler : schedulers.values()) {
			scheduler.start();
		}
	}
	
	public void stop() {
		log.info("Received Stop Request");
		for (Scheduler scheduler : schedulers.values()) {
			scheduler.stop();
		}
	}
}
