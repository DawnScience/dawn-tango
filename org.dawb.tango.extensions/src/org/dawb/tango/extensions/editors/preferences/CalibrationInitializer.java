/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.tango.extensions.editors.preferences;

import org.dawb.tango.extensions.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
public class CalibrationInitializer extends AbstractPreferenceInitializer {


	@Override
	public void initializeDefaultPreferences() {
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(CalibrationConstants.USE,  false);
		store.setDefault(CalibrationConstants.EXPR, "a + b(p-p0) + c(p-p0)^2 + d(p-p0)^3");
		
		// TODO default for a,b,c, d
		store.setDefault(CalibrationConstants.A,    1000);
		store.setDefault(CalibrationConstants.B,    2);
		store.setDefault(CalibrationConstants.C,    0.25);
		store.setDefault(CalibrationConstants.D,    0);
		
		store.setDefault(CalibrationConstants.LABEL,  "Energy");
	}
}
