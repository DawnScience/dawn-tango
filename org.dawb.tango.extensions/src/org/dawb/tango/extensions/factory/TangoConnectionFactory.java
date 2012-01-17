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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dawb.common.ui.preferences.CommonUIPreferenceConstants;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceProxy;


/**
 * This class is designed to give:
 * 1. a dummy connection or a tango one.
 * 2. deal with the fact that you can only have one attribute class listening to
 *    a connection and parameter combination at a time.
 * 
 * This allows a dummy mode to be used which is useful for testing as
 * no spec motors will be moved by the tango service.
 * 
 * @author gerring
 *
 */
public class TangoConnectionFactory {

	private static Logger logger = LoggerFactory.getLogger(TangoConnectionFactory.class);

	/**
	 * Get a connection to tango, this many also be a dummy connection
	 * if the system is in dummy mode.
	 * 
	 * The connection attaches no listener you call still call dispose() 
	 * when the connection is finished with.

	 * @param hardwareURI, e.g. "//margaux:20000/id11/motors/phi" or "//margaux:20000/id11/spec/shm"
	 * @param attributeName
	 * @return
	 */
	public static TangoConnection openConnection(final String hardwareURI, final String attributeName) throws Exception {

		return getTangoConnection(hardwareURI, attributeName, false);
	}

	/**
	 * Get a connection to tango, this many also be a dummy connection
	 * if the system is in dummy mode.
	 * 
	 * The connection attaches a listener and the dispose() method *must* be
	 * called when the connection is finished with.

	 * @param hardwareURI, e.g. "//margaux:20000/id11/motors/phi" or "//margaux:20000/id11/spec/shm"
	 * @param attributeName
	 * @return
	 */
	public static TangoConnection openMonitoredConnection(final String hardwareURI, 
			                                              final String attributeName) throws Exception {
		
		return getTangoConnection(hardwareURI, attributeName, true);
	}
	/**
	 * This connection connects to a DeviceProxy and sends commands to the
	 * string of the connection. It is used for Spec connections where commands are
	 * run and notified.
	 * 
	 * Not listening to any attribute
	 *  
	 * @param hardwareURI, e.g. "//margaux:20000/id11/motors/phi" or "//margaux:20000/id11/spec/shm"
	 * @return
	 * @throws Exception
	 */
	public static TangoConnection openCommandConnection(final String hardwareURI) throws Exception {
		
		return TangoConnectionFactory.getTangoConnection(hardwareURI, "Output", false);
	}

	/**
	 * This connection connects to a DeviceProxy and sends commands to the "ExecuteCmd"
	 * string of the connection. It is used for Spec connections where commands are
	 * run and notified.
	 *  
	 * @param hardwareURI, e.g. "//margaux:20000/id11/motors/phi" or "//margaux:20000/id11/spec/shm"
	 * @param outputAttribute
	 * @return
	 * @throws Exception
	 */
	public static TangoConnection openMonitoredCommandConnection(final String hardwareURI, final String outputAttribute) throws Exception {
		
		return TangoConnectionFactory.getTangoConnection(hardwareURI, outputAttribute, true);
	}
	
	private static Map<String,AbstractTangoConnection> cachedTangoConnections;
	private static Object                               LOCK=new Object();
	
	private static TangoConnection getTangoConnection(final String  hardwareURI,
													  final String  attributeName,
													  final boolean requireEvents) throws Exception {
		final boolean isDummy = isMockMode();

		if (!requireEvents && !isDummy) return new TangoConnectionImpl(hardwareURI, attributeName, false);
		
		//return new MonitoredTangoConnection(hardwareURI, attributeName, true);
        if (cachedTangoConnections==null) cachedTangoConnections = new ConcurrentHashMap<String, AbstractTangoConnection>(7);
		
        synchronized (LOCK) {
       	
    		AbstractTangoConnection connection;
            final String key = hardwareURI+":"+attributeName;
            
            if (cachedTangoConnections.containsKey(key)) {
            	connection = cachedTangoConnections.get(key);
            	connection.incrementCount();
            	return connection;
            }

            connection = isDummy
                       ? new MockTangoConnection(hardwareURI, attributeName)
                       : new TangoConnectionImpl(hardwareURI, attributeName, true);
        	cachedTangoConnections.put(key, connection);
        	
        	return connection;
        }
 	}
	
