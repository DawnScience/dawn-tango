/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.tango.extensions.factory;

import fr.esrf.TangoApi.DeviceAttribute;


public class TangoMockEvent  {

	private String message;
	
	public TangoMockEvent() {
		this("");
	}
	public TangoMockEvent(final String message) {
		this.message = message;
	}

	public DeviceAttribute getValue() {
		return new DeviceAttribute("Mock Message", message);
	}
	public DeviceAttribute[] getValues() {
		return new DeviceAttribute[]{getValue()};
	}
}
