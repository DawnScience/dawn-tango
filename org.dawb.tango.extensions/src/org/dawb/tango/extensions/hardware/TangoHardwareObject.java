/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.tango.extensions.hardware;

import org.dawb.common.ui.views.monitor.HardwareObject;
import org.dawb.common.ui.views.monitor.HardwareObjectEvent;
import org.dawb.tango.extensions.TangoUtils;
import org.dawb.tango.extensions.factory.TangoConnection;
import org.dawb.tango.extensions.factory.TangoConnectionEvent;
import org.dawb.tango.extensions.factory.TangoConnectionFactory;
import org.dawb.tango.extensions.factory.TangoConnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.TangoApi.DeviceAttribute;

public class TangoHardwareObject extends HardwareObject implements TangoConnectionListener {

	private static final Logger logger = LoggerFactory.getLogger(TangoHardwareObject.class);
	
	private transient TangoConnection tangoConnection;
	
	@Override
	protected void connect() throws Exception {
				
		// e.g. "motors/phi"
		// e.g. "motors/phi:position"
		this.label   = getHardwareName();
		if (hardwareName==null) {
			this.value = "-";
			return;
		}
		
		
		final int    index         = getHardwareName().lastIndexOf(":");
		final String attributeName = index>-1
		                        ? getHardwareName().substring(index+1)
		                        : "Position";
	    final String hardwareStub  = index>-1
		                        ? getHardwareName().substring(0, index)
		                        : getHardwareName();
		
		// e.g. "//lid112:20000/id11/motors/phi"
		final String hardwareAddress = TangoUtils.getHardwareAddress(hardwareStub);     
		try {
			try {
			    this.tangoConnection  = TangoConnectionFactory.openMonitoredConnection(hardwareAddress, attributeName);
				this.label            = tangoConnection.getName();
			    tangoConnection.addTangoConnectionListener(this);
			    
			} catch (fr.esrf.TangoApi.ConnectionFailed  cf) {
				this.value   = cf.errors!=null&&cf.errors.length>0 ? cf.errors[0].desc : cf.getMessage();
				this.tooltip = "Address '"+hardwareAddress+"' does not resolve. Go to 'Tango Preferences' or change name.";
				this.description = tooltip;
				logger.error("Failed to connect to motor "+hardwareAddress, cf);
				return;
				
			} 
		
		} catch (Throwable f) {
			this.value   = f.getMessage();
			this.tooltip = "Address '"+hardwareAddress+"' does not resolve. Go to 'Tango Preferences' or change name.";
			this.description = tooltip;
			logger.error("Failed to connect to motor "+hardwareAddress, f);
			return;
		}
		
		this.description = "Tango motor";

		updateValue(null);
		this.maximum = -20000d;
		this.minimum =  20000d;
		
		
		if (TangoConnectionFactory.isMockMode()) {
			final Thread refresh=new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// We do not mind if sleep fails
					}
					updateValue(null);
					notifyServerObjectListeners(new HardwareObjectEvent(TangoHardwareObject.this));
				}
				
			}, "Refresh "+getHardwareName());
			refresh.setDaemon(true);
			refresh.start();
		}
	}

	/**
	 * 
	 * @param event -  can be null
	 */
	private void updateValue(TangoConnectionEvent event) {
		
		try {
			DeviceAttribute val = event!=null
			                    ? event.getValue()
			                    : tangoConnection.getValue();
			                    
			this.value   = val.extractDouble();
			
		} catch (Throwable ne) {
			this.value   = ne.getMessage();
			logger.trace("Error processing tango event "+event, ne);
		}
	}

	@Override
	protected void disconnect() {
		
		if (tangoConnection==null) return;
		try {
			tangoConnection.removeTangoConnectionListener(this);
			tangoConnection.dispose();
		
		} catch (Exception e) {
			logger.error("Cannot unsubscribe stop tango event listening", e);
		}
		tangoConnection  = null;
	}

	@Override
	public void tangoEventPerformed(TangoConnectionEvent event) {
		updateValue(event);
		notifyServerObjectListeners(new HardwareObjectEvent(this));		
	}

}
