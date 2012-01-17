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

import java.io.File;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenSpecMonitorHandler extends AbstractHandler implements IWorkbenchWindowActionDelegate{

	private static final Logger logger = LoggerFactory.getLogger(OpenSpecMonitorHandler.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		

		try {
			final File tmp = File.createTempFile("sharedMemoryMonitor", "mon");
			tmp.createNewFile();
			
			final IWorkbenchPage page = EclipseUtils.getActivePage();
			
			final IFileStore externalFile = EFS.getLocalFileSystem().fromLocalFile(tmp);
			final IEditorInput store      = new FileStoreEditorInput(externalFile);

			page.openEditor(store, "org.dawb.tango.extensions.sharedMemEditor", true);
			
		} catch (Exception ne) {
			throw new ExecutionException("Cannot open shared memory monitor!", ne);
		}
		
		
		return Boolean.TRUE;

	}

	@Override
	public void run(IAction action) {
		try {
			execute(null);
		} catch (ExecutionException e) {
			logger.error("Cannot run action", e);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		
	}


}
