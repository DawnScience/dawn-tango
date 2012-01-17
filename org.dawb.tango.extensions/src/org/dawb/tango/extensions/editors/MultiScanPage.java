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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.Page;

public class MultiScanPage extends Page implements IAdaptable{

	private MultiScanComponent    multiScanComponent;
	private final MultiScanEditor      editor;
	private Composite             content;
	
	public MultiScanPage(final MultiScanEditor editor) {
		this.editor = editor;
	}
	
	@Override
	public void createControl(Composite main) {

        this.content = new Composite(main, SWT.NONE);
        content.setLayout(new GridLayout(1, false));
		org.dawb.common.ui.util.GridUtils.removeMargins(content);
      
		this.multiScanComponent = new MultiScanComponent(editor.getPlottingSystem());
		multiScanComponent.createPartControl(content);
		multiScanComponent.addDatasetListener(new IDatasetListener() {
			@Override
			public void datasetSelectionChanged(final DatasetChangedEvent event) {
				editor.updatePlot(event.getSelections());
			}
		});
				
	    final Control treeControl= multiScanComponent.getViewer().getControl();
	    final Menu    rightClick = editor.getMenuManager().createContextMenu(treeControl);
	    treeControl.setMenu(rightClick);

		// Finally
	    multiScanComponent.setData(editor.getData());
	}

	@Override
	public Control getControl() {
		return content;
	}

	@Override
	public void setFocus() {
		multiScanComponent.getViewer().getControl().setFocus();
	}

	public MultiScanComponent getMultiScanComponent() {
		return multiScanComponent;
	}

	public void dispose() {
		multiScanComponent.dispose();
		super.dispose();
	}
	

	@Override
	public Object getAdapter(Class type) {
		if (type == String.class) {
			return "Data";
		}
		return null;
	}

}
