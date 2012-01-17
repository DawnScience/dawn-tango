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

import java.util.Arrays;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceProxy;


/**
 * Hacked up class for testing shared memory ourside workbench.
 * @author gerring
 * 
 * Changing memory in spec: 
 * 
 * shared long array longtest[1024][1024] 
 * Fill array:
 * for (i=0;i<1024;i++) {for (j=0;j<1024;j++) longtest[i][j]=rand()} 
 * 
 * Constantly change array:
 * while(1) {for (i=0;i<1024;i++) {for (j=0;j<1024;j++) longtest[i][j]=(pow(j+a, 2)*i)}; sleep(1); a=rand()}
 *
 */
public class SharedMemoryTest {

	
	public static void main(String[] args) throws Exception {
		
		// We loop and when IsUpdated is not zero, we dump out the column
		
		final DeviceProxy dev = new DeviceProxy("//margaux:20000/id11/spec/shm");
		
		while(true) {
			
			if (isChanged(dev)) printLongColumn(dev);
			
			Thread.sleep(1000);
		}
	}

	private static boolean isChanged(DeviceProxy dev) throws DevFailed {
		
		final DeviceData  out = new DeviceData();
		out.insert(new String[]{"andy","longtest"});
		
		final DeviceData ret = dev.command_inout("IsUpdated", out);

		return ret.extractLong()==1;
	}

	private static void printLongColumn(DeviceProxy dev) throws DevFailed {

		final DeviceData  out = new DeviceData();
		out.insert(new String[]{"andy","longtest","0"});

		final DeviceData ret = dev.command_inout("GetLongColumn", out);

		System.out.println(Arrays.toString(ret.extractLongArray()));

	}
}
