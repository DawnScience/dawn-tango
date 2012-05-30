#	"$Name:  $";
#	"$Header:  $";
#=============================================================================
#
# file :        WorkflowExecutor.py
#
# description : Python source for the WorkflowExecutor and its commands. 
#                The class is derived from Device. It represents the
#                CORBA servant object which will be accessed from the
#                network. All commands which can be executed on the
#                WorkflowExecutor are implemented in this file.
#
# project :     TANGO Device Server
#
# $Author:  $
#
# $Revision:  $
#
# $Log:  $
#
# copyleft :    European Synchrotron Radiation Facility
#               BP 220, Grenoble 38043
#               FRANCE
#
#=============================================================================
#  		This file is generated by POGO
#	(Program Obviously used to Generate tango Object)
#
#         (c) - Software Engineering Group - ESRF
#=============================================================================
#


import PyTango
import sys


#==================================================================
#   WorkflowExecutor Class Description:
#
#         This class will encapsulate the DAWB workflow executor
#
#==================================================================
# 	Device States Description:
#
#   DevState.ON :       When the state of the device server is "ON" the workflow is idle.
#                       A new workflow can be started with the "Start" command.
#   DevState.RUNNING :  When the device server is in the "RUNNING" state a workflow is being executed.
#                       The workflow can be paused with the "Pause" command or aborted with the
#                       "Abort" command.
#   DevState.STANDBY :  When the device server is in the "STANDBY" state the workflow has been paused
#                       with the "Pause" command.
#                       The workflow can be restarted with the "Resume" command or be aborted
#                       with the "Abort" command.
#   DevState.FAULT :    If the device server is in the "FAULT" state no commands are allowed.
#   DevState.OPEN :     When the device server is in the "OPEN" state the workflow is halted and
#                       is waiting for user input.
#                       The expected user input can be retrieved with the "GetReviewData" command.
#                       The "SetReviewData" command sends the expected input to the workflow
#                       and it's execution is resumed.
#==================================================================


class WorkflowExecutor(PyTango.Device_4Impl):

#--------- Add you global variables here --------------------------

#------------------------------------------------------------------
#	Device constructor
#------------------------------------------------------------------
	def __init__(self,cl, name):
		PyTango.Device_4Impl.__init__(self,cl,name)
		WorkflowExecutor.init_device(self)

#------------------------------------------------------------------
#	Device destructor
#------------------------------------------------------------------
	def delete_device(self):
		print "[Device delete_device method] for device",self.get_name()


#------------------------------------------------------------------
#	Device initialization
#------------------------------------------------------------------
	def init_device(self):
		print "In ", self.get_name(), "::init_device()"
		self.set_state(PyTango.DevState.ON)
		self.get_device_properties(self.get_device_class())

#------------------------------------------------------------------
#	Always excuted hook method
#------------------------------------------------------------------
	def always_executed_hook(self):
		print "In ", self.get_name(), "::always_excuted_hook()"

#==================================================================
#
#	WorkflowExecutor read/write attribute methods
#
#==================================================================
#------------------------------------------------------------------
#	Read Attribute Hardware
#------------------------------------------------------------------
	def read_attr_hardware(self,data):
		print "In ", self.get_name(), "::read_attr_hardware()"



#------------------------------------------------------------------
#	Read StateAttribute attribute
#------------------------------------------------------------------
	def read_StateAttribute(self, attr):
		print "In ", self.get_name(), "::read_StateAttribute()"
		
		#	Add your own code here
		
		#attr_StateAttribute_read = 1
		attr_StateAttribute_read = PyTango.DevState.ON
		attr.set_value(attr_StateAttribute_read)


#------------------------------------------------------------------
#	Read RunningActorName attribute
#------------------------------------------------------------------
	def read_RunningActorName(self, attr):
		print "In ", self.get_name(), "::read_RunningActorName()"
		
		#	Add your own code here
		
		attr_RunningActorName_read = "Hello Tango world"
		attr.set_value(attr_RunningActorName_read)



#==================================================================
#
#	WorkflowExecutor command methods
#
#==================================================================

#------------------------------------------------------------------
#	Start command:
#
#	Description: The "Start" command is only allowed in the "ON" state. The command starts the
#                execution of a workflow with and XML string as input values.
#                
#	argin:  DevVarStringArray	Key - value pairs, first name of workflow then initial variables
#------------------------------------------------------------------
	def Start(self, argin):
		print "In ", self.get_name(), "::Start()"
		#	Add your own code here


#---- Start command State Machine -----------------
	def is_Start_allowed(self):
		if self.get_state() in [PyTango.DevState.RUNNING,
		                        PyTango.DevState.STANDBY,
		                        PyTango.DevState.FAULT]:
			#	End of Generated Code
			#	Re-Start of Generated Code
			return False
		return True


