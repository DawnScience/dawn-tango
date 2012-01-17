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

import java.util.regex.Pattern;

import org.dawb.tango.extensions.Activator;
import org.dawb.tango.extensions.TangoUtils;
import org.dawb.tango.extensions.editors.spec.SpecTextEditor;
import org.dawb.tango.extensions.factory.TangoConnection;
import org.dawb.tango.extensions.factory.TangoConnectionEvent;
import org.dawb.tango.extensions.factory.TangoConnectionFactory;
import org.dawb.tango.extensions.factory.TangoConnectionListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiScanMultiEditor extends MultiPageEditorPart implements TangoConnectionListener {

	public static final String ID = "org.dawb.workbench.editors.AsciiEditor"; //$NON-NLS-1$

	private static final Logger logger = LoggerFactory.getLogger(MultiScanMultiEditor.class);
	
	
	private transient TangoConnection  tangoConnection;

	private MultiScanEditor specEditor;
	private SpecTextEditor  textEditor;
	
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException{
        
		super.init(site, input);
	    setPartName(input.getName());
    }
	
	/**
	 * It might be necessary to show the tree editor on the first page.
	 * A property can be introduced to change the page order if this is required.
	 */
	@Override
	protected void createPages() {
		try {

			connect();

			this.specEditor = new MultiScanEditor(isConnected);
			addPage(0, specEditor, getEditorInput());
			setPageText(0, "Plot");

			this.textEditor = new SpecTextEditor();
			addPage(1, textEditor,   getEditorInput());			
			setPageText(1, "Text");

			
		} catch (PartInitException e) {
			logger.error("Cannot initiate "+getClass().getName()+"!", e);
		}
	}
	
	private Pattern   promptPattern;
	private boolean   isConnected = false;
	private String    specError   = null;
	public void connect() {
		
		if (tangoConnection!=null) return;
		
		this.promptPattern = Pattern.compile("\\d+\\."+TangoUtils.getSpecName()+">");

		final String address = TangoUtils.getSpecCommandAddress();
		try {
		    tangoConnection = TangoConnectionFactory.openMonitoredCommandConnection(address, "Output");
			tangoConnection.addTangoConnectionListener(this);
			this.isConnected = true;
			this.specError   = null;
		} catch (fr.esrf.TangoApi.ConnectionFailed cf) {
			
			if (cf.errors!=null && cf.errors.length>0) {
				Activator.getDefault().getLog().log(new Status(Status.WARNING, "org.dawb.tango.extensions", cf.errors[0].desc));
			}
			this.specError   = "Cannot connect to tango session '"+address+"'";
			logger.error("Failed to connect to spec-tango "+address, cf);
			
		} catch (Throwable f) {
			this.specError   = "Cannot connect to tango session '"+address+"'";
			logger.error("Failed to connect to spec-tango "+address, f);
			return;
		}

	}
	
	public void disconnect() {
		
		try {
		    if (tangoConnection!=null) {
		    	tangoConnection.removeTangoConnectionListener(this);
				tangoConnection.dispose();
		    }
			this.specError   = null;
		} catch (Exception e) {
			this.specError   = "Cannot disconnect to tango session '"+tangoConnection.getUri()+"'";
			logger.error("Failed to disconnect from tango!", e);
		} finally {
			this.isConnected = false;
		}
		tangoConnection  = null;
	}
	

	public String getSpecError() {
		return specError;
	}

	public boolean toggleConnect() {
		if (isConnected) disconnect();
		else             connect();
		return isConnected;
	}


	@Override
	public void tangoEventPerformed(final TangoConnectionEvent event) {
		
		if (tangoConnection==null) return;
		if (promptPattern  ==null) return;
		
		if (event.getErrorMessage()!=null) {
			// TODO Did not check if Ilog.log is thread safe so use UI.
			getSite().getShell().getDisplay().syncExec(new Runnable(){
				@Override
				public void run() {
					Activator.getDefault().getLog().log(new Status(Status.WARNING, 
						                            "org.dawb.tango.extensions", 
						                            event.getErrorMessage(),
						                            new Exception()));
				}
			});
			return;
		}
		
		try {
			final String value = event.getValue().extractString().trim();
		
			if (promptPattern.matcher(value).matches()) return;
			
			if (specEditor!=null) specEditor.addLine(value);
			if (textEditor!=null) textEditor.addLine(value);
			
		} catch (Exception ne) {
			logger.error("Cannot update tango event "+event, ne);
		}
		
	}
	
	/** 
	 * Save allowed but not save as.
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		textEditor.doSave(monitor);
	}

	/** 
	 * No Save
	 */
	@Override
	public void doSaveAs() {
		textEditor.doSaveAs();
	}

	/** 
	 * We are not saving this class
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	public void setActivePage(final int ipage) {
		super.setActivePage(ipage);
	}

	@Override
	public IEditorPart getActiveEditor() {
		return super.getActiveEditor();
	}

	public void dispose() {
		super.dispose();
		disconnect();
		tangoConnection = null;
		specEditor      = null;
		textEditor      = null;
	}
	
	public String getSpecSessionName() {
		// None if disconnected.
		if (tangoConnection==null) return null;
		return tangoConnection.getName();
	}
	
	public MultiScanEditor getMultiScanEditor() {
		return this.specEditor;
	}
	
	public Object getAdapter(final Class clazz) {
		
		if (clazz == Page.class) {
			final MultiScanEditor      ed  = getMultiScanEditor();
			return new MultiScanPage(ed);
		}
		
		return super.getAdapter(clazz);
	}
}
