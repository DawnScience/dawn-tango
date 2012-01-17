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

import java.util.Map;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;
import uk.ac.gda.richbeans.components.wrappers.TextWrapper;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;

import com.swtdesigner.ResourceManager;

public class MotorComposite extends Composite {

	private TextWrapper motorName,expression,attributeName;
	private BooleanWrapper readOnly;
	private Label expressionLabel;
	private ControlDecoration controlDecoration;

	public MotorComposite(Composite parent, int style) {
		
		super(parent, style);
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		setLayout(new GridLayout(1, false));
		
		final Composite main = new Composite(this, SWT.NONE);
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		main.setLayout(new GridLayout(2, false));

		final Label motorLabel = new Label(main, SWT.NONE);
		motorLabel.setText("Motor Name");
		
		controlDecoration = new ControlDecoration(motorLabel, SWT.LEFT | SWT.TOP);
		controlDecoration.setImage(ResourceManager.getPluginImage("org.dawb.passerelle.actors", "icons/hardware.gif"));
		controlDecoration.setDescriptionText("The motor path after the beamline part of the path, for instance 'motors/phi'");
		
		this.motorName = new TextWrapper(main, SWT.NONE);
		motorName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		motorName.setTextLimit(64);
		
		final Label readOnlyLabel = new Label(main, SWT.NONE);
		readOnlyLabel.setText("Read only");

		readOnly = new BooleanWrapper(main, SWT.NO_FOCUS);
		//readOnly.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		readOnly.addValueListener(new ValueAdapter() {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateVisibility();
			}
		});

		this.expressionLabel = new Label(main, SWT.NONE);
		expressionLabel.setText("Value");
		
		this.expression = new TextWrapper(main, SWT.NONE);
		expression.setTextType(TextWrapper.TEXT_TYPE.EXPRESSION);
		expression.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		

		final ExpandableComposite advancedComposite = new ExpandableComposite(this, SWT.NONE);
		advancedComposite.setExpanded(false);
		advancedComposite.setText("Advanced");
		advancedComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		final Composite advanced = new Composite(this, SWT.NONE);
		advanced.setLayout(new GridLayout(2, false));
		advanced.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		final Label label = new Label(advanced, SWT.NONE);
		label.setText("Attribute Name");
		
		this.attributeName = new TextWrapper(advanced, SWT.NONE);
		attributeName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		attributeName.setToolTipText("The attribute name or 'Position' if left blank.");
		
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

	public TextWrapper getMotorName() {
		return motorName;
	}

	public TextWrapper getExpression() {
		return expression;
	}
	
	public TextWrapper getAttributeName() {
		return attributeName;
	}
	
	public BooleanWrapper getReadOnly() {
		return readOnly;
	}

	protected void setExpressionVariables(final Map<String, Object> vars) {
		expression.setExpressionVariables(vars);
	}

	public void updateVisibility() {
		GridUtils.setVisible(expressionLabel, !readOnly.getValue().booleanValue());
		GridUtils.setVisible(expression,      !readOnly.getValue().booleanValue());
		
		layout(new Control[]{expressionLabel, expression});
	}
}
