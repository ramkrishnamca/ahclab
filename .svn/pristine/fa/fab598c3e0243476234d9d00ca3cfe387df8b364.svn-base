#!/usr/bin/env groovy

def call(vmBuildId, vmuuid, hostname) {

	stage("Update Table - vm_build") {
		echo "INSIDE OF updateVmBuildVmuuid FUNCTION"
		try {
			def updateComponentGroupAsset = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"VMUUID\":\"${vmuuid}\",\"MachineID\":\"${hostname}\"}", responseHandle: 'NONE', url: "http://${ecdbIP}/vm_build?VM_Build_ID=eq.${vmBuildId}"
		}
		catch(error) {
			def updateComponentGroupAsset = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"VMUUID\":\"${vmuuid}\",\"MachineID\":\"${hostname}\"}", responseHandle: 'NONE', url: "http://${ecdbIP}/vm_build?VM_Build_ID=eq.${vmBuildId}", validResponseCodes: '100:504'
			def updateComponentGroupAssetResponse = readJSON text: updateComponentGroupAsset.content
		
			//write to file for error handling
			sh """(echo Failed API call in updateVmBuildVmuuid.groovy; echo Reason: \"${updateComponentGroupAssetResponse.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
			currentBuild.result = 'FAILURE'
			throw new hudson.AbortException("Failed API: ${updateComponentGroupAssetResponse.message}")
		}
	}
}