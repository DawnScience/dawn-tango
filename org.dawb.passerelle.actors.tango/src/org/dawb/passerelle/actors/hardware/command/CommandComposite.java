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
import org.dawnsci.common.richbeans.components.wrappers.TextWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

public class CommandComposite extends Composite {

	private TextWrapper variableName, command;
	private TextWrapper attributeName, commandAttributeName;

	public CommandComposite(Composite parent, int style) {
		
		super(parent, style);
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		setLayout(new GridLayout(1, false));
		
		final Composite main = new Composite(this, SWT.NONE);
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		main.setLayout(new GridLayout(2, false));

		final Label variableLabel = new Label(main, SWT.NONE);
		variableLabel.setText("Variable Name");
		variableLabel.setToolTipText("The variable that the command will be read into.");
		
		this.variableName = new TextWrapper(main, SWT.NONE);
		variableName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		variableName.setTextLimit(64);
		variableName.setTextType(TextWrapper.TEXT_TYPE.EXPRESSION);
		
		final Label commandLabel = new Label(main, SWT.NONE);
		commandLabel.setText("Command");
		commandLabel.setToolTipText("The spec command to run.\nCommands of the form '<scan_command> <hardware_name> <<numerical args>...>' will have brackets and quotations inserted automatically. Variables can be inserted using '${variable_name}' notation.");
		
		this.command = new TextWrapper(main, SWT.NONE);
		command.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		command.setTextLimit(250);
		
		final ExpandableComposite advancedComposite = new ExpandableComposite(this, SWT.NONE);
		advancedComposite.setExpanded(false);
		advancedComposite.setText("Advanced");
		advancedComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		final Composite advanced = new Composite(this, SWT.NONE);
		advanced.setLayout(new GridLayout(2, false));
		advanced.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Label label = new Label(advanced, SWT.NONE);
		label.setText("Event Attribute");
		
		this.attributeName = new TextWrapper(advanced, SWT.NONE);
		attributeName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		attributeName.setToolTipText("The attribute name or 'Position' if left blank.");
		
		label = new Label(advanced, SWT.NONE);
		label.setText("Command Attribute");
		
		this.commandAttributeName = new TextWrapper(advanced, SWT.NONE);
		commandAttributeName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		commandAttributeName.setToolTipText("The command name or 'ExecuteCmd' if left blank.");
		
		GridUtils.setVisible(advanced, false);
		ExpansionAdapter expansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				GridUtils.setVisible(advanced, !advanced.isVisible());
				layout(new Control[]{advanced, advancedComposite});
				getParent().layout();
				getParent().getParent().layout();
			}
		};
		advancedComposite.addExpansionListener(expansionListener);

	}

	public TextWrapper getVariableName() {
		return variableName;
	}

	public TextWrapper getCommand() {
		return command;
	}

	public TextWrapper getAttributeName() {
		return attributeName;
	}

	public TextWrapper getCommandAttributeName() {
		return commandAttributeName;
	}

}
