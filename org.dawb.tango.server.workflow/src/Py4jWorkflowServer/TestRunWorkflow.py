import time

from py4j.java_gateway import JavaGateway

class Py4jWorkflowCallback(object):


    def setActorSelected(self, actorName):
        print actorName

    class Java:
        implements = ['org.dawb.workbench.jmx.py4j.Py4jWorkflowCallback']



if __name__ == '__main__':
    gateway = JavaGateway()
    gateway.restart_callback_server()
    gateway.entry_point.setPy4jWorkflowCallback(Py4jWorkflowCallback())
    gateway.entry_point.setWorkspacePath("/users/svensson/dawb")
    gateway.entry_point.setModelPath("/users/svensson/dawb_workspace/workflows/examples/loop_example.moml")
    gateway.entry_point.setInstallationPath("/opt/dawb/dawb")
    gateway.entry_point.setServiceTerminate(False)
    gateway.entry_point.setTangoSpecMode(True)
    gateway.entry_point.runWorkflow()
    print "Workflow started!"
    gateway.entry_point.synchronizeWorkflow()
    gateway.seviceClear()
    gateway._shutdown_callback_server()
