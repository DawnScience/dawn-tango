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

import java.util.Arrays;
import java.util.List;

import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.tango.extensions.Activator;
import org.dawb.tango.extensions.editors.SharedMemoryEditor;
import org.dawb.tango.extensions.editors.SharedMemoryUtils;
import org.dawb.tango.extensions.factory.TangoConnection;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SharedMemoryNamesAction extends Action implements IMenuCreator {

	private static final Logger logger = LoggerFactory.getLogger(SharedMemoryNamesAction.class);
	
	SharedMemoryNamesAction() {
		super("Choose shared memory variable", IAction.AS_DROP_DOWN_MENU);
		setMenuCreator(this);
		setImageDescriptor(Activator.imageDescriptorFromPlugin("icons/image_add.png"));
		setId(SharedMemoryNamesAction.class.getName());
	}
	
	private Menu fMenu;
	private CheckableActionGroup group;

	@Override
	public void dispose() {
		if (fMenu != null)  {
			fMenu.dispose();
			fMenu= null;
		}
	}

	@Override
	public Menu getMenu(Control parent) {
		if (fMenu != null) fMenu.dispose();

		fMenu= new Menu(parent);
		group = new CheckableActionGroup();

		final List<String> sharedNames = getSharedNames();
		for (String name : sharedNames) {
			addActionToMenu(fMenu, name);
		}

		return fMenu;
	}


	private List<String> getSharedNames() {
		
        final SharedMemoryEditor ed = (SharedMemoryEditor)EclipseUtils.getActivePage().getActiveEditor();
        final TangoConnection    con= ed.getTangoConnection();
        if (con==null) return Arrays.asList(new String[]{"Error - Tango Settings Invalid"});
		try {
			return SharedMemoryUtils.getSharedNames(con);
		} catch (Exception e) {
			logger.error("Cannot read shared names!", e);
			
			ed.showOpenConfigurationMessage(e.getMessage());
			
			return Arrays.asList(new String[]{"Error - Tango Settings Invalid"});
			
		}
	}

	protected void addActionToMenu(Menu parent, String name) {
		ActionContributionItem item= new ActionContributionItem(new SharedMemoryNameAction(name));
		item.fill(parent, -1);
	}
	
	@Override
	public Menu getMenu(Menu parent) {
	
		return null;
	}

	public class SharedMemoryNameAction extends Action {

		public SharedMemoryNameAction(String name) {
			super(name, IAction.AS_CHECK_BOX);
			group.add(this);	
		}
		
		public void run() {
			final IEditorPart part = EclipseUtils.getActivePage().getActiveEditor();
			setChecked(true);			
			if (part instanceof SharedMemoryEditor) {
				try {
					((SharedMemoryEditor)part).setMemoryName(getText());
				} catch (Exception e) {
					logger.error("Cannot change memory name!", e);
				}
			}
		}

	}


}
