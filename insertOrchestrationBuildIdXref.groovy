#!/usr/bin/env groovy

def call() {

	stage("Update Table - orchestration_build_id_xref") {
		echo "INSIDE OF insertOrchestrationBuildIdXref FUNCTION"		
		
		def checkBuildID = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/environment_build?select=EnvironmentName&Build_ID=eq.${BuildID}"
		def checkBuildIDResponse = readJSON text: checkBuildID.content
				
		if (checkBuildIDResponse.size() < 1) {
			sh """(echo Build_ID ${BuildID} does not exist; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
			currentBuild.result = 'FAILURE'
			throw new hudson.AbortException("Build_ID ${BuildID} does not exist")
		}
		
		
		try {
			def orchBuildIDXref = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"Orchestration_Job_ID\":${env.BUILD_NUMBER},\"Build_ID\":${BuildID}}", responseHandle: 'NONE', url: "http://${ecdbIP}/orchestration_build_id_xref"		
		}
		catch(error) {					
			def orchBuildIDXref = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"Orchestration_Job_ID\":${env.BUILD_NUMBER},\"Build_ID\":${BuildID}}", responseHandle: 'NONE', url: "http://${ecdbIP}/orchestration_build_id_xref", validResponseCodes: '100:504'			
			orchBuildIDXrefResponse = readJSON text: orchBuildIDXref.content
			
			//write to file for error handling
			sh """(echo Failed API call in insertOrchestrationBuildIdXref.groovy; echo Reason: \"${orchBuildIDXrefResponse.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
			currentBuild.result = 'FAILURE'
			throw new hudson.AbortException("Failed API: ${orchBuildIDXrefResponse.message}")
		}
	}
}
