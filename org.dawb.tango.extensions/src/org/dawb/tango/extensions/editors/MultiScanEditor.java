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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.views.PlotDataView;
import org.dawb.common.ui.views.monitor.actions.TangoPreferencesAction;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawb.gda.extensions.spec.MultiScanDataParser;
import org.dawb.tango.extensions.Activator;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.trace.ColorOption;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;


/**
 * An editor which combines a plot with a graph of data sets.
 * 
 * Currently this is for 1D analyses only so if the data does not contain 1D, this
 * editor will not show.
 * 
 */
public class MultiScanEditor extends EditorPart implements IReusableEditor {
	
	private static Logger logger = LoggerFactory.getLogger(MultiScanEditor.class);
	
	// This view is a composite of two other views.
	private IPlottingSystem             plottingSystem;	
    private MultiScanDataParser         data;
	private boolean                     isDefaultConnectedSpec;
	private Composite                   plot;

	private MenuManager menuMan;

	public MultiScanEditor(final boolean isConnectedSpec) {
		
		this.isDefaultConnectedSpec = isConnectedSpec;
		try {
			this.plottingSystem = PlottingFactory.createPlottingSystem();
			plottingSystem.setColorOption(ColorOption.BY_DATA);
		} catch (Exception e) {
			logger.error("Cannot locate any plotting systems!", e);
		}
 	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(input.getName());
	}

	@Override
	public boolean isDirty() {
		return false;
	}
	
	@Override
	public void createPartControl(final Composite parent) {
		
		final Composite  main       = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout = new GridLayout(1, false);
		main.setLayout(gridLayout);
		GridUtils.removeMargins(main);
				
		final Composite tools = new Composite(main, SWT.RIGHT);
		tools.setLayout(new GridLayout(2, false));
		GridUtils.removeMargins(tools);
		tools.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		
		// We use a local toolbar to make it clear to the user the tools
		// that they can use, also because the toolbar actions are 
		// hard coded.
		ToolBarManager toolMan = new ToolBarManager(SWT.FLAT|SWT.RIGHT);
		final ToolBar          toolBar = toolMan.createControl(tools);
		toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		
		ToolBarManager extraMan = new ToolBarManager(SWT.FLAT|SWT.RIGHT);
		final ToolBar          extraBar = extraMan.createControl(tools);
		extraBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		this.menuMan = new MenuManager();
		final IActionBars bars = this.getEditorSite().getActionBars();
		ActionBarWrapper wrapper = new ActionBarWrapper(toolMan,null,null,(IActionBars2)bars);
		
		createCustomMenuBarActionsRight(menuMan);
		createCustomToolbarActionsRight(extraMan);
		Action menuAction = new Action("", Activator.imageDescriptorFromPlugin("icons/DropDown.png")) {
			@Override
			public void run() {
				final Menu   mbar = menuMan.createContextMenu(extraBar);
				mbar.setVisible(true);
			}
		};
		extraMan.add(menuAction);

        this.plot = new Composite(main, SWT.BORDER);
		plot.setLayout(new FillLayout());
        plot.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				 		
		// NOTE use name of input. This means that although two files of the same
		// name could be opened, the editor name is clearly visible in the GUI and
		// is usually short.
		final String plotName = this.getEditorInput().getName();
		
        plottingSystem.createPlotPart(plot, plotName, wrapper, PlotType.XY, this);     
		
		// Finally
		if (toolMan!=null)   toolMan.update(true);
		if (extraMan!=null)  extraMan.update(true);
	    
 	}
	
