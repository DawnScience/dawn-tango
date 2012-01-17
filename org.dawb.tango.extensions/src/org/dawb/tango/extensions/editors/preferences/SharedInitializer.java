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
public class SharedInitializer extends AbstractPreferenceInitializer {


	@Override
	public void initializeDefaultPreferences() {
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(SharedConstants.SHARED_MON,  true);
		store.setDefault(SharedConstants.IMAGE_MODE,  true);
		store.setDefault(SharedConstants.HISTORY_MODE,true);
		store.setDefault(SharedConstants.MON_FREQ,    1000L);
		store.setDefault(SharedConstants.SPEC_SHARED, "spec/shm");
		store.setDefault(SharedConstants.CHUNK_SIZE,   10);
		store.setDefault(SharedConstants.HISTORY_SIZE, 20);
		store.setDefault(SharedConstants.CHUNK_INDEX,  9);
	}
}
