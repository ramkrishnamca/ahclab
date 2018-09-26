#!/usr/bin/env groovy

def call(vmBuildId, compBuildId) {
	if (compBuildId != "noCG") {
		stage("Update Table - install_item_build bootstrap") {
			echo "INSIDE OF updateInstallItemBootstrap FUNCTION"
			def componentvmBuildId
			try {
				def getComponentVmBuildId = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_vm_build?select=Component_VM_Build_ID&VM_Build_ID=eq.${vmBuildId}&&Component_Build_ID=eq.${compBuildId}"
				println('getComponentVmBuildId from ECDB: '+getComponentVmBuildId.content)                                                        
				def componentvmBuildIdResponse = readJSON text: getComponentVmBuildId.content
		
				componentvmBuildId = componentvmBuildIdResponse.Component_VM_Build_ID[0]
			}
			catch(error) {
				def getComponentVmBuildId = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_vm_build?select=Component_VM_Build_ID&VM_Build_ID=eq.${vmBuildId}&&Component_Build_ID=eq.${compBuildId}", validResponseCodes: '100:504'
				def componentvmBuildIdResponse = readJSON text: getComponentVmBuildId.content
				
				//write to file for error handling
				sh """(echo Failed API call in updateInstallItemBootstrap.groovy; echo Reason: \"${componentvmBuildIdResponse.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				currentBuild.result = 'FAILURE'
				throw new hudson.AbortException("Failed API: ${componentvmBuildIdResponse.message}")
			}
			try {
				def vmBuild = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"Built\":1}", responseHandle: 'NONE', url: "http://${ecdbIP}/install_item_build?Component_VM_Build_ID=eq.${componentvmBuildId}&&ItemType=eq.Bootstrap"
			}
			catch(error) {
				def vmBuild = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"Built\":1}", responseHandle: 'NONE', url: "http://${ecdbIP}/install_item_build?Component_VM_Build_ID=eq.${componentvmBuildId}&&ItemType=eq.Bootstrap", validResponseCodes: '100:504'
				def vmBuildResponse = readJSON text: vmBuild.content
				
				//write to file for error handling
				sh """(echo Failed API call in updateInstallItemBootstrap.groovy; echo Reason: \"${vmBuildResponse.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				currentBuild.result = 'FAILURE'
				throw new hudson.AbortException("Failed API: ${vmBuildResponse.message}")
			}
		}
	}
}