	private void createCustomMenuBarActionsRight(final MenuManager menuMan) {
		
		menuMan.add(new Action("Unselect and collapse all") {
			public void run() {
				plottingSystem.reset();
				
				final MultiScanComponent comp = getMultiScanComponent(false);
				if (comp==null) return;
				
				comp.clear();

			}
		});
		final Action tableColumns = new Action("Preferences...", IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "org.edna.workbench.editors.preferencePage", null, null);
				if (pref != null) pref.open();
			}
		};
		tableColumns.setImageDescriptor(Activator.imageDescriptorFromPlugin("icons/application_view_columns.png"));
		menuMan.add(tableColumns);
	}

	/**
	 * Override to provide extra content.
	 * @param toolMan
	 */
	protected void createCustomToolbarActionsRight(final ToolBarManager toolMan) {
		
		
		toolMan.add(new Separator(getClass().getName()+"Separator1"));

		final Action connectSpec = new Action("Disconnect from spec.", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				final IEditorPart part = EclipseUtils.getActiveEditor();
				if (part instanceof MultiScanMultiEditor) {
					final boolean isConnected = ((MultiScanMultiEditor)part).toggleConnect();
					setChecked(isConnected);
					if (isConnected) {
						setText("Disconnect from spec.");
					} else {
						setText("Connect to spec.");
						final String errorMessage = ((MultiScanMultiEditor)part).getSpecError();
						if (errorMessage!=null) {
							boolean yes = MessageDialog.openQuestion(getSite().getShell(), "Cannot Connect", errorMessage+"\n\nWould you like to open the Tango Preferences and configure the connection?");
						    if (yes) try {
						    	(new TangoPreferencesAction()).execute(null);
						    } catch (Exception ne) {
						    	logger.error("Cannot open Tango Preferences!", ne);
						    }
						}
					}
				}
			}
		};
		connectSpec.setChecked(isDefaultConnectedSpec);
		if (isDefaultConnectedSpec) {
			connectSpec.setText("Disconnect from spec.");
		} else {
			connectSpec.setText("Connect to spec.");
		}	
		connectSpec.setImageDescriptor(Activator.imageDescriptorFromPlugin("icons/connect_spec.png"));

		
		toolMan.add(connectSpec);
		
	}

	private boolean doingUpdate = false;
	
	/**
	 * 
	 * @param selections - takes copy and possibly modifies it
	 */
	protected void updatePlot(final List<AbstractDataset> sel) {
		
		if (doingUpdate) return;
		
		try {
			doingUpdate = true;
			if (sel==null||sel.size()<1) {
				plottingSystem.reset();
				return;
			}
			
			final List<IDataset> selections = new ArrayList<IDataset>(sel);

			IProgressService service =  getEditorSite()!=null 
			                         ? (IProgressService)getSite().getService(IProgressService.class)
					                 : (IProgressService)PlatformUI.getWorkbench().getService(IProgressService.class);
			try {
				service.run(true, true, new IRunnableWithProgress() {
					@Override
					public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
	
						monitor.beginTask("Updating selected DataSets", 100);

						
						final IDataset x = selections.remove(0);
						plottingSystem.clear();
						plottingSystem.createPlot1D(x, selections, monitor);
						
						monitor.done();
					}
				});
			} catch (Exception e) {
				logger.error("Cannot create plot required.", e);
			} 
		} finally {
			doingUpdate = false;
		}
	}

	@Override
	public void setInput(final IEditorInput input) {
		super.setInput(input);
		setPartName(input.getName());
		try {
			data = new MultiScanDataParser(EclipseUtils.getInputStream(input));
		} catch (Exception e) {
			logger.error("Cannot read "+input.getName(), e);
		}
	}	

	@Override
	public void setFocus() {
		plot.setFocus();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

    @Override
    public void dispose() {
     	if (plottingSystem!=null) plottingSystem.dispose();
      	super.dispose();
    }

    /**
     * Thread safe update method.
     * @param line
     */
	public void addLine(final String line) {
		
		if (line==null || "".equals(line)) return;
		
		final MultiScanComponent multiScanComponent = getMultiScanComponent(false);
		if (multiScanComponent==null) return;

		final String scanName = this.data.processLine(line);
		
		if (scanName!=null) {
			this.data.update(false);
			getSite().getShell().getDisplay().asyncExec(new Runnable(){
				@Override
				public void run() {
					multiScanComponent.refresh(scanName);
				}
			});
		}
	}

	public void setPlot(String scanName, String... plotNames) {
		MultiScanComponent multiScanComponent = getMultiScanComponent(true);
		if (multiScanComponent==null) return;
		multiScanComponent.setPlot(scanName, plotNames);
	}
	
	private MultiScanComponent getMultiScanComponent(boolean showView) {
		
		final IWorkbenchPage wb =EclipseUtils.getActivePage();
		if (wb==null) return null;
		
		PageBookView view = (PageBookView)wb.findView(PlotDataView.ID);
		if (view==null&&showView)
			try {
				view = (PageBookView)wb.showView(PlotDataView.ID);
			} catch (PartInitException e) {
				return null;
			}
		if (view==null) return null;
		
		IPage page = view.getCurrentPage();
		if (!(page instanceof MultiScanPage)) return null;
		return ((MultiScanPage)page).getMultiScanComponent();
	}

	public IPlottingSystem getPlottingSystem() {
		return this.plottingSystem;
	}
	
	public MultiScanDataParser getData() {
		return data;
	}

	public MenuManager getMenuManager() {
		return this.menuMan;
	}
}
