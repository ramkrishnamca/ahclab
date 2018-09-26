#!/usr/bin/env groovy

def call(hostname, ipAddress, clientID, compBuildId, vmBuildID, accountUsername, accountPassword, Internet) {
    stage("Bootstrap Windows"){
    
	def recipesToBootstrap = ''
	def installItemString = ''
	
	  
	
	def response1 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"customer_id\":${clientID}, \"vm_build_id\":${vmBuildID}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/terraform_vsphere"
			def jsonData = readJSON text: response1.content 			
			
			CloudProvider = jsonData.vm_clone_data.CloudProvider[0]
			CloudEnvironment = jsonData.vm_clone_data.CloudEnvironment[0] 
	
	def nfsPathJson = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/system_property_asset?select=SystemPropertyValue&&SystemPropertyName=eq.nfsPath&&Customer_ID=eq.${clientID}&&CloudProvider=eq.${CloudProvider}&&CloudEnvironment=eq.${CloudEnvironment}", validResponseCodes: '100:504'
	          nfsPathOutput = readJSON text: nfsPathJson.content
		  nfsPath = nfsPathOutput.SystemPropertyValue[0]
	windowsNfsPath1 = nfsPath.replace("//", "\\")
	windowsNfsPath = windowsNfsPath1.replace("/", "\\")

	if (compBuildId != "noCG") {
		recipesToBootstrap = getBootstrapRecipes vmBuildID, compBuildId
		echo "recipesToBootstrap: ${recipesToBootstrap}" 
		
		installItemString = getInstallItems vmBuildID, compBuildId, clientID
	}
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
					    knife bootstrap windows winrm ${ipAddress} --winrm-user '${accountUsername}' --winrm-password '${accountPassword}' --node-name \"${hostname}\" --bootstrap-install-command "Powershell Start-Process -Wait -FilePath msiexec.exe -ArgumentList  ^'/qn /i ^\\^${windowsNfsPath}\\ddi\\chefInstall\\chef-client-14.3.37-1-x64.msi^'" --run-list '${recipesToBootstrap}' -j '{${installItemString}}'"""
						
						sh """cd /etc/chef/chef-repo
					    knife node run_list remove ${hostname} '${recipesToBootstrap}'"""
						}
				else if (!recipesToBootstrap?.trim() && Internet == "false")
						{
						echo "running offline bootstrap install, no bootstrap recipes"
						sh """cd /etc/chef/chef-repo
					    knife bootstrap windows winrm ${ipAddress} --winrm-user '${accountUsername}' --winrm-password '${accountPassword}' --node-name \"${hostname}\" --bootstrap-install-command "Powershell Start-Process -Wait -FilePath msiexec.exe -ArgumentList  ^'/qn /i ^\\^${windowsNfsPath}\\ddi\\chefInstall\\chef-client-14.3.37-1-x64.msi^'" """
						
						}
				else if (recipesToBootstrap?.trim() && Internet == "true") {
					sh """set +x
					cd /etc/chef/chef-repo
					knife bootstrap windows winrm ${ipAddress} --winrm-user '${accountUsername}' --winrm-password '${accountPassword}' --node-name \"${hostname}\" --run-list '${recipesToBootstrap}' -j '{${installItemString}}'"""
					sh """cd /etc/chef/chef-repo
					knife node run_list remove ${hostname} '${recipesToBootstrap}'"""
				}
				else{
					sh """set +x
					cd /etc/chef/chef-repo
					knife bootstrap windows winrm ${ipAddress} --winrm-user '${accountUsername}' --winrm-password '${accountPassword}' --node-name \"${hostname}\" """
				}
			}
		}
		catch(error) {
			echo "Bootstrap failed, remove node and client from Chef"
			//write to file for error handling
			sh """(echo Hostname: ${hostname}; echo IP Address: ${ipAddress}; Failed to bootstrap Windows; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""			
			
			sh """cd /etc/chef/chef-repo
			knife node delete ${hostname} -y
			knife client delete ${hostname} -y"""	
			
			currentBuild.result = 'FAILURE'
			throw new hudson.AbortException("Failed to bootstrap Windows")			
		}		
	//}	  
	  sh "sudo -u root /bin/bash -c 'echo -e \"${ipAddress}\t${hostname}\" >> /etc/hosts'"
	}
    
}