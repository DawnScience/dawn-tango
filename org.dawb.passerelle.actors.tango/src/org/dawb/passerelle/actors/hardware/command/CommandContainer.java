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

import java.util.ArrayList;
import java.util.List;

public class CommandContainer {

	private List<CommandBean> beans;
	
	public CommandContainer() {
		beans = new ArrayList<CommandBean>();
	}
	
	public void clear() {
		if (beans!=null) beans.clear();
	}

	public List<CommandBean> getBeans() {
		return beans;
	}

	public void setBeans(List<CommandBean> expressions) {
		this.beans = expressions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((beans == null) ? 0 : beans.hashCode());
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
		CommandContainer other = (CommandContainer) obj;
		if (beans == null) {
			if (other.beans != null)
				return false;
		} else if (!beans.equals(other.beans))
			return false;
		return true;
	}
	
	/**
	 * Constructs user readable version of bean
	 */
	public String toString() {
		if (beans==null||beans.isEmpty()) return "No commands created, click to edit...";
		final StringBuilder buf = new StringBuilder();
		for (CommandBean b : beans) {

			buf.append("'");
			buf.append(b.getCommand());
			buf.append("'  ");
		}
		return buf.toString();
	}

	public int size() {
		return beans.size();
	}

	public boolean isEmpty() {
		return beans==null||size()<1;
	}
}
