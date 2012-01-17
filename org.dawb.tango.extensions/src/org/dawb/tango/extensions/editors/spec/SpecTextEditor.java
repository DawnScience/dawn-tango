/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.tango.extensions.editors.spec;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO Convert to syntax highlighting editor.
 * @author gerring
 *
 */
public class SpecTextEditor extends TextEditor {
	

	private static Logger logger = LoggerFactory.getLogger(SpecTextEditor.class);
	
	public void createPartControl(final Composite parent) {
		super.createPartControl(parent);
//		
		// TODO - custom syntax highlighting
//		final ISourceViewer viewer = getSourceViewer();
//		viewer.getDocument().addPositionUpdater(new IPositionUpdater() {		
//			@Override
//			public void update(DocumentEvent event) {
//				event.get
//			}
//		});
	}

    /**
     * Thread safe update method.
     * @param line
     */
	public void addLine(final String line) {
		
		if (line==null || "".equals(line)) return;
		
		final StringBuilder buf = new StringBuilder(line);
		buf.append("\n");
		getSite().getShell().getDisplay().asyncExec(new Runnable(){
			@Override
			public void run() {
		
				IDocument doc = getDocumentProvider().getDocument(null);
				if (doc == null) doc = SpecTextEditor.this.getSourceViewer().getDocument();
				try {
					doc.replace(doc.getLength(), 0, buf.toString());
					SpecTextEditor.this.getSourceViewer().getTextWidget().setCaretOffset(doc.getLength());
					SpecTextEditor.this.getSourceViewer().getTextWidget().setSelection(doc.getLength(), doc.getLength());
		 
				} catch (BadLocationException e) {
					logger.error("Cannot add text to document end!", e);
				}
			}
		});
	}
}
