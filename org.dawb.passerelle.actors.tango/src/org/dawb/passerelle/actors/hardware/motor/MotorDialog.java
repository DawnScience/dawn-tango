/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.passerelle.actors.hardware.motor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.util.GridUtils;
import org.dawb.passerelle.common.actors.AbstractDataMessageTransformer;
import org.dawb.passerelle.common.message.IVariable;
import org.eclipse.richbeans.widgets.dialog.BeanDialog;
import org.eclipse.richbeans.widgets.selector.BeanSelectionEvent;
import org.eclipse.richbeans.widgets.selector.BeanSelectionListener;
import org.eclipse.richbeans.widgets.selector.VerticalListEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ptolemy.kernel.util.NamedObj;

public class MotorDialog extends BeanDialog {

	private VerticalListEditor expressions;
	
	/**
	 * Used to check expressions entered.
	 */
	private AbstractDataMessageTransformer parent;
	
	protected MotorDialog(Shell parentShell, NamedObj container) {
		super(parentShell);
		this.parent = (AbstractDataMessageTransformer)container;
	}
	
	public Control createDialogArea(Composite parent) {
		
		final Composite main = (Composite)super.createDialogArea(parent);
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		final Label label = new Label(main, SWT.WRAP);
		label.setText("Set and read motor values here. The expression is used to set value, the motor value is read into a variable of the same name as the motor. For instance if the motor path is 'motors/phi', the motor name is 'phi'. Motors written are always read afterwards and their value passed on.");
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		expressions = new VerticalListEditor(main, SWT.NONE);
		expressions.setRequireSelectionPack(false);
		expressions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		expressions.setMinItems(0);
		expressions.setMaxItems(25);
		expressions.setDefaultName("motor");
		expressions.setEditorClass(MotorBean.class);
		
		final MotorComposite motorComp = createMotorComposite();
		expressions.setEditorUI(motorComp);
		expressions.setNameField("motorName");
		expressions.setAdditionalFields(new String[]{"expression"});
		expressions.setColumnWidths(new int[]{100, 300});
		expressions.setListHeight(150);
		expressions.addBeanSelectionListener(new BeanSelectionListener() {
			@Override
			public void selectionChanged(BeanSelectionEvent evt) {
				motorComp.updateVisibility();
			}
		});
		
		GridUtils.setVisible(expressions, true);
		expressions.getParent().layout(new Control[]{expressions});
		
		return main;
	}

	private MotorComposite createMotorComposite() {
		
		final MotorComposite expressionComposite = new MotorComposite(expressions, SWT.NONE);
		
		final Map<String,Object> values = new HashMap<String,Object>(7);
		final List<IVariable>    vars   = parent.getInputVariables();
		for (IVariable var : vars) {
			Object value = var.getExampleValue();
			if (value instanceof String) {
				try {
					value = Double.parseDouble((String)value);
				} catch (Exception igonred) {
					// Nothing
				}
			}
			values.put(var.getVariableName(), value);
		}
		expressionComposite.setExpressionVariables(values);
		return expressionComposite;
	}

	public VerticalListEditor getExpressions() {
		return expressions;
	}
	
	public int open() {
		expressions.setShowAdditionalFields(true);
        int ret = super.open();
        expressions = null;
        return ret;
	}
}
