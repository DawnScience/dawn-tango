//+============================================================================
// $Source:  $
//
// project :     Tango Device Server
//
// Description:	java source code for the WorkflowExecutor class and its commands.
//              This class is derived from DeviceImpl class.
//              It represents the CORBA servant obbject which
//              will be accessed from the network. All commands which
//              can be executed on the WorkflowExecutor are implemented
//              in this file.
//
// $Author:  $
//
// $Revision:  $
//
// $Log:  $
//
// copyleft :   European Synchrotron Radiation Facility
//              BP 220, Grenoble 38043
//              FRANCE
//
//-============================================================================
//
//  		This file is generated by POGO
//	(Program Obviously used to Generate tango Object)
//
//         (c) - Software Engineering Group - ESRF
//=============================================================================


package WorkflowExecutor;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.dawb.workbench.jmx.ActorSelectedBean;
import org.dawb.workbench.jmx.IRemoteServiceProvider;
import org.dawb.workbench.jmx.IRemoteWorkbench;
import org.dawb.workbench.jmx.UserDebugBean;
import org.dawb.workbench.jmx.UserInputBean;
import org.dawb.workbench.jmx.UserPlotBean;
import org.dawb.workbench.jmx.service.IWorkflowService;
import org.dawb.workbench.jmx.service.WorkflowFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoDs.Attribute;
import fr.esrf.TangoDs.DeviceClass;
import fr.esrf.TangoDs.DeviceImpl;
import fr.esrf.TangoDs.Except;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.TangoDs.Util;


/**
 *	Class Description:
 *	This class will encapsulate the DAWB workflow executor
 *
 * @author	$Author:  $
 * @version	$Revision:  $
 */

//--------- Start of States Description ----------
/*
 *	Device States Description:
*  DevState.ON :       When the state of the device server is "ON" the workflow is idle.
 *                      A new workflow can be started with the "Start" command.
*  DevState.RUNNING :  When the device server is in the "RUNNING" state a workflow is being executed.
 *                      The workflow can be paused with the "Pause" command or aborted with the
 *                      "Abort" command.
*  DevState.STANDBY :  When the device server is in the "STANDBY" state the workflow has been paused
 *                      with the "Pause" command.
 *                      The workflow can be restarted with the "Resume" command or be aborted
 *                      with the "Abort" command.
*  DevState.FAULT :    If the device server is in the "FAULT" state no commands are allowed.
*  DevState.OPEN :     When the device server is in the "OPEN" state the workflow is halted and
 *                      is waiting for user input.
 *                      The expected user input can be retrieved with the "GetReviewData" command.
 *                      The "SetReviewData" command sends the expected input to the workflow
 *                      and it's execution is resumed.
 */
//--------- End of States Description ----------


public class WorkflowExecutor extends DeviceImpl implements TangoConst
{
	protected	int	state;

	//--------- Start of attributes data members ----------

	protected DevState[]	attr_StateAttribute_read = new DevState[1];
	protected String[]	attr_RunningActorName_read = new String[1];

//--------- End of attributes data members ----------


	//--------- Start of properties data members ----------

	/**
	 *	
	 */
	boolean	isWorking;
	/**
	 *	Absolute path to the abort script
	 */
	String	abortScriptPath;
	/**
	 *	Absolute path to the workspace
	 */
	String	workspacePath;
	/**
	 *	Relative path in the workspace to the workspace model .moml file
	 */
	String	modelPath;
	/**
	 *	Absolute path to the DAWB executable
	 */
	String	installationPath;
	/**
	 *	Array of strings of available workflow model paths
	 */
	String[]	availableModelPaths;

//--------- End of properties data members ----------


	//	Add your own data members here
	//--------------------------------------


	private static final Logger logger = LoggerFactory.getLogger(WorkflowExecutor.class);
	private String reviewData = null;

	private String parName;

	private boolean isDialog;

	private String configurationXML;

	private Map<String, String> scalarValues;
	private Map<String, String> startScalarValues;
	private Map<String, String> outScalarValues;

	protected IWorkflowService service;
	
	public String runningActorName = "No actor running";

	private boolean tangoSpecMockMode = true;
	