	protected static void clearConnection(final AbstractTangoConnection connection) {
		
	    if (cachedTangoConnections==null) return;
	    final String key = connection.getUri()+":"+connection.getAttributeName();
        cachedTangoConnections.remove(key);
		
	}

	/**
	 * Call to clear any cahed connections.
	 */
	public static void clear() {
		TangoConnectionImpl.clear();
		cachedTangoConnections.clear();
	}

	/**
	 * Use this method to use the correct classloader when making a DeviceProxy or
	 * Tango will break. Do not use new DeviceProxy(...) outside this plugin or else the
	 * Corba classloading will be broken.
	 * 
	 * @param tangoURL
	 * @return
	 * @throws Exception 
	 */
	public static DeviceProxy openDirectConnection(String tangoURL) throws Exception {
		return new DeviceProxy(tangoURL);
	}

	public static boolean isMockMode() {
		// Default: if not previously set mock mode should be true (DAWB-329)
		// Note that the default value for MOCK_SESSION is also set
		// in org.dawb.common.ui.preferences.CommonUIPreferenceInitializer
		boolean returnValue = true;
		String debugProvenance = "by default";
		if (System.getProperty("org.dawb.test.session")==null) {
			final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawb.common.ui");
	   		if (store.contains(CommonUIPreferenceConstants.MOCK_SESSION)) {
	   			returnValue = store.getBoolean(CommonUIPreferenceConstants.MOCK_SESSION);
	   			debugProvenance = "from workspace store (.metadata)";
	   		} 
		} else {
			debugProvenance = "from system property org.dawb.test.session";
			// Only set mock mode to false if the string "false" is given
			if (System.getProperty("org.dawb.test.session").equals("false")) {
				returnValue = false;
			} 
		}
		logger.debug("TANGO Spec Mock Mode set to "+returnValue+" "+debugProvenance);
		return returnValue;
	}
	
	public static void setMockMode(boolean tangoMockModeNew) {
		System.setProperty("org.dawb.test.session", new Boolean(tangoMockModeNew).toString());
		final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawb.common.ui");
   		store.setValue(CommonUIPreferenceConstants.MOCK_SESSION, tangoMockModeNew);
	}

	public static Object getMockValue(String name) {
		return MockTangoConnection.getMockValue(name);
	}
	
	public static void setMockValue(String name, final Object value) {
		
		name = name.trim();
		MockTangoConnection.putMockValue(name, value);
		if (MockTangoConnection.isMockMotor(name)) {
			
			final DeviceAttribute dv;
			if (value instanceof Number) {
				dv = new DeviceAttribute("Position", ((Number)value).doubleValue());
			} else {
				dv = new DeviceAttribute("Position", value.toString());
			}
			final AbstractTangoConnection connection = MockTangoConnection.getMockMotor(name);
			final TangoConnectionEvent event = new TangoConnectionEvent(connection, dv);
			connection.fireTangoConnectionListeners(event);
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// Ignored, sleep just to make it feel real
		}

	}

	
	public static void notifyMockCommand(String name, final String message, final String cmd) {
		
		name = name.trim();
		if (MockTangoConnection.isMockMotor(name)) {
			
			final AbstractTangoConnection connection = MockTangoConnection.getMockMotor(name);
			if (message==null && cmd==null) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				final TangoConnectionEvent event = new TangoConnectionEvent(connection, new TangoMockEvent(), "End");
				event.setFinished(true);
				connection.fireTangoConnectionListeners(event);

			} else {
			    connection.fireTangoConnectionListeners(new TangoMockEvent(message), cmd);
			}
		}
	}

}
