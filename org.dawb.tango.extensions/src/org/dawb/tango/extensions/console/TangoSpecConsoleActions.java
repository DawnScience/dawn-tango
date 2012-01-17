/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.tango.extensions.console;

import org.dawb.common.ui.views.monitor.actions.TangoPreferencesAction;
import org.dawb.tango.extensions.Activator;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;

public class TangoSpecConsoleActions implements IConsolePageParticipant {

	
	private Action preferences, stop;
	private IActionBars bars;
	private TangoSpecConsolePage page;

	@Override
	public void init(final IPageBookViewPage page, final IConsole console) {
        
		if (!(page instanceof TangoSpecConsolePage)) return;
		
		this.page    = (TangoSpecConsolePage)page;
		this.bars    = page.getSite().getActionBars();
		 
		this.preferences = new Action("Tango Preferences...", Activator.imageDescriptorFromPlugin("icons/tango_preferences.gif")) {
			public void run() {
				try {
					(new TangoPreferencesAction()).execute(null);
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		};
		
	
		this.stop = new Action("Stop spec session", Activator.imageDescriptorFromPlugin("icons/stop_spec_console.gif")) {
			public void run() {
		    	IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
		    	manager.removeConsoles(new IConsole[]{console});
			}
		};
		
		bars.getMenuManager().add(new Separator());
		bars.getMenuManager().add(preferences);
	
	    IToolBarManager toolbarManager = bars.getToolBarManager();

        toolbarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, stop);
        toolbarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, preferences);

        bars.updateActionBars();
        

	}
	
	@Override
	public void dispose() {
		preferences = null;
		stop        = null;
		bars        = null;
		page        = null;
	}


	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}
	@Override
	public void activated() {
		updateVis();
	}
	@Override
	public void deactivated() {
		updateVis();
	}

	private void updateVis() {
		
		if (page==null) return;
		boolean isEnabled = page.getViewer().getTextWidget().isFocusControl();
		stop.setEnabled(isEnabled);
		preferences.setEnabled(isEnabled);
		bars.updateActionBars();
	}

}
