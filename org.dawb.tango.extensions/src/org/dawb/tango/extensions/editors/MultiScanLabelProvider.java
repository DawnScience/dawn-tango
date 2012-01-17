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

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

public class MultiScanLabelProvider extends ColumnLabelProvider {

	private static final Logger logger = LoggerFactory.getLogger(MultiScanLabelProvider.class);
	
	private AbstractPlottingSystem system;
	
	MultiScanLabelProvider(final AbstractPlottingSystem editor) {
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
			       : ((AbstractDataset)object).getName();
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		
		final TreeNode node  = (TreeNode)element;
		final Object  object = ((DefaultMutableTreeNode)node).getUserObject();
	    if (object instanceof AbstractDataset && system!=null) {
	    	return system.get1DPlotColor((AbstractDataset)object);
	    }
		return null;
	}


}
