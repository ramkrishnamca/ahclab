#!/usr/bin/env groovy

def call(vmBuildId, compBuildId) {
	echo "INSIDE OF getVmDetails FUNCTION"
	
	echo "vmBuildId = ${vmBuildId}"
	
	stage("Get VM Details - ${vmBuildId}") {
		                                                                    
		try {
			def response6 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"vm_build_id\":${vmBuildId}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/vmbuilddata"
			println('Response6 from ECDB: '+response6.content)
                                                        
			vmDetails = readJSON text: response6.content
			echo "VM Details = ${vmDetails}"		
		}
		catch(error) {
			def response6 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"vm_build_id\":${vmBuildId}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/vmbuilddata", validResponseCodes: '100:504'
			vmDetails = readJSON text: response6.content
			
			//write to file for error handling
			sh """(echo Failed API call in getVmDetails.groovy; echo Reason: \"${vmDetails.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
			currentBuild.result = 'FAILURE'
			throw new hudson.AbortException("Failed API: ${vmDetails.message}")
		}
		
		if (vmDetails.built.join(",") == "false") {
			echo "not built________________________________"
			def vmuuid = provisionVmVsphere CustomerID, vmBuildId, compBuildId, vmDetails
			updateVmBuilt vmBuildId
			assetTrackingUpdateVmBuild vmuuid
			installItemString = getInstallItems vmBuildId, compBuildId, CustomerID
			if (installItemString != '') {
				createParameterFile customerID, vmBuildId, installItemString, compBuildId
			}
			runInstallItems customerID, vmBuildId, compBuildId
		}
		else {
			echo "is built_-_-_-_-_-_-_-_-_-_-_-_-_-_-_"
			installItemString = getInstallItems vmBuildId, compBuildId, CustomerID
			if (installItemString != '') {
				createParameterFile customerID, vmBuildId, installItemString, compBuildId
			}
			runInstallItems customerID, vmBuildId, compBuildId
		}
		
	}		
}