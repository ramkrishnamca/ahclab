#!/usr/bin/env groovy

def call(BuildID) {
	echo "INSIDE OF reserveIPAM FUNCTION"
	
	stage("Reserve IPAM") {		
		
		try {
			def response7 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/vm_build?select=VM_Build_ID&Build_ID=eq.${BuildID}"
			println('Response7 from ECDB: '+response7.content)
            buildIdList = readJSON text: response7.content
		}
		catch(error) {
			def response7 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/vm_build?select=VM_Build_ID&Build_ID=eq.${BuildID}", validResponseCodes: '100:504'
			buildIdList = readJSON text: response7.content
			
			//write to file for error handling
			sh """(echo Failed API call in reserveIPAM.groovy; echo Reason: \"${buildIdList.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
			currentBuild.result = 'FAILURE'
			throw new hudson.AbortException("Failed API: ${buildIdList.message}")
		}

		for (buildIdListIndex = 0; buildIdListIndex < buildIdList.size(); buildIdListIndex++ ) {
			reserveAdminIp buildIdList[buildIdListIndex].VM_Build_ID
			reserveProdIp buildIdList[buildIdListIndex].VM_Build_ID
		}

		echo "Exit Reserve IPAM"
	}
}	
