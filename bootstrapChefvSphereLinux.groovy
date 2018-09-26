#!/usr/bin/env groovy

def call(hostname, ipAddress, clientID, compBuildId, vmBuildID, accountUsername, accountPassword, Internet) {
    stage("bootstrap Linux"){
		def recipesToBootstrap = ''
		def installItemString = ''
		def key = ''

def response1 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"customer_id\":${clientID}, \"vm_build_id\":${vmBuildID}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/terraform_vsphere"
			def jsonData = readJSON text: response1.content 			
			
			CloudProvider = jsonData.vm_clone_data.CloudProvider[0]
			CloudEnvironment = jsonData.vm_clone_data.CloudEnvironment[0] 
		
 def nfsPathJson = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/system_property_asset?select=SystemPropertyValue&&SystemPropertyName=eq.nfsPath&&Customer_ID=eq.${clientID}&&CloudProvider=eq.${CloudProvider}&&CloudEnvironment=eq.${CloudEnvironment}", validResponseCodes: '100:504'
                  nfsPathOutput = readJSON text: nfsPathJson.content		        
                  nfsPath = nfsPathOutput.SystemPropertyValue[0]

		if (compBuildId == "noCG") {
			sh "sudo -u root /bin/bash -c 'ssh-keyscan -H ${ipAddress}>> /root/.ssh/known_hosts'"
			def keyOutput = sh script:"sudo -u root cat /root/.ssh/${clientID}-id_rsa.pub", returnStdout: true 
			key = keyOutput.trim()		
		}
		else {
			recipesToBootstrap = getBootstrapRecipes vmBuildID, compBuildId
			echo "recipesToBootstrap: ${recipesToBootstrap}"
	 
			//confgiure key on jenkins host
			sh "sudo -u root /bin/bash -c 'ssh-keyscan -H ${ipAddress}>> /root/.ssh/known_hosts'"
		  
			//install chef and run ssh_config to remote node
			def keyOutput = sh script:"sudo -u root cat /root/.ssh/${clientID}-id_rsa.pub", returnStdout: true 
			key = keyOutput.trim()

			installItemString = getInstallItems vmBuildID, compBuildId, clientID
			if (installItemString?.trim()) {
				installItemString = """,
				${installItemString}""" 
			}
		}			
		installItemString = installItemString.replace("\\", "\\\\")
		//commenting this out for now, dont think its being used
		//withCredentials([usernamePassword(credentialsId: '7d3a38b7-5b98-4826-9908-dfdedd3e1de1', passwordVariable: 'password', usernameVariable: 'username')]) { 
			def sleepTimer = 0
			try {
				retry(10) {
					
					sleep(sleepTimer)
					if (sleepTimer > 0) {
						sh """cd /etc/chef/chef-repo
						knife node delete ${hostname} -y
						knife client delete ${hostname} -y"""
					}
					sleepTimer = 10
					if (recipesToBootstrap?.trim() && Internet == "false")
						{
						echo "running offline bootstrap install with recipes"
						sh """cd /etc/chef/chef-repo
					    knife bootstrap ${ipAddress} --sudo -x ${accountUsername} -P ${accountPassword} -N \"${hostname}\" -r '${recipesToBootstrap}' --bootstrap-install-command 'git clone git://10.65.43.79/ddi_git/; rpm -Uvh ddi_git/chef-14.3.37-1.el7.x86_64.rpm; echo "10.65.43.78     a1ddi9laut3003.laas.ahclab.local" >> /etc/hosts' -j '{"key":"${key}"${installItemString}}'"""
						sh """cd /etc/chef/chef-repo
					    knife node run_list remove ${hostname} '${recipesToBootstrap}'"""
						}
					else if (!recipesToBootstrap?.trim() && Internet == "false")
						{
						echo "running offline bootstrap install, no bootstrap recipes"
						sh """cd /etc/chef/chef-repo
					   knife bootstrap ${ipAddress} --sudo -x ${accountUsername} -P ${accountPassword} -N \"${hostname}\" --bootstrap-install-command \"git clone git://10.65.43.79/ddi_git/; rpm -Uvh ddi_git/chef-14.3.37-1.el7.x86_64.rpm; echo '10.65.43.78     a1ddi9laut3003.laas.ahclab.local' >> /etc/hosts\" """
					   }
						
					else if (recipesToBootstrap?.trim() && Internet == "true") {
						sh """set +x
						cd /etc/chef/chef-repo
						knife bootstrap ${ipAddress} --sudo -x ${accountUsername} -P ${accountPassword} -N \"${hostname}\" -r '${recipesToBootstrap}' -j '{"key":"${key}"${installItemString}}'"""
						//remove ssh_config from run list
						sh """cd /etc/chef/chef-repo
						knife node run_list remove ${hostname} '${recipesToBootstrap}'"""
					}
					else{
						sh """set +x
						cd /etc/chef/chef-repo
						knife bootstrap ${ipAddress} --sudo -x ${accountUsername} -P ${accountPassword} -N \"${hostname}\" """
					 
					}
				}
			}
			catch(error) {
				echo "Bootstrap failed, remove node and client from Chef"
				//write to file for error handling
				sh """(echo Hostname: ${hostname}; echo IP Address: ${ipAddress}; Failed to bootstrap Linux; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				
				sh """cd /etc/chef/chef-repo
				knife node delete ${hostname} -y
				knife client delete ${hostname} -y"""					
				
				currentBuild.result = 'FAILURE'
				throw new hudson.AbortException("Failed to bootstrap Linux")				
			}
		//}
	}
}