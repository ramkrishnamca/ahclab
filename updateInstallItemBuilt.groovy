#!/usr/bin/env groovy

def call(componentVmBuildId, buildOrder) {

	stage("Update Table - install_item_build") {
		echo "INSIDE OF updateInstallItemBuilt FUNCTION"
		
		try {
			def installItemsBuilt = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"Built\":1}", responseHandle: 'NONE', url: "http://${ecdbIP}/install_item_build?Component_VM_Build_ID=eq.${componentVmBuildId}&&BuildOrder=eq.${buildOrder}"
		}
		catch(error) {
			def installItemsBuilt = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"Built\":1}", responseHandle: 'NONE', url: "http://${ecdbIP}/install_item_build?Component_VM_Build_ID=eq.${componentVmBuildId}&&BuildOrder=eq.${buildOrder}", validResponseCodes: '100:504'
			installItemsBuiltResponse = readJSON text: installItemsBuilt.content
			
			//write to file for error handling
			sh """(echo Failed API call in updateInstallItemBuilt.groovy; echo Reason: \"${installItemsBuiltResponse.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
			currentBuild.result = 'FAILURE'
			throw new hudson.AbortException("Failed API: ${installItemsBuiltResponse.message}")
		}
	}
}
