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
import hd3gtv.tools.ApplicationArgsParam;

import java.io.File;
import java.util.ArrayList;

/**
 * Prepare in vars the parameters of the application
 * @author hdsdi3g
 * @version 1.0
 */
public class ExtendCopyOptions {
	
	private File filelist;
	private File dirlist;
	private File simplefilelist;
	private File copylist;
	private File copywaitlist;
	private File copyerrlist;
	private File copydonelist;
	private File verifylist;
	private File verifywaitlist;
	private File verifyerrlist;
	private File verifyoklist;
	private File verifynoklist;
	private boolean dolist;
	private boolean docopy;
	private boolean doverif;
	
	private boolean notesthash;
	private boolean exclude;
	private boolean nohidden;
	private boolean keep;
	private boolean force;
	private boolean makehashtag;
	private boolean dryrun;
	private String from;
	private File to;
	
	public ExtendCopyOptions(ApplicationArgs aargs) {
		
		ArrayList<ApplicationArgsParam> params = aargs.getParamKey("set"); //$NON-NLS-1$
		
		for (int pos = 0; pos < params.size(); pos++) {
			if (params.get(pos).getName().equalsIgnoreCase("filelist")) { //$NON-NLS-1$
				filelist = new File(params.get(pos).getValue());
				continue;
			}
			if (params.get(pos).getName().equalsIgnoreCase("dirlist")) { //$NON-NLS-1$
				dirlist = new File(params.get(pos).getValue());
				continue;
			}
			if (params.get(pos).getName().equalsIgnoreCase("simplefilelist")) { //$NON-NLS-1$
				simplefilelist = new File(params.get(pos).getValue());
				continue;
			}
			if (params.get(pos).getName().equalsIgnoreCase("copylist")) { //$NON-NLS-1$
				copylist = new File(params.get(pos).getValue());
				continue;
			}
			if (params.get(pos).getName().equalsIgnoreCase("copywaitlist")) { //$NON-NLS-1$
				copywaitlist = new File(params.get(pos).getValue());
				continue;
			}
			if (params.get(pos).getName().equalsIgnoreCase("copyerrlist")) { //$NON-NLS-1$
				copyerrlist = new File(params.get(pos).getValue());
				continue;
			}
			if (params.get(pos).getName().equalsIgnoreCase("copydonelist")) { //$NON-NLS-1$
				copydonelist = new File(params.get(pos).getValue());
				continue;
			}
			if (params.get(pos).getName().equalsIgnoreCase("verifylist")) { //$NON-NLS-1$
				verifylist = new File(params.get(pos).getValue());
				continue;
			}
			if (params.get(pos).getName().equalsIgnoreCase("verifywaitlist")) { //$NON-NLS-1$
				verifywaitlist = new File(params.get(pos).getValue());
				continue;
			}
			if (params.get(pos).getName().equalsIgnoreCase("verifyerrlist")) { //$NON-NLS-1$
				verifyerrlist = new File(params.get(pos).getValue());
				continue;
			}
			if (params.get(pos).getName().equalsIgnoreCase("verifyoklist")) { //$NON-NLS-1$
				verifyoklist = new File(params.get(pos).getValue());
				continue;
			}
			if (params.get(pos).getName().equalsIgnoreCase("verifynoklist")) { //$NON-NLS-1$
				verifynoklist = new File(params.get(pos).getValue());
				continue;
			}
		}
		
		String project = aargs.getLastAction();
		if (project == null) {
			project = "project_" + String.valueOf(System.currentTimeMillis()); //$NON-NLS-1$
		}
		if (project.equalsIgnoreCase("")) { //$NON-NLS-1$
			project = "project_" + String.valueOf(System.currentTimeMillis()); //$NON-NLS-1$
		}
		if (project.startsWith("-")) { //$NON-NLS-1$
			project = "project_" + String.valueOf(System.currentTimeMillis()); //$NON-NLS-1$
		}
		
		if (filelist == null) {
			filelist = new File(project + "-list.txt"); //$NON-NLS-1$
		}
		if (copywaitlist == null) {
			copywaitlist = new File(project + "-copywait.txt"); //$NON-NLS-1$
		}
		if (copyerrlist == null) {
			copyerrlist = new File(project + "-copyerror.txt"); //$NON-NLS-1$
		}
		if (copydonelist == null) {
			copydonelist = new File(project + "-copy.txt"); //$NON-NLS-1$
		}
		if (verifywaitlist == null) {
			verifywaitlist = new File(project + "-verifwait.txt"); //$NON-NLS-1$
		}
		if (verifyerrlist == null) {
			verifyerrlist = new File(project + "-veriferr.txt"); //$NON-NLS-1$
		}
		if (verifynoklist == null) {
			verifynoklist = new File(project + "-verif-nook.txt"); //$NON-NLS-1$
		}
		if (verifyoklist == null) {
			verifyoklist = new File(project + "-verif.txt"); //$NON-NLS-1$
		}
		
		if (copylist == null) {
			copylist = filelist;
		}
		if (verifylist == null) {
			verifylist = copydonelist;
		}
		
		String appaction = aargs.getFirstAction();
		if (appaction != null) {
			dolist = appaction.equalsIgnoreCase("l") | appaction.equalsIgnoreCase("lc") | appaction.equalsIgnoreCase("lcv") | appaction.equalsIgnoreCase("list"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			docopy = appaction.equalsIgnoreCase("c") | appaction.equalsIgnoreCase("lc") | appaction.equalsIgnoreCase("lcv") | appaction.equalsIgnoreCase("cv") | appaction.equalsIgnoreCase("copy"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			doverif = appaction.equalsIgnoreCase("v") | appaction.equalsIgnoreCase("cv") | appaction.equalsIgnoreCase("lcv") | appaction.equalsIgnoreCase("verify"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		
		exclude = aargs.getParamExist("-exclude"); //$NON-NLS-1$
		nohidden = aargs.getParamExist("-nohidden"); //$NON-NLS-1$
		keep = aargs.getParamExist("-keep"); //$NON-NLS-1$
		force = aargs.getParamExist("-force"); //$NON-NLS-1$
		makehashtag = aargs.getParamExist("-makehashtag"); //$NON-NLS-1$
		notesthash = aargs.getParamExist("-notesthash"); //$NON-NLS-1$
		dryrun = aargs.getParamExist("-dryrun"); //$NON-NLS-1$
		
		from = aargs.getSimpleParamValue("-from"); //$NON-NLS-1$
		try {
			to = new File(aargs.getSimpleParamValue("-to")); //$NON-NLS-1$
		} catch (NullPointerException e) {
		}
		
	}
	
	public File getFilelist() {
		return filelist;
	}
	
	public File getDirlist() {
		return dirlist;
	}
	
	public File getSimplefilelist() {
		return simplefilelist;
	}
	
	public File getCopylist() {
		return copylist;
	}
	
	public File getCopywaitlist() {
		return copywaitlist;
	}
	
	public File getCopyerrlist() {
		return copyerrlist;
	}
	
	public File getCopydonelist() {
		return copydonelist;
	}
	
	public File getVerifylist() {
		return verifylist;
	}
	
	public File getVerifywaitlist() {
		return verifywaitlist;
	}
	
	public File getVerifyerrlist() {
		return verifyerrlist;
	}
	
	public File getVerifyoklist() {
		return verifyoklist;
	}
	
	public File getVerifynoklist() {
		return verifynoklist;
	}
	
	public boolean isDolist() {
		return dolist;
	}
	
	public boolean isDocopy() {
		return docopy;
	}
	
	public boolean isDoverif() {
		return doverif;
	}
	
	public boolean isExclude() {
		return exclude;
	}
	
	public boolean isNohidden() {
		return nohidden;
	}
	
	public boolean isKeep() {
		return keep;
	}
	
	public boolean isForce() {
		return force;
	}
	
	public boolean isMakehashtag() {
		return makehashtag;
	}
	
	public boolean isDryrun() {
		return dryrun;
	}
	
	public String getFrom() {
		return from;
	}
	
	public File getTo() {
		return to;
	}
	
	public boolean isNotesthash() {
		return notesthash;
	}
}
