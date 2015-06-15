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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

import javax.management.MBeanServerConnection;

import org.dawb.tango.extensions.TangoUtils;
import org.dawb.workbench.jmx.RemoteWorkbenchAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceData;

class MockTangoConnection extends AbstractTangoConnection {

	private static final Logger logger = LoggerFactory.getLogger(MockTangoConnection.class);
	
	// TODO These are memory leaks but it's only mock mode.
	// [motorName->Value]
	private static Map<String,Object>                   mockValues;
	
	/**
	 * We break lazy initiation because this hashtable
	 * is not a serious iniefficiency and it makes it easier
	 * to deal with unexpected usages.
	 */
	static {
		mockValues = new ConcurrentHashMap<String, Object>(17);
	}
	
	// [motorName->AbstractTangoConnection]
	private static Map<String,AbstractTangoConnection>  mockListeners;
	
	// [command->IMockCommand]
	private static Map<String,IMockCommand>             commandMap;
	
	private String hardwareName;
	
	protected MockTangoConnection(String attributeName) {
		this(attributeName, null);
	}

	public MockTangoConnection(String hardwareURI, String attributeName) {
	
		super(hardwareURI, attributeName);
		if (commandMap==null) createCommandMap();
		
		this.hardwareName = hardwareURI.substring(hardwareURI.lastIndexOf("/")+1);
		
		if (attributeName!=null) {
			if (mockListeners==null) mockListeners = new ConcurrentHashMap<String, AbstractTangoConnection>();
			mockListeners.put(getHardwareName(), this); // Purposely only one, avoid memory leaks more.
			
			if (mockValues.get(getHardwareName())==null) {
				mockValues.put(getHardwareName(), 0d);
			}
		} 
	}
	/**
	 * Fire with current value. Only used for Mock mode
	 * @throws Exception 
	 */
	public void fireTangoConnectionListeners() throws Exception {
		final TangoConnectionEvent evt = new TangoConnectionEvent(this, getValue());
		fireTangoConnectionListeners(evt);
	}

	@Override
	public DeviceAttribute getValue() throws Exception {
		if (!mockValues.containsKey(getHardwareName())) {
			setValue(getHardwareName(), new Double(0)); // Sends over if required
		}
		return new DeviceAttribute(attributeName, ((Number)getRemoteValue()).doubleValue());
	}
	
	private Object getRemoteValue() throws Exception {
		if (TangoConnectionFactory.isMockMode()) {
			// We also send this back to the workbench.
			final MBeanServerConnection client     = getRemoteClient();			
			return client.invoke(RemoteWorkbenchAgent.REMOTE_WORKBENCH, "getMockMotorValue", new Object[]{getName()}, new String[]{String.class.getName()});
		} else {
			throw new Exception("Cannot set Mock Values when not in mock mode!");
		}
	}

	private static MBeanServerConnection remoteClientConnection;
	
	private MBeanServerConnection getRemoteClient() throws Exception {
		if (remoteClientConnection==null) remoteClientConnection = RemoteWorkbenchAgent.getInstance().getServerConnection(1000);
		return remoteClientConnection;
	}

	@Override
	public void setValue(DeviceAttribute value) throws Exception {
		setValue(getHardwareName(), value.extractDouble());
	}

	/**
	 * A few spec command, but not all are supported.
	 * TODO Fix to have command value.
	 */
	@Override
	public synchronized DeviceData executeCommand(final String unused, final DeviceData cmd, final boolean async) throws Exception {
		executeCommand(unused, cmd, async);
		return new DeviceData();
	}	
	/**
	 * A few spec command, but not all are supported.
	 * TODO Fix to have genuine mock mode
	 */
	@Override
	public synchronized void executeCommand(final String unused, final String cmd, final boolean async) throws Exception {
		
        if (async) {
	       	Thread thread=new Thread(new Runnable() {
	       		@Override
	       		public void run() {
	       			try {
						executeCommand(cmd);
					} catch (Exception e) {
						logger.error("Problem running command "+cmd, e);
					}
				}
	       	}, "Mock Command '"+cmd+"'");
	       	thread.start();
        } else {
        	executeCommand(cmd);
        }
	}
	private void executeCommand(String cmd) throws Exception {
		
		final Matcher matcher = TangoUtils.getBracketedMatcher(cmd);
		if (matcher==null) {
			notifyCommand("Command '"+cmd+"' not implemented in mock mode.", cmd);
			notifyEndCommand();
			return;
		}

		final String command = matcher.group(1);
		if (commandMap.containsKey(command)) {
			commandMap.get(command).processCommand(matcher, MockTangoConnection.this);

		} else {
			notifyCommand("Command '"+cmd+"' not implemented in mock mode.", cmd);
			notifyEndCommand();
		};
	}


