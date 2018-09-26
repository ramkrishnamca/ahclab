#!/usr/bin/env groovy

def call() {

	echo "INSIDE OF errorHandling FUNCTION"
	echo "Checking for errors..."
	def fileName = "${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"
	def checkFile = new File(fileName)
	if (checkFile.exists()) {
		echo "Errors found"
		String fileContents = new File("${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt").text
		echo "${fileContents}"
		
		//update post API for SNOW
		//def vmBuild = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"Built\":1}", responseHandle: 'NONE', url: "http://${ecdbIP}/vm_build?VM_Build_ID=eq.${vmBuildId}"

		sh """cd ${nfsPath}/chefparamfiles/
		rm -f FingerPrint-${BuildID}-Failed.txt"""
	}
	else {
		echo "No errors found"
	}
}
	