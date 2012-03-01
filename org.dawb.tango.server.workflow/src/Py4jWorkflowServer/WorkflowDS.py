#	"$Name:  $";
#	"$Header:  $";
#=============================================================================
#
# file :		WorkflowDS.py
#
# description : Python source for the WorkflowDS and its commands. 
#				The class is derived from Device. It represents the
#				CORBA servant object which will be accessed from the
#				network. All commands which can be executed on the
#				WorkflowDS are implemented in this file.
#
# project :	 TANGO Device Server
#
# $Author:  $
#
# $Revision:  $
#
# $Log:  $
#
# copyleft :	European Synchrotron Radiation Facility
#			   BP 220, Grenoble 38043
#			   FRANCE
#
#=============================================================================
#		  This file is generated by POGO
#	(Program Obviously used to Generate tango Object)
#
#		 (c) - Software Engineering Group - ESRF
#=============================================================================
#


import PyTango
import sys

try:
	from py4j.java_gateway import JavaGateway
except ImportError, e:
	print "Error! Py4j must be installed in Python in order for the WorkflowDS server to work."
	print "Please see http://py4j.sourceforge.net/install.html for installation instructions."
	sys.exit(1)
	
#==================================================================
#   WorkflowDS Class Description:
#
#
#==================================================================


class WorkflowDS(PyTango.Device_4Impl):

#--------- Add you global variables here --------------------------

#------------------------------------------------------------------
#	Device constructor
#------------------------------------------------------------------
	def __init__(self, cl, name):
		PyTango.Device_4Impl.__init__(self, cl, name)
		WorkflowDS.init_device(self)

#------------------------------------------------------------------
#	Device destructor
#------------------------------------------------------------------
	def delete_device(self):
		print "[Device delete_device method] for device", self.get_name()


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
#	WorkflowDS read/write attribute methods
#
#==================================================================
#------------------------------------------------------------------
#	Read Attribute Hardware
#------------------------------------------------------------------
	def read_attr_hardware(self, data):
		print "In ", self.get_name(), "::read_attr_hardware()"



#------------------------------------------------------------------
#	Read JobSuccess attribute
#------------------------------------------------------------------
	def read_JobSuccess(self, attr):
		print "In ", self.get_name(), "::read_JobSuccess()"
		
		#	Add your own code here
		
		attr_JobSuccess_read = "Hello Tango world"
		attr.set_value(attr_JobSuccess_read)


#------------------------------------------------------------------
#	Read JobFailure attribute
#------------------------------------------------------------------
	def read_JobFailure(self, attr):
		print "In ", self.get_name(), "::read_JobFailure()"
		
		#	Add your own code here
		
		attr_JobFailure_read = "Hello Tango world"
		attr.set_value(attr_JobFailure_read)


#------------------------------------------------------------------
#	Read StatisticsCollected attribute
#------------------------------------------------------------------
	def read_StatisticsCollected(self, attr):
		print "In ", self.get_name(), "::read_StatisticsCollected()"
		
		#	Add your own code here
		
		attr_StatisticsCollected_read = "Hello Tango world"
		attr.set_value(attr_StatisticsCollected_read)


#------------------------------------------------------------------
#	Read TestData attribute
#------------------------------------------------------------------
	def read_TestData(self, attr):
		print "In ", self.get_name(), "::read_TestData()"
		
		#	Add your own code here
		
		attr_TestData_read = "Hello Tango world"
		attr.set_value(attr_TestData_read)


#------------------------------------------------------------------
#	Write TestData attribute
#------------------------------------------------------------------
	def write_TestData(self, attr):
		print "In ", self.get_name(), "::write_TestData()"
		data = []
		attr.get_write_value(data)
		print "Attribute value = ", data

		#	Add your own code here



#==================================================================
#
#	WorkflowDS command methods
#
#==================================================================

#------------------------------------------------------------------
#	startJob command:
#
#	Description: 
#	argin:  DevVarStringArray	[<Module to execute>,<XML input>]
#	argout: DevString	job id
#------------------------------------------------------------------
	def startJob(self, argin):
		print "In ", self.get_name(), "::startJob()"
		#	Add your own code here
		if self.gateway is None:
			self.gateway = JavaGateway()
		self.gateway.restart_callback_server()
		self.gateway.entry_point.setPy4jWorkflowCallback(Py4jWorkflowCallback)
		return argin


#------------------------------------------------------------------
#	abort command:
#
#	Description: 
#	argin:  DevString	job id
#	argout: DevBoolean	
#------------------------------------------------------------------
	def abort(self, argin):
		print "In ", self.get_name(), "::abort()"
		#	Add your own code here
		
		return argout


