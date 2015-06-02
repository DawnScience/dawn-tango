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

import java.util.Hashtable;

import org.dawb.common.services.IHardwareService;
import org.dawb.tango.extensions.factory.TangoHardwareService;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	public static final String ID = "org.dawb.tango.extensions";
	
	private static BundleContext context;
	// The shared instance
	private static Activator plugin;
	
	public static Activator getDefault() {
		return plugin;
	}

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		plugin = this;
		
		Hashtable<String, String> props = new Hashtable<String, String>(1);
		props.put("description", "A service which provides access to Tango Hardware.");
		context.registerService(IHardwareService.class, new TangoHardwareService(), props);

		super.start(bundleContext);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		plugin = null;
		super.stop(bundleContext);
	}

	public static ImageDescriptor imageDescriptorFromPlugin(String path) {
		return imageDescriptorFromPlugin(plugin.getBundle().getSymbolicName(), path);
	}

	/**
	 * Creates the image, this should be disposed later.
	 * @param path
	 * @return Image
	 */
	public static Image getImage(String path) {
		ImageDescriptor des = imageDescriptorFromPlugin("org.dawb.tango.extensions", path);
		return des.createImage();
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin("org.dawb.tango.extensions", path);
	}

}
