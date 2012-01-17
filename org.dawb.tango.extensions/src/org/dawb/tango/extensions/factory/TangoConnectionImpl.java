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

import org.dawb.common.ui.Activator;
import org.dawb.common.ui.preferences.CommonUIPreferenceConstants;
import org.dawb.tango.extensions.TangoUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoApi.events.ITangoChangeListener;
import fr.esrf.TangoApi.events.TangoChangeEvent;
import fr.esrf.TangoApi.events.TangoEventsAdapter;

class TangoConnectionImpl extends AbstractTangoConnection {

	private static final Logger logger = LoggerFactory.getLogger(TangoConnectionImpl.class);
	
	protected DeviceProxy          tangoProxy;
	protected TangoEventsAdapter   tangoEvents;
	protected ITangoChangeListener listener;
	protected long                 lastEvent=0;
	
	public TangoConnectionImpl(final String uri, String attributeName, boolean requireEvents) throws Exception {
	   
		super(uri, attributeName);
				
		createTangoHost();
		
		this.tangoProxy    = new DeviceProxy(uri);
		
		if (requireEvents) {
		    this.tangoEvents   = new TangoEventsAdapter(tangoProxy);
		    this.listener      = new ITangoChangeListener() {
				@Override
				public void change(TangoChangeEvent event) {
					fireTangoConnectionListeners(event);
					lastEvent = System.currentTimeMillis();
				}
		    };
			if (attributeName!=null) {
			    tangoEvents.addTangoChangeListener(listener, attributeName, new String[0]);
			} else {
				this.attributeName = "Output";
			    tangoEvents.addTangoChangeListener(listener, attributeName, new String[0]);
			}
		}
	}

	/**
	 * If dispose has been called, throws an exception
	 * @param listener
	 */
	public void addTangoConnectionListener(final TangoConnectionListener l) {
		if (tangoEvents==null) throw new RuntimeException("This tango connection, '"+getUri()+"', is not listening to events!");
		super.addTangoConnectionListener(l);
	}
	
	private void createTangoHost() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		final StringBuffer buf = new StringBuffer();
		buf.append(store.getString(CommonUIPreferenceConstants.SERVER_NAME));
		buf.append(":");
		buf.append(store.getInt(CommonUIPreferenceConstants.SERVER_PORT));		    	
		System.setProperty("TANGO_HOST", buf.toString());
	}
	
	/**
	 * Can throw tango exceptions. Once called this connection is dead.
	 * 
	 * 
	 * @throws Exeption
	 */
	public synchronized void dispose() throws Exception {
				
		super.dispose();
				
		if (tangoEvents!=null && listener!=null) {
			tangoEvents.removeTangoChangeListener(listener, attributeName);
		}
		tangoProxy  = null;
		tangoEvents = null;
		listener    = null;
	}
	
	@Override
	public DeviceAttribute getValue() throws Exception {
		if (attributeName==null) throw new NullPointerException("Cannot read attribute "+null);
		return tangoProxy.read_attribute(attributeName);
	}
	
	@Override
	public void setValue(DeviceAttribute value) throws Exception {
		
		if (attributeName==null) throw new NullPointerException("Cannot read attribute "+null);
		tangoProxy.write_attribute(value); // Does not block for spec motors!
		
		// TODO Talk to Andy about blocking until setValue has completed? Spec does not...
		
		// For now we check value
		// FIXME
		// START BODGE WARNING
		
		try {
			Thread.sleep(50); // Fudge incase tango is not blocking properly again.
		} catch (Exception ignored) {
			
		}

		try {
			double current   = getValue().extractDouble();
			double required  = value.extractDouble();
			double tolerance = 0.001; // That does not really work!!
			
			int total = 0;
			while(current>(required+tolerance) || current<(required-tolerance)) {
				
				if (total > 5000) {
					logger.error("TIMEOUT: Cannot set motor value "+getUri()+" synchronously!");
					return;
				}
				try {
					Thread.sleep(200);
				} catch (Exception ne) {
					break;
				}
				total+=200;
				current  = getValue().extractDouble();
			}
		} catch (Exception ignored) {
			return;
		}
		// END BODGE WARNING
	}
	
	/**
	 * Runs command in eclipse job
	 */
	@Override
	public void executeCommand(final String cmdAttribute, final String originalCommand, final boolean async) throws Exception  {
		// Clean command
		String cmd = TangoUtils.getBracketedCommand(originalCommand);
		final DeviceData cmdData = new DeviceData();
		cmdData.insert(new String[]{cmd});

		executeCommand(cmdAttribute, originalCommand, cmdData, async);
	}
	/**
	 * Runs command in eclipse job if async (and returns null) or returns the new value if !async
	 */
	@Override
	public DeviceData executeCommand(final String cmdAttribute, final DeviceData cmdData, final boolean async) throws Exception  {
		return executeCommand(cmdAttribute, cmdData.toString(), cmdData, async);
	}
	
	/**
	 * Runs command in eclipse job
	 */
	private DeviceData executeCommand(final String cmdAttribute, 
									final String originalCommand,
									final DeviceData cmdData, 
									final boolean async) throws Exception  {
		
		
		// Use job for this, it is blocking
		if (async) {
			final Job executeCommand = new Job(originalCommand) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					executeCommand(cmdAttribute, originalCommand, cmdData);
					return Status.OK_STATUS;
				}
	
			};
			executeCommand.setPriority(Job.BUILD);
			executeCommand.schedule();
			return null;
		} else {
			return executeCommand(cmdAttribute, originalCommand, cmdData);
		}
	}
	
	private DeviceData executeCommand(final String cmdAttribute, final String originalCommand, final DeviceData cmdData) {
		
		try {
			final String timeout = System.getProperty("org.dawb.tango.spec.command.timeout");
			tangoProxy.set_timeout_millis(timeout!=null?Integer.parseInt(timeout):Integer.MAX_VALUE);
			DeviceData ret = tangoProxy.command_inout(cmdAttribute, cmdData);
			
			// For fast scans this returns before the spec events have finished being sent
			// over by tango.
			while((System.currentTimeMillis()-lastEvent)<1000) try {
				Thread.sleep(500);// Fudge
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
			TangoConnectionEvent event = new TangoConnectionEvent(TangoConnectionImpl.this, new TangoMockEvent(), "Finished '"+originalCommand+"'");
			event.setFinished(true);
			fireTangoConnectionListeners(event);

			return ret;
			
		} catch (DevFailed e) {
			TangoConnectionEvent event = new TangoConnectionEvent(TangoConnectionImpl.this, new TangoMockEvent(), "Failed '"+originalCommand+"'");
			
			if (e.errors!=null&&e.errors.length>0) {
				event.setErrorMessage(e.errors[e.errors.length-1].desc);
			} else {
				event.setErrorMessage(e.getLocalizedMessage());
			}

			event.setFinished(true);
			fireTangoConnectionListeners(event);
			
			return null;
		}			
	}

	public static void clear() {
		// clear anything cached statically
		
	}

}
