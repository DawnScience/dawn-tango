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

import java.util.EventObject;

import fr.esrf.TangoApi.AttrReadEvent;
import fr.esrf.TangoApi.AttrWrittenEvent;
import fr.esrf.TangoApi.CmdDoneEvent;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.events.EventData;
import fr.esrf.TangoApi.events.TangoChangeEvent;

public class TangoConnectionEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4784933927171042199L;
	
	
	private Object          tangoEvent;
	private String          command;
	private boolean         finished = false;
	private DeviceAttribute value;
	private String          errorMessage;

	public TangoConnectionEvent(final TangoConnection source, final DeviceAttribute value) {
		super(source);
		this.value = value;
	}

	public TangoConnectionEvent(final TangoConnection source, final Object evt, final String command) {
		super(source);
		this.tangoEvent = evt;
		this.command    = command;
	}

	public TangoConnectionEvent(final TangoConnection source, final EventObject tangoEvent) {
		super(source);
		this.tangoEvent = tangoEvent;
	}

	public Object getOriginalEvent() {
		return tangoEvent;
	}

	public DeviceAttribute getValue() throws Exception {
		
		if (value!=null) return value;
		if (tangoEvent instanceof TangoMockEvent) {
			return ((TangoMockEvent)tangoEvent).getValue();
		}
		if (tangoEvent instanceof TangoChangeEvent) {
			return ((TangoChangeEvent)tangoEvent).getValue();
		}
		if (tangoEvent instanceof EventData) {
			return ((EventData)tangoEvent).attr_value;
		}
		return null;
	}
	
	public DeviceAttribute[] getValues() throws Exception {
		
		if (value!=null) return new DeviceAttribute[]{value};

		if (tangoEvent instanceof TangoMockEvent) {
			return ((TangoMockEvent)tangoEvent).getValues();
		}
		if (tangoEvent instanceof AttrReadEvent) {
			return ((AttrReadEvent)tangoEvent).argout;
		}
		if (tangoEvent instanceof AttrWrittenEvent) {
			return null;
		}
		return null;
	}
	
	public boolean isFinishedEvent() {
		if (finished) return true;
		return tangoEvent instanceof CmdDoneEvent;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public String getCommand() {
		return command;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
