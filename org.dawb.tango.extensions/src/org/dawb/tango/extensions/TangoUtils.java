/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.tango.extensions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dawb.common.ui.Activator;
import org.dawb.common.ui.preferences.CommonUIPreferenceConstants;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import fr.esrf.TangoApi.ConnectionFailed;

public class TangoUtils {

	public static final Pattern COMMENT = Pattern.compile("\\#.*");
	
	public static final Pattern CMD;
	public static final Pattern PRINT;
	public static final Pattern SCAN_LINE;
	public static final Pattern HEADER_LINE;
	
	private static final String TXT    = "([a-zA-Z][_a-zA-Z\\d]*)";
    private static final String NUM    = "([-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?)";
	private static final String TXTWS  = "([ \t]+[a-zA-Z][_a-zA-Z\\d]*)";
	
	private static final String TXTWS2_NOSPC  = "([ \t]*[_a-zA-Z\\d]*[ ]?[a-zA-Z]?[\\d]*)";
	private static final String TXTWS2 = "([ \t]+[_a-zA-Z\\d]*[ ]?[a-zA-Z]?[\\d]*)";
	
    private static final String NUMWS  = "([ \t]+[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?)";
	
	static {
		
		StringBuilder buf = new StringBuilder();
		buf.append(TXT);
		buf.append(TXTWS);
		buf.append(NUMWS);
		for (int i = 0; i < 3; i++) {
			buf.append(NUMWS);
			buf.append("?");
		}
		CMD = Pattern.compile(buf.toString());
		
		buf = new StringBuilder();
		buf.append(NUM);
		buf.append(NUMWS);
		for (int i = 0; i < 50; i++) {
			buf.append(NUMWS);
			buf.append("?");
		}
		SCAN_LINE = Pattern.compile(buf.toString());
		
		buf = new StringBuilder();
		buf.append("#");
		buf.append(TXTWS2_NOSPC);
		for (int i = 0; i < 50; i++) {
			buf.append(TXTWS2);
			buf.append("?");
		}
		HEADER_LINE = Pattern.compile(buf.toString());

		PRINT = Pattern.compile("print ([a-zA-Z0-9\\'\"]+)");
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(CMD.pattern());
		System.out.println("1.0".matches(NUM));
		
		Matcher test = CMD.matcher("ascan phi 0.1 10 10 .1");
		System.out.println(test.matches());
		
		System.out.println(CMD.matcher("ascan phi 0 10 10 1").matches());
		System.out.println(CMD.matcher("ascan kap1 0 10 10 1").matches());
		
		test = SCAN_LINE.matcher("1.0 0.0 1.0 1.0 0.107 1.0");
		System.out.println(test.matches());
		System.out.println(test.groupCount());
		
		test = SCAN_LINE.matcher("0  0.0000  0 0   0.107  0");
		System.out.println(test.matches());
		System.out.println(test.groupCount());
		
		test = SCAN_LINE.matcher("0    0.0000        0        0      0.107        0");
		System.out.println(test.matches());
		System.out.println(test.groupCount());

		test = HEADER_LINE.matcher("#       Phi Detector  Monitor    Seconds  Flux I0");
		System.out.println(test.matches());
		System.out.println(test.groupCount());

		System.out.println("FINISHED!");

	}

	public static String getHardwareAddress(final String hardwareName) {
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		final StringBuffer buf = new StringBuffer("//");
		buf.append(store.getString(CommonUIPreferenceConstants.SERVER_NAME));
		buf.append(":");
		buf.append(store.getInt(CommonUIPreferenceConstants.SERVER_PORT));
		buf.append("/");
		buf.append(store.getString(CommonUIPreferenceConstants.BEAMLINE_NAME));
		buf.append("/");
		buf.append(hardwareName);
		
		return buf.toString();
	}

	/**
	 * Converts a command as typed on the command line or into a 
	 * macro file into a true spec command.
	 * 
	 * @param typedCommand
	 * @return
	 */
	public static String getBracketedCommand(String typedCommand) {
		
		if (typedCommand==null) return null;
		typedCommand = typedCommand.trim();
		
		final StringBuilder buf = new StringBuilder();
		if (!typedCommand.startsWith("eval('")) {
			buf.append("eval('");
		}
		
		buf.append(typedCommand);
		if (!typedCommand.startsWith("eval('") && !typedCommand.endsWith("')")) {
			buf.append("')");
		}
		
		return buf.toString();
	}
	
	public static Matcher getBracketedMatcher(final String typedCommand) {
		
		final Matcher matcher = CMD.matcher(typedCommand);
		if (matcher.matches()) {
			return matcher;
		}
		return null;
	}

	public static String getSpecName() {
		
		IPreferenceStore commonStore =  new ScopedPreferenceStore(new InstanceScope(),"org.dawb.common.ui");
		String specName;
		boolean isDummy = commonStore.getBoolean(CommonUIPreferenceConstants.MOCK_SESSION);
		if (isDummy) {
			specName = "SPEC_mock";
		} else {
			specName = commonStore.getString(CommonUIPreferenceConstants.SPEC_NAME);
			specName = specName.toUpperCase();
		}
		return specName;
	}

	public static String getSpecCommandAddress() {
		return TangoUtils.getHardwareAddress("spec/"+getSpecName().toLowerCase());
	}

	
	public static String getMessageFromException(final String cmd, final Exception ne) {
		if (ne instanceof ConnectionFailed) {
	    	ConnectionFailed cf = (ConnectionFailed)ne;
			if (cf.errors!=null&&cf.errors.length>0) {
				return cf.errors[0].desc;
			} else {
				return "An error occurred sending command: \""+cmd+"\". It has not been run.\n"+cf.getMessage(); 
			}
	    } else {
	    	return ne.getMessage();
	    }
	}
}
