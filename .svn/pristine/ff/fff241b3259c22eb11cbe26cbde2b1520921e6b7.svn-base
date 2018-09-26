#!/usr/bin/env groovy

def call(data) {
			stage("generate vSphere Vm terraform file"){
				def customerID = data.Customer_ID
				def sysProps = data.provider_data
				def vsphereDatacenter = sysProps.find { it['SystemPropertyName'] ==  'vsphere_datacenter' }?.get("SystemPropertyValue")
				def vsphereDatastore  = sysProps.find { it['SystemPropertyName'] ==  'vsphere_datastore' }?.get("SystemPropertyValue")
				def vsphereResourcePool = sysProps.find { it['SystemPropertyName'] ==  'vsphere_resource_pool' }?.get("SystemPropertyValue")
				def windowsTimeZoneID = sysProps.find { it['SystemPropertyName'] ==  'windowsTimeZoneID' }?.get("SystemPropertyValue")
				def vsphereVirtualMachineTemplate = data.vm_clone_data.TemplateName[0]
				def vsphereNetwork = data.vm_network_data.NetworkID
				def os = data.vm_clone_data.OSType[0]
				def vmName = data.vm_clone_data.Hostname[0]
				def cpu = data.vm_clone_data.CPU[0]
				def memory = data.vm_clone_data.Memory_GB[0] * 1024				
				def folder = sysProps.find { it['SystemPropertyName'] ==  'vsphere_folder' }?.get("SystemPropertyValue")
				
				def CloudProvider = data.vm_clone_data.CloudProvider[0]
				def CloudEnvironment = data.vm_clone_data.CloudEnvironment[0]
				
				def networkData = data.vm_network_data				
				def adminNetworkId = getAdminNetworkId CloudProvider, CloudEnvironment			
				def primaryNetwork  = networkData.find { it['NetworkID'] ==  "${adminNetworkId}" }
				def IpAddress =  [primaryNetwork.IPAddress]
				def subnetmask  = [primaryNetwork.Netmask_Bits]
				
				if( data.vm_network_data.IPAddress.size() > 1){
					def secondNetwork = networkData.find { it['NetworkID'] !=  "${adminNetworkId}" }
					IpAddress.add(secondNetwork.IPAddress)
					subnetmask.add(secondNetwork.Netmask_Bits)
				}				
				
				def gateway = networkData.find { it['NetworkID'] ==  "${adminNetworkId}" }?.get("GatewayIP")				
				def disks = data.vm_disk_data				
				
				//def dnsServer = data.dns_domain.DNS_IP[0]
				def dnsServer = ''
				for (def myIndex = 0; myIndex < data.dns_domain.size(); myIndex++) {
					if (myIndex > 0) {
						dnsServer = dnsServer + ",\"" + data.dns_domain.DNS_IP[myIndex] + "\""
					}
					else {
						dnsServer = "\"" + data.dns_domain.DNS_IP[myIndex] + "\""   
					}					 
				}	
				echo "dnsServer = ${dnsServer}"

				
				def dnsSuffix = data.dns_domain.DomainSuffix[0]

				echo "getting run as user --------------"				
				def runAs = data.vm_clone_data.RunAs[0]
				
				if (!data.vm_clone_data.RunAs[0].equals(null) && runAs != "") { 				
					def accountUser = getSystemAccount customerID, CloudEnvironment, CloudProvider, runAs		
			
					//def accountUser
					 username = accountUser[0]
					 password = accountUser[1]
					/*try {
						def accountUserResponse = httpRequest consoleLogResponseBody: false, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'LEAVE_OPEN', url:  "http://${ecdbIP}/v-system_account_asset?select=Username,Password&&Customer_ID=eq.${customerID}&&CloudProvider=eq.${CloudProvider}&&CloudEnvironment=eq.${CloudEnvironment}&&AccountTitle=eq.${runAs}"                                                     
																				
						def accountUser = readJSON text: accountUserResponse.content
						username =  accountUser.Username[0]
						password = accountUser.Password[0]	
					}
					catch(error) {
						def accountUserResponse = httpRequest consoleLogResponseBody: false, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'LEAVE_OPEN', url:  "http://${ecdbIP}/v-system_account_asset?select=Username,Password&&Customer_ID=eq.${customerID}&&CloudProvider=eq.${CloudProvider}&&CloudEnvironment=eq.${CloudEnvironment}&&AccountTitle=eq.${runAs}", validResponseCodes: '100:504'	
						def accountUser = readJSON text: accountUserResponse.content
						
						//write to file for error handling
						sh """(echo Failed API call in generateVsphereVmTF.groovy; echo Reason: \"${accountUser.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
						currentBuild.result = 'FAILURE'
						throw new hudson.AbortException("Failed API: ${accountUser.message}")
					}	
					*/					
				}
				else{					
					//write to file for error handling
					sh """(echo Failed in generateVsphereVmTF.groovy; echo Reason: run as user is null; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
					currentBuild.result = 'FAILURE'
					throw new hudson.AbortException("Failed: run as user is null")
				}				

				def dataSection = generateTFvsphereDataSection vsphereDatacenter, vsphereDatastore, vsphereResourcePool, vsphereVirtualMachineTemplate, vsphereNetwork				
				def resourceSection = generateTFvsphereVmResourceSection os, vmName, cpu, memory, folder, IpAddress, disks, subnetmask, gateway, dnsServer, dnsSuffix, password, windowsTimeZoneID				
				def terraformString = "${dataSection}" + "\n" + "${resourceSection}"
				
        return terraformString	
		}
}