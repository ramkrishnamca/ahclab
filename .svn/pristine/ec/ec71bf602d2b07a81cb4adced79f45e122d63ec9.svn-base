#!/usr/bin/env groovy

def call(compGrpBuildID) {
	echo "INSIDE OF updateCompGrpBuild FUNCTION"
	
	stage("Update Table - component_group_build") {
		try {
			def updateComponentBuild = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"Built\":1}", responseHandle: 'NONE', url: "http://${ecdbIP}/component_group_build?Component_Group_Build_ID=eq.${compGrpBuildID}"
		}
		catch(error) {
			def updateComponentBuild = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"Built\":1}", responseHandle: 'NONE', url: "http://${ecdbIP}/component_group_build?Component_Group_Build_ID=eq.${compGrpBuildID}", validResponseCodes: '100:504'
			updateComponentBuildResponse = readJSON text: updateComponentBuild.content
			
			//write to file for error handling
			sh """(echo Failed API call in updateCompGrpBuild.groovy; echo Reason: \"${updateComponentBuildResponse.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
			currentBuild.result = 'FAILURE'
			throw new hudson.AbortException("Failed API: ${updateComponentBuildResponse.message}")
		}
	}
}