	private String workflowLog = "";


//=========================================================
/**
 *	Constructor for simulated Time Device Server.
 *
 *	@param	cl	The DeviceClass object
 *	@param	s	The Device name.
 */
WorkflowExecutor(DeviceClass cl, String s) throws DevFailed
	{
		super(cl,s);
		init_device();
	}
//=========================================================
/**
 *	Constructor for simulated Time Device Server.
 *
 *	@param	cl	The DeviceClass object
 *	@param	s	The Device name.
 *	@param	d	Device description.
 */
WorkflowExecutor(DeviceClass cl, String s, String d) throws DevFailed
	{
		super(cl,s,d);
		init_device();
	}

//=========================================================
/**
 *	Called when device is deleted.
 */
//=========================================================
	public void delete_device() throws DevFailed
	{
	}

//=========================================================
/**
 *	Initialize the device.
 */
//=========================================================
	public void init_device() throws DevFailed
	{
		System.out.println("WorkflowExecutor() create " + device_name);

		//	Initialise variables to default values
		//-------------------------------------------
		get_device_property();
		set_state(DevState.ON);
	}

//===================================================================
/**
 *	Read the device properties from database.
 */
//===================================================================			
	public void get_device_property() throws DevFailed
	{
		//	Initialize your default values here.
		//------------------------------------------


		//	Read device properties from database.(Automatic code generation)
		//-------------------------------------------------------------
		if (Util._UseDb==false)
			return;
		String[]	propnames = {
				"IsWorking",
				"WorkspacePath",
				"ModelPath",
				"InstallationPath",
				"AvailableModelPaths",
				"AbortScriptPath"
			};

		//	Call database and extract values
		//--------------------------------------------
		DbDatum[]	dev_prop = get_db_device().get_property(propnames);
		WorkflowExecutorClass	ds_class = (WorkflowExecutorClass)get_device_class();
		int	i = -1;
		//	Extract IsWorking value
		if (dev_prop[++i].is_empty()==false)		isWorking = dev_prop[i].extractBoolean();
		else
		{
			//	Try to get value from class property
			DbDatum	cl_prop = ds_class.get_class_property(dev_prop[i].name);
			if (cl_prop.is_empty()==false)	isWorking = cl_prop.extractBoolean();
		}

		//	Extract WorkspacePath value
		if (dev_prop[++i].is_empty()==false)		workspacePath = dev_prop[i].extractString();
		else
		{
			//	Try to get value from class property
			DbDatum	cl_prop = ds_class.get_class_property(dev_prop[i].name);
			if (cl_prop.is_empty()==false)	workspacePath = cl_prop.extractString();
		}

		//	Extract ModelPath value
		if (dev_prop[++i].is_empty()==false)		modelPath = dev_prop[i].extractString();
		else
		{
			//	Try to get value from class property
			DbDatum	cl_prop = ds_class.get_class_property(dev_prop[i].name);
			if (cl_prop.is_empty()==false)	modelPath = cl_prop.extractString();
		}

		//	Extract InstallationPath value
		if (dev_prop[++i].is_empty()==false)		installationPath = dev_prop[i].extractString();
		else
		{
			//	Try to get value from class property
			DbDatum	cl_prop = ds_class.get_class_property(dev_prop[i].name);
			if (cl_prop.is_empty()==false)	installationPath = cl_prop.extractString();
		}

		//	Extract AvailableModelPaths value
		if (dev_prop[++i].is_empty()==false)		availableModelPaths = dev_prop[i].extractStringArray();
		else
		{
			//	Try to get value from class property
			DbDatum	cl_prop = ds_class.get_class_property(dev_prop[i].name);
			if (cl_prop.is_empty()==false)	availableModelPaths = cl_prop.extractStringArray();
		}

		//	Extract AbortScriptPath value
		if (dev_prop[++i].is_empty()==false)		abortScriptPath = dev_prop[i].extractString();
		else
		{
			//	Try to get value from class property
			DbDatum	cl_prop = ds_class.get_class_property(dev_prop[i].name);
			if (cl_prop.is_empty()==false)	abortScriptPath = cl_prop.extractString();
		}

		//	End of Automatic code generation
		//-------------------------------------------------------------

	}
//=========================================================
/**
 *	Method always executed before command execution.
 */
//=========================================================
	public void always_executed_hook()
	{	
		//get_logger().info("In always_executed_hook method()");
	}

//===================================================================
/**
 *	Method called by the read_attributes CORBA operation to
 *	read device hardware
 *
 *	@param	attr_list	Vector of index in the attribute vector
 *		of attribute to be read
 */
//===================================================================			
	public void read_attr_hardware(Vector attr_list) throws DevFailed
	{
		//get_logger().info("In read_attr_hardware for "+attr_list.size()+" attribute(s)");

		//	Switch on attribute name
		//---------------------------------
	}
//===================================================================
/**
 *	Method called by the read_attributes CORBA operation to
 *	set internal attribute value.
 *
 *	@param	attr	reference to the Attribute object
 */
//===================================================================			
	public void read_attr(Attribute attr) throws DevFailed
	{
		String attr_name = attr.get_name();
		//get_logger().info("In read_attr for attribute " + attr_name);

		//	Switch on attribute name
		//---------------------------------
		if (attr_name == "StateAttribute")
		{
			attr.set_value(this.get_state());
		}
		else
		if (attr_name == "RunningActorName")
		{
			if (get_state() == DevState.ON) 
				this.runningActorName = "No actor running";
			attr.set_value(this.runningActorName);
		}
	}


//=========================================================
/**
 *	Execute command "Start" on device.
 *	The "Start" command is only allowed in the "ON" state. The command starts the
 *	execution of a workflow with and XML string as input values.
 *
 * @param	argin	Key - value pairs, first name of workflow then initial variables
 */
//=========================================================
	public void start(String[] argin) throws DevFailed
	{
		get_logger().debug("Entering start()");
		
		workflowLog = "";

		if (argin.length > 1) {
			if (argin[0].equals("modelpath")) {
				this.modelPath = argin[1];
			}
		}
		
		get_logger().info("ModelPath set to: "+this.modelPath);
		this.startScalarValues = new HashMap<String, String>(argin.length / 2);

		for (int i = 0; i < argin.length - 1; i += 2) {
			this.startScalarValues.put(argin[i], argin[i+1]);
			get_logger().info("Added start key: "+argin[i]+", value: "+argin[i+1]);
		}

		get_logger().info("Starting the workflow!");
		this.startWorkflowThread(this);

		get_logger().debug("Exiting start()");
	}


//=========================================================
/**
 *	Execute command "Abort" on device.
 *	Aborts the workflow
 *
 */
//=========================================================
	public void abort() throws DevFailed
	{
		get_logger().debug("Entering abort()");

		// ---Add your Own code to control device here ---

		// Try to stop the service
		try {
			service.stop(0);
			logger.info("Workflow stopped.");
		} catch(Exception ex) {
			logger.info("Exception caught when trying to stop the workflow:"+ex);
		} 
		
		// Try to clear the service
		try {
			service.clear();
			logger.info("Workflow cleared.");
		} catch(Exception ex) {
			logger.info("Exception caught when trying to stop the workflow:"+ex);
		}
		
		// If an abort script is configured run it
		if (abortScriptPath != null) {
			String workflowModelPath = workspacePath +"/"+modelPath;
			logger.info("Running the abort script: "+abortScriptPath+" for the model path: "+workflowModelPath);
			try {
				Thread.sleep(1000);
				Runtime.getRuntime().exec(abortScriptPath + " " + workflowModelPath);
			} catch (Exception ex) {
				logger.info("Exception caught when trying to run the abort script: "+ex);
			}
		}

		set_state(DevState.ON);

		get_logger().debug("Exiting abort()");
	}


//=========================================================
/**
 *	Execute command "Pause" on device.
 *	Pauses the execution of the workflow. Only allowed in the "RUNNING" state.
 *
 */
//=========================================================
	public void pause() throws DevFailed
	{
		get_logger().info("Entering pause()");

		// ---Add your Own code to control device here ---
		set_state(DevState.STANDBY);

		get_logger().info("Exiting pause()");
	}


//=========================================================
/**
 *	Execute command "Resume" on device.
 *	Resumes a paused workflow. Only allowed if the device server is in the "STANDBY" state.
 *
 */
//=========================================================
	public void resume() throws DevFailed
	{
		get_logger().debug("Entering resume()");

		// ---Add your Own code to control device here ---
		set_state(DevState.RUNNING);

		get_logger().debug("Exiting resume()");
	}


//=========================================================
/**
 *	Execute command "SetReviewData" on device.
 *	Sends XML data to the review actor which has stopped the workflow and its execution is resumed.
 *	Only allowed in the "OPEN" state.
 *
 * @param	argin	XML review data for the workflow
 */
//=========================================================
	public void set_review_data(String argin) throws DevFailed
	{
		get_logger().debug("Entering set_review_data()");

		// ---Add your Own code to control device here ---
		this.reviewData = argin;
		this.parName = null;
		this.configurationXML = null;
		this.isDialog = false;
		this.scalarValues = null;
		get_logger().debug("Exiting set_review_data()");
	}


//=========================================================
/**
 *	Execute command "GetReviewData" on device.
 *	Retrieves an XML string describing the expected review input and its current values.
 *	Only available in the "OPEN" state.
 *
 * @return	XML string describing the expected review input and its current values
 */
//=========================================================
	public String get_review_data() throws DevFailed
	{
		get_logger().debug("Entering get_review_data()");

		// ---Add your Own code to control device here ---
		String argout = this.configurationXML;
		get_logger().debug("argout = "+ argout);

		get_logger().debug("Exiting get_review_data()");
		return argout;
	}

