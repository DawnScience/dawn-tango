/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.tango.extensions;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoApi.events.ITangoChangeListener;
import fr.esrf.TangoApi.events.TangoChangeEvent;
import fr.esrf.TangoApi.events.TangoEventsAdapter;

public class MotorTest {

	
	public static void main(String[] args) throws Exception {
		
		                                 
		final DeviceProxy dev = new DeviceProxy("//margaux:20000/id11/motors/phi");
		final DeviceProxy dev2 = new DeviceProxy("//margaux:20000/id11/motors/phi");
		//DeviceProxy dev = new DeviceProxy("//deino:20000/id14-4/motor/kappa");
		
		
		// Test value
		DeviceAttribute val = dev.read_attribute("Position");
		System.out.println("Position of "+dev.get_name()+" is: "+val.extractDouble());
		
		val = dev2.read_attribute("Position");
		System.out.println("Position of "+dev2.get_name()+" is: "+val.extractDouble());
		
		
		TangoEventsAdapter event_supplier = new TangoEventsAdapter(dev);
		ITangoChangeListener     listener = new ITangoChangeListener() {

			@Override
			public void change(TangoChangeEvent event) {
				try {
					// Get attribute and extract value
					DeviceAttribute val = event.getValue();
					System.out.println("Position of "+dev.get_name()+" is: "+val.extractDouble());

				} catch (DevFailed e) {

					// Check if heart beat
					if (e.errors[0].reason.equals("API_EventTimeout"))
						System.out.println(dev + " : API_EventTimeout");
					else
						fr.esrf.TangoDs.Except.print_exception(e);
				}

			} 
		};

		event_supplier.addTangoChangeListener(listener, "Position", new String[0]); 
		
	    event_supplier = new TangoEventsAdapter(dev2);
		listener = new ITangoChangeListener() {

			@Override
			public void change(TangoChangeEvent event) {
				try {
					// Get attribute and extract value
					DeviceAttribute val = event.getValue();
					System.out.println("Position of "+dev.get_name()+" is: "+val.extractDouble());

				} catch (DevFailed e) {

					// Check if heart beat
					if (e.errors[0].reason.equals("API_EventTimeout"))
						System.out.println(dev + " : API_EventTimeout");
					else
						fr.esrf.TangoDs.Except.print_exception(e);
				}

			} 
		};

		event_supplier.addTangoChangeListener(listener, "Position", new String[0]); 
		
//		CallBack callback = new CallBack() {
//			public void push_event(EventData event) {
//
//			      System.out.print("Event at " + new Date(event.date) + ": ");
//			      if (event.err)
//			            Except.print_exception(event.errors);
//			      else
//			      {
//			            DeviceAttribute value = event.attr_value;
//			            if (value.hasFailed())
//			                  Except.print_exception(value.getErrStack());
//			            else
//			            try
//			            {
//			                  if (value.getType()==TangoConst.Tango_DEV_DOUBLE)
//			                        System.out.println(value.extractDouble());
//			            }
//			            catch(DevFailed e)
//			            {
//			                  Except.print_exception(e);
//			            }
//			      }
//
//			}
//		};
//		dev.subscribe_event("Position", TangoConst.CHANGE_EVENT, callback, new String[] {}, true); 

//		while(true) Thread.sleep(100); // We have to kill process to stop, this is only a test
	}
}
