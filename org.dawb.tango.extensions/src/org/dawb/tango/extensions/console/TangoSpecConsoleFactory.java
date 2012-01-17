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

import org.dawb.tango.extensions.Activator;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;

public class TangoSpecConsoleFactory implements IConsoleFactory {

    int counter = 1;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.console.IConsoleFactory#openConsole()
     */
    public void openConsole() {
    	IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
    	final TangoSpecConsole console = createConsole();
    	manager.addConsoles(new IConsole[] { console });
    	manager.showConsoleView(console);
    }

    private int num = 0;
    
	private TangoSpecConsole createConsole() {
		num++;
		return new TangoSpecConsole("Spec Console "+num, Activator.imageDescriptorFromPlugin("icons/application_xp_terminal.png"), true);
	}

}
