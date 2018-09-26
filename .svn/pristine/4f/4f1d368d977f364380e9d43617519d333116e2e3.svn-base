#!/usr/bin/env groovy

def call(customerID, vmBuildID, compBuildId, vmDetails) {

				stage("Provision VM vmBuildID: ${vmBuildID}"){
				def jsonData
				try {
					def response1 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"customer_id\":${customerID}, \"vm_build_id\":${vmBuildID}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/terraform_vsphere"
                    
					println('Response1 from ECDB: '+response1.content)
                    
					jsonData = readJSON text: response1.content
				}
				catch(error) {
					def response1 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"customer_id\":${customerID}, \"vm_build_id\":${vmBuildID}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/terraform_vsphere", validResponseCodes: '100:504'
					jsonData = readJSON text: response1.content
					
					//write to file for error handling
					sh """(echo Failed API call in provisionVmVsphere.groovy; echo Reason: \"${jsonData.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
					currentBuild.result = 'FAILURE'
					throw new hudson.AbortException("Failed API: ${jsonData.message}")
				}
				
				def CloudProvider = jsonData.vm_clone_data.CloudProvider[0]
				def CloudEnvironment = jsonData.vm_clone_data.CloudEnvironment[0]
				def sysProps = jsonData.provider_data
				echo "sysprops ${sysProps}"
				def vSphereServer = sysProps.find { it['SystemPropertyName'] ==  'vsphere_server' }?.get("SystemPropertyValue")
				echo "vsphere server ${vSphereServer}"
				def networkData = jsonData.vm_network_data
				adminNetworkId = getAdminNetworkId CloudProvider, CloudEnvironment
				def ipAddress = networkData.find { it['NetworkID'] ==  "${adminNetworkId}" }?.get("IPAddress")
				//def ipAddress = jsonData.vm_network_data.IPAddress[0]
				def hostname = jsonData.vm_clone_data.Hostname[0]
				def os = jsonData.vm_clone_data.OSType[0]
				echo "${ipAddress} ${hostname} ${os}"
				
				def vmuuid = vmDetails.vmuuid[0]
				if (vmDetails.vmuuid[0].equals(null) || vmDetails.vmuuid[0] == "") {
					def directoryName = "vmBuildID${vmBuildID}"
					echo "directory name ${directoryName}"
					def tfFileString = generateVsphereVmTF jsonData
					
					def subdir = new File("$JENKINS_HOME/terraform/${directoryName}")
					subdir.mkdir()
									//sh "mkdir -p $JENKINS_HOME/terraform/`date +%y%m%d%H%M%s`${directoryName}"
					writeFile file: "$JENKINS_HOME/terraform/${directoryName}/cloneVm.tf", text: "${tfFileString}"	
					accountName = "vSphere_Admin"	
					
					vSphereAccount = getSystemAccount customerID, CloudEnvironment, CloudProvider, accountName
					vmuuid = runTerraform directoryName, vSphereServer, hostname, ipAddress, vSphereAccount
					echo "vmuuid var = ${vmuuid}"				
					
					updateIpPoolVmuuid jsonData, vmuuid
					assetTrackingVmDetails jsonData, vmBuildID, vmuuid, customerID
					updateVmBuildVmuuid vmBuildID, vmuuid, hostname
				}

				//get account info
				echo "-----getting account info for bootstrapping------"
				def runAs = jsonData.vm_clone_data.RunAs[0]					
				
				accountUser = getSystemAccount customerID, CloudEnvironment, CloudProvider, runAs		
			
			//def accountUser
			def username = accountUser[0]
			def password = accountUser[1]
			
				
			/*	try {
					def accountUserResponse = httpRequest consoleLogResponseBody: false, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'LEAVE_OPEN', url: "http://${ecdbIP}/v-system_account_asset?select=Username,Password&&Customer_ID=eq.${customerID}&&CloudProvider=eq.${CloudProvider}&&CloudEnvironment=eq.${CloudEnvironment}&&AccountTitle=eq.${runAs}"                                                     
	        
					accountUser = readJSON text: accountUserResponse.content
					username =  accountUser.Username[0]
					password = accountUser.Password[0]
				}
				catch(error) {
					def accountUserResponse = httpRequest consoleLogResponseBody: false, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'LEAVE_OPEN', url: "http://${ecdbIP}/v-system_account_asset?select=Username,Password&&Customer_ID=eq.${customerID}&&CloudProvider=eq.${CloudProvider}&&CloudEnvironment=eq.${CloudEnvironment}&&AccountTitle=eq.${runAs}", validResponseCodes: '100:504'					
					accountUser = readJSON text: accountUserResponse.content
					
					//write to file for error handling
					sh """(echo Failed API call in provisionVmVsphere.groovy; echo Reason: \"${accountUser.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
					currentBuild.result = 'FAILURE'
					throw new hudson.AbortException("Failed API: ${accountUser.message}")
				}
			*/
				
				//sleep 30
				
				for (def count = 1; count <= 30; count++) {
				def testPing = sh (
					script: "ping -c 10 ${ipAddress} | awk '/packets transmitted/ {print \$6}' | sed 's/\\%//g'",
					returnStdout: true
				) 
				if (testPing.toInteger() == 0) {
					break
				}
				else if (count == 30) {
					//write to file for error handling
					sh """(echo Hostname: ${hostname}; echo IP Address: ${ipAddress}; echo Ping Test failed after powering on VM; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
					
					currentBuild.result = 'FAILURE'
					throw new hudson.AbortException('Failed to ping OS')
					break
				}
				else {
					sleep(2)
				}
			}

			def InternetAccessibleResponse = httpRequest consoleLogResponseBody: false, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'LEAVE_OPEN', url: "http://${ecdbIP}/cloud_provider_asset?select=InternetAccessible&&Customer_ID=eq.${customerID}&&CloudProvider=eq.${CloudProvider}&&CloudEnvironment=eq.${CloudEnvironment}"
			
			InternetAccessibleFlag = readJSON text: InternetAccessibleResponse.content

			Internet = "${InternetAccessibleFlag.InternetAccessible[0]}"
			echo "is this VM internet Accessible: ${Internet}"
            

			if(os.toLowerCase() == 'linux'){
				bootstrapChefvSphereLinux hostname, ipAddress, customerID, compBuildId, vmBuildID, username, password, Internet 
				updateInstallItemBootstrap vmBuildID, compBuildId
				}
			else if(os.toLowerCase() == 'windows'){
				bootstrapChefvSphereWindows hostname, ipAddress, customerID, compBuildId, vmBuildID, username, password, Internet 
				updateInstallItemBootstrap vmBuildID, compBuildId
				}
			else{
         			echo "incorrect os type"
       				}
					
			return vmuuid
	}
}