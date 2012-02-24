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

import hd3gtv.tools.ApplicationArgs;
import hd3gtv.tools.TimeUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * Prepare in vars the parameters of the application
 * @author hdsdi3g
 * @version 1.0
 */
public class ExtendCopy {
	
	public static void main(String[] args) {
		new ExtendCopy(args);
	}
	
	protected ApplicationArgs aargs;
	
	private static final String about = "ExtendCopy v1.0 - Copyright (C) hdsdi3g for hd3g.tv 2009-2012"; //$NON-NLS-1$
	public static final int RETURN_CODE_NO_PARAM = 1;
	public static final int RETURN_CODE_GENERIC_ERROR = 2;
	
	ExtendCopy(String[] args) {
		
		aargs = new ApplicationArgs(args);
		
		if (aargs.isEmpty()) {
			showHelp();
			System.exit(RETURN_CODE_NO_PARAM);
		}
		
		injectDefaultSystemProperty("extendcopy.hashname", "MD5"); //$NON-NLS-1$ //$NON-NLS-2$
		
		long starttime = System.currentTimeMillis();
		
		ExtendCopyOptions options = new ExtendCopyOptions(aargs);
		
		if (options.isDolist()) {
			DirListing scfunction = new DirListing();
			scfunction.doJob(options);
		}
		if (options.isDocopy()) {
			CopyList scfunction = new CopyList();
			scfunction.doJob(options);
		}
		if (options.isDoverif()) {
			VerifyList scfunction = new VerifyList();
			scfunction.doJob(options);
		}
		
		long endtime = System.currentTimeMillis();
		
		if (((endtime - starttime) / 1000) > 0) {
			System.err.println();
			System.err.print("Execution time: "); //$NON-NLS-1$
			System.err.println(TimeUtils.secondstoHMS((endtime - starttime) / 1000));
		}
	}
	
	/**
	 * If this system property is not defined, it's defined.
	 * It allows to define a default value for a key and always have a no null value for this property key.
	 * @param key the key like -Dkey=value
	 * @param defaultvalue the value to set
	 */
	public static final void injectDefaultSystemProperty(String key, String defaultvalue) {
		String currentvalue = System.getProperty(key, ""); //$NON-NLS-1$
		if (currentvalue.trim().equals("")) { //$NON-NLS-1$
			System.setProperty(key, defaultvalue);
		}
	}
	
	/**
	 * If system property don't exists, return false.
	 * @param key the key like -Dkey=value
	 */
	public static final boolean getBooleanSystemProperty(String key) {
		return System.getProperty(key, "").equalsIgnoreCase("true"); //$NON-NLS-1$//$NON-NLS-2$
	}
	
	public static void catchError(Exception e, boolean fatal) {
		e.printStackTrace();
		if (fatal) {
			System.exit(ExtendCopy.RETURN_CODE_GENERIC_ERROR);
		}
	}
	
	private static void showHelp() {
		
		System.out.println(about);
		
		String doc_localized = "hd3gtv/extendcopy/internaldoc-" + Locale.getDefault().getLanguage().toString() + ".txt"; //$NON-NLS-1$ //$NON-NLS-2$
		String doc_generic = "hd3gtv/extendcopy/internaldoc.txt"; //$NON-NLS-1$
		
		try {
			InputStream stream_ressource = ClassLoader.getSystemResourceAsStream(doc_localized);
			
			if (stream_ressource == null) {
				stream_ressource = ClassLoader.getSystemResourceAsStream(doc_generic);
			}
			if (stream_ressource == null) {
				throw new NullPointerException(doc_generic + " don't exists"); //$NON-NLS-1$
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader(stream_ressource, "UTF-8")); //$NON-NLS-1$
			String line;
			while ((line = br.readLine()) != null) {
				if (line.trim().startsWith("#")) { //$NON-NLS-1$
					continue;
				}
				System.out.println(line);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Can't load internal documentation."); //$NON-NLS-1$
			System.err.println("Please report this at https://github.com/hdsdi3g"); //$NON-NLS-1$
		}
	}
}
