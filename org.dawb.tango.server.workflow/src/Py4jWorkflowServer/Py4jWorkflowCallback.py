from py4j.java_collections import JavaMap


class Py4jWorkflowCallback(object):



    def __init__(self, _parent, _gateway_client):
        self._parent = _parent
        self._gateway_client = _gateway_client 
        print "="*80
        print "="*80
        print "In __init__"
        print "="*80
        print "="*80


    def setActorSelected(self, actorName, isSelected):
        print "="*80
        print "="*80
        print "Py4jWorkflowCallback.setActorSelected: ", actorName, " is selected: ", isSelected
        print "="*80
        print "="*80
        
        
    def showMessage(self, strTitle, strMessage, iType):
        print "="*80
        print "="*80
        print "Py4jWorkflowCallback.showMessage: title = ", strTitle
        print "Py4jWorkflowCallback.showMessage: message = ", strMessage
        print "Py4jWorkflowCallback.showMessage: iType = ", iType
        print "="*80
        print "="*80
        
        
    def createUserInput(self, actorName, dictUserValues):
        print "="*80
        print "="*80
        print "Py4jWorkflowCallback.createUserInput: actorName = ", actorName
        print "Py4jWorkflowCallback.createUserInput: dict = ", dictUserValues
        returnMap = None
        if actorName == "Start":
            java_map = self._gateway_client.jvm.java.util.HashMap()
            java_map["dataInput"] = self._parent.getDataInput()
            java_map["defaltValues"] = "false"
            print java_map
            returnMap = java_map
        elif actorName == "End":
            if "dataOutput" in dictUserValues.keys():
                self._parent.setDataOutput(dictUserValues["dataOutput"])
            returnMap = dictUserValues
        else:
            returnMap = self._parent.createUserInput(actorName, dictUserValues)
#        newDict[unicode("x")] = unicode("2.0")
#        print "Py4jWorkflowCallback.createUserInput: new dict = ", newDict
        print "="*80
        print "="*80
        return returnMap
    
    class Java: # IGNORE:W0232
        implements = ['org.dawb.workbench.jmx.py4j.Py4jWorkflowCallback']
    
