#!/usr/bin/env groovy

def call(compBuildID) {
	echo "INSIDE OF updateComponentBuild FUNCTION"
	
	stage("Update Table - component_build") {
		try {
			def updateComponentBuild = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"Built\":1}", responseHandle: 'NONE', url: "http://${ecdbIP}/component_build?Component_Build_ID=eq.${compBuildID}"
		}
		catch(error) {
			def updateComponentBuild = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"Built\":1}", responseHandle: 'NONE', url: "http://${ecdbIP}/component_build?Component_Build_ID=eq.${compBuildID}", validResponseCodes: '100:504'
			updateComponentBuildResponse = readJSON text: updateComponentBuild.content 
			
			//write to file for error handling
			sh """(echo Failed API call in updateComponentBuild.groovy; echo Reason: \"${updateComponentBuildResponse.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
			currentBuild.result = 'FAILURE'
			throw new hudson.AbortException("Failed API: ${updateComponentBuildResponse.message}")
		}
	}
}