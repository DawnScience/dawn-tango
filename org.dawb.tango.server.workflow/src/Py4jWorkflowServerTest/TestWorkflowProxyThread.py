


import sys, unittest, time

sys.path.append("../Py4jWorkflowServer")

from WorkflowProxyThread import WorkflowProxyThread


class TestWorkflowProxyThread(unittest.TestCase):
    
    def test_startServer(self):
        workflowProxyThread = WorkflowProxyThread(None)
        workflowProxyThread.start()
        time.sleep(1)
        self.assertTrue(workflowProxyThread.is_alive(), "Thread is alive")
        workflowProxyThread.shutdown()
        time.sleep(1)
        self.assertFalse(workflowProxyThread.is_alive(), "Thread is shutdown")
        

    def test_connectToGateway(self):
        workflowProxyThread = WorkflowProxyThread(None)
        workflowProxyThread.startJavaGatewayServer()
        
    def test_runWorkflow(self):
        workflowProxyThread = WorkflowProxyThread(None)
        workflowProxyThread.start()
        time.sleep(1)
        workflowProxyThread.startJob("strJobName", "strJobArg")
        time.sleep(1)
        workflowProxyThread.synchronizeWorkflow()
        time.sleep(1)
        workflowProxyThread.shutdown()
        time.sleep(1)
        