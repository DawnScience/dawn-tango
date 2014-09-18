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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawb.common.util.io.FileUtils;
import org.dawb.tango.extensions.Activator;
import org.dawb.tango.extensions.TangoUtils;
import org.dawb.tango.extensions.editors.actions.SharedMemoryActions;
import org.dawb.tango.extensions.editors.preferences.SharedConstants;
import org.dawb.tango.extensions.factory.TangoConnection;
import org.dawb.tango.extensions.factory.TangoConnectionFactory;
import org.dawnsci.plotting.AbstractPlottingSystem;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.ColorOption;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.Tango.DevError;
import fr.esrf.TangoApi.DeviceData;


/**
 * An editor which combines a plot with a graph of data sets.
 * 
 * Currently this is for 1D analyses only so if the data does not contain 1D, this
 * editor will not show.
 * 
 */
public class SharedMemoryEditor extends EditorPart {
	
	private static Logger logger = LoggerFactory.getLogger(SharedMemoryEditor.class);
	
	// This view is a composite of two other views.
	private IPlottingSystem             plottingSystem;	
	private Composite                   tools;
	private String                      memoryName;
	private boolean                     isMonitoring;
	private boolean                     isHistoryMode;
	private TangoConnection             connection;
	
	private final BlockingDeque<List<Dataset>> plotQueue;
	
	// NOTE At first glance, it might seem inefficient to keep the history here
	// but it does not make much odds. The graph redraws everything anyway if you
	// add one, so adding all should not be much slower than adding one at a time.
	private final List<Dataset> history;
	private Thread                      monitoringThread;
	private PlotType                    plotType;
	
	public SharedMemoryEditor() {
	
		this.plotQueue    = new LinkedBlockingDeque<List<Dataset>>(7);
		this.history      = new ArrayList<Dataset>(31);
		this.isMonitoring = Activator.getDefault().getPreferenceStore().getBoolean(SharedConstants.SHARED_MON);
		
		this.plotType = Activator.getDefault().getPreferenceStore().getBoolean(SharedConstants.IMAGE_MODE)
		              ? PlotType.IMAGE
		              : PlotType.XY;
		this.isHistoryMode = Activator.getDefault().getPreferenceStore().getBoolean(SharedConstants.HISTORY_MODE);
		try {
	        this.plottingSystem = PlottingFactory.createPlottingSystem();
	        plottingSystem.setColorOption(ColorOption.NONE);
		} catch (Exception ne) {
			logger.error("Cannot locate any plotting systems!", ne);
		}
 	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		
		setSite(site);
		super.setInput(input);
		setPartName("Shared Memory");
	}

	@Override
	public boolean isDirty() {
		return false;
	}
	
