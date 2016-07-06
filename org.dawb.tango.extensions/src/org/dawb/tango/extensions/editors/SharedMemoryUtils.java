/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.tango.extensions.editors;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.dawb.common.ui.util.CalibrationUtils;
import org.dawb.tango.extensions.Activator;
import org.dawb.tango.extensions.TangoUtils;
import org.dawb.tango.extensions.editors.preferences.CalibrationConstants;
import org.dawb.tango.extensions.editors.preferences.SharedConstants;
import org.dawb.tango.extensions.factory.TangoConnection;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.preference.IPreferenceStore;

import fr.esrf.TangoApi.DeviceData;

public class SharedMemoryUtils {

	public static List<Dataset> getSharedMemoryValue(final TangoConnection connection,
						                                     final String          memoryName,
						                                     final PlotType        type) throws Exception { 
		
		final int chunkSize = Activator.getDefault().getPreferenceStore().getInt(SharedConstants.CHUNK_SIZE);
		return SharedMemoryUtils.getSharedMemoryValue(connection, memoryName, chunkSize, type);
	}
	
	public static List<Dataset> getSharedMemoryValue(final TangoConnection connection,
															 final String          memoryName,
															 final int             chunkSize,
															 final PlotType        type) throws Exception {       

		if (connection==null) return null;	
		
		DeviceData  out = new DeviceData();
		out.insert(new String[]{TangoUtils.getSpecName().toLowerCase(), memoryName});
		DeviceData ret = connection.executeCommand("GetArrayInfo", out, false);
        final int[] info = ret.extractLongArray();
        // [rows, cols, type, flag]     type==1 : Double       type==2 : Long
		
		if (type==PlotType.IMAGE) {
			out = new DeviceData();
			out.insert(new String[]{TangoUtils.getSpecName().toLowerCase(),memoryName});
			
			if (info[2]==1) { // Double/Float
				ret = connection.executeCommand(getDoubleArrayCommand(), out, false);
				final double[] data = ret.extractDoubleArray();
				return Arrays.asList(new Dataset[]{DatasetFactory.createFromObject(data, info[0],info[1])});//TODO Size!!
			
			} else { // Long/Int
				ret = connection.executeCommand(getLongArrayCommand(), out, false);
				final int[] data = ret.extractLongArray();
				return Arrays.asList(new Dataset[]{DatasetFactory.createFromObject(data, info[0],info[1])});//TODO Size!!
			}
			
		} else {
			
			final List<Dataset> sets = new ArrayList<Dataset>(chunkSize);
			final String dataTime = DateFormat.getDateTimeInstance().format(new Date());
			for (int i = 0; i < chunkSize; i++) {
				out = new DeviceData();
				out.insert(new String[]{TangoUtils.getSpecName().toLowerCase(),memoryName,String.valueOf(i)});
				
				final Dataset set;
				if (info[2]==1) { // Double/Float
					ret = connection.executeCommand(getDoubleSliceCommand(), out, false);
					final double[] data = ret.extractDoubleArray();
					set = DatasetFactory.createFromObject(data);
				
				} else { // Long/Int
					ret = connection.executeCommand(getLongSliceCommand(), out, false);
					final int[] data = ret.extractLongArray();
					set = DatasetFactory.createFromObject(data);
				}
				
				set.setName(i+" ("+dataTime+")");
				sets.add(set);
			    
			}
			return sets;
		}
	}
	
	
	private static String getLongArrayCommand() {
		final String prop = System.getProperty("org.dawb.tango.extensions.shared.mem.long.array.cmd");
		if (prop==null) return "GetLongArray";
		return prop;
	}
	
	private static String getLongSliceCommand() {
		final String prop = System.getProperty("org.dawb.tango.extensions.shared.mem.long.slice.cmd");
		if (prop==null) return "GetLongRow";
		return prop;
	}

	private static String getDoubleArrayCommand() {
		final String prop = System.getProperty("org.dawb.tango.extensions.shared.mem.double.array.cmd");
		if (prop==null) return "GetDoubleArray";
		return prop;
	}
	
	private static String getDoubleSliceCommand() {
		final String prop = System.getProperty("org.dawb.tango.extensions.shared.mem.double.slice.cmd");
		if (prop==null) return "GetDoubleRow";
		return prop;
	}

	/**
	 * Returns a list of spec names for the current spec session.
	 * @param connection
	 * @return
	 * @throws Exception
	 */
	public static List<String> getSharedNames(TangoConnection connection) throws Exception {
		
		final DeviceData  out = new DeviceData();
		out.insert(TangoUtils.getSpecName().toLowerCase());
		final DeviceData ret = connection.executeCommand("GetArrayList", out, false);
		final List<String> names = Arrays.asList(ret.extractStringArray());
		if (names.isEmpty()) throw new Exception("Cannot get spec names from tango interface, perhaps the spec name of '"+TangoUtils.getSpecName().toLowerCase()+"' is incorrect or the uri of "+connection.getUri()+" is not connecting.");
		return names;
	}


	public static IDataset getXAxis(final Dataset set) throws Exception {
		
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		final boolean        isCalib = store.getBoolean(CalibrationConstants.USE);
		if (!isCalib) {
			return DatasetFactory.createRange(0, set.getSize());
		}
		
        return CalibrationUtils.getCalibrated(set, null, true);
	}


	/**
	 * true if calibration is used.
	 * 
	 * @return
	 */
	public static boolean isCalibrationUsed() {
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		return store.getBoolean(CalibrationConstants.USE);
	}
}