	public void setStatusToOpen(String parName, boolean isDialog, String configurationXML, Map<String, String> scalarValues) throws DevFailed
	{
		get_logger().debug("Setting status to OPEN");
		this.parName = parName;
		this.isDialog = isDialog;
		this.configurationXML = configurationXML;
		this.scalarValues = scalarValues;
		this.reviewData = null;
		this.outScalarValues = null;
		set_state(DevState.OPEN);
		get_logger().debug("Status set to OPEN");
	}

	public void setStatusToRunning() throws DevFailed
	{
		get_logger().debug("Setting status to RUNNING");
		set_state(DevState.RUNNING);
		get_logger().debug("Status set to RUNNING");
	}

	public synchronized boolean hasReviewData() {

		boolean value = (this.outScalarValues != null);
		//get_logger().info("outScalarValues is "+ value);
		return value;
	}
	
//=========================================================
/**
 *	Execute command "GetScalarValuesMap" on device.
 *
 * @return	The review data as key/value pairs
 */
//=========================================================
	public String[] get_scalar_values_map() throws DevFailed
	{
		get_logger().debug("Entering get_scalar_values_map()");

		String[]	argout = new String[0];
		
		if (this.scalarValues != null) {
			int length = this.scalarValues.size() * 2;
			 argout = new String[length];
	
	
			// ---Add your Own code to control device here ---
			
			/*copy the contents of the scalarValues map into a string array, alternating keys and values*/
			Iterator<String> keys_iterator = this.scalarValues.keySet().iterator();
			
			int current_index = 0;
			while(keys_iterator.hasNext()) {
				String key = keys_iterator.next();
				argout[current_index] = key;
				argout[current_index + 1] = this.scalarValues.get(key);
				current_index += 2;
			}
	
			get_logger().debug("Exiting get_scalar_values_map()");
		}
		return argout;
	}
	
