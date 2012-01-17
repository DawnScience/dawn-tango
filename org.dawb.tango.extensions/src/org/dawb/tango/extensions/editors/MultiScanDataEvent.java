/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.tango.extensions.editors;

import java.util.Collection;
import java.util.EventObject;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

public class MultiScanDataEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3884474453471553765L;
	private String                      scanName;
	private Collection<AbstractDataset> data;
	private Object                      userObject;

	public MultiScanDataEvent(Object source, final String scanName, Collection<AbstractDataset> data) {
		super(source);
		this.scanName = scanName;
		this.data     = data;
	}

	public String getScanName() {
		return scanName;
	}

	public void setScanName(String scanName) {
		this.scanName = scanName;
	}

	public Collection<AbstractDataset> getData() {
		return data;
	}

	public void setData(Collection<AbstractDataset> data) {
		this.data = data;
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

}
