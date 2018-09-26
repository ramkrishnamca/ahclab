#!/usr/bin/env groovy

def call(CloudProvider, CloudEnvironment) {             
    stage("get Admin Network"){        
		
		try {
			def adminNetworkResponse = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/system_property_asset?select=SystemPropertyValue&&SystemPropertyName=eq.vsphere_network&&CloudProvider=eq.${CloudProvider}&&CloudEnvironment=eq.${CloudEnvironment}"
			println('adminNetworkResponse from ECDB: '+adminNetworkResponse.content)
        	adminNetwork = readJSON text: adminNetworkResponse.content
        	adminNetworkName = adminNetwork.SystemPropertyValue[0]
        	echo "${adminNetworkName}"
		}
		catch(error) {
			def adminNetworkResponse = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/system_property_asset?select=SystemPropertyValue&&SystemPropertyName=eq.vsphere_network&&CloudProvider=eq.${CloudProvider}&&CloudEnvironment=eq.${CloudEnvironment}", validResponseCodes: '100:504'			
			adminNetwork = readJSON text: adminNetworkResponse.content
			
			//write to file for error handling
			sh """(echo Failed API call in getAdminNetworkId.groovy; echo Reason: \"${adminNetwork.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
			currentBuild.result = 'FAILURE'
			throw new hudson.AbortException("Failed API: ${adminNetwork.message}")
		}
	
		return adminNetworkName
			
	}
}
