#!/usr/bin/env groovy

def call(vmBuildId) {
	echo "INSIDE OF reserveProdIp FUNCTION"
	
	stage("Reserve/Get Prod IP - ${vmBuildId}") {
		def response7
		try {
			response7 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"vm_build_id\":${vmBuildId}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/ipam_prod"
      		println('Response7 from ECDB: '+response7.content)
        	//prodIpAddress = readJSON text: response7.content
		}
		catch(error) {
			response7 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"vm_build_id\":${vmBuildId}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/ipam_prod", validResponseCodes: '100:504'
			prodIpAddress = readJSON text: response7.content
			
			//write to file for error handling
			sh """(echo Failed API call in reserveProdIp.groovy; echo Reason: \"${prodIpAddress.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
			currentBuild.result = 'FAILURE'
			throw new hudson.AbortException("Failed API: ${prodIpAddress.message}")
		}
		
		return response7.content
	}
		
}