	private interface IMockCommand {
		void processCommand(Matcher matcher, MockTangoConnection connection) throws Exception;
	}
	
	private static void createCommandMap() {
		commandMap = new ConcurrentHashMap<String, IMockCommand>();
		commandMap.put("mv",    new MVMockCommand());
		commandMap.put("ascan", new ScanMockCommand());
	}
	
	public static final class MVMockCommand implements IMockCommand {

		@Override
		public void processCommand(final Matcher             matcher,
                                   final MockTangoConnection connection) throws Exception {
			
			connection.setValue(matcher.group(2), Double.parseDouble(matcher.group(3)));
			connection.notifyCommand(matcher.group(2) + " at " +matcher.group(3), matcher.group(0));
			connection.notifyEndCommand();
		}

	}
	public static final class ScanMockCommand implements IMockCommand {

		private NumberFormat format = new DecimalFormat("######0.0000");
		@Override
		public void processCommand(final Matcher             matcher,
				                   final MockTangoConnection connection) throws Exception {
			
			if (!matcher.matches()) {
				connection.notifyCommand("Command '"+matcher.group(0)+"' not implemented in mock mode.", matcher.group(0));
				connection.notifyEndCommand();
				return;
			}
			
			final double start = getDouble(matcher, 3);
			final double end   = getDouble(matcher, 5);
			final double interv= getDouble(matcher, 7);
			final double time  = getDouble(matcher, 9);
			
			final double step  = (end-start)/interv;
			connection.notifyCommand("  #\t"+matcher.group(2)+"\tDetector\tMonitor\tSeconds\tFlux I0", matcher.group(0));
			
			int index = 0;
			for (double i = start; i <= end; i+=step) {
				
				long startTime = System.currentTimeMillis();
				try {
					Thread.sleep(Math.round(time*1000d));
				} catch (InterruptedException ignored) {
					// continue
				}
				long endTime = System.currentTimeMillis();
				double timeMs  = (endTime-startTime)/1000d;
				
				connection.setValue(matcher.group(2), i);
				connection.notifyCommand("  "+index+"\t"+format.format(i)+"\t0\t\t0\t"+format.format(timeMs)+"\t0", matcher.group(0));

				index++;
			}
			
			connection.notifyEndCommand();
		}
		
		private double getDouble(Matcher matcher, int i) {
			try {
			    return Double.parseDouble(matcher.group(i));
		    } catch (Exception ne) {
		    	return Long.parseLong(matcher.group(i));
		    }
		}

	}

	
	private void notifyEndCommand() throws Exception {
		notifyCommand(null, null);		
	}

	private void notifyCommand(final String message, final String cmd) throws Exception {
		if (TangoConnectionFactory.isMockMode()) {
			// We also send this back to the workbench.
			final MBeanServerConnection client     = getRemoteClient();					
			client.invoke(RemoteWorkbenchAgent.REMOTE_WORKBENCH, "notifyMockCommand", new Object[]{getName(), message, cmd}, new String[]{String.class.getName(),String.class.getName(),String.class.getName()});
			
			// We also set this in this VM
			if (System.getProperty("eclipse.debug.session")==null) TangoConnectionFactory.notifyMockCommand(getName(), message, cmd);
		} else {
			throw new Exception("Cannot set Mock Values when not in mock mode!");
		}
		
	}
	
	private void setValue(String name, Object value) throws Exception {
		
		if (TangoConnectionFactory.isMockMode()) {
			// We also send this back to the workbench.
			final MBeanServerConnection client     = getRemoteClient();					
			client.invoke(RemoteWorkbenchAgent.REMOTE_WORKBENCH, "setMockMotorValue", new Object[]{name, value}, new String[]{String.class.getName(), Object.class.getName()});
			
			// We also set this in this VM
			if (System.getProperty("eclipse.debug.session")==null) TangoConnectionFactory.setMockValue(name, value);
		} else {
			throw new Exception("Cannot set Mock Values when not in mock mode!");
		}
	}
	

	private String getHardwareName() {
		return hardwareName;
	}

	public static void putMockValue(String name, Object value) {
		if (mockValues==null) return;
		mockValues.put(name, value);
	}

	public static boolean isMockMotor(String name) {
		if (mockListeners==null) return false;
		return mockListeners.containsKey(name);
	}

	public static AbstractTangoConnection getMockMotor(String name) {
		if (mockListeners==null) return null;
		return mockListeners.get(name);
	}

	public static Object getMockValue(String name) {
		if (!mockValues.containsKey(name)) {
			mockValues.put(name, new Double(0));
		}
		return mockValues.get(name);
	}

}
