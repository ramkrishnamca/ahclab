#!/usr/bin/env groovy

def call(buildId, componentBuildOrders, componentGroupBuildIDFormat) {
	echo "INSIDE OF getCompDetails FUNCTION"
	for (def compBuildOrderIndex = 0; compBuildOrderIndex < componentBuildOrders.size(); compBuildOrderIndex++) {
        
		try {
			def response4 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component?select=ComponentName,Component_Build_ID,Component_Group_Build_ID&Build_ID=eq.${buildId}&&Built=eq.false&&Component_Group_Build_ID=in.(${componentGroupBuildIDFormat})&&BuildOrder=eq.${componentBuildOrders[compBuildOrderIndex].buildorder}&&order=BuildOrder.asc"
                                            
			println('Response4 from ECDB: '+response4.content)
                                            
			componentDetails = readJSON text: response4.content                                      
		}
		catch(error) {
			def response4 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component?select=ComponentName,Component_Build_ID,Component_Group_Build_ID&Build_ID=eq.${buildId}&&Built=eq.false&&Component_Group_Build_ID=in.(${componentGroupBuildIDFormat})&&BuildOrder=eq.${componentBuildOrders[compBuildOrderIndex].buildorder}&&order=BuildOrder.asc", validResponseCodes: '100:504'
			componentDetails = readJSON text: response4.content 
			
			//write to file for error handling
			sh """(echo Failed API call in getCompDetails.groovy; echo Reason: \"${componentDetails.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
			currentBuild.result = 'FAILURE'
			throw new hudson.AbortException("Failed API: ${componentDetails.message}")
		}
                                            
		def component = [:]
                                            
                                            
		for (def componentIndex = 0; componentIndex < componentDetails.size(); componentIndex++) { 
                                                
			def index = componentIndex
                                                
			component["component ${componentIndex}"] = {
                                                    
				stage("Build Component ${componentDetails.ComponentName[index]} (Build Order ${componentBuildOrders[compBuildOrderIndex].buildorder})") {
                                                        
					getVmBuildId componentDetails.Component_Build_ID[index]
					updateComponentBuild componentDetails.Component_Build_ID[index]					
				}
                                                
			}
                                            
		}
                                            
		parallel component
                                        
	}	
}
