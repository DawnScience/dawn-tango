/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.passerelle.actors.hardware;

import java.util.List;

import org.dawb.common.ui.preferences.CommonUIPreferenceConstants;
import org.dawb.passerelle.common.actors.AbstractDataMessageSource;
import org.dawb.passerelle.common.message.IVariable;
import org.dawb.passerelle.common.message.IVariable.VARIABLE_TYPE;
import org.dawb.passerelle.common.message.MessageUtils;
import org.dawb.passerelle.common.message.Variable;
import org.dawb.tango.extensions.TangoUtils;
import org.dawb.tango.extensions.editors.SharedMemoryUtils;
import org.dawb.tango.extensions.editors.preferences.SharedConstants;
import org.dawb.tango.extensions.factory.TangoConnection;
import org.dawb.tango.extensions.factory.TangoConnectionFactory;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.analysis.api.message.DataMessageComponent;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TerminationException;
import com.isencia.passerelle.message.ManagedMessage;

import fr.esrf.TangoApi.DeviceData;

/**
 * This actor reads arrays from shared memory and sends them
 * into the pipeline.
 * 
 * We reply on preferences set in the GUI for the workspace. We
 * read them and this configures tango.
 * 
 * @author gerring
 *
 */
public class SharedMemorySource extends AbstractDataMessageSource {
	
	private static final Logger logger = LoggerFactory.getLogger(SharedMemorySource.class);
	
	private static final String[] DATA_CHOICES = new String[]{"1D List", "2D"};
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5335673681296652024L;
	
	private StringParameter specTangoUri,variable,dataType,outputName,specName;
	
	/**
	 * The data will not be fired into the pipeline greater than this rate.
	 */
	private Parameter sourceFreq;

	/**
	 * After this period the source will exit and stop checking
	 */
	private Parameter inactiveFreq;
	
	/**
	 * The chunk size used when reading a memory chunk.
	 */
	private Parameter chunkSize;
	
	public SharedMemorySource(CompositeEntity container, String name) throws Exception {	
		
		super(container, name);
		
		final ScopedPreferenceStore store = new ScopedPreferenceStore(new InstanceScope(),"org.dawb.tango.extensions");

		this.specTangoUri = new StringParameter(this, "Spec Tango URI");
		registerExpertParameter(specTangoUri);
        specTangoUri.setExpression(store.getString(SharedConstants.SPEC_SHARED));
        // Default to last uri
       
		this.variable = new StringParameter(this, "Spec Variable") {
			private static final long serialVersionUID = -1902977727142062610L;
			public String[] getChoices() {
				try {
					return getMemoryChoices();
				} catch (Exception e) {
					logger.error("Cannot get memory names", e);
					return new String[]{"Error - shared memory choices invalid!"};
				}
			}
		};
		registerConfigurableParameter(variable);
		
		this.outputName = new StringParameter(this, "Output Name");
		registerConfigurableParameter(outputName);
		outputName.setExpression("x");
    
		this.sourceFreq = new Parameter(this, "Source Frequency", new IntToken(store.getInt(SharedConstants.MON_FREQ)));
		registerConfigurableParameter(sourceFreq);

		this.inactiveFreq = new Parameter(this, "Inactive After", new IntToken(-1));
		registerExpertParameter(inactiveFreq);

		this.chunkSize = new Parameter(this, "Chunk Size", new IntToken(store.getInt(SharedConstants.CHUNK_SIZE)));
		registerExpertParameter(chunkSize);
		
		this.specName = new StringParameter(this, "Spec Name");
		registerConfigurableParameter(specName);
		IPreferenceStore commonStore =  new ScopedPreferenceStore(new InstanceScope(),"org.dawb.common.ui");
		specName.setExpression(commonStore.getString(CommonUIPreferenceConstants.SPEC_NAME));
		
		this.dataType = new StringParameter(this, "Data Type") {
			private static final long serialVersionUID = -751428263040559946L;
			public String[] getChoices() {
				return DATA_CHOICES;
			}
		};
		dataType.setExpression(DATA_CHOICES[0]);
		registerConfigurableParameter(dataType);
       
	}

	private TangoConnection connection = null;

	private ManagedMessage triggerMsg;
	
	protected void acceptTriggerMessage(ManagedMessage triggerMsg) {
		this.triggerMsg = triggerMsg;
	}
	