    /**
     * Thread safe
     * @param isVisible
     */
	public void setToolbarsVisible(final boolean isVisible) {
		if (tools==null||tools.isDisposed()) return;
		
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (tools==null || tools.isDisposed()) return;
				GridUtils.setVisible(tools, isVisible);
				tools.getParent().layout(new Control[]{tools});
				tools.getParent().getParent().layout();
			}
		});
	}

	@Override
	public void createPartControl(final Composite parent) {
		
		final Composite  main       = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		main.setLayout(gridLayout);
		GridUtils.removeMargins(main);
		
		tools = new Composite(main, SWT.NONE);
		tools.setLayout(new GridLayout(4, false));
		tools.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridUtils.removeMargins(tools);

		final Text point = new Text(tools, SWT.LEFT);
		point.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		point.setEditable(false);
		GridUtils.setVisible(point, false);
		point.setBackground(tools.getBackground());
		((AbstractPlottingSystem)plottingSystem).setPointControls(point);

		ToolBarManager sharedMan = new ToolBarManager(SWT.FLAT|SWT.RIGHT);
		final ToolBar  sharedBar = sharedMan.createControl(tools);
		sharedBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));		
		SharedMemoryActions.createActions(sharedMan);

		// We use a local toolbar to make it clear to the user the tools
		// that they can use, also because the toolbar actions are 
		// hard coded.
		final ToolBarManager toolMan = new ToolBarManager(SWT.FLAT|SWT.RIGHT);
		final ToolBar  toolBar = toolMan.createControl(tools);
		toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		final ToolBarManager rightMan = new ToolBarManager(SWT.FLAT|SWT.RIGHT);
		final ToolBar        rightBar = rightMan.createControl(tools);
		rightBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		final MenuManager    menuMan = new MenuManager();
		final IActionBars bars = this.getEditorSite().getActionBars();
		ActionBarWrapper wrapper = new ActionBarWrapper(toolMan,menuMan,null,(IActionBars2)bars);
		//wrapper.setToolbarControl(tools);
		
		// NOTE use name of input. This means that although two files of the same
		// name could be opened, the editor name is clearly visible in the GUI and
		// is usually short.
		final String plotName = this.getEditorInput().getName();

		final Composite plot = new Composite(main, SWT.NONE);
		plot.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		plot.setLayout(new FillLayout());
		
        plottingSystem.createPlotPart(plot, plotName, wrapper, PlotType.IMAGE, this);
        
	    Action menuAction = new Action("", Activator.getImageDescriptor("/icons/DropDown.png")) {
	    	@Override
	    	public void run() {
	    		final Menu   mbar = menuMan.createContextMenu(rightBar);
	    		mbar.setVisible(true);
	    	}
	    };
	    rightMan.add(menuAction);

	    sharedMan.update(true);
		toolMan.update(true);
		rightMan.update(true);
	    
		
		try {
		    final InputStream  in  = EclipseUtils.getInputStream(getEditorInput());
		    final StringBuffer buf = FileUtils.readFile(in);
		    if (buf!=null && buf.length()>0) {
		    	setMemoryNameInternal(buf.toString());
		    }
		} catch (Throwable ignored) {
			// Try and read memory name.
		}

 	}
	
	private void startMonitoring() throws Exception {
		
    	plotQueue.clear();

	    createConnection();
		createMonitoringThread();
		createPlotQueue();
		createFirstPlot();
	}

	private void createConnection() throws Exception {
		
    	final String hardwareURI = TangoUtils.getHardwareAddress(Activator.getDefault().getPreferenceStore().getString(SharedConstants.SPEC_SHARED));
		
    	try {
		    this.connection = TangoConnectionFactory.openCommandConnection(hardwareURI);
    	} catch (fr.esrf.TangoApi.ConnectionFailed cnf) {
    		showOpenConfigurationMessage(cnf.getMessage());
    	}
	}

	public void showOpenConfigurationMessage(final String message) {
		
		Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.ID, message));
		
		if (getSite().getShell().isVisible()) {
    		final boolean ret = MessageDialog.openQuestion(getSite().getShell(), "Cannot Connect to Tango", "Cannot connect to the shared memory.\n\nWould you like to open the Tango Preferences?");
	        if (ret)  SharedMemoryActions.tangoPrefs.run();
		}
	}

	private void createFirstPlot() throws Exception {
		final List<Dataset> sets = SharedMemoryUtils.getSharedMemoryValue(connection, getMemoryName(), plotType);
		if (sets==null) return;
		addPlot(sets);
	}

	private void stopMonitoring() throws Exception {
		if (connection!=null) connection.dispose();
		connection = null;
		while(monitoringThread!=null) Thread.sleep(100);
		
		plotQueue.clear();
		plotQueue.add(Collections.EMPTY_LIST);
	}
	
	private synchronized void createMonitoringThread() {
		
		this.monitoringThread = new Thread(new Runnable() {

			@Override
			public void run() {
				
				try {
					while(connection!=null) {
						
						try {
							final DeviceData  out = new DeviceData();
							final String      uri = TangoUtils.getSpecName().toLowerCase();
							final String      mem = getMemoryName();
							out.insert(new String[]{uri,mem});
							
							final DeviceData ret = connection.executeCommand("IsUpdated", out, false);
							if (ret.extractLong()==1) {
								
								final List<Dataset> sets = SharedMemoryUtils.getSharedMemoryValue(connection, getMemoryName(), plotType);
								if (sets==null)     continue;
								if (sets.isEmpty()) continue;
							
								plotQueue.clear();
								addPlot(sets);
							}
							
							final long time = Activator.getDefault().getPreferenceStore().getLong(SharedConstants.MON_FREQ);
							Thread.sleep(time);

						} catch (Exception ne) {
							break;
						}
					}
				} finally {
				   SharedMemoryEditor.this.monitoringThread = null;
				}
			}
			
		}, "Thread for monitoring "+memoryName);
		monitoringThread.setDaemon(true);
		monitoringThread.setPriority(4);
		monitoringThread.start();
	}
	
	protected void addPlot(List<Dataset> sets) {
		
		if (isHistoryMode && plotType==PlotType.XY) {
			
			int index = Activator.getDefault().getPreferenceStore().getInt(SharedConstants.CHUNK_INDEX);
			if (index>(sets.size()-1) || index<0) index = sets.size()-1;
			Dataset h = sets.get(index);
			history.add(0, h);
			
		} else {
		    history.addAll(0, sets);
		}
		
		while (history.size()>Activator.getDefault().getPreferenceStore().getInt(SharedConstants.HISTORY_SIZE)) {
			history.remove(history.size()-1);
		}
		plotQueue.add(history);

	}

	/**
	 * Queue required to avoid many updates coming in from monitoring thread.
	 */
	private void createPlotQueue() {
		
		final Thread queueThread = new Thread(new Runnable() {

			@Override
			public void run() {
				
				try {
					while (connection!=null&&plottingSystem!=null && !tools.isDisposed() ) {
						
						final List<Dataset> sets = plotQueue.take();
						if (sets.isEmpty()) break;
						
						
						if (plotType==PlotType.IMAGE) {
							//setToolbarsVisible(true);
							final IDataset set = sets.get(0);
							final List<IDataset> axes = new ArrayList<IDataset>(2);
							if (set==null || set.getShape()==null) {
								logger.error("Cannot read file "+getEditorInput().getName());
								return;
							}
							axes.add(createAxisDataset((set.getShape()[0])));
							axes.add(createAxisDataset((set.getShape()[1])));
							plottingSystem.createPlot2D(set, axes, null);
							
						} else {
							//setToolbarsVisible(false);
							final IDataset axis = SharedMemoryUtils.getXAxis(sets.get(0));
							plottingSystem.clear();
							final List<IDataset> ys = new ArrayList<IDataset>();
							for (Dataset iDataset : sets) ys.add(iDataset);
							plottingSystem.createPlot1D(axis, ys, null);
						}
					}
					
				} catch (Exception ne) {
					logger.error("Problem plotting", ne);
					return;
				} finally {
					logger.info("Plot queue finished.");
				}
			}
			
		}, "Plot Queue Job");
		queueThread.setPriority(4);
		queueThread.setDaemon(true);
		queueThread.start();
	}
	
	private static Dataset createAxisDataset(int size) {
		final int[] data = new int[size];
		for (int i = 0; i < data.length; i++) data[i] = i;
		IntegerDataset ret = new IntegerDataset(data, size);
		return ret;
	}
	
	/**
	 * Override to provide extra content.
	 * @param toolMan
	 */
	protected void createCustomToolbarActionsRight(final ToolBarManager toolMan) {

		toolMan.add(new Separator(getClass().getName()+"Separator1"));

		final Action tableColumns = new Action("Open editor preferences.", IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "org.edna.workbench.editors.preferencePage", null, null);
				if (pref != null) pref.open();
			}
		};
		tableColumns.setChecked(false);
		tableColumns.setImageDescriptor(Activator.getImageDescriptor("icons/application_view_columns.png"));

		toolMan.add(tableColumns);
		
	}

	@Override
	public void setFocus() {
		
	}

	@Override
	public void doSave(IProgressMonitor monitor) {

	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

    @Override
    public void dispose() {
    	
    	plotQueue.clear();
    	plotQueue.add(Collections.EMPTY_LIST);
    	if (connection!=null) {
			try {
				connection.dispose();
			} catch (Exception e) {
				logger.error("Cannot close connection "+connection, e);
			}
    	}
     	if (plottingSystem!=null) plottingSystem.dispose();
     	plottingSystem   = null;
    	history.clear();
     	super.dispose();
    }

	public String getMemoryName() {
		return memoryName;
	}

	public void setMemoryName(String memoryName) throws Exception {
		this.memoryName = memoryName;
		// We try and save this
		try {
			final OutputStream out = EclipseUtils.getOutputStream(getEditorInput());
			FileUtils.write(out, memoryName, "UTF-8", false);
		} catch (Throwable ignored) {
			// Not the end of the world.
		}
		setMemoryNameInternal(memoryName);
	}
	
	private void setMemoryNameInternal(final String memoryName) throws Exception {
		this.memoryName = memoryName;
		setPartName("Monitor "+memoryName);
		if (isMonitoring) {
			stopMonitoring();
			startMonitoring();
		}
	}

	public boolean isMonitoring() {
		return isMonitoring;
	}

	public void setMonitoring(boolean isMonitoring) throws Exception {
		this.isMonitoring = isMonitoring;
		if (!isMonitoring) {
			stopMonitoring();
		} else {
			startMonitoring();
		}
	}

	public void setPlotType(PlotType pt) {
		plotType = pt;
		try {
			createFirstPlot();
		} catch (Exception e) {
			logger.error("Cannot do a plot", e);
		}
	}

	/**
	 * Attempts to create a connection if one does not already exist.
	 */
	public TangoConnection getTangoConnection() {
		
		if (this.connection==null) {
			try {
				final String hardwareURI = TangoUtils.getHardwareAddress(Activator.getDefault().getPreferenceStore().getString(SharedConstants.SPEC_SHARED));
				this.connection = TangoConnectionFactory.openCommandConnection(hardwareURI);
				
			} catch (fr.esrf.Tango.DevFailed e) {
				if (e.errors!=null&&e.errors.length>0) {
					for (int i = 0; i < e.errors.length; i++) {
						DevError error = e.errors[i];
						logger.error(error.desc);
					}
				}
			    logger.error("Cannot create connection ", e);

		    } catch (Exception ne) {
				logger.error("Cannot create connection ", ne);
				return null;
			}
		}
		return this.connection;
	}

	public boolean isHistoryMode() {
		return isHistoryMode;
	}

	public void setHistoryMode(boolean isHistoryMode) {
		this.isHistoryMode = isHistoryMode;
		if (!isHistoryMode) history.clear();
	}
}
