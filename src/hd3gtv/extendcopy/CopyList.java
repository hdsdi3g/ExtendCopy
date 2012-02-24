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

import hd3gtv.tools.FileCopy;
import hd3gtv.tools.FileNameFactory;
import hd3gtv.tools.Progress;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

@SuppressWarnings("nls")
/**
 * @author hdsdi3g
 * @version 1.0
 */
public class CopyList extends Basefunctions {
	
	public void process() {
		if (options.isForce() & options.isKeep()) {
			ExtendCopy.catchError(new Exception("Impossible to set force and keep options together"), true);
		}
		
		boolean createdestdirfromsourcedir;
		if (options.getFrom().endsWith(File.separator) == false) {
			createdestdirfromsourcedir = true;
		} else {
			createdestdirfromsourcedir = false;
		}
		
		File root_dir = new File(options.getFrom());
		chechReading(root_dir, true);
		chechReading(options.getTo(), true);
		
		boolean presence_done = false;
		boolean presence_wait = false;
		
		if (options.getCopydonelist() != null) {
			presence_done = options.getCopydonelist().exists();
		}
		if (options.getCopywaitlist() != null) {
			presence_wait = options.getCopywaitlist().exists();
		}
		
		chechReading(options.getCopylist(), false);
		
		if (presence_wait) {
			if (presence_done) {
				System.err.println("The copy seem stopped: continue the copy.");
				chechReading(options.getCopywaitlist(), false);
				
				FileNameFactory oldcopyfile = new FileNameFactory(options.getCopylist());
				oldcopyfile.addSourceDirNameInDestFileName();
				oldcopyfile.addSourceFileNameInDestFileName();
				oldcopyfile.addValueInDestFileName("-last"); //$NON-NLS-1$
				oldcopyfile.addValueInDestFileName(String.valueOf(System.currentTimeMillis()));
				oldcopyfile.addValueInDestFileName(".txt"); //$NON-NLS-1$
				
				options.getCopylist().renameTo(oldcopyfile.getDestfile());
				options.getCopywaitlist().renameTo(options.getCopylist());
				
			} else {
				System.err.println("The copy seem badly finished: restart the copy.");
			}
		} else {
			if (presence_done) {
				System.err.println("The copy seem finished: ignore the copy.");
				return;
			}
		}
		
		if (options.getCopydonelist() != null) {
			checkWriting(options.getCopydonelist());
		}
		if (options.getCopywaitlist() != null) {
			checkWriting(options.getCopywaitlist());
		}
		if (options.getCopyerrlist() != null) {
			checkWriting(options.getCopyerrlist());
		}
		
		/**
		 * Import copylist
		 */
		try {
			joblist = new ArrayList<JobElement>();
			
			InputStream ips = new FileInputStream(options.getCopylist());
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String line;
			String[] elements;
			String source_file_name;
			File source_file;
			File dest_file;
			boolean noerror;
			
			while ((line = br.readLine()) != null) {
				/**
				 * For all elements of the file list entry
				 */
				if (line.trim().startsWith("#") | line.trim().startsWith(";")) { //$NON-NLS-1$ //$NON-NLS-2$
					continue;
				}
				
				elements = line.split("\t"); //$NON-NLS-1$
				noerror = true;
				
				source_file_name = elements[0];
				source_file = new File(source_file_name);
				
				if (source_file.exists() == false) {
					addFileInError(source_file_name, "Not exists");
					noerror = false;
				} else {
					if (source_file.isFile() == false) {
						addFileInError(source_file_name, "Not a file");
						noerror = false;
					} else {
						if (source_file.canRead() == false) {
							addFileInError(source_file_name, "Can't read");
							noerror = false;
						}
					}
				}
				
				if (noerror) {
					if (source_file.getPath().startsWith(root_dir.getPath())) {
						if (createdestdirfromsourcedir) {
							dest_file = new File(options.getTo().getPath() + File.separator + root_dir.getName() + File.separator + source_file.getPath().substring(root_dir.getPath().length()));
						} else {
							dest_file = new File(options.getTo().getPath() + File.separator + source_file.getPath().substring(root_dir.getPath().length()));
						}
						
						if (options.isDryrun()) {
							System.out.print(source_file);
							System.out.print("\t"); //$NON-NLS-1$
							System.out.println(dest_file);
						} else {
							joblist.add(new JobElement(source_file, dest_file));
						}
					} else {
						addFileInError(source_file_name, "Bad source dir for make dest dir:\t" + root_dir.getPath());
					}
				}
			}
			br.close();
			ipsr.close();
			ips.close();
		} catch (IOException e) {
			ExtendCopy.catchError(e, true);
		}
		
		if (joblist == null) {
			ExtendCopy.catchError(new Exception("No file to process"), true);
		}
		
		totalfilecount = joblist.size();
		
		if (totalfilecount == 0) {
			if (options.isDryrun() == false) {
				ExtendCopy.catchError(new Exception("No file to process"), true);
			} else {
				return;
			}
		}
		
		/** initial list */
		dumpWaitFileList();
		
		/**
		 * Copy
		 */
		File from;
		File to;
		for (int i = 0; i < totalfilecount; i++) {
			current_pos_in_list = i;
			try {
				if (options.isDryrun() == false) {
					from = joblist.get(i).source;
					to = joblist.get(i).dest;
					/**
					 * Copy of the file
					 */
					Progress progress = null;
					FileCopy filecopymd5 = null;
					
					if (to.exists()) {
						
						if ((options.isKeep()) && (options.isForce() == false)) {
							throw new IOException("Dest file exists");
						} else {
							if (options.isForce() == true) {
								System.out.println(to.getPath() + "\tDelete dest file before copy.");
								to.delete();
							} else {
								/**
								 * keep == false
								 */
								if (from.length() == to.length()) {
									/**
									 * The file has been copied in whole
									 */
									System.out.println(to.getPath() + "\tEXISTS : IGNORE");
									dumpWaitFileList();
									continue;
								} else {
									/**
									 * The file has already been partially copy.
									 * We start again.
									 */
									System.out.println(to.getPath() + "\tDelete dest file before copy.");
									to.delete();
								}
							}
						}
					}
					
					try {
						progress = new Progress(System.out);
						filecopymd5 = new FileCopy(from, to, hashname);
						filecopymd5.setProgress(progress);
					} catch (IOException e) {
						throw e;
					} catch (NoSuchAlgorithmException nsae) {
						throw nsae;
					}
					
					try {
						filecopymd5.copy();
					} catch (IOException e) {
						throw e;
					}
					
					try {
						filecopymd5.closeStreams();
					} catch (IOException e) {
						throw e;
					}
					
					String filehash = filecopymd5.getFilehash();
					StringBuffer filelinehash = new StringBuffer();
					filelinehash.append(from.getPath());
					filelinehash.append("\t"); //$NON-NLS-1$
					filelinehash.append(to.getPath());
					filelinehash.append("\t"); //$NON-NLS-1$
					filelinehash.append(from.length());
					filelinehash.append("\t"); //$NON-NLS-1$
					filelinehash.append(filehash);
					
					System.out.println(filelinehash.toString());
					
					if (options.getCopydonelist() != null) {
						try {
							OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(options.getCopydonelist(), true));
							osw.write(filelinehash.toString());
							osw.write(lineseparator);
							osw.close();
						} catch (Exception e) {
							ExtendCopy.catchError(e, false);
						}
					}
					
					if (options.isMakehashtag()) {
						try {
							FileNameFactory fnf = new FileNameFactory(to);
							fnf.setDestdir(fnf.getSourcedir());
							fnf.addSourceFileNameInDestFileName();
							fnf.addValueInDestFileName("."); //$NON-NLS-1$
							fnf.addValueInDestFileName(hashname.toLowerCase());
							
							OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fnf.getDestfile()));
							osw.write(filehash);
							osw.write("  "); //$NON-NLS-1$
							osw.write(to.getName());
							osw.write(lineseparator);
							osw.close();
						} catch (Exception e) {
							ExtendCopy.catchError(e, false);
						}
					}
					
					dumpWaitFileList();
				}
			} catch (Exception e) {
				addFileInError(joblist.get(i).source.getPath(), e.getMessage());
			}
		}
		
		if (options.getCopydonelist() != null) {
			if (options.getCopydonelist().exists() == false) {
				try {
					OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(options.getCopydonelist(), true));
					osw.write("# no files are copied");
					osw.close();
				} catch (Exception e) {
					ExtendCopy.catchError(e, false);
				}
			}
		}
		
	}
	
	protected boolean isVerifyJob() {
		return false;
	}
}
