
import os, unittest, tempfile, os, shutil

if not "JMX_LOC" in os.environ.keys():
    strCwd = os.getcwd()
    strJmxLoc = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(strCwd)))), "dawn-workflow")
    print strJmxLoc
    os.environ["JMX_LOC"] = strJmxLoc

from WorkflowProxyThread import WorkflowProxyThread


class TestWorkflowSleep(unittest.TestCase):
    
    _strTestFile = "test_python.moml"
    
    def setUp(self):
        # Create a new workspace
        self._strWorkspacePath = tempfile.mkdtemp(prefix="dawb_workspace_")
        self._strModelDir = os.path.join(self._strWorkspacePath, "workflow")
        os.mkdir(self._strModelDir)
        self._strSrcDir = os.path.join(self._strModelDir, "src")
        os.mkdir(self._strSrcDir)
        self._strPathWorkflow = os.path.join(self._strModelDir, self._strTestFile)
        self._strPathScript = os.path.join(self._strSrcDir, "test_python.py")
        shutil.copyfile(self._strTestFile, self._strPathWorkflow)
        shutil.copyfile("test_python.py", self._strPathScript)

        
    def test_runWorkflow(self):
        workflowProxyThread = WorkflowProxyThread(None)
        workflowProxyThread.start()
        workflowProxyThread.setWorkspacePath(self._strWorkspacePath)
        workflowProxyThread.startJob(self._strPathWorkflow, "XMLinput")
        workflowProxyThread.synchronizeWorkflow()
        workflowProxyThread.shutdownJavaGatewayServer()        
        workflowProxyThread.shutdown()


    def tearDown(self):
        shutil.rmtree(self._strPathWorkflow, ignore_errors=True)
