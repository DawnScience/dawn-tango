

import os, time, threading, subprocess


try:
    from py4j.java_gateway import JavaGateway
except ImportError, e:
    print "Error! Py4j must be installed in Python in order for the WorkflowDS server to work."
    print "Please see http://py4j.sourceforge.net/install.html for installation instructions."
    raise

if not "JMX_LOC" in os.environ.keys():
    print "Error! The environment variable JMX_LOC must be defined!"
    raise Exception("JMX_LOC not defined")


class WorkflowProxyThread(threading.Thread):
    
    def __init__(self, _workflowDS=None):
        threading.Thread.__init__(self)
        self._workflowDS = _workflowDS
        self._bShutdown = False
        self._subprocess = None
        self._iPID = None
        self._gateway_client = None
        #self._strInstallationPath = "/sware/isdd/soft/dawb/nightly/linux_x64/dawb/dawb"
        self._strInstallationPath = "/opt/dawb/dawb"
        self._strDataInput = ""
        self._strDataOutput = None
        self._strModelPath = None
        self._strJobId = None
        self._strActorSelected = None
        
        
    def run(self):
        try:
            # Start the java gateway server
            self.startJavaGatewayServer()   
            # Start the job
            self._gateway_client.entry_point.setPy4jWorkflowCallback(self)
            self._gateway_client.entry_point.setWorkspacePath(self._strWorkspacePath)
            self._gateway_client.entry_point.setModelPath(self._strModelPath)
            self._gateway_client.entry_point.setInstallationPath(self._strInstallationPath)
            self._gateway_client.entry_point.setServiceTerminate(False)
            self._gateway_client.entry_point.setTangoSpecMode(False)
            self._gateway_client.entry_point.runWorkflow()
            self._gateway_client.entry_point.synchronizeWorkflow()
            if self._workflowDS is not None:
                self._workflowDS.setJobOutput(self.getDataOutput())
                self._workflowDS.set_jobSuccess(self._strJobId)
        except Exception, e:
            print e
            if self._workflowDS is not None:
                self._workflowDS.set_jobFailure(self._strJobId)            
        finally:
            self.shutdownJavaGatewayServer()        
            
        
    
    def setWorkspacePath(self, _strWorkspacePath):
        self._strWorkspacePath = _strWorkspacePath
    
    
    def startJob(self, _strModelPath, _strJobArg):
        self._strModelPath = _strModelPath
        strJobName = os.path.splitext(os.path.basename(self._strModelPath))[0]
        self._strDataInput = _strJobArg
        self._strJobId = "job_%s_%s" % (strJobName, time.strftime("%Y%H%M%S"))
        self.start()
         
    
    def synchronize(self, _fTimeOut=30.0):
        self.join(_fTimeOut)


    
    def startJavaGatewayServer(self):
        self.shutdownJavaGatewayServer()
        time.sleep(0.1)
        strPathToDawbJmx = os.path.join(os.environ["JMX_LOC"], "org.dawb.workbench.jmx")
        listClasspath = []
        listClasspath.append(os.path.join(strPathToDawbJmx, "py4j0.7.jar"))
        listClasspath.append(os.path.join(strPathToDawbJmx, "com.springsource.slf4j.api-1.5.6.jar"))
        listClasspath.append(os.path.join(strPathToDawbJmx, "com.springsource.ch.qos.logback.classic-0.9.15.jar"))
        listClasspath.append(os.path.join(strPathToDawbJmx, "com.springsource.ch.qos.logback.core-0.9.15.jar"))
        listClasspath.append(os.path.join(strPathToDawbJmx, "bin"))
        listClasspath.append("/tmp/123456")
        args = []
        strClasspathTotal = ""
        for strClasspath in listClasspath:
            strClasspathTotal += strClasspath + ":"
        strClasspathTotal = strClasspathTotal[:-1]
        args.append("java")
        args.append("-classpath")
        args.append(strClasspathTotal)
        args.append("org.dawb.workbench.jmx.py4j.GatewayServerWorkflow")
        self._subprocess = subprocess.Popen(args)
        self._iPID = self._subprocess.pid
        # Give some time for the process to start...
        time.sleep(0.1)
        self._gateway_client = JavaGateway()
        self._gateway_client.restart_callback_server()
        time.sleep(0.5)


    def shutdownJavaGatewayServer(self):
        if self._gateway_client is None:
            self._gateway_client = JavaGateway()
        #self._gateway_client._shutdown_callback_server()
        self._gateway_client.shutdown()
        self._gateway_client = None
        if self._subprocess is not None:
            self._subprocess.kill()
            self._subprocess = None
        # 
        time.sleep(1)

#################################################################################
# 
# Py4jWorkfloCallback methonds

    def createUserInput(self, actorName, dictUserValues):
        print "="*80
        print "="*80
        print "WorkflowDS.createUserInput: actorName = ", actorName
        print "WorkflowDS.createUserInput: dictIn = ", dictUserValues
        returnMap = dictUserValues
        if actorName == "Start":
            java_map = JavaGateway().jvm.java.util.HashMap()
            java_map["dataInput"] = self.getDataInput()
            java_map["defaltValues"] = "false"
            returnMap = java_map
        elif actorName == "End":
            if "dataOutput" in dictUserValues.keys():
                self.setDataOutput(dictUserValues["dataOutput"])
            returnMap = dictUserValues
        else:
            # TODO: TANGO callback
            returnMap = dictUserValues
        print "WorkflowDS.createUserInput: dictOut = ", returnMap
        print "="*80
        print "="*80
        return returnMap


    def setActorSelected(self, actorName, isSelected):
        print "="*80
        print "="*80
        print "WorkflowDS.setActorSelected: ", actorName, " is selected: ", isSelected
        if isSelected:
            self._strActorSelected = actorName
            if self._workflowDS is not None:
                self._workflowDS.push_change_event("actorSelected", actorName)
        print "="*80
        print "="*80


    def showMessage(self, strTitle, strMessage, iType):
        print "="*80
        print "="*80
        print "WorkflowDS.showMessage: title = ", strTitle
        print "WorkflowDS.showMessage: message = ", strMessage
        print "WorkflowDS.showMessage: iType = ", iType
        print "="*80
        print "="*80

    class Java: # IGNORE:W0232
        implements = ['org.dawb.workbench.jmx.py4j.Py4jWorkflowCallback']

#
#
##################################################################################
    
    def getDataInput(self):
        return self._strDataInput
    
        
    def getDataOutput(self):
        return self._strDataOutput

    def setDataOutput(self, _dataOutput):
        print "In WorkflowProxyThread::getJobOutput()"
        print _dataOutput
        print str(_dataOutput)
        self._strDataOutput = str(_dataOutput)
        print self._strDataOutput
        
    def getJobId(self):
        return self._strJobId
