


import os, sys, unittest, time, tempfile, shutil

if not "JMX_LOC" in os.environ.keys():
    strCwd = os.getcwd()
    strJmxLoc = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(strCwd)))), "dawn-workflow")
    print strJmxLoc
    os.environ["JMX_LOC"] = strJmxLoc


sys.path.append("../Py4jWorkflowServer")

from WorkflowProxyThread import WorkflowProxyThread

listTestWorkflows = ["test_sleep.moml", "test_review.moml", "test_message.moml", "loop_example.moml"]
#listTestWorkflows = ["test_message.moml"]


class TestWorkflowProxyThread(unittest.TestCase):
    
    def test_startServer(self):
        print "test_startServer"
        workflowProxyThread = WorkflowProxyThread(None)
        workflowProxyThread.start()
        self.assertTrue(workflowProxyThread.is_alive(), "Thread is alive")
        workflowProxyThread.shutdown()
        self.assertFalse(workflowProxyThread.is_alive(), "Thread is shutdown")
        return True
        

    def test_connectToGateway(self):
        print "test_connectToGateway"
        workflowProxyThread = WorkflowProxyThread(None)
        workflowProxyThread.startJavaGatewayServer()
        workflowProxyThread.shutdownJavaGatewayServer()
        workflowProxyThread.startJavaGatewayServer()
        workflowProxyThread.shutdownJavaGatewayServer()
        return True
        
        
