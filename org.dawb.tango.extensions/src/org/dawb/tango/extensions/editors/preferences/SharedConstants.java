/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.tango.extensions.editors.preferences;

public class SharedConstants {
	
	/**
	 * true/false for monitoring on
	 */
	public static final String SHARED_MON = "org.dawb.tango.extensions.shared.mem.monitoring.default.on";

	/**
	 * Frequency of monitoring the memory to see if it has changed, ms
	 */
	public static final String MON_FREQ = "org.dawb.tango.extensions.shared.mem.monitoring.frequency";

	/**
	 * The shared url key, by default "spec/shm", user should not need to change too much.
	 */
	public static final String SPEC_SHARED = "org.dawb.tango.extensions.shared.mem.uri";

	/**
	 * Records wether the user last had the shared memory monitor in 1D or 2D mode
	 */
	public static final String IMAGE_MODE = "org.dawb.tango.extensions.shared.mem.image.mode";
	
	/**
	 * Records the history preferences
	 */
	public static final String HISTORY_MODE = "org.dawb.tango.extensions.shared.mem.history.mode";

	/**
	 * The row count to plot when monitoring scalar things
	 */
	public static final String CHUNK_SIZE = "org.dawb.tango.extensions.shared.mem.chunk.size";
	
	/**
	 * The history when in history mode
	 */
	public static final String HISTORY_SIZE = "org.dawb.tango.extensions.shared.mem.history.size";

	/**
	 * The index of the chunk to add to history
	 */
	public static final String CHUNK_INDEX = "org.dawb.tango.extensions.shared.mem.chunk.index";

}