	public Map<String,String> getScalarValues() {
		return this.outScalarValues;
	}
//=========================================================
/**
 *	Execute command "SetScalarValuesMap" on device.
 *
 * @param	argin	Set value for the review data
 */
//=========================================================
	public void set_scalar_values_map(String[] argin) throws DevFailed
	{
		get_logger().debug("Entering set_scalar_values_map()");

		int noValues = argin.length / 2;
	
		set_state(DevState.RUNNING);
		
		get_logger().debug("Entering set_scalar_values_map()");
		
		// ---Add your Own code to control device here ---
		if(argin.length % 2 != 0) {
			return;
		}
		
		if (this.startScalarValues != null) {
			noValues = noValues + this.startScalarValues.size();
		}

		this.outScalarValues = new HashMap<String, String>(noValues);
			
		for (int i = 0; i < argin.length - 1; i += 2) {
			this.outScalarValues.put(argin[i], argin[i+1]);
		}
		
		if (this.startScalarValues != null) {
			this.outScalarValues.putAll(this.startScalarValues);
		}
		
		this.startScalarValues = null;

		get_logger().debug("Exiting set_scalar_values_map()");
	}


//=========================================================
/**
 *	Execute command "GetAvailableWorkflows" on device.
 *
 * @return	XML string of available workflows
 */
//=========================================================
	public String get_available_workflows() throws DevFailed
	{
		String	argout = new String();

		get_logger().debug("Entering get_available_workflows()");

		if (this.availableModelPaths == null) {
			argout = "<workflows></workflows>";
		} else {
			for (String xmlLine:this.availableModelPaths)
				argout += xmlLine + "\n";
		}
		get_logger().info("Available workflows: "+ argout);
		get_logger().debug("Exiting get_available_workflows()");
		return argout;
	}


//=========================================================
/**
 *	Execute command "IsDialog" on device.
 *
 * @return	Boolean for dialog
 */
//=========================================================
	public boolean is_dialog() throws DevFailed
	{
		get_logger().debug("Entering is_dialog()");

		get_logger().debug("IsDialog: "+ this.isDialog);

		get_logger().debug("Exiting is_dialog()");
		return this.isDialog;
	}


//=========================================================
/**
 *	Execute command "SetTangoSpecMockMode" on device.
 *	Sets the TANGO Spec mock mode on or off
 *
 * @param	argin	setTangoSpecMockMode
 */
//=========================================================
	public void set_tango_spec_mock_mode(boolean argin) throws DevFailed
	{
		get_logger().debug("Entering set_tango_spec_mock_mode()");

		get_logger().info("Setting TANGO Spec Mock Mode to: "+argin);
		this.tangoSpecMockMode = argin;
		// Store the mock mode in the workspace
		get_logger().debug("Exiting set_tango_spec_mock_mode()");
	}


//=========================================================
/**
 *	Execute command "IsTangoSpecMockMode" on device.
 *	Returns the state of TANGO Spec mock mode
 *
 * @return	
 */
//=========================================================
	public boolean is_tango_spec_mock_mode() throws DevFailed
	{
		boolean	argout = tangoSpecMockMode;

		get_logger().debug("Entering is_tango_spec_mock_mode()");

		get_logger().info("TANGO Spec Mock Mode is: "+argout);

		get_logger().debug("Exiting is_tango_spec_mock_mode()");
		return argout;
	}

//=========================================================
/**
 *	Execute command "GetWorkflowLog" on device.
 *
 * @return	Total log of the current workflow
 */
//=========================================================
	public String get_workflow_log() throws DevFailed
	{
		String	argout = new String();

		get_logger().debug("Entering get_workflow_log()");
		
		argout = read_log_file();
		
		get_logger().debug("Exiting get_workflow_log()");
		return argout;
	}

//=========================================================
/**
 *	Execute command "GetWorkflowLogIncremental" on device.
 *
 * @return	Incremental log of the current workflow 
 */
//=========================================================
	public String get_workflow_log_incremental() throws DevFailed
	{
		String	argout = new String();

		get_logger().debug("Entering get_workflow_log_incremental()");
		get_logger().debug("Length of workflowLog: "+workflowLog.length());
		
		int startIndex = workflowLog.length();
		workflowLog = read_log_file();
		int endIndex = workflowLog.length();
		get_logger().debug("Start index: "+startIndex+", end index:"+endIndex);
		argout = workflowLog.substring(startIndex, endIndex);
		
		get_logger().debug("Length of argout: "+argout.length());

		get_logger().debug("Exiting get_workflow_log_incremental()");
		return argout;
	}


