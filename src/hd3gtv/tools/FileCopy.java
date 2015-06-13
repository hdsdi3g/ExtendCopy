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
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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
	private FileChannel source_channel;
	private FileChannel dest_channel;
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
		IOException last_e = null;
		try {
			if (source_channel != null) {
				source_channel.close();
			}
		} catch (IOException e) {
			last_e = e;
		}
		try {
			if (dest_channel != null) {
				dest_channel.close();
			}
		} catch (IOException e) {
			if (last_e != null) {
				last_e.printStackTrace();
			}
			last_e = e;
		}
		try {
			if (fileinputstream != null) {
				fileinputstream.close();
			}
		} catch (IOException e) {
			if (last_e != null) {
				last_e.printStackTrace();
			}
			last_e = e;
		}
		try {
			if (fileoutputstream != null) {
				fileoutputstream.close();
				fileinputstream.close();
			}
		} catch (IOException e) {
			if (last_e != null) {
				last_e.printStackTrace();
			}
			last_e = e;
		}
		if (last_e != null) {
			throw last_e;
		}
	}
	
	public void copy() throws IOException {
		fileinputstream = new FileInputStream(sourcefile);
		source_channel = fileinputstream.getChannel();
		
		fileoutputstream = new FileOutputStream(destfile);
		dest_channel = fileoutputstream.getChannel();
		
		messagedigestinstance.reset();
		
		ByteBuffer buffer = ByteBuffer.allocate(4 * 1024);
		@SuppressWarnings("unused")
		int len;
		
		while ((len = source_channel.read(buffer)) != -1) {
			buffer.flip();
			dest_channel.write(buffer);
			buffer.position(0);
			messagedigestinstance.update(buffer);
			buffer.clear();
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
