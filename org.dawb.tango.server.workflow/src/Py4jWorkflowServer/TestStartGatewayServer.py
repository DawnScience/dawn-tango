# Test script for starting the Java Py4j Gateway server
# in a DAWB installation

import os

strPathToDawb = "/opt/dawb"
strPathToPlugins = os.path.join(strPathToDawb, "plugins")

# Find the path to the org.dawb.workbench.jmx jar file

strClassPath1 = os.path.join(strPathToPlugins, "org.dawb.workbench.jmx_1.0.0.201202240013.jar") 
strClassPath2 = os.path.join(strPathToPlugins, "com.springsource.slf4j.api_1.5.6.jar") 
strClassPath3 = os.path.join(strPathToPlugins, "com.springsource.ch.qos.logback.classic_0.9.15.jar")
strClassPath4 = os.path.join(strPathToPlugins, "com.springsource.ch.qos.logback.core_0.9.15.jar")
strClassPath5 = "/users/svensson/git/dawn-workflow/org.dawb.workbench.jmx/py4j0.7.jar"

strCommandLine = "java -cp %s:%s:%s:%s:%s org.dawb.workbench.jmx.py4j.GatewayServerWorkflow" % \
    (strClassPath1, strClassPath2, strClassPath3, strClassPath4, strClassPath5)
    
print strCommandLine