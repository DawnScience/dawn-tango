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

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.tango.extensions.editors.MultiScanMultiEditor;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;

public class ConnectTangoHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		final IEditorPart part = EclipseUtils.getActiveEditor();
		if (part instanceof MultiScanMultiEditor) {
			final MultiScanMultiEditor sme = (MultiScanMultiEditor)part;
			sme.toggleConnect();
		}
		return Boolean.TRUE;
	}

}
