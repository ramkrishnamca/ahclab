#!/usr/bin/env groovy

def call() {
	echo "INSIDE OF decommissionEnvironment FUNCTION"
	
	stage("Decommission Environment") {
		def getBuildIds = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/environment_build?select=Build_ID&EnvironmentName=eq.${DecommissionValue}&&Customer_ID=eq.${CustomerID}"
		def buildIdsResponse = readJSON text: getBuildIds.content
		
		echo "buildIdsResponse = ${buildIdsResponse}"
		
		if (buildIdsResponse[0].equals(null)) {
			currentBuild.result = 'FAILURE'
			throw new hudson.AbortException("Failed no record found for Environment ${DecommissionValue} and Customer_ID ${CustomerID}")		
		}		
		
		def buildID = [:]                     
		
		for (def index = 0; index < buildIdsResponse.size(); index++) {
			def myIndex = index
			
			buildID["buildID ${buildIdsResponse[index]}"] = {
				
				stage("Decommission ${buildIdsResponse[myIndex]}") {                                     
					decommissionFingerPrint buildIdsResponse.Build_ID[myIndex]
				}
			}
		}
		parallel buildID	
		
		def removeEnvironment = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"environment\":\"${DecommissionValue}\",\"customer\":${CustomerID}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/decommission_environment"
		def removeEnvironmentResponse = readJSON text: removeEnvironment.content
		echo "removeEnvironmentResponse = ${removeEnvironmentResponse}"			
	}
}