	@Override
	protected ManagedMessage getDataMessage() throws ProcessingException {
		
		try {
			
			if (connection==null) connection = createConnection();
			
			final String memoryName = variable.getExpression();
			
			final boolean isData = isData(memoryName);
			if (!isData) return null;
			
			final PlotType plotType = DATA_CHOICES[0].equals(dataType.getExpression())
			                        ? PlotType.XY : PlotType.IMAGE;
			
			final int cSize = ((IntToken)chunkSize.getToken()).intValue();
			
			final List<Dataset> sets = SharedMemoryUtils.getSharedMemoryValue(connection, 
					                                                                  memoryName, 
					                                                                  cSize, 
					                                                                  plotType);
			
			final DataMessageComponent  ret  = new DataMessageComponent();
			final String               name  = outputName.getExpression();     
			if (plotType == PlotType.IMAGE) {
				final Dataset image = sets.get(0);
				image.setName(name);
				ret.addList(name, sets.get(0));
			} else {
				int i = 1;
				for (Dataset a : sets) {
					
					a.setName(name+i);
					ret.addList(name+i, a);
					i++;
				}
			}
			
			if (triggerMsg!=null) {
				try {
					final DataMessageComponent c = MessageUtils.coerceMessage(triggerMsg);
					ret.addScalar(c.getScalar());
				} catch (Exception ignored) {
					logger.info("Trigger for "+getName()+" is not DataMessageComponent, no data added.");
				}
			}
			
			return MessageUtils.getDataMessage(ret, null);
			
		} catch (Exception ne) {
			throw createDataMessageException("Cannot extract shared memory", ne);
		
		}
	}
	
	/**
	 * Waits until a timeout occurs. A timeout of 0 or less means wait until stop has been pressed.
	 * @param memoryName
	 * @return
	 * @throws Exception
	 */
	private boolean isData(String memoryName) throws Exception {

		final DeviceData  out = new DeviceData();
		out.insert(new String[]{this.specName.getExpression(), memoryName});
		
		DeviceData ret = connection.executeCommand("IsUpdated", out, false);
		
		int totalTime = ((IntToken)inactiveFreq.getToken()).intValue();
		
		// 0 or less means wait for ever
		if (totalTime<0) totalTime = Integer.MAX_VALUE;
		int waitedTime  = 0;
		while (ret.extractLong()!=1 && waitedTime<totalTime && !isFinishRequested()) { 
			try {
				Thread.sleep(100);
				ret = connection.executeCommand("IsUpdated", out, false);
			} finally {
				if (totalTime!=Integer.MAX_VALUE) waitedTime+=100;
			}
		}
		
		if (ret.extractLong()==1) {
			return true;
		} else {
			logger.warn("The actor '"+getDisplayName()+"' has timed out after "+totalTime+" ms.");
			return false;
		}
	}

	protected boolean doPostFire() throws ProcessingException {
		try {
			final int freq = ((IntToken)sourceFreq.getToken()).intValue();
			if (!isFinishRequested()) Thread.sleep(freq);
			
		} catch (Exception e) {
			throw new ProcessingException("Cannot wait for source frequency time after last data!", this, e);
		}
	    return super.doPostFire();
	}
	
	protected void doWrapUp() throws TerminationException {
		try {
		    if (connection!=null) connection.dispose();
		} catch (Exception ne) {
			throw new TerminationException("Cannot close tango connection", this, ne);
		} finally {
			super.doWrapUp();
		}
	}
	
	@Override
	protected String getExtendedInfo() {
		return "Actor to monitor shared memory.";
	}
	

	private String[] getMemoryChoices() throws Exception {
		
		TangoConnection connection = null;
		try {
			connection = createConnection();
			final List<String> names = SharedMemoryUtils.getSharedNames(connection);
			return names.toArray(new String[names.size()]);
			
		} catch (Exception ne) {
			logger.error("Cannot read shared memory variables!", ne);
			return new String[]{ne.getMessage()};
		} finally {
			if (connection!=null) connection.dispose();
		}
	}

	private TangoConnection createConnection() throws Exception {
		
    	final String hardwareURI = TangoUtils.getHardwareAddress(specTangoUri.getExpression());
    	try {
		    return TangoConnectionFactory.openCommandConnection(hardwareURI);
    	} catch (fr.esrf.TangoApi.ConnectionFailed cnf) {
    		logger.error("Cannot run '"+getDisplayName()+"' because the tango connection cannot be made!\n"+cnf.errors[0].desc);
    	    throw cnf;
    	}
	}
	
	@Override
	public List<IVariable> getOutputVariables() {
		
		try {
		    final List<IVariable>    ret  = super.getOutputVariables();
			final PlotType plotType = DATA_CHOICES[0].equals(dataType.getExpression())
                                    ? PlotType.XY : PlotType.IMAGE;

			if (plotType==PlotType.IMAGE) {
				ret.add(new Variable(outputName.getExpression(), VARIABLE_TYPE.ARRAY, null, Dataset.class));
			} else {
				final int cSize = ((IntToken)chunkSize.getToken()).intValue();
				for (int i = 1; i <= cSize; i++) {
					ret.add(new Variable(outputName.getExpression()+i, VARIABLE_TYPE.ARRAY, null, Dataset.class));
				}
			}
			return ret;
			
		} catch (Exception e) {
			logger.error("Cannot read variables", e);
			return null;
		}

	}

	@Override
	protected boolean mustWaitForTrigger() {
		if (triggerMsg!=null) return false;
		return trigger.getWidth()>0;
	}
	
}
