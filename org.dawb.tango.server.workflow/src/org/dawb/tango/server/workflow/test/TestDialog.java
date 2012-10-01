/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.tango.server.workflow.test;

/**
 *   Example of a client using the TANGO Api
 */
import org.dawb.workbench.jmx.service.WorkflowFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import WorkflowExecutor.WorkflowExecutor;
import WorkflowExecutor.WorkflowExecutor.TangoServerServiceProvider;
import fr.esrf.Tango.*;
import fr.esrf.TangoDs.*;
import fr.esrf.TangoApi.*;
   
public class TestDialog
{
	private static final Logger logger = LoggerFactory.getLogger(WorkflowExecutor.class);
	public String availableWorkflows = null;
	public static DeviceProxy dev = null;
	
	public void testSuite() throws DevFailed, InterruptedException {
		checkOnState();
		getAvailableWorflows();
		startTestDialog();
	}
	
	private void startTestDialog() throws DevFailed {
		String[] arg0 = {"modelpath","common/tests/test_dialogs.moml"};
		DeviceData argin = new DeviceData();
		argin.insert(arg0);
    	logger.debug("Starting test_dialogs workflow");
        DeviceData argout = dev.command_inout("Start", argin);
    	logger.debug("Workflow started: "+argout);
        waitForState(DevState.OPEN, 100000);
		argin = new DeviceData();
		argin.insert(new String[]{"x","10"});
		argout = dev.command_inout("SetScalarValuesMap", argin);
        waitForState(DevState.OPEN, 100000);
		argin = new DeviceData();
		argin.insert(new String[]{"x","10"});
		argout = dev.command_inout("SetScalarValuesMap", argin);
        waitForState(DevState.OPEN, 100000);
		argin = new DeviceData();
		argin.insert(new String[]{"x","10"});
		argout = dev.command_inout("SetScalarValuesMap", argin);
        waitForState(DevState.ON, 100000);
        
	}

	
	private String[] makeDataExchange(String[] inputScalarValues, int timeOut) throws DevFailed {
		final ExchangeData exchangeData = new ExchangeData();
		exchangeData.setInScalarValues(inputScalarValues);
		waitForState(DevState.OPEN, timeOut);
		logger.debug("Server in OPEN state");
		//Thread.sleep(5000);
		// Get review data
		DeviceData argoutData = dev.command_inout("GetScalarValuesMap");
		exchangeData.setOutScalarValues(argoutData.extractStringArray());
		// Set review data
		//Thread.sleep(1000);
		DeviceData argin = new DeviceData();
		argin.insert(exchangeData.getInScalarValues());
		argoutData = dev.command_inout("SetScalarValuesMap", argin);

		return exchangeData.getOutScalarValues();
	}


	private void waitForState(final DevState desiredState, final int timeOut) {

		final Thread waitThread = new Thread(new Runnable() {
			@Override
			public void run() {				
				try {
					// Wait for state
			        DeviceAttribute stateAttribute = dev.read_attribute("StateAttribute");
			        DevState  receivedState = stateAttribute.extractDevState();
		        	logger.debug("Waiting for state");
			        while (!receivedState.equals(desiredState)) {
			        	Thread.sleep(1000);
			        	stateAttribute = dev.read_attribute("StateAttribute");
				        receivedState = stateAttribute.extractDevState();
			        	logger.debug("Still waiting state");
			        }
		        	logger.debug("Server in desired state");
				} catch (Exception ne) {
					logger.error("Exception in exchangeData", ne);
					ne.printStackTrace();
				}
			}

		});
	
		// Start thread
		waitThread.start();
		// Wait till timeout
		try {
			waitThread.join(timeOut);
			if (waitThread.isAlive()) {
				logger.error("Time-out while waiting for exchangeData to finish!");
				waitThread.interrupt();
			}
		} catch (InterruptedException e) {
			logger.error("InterruptedException while waiting for exchangeData to finish!");
			e.printStackTrace();
		}
	}

	
	public void checkOnState() throws DevFailed {
        // Check that the server is in the ON state
        DeviceData argoutState = dev.command_inout("State");
        DevState  receivedState = argoutState.extractDevState();
        if (!receivedState.equals(DevState.ON)) {
        	logger.error("Error! Server on in ON state.");
        	System.exit(1);
        }
        logger.info("Server in ON state.");
	}

	public void getAvailableWorflows() throws DevFailed {
		DeviceData argout = dev.command_inout("GetAvailableWorkflows");
		this.availableWorkflows = argout.extractString();
		logger.info("Available workflows:");
		logger.info(this.availableWorkflows);
	}
	
	public static void main (String args[])
	{
		final TestDialog testAbort = new TestDialog();
		// Check that we have an argument
		if (args.length < 1) {
			logger.info("Usage: TestRunWorkflow device (e.g. id14he2/workflow/1)");
			System.exit(1);
		}
	    try
	    {
	    	// Connect to the device. The first argument must be the device name
	        dev = new DeviceProxy(args[0]);
	        // Run the server test suite
	        testAbort.testSuite();
		} catch (Exception e) {
			Except.print_exception(e);
			System.exit(1);
		}
	}
}

//final class ExchangeData {
//	private String[] inScalarValues = null;
//	private String[] outScalarValues = null;
//	public String[] getInScalarValues() {
//		return inScalarValues;
//	}
//	public void setInScalarValues(String[] inScalarValues) {
//		this.inScalarValues = inScalarValues;
//	}
//	public String[] getOutScalarValues() {
//		return outScalarValues;
//	}
//	public void setOutScalarValues(String[] outScalarValues) {
//		this.outScalarValues = outScalarValues;
//	}
//}