#------------------------------------------------------------------
#	getJobState command:
#
#	Description: 
#	argin:  DevString	job_id
#	argout: DevString	job state
#------------------------------------------------------------------
	def getJobState(self, argin):
		print "In ", self.get_name(), "::getJobState()"
		#	Add your own code here
		
		return argout


#------------------------------------------------------------------
#	initPlugin command:
#
#	Description: 
#	argin:  DevString	plugin name
#	argout: DevString	Message
#------------------------------------------------------------------
	def initPlugin(self, argin):
		print "In ", self.get_name(), "::initPlugin()"
		#	Add your own code here
		
		return argout


#------------------------------------------------------------------
#	cleanJob command:
#
#	Description: 
#	argin:  DevString	jobId
#	argout: DevString	Message
#------------------------------------------------------------------
	def cleanJob(self, argin):
		print "In ", self.get_name(), "::cleanJob()"
		#	Add your own code here
		
		return argout


#------------------------------------------------------------------
#	collectStatistics command:
#
#	Description: 
#------------------------------------------------------------------
	def collectStatistics(self):
		print "In ", self.get_name(), "::collectStatistics()"
		#	Add your own code here


#------------------------------------------------------------------
#	getStatistics command:
#
#	Description: 
#	argout: DevString	Retrieve statistics about jobs
#------------------------------------------------------------------
	def getStatistics(self):
		print "In ", self.get_name(), "::getStatistics()"
		#	Add your own code here
		
		return argout


#------------------------------------------------------------------
#	getJobOutput command:
#
#	Description: 
#	argin:  DevString	jobId
#	argout: DevString	job output xml
#------------------------------------------------------------------
	def getJobOutput(self, argin):
		print "In ", self.get_name(), "::getJobOutput()"
		#	Add your own code here
		
		return argout


#------------------------------------------------------------------
#	getJobInput command:
#
#	Description: 
#	argin:  DevString	jobId
#	argout: DevString	job input xml
#------------------------------------------------------------------
	def getJobInput(self, argin):
		print "In ", self.get_name(), "::getJobInput()"
		#	Add your own code here
		
		return argout


#==================================================================
#
#	WorkflowDSClass class definition
#
#==================================================================
class WorkflowDSClass(PyTango.DeviceClass):

	#	Class Properties
	class_property_list = {
		}


	#	Device Properties
	device_property_list = {
		}


	#	Command definitions
	cmd_list = {
		'startJob':
			[[PyTango.DevVarStringArray, "[<Module to execute>,<XML input>]"],
			[PyTango.DevString, "job id"]],
		'abort':
			[[PyTango.DevString, "job id"],
			[PyTango.DevBoolean, ""]],
		'getJobState':
			[[PyTango.DevString, "job_id"],
			[PyTango.DevString, "job state"]],
		'initPlugin':
			[[PyTango.DevString, "plugin name"],
			[PyTango.DevString, "Message"]],
		'cleanJob':
			[[PyTango.DevString, "jobId"],
			[PyTango.DevString, "Message"]],
		'collectStatistics':
			[[PyTango.DevVoid, "nothing needed"],
			[PyTango.DevVoid, "Collect some statistics about jobs"]],
		'getStatistics':
			[[PyTango.DevVoid, "nothing needed"],
			[PyTango.DevString, "Retrieve statistics about jobs"]],
		'getJobOutput':
			[[PyTango.DevString, "jobId"],
			[PyTango.DevString, "job output xml"]],
		'getJobInput':
			[[PyTango.DevString, "jobId"],
			[PyTango.DevString, "job input xml"]],
		}


	#	Attribute definitions
	attr_list = {
		'JobSuccess':
			[[PyTango.DevString,
			PyTango.SCALAR,
			PyTango.READ]],
		'JobFailure':
			[[PyTango.DevString,
			PyTango.SCALAR,
			PyTango.READ]],
		'StatisticsCollected':
			[[PyTango.DevString,
			PyTango.SCALAR,
			PyTango.READ]],
		'TestData':
			[[PyTango.DevString,
			PyTango.SCALAR,
			PyTango.READ_WRITE],
			{
				'Polling period':100000,
			} ],
		}


#------------------------------------------------------------------
#	WorkflowDSClass Constructor
#------------------------------------------------------------------
	def __init__(self, name):
		PyTango.DeviceClass.__init__(self, name)
		self.set_type(name);
		print "In WorkflowDSClass  constructor"

#==================================================================
#
#	WorkflowDS class main method
#
#==================================================================
if __name__ == '__main__':
	try:
		py = PyTango.Util(sys.argv)
		py.add_TgClass(WorkflowDSClass, WorkflowDS, 'WorkflowDS')

		U = PyTango.Util.instance()
		U.server_init()
		U.server_run()

	except PyTango.DevFailed, e:
		print '-------> Received a DevFailed exception:', e
	except Exception, e:
		print '-------> An unforeseen exception occured....', e
