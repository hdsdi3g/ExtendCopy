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

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author hdsdi3g
 * @version 1.0
 */
public class Progress extends Thread {
	
	private double datalen_dlb;
	private long datapos;
	private boolean stop;
	private PrintStream out;
	private long averagebitrate;
	private long instantbitrate;
	private long starttime;
	private double current_ratio_complete;
	private double range_quantum;
	private double last_ratio_complete;
	private double current_duration;
	private double averagetimeleft;
	private Locale locale = Locale.US;
	
	public Progress(PrintStream out) {
		stop = false;
		this.out = out;
		setDaemon(true);
		averagebitrate = 0;
		instantbitrate = 0;
		range_quantum = 100;
		last_ratio_complete = 0;
		current_ratio_complete = 0;
		current_duration = 0;
	}
	
	public void setLocale(Locale locale) {
		if (locale != null) {
			this.locale = locale;
		}
	}
	
	public void setStop() {
		stop = true;
	}
	
	void incDatapos(long incr) {
		datapos = datapos + incr;
	}
	
	public void setDatalen(long datalen) {
		datalen_dlb = datalen;
	}
	
	public void run() {
		
		starttime = System.currentTimeMillis();
		
		boolean show_header = false;
		
		long instant_starttime = System.nanoTime();
		long instant_endtime = 0;
		
		/**
		 * La Qte de donnee transferes, en o.
		 */
		long current_dataxfer = 0;
		
		/**
		 * La Qte de donnee transferes depuis le dernier tour, en o.
		 */
		long delta_dataxfer = 0;
		
		/**
		 * en s
		 */
		double instantduration = 0;
		
		/**
		 * qt de donnes restantes a transferer, en o
		 */
		double last_data_len = datalen_dlb;
		
		try {
			while (stop == false) {
				
				current_ratio_complete = (Math.round((datapos / datalen_dlb) * range_quantum)) / range_quantum; // R = ((o / o) * R)/R
				
				if (current_ratio_complete != last_ratio_complete) {
					if (show_header == false) {
						show_header = true;
						synchronized (out) {
							out.println();
							for (int i = 0; i < 80; i++) {
								out.print("="); //$NON-NLS-1$
							}
							out.println();
							out.print("Start : "); //$NON-NLS-1$
							out.println(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss,SSS").format(starttime)); //$NON-NLS-1$
							out.flush();
						}
					}
					
					instant_endtime = System.nanoTime(); // ns
					delta_dataxfer = datapos - current_dataxfer; // o = o - o
					last_ratio_complete = current_ratio_complete; // R = R
					
					current_duration = (System.currentTimeMillis() - starttime) / 1000d; // s = (ms - ms)/1000
					averagebitrate = Math.round((datapos / current_duration) / 1024d); // ko/s = (o / s)/1024
					
					instantduration = (((instant_endtime - instant_starttime)) / (1000d * 1000d * 1000d)); // s = (ns / ns) / (10e-9 >> 10e0)
					
					instantbitrate = Math.round((delta_dataxfer / instantduration) / 1024d); // ko/s = (o / s)*1000
					
					last_data_len = datalen_dlb - datapos;
					if (last_data_len == 0) {
						last_data_len = 1;
					}
					
					averagetimeleft = Math.round(1 / (averagebitrate / (last_data_len / 1024d))); // 1/ (ko/s / ko)
					
					/**
					 * ++ Date actuelle
					 * 15.9 s 13.7 Mo/s 13.2 Mo/s 165.4 sLi 171.1 sLa 9%
					 * 16.793 s 28.1 Mo/s 14.0 Mo/s 79.7 sLi 159.7 sLa 10%
					 * 17.752 s 25.6 Mo/s 14.6 Mo/s 86.4 sLi 151.2 sLa 11%
					 * 20.3 s 33.5 Mo/s 30.4 Mo/s 481.4 sLi 529.9 sLa 19%
					 * 126.159 s 33.8 Mo/s 30.6 Mo/s 471.0 sLi 520.7 sLa 20%
					 * 00:00:00 12.4/28.2 Mo/s 99% [ETA 00:00:00]
					 */
					
					synchronized (out) {
						out.print(TimeUtils.secondsToYWDHMS((long) current_duration));
						out.print("\t"); //$NON-NLS-1$
						out.format(locale, "%,.1f", instantbitrate / 1024d); //$NON-NLS-1$
						out.print(" MB\t"); //$NON-NLS-1$
						out.format(locale, "%,.1f", averagebitrate / 1024d); //$NON-NLS-1$
						out.print(" MB\t"); //$NON-NLS-1$
						out.print(Math.round(last_ratio_complete * 100));
						out.print("%\t[ETA "); //$NON-NLS-1$
						out.print(TimeUtils.secondsToYWDHMS((long) averagetimeleft));
						out.println("]"); //$NON-NLS-1$
						out.flush();
					}
					instant_starttime = System.nanoTime();
					current_dataxfer = datapos;
					
				}
			}
			sleep(10);
		} catch (Exception e) {
			e.printStackTrace(out);
		}
		
	}
	
	/**
	 * Arrondi avec un certain nombre de chiffres apres la virgule.
	 * @param prec le nombre de chiffres : 1 = 1.2, 2 = 1.23
	 */
	public static double roundTo(double value, int prec) {
		double coef = Math.pow(10, prec);
		return Math.round(value * coef) / coef;
	}
	
	/**
	 * @return le debit moyen actuel en ko/sec
	 */
	public long getCurrentbitrate() {
		return averagebitrate;
	}
	
	/**
	 * @return le debit instantane en ko/sec
	 */
	public long getInstantbitrate() {
		return instantbitrate;
	}
	
	/**
	 * @return La position (entre 0 et 1) de l'etat du travail.
	 */
	public double getCurrent_ratio_complete() {
		return current_ratio_complete;
	}
	
	/**
	 * @return Le temps restant, en seconde, en prenant en compte les valeurs moyennes;
	 */
	public double getAveragetimeleft() {
		return averagetimeleft;
	}
}
