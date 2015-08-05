/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.passerelle.actors.hardware.command;

import org.dawb.common.ui.util.GridUtils;
import org.eclipse.richbeans.widgets.dialog.BeanDialog;
import org.eclipse.richbeans.widgets.selector.VerticalListEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ptolemy.kernel.util.NamedObj;

public class CommandDialog extends BeanDialog {

	private VerticalListEditor beans;
	
	
	protected CommandDialog(Shell parentShell, NamedObj container) {
		super(parentShell);
	}
	
	public Control createDialogArea(Composite parent) {
		
		final Composite main = (Composite)super.createDialogArea(parent);
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		final Label label = new Label(main, SWT.WRAP);
		label.setText("Call individual commands and record their output here. Note that you can also substitute values directly into a spec macro using the 'Spec Macro' attribute, however that does not record response.");
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		beans = new VerticalListEditor(main, SWT.NONE);
		beans.setRequireSelectionPack(false);
		beans.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		beans.setMinItems(0);
		beans.setMaxItems(25);
		beans.setDefaultName("command_result");
		beans.setEditorClass(CommandBean.class);
		
		final CommandComposite beanComp =  new CommandComposite(beans, SWT.NONE);
		beans.setEditorUI(beanComp);
		beans.setNameField("variableName");
		beans.setAdditionalFields(new String[]{"command"});
		beans.setColumnWidths(new int[]{120, 300});
		beans.setListHeight(150);
		
		GridUtils.setVisible(beans, true);
		beans.getParent().layout(new Control[]{beans});
		return main;
	}

	public VerticalListEditor getBeans() {
		return beans;
	}
	
	public int open() {
		beans.setShowAdditionalFields(true);
        int ret = super.open();
        beans = null;
        return ret;
	}
}