	private String read_log_file() {
		String argout = "";
		//File logFile = new File(this.workspacePath+"/log/workbench.log");
		File logFile = new File(System.getProperty("user.home")+"/.dawb/workflow_executor.log");
		if (logFile.exists()) {
			try {
				get_logger().debug("Log file "+logFile.getAbsolutePath()+" exists!");
				FileReader input;
				input = new FileReader(logFile);
				BufferedReader bufRead = new BufferedReader(input);
				
				String line = bufRead.readLine();
				while (line != null){
					if (!line.contains("workflow error")) {
						argout += line + "\n";
					}
					line = bufRead.readLine();
				}
				bufRead.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else {
			get_logger().warn("Log file "+logFile.getAbsolutePath()+" doesn't exist!");
		}
		return argout;
	}


//=========================================================
/**
 *	main part for the device server class
 */
//=========================================================
	public static void main(String[] argv)
	{
		try
		{
			Util tg = Util.init(argv,"WorkflowExecutor");
			tg.server_init();

			System.out.println("Ready to accept request");

			tg.server_run();
		}

		catch (OutOfMemoryError ex)
		{
			System.err.println("Can't allocate memory !!!!");
			System.err.println("Exiting");
		}
		catch (UserException ex)
		{
			Except.print_exception(ex);
			
			System.err.println("Received a CORBA user exception");
			System.err.println("Exiting");
		}
		catch (SystemException ex)
		{
			Except.print_exception(ex);
			
			System.err.println("Received a CORBA system exception");
			System.err.println("Exiting");
		}
		
		System.exit(-1);		
	}
	
	public static class TangoServerServiceProvider implements IRemoteServiceProvider {

		private WorkflowExecutor workflowExecutorInstance = null;
		
		public TangoServerServiceProvider(WorkflowExecutor workflowExecutor) {
			super();
			this.workflowExecutorInstance = workflowExecutor;
		}

		@Override
		public IRemoteWorkbench getRemoteWorkbench() throws Exception {
			return new TangoServerRemoteWorkbench(workflowExecutorInstance);
		}

		@Override
		public int getStartPort() {
			return 21701;
		}

		@Override
		public String getWorkspacePath() {
			String workspacePath = this.workflowExecutorInstance.workspacePath;
			logger.info("Workspace path:"+workspacePath);
			return workspacePath;
		}

		@Override
		public String getModelPath() {
			String modelPath = this.workflowExecutorInstance.workspacePath + "/" + this.workflowExecutorInstance.modelPath;
			logger.info("Model path:"+modelPath);
			return modelPath;
		}

		@Override
		public String getInstallationPath() {
			logger.info("Installation path:"+this.workflowExecutorInstance.installationPath);
			return this.workflowExecutorInstance.installationPath;
		}

		@Override
		public boolean getTangoSpecMockMode() {
			logger.info("TANGO Spec Mock Mode:"+this.workflowExecutorInstance.tangoSpecMockMode);
			return this.workflowExecutorInstance.tangoSpecMockMode;
		}

		@Override
		public boolean getServiceTerminate() {
			// Always force termination!
			return true;
		}

	}

	public static class TangoServerRemoteWorkbench implements IRemoteWorkbench {

		private Map<String, Object> mockValues;
		private WorkflowExecutor workflowExecutorInstance;

		public TangoServerRemoteWorkbench(
				WorkflowExecutor workflowExecutorInstance) {
			super();
			this.workflowExecutorInstance = workflowExecutorInstance;
		}

		@Override
		public boolean openFile(String fullPath) {
			logger.info("File Open Requested");
			logger.info("Path "+fullPath);
			return true;
		}

		@Override
		public boolean monitorDirectory(String fullPath, boolean startMonitoring) {
			logger.info("Directory Monitor Requested");
			logger.info("Path "+fullPath);
			return true;
		}

		@Override
		public boolean refresh(String projectName, String resourcePath) {
			logger.info("Refresh Requested");
			logger.info("Project "+projectName+"; path "+resourcePath);
			return true;
		}

		@Override
		public boolean showMessage(String title, String message, int type) {
			logger.info("Show Message Requested");
			logger.info("Title "+title+"; message "+message);
			String typeString = null;
			switch (type) {
			case MessageDialog.ERROR:       typeString = "error";       break;
			case MessageDialog.WARNING:     typeString = "warning";     break;
			case MessageDialog.INFORMATION: typeString = "info";        break;
			}
			String xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
			xmlMessage += "<message>\n";
			xmlMessage += "  <type>"+typeString+"</type>\n";
			xmlMessage += "  <text>"+message+"</text>\n";
			xmlMessage += "</message>\n";
			logger.debug("XML message: "+xmlMessage);
			try {
				// Sleep 0.5s in order to allow polling with a frequency > 1 ms...
				Thread.sleep(500);
				this.workflowExecutorInstance.setStatusToOpen(title, true, xmlMessage, null);
				while (! this.workflowExecutorInstance.hasReviewData() && this.workflowExecutorInstance.get_state() == DevState.OPEN) {
					Thread.sleep(500);// User is pressing ok...
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (DevFailed e) {
				e.printStackTrace();
			}

			return true;
		}

		@Override
		public void logStatus(String pluginId, String message, Throwable throwable) {
			logger.error(message, throwable);
		}

		@Override
		public Map<String, String> createUserInput(final UserInputBean bean) throws Exception {
			logger.debug("Create User Input Requested");
			logger.debug("Actor "+bean.getPartName());
			final Map<String,String> ret = new HashMap<String,String>(0);
			try {
				// TODO bean also has isSilent to know if user configured actor
				// to be silent (i.e. no UI) when it runs. On the Java client
				// this simply means there is no dialog shown and the default
				// values are used.
				// Sleep 0.5s in order to allow polling with a frequency > 1 ms...
				Thread.sleep(500);
				this.workflowExecutorInstance.setStatusToOpen(bean.getPartName(),
															bean.isDialog(), 
															bean.getConfigurationXML(),
															bean.getScalar());
				while (! this.workflowExecutorInstance.hasReviewData() && this.workflowExecutorInstance.get_state() == DevState.OPEN) {
					Thread.sleep(500);// User is pressing ok...
				}
				if (this.workflowExecutorInstance.get_state() == DevState.RUNNING) {
					ret.putAll(this.workflowExecutorInstance.getScalarValues());
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (DevFailed e) {
				e.printStackTrace();
			}

			return ret;
		}

		@Override
		public boolean setActorSelected(final ActorSelectedBean bean) throws Exception {
			
			logger.debug("Select Actor Requested");
			logger.debug("Actor "+bean.getActorName()+"; isSelected "+bean.isSelected());
			if (bean.isSelected())
				this.workflowExecutorInstance.runningActorName = bean.getActorName();
			return true;
		}

		@Override
		public void setMockMotorValue(String motorName, Object value) {
			if (mockValues==null) mockValues = new HashMap<String,Object>(3);
			mockValues.put(motorName, value);
		}

		@Override
		public Object getMockMotorValue(String motorName) {
			if (mockValues==null) return null;
			return mockValues.get(motorName);
		}

		@Override
		public void notifyMockCommand(String motorName, String message, String cmd) {
			logger.debug("Mock Notify Requested");
			logger.debug("Motor "+motorName+"; message "+message);
		}

		@Override
		public void executionStarted() {
			// No need to implement for now
		}

		@Override
		public void executionTerminated(int returnCode) {
			// No need to implement for now
		}

		@Override
		public UserPlotBean createPlotInput(UserPlotBean bean) throws Exception {
			throw new Exception("Plot input method is not supported on "+getClass().getName());
		}

		@Override
		public UserDebugBean debug(UserDebugBean bean) throws Exception {
			throw new Exception("Debug method is not supported on "+getClass().getName());
		}

	}


	/**
	 * Method to show how to run above in thread.
	 */
	private void startWorkflowThread(final WorkflowExecutor instance) {
		IWorkflowService serviceReference = null;
		logger.debug("Before creating thread");
		final Thread workflowThread = new Thread(new Runnable() {
			@Override
			public void run() {
				
				try {
			        // Create a new service each time 
					service  = WorkflowFactory.createWorkflowService(new TangoServerServiceProvider(instance));
					final Process          workflow = service.start();
					
					try {
						// 1. Set tango state to running.
						//...
						set_state(DevState.RUNNING);
						
						// 2. Wait until workflow is finished.
						workflow.waitFor();
	
					} finally {
						// 3. Set tango state to not running.
						//...
						set_state(DevState.ON);
						
						// 4. Release any memory used by the object and close agent JMX service
						service.clear();
					}
						
					
				} catch (Exception ne) {
					logger.error("Cannot run workflow using Amoeba", ne);
				}
	
			}
		});
		logger.debug("After creating thread");
		
		// Always name your threads, this makes debugging easier
		workflowThread.setName("Workflow Thread");

		// Use start method to start a new thread.
		workflowThread.start();
		
	}

}	

//--------------------------------------------------------------------------
/* end of $Source: /cvsroot/tango-cs/tango/tools/pogo/templates/java/DevServ.java,v $ */
