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
import fr.esrf.TangoApi.DeviceData;

/**
 * Wraps a connection which may be a dummy!
 * @author gerring
 *
 */
public interface TangoConnection {

	/**
	 * Because tango sometimes caches connections - dispose is *NOT* garanteed
	 * to kill this connection. If there are more than one connections to the 
	 * motor.
	 * 
	 * Therefore you must always call removeTangoConnectionListener(...) which will 
	 * and then dispose() which will dispose if you are the last user.
	 * 
	 * This is a restriction of tango.
	 * 
	 * @throws Exception
	 */
	public void dispose() throws Exception;

	/**
	 * Returns a Tango attribute for the value
	 * @return
	 */
	public DeviceAttribute getValue()  throws Exception;
	
	/**
	 * Returns a Tango attribute for the value
	 * @return
	 */
	public void setValue(DeviceAttribute value)  throws Exception;

	/**
	 * If dispose has been called, throws an exception
	 * @param listener
	 */
	public void addTangoConnectionListener(final TangoConnectionListener listener);
	
	/**
	 * dispose automatically does this for all connections.
	 * @param listener
	 */
	public void removeTangoConnectionListener(final TangoConnectionListener listener);

	/**
	 * Send a command to a spec session.
	 * @param cmd
	 */
	public void executeCommand(final String commandAttribute, final String cmd, final boolean async)  throws Exception;
	
	/**
	 * Send a command to a spec session. NOTE: Returns null if the async is true.
	 * @param cmd
	 */
	public DeviceData executeCommand(final String commandAttribute, final DeviceData data, final boolean async)  throws Exception;

	/**
	 * Return the tango uri for this tango connection
	 * @return
	 */
	public String getUri();

	/**
	 * 
	 * @return
	 */
	public String getName();
	
	/**
	 * May be null
	 * @return
	 */
	public String getAttributeName();

}
