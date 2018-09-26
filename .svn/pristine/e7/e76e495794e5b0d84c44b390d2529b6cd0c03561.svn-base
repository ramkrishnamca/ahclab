#!/usr/bin/env groovy

def call() {
	echo "INSIDE OF assetTrackingPostActions FUNCTION"
	
	try {
		def getEnvName = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-environment_build?select=EnvironmentName&Build_ID=eq.${BuildID}"		
		println('getEnvName from ECDB: '+getEnvName.content)                        
		envNameResponse = readJSON text: getEnvName.content
		
		envName = envNameResponse.EnvironmentName[0]
	}
	catch(error) {
		def getEnvName = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-environment_build?select=EnvironmentName&Build_ID=eq.${BuildID}", validResponseCodes: '100:504'
		envNameResponse = readJSON text: getEnvName.content
		
		//write to file for error handling
		sh """(echo Failed API call in assetTrackingPostActions.groovy; echo Reason: \"${envNameResponse.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""	
			
		return envNameResponse.message			
	}
	
	try {
		def getComponentGroupIDs = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_group?select=Component_Group_ID,Built,ComponentGroupName,Component_Group_Build_ID&Build_ID=eq.${BuildID}"		
		println('getComponentGroupIDs from ECDB: '+getComponentGroupIDs.content)                        
		componentGroupIDsBuiltResponse = readJSON text: getComponentGroupIDs.content
	}
	catch(error) {
		def getComponentGroupIDs = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_group?select=Component_Group_ID,Built,ComponentGroupName,Component_Group_Build_ID&Build_ID=eq.${BuildID}", validResponseCodes: '100:504'
		componentGroupIDsBuiltResponse = readJSON text: getComponentGroupIDs.content
		
		//write to file for error handling
		sh """(echo Failed API call in assetTrackingPostActions.groovy; echo Reason: \"${componentGroupIDsBuiltResponse.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""	
			
		return componentGroupIDsBuiltResponse.message
	}	
	
	for (def index = 0; index < componentGroupIDsBuiltResponse.size(); index++) {
		def componentGroupID = componentGroupIDsBuiltResponse.Component_Group_ID[index]
		def componentGroupBuilt = componentGroupIDsBuiltResponse.Built[index]
		def componentGroupName = componentGroupIDsBuiltResponse.ComponentGroupName[index]
		def componentGroupBuildID = componentGroupIDsBuiltResponse.Component_Group_Build_ID[index]
		
		try {
			def getComponentGroupAsset = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_group_asset?select=Built&Component_Group_ID=eq.${componentGroupID}&&EnvironmentName=eq.${envName}&&Customer_ID=eq.${CustomerID}"		
			println('getComponentGroupAsset from ECDB: '+getComponentGroupAsset.content)                        
			componentGroupAssetResponse = readJSON text: getComponentGroupAsset.content
		}
		catch(error) {
			def getComponentGroupAsset = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_group_asset?select=Built&Component_Group_ID=eq.${componentGroupID}&&EnvironmentName=eq.${envName}&&Customer_ID=eq.${CustomerID}", validResponseCodes: '100:504'
			componentGroupAssetResponse = readJSON text: getComponentGroupAsset.content
			
			//write to file for error handling
			sh """(echo Failed API call in assetTrackingPostActions.groovy; echo Reason: \"${componentGroupAssetResponse.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				
			return componentGroupAssetResponse.message
		}			
		
		if (componentGroupAssetResponse.size() < 1) {
			//need to insert into component_group_asset
			try {
				def insertIntoComponentGroupAsset = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"EnvironmentName\":\"${envName}\",\"Component_Group_ID\":${componentGroupID},\"ComponentGroupName\":\"${componentGroupName}\",\"Built\":${componentGroupBuilt},\"Customer_ID\":${CustomerID}}", responseHandle: 'NONE', url: "http://${ecdbIP}/component_group_asset"
			}
			catch(error) {
				def insertIntoComponentGroupAsset = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"EnvironmentName\":\"${envName}\",\"Component_Group_ID\":${componentGroupID},\"ComponentGroupName\":\"${componentGroupName}\",\"Built\":${componentGroupBuilt},\"Customer_ID\":${CustomerID}}", responseHandle: 'NONE', url: "http://${ecdbIP}/component_group_asset", validResponseCodes: '100:504'				
				insertIntoComponentGroupAssetResponse = readJSON text: insertIntoComponentGroupAsset.content
			
				//write to file for error handling
				sh """(echo Failed API call in assetTrackingPostActions.groovy; echo Reason: \"${insertIntoComponentGroupAssetResponse.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
					
				return insertIntoComponentGroupAssetResponse.message
			}				
		}
		else if (componentGroupAssetResponse.Built[0].equals(false) && componentGroupIDsBuiltResponse.Built[index].equals(true)) {
			//update component_group_asset, set built = true
			try {
				def updateComponentGroupAsset = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"Built\":1}", responseHandle: 'NONE', url: "http://${ecdbIP}/component_group_asset?Component_Group_ID=eq.${componentGroupID}&&EnvironmentName=eq.${envName}&&Customer_ID=eq.${CustomerID}"	
			}
			catch(error) {
				def updateComponentGroupAsset = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"Built\":1}", responseHandle: 'NONE', url: "http://${ecdbIP}/component_group_asset?Component_Group_ID=eq.${componentGroupID}&&EnvironmentName=eq.${envName}&&Customer_ID=eq.${CustomerID}", validResponseCodes: '100:504'
				updateComponentGroupAssetResponse = readJSON text: updateComponentGroupAsset.content
			
				//write to file for error handling
				sh """(echo Failed API call in assetTrackingPostActions.groovy; echo Reason: \"${updateComponentGroupAssetResponse.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
					
				return updateComponentGroupAssetResponse.message
			}				
		}
		else {
			echo "record already there, no update for component_group_asset"
		}
		
		try {
			def getComponentGroupAssetID = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/component_group_asset?select=ID&Component_Group_ID=eq.${componentGroupID}&&EnvironmentName=eq.${envName}&&Customer_ID=eq.${CustomerID}"		
			println('getComponentGroupAssetID from ECDB: '+getComponentGroupAssetID.content)                        
			componentGroupAssetIDResponse = readJSON text: getComponentGroupAssetID.content
		
			componentGroupAssetID = componentGroupAssetIDResponse.ID[0]
		}
		catch(error) {
			def getComponentGroupAssetID = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/component_group_asset?select=ID&Component_Group_ID=eq.${componentGroupID}&&EnvironmentName=eq.${envName}&&Customer_ID=eq.${CustomerID}", validResponseCodes: '100:504'
			componentGroupAssetIDResponse = readJSON text: getComponentGroupAssetID.content
			
			//write to file for error handling
			sh """(echo Failed API call in assetTrackingPostActions.groovy; echo Reason: \"${componentGroupAssetIDResponse.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				
			return componentGroupAssetIDResponse.message
		}

		try {
			def getComponent = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component?select=ComponentName,Component_ID,Component_Build_ID&Component_Group_Build_ID=eq.${componentGroupBuildID}"		
			println('getComponent from ECDB: '+getComponent.content)                        
			componentResponse = readJSON text: getComponent.content		
		}
		catch(error) {
			def getComponent = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component?select=ComponentName,Component_ID,Component_Build_ID&Component_Group_Build_ID=eq.${componentGroupBuildID}", validResponseCodes: '100:504'
			componentResponse = readJSON text: getComponent.content	
			
			//write to file for error handling
			sh """(echo Failed API call in assetTrackingPostActions.groovy; echo Reason: \"${componentResponse.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				
			return componentResponse.message
		}	
		
		for (def index2 = 0; index2 < componentResponse.size(); index2++) {
			def componentName = componentResponse.ComponentName[index2]
			def componentID = componentResponse.Component_ID[index2]
			def componentBuildID = componentResponse.Component_Build_ID[index2]			
			
			try {
				def getVmuuid = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_vm_build?select=VMUUID&Component_Build_ID=eq.${componentBuildID}"		
				println('getVmuuid from ECDB: '+getVmuuid.content)                        
				vmuuidResponse = readJSON text: getVmuuid.content
			}
			catch(error) {
				def getVmuuid = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_vm_build?select=VMUUID&Component_Build_ID=eq.${componentBuildID}", validResponseCodes: '100:504'
				vmuuidResponse = readJSON text: getVmuuid.content
				
				//write to file for error handling
				sh """(echo Failed API call in assetTrackingPostActions.groovy; echo Reason: \"${vmuuidResponse.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
					
				return vmuuidResponse.message
			}				
			
			for (def index3 = 0; index3 < vmuuidResponse.size(); index3++) {
				def vmuuid = vmuuidResponse.VMUUID[index3]
			
				//if vmuuid is null the vm is not provisioned, no need to continue. 
				if (!vmuuidResponse.VMUUID[index3].equals(null) && vmuuid.length() > 0) {			
					try {
						def getComponentAsset = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_asset?select=Component_VM_Build_Built,Component_Asset_ID,Component_Asset_Built&Component_Build_ID=eq.${componentBuildID}&&VMUUID=eq.${vmuuid}"
						println('getComponentAsset from ECDB: '+getComponentAsset.content)                        
						componentAssetResponse = readJSON text: getComponentAsset.content	
					}
					catch(error) {
						def getComponentAsset = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_asset?select=Component_VM_Build_Built,Component_Asset_ID,Component_Asset_Built&Component_Build_ID=eq.${componentBuildID}&&VMUUID=eq.${vmuuid}", validResponseCodes: '100:504'
						componentAssetResponse = readJSON text: getComponentAsset.content
						
						//write to file for error handling
						sh """(echo Failed API call in assetTrackingPostActions.groovy; echo Reason: \"${componentAssetResponse.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
							
						return componentAssetResponse.message
					}				
					
					if (componentAssetResponse.Component_Asset_ID[0].equals(null)) {
						//need to insert record - component_asset
						echo "insert record - component_asset"
						try {
							def insertIntoComponentAsset = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"Component_Group_Asset_ID\":${componentGroupAssetID},\"Component_ID\":${componentID},\"VMUUID\":\"${vmuuid}\",\"ComponentName\":\"${componentName}\",\"Built\":${componentAssetResponse.Component_VM_Build_Built[0]}}", responseHandle: 'NONE', url: "http://${ecdbIP}/component_asset"
						}
						catch(error) {
							def insertIntoComponentAsset = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"Component_Group_Asset_ID\":${componentGroupAssetID},\"Component_ID\":${componentID},\"VMUUID\":\"${vmuuid}\",\"ComponentName\":\"${componentName}\",\"Built\":${componentAssetResponse.Component_VM_Build_Built[0]}}", responseHandle: 'NONE', url: "http://${ecdbIP}/component_asset", validResponseCodes: '100:504'							
							insertIntoComponentAssetResponse = readJSON text: insertIntoComponentAsset.content
						
							//write to file for error handling
							sh """(echo Failed API call in assetTrackingPostActions.groovy; echo Reason: \"${insertIntoComponentAssetResponse.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
								
							return insertIntoComponentAssetResponse.message
						}					
					}
					else if (componentAssetResponse.Component_Asset_Built[0].equals(false) && componentAssetResponse.Component_VM_Build_Built[0].equals(true)) {
						//update component_asset, set built = true"
						echo "update record - component_asset"
						componentName = componentName.replace(" ", "%20")
						try {
							def updateComponentAsset = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"Built\":1}", responseHandle: 'NONE', url: "http://${ecdbIP}/component_asset?Component_Group_Asset_ID=eq.${componentGroupAssetID}&&Component_ID=eq.${componentID}&&VMUUID=eq.${vmuuid}&&ComponentName=eq.${componentName}"	
						}
						catch(error) {
							def updateComponentAsset = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"Built\":1}", responseHandle: 'NONE', url: "http://${ecdbIP}/component_asset?Component_Group_Asset_ID=eq.${componentGroupAssetID}&&Component_ID=eq.${componentID}&&VMUUID=eq.${vmuuid}&&ComponentName=eq.${componentName}", validResponseCodes: '100:504'
							
							updateComponentAssetResponse = readJSON text: updateComponentAsset.content
						
							//write to file for error handling
							sh """(echo Failed API call in assetTrackingPostActions.groovy; echo Reason: \"${updateComponentAssetResponse.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
								
							return updateComponentAssetResponse.message
						}							
					}
					else {
						echo "record already there, no update for component_asset"
					}					
				}
			}
		}	
	}
}