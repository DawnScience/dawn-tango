/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.tango.extensions.console;

import org.dawb.common.ui.views.monitor.actions.TangoPreferencesAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.console.TextConsolePage;
import org.eclipse.ui.console.TextConsoleViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TangoSpecConsolePage extends TextConsolePage {

	private static Logger logger = LoggerFactory.getLogger(TangoSpecConsolePage.class);
	
	private TangoSpecPartitioner partitioner;

	public TangoSpecConsolePage(TextConsole console, IConsoleView view) {
		super(console, view);
	}
	
	private static boolean isShowingError = false;
	
	protected TextConsoleViewer createViewer(Composite parent) {
				
		final TextConsoleViewer viewer = super.createViewer(parent);
		partitioner.setTextViewer(viewer);
		
		viewer.getTextWidget().addVerifyListener(new VerifyListener() {
		
			@Override
			public void verifyText(final VerifyEvent event) {
					
				if (!viewer.isEditable()) {
					event.doit = false;
					return;
				}
				
				final StyledText text = (StyledText)event.getSource();
                if ("".equals(event.text)) {
					if (text.getCaretOffset()<=partitioner.getPromptLocation()) event.doit = false;
					return;
				}
                
                if (text.getCaretOffset()>=partitioner.getPromptLocation()) {
                	return;
                }
                
                // All text added to the end
				event.doit = false;
				try {
					viewer.getDocument().replace(viewer.getDocument().getLength(), 0, event.text);
					text.setCaretOffset(viewer.getDocument().getLength());
					text.setSelection(viewer.getDocument().getLength(),viewer.getDocument().getLength());
				    // Refresh last line
				    final int line = viewer.getDocument().get().lastIndexOf("\n");
				    text.redrawRange(line, viewer.getDocument().getLength()-line, false);

				} catch (BadLocationException e) {
					logger.error("Cannot append text to document", e);
				}
			}
		});
		
		viewer.getTextWidget().addVerifyKeyListener(new VerifyKeyListener() {
			@Override
			public void verifyKey(VerifyEvent event) {
				
				if (!viewer.isEditable()) {
					event.doit = false;
					return;
				}
				
				if (event.keyCode == 13) {
					partitioner.runCommand();
					event.doit = false;
					return;
				} else if (event.keyCode == SWT.ARROW_UP) {
					partitioner.moveCommand(-1);
					event.doit = false;
					return;
				} else if (event.keyCode == SWT.ARROW_DOWN) {
					partitioner.moveCommand(1);
					event.doit = false;
					return;
				} else if (event.keyCode == SWT.ARROW_LEFT) {
					final StyledText text = (StyledText)event.getSource();
					if (text.getCaretOffset()<=partitioner.getPromptLocation()) event.doit = false;
					return;
				} else if (event.keyCode == SWT.ARROW_RIGHT) {
					return;
				}
				
				partitioner.resetCommandPosition();
			}
		});

		try {
			
		    partitioner.connectSpec();
		    
		} catch (Exception ne) {
			if (isShowingError) return viewer;
			isShowingError = true; // HACK because eclipse can call this twice.
			try {
				boolean yes = MessageDialog.openQuestion(getSite().getShell(), "Cannot Connect", "Cannot connect through tango to spec with the current settings.\n\nWould you like to open the Tango Preferences and configure the connection?");
			    if (yes) try {
			    	(new TangoPreferencesAction()).execute(null);
			    } catch (Exception neOther) {
			    	logger.error("Cannot open Tango Preferences!", neOther);
			    }
			} finally {
				isShowingError = false;
			}
		}

		return viewer;
	}

	public TangoSpecPartitioner getPartitioner() {
		return partitioner;
	}

	public void setPartitioner(TangoSpecPartitioner partitioner) {
		this.partitioner = partitioner;
	}
}
