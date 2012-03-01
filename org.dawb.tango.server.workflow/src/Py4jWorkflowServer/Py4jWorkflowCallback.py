class Py4jWorkflowCallback(object):

    def __init__(self, _parent):
        self.parent = _parent


    def setActorSelected(self, actorName, isSelected):
        print actorName, " is selected: ", isSelected
        
    class Java: #IGNORE:W0232
        implements = ["org.dawb.workbench.jmx.py4j.Py4jWorkflowCallback"]
    
