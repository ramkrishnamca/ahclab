#!/usr/bin/env groovy

def call(jsonData, vmuuid) {
	echo "INSIDE OF updateIpPoolVmuuid FUNCTION"
	
	stage("Update Table - ip_pool") {		
		
		def myEnvName = jsonData.vm_clone_data.EnvironmentName[0]
		def myHostname = jsonData.vm_clone_data.Hostname[0]	
		def myVmAdminIp = jsonData.vm_network_data.IPAddress[0]	
		def myAdminNetworkId = jsonData.vm_network_data.NetworkID[0]
		def myVmProdIp = ''
		def myProdNetworkId = ''		
		
		def multiNetworkId = false
				
		//2 NICs
		if (jsonData.vm_network_data.NetworkID.size() > 1) {
			myProdNetworkId = jsonData.vm_network_data.NetworkID[1]
			myVmProdIp = jsonData.vm_network_data.IPAddress[1]
			multiNetworkId = true
		}
		
		if (multiNetworkId) {
			//Add update ip_pool admin and prod network api here:
			//Admin
			try {
				def updateIpPoolAdmin1 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"VMUUID\":\"${vmuuid}\",\"EnvironmentName\":\"${myEnvName}\"}", responseHandle: 'NONE', url: "http://${ecdbIP}/ip_pool?NetworkID=eq.${myAdminNetworkId}&&IPAddress=eq.${myVmAdminIp}&&Hostname=eq.${myHostname}&&Customer_ID=eq.${CustomerID}"
			}
			catch(error) {
				def updateIpPoolAdmin1 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"VMUUID\":\"${vmuuid}\",\"EnvironmentName\":\"${myEnvName}\"}", responseHandle: 'NONE', url: "http://${ecdbIP}/ip_pool?NetworkID=eq.${myAdminNetworkId}&&IPAddress=eq.${myVmAdminIp}&&Hostname=eq.${myHostname}&&Customer_ID=eq.${CustomerID}", validResponseCodes: '100:504'				
				updateIpPoolAdmin1Response = readJSON text: updateIpPoolAdmin1.content
			
				//write to file for error handling
				sh """(echo Failed API call in updateIpPoolVmuuid.groovy; echo Reason: \"${updateIpPoolAdmin1Response.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				currentBuild.result = 'FAILURE'
				throw new hudson.AbortException("Failed API: ${updateIpPoolAdmin1Response.message}")
			}
			
			//Prod
			try {
				def updateIpPoolProd1 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"VMUUID\":\"${vmuuid}\",\"EnvironmentName\":\"${myEnvName}\"}", responseHandle: 'NONE', url: "http://${ecdbIP}/ip_pool?NetworkID=eq.${myProdNetworkId}&&IPAddress=eq.${myVmProdIp}&&Hostname=eq.${myHostname}&&Customer_ID=eq.${CustomerID}"	
			}
			catch(error) {
				def updateIpPoolProd1 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"VMUUID\":\"${vmuuid}\",\"EnvironmentName\":\"${myEnvName}\"}", responseHandle: 'NONE', url: "http://${ecdbIP}/ip_pool?NetworkID=eq.${myProdNetworkId}&&IPAddress=eq.${myVmProdIp}&&Hostname=eq.${myHostname}&&Customer_ID=eq.${CustomerID}", validResponseCodes: '100:504'
				updateIpPoolProd1Response = readJSON text: updateIpPoolProd1.content
			
				//write to file for error handling
				sh """(echo Failed API call in updateIpPoolVmuuid.groovy; echo Reason: \"${updateIpPoolProd1Response.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				currentBuild.result = 'FAILURE'
				throw new hudson.AbortException("Failed API: ${updateIpPoolProd1Response.message}")
			}
				
		}
		//only 1 NIC, update tables for admin
		else {
			//Add update ip_pool admin network api here:
			try {
				def updateIpPoolAdmin2 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"VMUUID\":\"${vmuuid}\",\"EnvironmentName\":\"${myEnvName}\"}", responseHandle: 'NONE', url: "http://${ecdbIP}/ip_pool?NetworkID=eq.${myAdminNetworkId}&&IPAddress=eq.${myVmAdminIp}&&Hostname=eq.${myHostname}&&Customer_ID=eq.${CustomerID}"	
			}
			catch(error) {
				def updateIpPoolAdmin2 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"VMUUID\":\"${vmuuid}\",\"EnvironmentName\":\"${myEnvName}\"}", responseHandle: 'NONE', url: "http://${ecdbIP}/ip_pool?NetworkID=eq.${myAdminNetworkId}&&IPAddress=eq.${myVmAdminIp}&&Hostname=eq.${myHostname}&&Customer_ID=eq.${CustomerID}", validResponseCodes: '100:504'
				updateIpPoolAdmin2Response = readJSON text: updateIpPoolAdmin2.content
			
				//write to file for error handling
				sh """(echo Failed API call in updateIpPoolVmuuid.groovy; echo Reason: \"${updateIpPoolAdmin2Response.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				currentBuild.result = 'FAILURE'
				throw new hudson.AbortException("Failed API: ${updateIpPoolAdmin2Response.message}")
			}				
		}
	}
}