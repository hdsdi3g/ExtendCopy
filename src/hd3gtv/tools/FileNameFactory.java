/*
 * This file is part of Java Tools for hdsdi3g'.
 * 
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * Copyright (C) hdsdi3g for hd3g.tv 2009-2012
 * 
*/
package hd3gtv.tools;

import java.io.File;

/**
 * @author hdsdi3g
 * @version 1.0
 */
public class FileNameFactory {
	
	private String sourceext;
	private File sourcedir;
	private String sourcebasename;
	private File destfile;
	private StringBuffer destfilename;
	
	public FileNameFactory(File sourcefile) {
		sourcedir = new File(sourcefile.getAbsolutePath()).getParentFile();
		sourcebasename = sourcefile.getName();
		int posdot = sourcebasename.lastIndexOf("."); //$NON-NLS-1$
		if (posdot < 1) {
			sourceext = ""; //$NON-NLS-1$
		} else {
			sourceext = sourcebasename.substring(posdot + 1, sourcebasename.length());
		}
		destfile = null;
	}
	
	public String getSourcebasename() {
		return sourcebasename;
	}
	
	public File getSourcedir() {
		return sourcedir;
	}
	
	public String getSourceext() {
		return sourceext;
	}
	
	public void setDestdir(File destdir) {
		destfilename = new StringBuffer();
		destfilename.append(destdir.getPath());
		destfilename.append(File.separator);
	}
	
	public void addValueInDestFileName(String value) {
		destfilename.append(value);
	}
	
	/**
	 * Adds a / at the end if there is not already
	 */
	public void addPathSeparator() {
		if ((destfilename.substring(destfilename.length()).equals(File.separator)) == false) {
			destfilename.append(File.separator);
		}
	}
	
	public void addSourceDirNameInDestFileName() {
		setDestdir(sourcedir);
	}
	
	public void addSourceFileNameInDestFileName() {
		destfilename.append(sourcebasename);
	}
	
	public File getDestfile() {
		if (destfile == null) {
			if (destfilename != null) {
				if (destfilename.length() > 1) {
					destfile = new File(destfilename.toString());
				}
			}
		}
		return destfile;
	}
	
}
