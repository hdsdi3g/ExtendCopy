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
import hd3gtv.tools.FileVerify;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

@SuppressWarnings("nls")
/**
 * @author hdsdi3g
 * @version 1.0
 */
public class VerifyList extends Basefunctions {
	
	public void process() {
		chechReading(options.getVerifylist(), false);
		
		boolean presence_oklist = false;
		boolean presence_waitlist = false;
		
		if (options.getVerifyoklist() != null) {
			presence_oklist = options.getVerifyoklist().exists();
		}
		if (options.getVerifywaitlist() != null) {
			presence_waitlist = options.getVerifywaitlist().exists();
		}
		
		if (presence_waitlist) {
			if (presence_oklist) {
				System.err.println("The verification seem stopped: continue the verification.");
				chechReading(options.getVerifywaitlist(), false);
				
				FileNameFactory oldveriffile = new FileNameFactory(options.getVerifylist());
				oldveriffile.addSourceDirNameInDestFileName();
				oldveriffile.addSourceFileNameInDestFileName();
				oldveriffile.addValueInDestFileName("-last"); //$NON-NLS-1$
				oldveriffile.addValueInDestFileName(String.valueOf(System.currentTimeMillis()));
				oldveriffile.addValueInDestFileName(".txt"); //$NON-NLS-1$
				
				options.getVerifylist().renameTo(oldveriffile.getDestfile());
				options.getVerifywaitlist().renameTo(options.getVerifylist());
				
			} else {
				// wait alone, he stopped before he begins, we start over.
				System.err.println("The verification seem badly finished: restart the verification.");
			}
		} else {
			if (presence_oklist) {
				System.err.println("The verification seem finished. End of process.");
				return;
			}
		}
		
		if (options.getVerifywaitlist() != null) {
			checkWriting(options.getVerifywaitlist());
		}
		if (options.getVerifyerrlist() != null) {
			checkWriting(options.getVerifyerrlist());
		}
		if (options.getVerifyoklist() != null) {
			checkWriting(options.getVerifyoklist());
		}
		if (options.getVerifynoklist() != null) {
			checkWriting(options.getVerifynoklist());
		}
		
		joblist = new ArrayList<JobElement>();
		try {
			int linenum = 0;
			InputStream ips = new FileInputStream(options.getVerifylist());
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String line;
			String[] elements;
			
			String dest_file_name;
			String dest_file_size;
			
			File source_file;
			long size_file;
			String hash_value;
			boolean noerror;
			
			while ((line = br.readLine()) != null) {
				linenum++;
				/**
				 * For all elements of the list file entry
				 */
				if (line.startsWith("#") | line.startsWith(";")) { //$NON-NLS-1$//$NON-NLS-2$
					continue;
				}
				elements = line.split("\t"); //$NON-NLS-1$
				noerror = true;
				
				if (elements.length < 4) {
					throw new Exception("Bad line format : " + String.valueOf(elements.length) + " cols. Line no " + String.valueOf(linenum));
				}
				
				dest_file_name = elements[1];
				dest_file_size = elements[2];
				hash_value = elements[3];
				
				source_file = new File(dest_file_name);
				
				size_file = -1;
				try {
					size_file = Long.parseLong(dest_file_size);
				} catch (NumberFormatException nfe) {
					addFileInError(dest_file_name, nfe.getMessage());
					noerror = false;
				}
				
				if (noerror) {
					joblist.add(new JobElement(source_file, hash_value, size_file));
				}
			}
			br.close();
			ipsr.close();
			ips.close();
		} catch (Exception e) {
			ExtendCopy.catchError(e, true);
		}
		
		/**
		 * Pour tous les elements, on verifie
		 */
		totalfilecount = joblist.size();
		
		dumpWaitFileList();
		
		FileVerify fileverify = null;
		StringBuffer sb;
		
		int sum_fok = 0;
		int sum_fnull = 0;
		int sum_fmiss = 0;
		int sum_fbadsize = 0;
		int sum_fbaddigest = 0;
		int sum_fioerr = 0;
		for (int i = 0; i < totalfilecount; i++) {
			current_pos_in_list = i;
			dumpWaitFileList();
			try {
				fileverify = null;
				if (joblist.get(i).source.exists() == false) {
					sum_fmiss++;
					throw new SCFunctionVerifyError(SCFunctionVerifyErrorCause.MISSING_FILE, joblist.get(i), null);
				}
				if (joblist.get(i).source.isFile() == false) {
					sum_fioerr++;
					throw new SCFunctionVerifyError(SCFunctionVerifyErrorCause.NOT_A_FILE, joblist.get(i), null);
				}
				if (joblist.get(i).source.canRead() == false) {
					sum_fioerr++;
					throw new SCFunctionVerifyError(SCFunctionVerifyErrorCause.CANT_READ, joblist.get(i), null);
				}
				if (joblist.get(i).size == 0) {
					sum_fnull++;
					throw new SCFunctionVerifyError(SCFunctionVerifyErrorCause.EMPTY, joblist.get(i), null);
				}
				if (joblist.get(i).source.length() > joblist.get(i).size) {
					sum_fbadsize++;
					sb = new StringBuffer();
					sb.append("SRC\t");
					sb.append(joblist.get(i).size);
					sb.append("\tDEST\t");
					sb.append(joblist.get(i).source.length());
					throw new SCFunctionVerifyError(SCFunctionVerifyErrorCause.TOO_BIG_SIZE, joblist.get(i), sb.toString());
				}
				if (joblist.get(i).source.length() < joblist.get(i).size) {
					sum_fbadsize++;
					sb = new StringBuffer();
					sb.append("SRC\t");
					sb.append(joblist.get(i).size);
					sb.append("\tDEST\t");
					sb.append(joblist.get(i).source.length());
					throw new SCFunctionVerifyError(SCFunctionVerifyErrorCause.TOO_SMALL_SIZE, joblist.get(i), sb.toString());
				}
				
				System.out.println(joblist.get(i).source);
				
				if (options.isNotesthash()) {
					sum_fok++;
					if (options.getVerifyoklist() != null) {
						FileOutputStream fos = new FileOutputStream(options.getVerifyoklist(), true);
						OutputStreamWriter osw = new OutputStreamWriter(fos);
						osw.write(joblist.get(i).formatVerifyElement());
						osw.write(lineseparator);
						osw.close();
						fos.close();
					}
				} else {
					fileverify = new FileVerify(joblist.get(i).source, joblist.get(i).hash, hashname);
					fileverify.compute();
					fileverify.closeStreams();
					
					if (fileverify.isValid()) {
						sum_fok++;
						if (options.getVerifyoklist() != null) {
							FileOutputStream fos = new FileOutputStream(options.getVerifyoklist(), true);
							OutputStreamWriter osw = new OutputStreamWriter(fos);
							osw.write(joblist.get(i).formatVerifyElement());
							osw.write(lineseparator);
							osw.close();
							fos.close();
						}
					} else {
						sum_fbaddigest++;
						sb = new StringBuffer();
						sb.append("SRC\t");
						sb.append(joblist.get(i).hash);
						sb.append("\tDEST\t");
						sb.append(fileverify.getDestdigest());
						throw new SCFunctionVerifyError(SCFunctionVerifyErrorCause.BAD_DIGEST, joblist.get(i), sb.toString());
					}
				}
				
			} catch (SCFunctionVerifyError scfve) {
				System.err.println(scfve.getMessage());
				if (options.getVerifynoklist() != null) {
					try {
						FileOutputStream fos = new FileOutputStream(options.getVerifynoklist(), true);
						OutputStreamWriter osw = new OutputStreamWriter(fos);
						osw.write(scfve.getMessage());
						osw.write(lineseparator);
						osw.close();
						fos.close();
					} catch (Exception e) {
						ExtendCopy.catchError(e, false);
					}
				}
			} catch (Exception e) {
				addFileInError(joblist.get(i).source.getPath(), e.getMessage());
				e.printStackTrace();
				if (fileverify != null) {
					try {
						fileverify.closeStreams();
					} catch (IOException eclose) {
					}
				}
			}
		}
		
		System.out.flush();
		System.out.println();
		System.out.println("Statistics :");
		System.out.print("Files OK\t");
		System.out.println(sum_fok);
		
		System.out.print("Empty files\t");
		System.out.println(sum_fnull);
		
		System.out.print("Missing files\t");
		System.out.println(sum_fmiss);
		
		System.out.print("Bad file size\t");
		System.out.println(sum_fbadsize);
		
		System.out.print("Bad file content\t");
		System.out.println(sum_fbaddigest);
		
		System.out.print("Bad file type\t");
		System.out.println(sum_fioerr);
		
		System.out.print("TOTAL\t\t");
		System.out.println(totalfilecount);
		
	}
	
	private enum SCFunctionVerifyErrorCause {
		MISSING_FILE, NOT_A_FILE, CANT_READ, TOO_BIG_SIZE, TOO_SMALL_SIZE, EMPTY, BAD_DIGEST
	};
	
	private class SCFunctionVerifyError extends Exception {
		private static final long serialVersionUID = -7161133345027022755L;
		private String message;
		
		public SCFunctionVerifyError(SCFunctionVerifyErrorCause cause, JobElement element, String info) {
			StringBuffer sb = new StringBuffer();
			sb.append("@_"); //$NON-NLS-1$
			sb.append(cause.toString());
			sb.append("_@"); //$NON-NLS-1$
			sb.append(element.formatVerifyElement());
			if (info != null) {
				sb.append("\t"); //$NON-NLS-1$
				sb.append(info);
			}
			message = sb.toString();
		}
		
		public String getMessage() {
			return message;
		}
	}
	
	protected boolean isVerifyJob() {
		return true;
	}
	
}
