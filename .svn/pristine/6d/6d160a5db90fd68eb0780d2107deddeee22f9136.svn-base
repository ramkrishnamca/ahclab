#!/usr/bin/env groovy

def call(jsonData, vmBuildID, vmuuid, customerID) {
	echo "INSIDE OF assetTrackingVmDetails FUNCTION"
	
	stage("Asset Tracking - vm_asset, networkid_asset, and disk_asset") {
		
		def myEnvName = jsonData.vm_clone_data.EnvironmentName[0]
		def myHostname = jsonData.vm_clone_data.Hostname[0]
		def myOsType = jsonData.vm_clone_data.OSType[0]
		def myCpu = jsonData.vm_clone_data.CPU[0]
		def myMemoryGb = jsonData.vm_clone_data.Memory_GB[0]		
		def myTemplateName = jsonData.vm_clone_data.TemplateName[0]
		def myAdminNetworkId = ''		
		def myProdNetworkId = ''
		
		def multiNetworkId = false
		//2 NICs
		if (jsonData.vm_network_data.NetworkID.size() > 1) {
			myAdminNetworkId = jsonData.vm_network_data.NetworkID[0]
			myProdNetworkId = jsonData.vm_network_data.NetworkID[1]			
			multiNetworkId = true
		}
		//1 NIC
		else {
			myAdminNetworkId = jsonData.vm_network_data.NetworkID[0]
		} 		
		
		//Add insert vm_asset api here:
		try {
			def insertIntoVmAsset = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"VMUUID\":\"${vmuuid}\",\"EnvironmentName\":\"${myEnvName}\",\"Hostname\":\"${myHostname}\",\"OSType\":\"${myOsType}\",\"VMDeployed\":1,\"Built\":0,\"CPU\":${myCpu},\"Memory_GB\":${myMemoryGb},\"Customer_ID\":${customerID},\"MachineID\":\"${myHostname}\"}", responseHandle: 'NONE', url: "http://${ecdbIP}/vm_asset"	
		}
		catch(error) {
			def insertIntoVmAsset = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"VMUUID\":\"${vmuuid}\",\"EnvironmentName\":\"${myEnvName}\",\"Hostname\":\"${myHostname}\",\"OSType\":\"${myOsType}\",\"VMDeployed\":1,\"Built\":0,\"CPU\":${myCpu},\"Memory_GB\":${myMemoryGb},\"Customer_ID\":${customerID},\"MachineID\":\"${myHostname}\"}", responseHandle: 'NONE', url: "http://${ecdbIP}/vm_asset", validResponseCodes: '100:504'			
			insertIntoVmAssetResponse = readJSON text: insertIntoVmAsset.content
			
			//write to file for error handling
			sh """(echo Failed API call in assetTrackingVmDetails.groovy; echo Reason: \"${insertIntoVmAssetResponse.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
			currentBuild.result = 'FAILURE'
			throw new hudson.AbortException("Failed API: ${insertIntoVmAssetResponse.message}")
		}
		
		//null check for vm_disk_data. if null, use disk size from template(else section)
		if (!jsonData.vm_disk_data.equals(null)) {
			//looping all disks
			for (def index = 0; index < jsonData.vm_disk_data.size(); index++) {
			
				def myVolumeSizeGb = jsonData.vm_disk_data.VolumeSize_GB[index]
				//Add disk_asset api here:
				try {
					def insertIntoDiskAsset1 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"VMUUID\":\"${vmuuid}\",\"VolumeSize_GB\":${myVolumeSizeGb},\"SharedDisk\":0,\"SharedDisk_VM_Xref\":${vmBuildID}}", responseHandle: 'NONE', url: "http://${ecdbIP}/disk_asset"
				}
				catch(error) {
					def insertIntoDiskAsset1 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"VMUUID\":\"${vmuuid}\",\"VolumeSize_GB\":${myVolumeSizeGb},\"SharedDisk\":0,\"SharedDisk_VM_Xref\":${vmBuildID}}", responseHandle: 'NONE', url: "http://${ecdbIP}/disk_asset", validResponseCodes: '100:504'					
					insertIntoDiskAsset1Response = readJSON text: insertIntoDiskAsset1.content
					
					//write to file for error handling
					sh """(echo Failed API call in assetTrackingVmDetails.groovy; echo Reason: \"${insertIntoDiskAsset1Response.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
					currentBuild.result = 'FAILURE'
					throw new hudson.AbortException("Failed API: ${insertIntoDiskAsset1Response.message}")
				}
			}
		}
		else {
			//get template disk size:
			try {
				def getTemplateDiskDetails = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/vmtemplate_asset?select=OSDiskSize_GB&TemplateName=eq.${myTemplateName}"
				println('getTemplateDiskDetails from ECDB: '+getTemplateDiskDetails.content)
				templateDiskDetails = readJSON text: getTemplateDiskDetails.content
			}
			catch(error) {
				def getTemplateDiskDetails = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/vmtemplate_asset?select=OSDiskSize_GB&TemplateName=eq.${myTemplateName}", validResponseCodes: '100:504'				
				templateDiskDetails = readJSON text: getTemplateDiskDetails.content
				
				//write to file for error handling
				sh """(echo Failed API call in assetTrackingVmDetails.groovy; echo Reason: \"${templateDiskDetails.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				currentBuild.result = 'FAILURE'
				throw new hudson.AbortException("Failed API: ${templateDiskDetails.message}")
			}		
			//update disk_asset with template disk size:
			try {
				def insertIntoDiskAsset2 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"VMUUID\":\"${vmuuid}\",\"VolumeSize_GB\":${templateDiskDetails.OSDiskSize_GB[0]},\"SharedDisk\":0,\"SharedDisk_VM_Xref\":${vmBuildID}}", responseHandle: 'NONE', url: "http://${ecdbIP}/disk_asset"
			}
			catch(error) {
				def insertIntoDiskAsset2 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"VMUUID\":\"${vmuuid}\",\"VolumeSize_GB\":${templateDiskDetails.OSDiskSize_GB[0]},\"SharedDisk\":0,\"SharedDisk_VM_Xref\":${vmBuildID}}", responseHandle: 'NONE', url: "http://${ecdbIP}/disk_asset", validResponseCodes: '100:504'
				insertIntoDiskAsset2Response = readJSON text: insertIntoDiskAsset2.content
				
				//write to file for error handling
				sh """(echo Failed API call in assetTrackingVmDetails.groovy; echo Reason: \"${insertIntoDiskAsset2Response.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				currentBuild.result = 'FAILURE'
				throw new hudson.AbortException("Failed API: ${insertIntoDiskAsset2Response.message}")
			}
		}	
		
		//if 2 NICs then update tables for admin and prod
		if (multiNetworkId) {						   
			//Add insert networkid_asset admin and prod network api here:
			//Admin
			try {
				def insertIntoNetworkIdAssetAdmin1 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"NetworkID\":\"${myAdminNetworkId}\",\"VMUUID\":\"${vmuuid}\"}", responseHandle: 'NONE', url: "http://${ecdbIP}/networkid_asset"
			}
			catch(error) {
				def insertIntoNetworkIdAssetAdmin1 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"NetworkID\":\"${myAdminNetworkId}\",\"VMUUID\":\"${vmuuid}\"}", responseHandle: 'NONE', url: "http://${ecdbIP}/networkid_asset", validResponseCodes: '100:504'
				insertIntoNetworkIdAssetAdmin1Response = readJSON text: insertIntoNetworkIdAssetAdmin1.content
				
				//write to file for error handling
				sh """(echo Failed API call in assetTrackingVmDetails.groovy; echo Reason: \"${insertIntoNetworkIdAssetAdmin1Response.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				currentBuild.result = 'FAILURE'
				throw new hudson.AbortException("Failed API: ${insertIntoNetworkIdAssetAdmin1Response.message}")
			}		
			//Prod
			try {
				def insertIntoNetworkIdAssetProd1 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"NetworkID\":\"${myProdNetworkId}\",\"VMUUID\":\"${vmuuid}\"}", responseHandle: 'NONE', url: "http://${ecdbIP}/networkid_asset"
			}
			catch(error) {
				def insertIntoNetworkIdAssetProd1 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"NetworkID\":\"${myProdNetworkId}\",\"VMUUID\":\"${vmuuid}\"}", responseHandle: 'NONE', url: "http://${ecdbIP}/networkid_asset", validResponseCodes: '100:504'
				insertIntoNetworkIdAssetProd1Response = readJSON text: insertIntoNetworkIdAssetProd1.content
				
				//write to file for error handling
				sh """(echo Failed API call in assetTrackingVmDetails.groovy; echo Reason: \"${insertIntoNetworkIdAssetProd1Response.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				currentBuild.result = 'FAILURE'
				throw new hudson.AbortException("Failed API: ${insertIntoNetworkIdAssetProd1Response.message}")
			}			
		}
		//only 1 NIC, update tables for admin
		else {									 
			//Add insert networkid_asset admin network api here:
			try {
				def insertIntoNetworkIdAssetAdmin2 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"NetworkID\":\"${myAdminNetworkId}\",\"VMUUID\":\"${vmuuid}\"}", responseHandle: 'NONE', url: "http://${ecdbIP}/networkid_asset"
			}
			catch(error) {
				def insertIntoNetworkIdAssetAdmin2 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"NetworkID\":\"${myAdminNetworkId}\",\"VMUUID\":\"${vmuuid}\"}", responseHandle: 'NONE', url: "http://${ecdbIP}/networkid_asset", validResponseCodes: '100:504'
				insertIntoNetworkIdAssetAdmin2Response = readJSON text: insertIntoNetworkIdAssetAdmin2.content
				
				//write to file for error handling
				sh """(echo Failed API call in assetTrackingVmDetails.groovy; echo Reason: \"${insertIntoNetworkIdAssetAdmin2Response.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				currentBuild.result = 'FAILURE'
				throw new hudson.AbortException("Failed API: ${insertIntoNetworkIdAssetAdmin2Response.message}")
			}				
		}		
	}
}	
