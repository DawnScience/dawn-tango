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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.dawnsci.plotting.AbstractPlottingSystem;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiScanLabelProvider extends ColumnLabelProvider {

	private static final Logger logger = LoggerFactory.getLogger(MultiScanLabelProvider.class);
	
	private IPlottingSystem<Composite> system;
	
	MultiScanLabelProvider(final IPlottingSystem<Composite> editor) {
		this.system = editor;
	}
	
//	@Override
//	public Image getColumnImage(Object element, int columnIndex) {
//		return null;
//	}
//
//	/**
//	 * { "Name", "Class", "Dims", "Type", "Size" };
//	 */
//	@Override
//	public String getColumnText(Object element, int columnIndex) {
//		
//		if (!(element instanceof TreeNode)) return null;
//		return getText(element);
//	}
	
	public String getText(Object element) {
		
		final TreeNode node  = (TreeNode)element;
		final Object  object = ((DefaultMutableTreeNode)node).getUserObject();

		return object instanceof String
			       ? (String)object
			       : ((Dataset)object).getName();
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		
		final TreeNode node  = (TreeNode)element;
		final Object  object = ((DefaultMutableTreeNode)node).getUserObject();
	    if (object instanceof Dataset && system!=null) {
	    	
	    	return ((AbstractPlottingSystem)system).get1DPlotColor((Dataset)object);
	    }
		return null;
	}


}
