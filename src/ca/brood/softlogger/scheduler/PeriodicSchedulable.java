/*******************************************************************************
 * Copyright (c) 2013-2016 Charles Hache <chache@cygnustech.ca>.  
 * All rights reserved. 
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
 * along with softlogger.  If not, see <https://www.gnu.org/licenses/gpl-3.0.en.html>.
 * 
 * Contributors:
 *     Charles Hache <chache@cygnustech.ca> - initial API and implementation
 ******************************************************************************/

package ca.brood.softlogger.scheduler;

public class PeriodicSchedulable implements Schedulable, Comparable<Schedulable> {

	protected volatile long nextRun;	//in nanoseconds
	protected volatile int period;	//in milliseconds
	protected Runnable action;
	
	public PeriodicSchedulable() {
		period = Integer.MAX_VALUE;
		nextRun = Long.MAX_VALUE;
	}
	
	public PeriodicSchedulable(PeriodicSchedulable o) {
		nextRun = o.nextRun;
		period = o.period;
		action = o.action;
	}
	
	public PeriodicSchedulable clone() {
		return new PeriodicSchedulable(this);
	}
	
	public PeriodicSchedulable(int period) {
		setPeriod(period);
	}
	public PeriodicSchedulable(int period, Runnable action) {
		this(period);
		this.action = action;
	}
	
	public synchronized int getPeriod() {
		return period;
	}
	
	public synchronized void setPeriod(int p) {
		period = p;
		setNextRun(System.nanoTime() + (getPeriod()*1000000l));
	}

	@Override
	public void execute() {
		if (this.action != null) {
			this.action.run();
		}
		
		updateNextRunWithPeriod();
	}
	
	public void setAction(Runnable action) {
		this.action = action;
	}

	@Override
	public int compareTo(Schedulable other) {
		return (int)(this.getNextRun() - other.getNextRun());
	}
	@Override
	public synchronized long getNextRun() {
		return nextRun;
	}
	
	public synchronized void setNextRun(long nr) {
		nextRun = nr;
	}
	
	//The strategy for getting the next run time:
	//If the scheduler was paused for a long while, it could be
	//our next run is far in the past.  If we add the period to
	//it then that could still be in the past.  In this case we
	//add the period to the current time.  Otherwise, we add the
	//period to the previous next run time - the effect of this
	//is that in general the periods between the run times are 
	//constant, regardless of how long a schedulee's action takes 
	//to run.
	protected synchronized void updateNextRunWithPeriod() {
		long oldNextRun = getNextRun();
		long periodNanos = getPeriod() * 1000000l;
		long curTime = System.nanoTime();
		if ((curTime - oldNextRun) < 2l * periodNanos) {
			setNextRun(oldNextRun + periodNanos);
		} else {
			setNextRun(curTime + periodNanos);
		}
	}
}
