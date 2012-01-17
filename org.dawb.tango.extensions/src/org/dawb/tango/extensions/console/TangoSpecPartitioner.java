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

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import java.util.regex.Pattern;

import org.dawb.common.ui.DawbUtils;
import org.dawb.common.util.io.IOCollectionUtils;
import org.dawb.tango.extensions.TangoUtils;
import org.dawb.tango.extensions.factory.TangoConnection;
import org.dawb.tango.extensions.factory.TangoConnectionEvent;
import org.dawb.tango.extensions.factory.TangoConnectionFactory;
import org.dawb.tango.extensions.factory.TangoConnectionListener;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.TypedRegion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.IConsoleDocumentPartitioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TangoSpecPartitioner implements IConsoleDocumentPartitioner, TangoConnectionListener {
	

	protected static final String PROMPT_CATEGORY  = "org.dawb.tango.extensions.console.PROMPT";
	protected static final String COMMENT_CATEGORY = "org.dawb.tango.extensions.console.COMMENT";
	protected static final String SCAN_CATEGORY    = "org.dawb.tango.extensions.console.SCAN_NUMBERS";
	protected static final String COMMAND_CATEGORY = "org.dawb.tango.extensions.console.COMMAND";
	
	private static Logger logger = LoggerFactory.getLogger(TangoSpecPartitioner.class);
	
	private transient TangoConnection  tangoConnection;
	private transient TextViewer       textViewer;
	
	private Color     green,darkGrey,blue,bg,darkBlue,black;
	private IDocument document;
	private String    specName;
	private int       count    = 0;
	private int       prompt   = 0;
	private List<String>         undoStack;
	private ListIterator<String> stackNavigation;
	private Pattern   promptPattern; 

	public TangoSpecPartitioner(final String specName) {
		
		this.specName      = specName.toUpperCase();
		this.promptPattern = Pattern.compile("\\d+\\."+specName+">");
		
		this.black = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
		this.green = Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
		this.darkBlue = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE);
		this.darkGrey = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
		this.blue = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
		this.bg = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
		
		undoStack = new Vector<String>(31); // We need synchronized
		try {
			final List<String> saved = (List<String>)IOCollectionUtils.readCollection(DawbUtils.getDawbHome()+specName+".collection");
			undoStack.addAll(saved);
		} catch (Throwable ne) {
		}
		
	}

	@Override
	public void connect(IDocument document) {
		this.document = document;
	}

	public void connectSpec() throws Exception {
		
		if (tangoConnection!=null) return;
		final String address = TangoUtils.getSpecCommandAddress();
		textViewer.setEditable(true);
		try {
		    tangoConnection = TangoConnectionFactory.openMonitoredCommandConnection(address, "Output");
			tangoConnection.addTangoConnectionListener(this);
		    
			document.set(getStartingText());
			updateStartConsolePosition();
		
		} catch (Exception f) {
			document.set(f.getMessage());
			textViewer.setEditable(false);
			logger.error("Failed to connect to spec-tango "+address, f);
			throw f;
		}

	}

	@Override
	public void disconnect() {
		try {
		    if (tangoConnection!=null) {
		    	tangoConnection.removeTangoConnectionListener(this);
				tangoConnection.dispose();
		    }
		} catch (Exception e) {
			logger.error("Failed to disconnect from tango!", e);
		}
		textViewer       = null;
		tangoConnection  = null;
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
		
	}
	public boolean documentChanged(DocumentEvent event) {
		return false;
	}

	public boolean runCommand() {

		if (document==null) return false;

		String  cmd        = "";
		try {

			cmd = document.get(prompt, document.getLength()-prompt);
			cmd = cmd.replace("\n", "");

			if (!"".equals(cmd)) {
				
				addUndoableCommand(cmd);
				

				// runs command, listeners should be notified.
				if (!isCommandValid(cmd)) {
					printNextSpecPrompt(false);
					return false;
				}
				
				tangoConnection.executeCommand("ExecuteCmd", cmd, true);
				setEditable(false);
				
			} else {
				printNextSpecPrompt(false);
			}

		} catch (fr.esrf.TangoApi.ConnectionFailed  cf) {
			if (cf.errors!=null&&cf.errors.length>0) {
			    printMessage("\n+"+cf.errors[0].desc);
			} else {
				printMessage("\nAn error occurred sending command: \""+cmd+"\". It has not been run.\n"+cf.getMessage()); 
			}
			printNextSpecPrompt(false);
		} catch (Exception e) {
			logger.error("Cannot connect to tango spec server!", e);
			printMessage("\nAn error occurred sending command: \""+cmd+"\". It has not been run.\n"+e.getMessage()); 
			printNextSpecPrompt(false);
		}

		return false;
	}
	

	private boolean isCommandValid(String cmd) {
		if ("help".equals(cmd)) {
			printMessage("\nApologies, the help menu is not available via remote spec terminal.\nA later version of spec remote terminal will have a help web page."); 
			return false;
		}
		if ("config".equals(cmd)) {
			printMessage("\nThe 'config' command is not supported via remote spec terminal.\nPlease contact your beamline support to change hardware configuration."); 
			return false;
		}
		return true;
	}

	@Override
	public void tangoEventPerformed(TangoConnectionEvent event) {
		
		if (event.isFinishedEvent()) {
			setEditable(true);
			if (event.getErrorMessage()!=null) {
				printMessage("\n"+event.getErrorMessage());
			}
		    printNextSpecPrompt(true);
		} else {
			try {
				final String value = event.getValue().extractString().trim();
				if ("".equals(value))                       return;
				if (promptPattern.matcher(value).matches() && !isEditable()) {
					setEditable(true);
				    printNextSpecPrompt(true);
                    return;					
				}
				printMessage("\n"+value);
				setCaretLocation(true);
				setEditable(false);
				
			} catch (Exception e) {
				logger.error("Problem reading value from "+event, e);
			}
		}
	}



	private void addUndoableCommand(String cmd) throws Exception {
		
		try {
			if (undoStack.isEmpty()) {
				undoStack.add(0, cmd);
				IOCollectionUtils.saveCollection(DawbUtils.getDawbHome()+specName+".collection", undoStack);
			} else if (!undoStack.get(0).equals(cmd)) {
				undoStack.add(0, cmd);
				IOCollectionUtils.saveCollection(DawbUtils.getDawbHome()+specName+".collection", undoStack);
			}
			
			while (undoStack.size()>100) {
				undoStack.remove(100);
			}
		} finally {
		    resetCommandPosition();
		}
	}

	private void printNextSpecPrompt(final boolean ranCommand) {
		
		final String specPrompt = getNextSpecPrompt(ranCommand);

		safeAppend(specPrompt);
		setCaretLocation(true);			
        resetCommandPosition();
	}

	private void clearCommand() {
		int len = document.getLength()-prompt;
		if (len<0) len = 0;
		safeReplace(prompt, len, "");
	}


	private void printMessage(final String message) {
		safeAppend(message);	
	};
	
	private void safeAppend(final String message) {
		safeReplace(document.getLength(), 0, message);
	}
	
	private void safeReplace(final int offset, final int length, final String message) {
		
		if (textViewer==null) return;
		if (textViewer.getTextWidget()==null) return;
		if (textViewer.getTextWidget().isDisposed()) return;
		if (textViewer.getTextWidget().getDisplay().getThread()==null) return;
		
		if (Thread.currentThread()!=textViewer.getTextWidget().getDisplay().getThread()) {
			textViewer.getTextWidget().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
					    document.replace(offset, length, message);
					    textViewer.getTextWidget().setSelection(offset, offset);
					    
					    // Refresh last line
					    final int line = document.get().lastIndexOf("\n");
					    textViewer.getTextWidget().redrawRange(line, document.getLength()-line, false);
					} catch (BadLocationException e) {
						logger.error("Cannot insert tango error", e);
					}
				}
			});
		} else {
			try {
			    document.replace(offset, length, message);
			    textViewer.getTextWidget().setSelection(offset, offset);
			    // Refresh last line
			    final int line = document.get().lastIndexOf("\n");
			    textViewer.getTextWidget().redrawRange(line, document.getLength()-line, false);
			} catch (BadLocationException e) {
				logger.error("Cannot insert tango error", e);
			}
		}
	}
	

	private void setEditable(final boolean editable) {
		
		if (textViewer==null) return;
		if (textViewer.getTextWidget()==null) return;
		if (textViewer.getTextWidget().isDisposed()) return;
		if (textViewer.getTextWidget().getDisplay().getThread()==null) return;
		
		if (Thread.currentThread()!=textViewer.getTextWidget().getDisplay().getThread()) {
			textViewer.getTextWidget().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					textViewer.getTextWidget().setEditable(editable);
				}
			});
		} else {
		    textViewer.getTextWidget().setEditable(editable);
		}
	}

	private boolean isEditable() {
		
		if (textViewer==null) return false;
		if (textViewer.getTextWidget()==null) return false;
		if (textViewer.getTextWidget().isDisposed()) return false;
		if (textViewer.getTextWidget().getDisplay().getThread()==null) return false;
		
		final List<Boolean> out = new ArrayList<Boolean>(1);
		if (Thread.currentThread()!=textViewer.getTextWidget().getDisplay().getThread()) {
			textViewer.getTextWidget().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
                   out.add(textViewer.isEditable());
				}
			});
		} else {
			out.add(textViewer.isEditable());
		}
		return out.get(0);
	}

	private String getNextSpecPrompt(boolean incrementCmd) {
		if (incrementCmd) ++count;
		return "\n"+count+"."+specName+"> ";
	}

	@Override
	public String[] getLegalContentTypes() {
		return new String[]{IDocument.DEFAULT_CONTENT_TYPE};
	}

	@Override
	public String getContentType(int offset) {
		 return IDocument.DEFAULT_CONTENT_TYPE;
	}

	@Override
	public ITypedRegion[] computePartitioning(int offset, int length) {
		return new TypedRegion[]{new TypedRegion(offset, length, IDocument.DEFAULT_CONTENT_TYPE)};
	}

	@Override
	public ITypedRegion getPartition(int offset) {
		 return new TypedRegion(offset, 1, IDocument.DEFAULT_CONTENT_TYPE);
	}

	@Override
	public boolean isReadOnly(int offset) {
		if (document==null) return false;
		return document.getLength()==offset;
	}

	@Override
	public StyleRange[] getStyleRanges(int offset, int start) {
		
		try {
			//TODO Replace with loop
	        final List<StyleRange> result = new ArrayList<StyleRange>();	        
	    	Position[] posses = null;
			
			posses = document.getPositions(COMMENT_CATEGORY);
			if (posses!=null) {
				for (Position position : posses) {
					if (position.overlapsWith(offset, start)) {
						result.add(new StyleRange(position.offset, position.length, darkGrey, bg));
					}
				}
			}
	   
			posses = document.getPositions(SCAN_CATEGORY);
			if (posses!=null) {
				for (Position position : posses) {
					if (position.overlapsWith(offset, start)) {
						result.add(new StyleRange(position.offset, position.length, blue, bg));
					}
				}
			}
			
			posses = document.getPositions(COMMAND_CATEGORY);
			if (posses!=null) {
				for (Position position : posses) {
					if (position.overlapsWith(offset, start)) {
						result.add(new StyleRange(position.offset, position.length, darkBlue, bg));
					}
				}
			}
			
			posses = document.getPositions(PROMPT_CATEGORY);
			if (posses!=null) {
				for (Position position : posses) {
					if (position.overlapsWith(offset, start)) {
						result.add(new StyleRange(position.offset, position.length, green, bg));
					}
				}
			}
			
	        return result.toArray(new StyleRange[result.size()]);

		} catch (BadPositionCategoryException e) {
			logger.error("Cannot create styled ranges", e);
		}
		
		return new StyleRange[]{};
		
	}

	/**
	 * Just hard coded for now, fix later if adopted.
	 * @return
	 */
	private String getStartingText() {
		final StringBuilder buf = new StringBuilder();
		buf.append("\t\tWelcome to \"spec\" Remote Console - Beta\n");
		buf.append(getNextSpecPrompt(true));        
		return buf.toString();
	}

	public TextViewer getTextViewer() {
		return textViewer;
	}

	public void setTextViewer(TextViewer textViewer) {
		this.textViewer = textViewer;
	}
	
	public int getPromptLocation() {
		return prompt;
	}
	
	private void updateStartConsolePosition() {
		
		final Thread later = new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					logger.error("Position thread interupted.");
				}
				setCaretLocation(true);
			}
		}, "Init position console");
		later.start();
	}
	
	private void setCaretLocation(final boolean keepPrompt) {
		
		if (textViewer==null) return;
		if (textViewer.getTextWidget()==null) return;
		if (textViewer.getTextWidget().isDisposed()) return;
		if (textViewer.getTextWidget().getDisplay().getThread()==null) return;

		textViewer.getControl().getDisplay().syncExec(new Runnable() {
			public void run() {
				final int location = document.getLength();
                textViewer.getTextWidget().setCaretOffset(location);
                textViewer.getTextWidget().setSelection(location, location);
                if (keepPrompt) prompt = location;
			}
		});
	}

	public void resetCommandPosition() {
		stackNavigation = null;
	} 

	public synchronized void moveCommand(final int direction) {
		
		if (undoStack.isEmpty()) return;
		if (stackNavigation==null) stackNavigation = undoStack.listIterator();
		
		try {
			String cmd = "";
			if (direction == -1) {
				if (stackNavigation.hasNext()) {
					cmd = stackNavigation.next();
				} else {
					return;
				}
			} else {
				if (stackNavigation.hasPrevious()) {
					cmd = stackNavigation.previous();
				} 
			}
	
			clearCommand();
			safeAppend(cmd);
			setCaretLocation(false);
			
		} catch (ConcurrentModificationException ce) {
			stackNavigation = null;
		}
	}

}
