#!/usr/bin/env groovy

def call(componentBuildID, vmBuildId) {
	echo "INSIDE OF updateCompVmBuild FUNCTION"
	
	stage("Update Table - component_vm_build") {
		
		try{
			def updateComponentVmBuild = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"Built\":1}", responseHandle: 'NONE', url: "http://${ecdbIP}/component_vm_build?Component_Build_ID=eq.${componentBuildID}&&VM_Build_ID=eq.${vmBuildId}"
		}
		catch(error) {
			def updateComponentVmBuild = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"Built\":1}", responseHandle: 'NONE', url: "http://${ecdbIP}/component_vm_build?Component_Build_ID=eq.${componentBuildID}&&VM_Build_ID=eq.${vmBuildId}", validResponseCodes: '100:504'
			def updateComponentVmBuildResponse = readJSON text: updateComponentVmBuild.content
			
			//write to file for error handling
			sh """(echo Failed API call in updateCompVmBuild.groovy; echo Reason: \"${updateComponentVmBuildResponse.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
			currentBuild.result = 'FAILURE'
			throw new hudson.AbortException("Failed API: ${updateComponentVmBuildResponse.message}")
		}			
	}
}	
