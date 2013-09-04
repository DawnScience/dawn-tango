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

import org.dawb.common.ui.Activator;
import org.dawb.common.ui.preferences.CommonUIPreferenceConstants;
import org.dawnsci.io.spec.SpecSyntax;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import fr.esrf.TangoApi.ConnectionFailed;

public class TangoUtils {



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
		
		final Matcher matcher = SpecSyntax.CMD.matcher(typedCommand);
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
