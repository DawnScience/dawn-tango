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

import org.dawb.tango.extensions.TangoUtils;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleDocumentPartitioner;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TangoSpecConsole extends TextConsole {

	private static Logger logger = LoggerFactory.getLogger(TangoSpecConsole.class);

	public static final String ID = "org.dawb.tango.extensions.TangoSpecConsole";
	
	private TangoSpecPartitioner partitioner;

	public TangoSpecConsole(final String          name, 
			                final ImageDescriptor imageDescriptor, 
			                final boolean         autoLifecycle) {
		
		super(name, TangoSpecConsole.class.getName(), imageDescriptor, autoLifecycle);
		
        partitioner = new TangoSpecPartitioner(TangoUtils.getSpecName());       
        getDocument().addPositionCategory(TangoSpecPartitioner.PROMPT_CATEGORY);
        getDocument().addPositionCategory(TangoSpecPartitioner.COMMENT_CATEGORY);
        getDocument().addPositionCategory(TangoSpecPartitioner.SCAN_CATEGORY);
        getDocument().addPositionCategory(TangoSpecPartitioner.COMMAND_CATEGORY);
        getDocument().setDocumentPartitioner(partitioner);
        partitioner.connect(getDocument());
        
        final String promptRegEx = "\\d+\\."+TangoUtils.getSpecName().toUpperCase()+">";
        addPatternMatchListener(new RegExListener(promptRegEx,                    TangoSpecPartitioner.PROMPT_CATEGORY));
        addPatternMatchListener(new RegExListener(TangoUtils.COMMENT.pattern(),   TangoSpecPartitioner.COMMENT_CATEGORY));
        addPatternMatchListener(new RegExListener(TangoUtils.SCAN_LINE.pattern(), TangoSpecPartitioner.SCAN_CATEGORY));
 	    addPatternMatchListener(new RegExListener(TangoUtils.CMD.pattern(),       TangoSpecPartitioner.COMMAND_CATEGORY));
	
	}
	
	public IPageBookViewPage createPage(IConsoleView view) {
		TangoSpecConsolePage textPage = new TangoSpecConsolePage(this, view);
		textPage.setPartitioner(partitioner);
		
		final IPreferenceStore store = new ScopedPreferenceStore(new InstanceScope(), "org.dawb.common.ui");
		store.addPropertyChangeListener(new IPropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals("org.dawb.remote.session.mock")) {
					IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
			    	manager.removeConsoles(new IConsole[]{TangoSpecConsole.this});
			    }
			}
		});

		return textPage;
	}
	
	public void dispose() {
		super.dispose();
		partitioner.disconnect();
	}
	
	public void init() {
		super.init();
	}

	@Override
	protected IConsoleDocumentPartitioner getPartitioner() {
		return partitioner;
	}
	
	public class RegExListener implements IPatternMatchListener {

		private String regex;
		private String category;
		private int stagger;
		public RegExListener(String regex, String category) {
            this(regex, category, 0);
		}
		public RegExListener(String regex, String category, int stagger) {
			this.regex    = regex;
			this.category = category;
			this.stagger  = stagger;
		}

		@Override
		public void matchFound(PatternMatchEvent event) {
			try {
				final Position pos = new Position(event.getOffset()+stagger, event.getLength()-stagger);
				getDocument().addPosition(category, pos);
			} catch (Exception e) {
				logger.error("Cannot add position",e);
			}
		}

		@Override
		public String getPattern() {
			return regex;
		}

        public int getCompilerFlags() {
            return 0;
        }

        public String getLineQualifier() {
            return null;
        }

        public void connect(TextConsole console) {
        }

        public void disconnect() {
        }

	}
	
}
