/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.tango.extensions.editors.actions;

import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.preferences.TangoPreferencePage;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.tango.extensions.Activator;
import org.dawb.tango.extensions.editors.SharedMemoryEditor;
import org.dawb.tango.extensions.editors.preferences.SharedConstants;
import org.dawb.tango.extensions.editors.preferences.SharedPreferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Not intended for outside use.
 * @author gerring
 *
 */
public class SharedMemoryActions {

	private static final Logger logger = LoggerFactory.getLogger(SharedMemoryActions.class);
	
	public static final Action monitoring, sharedMemory, history, chunk, oneD, twoD, tangoPrefs, sharedPrefs;
	static {
		
		sharedMemory = new SharedMemoryNamesAction();
		
			
		monitoring = new Action("Start/Stop Monitoring", Action.AS_CHECK_BOX) {
			public void run() {
				final IEditorPart part = EclipseUtils.getActivePage().getActiveEditor();
				
				final boolean mon = !Activator.getDefault().getPreferenceStore().getBoolean(SharedConstants.SHARED_MON);
				if (part instanceof SharedMemoryEditor) {
					try {
						((SharedMemoryEditor)part).setMonitoring(mon);
					} catch (Exception e) {
						logger.error("Cannot change monitoring state shared memory", e);
					}
				}
				if (mon) {
					monitoring.setImageDescriptor(Activator.getImageDescriptor("icons/control_stop_blue.png"));
				} else {
					monitoring.setImageDescriptor(Activator.getImageDescriptor("icons/control_play_blue.png"));
				}
				Activator.getDefault().getPreferenceStore().setValue(SharedConstants.SHARED_MON, mon);
			}
		};
		
		if (Activator.getDefault().getPreferenceStore().getBoolean(SharedConstants.SHARED_MON)) {
			monitoring.setChecked(true);
			monitoring.setImageDescriptor(Activator.getImageDescriptor("icons/control_stop_blue.png"));
		} else {
			monitoring.setChecked(false);
			monitoring.setImageDescriptor(Activator.getImageDescriptor("icons/control_play_blue.png"));
		}
		
		CheckableActionGroup group = new CheckableActionGroup();
		history = new Action("History mode, display 1 from N. See shared memory properties for settings.", Action.AS_CHECK_BOX) {
			public void run() {
				Activator.getDefault().getPreferenceStore().setValue(SharedConstants.HISTORY_MODE, true);
				final IEditorPart part = EclipseUtils.getActivePage().getActiveEditor();
				if (part instanceof SharedMemoryEditor) {
					((SharedMemoryEditor)part).setHistoryMode(true);
				}
			}
		};
		group.add(history);
		history.setImageDescriptor(Activator.getImageDescriptor("icons/history.gif"));
		
		chunk = new Action("Chunk mode, continously refresh the last chunk of spectra.", Action.AS_CHECK_BOX) {
			public void run() {
				Activator.getDefault().getPreferenceStore().setValue(SharedConstants.HISTORY_MODE, false);
				final IEditorPart part = EclipseUtils.getActivePage().getActiveEditor();
				if (part instanceof SharedMemoryEditor) {
					((SharedMemoryEditor)part).setHistoryMode(false);
				}
			}
		};
		group.add(chunk);
		chunk.setImageDescriptor(Activator.getImageDescriptor("icons/chunk.gif"));
			
	
		group = new CheckableActionGroup();
		oneD = new Action("Plot memory in one dimension.", Action.AS_CHECK_BOX) {
			public void run() {
				Activator.getDefault().getPreferenceStore().setValue(SharedConstants.IMAGE_MODE, false);
				final IEditorPart part = EclipseUtils.getActivePage().getActiveEditor();
				if (part instanceof SharedMemoryEditor) {
					((SharedMemoryEditor)part).setPlotType(PlotType.PT1D);
				}
			}
		};
		group.add(oneD);
		oneD.setImageDescriptor(Activator.getImageDescriptor("icons/chart_line.png"));
			
		
		twoD = new Action("Plot memory as an image.", Action.AS_CHECK_BOX) {
			public void run() {
				Activator.getDefault().getPreferenceStore().setValue(SharedConstants.IMAGE_MODE, true);
				final IEditorPart part = EclipseUtils.getActivePage().getActiveEditor();
				if (part instanceof SharedMemoryEditor) {
					((SharedMemoryEditor)part).setPlotType(PlotType.IMAGE);
				}
			}
		};
		group.add(twoD);
		twoD.setImageDescriptor(Activator.getImageDescriptor("icons/picture.png"));

		tangoPrefs = new Action("Tango Preferences...", Action.AS_PUSH_BUTTON) {
			public void run() {
				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
						                                                         TangoPreferencePage.ID, null, null);
				if (pref != null)pref.open();

	    	}
	    };
	    tangoPrefs.setImageDescriptor(Activator.getImageDescriptor("icons/tango_preferences.gif"));
	    
		sharedPrefs = new Action("Shared Memory Preferences...", Action.AS_PUSH_BUTTON) {
			public void run() {
				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
						                                                         SharedPreferences.ID, null, null);
				if (pref != null)pref.open();

	    	}
	    };
	    sharedPrefs.setImageDescriptor(Activator.getImageDescriptor("icons/sharedprops.gif"));

	}
	
	public static void createActions(final IContributionManager man) {
		
		man.add(sharedMemory);
		man.add(new Separator(SharedMemoryActions.class.getName()+".sep0"));
		man.add(monitoring);
		man.add(new Separator(SharedMemoryActions.class.getName()+".sep1"));
		man.add(history);
		man.add(chunk);
		man.add(new Separator(SharedMemoryActions.class.getName()+".sep2"));
		man.add(oneD);
		man.add(twoD);
		man.add(new Separator(SharedMemoryActions.class.getName()+".sep3"));
		man.add(sharedPrefs);
		man.add(tangoPrefs);
		
		final boolean isImage = Activator.getDefault().getPreferenceStore().getBoolean(SharedConstants.IMAGE_MODE);
		if (isImage) {
			twoD.setChecked(true);
		} else {
			oneD.setChecked(true);
		}

		final boolean isHist = Activator.getDefault().getPreferenceStore().getBoolean(SharedConstants.HISTORY_MODE);
		if (isHist) {
			history.setChecked(true);
		} else {
			chunk.setChecked(true);
		}
	}

	
}
