

import os, time, threading, subprocess

from Py4jWorkflowCallback import Py4jWorkflowCallback

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
    
    def __init__(self, _parent):
        threading.Thread.__init__(self)
        self._workflowDS = _parent
        self._bShutdown = False
        self._subprocess = None
        self._iPID = None
        self._gateway = None
        #self._strInstallationPath = "/sware/isdd/soft/dawb/nightly/linux_x64/dawb/dawb"
        self._strInstallationPath = "/opt/dawb/dawb"
        
        
    def run(self):
        while(not self._bShutdown):
            time.sleep(1)
            
            
    def shutdown(self):
        self.shutdownJavaGatewayServer()
        self._bShutdown = True
        time.sleep(1)
        
    
    def setWorkspacePath(self, _strWorkspacePath):
        self._strWorkspacePath = _strWorkspacePath
    
    
    def startJob(self, _strJobName, _strJobArg):
        # Start the java gateway server
        self.startJavaGatewayServer()   
        # Start the job
        self._gateway.entry_point.setPy4jWorkflowCallback(Py4jWorkflowCallback(self))
        self._gateway.entry_point.setWorkspacePath(self._strWorkspacePath)
        self._gateway.entry_point.setModelPath(_strJobName)
        self._gateway.entry_point.setInstallationPath(self._strInstallationPath)
        self._gateway.entry_point.setServiceTerminate(False)
        self._gateway.entry_point.setTangoSpecMode(False)
        self._gateway.entry_point.runWorkflow()
         
    
    def synchronizeWorkflow(self):
        self._gateway.entry_point.synchronizeWorkflow()

    
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
        self._gateway = JavaGateway()
        self._gateway.restart_callback_server()
        time.sleep(0.5)


    def shutdownJavaGatewayServer(self):
        if self._gateway is None:
            self._gateway = JavaGateway()
        self._gateway._shutdown_callback_server()
        self._gateway.shutdown()
        self._gateway = None
        if self._subprocess is not None:
            self._subprocess.kill()
            self._subprocess = None
        # 
        time.sleep(1)
