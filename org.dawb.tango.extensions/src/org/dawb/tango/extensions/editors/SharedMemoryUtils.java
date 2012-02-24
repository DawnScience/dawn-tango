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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.util.ExpressionFactory;
import org.dawb.common.util.IExpressionEvaluator;
import org.dawb.tango.extensions.Activator;
import org.dawb.tango.extensions.TangoUtils;
import org.dawb.tango.extensions.editors.preferences.CalibrationConstants;
import org.dawb.tango.extensions.editors.preferences.SharedConstants;
import org.dawb.tango.extensions.factory.TangoConnection;
import org.eclipse.jface.preference.IPreferenceStore;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import fr.esrf.TangoApi.DeviceData;

public class SharedMemoryUtils {

	public static List<AbstractDataset> getSharedMemoryValue(final TangoConnection connection,
						                                     final String          memoryName,
						                                     final PlotType        type) throws Exception { 
		
		final int chunkSize = Activator.getDefault().getPreferenceStore().getInt(SharedConstants.CHUNK_SIZE);
		return SharedMemoryUtils.getSharedMemoryValue(connection, memoryName, chunkSize, type);
	}
	
	public static List<AbstractDataset> getSharedMemoryValue(final TangoConnection connection,
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
				return Arrays.asList(new AbstractDataset[]{new DoubleDataset(data, new int[]{info[0],info[1]})});//TODO Size!!
			
			} else { // Long/Int
				ret = connection.executeCommand(getLongArrayCommand(), out, false);
				final int[] data = ret.extractLongArray();
				return Arrays.asList(new AbstractDataset[]{new IntegerDataset(data, new int[]{info[0],info[1]})});//TODO Size!!
			}
			
		} else {
			
			final List<AbstractDataset> sets = new ArrayList<AbstractDataset>(chunkSize);
			final String dataTime = DateFormat.getDateTimeInstance().format(new Date());
			for (int i = 0; i < chunkSize; i++) {
				out = new DeviceData();
				out.insert(new String[]{TangoUtils.getSpecName().toLowerCase(),memoryName,String.valueOf(i)});
				
				final AbstractDataset set;
				if (info[2]==1) { // Double/Float
					ret = connection.executeCommand(getDoubleSliceCommand(), out, false);
					final double[] data = ret.extractDoubleArray();
					set = new DoubleDataset(data,data.length);
				
				} else { // Long/Int
					ret = connection.executeCommand(getLongSliceCommand(), out, false);
					final int[] data = ret.extractLongArray();
					set = new IntegerDataset(data,data.length);
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


	public static IDataset getXAxis(final AbstractDataset set) throws Exception {
		
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		final boolean        isCalib = store.getBoolean(CalibrationConstants.USE);
		if (!isCalib) {
			return AbstractDataset.arange(0, set.getSize());
		}
		
        return getCalibrated(set, null, true);
	}

	/**
	 * Returns the calibrated abstract data set if required, otherwise returns set
	 * unchanged.
	 * 
	 * @param set
	 * @param scalar optionally specify some extra scalar values for the calibration expression.
	 * @return
	 * @throws Exception
	 */
	public static IDataset getCalibrated(final IDataset set, final Map<String,String> scalar, final boolean checkEnabled) throws Exception {
		
		
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		if (checkEnabled) {
			final boolean        isCalib = store.getBoolean(CalibrationConstants.USE);
			if (!isCalib)  return AbstractDataset.arange(0, set.getSize(), 1, AbstractDataset.INT32);
		}
		
     	final String expr = store.getString(CalibrationConstants.EXPR);
		final String name = store.getString(CalibrationConstants.LABEL);
		final double a    = store.getDouble(CalibrationConstants.A);
		final double b    = store.getDouble(CalibrationConstants.B);
		final double c    = store.getDouble(CalibrationConstants.C);
		final double d    = store.getDouble(CalibrationConstants.D);
		
		
		final double[] calib = new double[set.getSize()];
		
		// TODO FIXME - What is the definition of p and p0??
		final double p0 = 0;
	
		final IExpressionEvaluator eval = ExpressionFactory.createExpressionEvaluator();
		eval.setExpression(expr);
		
		final Map<String,Object> vals = new HashMap<String,Object>(7);
		for (int i = 0; i < calib.length; i++) {
			
			vals.clear();
			if (scalar!=null) vals.putAll(scalar);
			vals.put("p",  i);
			vals.put("p0", p0);
			vals.put("a",  a);
			vals.put("b",  b);
			vals.put("c",  c);
			vals.put("d",  d);
			
			calib[i] = eval.evaluate(vals);
		}
		
		DoubleDataset ret = new DoubleDataset(calib, calib.length);
		ret.setName(name);
		return ret;	
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
