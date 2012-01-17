/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.tango.extensions.editors.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.widgets.LabelFieldEditor;
import org.dawb.common.util.ExpressionUtils;
import org.dawb.tango.extensions.Activator;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class CalibrationPreferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage{

	public static final String ID = "org.dawb.tango.extensions.calibration.preferences";
	private List<StringFieldEditor> editors;
	private BooleanFieldEditor use;
	
	public CalibrationPreferences() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}


	@Override
	protected void createFieldEditors() {

		editors = new ArrayList<StringFieldEditor>(7);
		
		this.use = new BooleanFieldEditor(CalibrationConstants.USE, "Use calibration", getFieldEditorParent());
		addField(use);
		
		StringFieldEditor expr = new StringFieldEditor(CalibrationConstants.EXPR, "Calibration Expression", getFieldEditorParent());
		expr.getLabelControl(getFieldEditorParent()).setToolTipText("Expression to use in calibration, the variable p is used for pixel value and p0 for the first pixel.");
		addField(expr);
		editors.add(expr);
		expr.getTextControl(getFieldEditorParent()).setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL);
		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL);
		
		addField(new LabelFieldEditor("Coefficients:", getFieldEditorParent()));
		StringFieldEditor a = new StringFieldEditor(CalibrationConstants.A, "a", getFieldEditorParent());
		a.getLabelControl(getFieldEditorParent()).setToolTipText("Coefficient a used in the calibration expression.");
		addField(a);
		editors.add(a);
		
		StringFieldEditor b = new StringFieldEditor(CalibrationConstants.B, "b", getFieldEditorParent());
		b.getLabelControl(getFieldEditorParent()).setToolTipText("Coefficient b used in the calibration expression.");
		addField(b);
		editors.add(b);
		
		StringFieldEditor c = new StringFieldEditor(CalibrationConstants.C, "c", getFieldEditorParent());
		c.getLabelControl(getFieldEditorParent()).setToolTipText("Coefficient c used in the calibration expression.");
		addField(c);
		editors.add(c);
		
		StringFieldEditor d = new StringFieldEditor(CalibrationConstants.D, "d", getFieldEditorParent());
		d.getLabelControl(getFieldEditorParent()).setToolTipText("Coefficient d used in the calibration expression.");
		addField(d);
		editors.add(d);
		
		updateEnabled(Activator.getDefault().getPreferenceStore().getBoolean(CalibrationConstants.USE));
		
		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL);
		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL);
		addField(new LabelFieldEditor("", getFieldEditorParent()));
		addField(new StringFieldEditor(CalibrationConstants.LABEL, "X-axis label", getFieldEditorParent()));
	}

	public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        
        if (((FieldEditor)event.getSource()).getPreferenceName().equals(CalibrationConstants.USE)) {
        	updateEnabled((Boolean)event.getNewValue());
        }
        checkState();
	}

    protected void checkState() {

		super.checkState();
		
		final boolean         isUsed = use.getBooleanValue();
		if (!isUsed) {
			setErrorMessage(null);
			setValid(true);
			return;
		}
		
		final boolean validSyntax = ExpressionUtils.isValidSyntax(editors.get(0).getStringValue());	
		if (validSyntax) {
			editors.get(0).getTextControl(getFieldEditorParent()).setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
			setErrorMessage(null);
			setValid(true);
		} else {
			
			editors.get(0).getTextControl(getFieldEditorParent()).setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
			setErrorMessage("The expression '"+editors.get(0).getStringValue()+"' is not valid.");
			setValid(false);
		}   	
    }
    
	private void updateEnabled(boolean enabled) {
		for (StringFieldEditor ed : editors) {
			ed.setEnabled(enabled, getFieldEditorParent());
		}
		
		final Control[] controls = getFieldEditorParent().getChildren();
		for (int i = 0; i < controls.length; i++) {
			if (controls[i] instanceof Text) {
				controls[i].setEnabled(enabled);
			}
		}
		getFieldEditorParent().layout(true, true);
	}


	@Override
	public void init(IWorkbench workbench) {
		
	}

}
