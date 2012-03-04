


import os, sys, unittest, time, tempfile, shutil

sys.path.append("../Py4jWorkflowServer")

from WorkflowProxyThread import WorkflowProxyThread

#listTestWorkflows = ["test_sleep.moml", "test_review.moml", "test_message.moml", "loop_example.moml"]
listTestWorkflows = ["test_message.moml"]


class TestWorkflowProxyThread(unittest.TestCase):
    
    @classmethod
    def setUpClass(self):
        # Create a new workspace
        self._strWorkspacePath = tempfile.mkdtemp(prefix = "dawb_workspace_")
        strModelDir = os.path.join(self._strWorkspacePath, "workflow")
        os.mkdir(strModelDir)
        self._listTestWorkflow = []
        for strWorkflow in listTestWorkflows:
            strPathWorkflow = os.path.join(strModelDir, strWorkflow)
            self._listTestWorkflow.append(strPathWorkflow)
            shutil.copyfile(strWorkflow, strPathWorkflow)
    
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
        time.sleep(0.1)
        workflowProxyThread.shutdownJavaGatewayServer()
        time.sleep(0.1)
        workflowProxyThread.startJavaGatewayServer()
        time.sleep(0.1)
        workflowProxyThread.shutdownJavaGatewayServer()
        
        
    def test_runWorkflow(self):
        workflowProxyThread = WorkflowProxyThread(None)
        workflowProxyThread.start()
        time.sleep(0.1)
        workflowProxyThread.setWorkspacePath(self._strWorkspacePath)
        for strWorkflowPath in self._listTestWorkflow:
            workflowProxyThread.startJob(strWorkflowPath, "strJobArg")
            time.sleep(0.1)
            workflowProxyThread.synchronizeWorkflow()
            time.sleep(1)
        workflowProxyThread.shutdown()
        time.sleep(0.1)
        