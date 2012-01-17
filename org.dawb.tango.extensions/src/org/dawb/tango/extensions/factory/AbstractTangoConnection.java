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

import java.util.Collection;
import java.util.EventObject;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractTangoConnection implements TangoConnection {
	
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractTangoConnection.class);

	private   Collection<TangoConnectionListener> listeners;
	protected String                              attributeName;
	protected String                              hardwareUri;	
	private   int                  connectionCount;

	/**
	 * Attribute name may be null
	 * @param attributeName
	 */
	protected AbstractTangoConnection(final String uri, String attributeName) {
		// Tango events can come on many threads and frequently
		this.attributeName = attributeName;
		this.hardwareUri   = uri;
		this.connectionCount = 1;
	}
	
	/**
	 * If dispose has been called, throws an exception
	 * @param listener
	 */
	public void addTangoConnectionListener(final TangoConnectionListener listener) {
		if (listeners==null) listeners = new ConcurrentLinkedQueue<TangoConnectionListener>();
		listeners.add(listener);
	}
	
	/**
	 * dispose automatically does this for all connections.
	 * @param listener
	 */
	public void removeTangoConnectionListener(final TangoConnectionListener listener) {
		if (listeners==null) return;
		listeners.remove(listener);
	}

	/**
	 * Does nothing if dispose has been called.
	 * @param tangoEvent
	 */
	protected boolean fireTangoConnectionListeners(final TangoConnectionEvent event) {
		if (listeners == null)   return false;
		if (listeners.isEmpty()) return false;
		for (TangoConnectionListener l : listeners) l.tangoEventPerformed(event);
		return true;
	}
	
	/**
	 * Does nothing if dispose has been called.
	 * @param tangoEvent
	 */
	protected void fireTangoConnectionListeners(final EventObject tangoEvent) {
		if (listeners == null) return;
		final TangoConnectionEvent event = new TangoConnectionEvent(this, tangoEvent);
		for (TangoConnectionListener l : listeners) l.tangoEventPerformed(event);
	}
	
	protected void fireTangoConnectionListeners(Object realTangoEvent, final String command) {
		if (listeners == null) return;
		try {
			final TangoConnectionEvent event = new TangoConnectionEvent(this, realTangoEvent, command);
			fireTangoConnectionListeners(event);
		} catch (Exception e) {
			logger.error("Cannot fire listeners!", e);
		}
	}


	public void dispose() throws Exception {
		if (listeners!=null) {
			listeners.clear();
			listeners = null;
		}
		if (connectionCount>1) {
			connectionCount--;
			return;
		}
		TangoConnectionFactory.clearConnection(this);
	}
	
	public String getUri() {
		return hardwareUri;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getName() {
		return getUri().substring(getUri().lastIndexOf("/")+1);
	}

	public String getAttributeName() {
		return attributeName;
	}


	/**
	 * Override to 
	 */

	public void incrementCount() {
		connectionCount++;
	}

}
