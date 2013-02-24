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
package ca.brood.softlogger.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.log4j.Logger;

import ca.brood.softlogger.modbus.register.RealRegister;

public class CsvFileWriter {
	private String filename;
	private File file;
	private boolean writeHeaders;
	private Logger log;
	private ArrayList<String> headers;
	
	public CsvFileWriter (String fileName) {
		log = Logger.getLogger(CsvFileWriter.class);
		this.filename = fileName;
		writeHeaders = true;
		headers = new ArrayList<String>();
	}
	
	public CsvFileWriter(CsvFileWriter o) {
		this(o.filename);
		this.writeHeaders = o.writeHeaders;
		for (String s : o.headers) {
			this.headers.add(s);
		}
	}
	
	public String getFilename() {
		return filename;
	}
	
	public synchronized void setFilename(String name) {
		filename = name;
		newFile();
	}
	
	public synchronized void setHeaders(ArrayList<String> heads) {
		headers = heads;
	}
	
	private synchronized void initialize() {
		String theFileName;
		Calendar cal = Calendar.getInstance();
		theFileName = String.format("%1$tY%1$tm%1$td-%1$tT", cal)+"-"+filename;
		
		log.debug("Initializing file: "+theFileName);
		file = new File(theFileName);
		try {
			file.createNewFile();
		} catch (IOException e) {
			log.error("Error creating file", e);
		}
		writeHeaders = true;
	}
	
	public void newFile() {
		initialize();	
	}
	
	public synchronized boolean writeData(ArrayList<String> values) {
		boolean ret = false;
		
		if (file == null)
			initialize();
		if (!file.canWrite()) {
			log.debug("Can't write to file.");
			initialize();
			if (!file.canWrite()) {
				log.error("Couldn't get a writable file. Aborting");
				return false;
			}
		}
		
		FileOutputStream fos = null;
		for (int tryNo = 1; tryNo <=2; tryNo++) {
			try {
				fos = new FileOutputStream(file, true);
			} catch (Exception e) {
				if (tryNo == 1) {
					log.error("Couldn't get an output stream on the file, trying again.");
					initialize();
				} else if (tryNo == 2) {
					log.error("Failed to get an output stream on a file. Aborting.");
					return false;
				}
			}
		}
		
		try {
			boolean first = true;
			if (writeHeaders) {
				for (String head : headers) {
					if (!first)
						fos.write(", ".getBytes());
					else
						first = false;
					fos.write(head.getBytes());
				}
				fos.write(System.getProperty("line.separator").getBytes());
				writeHeaders = false;
			}
			first = true;
			for (String val : values) {
				if (!first)
					fos.write(", ".getBytes());
				else
					first = false;
				if (val.contains("\"")) {
					fos.write(("\""+val+"\"").getBytes());
				} else {
					fos.write(val.getBytes());
				}
			}
			fos.write(System.getProperty("line.separator").getBytes());
			ret = true;
		} catch (Exception e) {
			log.error("Failed in writing to file", e);
			ret = false;
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				log.error("Failed to close output stream",e);
				ret = false;
			}
		}
		return ret;
	}
}