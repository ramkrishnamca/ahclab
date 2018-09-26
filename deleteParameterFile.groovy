#!/usr/bin/env groovy

def call(hostname, ipAddress, clientID, os, CloudProvider, CloudEnvironment, componentvmBuildId) {
	stage("Delete Chef Paramter File"){
			try {
				def installItemsResponse = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], 	responseHandle: 'NONE', url: "http://${ecdbIP}/v-install_item_build?select=ObjectID,ItemType,RunAs,BuildOrder&&Component_VM_Build_ID=eq.${componentvmBuildId}&&ItemType=neq.Bootstrap&&order=BuildOrder.desc"
				def installItems = readJSON text: installItemsResponse.content        	
				runAs = installItems.RunAs[0]
				}
			catch(error) {
				def installItemsResponse = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], 	responseHandle: 'NONE', url: "http://${ecdbIP}/v-install_item_build?select=ObjectID,ItemType,RunAs,BuildOrder&&Component_VM_Build_ID=eq.${componentvmBuildId}&&ItemType=neq.Bootstrap&&order=BuildOrder.desc", validResponseCodes: '100:504'				
				def installItems = readJSON text: installItemsResponse.content
				
				//write to file for error handling
				sh """(echo Failed API call in createParameterFile.groovy; echo Reason: \"${installItems.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				currentBuild.result = 'FAILURE'
				throw new hudson.AbortException("Failed API: ${installItems.message}")
			}
		
			def accountUsername
			def accountPassword
			try {
				def accountUserResponse = httpRequest consoleLogResponseBody: false, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'LEAVE_OPEN', url: "http://${ecdbIP}/v-system_account_asset?select=Username,Password&&Customer_ID=eq.${clientID}&&CloudProvider=eq.${CloudProvider}&&CloudEnvironment=eq.${CloudEnvironment}&&AccountTitle=eq.${runAs}" 
				                                                        
        		def accountUser = readJSON text: accountUserResponse.content
				accountUsername =  accountUser.Username[0]
				accountPassword = accountUser.Password[0]
			}
			catch(error) {
				def accountUserResponse = httpRequest consoleLogResponseBody: false, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'LEAVE_OPEN', url: "http://${ecdbIP}/v-system_account_asset?select=Username,Password&&Customer_ID=eq.${clientID}&&CloudProvider=eq.${CloudProvider}&&CloudEnvironment=eq.${CloudEnvironment}&&AccountTitle=eq.${runAs}", validResponseCodes: '100:504'
				def accountUser = readJSON text: accountUserResponse.content
				
				//write to file for error handling
				sh """(echo Failed API call in createParameterFile.groovy; echo Reason: \"${accountUser.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				currentBuild.result = 'FAILURE'
				throw new hudson.AbortException("Failed API: ${accountUser.message}")
			}
			
	//delete Chef Parameter File
			if(os.toLowerCase() == 'linux'){
				echo "Deleting Chef Parameter File on Linux"
				commandToRun = "rm -f /etc/chef/variable.json"
				sh """sudo -u root cd /root/.ssh
				sudo ssh -i /root/.ssh/${clientID}-id_rsa ${ipAddress} rm -f /etc/chef/variable.json"""
			}
			else if( os.toLowerCase() == 'windows'){	
				if("${accountUsername}" != "null"){	
				echo "Deleting Chef Parameter File on Windows"
				sh """cd /etc/chef/chef-repo
				knife winrm 'name:${hostname}' 'find \"C:\\Users\\Administrator\\Desktop\\variable.json\" -delete -Force' --winrm-user '${accountUsername}' --winrm-password '${accountPassword}'"""
				}
				else{
					echo "account user was empty, no need to delete parameter file"
				}
			}
		}
	}