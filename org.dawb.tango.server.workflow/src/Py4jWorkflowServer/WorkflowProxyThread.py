

import sys, time, threading

try:
    from py4j.java_gateway import JavaGateway2
except ImportError, e:
    print "Error! Py4j must be installed in Python in order for the WorkflowDS server to work."
    print "Please see http://py4j.sourceforge.net/install.html for installation instructions."
    sys.exit(1)


class WorkflowProxyThread(threading.Thread):
    
    def __init__(self, _parent):
        threading.Thread.__init__(self)
        self.workflowDS = _parent
        self.bShutdown = False
        
        
    def run(self):
        while(not self.bShutdown):
            time.sleep(1)
            
            
    def shutdown(self):
        self.bShutdown = True
