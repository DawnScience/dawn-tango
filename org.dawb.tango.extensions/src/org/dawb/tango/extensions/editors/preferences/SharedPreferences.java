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

import org.dawb.common.ui.widgets.LabelFieldEditor;
import org.dawb.tango.extensions.Activator;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class SharedPreferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage{

	public static final String ID = "org.dawb.tango.extensions.shared.preferences";
	
	public SharedPreferences() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}


	@Override
	protected void createFieldEditors() {

		{
			final IntegerFieldEditor monFreq = new IntegerFieldEditor(SharedConstants.MON_FREQ, "Monitor Frequency in ms", getFieldEditorParent());
			monFreq.setValidRange(500, 10000);
			monFreq.getLabelControl(getFieldEditorParent()).setToolTipText("The frequency to go to the shared memory and attempt to read it. If the memory has not changed since the last read, nothing will happen.");
			addField(monFreq);
		}

		{
			final IntegerFieldEditor histSize = new IntegerFieldEditor(SharedConstants.HISTORY_SIZE, "History size", getFieldEditorParent());
			histSize.setValidRange(5, 10000);
			histSize.getLabelControl(getFieldEditorParent()).setToolTipText("The history size when the system is in history mode.");
			addField(histSize);
		}

		{
			final IntegerFieldEditor chunkIndex = new IntegerFieldEditor(SharedConstants.CHUNK_INDEX, "Chunk Index", getFieldEditorParent());
			chunkIndex.setValidRange(0, 999);
			chunkIndex.getLabelControl(getFieldEditorParent()).setToolTipText("The chunk index when reading chunks of spectra to add to the history. This is a 0 based index, if set to more than the chunk size, the chunk size - 1 will be used.");
			addField(chunkIndex);
		}

		{
			addField(new LabelFieldEditor(" ", getFieldEditorParent()));
			final LabelFieldEditor label = new LabelFieldEditor("Advanced Options", getFieldEditorParent());
			label.getLabelControl(getFieldEditorParent()).setToolTipText("Advanced options below may also need the spec shared memory variable to be changed or the macro altered or tango reconfigured, use with caution.");
			addField(label);
		}

		{
			final IntegerFieldEditor chunkSize = new IntegerFieldEditor(SharedConstants.CHUNK_SIZE, "Chunk Size", getFieldEditorParent());
			chunkSize.setValidRange(1, 1000);
			chunkSize.getLabelControl(getFieldEditorParent()).setToolTipText("The size of the rows in the shared memory to read when updating.");
			addField(chunkSize);
		}

		{
			final StringFieldEditor tangoAddress = new StringFieldEditor(SharedConstants.SPEC_SHARED, "Spec Tango Uri", getFieldEditorParent());
			tangoAddress.getLabelControl(getFieldEditorParent()).setToolTipText("Change this URI only if you are able to match it with the tango server.");
			addField(tangoAddress);
		}
	}


	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}

}
