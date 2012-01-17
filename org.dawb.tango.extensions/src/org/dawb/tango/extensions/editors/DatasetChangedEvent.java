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

import java.util.EventObject;
import java.util.List;

import org.eclipse.jface.viewers.CheckboxTreeViewer;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

public class DatasetChangedEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4514585546528830846L;
	
	private List<AbstractDataset> selections;

	public DatasetChangedEvent(CheckboxTreeViewer dataViewer,
			List<AbstractDataset> selections) {
		super(dataViewer);
		this.selections = selections;
	}

	public List<AbstractDataset> getSelections() {
		return selections;
	}

	public void setSelections(List<AbstractDataset> selections) {
		this.selections = selections;
	}

}
