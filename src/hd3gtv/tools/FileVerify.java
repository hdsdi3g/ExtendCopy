/*
 * This file is part of Java Tools for hdsdi3g
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Read a file for an hash compute
 * @see FileCopy
 * @author hdsdi3g
 * @version 1.0
 */
public class FileVerify {
	
	private MessageDigest messagedigestinstance;
	private File sourcefile;
	private FileInputStream fileinputstream;
	private byte[] filehash;
	private Progress progress;
	
	private String sourcedigest;
	private String destdigest;
	
	public FileVerify(File sourcefile, String sourcedigest, String digestname) throws NoSuchAlgorithmException, FileNotFoundException {
		messagedigestinstance = MessageDigest.getInstance(digestname);
		this.sourcefile = sourcefile;
		if (sourcefile.exists() == false) {
			throw new FileNotFoundException(sourcefile.getPath());
		}
		if (sourcefile.isDirectory()) {
			throw new FileNotFoundException(sourcefile.getPath() + " is directory"); //$NON-NLS-1$
		}
		this.sourcedigest = sourcedigest;
	}
	
	public void setProgress(Progress progress) {
		this.progress = progress;
	}
	
	public void closeStreams() throws IOException {
		if (fileinputstream != null) {
			fileinputstream.close();
		}
	}
	
	private static String byteToString(byte[] b) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			int v = b[i] & 0xFF;
			if (v < 16) {
				sb.append(0);
			}
			sb.append(Integer.toString(v, 16).toLowerCase());
		}
		return sb.toString();
	}
	
	public void compute() throws IOException {
		fileinputstream = new FileInputStream(sourcefile);
		
		if (progress != null) {
			progress.start();
			progress.setDatalen(sourcefile.length());
		}
		
		messagedigestinstance.reset();
		
		byte[] buffer = new byte[512 * 1024];
		int len;
		
		while ((len = fileinputstream.read(buffer)) > 0) {
			if (progress != null) {
				progress.incDatapos(len);
			}
			messagedigestinstance.update(buffer, 0, len);
		}
		filehash = messagedigestinstance.digest();
		
		destdigest = byteToString(filehash);
		
		if (progress != null) {
			progress.setStop();
		}
	}
	
	public boolean isValid() {
		return destdigest.equalsIgnoreCase(sourcedigest);
	}
	
	public String getDestdigest() {
		return destdigest;
	}
	
}
