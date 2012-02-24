/*
 * This file is part of ExtendCopy.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * Copyright (C) hdsdi3g for hd3g.tv 2009-2012
 * 
*/
package hd3gtv.extendcopy;

import hd3gtv.tools.FileNameFactory;
import hd3gtv.tools.TimeUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

@SuppressWarnings("nls")
/**
 * @author hdsdi3g
 * @version 1.0
 */
public class DirListing {
	
	private static String lineseparator = System.getProperty("line.separator"); //$NON-NLS-1$
	private static final String COLS_SEPARATOR = "\t"; //$NON-NLS-1$
	private long globalsize;
	private long filescount;
	private long dirscount;
	private long errorcount;
	private OutputStreamWriter osw_fout;
	private OutputStreamWriter osw_dout;
	private OutputStreamWriter osw_foutfs;
	private HashMap<String, Boolean> exclude_files;
	private ExtendCopyOptions options;
	
	public DirListing() {
		exclude_files = new HashMap<String, Boolean>();
		addExcludedFile("Icon\r"); //$NON-NLS-1$
	}
	
	public void addExcludedFile(String filename) {
		if (filename != null) {
			exclude_files.put(filename, true);
		}
	}
	
	public void doJob(ExtendCopyOptions options) {
		this.options = options;
		process();
	}
	
	private void process() {
		if (options.isDocopy() & (options.getFilelist() != null)) {
			if (options.getFilelist().exists()) {
				System.err.println("List exists (" + options.getFilelist().getName() + ") : ignore listing, go to copy.");
				return;
			}
		}
		
		File fromdir = null;
		/**
		 * Chech
		 */
		try {
			if (options.getFrom() == null) {
				throw new FileNotFoundException("no from is null");
			}
			fromdir = new File(options.getFrom());
			if (fromdir.exists() == false) {
				throw new FileNotFoundException(fromdir.getPath());
			}
			if (fromdir.isDirectory() == false) {
				throw new FileNotFoundException(fromdir.getPath() + " is not a directory.");
			}
			if (fromdir.canRead() == false) {
				throw new FileNotFoundException(fromdir.getPath() + " can't read.");
			}
		} catch (FileNotFoundException e) {
			ExtendCopy.catchError(e, true);
		}
		
		/**
		 * Open streams
		 */
		try {
			if (options.getSimplefilelist() != null) {
				osw_fout = new OutputStreamWriter(new FileOutputStream(options.getSimplefilelist()));
			}
			if (options.getDirlist() != null) {
				osw_dout = new OutputStreamWriter(new FileOutputStream(options.getDirlist()));
			}
			if (options.getFilelist() != null) {
				osw_foutfs = new OutputStreamWriter(new FileOutputStream(options.getFilelist()));
			}
			
		} catch (FileNotFoundException e) {
			ExtendCopy.catchError(e, true);
		}
		
		if (options.isExclude()) {
			addExcludedFile(".DS_Store"); //$NON-NLS-1$
			addExcludedFile("Thumbs.db"); //$NON-NLS-1$
			addExcludedFile("desktop.ini"); //$NON-NLS-1$
		}
		
		/**
		 * Watching...
		 */
		globalsize = 0;
		filescount = 0;
		dirscount = 0;
		errorcount = 0;
		crawlerDir(fromdir);
		
		/**
		 * Close streams
		 */
		try {
			if (osw_fout != null) {
				osw_fout.flush();
				osw_fout.close();
			}
			
			if (osw_dout != null) {
				osw_dout.flush();
				osw_dout.close();
			}
			
			if (osw_foutfs != null) {
				osw_foutfs.flush();
				osw_foutfs.close();
			}
		} catch (IOException e) {
			ExtendCopy.catchError(e, true);
		}
		
		/**
		 * Thanks
		 */
		System.out.flush();
		System.err.println();
		System.err.println("Statistics:");
		System.err.print("Overall size\t\t\t");
		System.err.print(globalsize);
		System.err.println(" bytes");
		
		System.err.print("Number of files\t\t");
		System.err.println(filescount);
		
		System.err.print("Number of directories\t\t");
		System.err.println(dirscount);
		
		System.err.print("Number of items excluded\t");
		System.err.println(errorcount);
	}
	
	/**
	 * Recursive on dir
	 */
	private void crawlerDir(File dir) {
		try {
			if (dir.canRead()) {
				if (options.isNohidden() && dir.isHidden()) {
					onFoundError("# " + dir.getPath().trim() + " is hidden");
					return;
				}
				if (exclude_files.containsKey(dir.getName())) {
					onFoundError("# " + dir.getPath().trim() + " is excluded by name");
					return;
				}
				
				if (dir.isDirectory()) {
					File[] listfile = dir.listFiles();
					onFoundDir(dir);
					for (int i = 0; i < listfile.length; i++) {
						crawlerDir(listfile[i]);
					}
				} else {
					if (dir.isFile()) {
						onFoundFile(dir);
						globalsize = globalsize + dir.length();
					} else {
						onFoundError("# " + dir + " ! It's not a file or a directory !");
					}
				}
			} else {
				onFoundError("# " + dir + " ! Can't read !");
			}
		} catch (Exception e) {
			onFoundError("# ERROR : " + e.getMessage());
		}
	}
	
	private void onFoundDir(File directory) {
		dirscount++;
		try {
			if (osw_dout != null) {
				osw_dout.write(directory.getPath());
				osw_dout.write(lineseparator);
			}
		} catch (IOException e) {
			ExtendCopy.catchError(e, true);
		}
	}
	
	private void onFoundFile(File foundfile) {
		filescount++;
		String filename;
		long lastmod;
		long now = System.currentTimeMillis();
		SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		FileNameFactory filenamefactory;
		long filelength;
		String moddate;
		long difftime;
		String humandifftime;
		String filebasename;
		String fileext;
		
		StringBuffer sb_linefileverbose;
		try {
			filename = foundfile.getPath();
			lastmod = foundfile.lastModified();
			filenamefactory = new FileNameFactory(foundfile);
			filelength = foundfile.length();
			moddate = simpledateformat.format(new Date(lastmod));
			filebasename = filenamefactory.getSourcebasename();
			fileext = filenamefactory.getSourceext();
			difftime = (now - lastmod) / 1000;
			humandifftime = TimeUtils.secondsToYWDHMS(difftime);
			
			sb_linefileverbose = new StringBuffer();
			sb_linefileverbose.append(filename);
			sb_linefileverbose.append(COLS_SEPARATOR);
			sb_linefileverbose.append(filelength);
			sb_linefileverbose.append(COLS_SEPARATOR);
			sb_linefileverbose.append(moddate);
			sb_linefileverbose.append(COLS_SEPARATOR);
			sb_linefileverbose.append(filebasename);
			sb_linefileverbose.append(COLS_SEPARATOR);
			sb_linefileverbose.append(fileext);
			sb_linefileverbose.append(COLS_SEPARATOR);
			sb_linefileverbose.append(difftime);
			sb_linefileverbose.append(COLS_SEPARATOR);
			sb_linefileverbose.append(humandifftime);
			
			System.out.println(filename);
			if (osw_fout != null) {
				osw_fout.write(filename);
				osw_fout.write(lineseparator);
			}
			
			if (osw_foutfs != null) {
				osw_foutfs.write(sb_linefileverbose.toString());
				osw_foutfs.write(lineseparator);
			}
		} catch (IOException e) {
			ExtendCopy.catchError(e, true);
		}
	}
	
	private void onFoundError(String errormessage) {
		errorcount++;
		System.err.println(errormessage);
	}
	
}
