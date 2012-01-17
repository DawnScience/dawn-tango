/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.tango.extensions.jive;

import java.awt.Frame;

import jive3.MainPanel;

import org.dawb.common.ui.Activator;
import org.dawb.common.ui.preferences.CommonUIPreferenceConstants;
import org.dawb.common.ui.util.GridUtils;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

/**
 * Adding the jive application as a view in the workbench
 * 
 * This causes the old swing problem on linux. ESRF should port
 * Jive to RCP as soon as practicable.
 * 
 * @author gerring
 *
 */
public class JiveView extends ViewPart {

	public static final String ID = "org.dawb.tango.extensions.JiveView"; //$NON-NLS-1$
	private Composite swtAwtComponent;
	private Frame frame;

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		
		parent.setLayout(new GridLayout(1, false));		
		GridUtils.removeMargins(parent);
		
		final CLabel warning = new CLabel(parent, SWT.NONE);
		warning.setText("This part is in beta form and not recommended for general use.");
		warning.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
		warning.setImage(org.dawb.tango.extensions.Activator.imageDescriptorFromPlugin("icons/warning.gif").createImage());
		warning.setToolTipText("The part makes a system call to exit which can cause the workbench to exit unexpectedly.");
		
		swtAwtComponent = new Composite(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND);

		GridData gdlist = new GridData(SWT.FILL, SWT.FILL, true, true);
		gdlist.verticalSpan = 1;
		gdlist.horizontalSpan = 1;
		swtAwtComponent.setLayout(new GridLayout());
		GridUtils.removeMargins(swtAwtComponent);
		swtAwtComponent.setLayoutData(gdlist);
		
		frame = SWT_AWT.new_Frame(swtAwtComponent);
	
		createSystemProperties();
		
		// Bodge - should be non-frame component which you can use
		// at the top level but would have to change jive to do that
		// as concrete class MainPanel is passed around all over Jive :(
		final MainPanel jive = new MainPanel();
		jive.setVisible(false); // Causes frame to popup, unavoidable.
		
		frame.setMenuBar(jive.getMenuBar());
		frame.add(jive.getContentPane());
		jive.dispose();
		
		createActions();
		initializeToolBar();
		initializeMenu();
	}

	private void createSystemProperties() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		final StringBuffer buf = new StringBuffer();
		buf.append(store.getString(CommonUIPreferenceConstants.SERVER_NAME));
		buf.append(":");
		buf.append(store.getInt(CommonUIPreferenceConstants.SERVER_PORT));		    	
		System.setProperty("TANGO_HOST", buf.toString());

	}

	@Override
	public void dispose() {
		
		super.dispose();
		
		// Not really necessary normally but nullifying things
		// helps the garbage collector.
		if (swtAwtComponent!=null) swtAwtComponent.dispose();
		swtAwtComponent=null;
		if (frame!=null) frame.dispose();
		frame          =null;
	}
	
	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

}