#------------------------------------------------------------------
#	Abort command:
#
#	Description: Aborts the workflow
#                
#------------------------------------------------------------------
	def Abort(self):
		print "In ", self.get_name(), "::Abort()"
		#	Add your own code here


#------------------------------------------------------------------
#	Pause command:
#
#	Description: Pauses the execution of the workflow. Only allowed in the "RUNNING" state.
#                
#------------------------------------------------------------------
	def Pause(self):
		print "In ", self.get_name(), "::Pause()"
		#	Add your own code here


#---- Pause command State Machine -----------------
	def is_Pause_allowed(self):
		if self.get_state() in [PyTango.DevState.ON,
		                        PyTango.DevState.STANDBY,
		                        PyTango.DevState.FAULT]:
			#	End of Generated Code
			#	Re-Start of Generated Code
			return False
		return True


#------------------------------------------------------------------
#	Resume command:
#
#	Description: Resumes a paused workflow. Only allowed if the device server is in the "STANDBY" state.
#                
#------------------------------------------------------------------
	def Resume(self):
		print "In ", self.get_name(), "::Resume()"
		#	Add your own code here


#---- Resume command State Machine -----------------
	def is_Resume_allowed(self):
		if self.get_state() in [PyTango.DevState.ON,
		                        PyTango.DevState.RUNNING,
		                        PyTango.DevState.FAULT]:
			#	End of Generated Code
			#	Re-Start of Generated Code
			return False
		return True


#------------------------------------------------------------------
#	GetReviewData command:
#
#	Description: Retrieves an XML string describing the expected review input and its current values.
#                Only available in the "OPEN" state.
#                
#	argout: DevString	XML string describing the expected review input and its current values
#------------------------------------------------------------------
	def GetReviewData(self):
		print "In ", self.get_name(), "::GetReviewData()"
		#	Add your own code here
		
		return argout


#---- GetReviewData command State Machine -----------------
	def is_GetReviewData_allowed(self):
		if self.get_state() in [PyTango.DevState.ON,
		                        PyTango.DevState.RUNNING,
		                        PyTango.DevState.STANDBY,
		                        PyTango.DevState.FAULT]:
			#	End of Generated Code
			#	Re-Start of Generated Code
			return False
		return True


#------------------------------------------------------------------
#	GetScalarValuesMap command:
#
#	Description: 
#	argout: DevVarStringArray	The review data as key/value pairs
#------------------------------------------------------------------
	def GetScalarValuesMap(self):
		print "In ", self.get_name(), "::GetScalarValuesMap()"
		#	Add your own code here
		
		return argout


#---- GetScalarValuesMap command State Machine -----------------
	def is_GetScalarValuesMap_allowed(self):
		if self.get_state() in [PyTango.DevState.ON,
		                        PyTango.DevState.RUNNING,
		                        PyTango.DevState.STANDBY,
		                        PyTango.DevState.FAULT]:
			#	End of Generated Code
			#	Re-Start of Generated Code
			return False
		return True


#------------------------------------------------------------------
#	SetScalarValuesMap command:
#
#	Description: 
#	argin:  DevVarStringArray	Set value for the review data
#------------------------------------------------------------------
	def SetScalarValuesMap(self, argin):
		print "In ", self.get_name(), "::SetScalarValuesMap()"
		#	Add your own code here


#------------------------------------------------------------------
#	GetAvailableWorkflows command:
#
#	Description: 
#	argout: DevString	XML string of available workflows
#------------------------------------------------------------------
	def GetAvailableWorkflows(self):
		print "In ", self.get_name(), "::GetAvailableWorkflows()"
		#	Add your own code here
		
		return argout


#------------------------------------------------------------------
#	IsDialog command:
#
#	Description: 
#	argout: DevBoolean	Boolean for dialog
#------------------------------------------------------------------
	def IsDialog(self):
		print "In ", self.get_name(), "::IsDialog()"
		#	Add your own code here
		
		return argout


#---- IsDialog command State Machine -----------------
	def is_IsDialog_allowed(self):
		if self.get_state() in [PyTango.DevState.ON,
		                        PyTango.DevState.RUNNING,
		                        PyTango.DevState.STANDBY,
		                        PyTango.DevState.FAULT]:
			#	End of Generated Code
			#	Re-Start of Generated Code
			return False
		return True


#------------------------------------------------------------------
#	SetTangoSpecMockMode command:
#
#	Description: Sets the TANGO Spec mock mode on or off
#                
#	argin:  DevBoolean	setTangoSpecMockMode
#------------------------------------------------------------------
	def SetTangoSpecMockMode(self, argin):
		print "In ", self.get_name(), "::SetTangoSpecMockMode()"
		#	Add your own code here


#---- SetTangoSpecMockMode command State Machine -----------------
	def is_SetTangoSpecMockMode_allowed(self):
		if self.get_state() in [PyTango.DevState.RUNNING,
		                        PyTango.DevState.STANDBY,
		                        PyTango.DevState.FAULT,
		                        PyTango.DevState.OPEN]:
			#	End of Generated Code
			#	Re-Start of Generated Code
			return False
		return True


