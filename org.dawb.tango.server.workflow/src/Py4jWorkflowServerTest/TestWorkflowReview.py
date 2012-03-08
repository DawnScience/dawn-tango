


import os, unittest, tempfile, os, shutil

if not "JMX_LOC" in os.environ.keys():
    strCwd = os.getcwd()
    strJmxLoc = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(strCwd)))), "dawn-workflow")
    print strJmxLoc
    os.environ["JMX_LOC"] = strJmxLoc

from WorkflowProxyThread import WorkflowProxyThread


class TestWorkflowReview(unittest.TestCase):
    
    _strTestFile = "test_review.moml"
    
    def setUp(self):
        # Create a new workspace
        self._strWorkspacePath = tempfile.mkdtemp(prefix="dawb_workspace_")
        self._strModelDir = os.path.join(self._strWorkspacePath, "workflow")
        os.mkdir(self._strModelDir)
        self._strPathWorkflow = os.path.join(self._strModelDir, self._strTestFile)
        shutil.copyfile(self._strTestFile, self._strPathWorkflow)

        
    def test_runWorkflow(self):
        workflowProxyThread = WorkflowProxyThread(None)
        workflowProxyThread.start()
        workflowProxyThread.setWorkspacePath(self._strWorkspacePath)
        workflowProxyThread.startJob(self._strPathWorkflow, "XMLinput")
        workflowProxyThread.synchronizeWorkflow()
        workflowProxyThread.shutdown()


    def tearDown(self):
        shutil.rmtree(self._strWorkspacePath)
