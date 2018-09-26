#!/usr/bin/env groovy

def call() {
	echo "INSIDE OF decommissionFingerPrint FUNCTION"
	
	stage("Decommission FingerPrint") {
		def myVmuuidString = ''		
		def getVmuuids = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/vm_build?select=VMUUID&Build_ID=eq.${DecommissionValue}&&Customer_ID=eq.${CustomerID}"
		def vmuuidsResponse = readJSON text: getVmuuids.content		
		
		if (vmuuidsResponse[0].equals(null)) {
			currentBuild.result = 'FAILURE'
			throw new hudson.AbortException("Failed no record found for FingerPrint ${DecommissionValue} and Customer_ID ${CustomerID}")		
		}	
		
		for (def index = 0; index < vmuuidsResponse.size(); index++) {
			if (index == 0) {
				myVmuuidString = myVmuuidString + vmuuidsResponse.VMUUID[index]
			}
			else {
				myVmuuidString = myVmuuidString + "," + vmuuidsResponse.VMUUID[index]
			}
		}		
		decommissionVM myVmuuidString
		
		stage("Clearing ECDB Build Tables") { 
			def resetBuild = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"build_id\":${DecommissionValue}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/decommission_fingerprint"
			def resetBuildResponse = readJSON text: resetBuild.content
			echo "resetBuildResponse = ${resetBuildResponse.hostname}"
		}
	}
}

def call(buildID) {
	echo "INSIDE OF decommissionFingerPrint FUNCTION"
	
	stage("Decommission FingerPrint") {
		def myVmuuidString = ''		
		def getVmuuids = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/vm_build?select=VMUUID&Build_ID=eq.${buildID}&&Customer_ID=eq.${CustomerID}"
		def vmuuidsResponse = readJSON text: getVmuuids.content
		
		if (vmuuidsResponse[0].equals(null)) {
			currentBuild.result = 'FAILURE'
			throw new hudson.AbortException("Failed no record found for FingerPrint ${buildID} and Customer_ID ${CustomerID}")		
		}	
		
		for (def index = 0; index < vmuuidsResponse.size(); index++) {
			if (index == 0) {
				myVmuuidString = myVmuuidString + vmuuidsResponse.VMUUID[index]
			}
			else {
				myVmuuidString = myVmuuidString + "," + vmuuidsResponse.VMUUID[index]
			}
		}		
		decommissionVM myVmuuidString
		
		stage("Clearing ECDB Build Tables") { 
		
			def resetBuild = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"build_id\":${buildID}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/decommission_fingerprint"
			def resetBuildResponse = readJSON text: resetBuild.content
			echo "resetBuildResponse = ${resetBuildResponse.hostname}"
		}
	}
}