#!/usr/bin/env groovy

def call(clientID, vmBuildId, varString, compBuildId) {
	println("inside createParamterFile function")
	stage("Create Chef Parameter File"){
		def networkData
		def adminNetworkId
		def ipAddress
		def hostname
		def os
		def CloudProvider
		def CloudEnvironment	
		
	
		try {
			def response1 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"customer_id\":${clientID}, \"vm_build_id\":${vmBuildId}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/terraform_vsphere"
			def jsonData = readJSON text: response1.content 			
			
			CloudProvider = jsonData.vm_clone_data.CloudProvider[0]
			CloudEnvironment = jsonData.vm_clone_data.CloudEnvironment[0] 
			networkData = jsonData.vm_network_data
			adminNetworkId = getAdminNetworkId CloudProvider, CloudEnvironment
			ipAddress = networkData.find { it['NetworkID'] ==  "${adminNetworkId}" }?.get("IPAddress")
			hostname = jsonData.vm_clone_data.Hostname[0]
			os = jsonData.vm_clone_data.OSType[0] 			
		}
		catch(error) {
			def response1 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"customer_id\":${clientID}, \"vm_build_id\":${vmBuildId}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/terraform_vsphere", validResponseCodes: '100:504'
			def jsonData = readJSON text: response1.content  
			
			//write to file for error handling
			sh """(echo Failed API call in createParameterFile.groovy; echo Reason: \"${jsonData.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
			currentBuild.result = 'FAILURE'
			throw new hudson.AbortException("Failed API: ${jsonData.message}")
		} 

		def nfsPathJson = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/system_property_asset?select=SystemPropertyValue&&SystemPropertyName=eq.nfsPath&&Customer_ID=eq.${clientID}&&CloudProvider=eq.${CloudProvider}&&CloudEnvironment=eq.${CloudEnvironment}", validResponseCodes: '100:504'
				        
                  nfsPathOutput = readJSON text: nfsPathJson.content
				        
                  nfsPath = nfsPathOutput.SystemPropertyValue[0]
				        
		  echo "the nfs path ${nfsPath}"
    
		//create files on linux
		if(os.toLowerCase() == 'linux'){
			varString = varString.replace("\\", "\\\\")
			commandToRun = """cat > /etc/chef/variable.json<< EOL
			{${varString}}
EOL"""			
			sh """set +x
			sudo -u root cd /root/.ssh
			sudo ssh -i /root/.ssh/${clientID}-id_rsa ${ipAddress} /bin/bash -c '${commandToRun}'"""

        }
         //create file on windows
		else if( os.toLowerCase() == 'windows'){
			def componentvmBuildId
			try {
				def componentvmBuildIdJson = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_vm_build?select=Component_VM_Build_ID&&VM_Build_ID=eq.${vmBuildId}&&Component_Build_ID=eq.${compBuildId}"
				componentvmBuildId = readJSON text: componentvmBuildIdJson.content
				componentvmBuildId  = componentvmBuildId.Component_VM_Build_ID[0]
			}
			catch(error) {
				def componentvmBuildIdJson = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_vm_build?select=Component_VM_Build_ID&&VM_Build_ID=eq.${vmBuildId}&&Component_Build_ID=eq.${compBuildId}", validResponseCodes: '100:504'
				componentvmBuildId = readJSON text: componentvmBuildIdJson.content
				
				//write to file for error handling
				sh """(echo Failed API call in createParameterFile.groovy; echo Reason: \"${componentvmBuildId.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				currentBuild.result = 'FAILURE'
				throw new hudson.AbortException("Failed API: ${componentvmBuildId.message}")
			}
			try {
				def installItemsResponse = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], 	responseHandle: 'NONE', url: "http://${ecdbIP}/v-install_item_build?select=ObjectID,ItemType,RunAs,BuildOrder&&Component_VM_Build_ID=eq.${componentvmBuildId}&&Built=eq.false&&ItemType=neq.Bootstrap&&ItemType=neq.Jenkins&&order=BuildOrder.asc"
				
				def installItems = readJSON text: installItemsResponse.content        	
				recipeName = installItems.ObjectID[0]
				itemType = installItems.ItemType[0]
				runAs = installItems.RunAs[0]
				buildOrder = installItems.BuildOrder[0]
			}
			catch(error) {
				def installItemsResponse = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], 	responseHandle: 'NONE', url: "http://${ecdbIP}/v-install_item_build?select=ObjectID,ItemType,RunAs,BuildOrder&&Component_VM_Build_ID=eq.${componentvmBuildId}&&Built=eq.false&&ItemType=neq.Bootstrap&&ItemType=neq.Jenkins&&order=BuildOrder.asc", validResponseCodes: '100:504'				
				def installItems = readJSON text: installItemsResponse.content
				
				//write to file for error handling
				sh """(echo Failed API call in createParameterFile.groovy; echo Reason: \"${installItems.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				currentBuild.result = 'FAILURE'
				throw new hudson.AbortException("Failed API: ${installItems.message}")
			}
			
			if("${runAs}" != "null" || "${runAs}" != "" ){ 	
			def accountUser = getSystemAccount clientID, CloudEnvironment, CloudProvider, runAs		
						
			 accountUsername = accountUser[0]
			 accountPassword = accountUser[1]
			
                varString = varString.replace("\\", "\\\\")
				varString = varString.replace("\n", "`r`n")
				varString = varString.replace('"', '`"')
	
			  sh """cd /etc/chef/chef-repo
    knife winrm 'name:${hostname}' --winrm-shell powershell 'Set-Content -Path "C:\\Users\\Administrator\\Desktop\\variable.json" -Value "{${varString}}"' --winrm-user '${accountUsername}' --winrm-password '${accountPassword}'"""
   
				}
			else{
				echo "account user was empty, no need to create parameter file"
				}
		}
		else{
			echo "incorrect os type"
		}
	}
}