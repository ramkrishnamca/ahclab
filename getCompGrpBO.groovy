#!/usr/bin/env groovy

def call(buildId) {
	echo "INSIDE OF getCompGrpBO FUNCTION"
	try {
		def response1 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"build_id\":${buildId}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/uniquecgbo"
        println('Response1 from ECDB: '+response1.content)
        compGroupBuildOrders = readJSON text: response1.content	
	}
	catch (error) {
		def response1 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"build_id\":${buildId}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/uniquecgbo", validResponseCodes: '100:504'
		compGroupBuildOrders = readJSON text: response1.content	
		
		//write to file for error handling
		sh """(echo Failed API call in getCompGrpBO.groovy; echo Reason: \"${compGroupBuildOrders.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
		currentBuild.result = 'FAILURE'
		throw new hudson.AbortException("Failed API: ${compGroupBuildOrders.message}")
	}
	
	return compGroupBuildOrders
}
