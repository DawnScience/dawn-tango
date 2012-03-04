class Py4jWorkflowCallback(object):



    def __init__(self, _parent):
        self.parent = _parent
        print "="*80
        print "="*80
        print "In __init__"
        print "="*80
        print "="*80


    def setActorSelected(self, actorName, isSelected):
        print "="*80
        print "="*80
        print actorName, " is selected: ", isSelected
        print "="*80
        print "="*80
        
    class Java:
        implements = ['org.dawb.workbench.jmx.py4j.Py4jWorkflowCallback']
    
