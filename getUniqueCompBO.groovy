#!/usr/bin/env groovy

def call(buildId, compGrpBuildId) {
	echo "INSIDE OF getUniqueCompBO FUNCTION"
	
	try {
		def response3 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"build_id\":${buildId},\"component_group_build_id\":${compGrpBuildId}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/uniquecompbo"
                                        
		println('Response3 from ECDB: '+response3.content)
                                        
		componentBuildOrders = readJSON text: response3.content        
	}
	catch(error) {
		def response3 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"build_id\":${buildId},\"component_group_build_id\":${compGrpBuildId}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/uniquecompbo", validResponseCodes: '100:504'
		componentBuildOrders = readJSON text: response3.content
		
		//write to file for error handling
		sh """(echo Failed API call in getUniqueCompBO.groovy; echo Reason: \"${componentBuildOrders.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
		currentBuild.result = 'FAILURE'
		throw new hudson.AbortException("Failed API: ${componentBuildOrders.message}")
	}
	
	return componentBuildOrders
	
}
