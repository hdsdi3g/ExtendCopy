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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Copy a file with an hash compute
 * @see FileVerify
 * @author hdsdi3g
 * @version 1.0
 */
public class FileCopy {
	
	private MessageDigest messagedigestinstance;
	private File sourcefile;
	private File destfile;
	private FileInputStream fileinputstream;
	private FileOutputStream fileoutputstream;
	private byte[] filehash;
	
	public FileCopy(File sourcefile, File destfile, String digestname) throws NoSuchAlgorithmException, FileNotFoundException {
		messagedigestinstance = MessageDigest.getInstance(digestname);
		this.sourcefile = sourcefile;
		if (sourcefile.exists() == false) {
			throw new FileNotFoundException(sourcefile.getPath());
		}
		if (sourcefile.isDirectory()) {
			throw new FileNotFoundException(sourcefile.getPath() + " is directory"); //$NON-NLS-1$
		}
		this.destfile = destfile;
		if (destfile.exists()) {
			destfile.delete();
		}
		
		if (destfile.getParentFile() != null) {
			destfile.getParentFile().mkdirs();
		}
	}
	
	public void closeStreams() throws IOException {
		if (fileoutputstream != null) {
			fileoutputstream.flush();
			fileoutputstream.close();
		}
		if (fileinputstream != null) {
			fileinputstream.close();
		}
	}
	
	public void copy() throws IOException {
		fileinputstream = new FileInputStream(sourcefile);
		fileoutputstream = new FileOutputStream(destfile);
		
		messagedigestinstance.reset();
		
		byte[] buffer = new byte[512 * 1024];
		int len;
		
		while ((len = fileinputstream.read(buffer)) > 0) {
			fileoutputstream.write(buffer, 0, len);
			messagedigestinstance.update(buffer, 0, len);
		}
		filehash = messagedigestinstance.digest();
	}
	
	public static String byteToString(byte[] b) {
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
	
	public String getFilehash() {
		return byteToString(filehash);
	}
	
}
