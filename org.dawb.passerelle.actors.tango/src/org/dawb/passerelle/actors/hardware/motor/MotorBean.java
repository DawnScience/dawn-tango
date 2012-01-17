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

public class MotorBean {

	private String  motorName;
	private String  expression;
	private String  attributeName;
	private boolean readOnly;
	
	public MotorBean() {}
	public MotorBean(String name, String expr) {
		setMotorName(name);
		setExpression(expr);
	}
	public String getMotorName() {
		return motorName;
	}
	public void setMotorName(String actorName) {
		this.motorName = actorName;
	}
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributeName == null) ? 0 : attributeName.hashCode());
		result = prime * result
				+ ((expression == null) ? 0 : expression.hashCode());
		result = prime * result
				+ ((motorName == null) ? 0 : motorName.hashCode());
		result = prime * result + (readOnly ? 1231 : 1237);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MotorBean other = (MotorBean) obj;
		if (attributeName == null) {
			if (other.attributeName != null)
				return false;
		} else if (!attributeName.equals(other.attributeName))
			return false;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		if (motorName == null) {
			if (other.motorName != null)
				return false;
		} else if (!motorName.equals(other.motorName))
			return false;
		if (readOnly != other.readOnly)
			return false;
		return true;
	}
	public boolean isReadOnly() {
		return readOnly;
	}
	public boolean getReadOnly() {
		return readOnly;
	}
	public void setReadOnly(boolean isReadOnly) {
		this.readOnly = isReadOnly;
	}
	public String getAttributeName() {
		return attributeName;
	}
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
}
