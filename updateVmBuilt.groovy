#!/usr/bin/env groovy

def call(vmBuildId) {

	stage("Update Table - vm_build") {
		echo "INSIDE OF updateVmBuilt FUNCTION"
		try {
			def vmBuild = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"Built\":1}", responseHandle: 'NONE', url: "http://${ecdbIP}/vm_build?VM_Build_ID=eq.${vmBuildId}"	
		}
		catch(error) {
			def vmBuild = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"Built\":1}", responseHandle: 'NONE', url: "http://${ecdbIP}/vm_build?VM_Build_ID=eq.${vmBuildId}", validResponseCodes: '100:504'
			vmBuildResponse = readJSON text: vmBuild.content
			
			//write to file for error handling
			sh """(echo Failed API call in updateVmBuilt.groovy; echo Reason: \"${vmBuildResponse.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
			currentBuild.result = 'FAILURE'
			throw new hudson.AbortException("Failed API: ${vmBuildResponse.message}")
		}
	}
}
