#!/usr/bin/env groovy

def call(compBuildId) {
	echo "INSIDE OF getVmBuildId FUNCTION"
	def componentVmDetails = ''
	try {
		def response5 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_vm_build?select=VM_Build_ID&&Built=eq.false&&Component_Build_ID=eq.${compBuildId}"
                                                        
		println('Response5 from ECDB: '+response5.content)
                                                        
		componentVmDetails = readJSON text: response5.content
	}
	catch(error) {
		def response5 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_vm_build?select=VM_Build_ID&&Built=eq.false&&Component_Build_ID=eq.${compBuildId}", validResponseCodes: '100:504'
		componentVmDetails = readJSON text: response5.content
		
		//write to file for error handling
		sh """(echo Failed API call in getVmBuildId.groovy; echo Reason: \"${componentVmDetails.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
		currentBuild.result = 'FAILURE'
		throw new hudson.AbortException("Failed API: ${componentVmDetails.message}")
	}
		
                                                        
	def componentVmAssoc = [:]
                                                        
	for (def componentVmAssocIndex = 0; componentVmAssocIndex < componentVmDetails.size(); componentVmAssocIndex++) {
                                                            
		def index2 = componentVmAssocIndex
                                                            
		componentVmAssoc["componentVmAssoc ${componentVmAssocIndex}"] = {
                                                                
			stage("Build Component/VM Association - ${componentVmDetails.VM_Build_ID[index2]}") {
				
				getVmDetails componentVmDetails.VM_Build_ID[index2], compBuildId	
                updateCompVmBuild compBuildId, componentVmDetails.VM_Build_ID[index2]                                               
			}
			
                                                            
		}                                                   
                                                        
	} 
             	                                           
	parallel componentVmAssoc	
}
