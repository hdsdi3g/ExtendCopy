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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

@SuppressWarnings("nls")
/**
 * @author hdsdi3g
 * @version 1.0
 */
abstract class Basefunctions {
	
	protected String hashname = "MD5"; //$NON-NLS-1$
	protected static String lineseparator = System.getProperty("line.separator"); //$NON-NLS-1$
	protected ArrayList<JobElement> joblist = null;
	protected int current_pos_in_list = 0;
	protected int totalfilecount = 0;
	protected ExtendCopyOptions options;
	
	public Basefunctions() {
		hashname = System.getProperty("extendcopy.hashname", "MD5"); //$NON-NLS-1$//$NON-NLS-2$
	}
	
	public void doJob(ExtendCopyOptions options) {
		this.options = options;
		System.out.flush();
		System.err.flush();
		process();
		System.out.flush();
		System.err.flush();
		if (joblist != null) {
			joblist.clear();
		}
		joblist = null;
	}
	
	protected abstract void process();
	
	protected void addFileInError(String element, String cause) {
		System.err.print(element);
		System.err.print("\t"); //$NON-NLS-1$
		System.err.println(cause);
		
		File list_err;
		if (isVerifyJob()) {
			list_err = options.getVerifyerrlist();
		} else {
			list_err = options.getCopyerrlist();
		}
		
		if (list_err != null) {
			try {
				FileOutputStream fos = new FileOutputStream(list_err, true);
				OutputStreamWriter osw = new OutputStreamWriter(fos);
				osw.write(element);
				osw.write("\t"); //$NON-NLS-1$
				osw.write(cause);
				osw.write(lineseparator);
				osw.close();
				fos.close();
			} catch (Exception e) {
				ExtendCopy.catchError(e, false);
			}
		}
	}
	
	public static void chechReading(File file, boolean musttobeadir) {
		if (file == null) {
			ExtendCopy.catchError(new NullPointerException(), true);
		}
		if (file.exists() == false) {
			ExtendCopy.catchError(new FileNotFoundException(file.getPath()), true);
		}
		if (musttobeadir) {
			if (file.isDirectory() == false) {
				ExtendCopy.catchError(new FileNotFoundException(file.getPath() + " is not a directory."), true);
			}
		} else {
			if (file.isFile() == false) {
				ExtendCopy.catchError(new FileNotFoundException(file.getPath() + " is not a file."), true);
			}
		}
		if (file.canRead() == false) {
			ExtendCopy.catchError(new FileNotFoundException(file.getPath() + " can't read."), true);
		}
		
	}
	
	public static void checkWriting(File file) {
		try {
			boolean fileexists = file.exists();
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file, true));
			osw.close();
			if (fileexists == false) {
				file.delete();
			}
		} catch (IOException e) {
			System.err.println("Error : can't write in file " + file.getPath());
			e.printStackTrace();
			System.exit(ExtendCopy.RETURN_CODE_GENERIC_ERROR);
		}
	}
	
	protected class JobElement {
		
		File source;
		File dest;
		String hash;
		long size;
		
		JobElement(File source, File dest) throws FileNotFoundException {
			this.source = source;
			if (source.exists() == false) {
				throw new FileNotFoundException(source.getPath());
			}
			if (source.isFile() == false) {
				throw new FileNotFoundException(source.getPath() + " is not a file");
			}
			this.dest = dest;
			this.size = source.length();
		}
		
		JobElement(File source, String hash, long size) {
			this.source = source;
			this.hash = hash;
			this.size = size;
		}
		
		public String formatVerifyElement() {
			StringBuffer sb = new StringBuffer();
			sb.append("\t"); //$NON-NLS-1$
			sb.append(source);
			sb.append("\t"); //$NON-NLS-1$
			sb.append(size);
			sb.append("\t"); //$NON-NLS-1$
			sb.append(hash);
			return sb.toString();
		}
		
	}
	
	protected abstract boolean isVerifyJob();
	
	protected void dumpWaitFileList() {
		File list_wait;
		if (isVerifyJob()) {
			list_wait = options.getVerifywaitlist();
		} else {
			list_wait = options.getCopywaitlist();
		}
		if ((list_wait != null) & (joblist != null)) {
			try {
				if ((current_pos_in_list + 1) == totalfilecount) {
					list_wait.delete();
				} else {
					OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(list_wait));
					
					for (int i = current_pos_in_list; i < totalfilecount; i++) {
						if (isVerifyJob()) {
							osw.write(joblist.get(i).formatVerifyElement());
						} else {
							osw.write(joblist.get(i).source.getPath());
						}
						osw.write(lineseparator);
					}
					osw.close();
				}
			} catch (Exception e) {
				ExtendCopy.catchError(e, false);
			}
		}
	}
	
}