#------------------------------------------------------------------
#	IsTangoSpecMockMode command:
#
#	Description: Returns the state of TANGO Spec mock mode
#                
#	argout: DevBoolean	
#------------------------------------------------------------------
	def IsTangoSpecMockMode(self):
		print "In ", self.get_name(), "::IsTangoSpecMockMode()"
		#	Add your own code here
		
		return argout


#------------------------------------------------------------------
#	GetWorkflowLog command:
#
#	Description: 
#	argout: DevString	Total log of the current workflow
#------------------------------------------------------------------
	def GetWorkflowLog(self):
		print "In ", self.get_name(), "::GetWorkflowLog()"
		#	Add your own code here
		
		return argout


#------------------------------------------------------------------
#	GetWorkflowLogIncremental command:
#
#	Description: 
#	argout: DevString	Incremental log of the current workflow 
#------------------------------------------------------------------
	def GetWorkflowLogIncremental(self):
		print "In ", self.get_name(), "::GetWorkflowLogIncremental()"
		#	Add your own code here
		
		return argout


#==================================================================
#
#	WorkflowExecutorClass class definition
#
#==================================================================
class WorkflowExecutorClass(PyTango.DeviceClass):

	#	Class Properties
	class_property_list = {
		}


	#	Device Properties
	device_property_list = {
		'IsWorking':
			[PyTango.DevBoolean,
			"",
			[] ],
		'WorkspacePath':
			[PyTango.DevString,
			"Absolute path to the workspace",
			[] ],
		'ModelPath':
			[PyTango.DevString,
			"Relative path in the workspace to the workspace model .moml file",
			[] ],
		'InstallationPath':
			[PyTango.DevString,
			"Absolute path to the DAWB executable",
			[] ],
		'AvailableModelPaths':
			[PyTango.DevVarStringArray,
			"Array of strings of available workflow model paths",
			[] ],
		}


	#	Command definitions
	cmd_list = {
		'Start':
			[[PyTango.DevVarStringArray, "Key - value pairs, first name of workflow then initial variables"],
			[PyTango.DevVoid, ""]],
		'Abort':
			[[PyTango.DevVoid, ""],
			[PyTango.DevVoid, ""]],
		'Pause':
			[[PyTango.DevVoid, ""],
			[PyTango.DevVoid, ""]],
		'Resume':
			[[PyTango.DevVoid, ""],
			[PyTango.DevVoid, ""]],
		'GetReviewData':
			[[PyTango.DevVoid, ""],
			[PyTango.DevString, "XML string describing the expected review input and its current values"]],
		'GetScalarValuesMap':
			[[PyTango.DevVoid, ""],
			[PyTango.DevVarStringArray, "The review data as key/value pairs"]],
		'SetScalarValuesMap':
			[[PyTango.DevVarStringArray, "Set value for the review data"],
			[PyTango.DevVoid, ""]],
		'GetAvailableWorkflows':
			[[PyTango.DevVoid, ""],
			[PyTango.DevString, "XML string of available workflows"]],
		'IsDialog':
			[[PyTango.DevVoid, ""],
			[PyTango.DevBoolean, "Boolean for dialog"]],
		'SetTangoSpecMockMode':
			[[PyTango.DevBoolean, "setTangoSpecMockMode"],
			[PyTango.DevVoid, ""]],
		'IsTangoSpecMockMode':
			[[PyTango.DevVoid, ""],
			[PyTango.DevBoolean, ""]],
		'GetWorkflowLog':
			[[PyTango.DevVoid, ""],
			[PyTango.DevString, "Total log of the current workflow"]],
		'GetWorkflowLogIncremental':
			[[PyTango.DevVoid, ""],
			[PyTango.DevString, "Incremental log of the current workflow "]],
		}


	#	Attribute definitions
	attr_list = {
		'StateAttribute':
			[[PyTango.DevState,
			PyTango.SCALAR,
			PyTango.READ]],
		'RunningActorName':
			[[PyTango.DevString,
			PyTango.SCALAR,
			PyTango.READ]],
		}


#------------------------------------------------------------------
#	WorkflowExecutorClass Constructor
#------------------------------------------------------------------
	def __init__(self, name):
		PyTango.DeviceClass.__init__(self, name)
		self.set_type(name);
		print "In WorkflowExecutorClass  constructor"

#==================================================================
#
#	WorkflowExecutor class main method
#
#==================================================================
if __name__ == '__main__':
	try:
		py = PyTango.Util(sys.argv)
		py.add_TgClass(WorkflowExecutorClass,WorkflowExecutor,'WorkflowExecutor')

		U = PyTango.Util.instance()
		U.server_init()
		U.server_run()

	except PyTango.DevFailed,e:
		print '-------> Received a DevFailed exception:',e
	except Exception,e:
		print '-------> An unforeseen exception occured....',e