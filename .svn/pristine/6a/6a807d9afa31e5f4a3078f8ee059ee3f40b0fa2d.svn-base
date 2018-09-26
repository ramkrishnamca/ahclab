#!/usr/bin/env groovy

def call(vmBuildId) {
	echo "INSIDE OF reserveAdminIp FUNCTION"
	
	stage("Reserve/Get Admin IP - ${vmBuildId}") {
		def response7
		def adminIpAddress
		try {
			response7 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"vm_build_id\":${vmBuildId}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/ipam_admin"
      		println('Response7 from ECDB: '+response7.content)			
		}
		catch(error) {
			response7 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"vm_build_id\":${vmBuildId}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/ipam_admin", validResponseCodes: '100:504'
			adminIpAddress = readJSON text: response7.content
			
			//write to file for error handling
			sh """(echo Failed API call in reserveAdminIp.groovy; echo Reason: \"${adminIpAddress.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
			currentBuild.result = 'FAILURE'
			throw new hudson.AbortException("Failed API: ${adminIpAddress.message}")
		}
		
		adminIpAddress = response7.content
		
		if (adminIpAddress == "null") {
			sh """(echo No Admin IP Address Available ; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
			currentBuild.result = 'FAILURE'
			throw new hudson.AbortException("Failed: No Admin IP Address Available")
		}
		
		return response7.content
	}
		
}

