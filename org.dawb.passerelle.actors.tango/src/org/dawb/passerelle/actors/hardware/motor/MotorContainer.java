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

import java.util.ArrayList;
import java.util.List;

public class MotorContainer {

	private List<MotorBean> expressions;
	
	public MotorContainer() {
		expressions = new ArrayList<MotorBean>();
	}
	
	public void clear() {
		if (expressions!=null) expressions.clear();
	}

	public List<MotorBean> getExpressions() {
		return expressions;
	}

	public void setExpressions(List<MotorBean> expressions) {
		this.expressions = expressions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((expressions == null) ? 0 : expressions.hashCode());
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
		MotorContainer other = (MotorContainer) obj;
		if (expressions == null) {
			if (other.expressions != null)
				return false;
		} else if (!expressions.equals(other.expressions))
			return false;
		return true;
	}


	public MotorBean getBean(String name) {
		if (name==null)        return null;
		if (expressions==null) return null;
		for (MotorBean b : expressions) {
			if (name.equals(b.getMotorName())) return b;
		}
		return null;
	}
	
	/**
	 * Constructs user readable version of bean
	 */
	public String toString() {
		if (expressions==null||expressions.isEmpty()) return "No motors set or read, click to edit...";
		final StringBuilder buf = new StringBuilder();
		for (MotorBean b : expressions) {
			if (b.isReadOnly()) {
				buf.append("read '");
				buf.append(b.getMotorName());
				buf.append("'");
			} else {
				buf.append("set '");
				buf.append(b.getMotorName());
				buf.append("' to ");
				buf.append(b.getExpression());
			}
			buf.append(";  ");
		}
		return buf.toString();
	}

	public int size() {
		return expressions.size();
	}

	public boolean isEmpty() {
		return expressions==null||size()<1;
	}
}
