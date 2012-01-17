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

import org.dawb.common.services.IHardwareService;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

import fr.esrf.TangoApi.DeviceAttribute;

public class TangoHardwareService extends AbstractServiceFactory implements IHardwareService {

	@Override
	public Object getValue(String hardwareUri) throws Exception {
		
		final TangoConnection connection = TangoConnectionFactory.openConnection(hardwareUri, null);
		return connection.getValue();
	}

	@Override
	public void setValue(String hardwareUri, Object value)  throws Exception {
		final TangoConnection connection = TangoConnectionFactory.openConnection(hardwareUri, null);
        connection.setValue((DeviceAttribute)value);
	}

	@Override
	public Object getMockValue(String name) {
		return TangoConnectionFactory.getMockValue(name);
	}

	@Override
	public void setMockValue(String name, Object value) {
		TangoConnectionFactory.setMockValue(name, value);
	}

	@Override
	public void notifyMockCommand(String motorName, String message, String value) {
		TangoConnectionFactory.notifyMockCommand(motorName, message, value);
	}

	@Override
	public Object create(Class serviceInterface,
			             IServiceLocator parentLocator,
			             IServiceLocator locator) {

        if (serviceInterface==IHardwareService.class) {
        	return new TangoHardwareService();
        }
        return null;
	}

}
