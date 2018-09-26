#!/usr/bin/env groovy

def call(vmuuid) {

	stage("Asset Tracking - vm_asset") {
		echo "INSIDE OF assetTrackingUpdateVmBuild FUNCTION"
		try {
			def vmAsset = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"Built\":1}", responseHandle: 'NONE', url: "http://${ecdbIP}/vm_asset?VMUUID=eq.${vmuuid}"	
		}
		catch(error) {
			def vmAsset = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"Built\":1}", responseHandle: 'NONE', url: "http://${ecdbIP}/vm_asset?VMUUID=eq.${vmuuid}", validResponseCodes: '100:504'
			vmAssetResponse = readJSON text: vmAsset.content
			
			//write to file for error handling
			sh """(echo Failed API call in assetTrackingUpdateVmBuild.groovy; echo Reason: \"${vmAssetResponse.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
			currentBuild.result = 'FAILURE'
			throw new hudson.AbortException("Failed API: ${vmAssetResponse.message}")
		}
	}
}
