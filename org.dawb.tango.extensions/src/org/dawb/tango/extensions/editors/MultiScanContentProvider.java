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

import javax.swing.tree.TreeNode;

import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

public class MultiScanContentProvider implements ILazyTreeContentProvider {

	private TreeViewer treeViewer;

	public MultiScanContentProvider() {

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		treeViewer = (TreeViewer) viewer;
		treeViewer.refresh();
	}

	@Override
	public void updateElement(Object parent, int index) {
		TreeNode node = (TreeNode) parent;
		if (node==null) return;
		if (index < node.getChildCount()) {
			TreeNode element = node.getChildAt(index);
			treeViewer.replace(parent, index, element);
			updateChildCount(element, -1);
		}
	}

	@Override
	public void updateChildCount(Object element, int currentChildCount) {
		TreeNode node = (TreeNode) element;
		int size = node.getChildCount();
		treeViewer.setChildCount(element, size);
	}

	@Override
	public Object getParent(Object element) {
		if (element==null || !(element instanceof TreeNode)) {
			return null;
		}
		final TreeNode node = ((TreeNode) element);
		return node.getParent();
	}


}
