#!/usr/bin/env groovy

def call() {
	echo "INSIDE OF decommissionVM FUNCTION"
	
	stage("Decommission VM") {

		def vmuuidList = DecommissionValue.tokenize(",")
                        
		def vmuuids = [:]                     
		
		for (def vmuuidIndex = 0; vmuuidIndex < vmuuidList.size(); vmuuidIndex++) {
			def myIndex = vmuuidIndex
			
			vmuuids["vmuuid ${vmuuidList[vmuuidIndex]}"] = {
				
				stage("Decommission ${vmuuidList[myIndex]}") {                                     
					def getVmDetails = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/vm_build?select=VM_Build_ID,Build_ID,Hostname,CloudEnvironment,CloudProvider&VMUUID=eq.${vmuuidList[myIndex]}&&Customer_ID=eq.${CustomerID}"
					def vmDetailsResponse = readJSON text: getVmDetails.content					
					
					if (vmDetailsResponse[0].equals(null)) {
                            currentBuild.result = 'FAILURE'
							throw new hudson.AbortException("Failed no record found for VMUUID ${vmuuidList[myIndex]} and Customer_ID ${CustomerID}")
                    }				
					
					def vmBuildId
					def directoryName									
					for (def index = 0; index < vmDetailsResponse.size(); index++) {
						vmBuildId = vmDetailsResponse.VM_Build_ID[index]
						def directoryNameCheck = "vmBuildID${vmBuildId}"											
															
						def fileCheck = sh (
							script: "[ -f ${nfsPath}/terraform/statefiles/${directoryNameCheck}.tfstate ] && echo \"File exist\" || echo \"File does not exist\"",
							returnStdout: true
						)							
						
						if (fileCheck == "File exist\n") {											
							directoryName = "vmBuildID${vmBuildId}"											
						}
					}								
					
					def getIpAddress = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/ip_pool?select=IPAddress&VMUUID=eq.${vmuuidList[myIndex]}"
					def ipAddress = readJSON text: getIpAddress.content										
					
					def hostname = vmDetailsResponse.Hostname[0]
					//def customerID = vmDetailsResponse.Customer_ID[0]
					def cloudEnv = vmDetailsResponse.CloudEnvironment[0]
					def cloudProvider = vmDetailsResponse.CloudProvider[0]
					def buildId = vmDetailsResponse.Build_ID[0]
					def ip = ipAddress.IPAddress[0]										
					
					def getSystemProperty = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/system_property_asset?select=SystemPropertyValue&SystemPropertyName=eq.vsphere_server&&CloudProvider=eq.${cloudProvider}&&CloudEnvironment=eq.${cloudEnv}&&Customer_ID=eq.${CustomerID}"								
					def sysProp = readJSON text: getSystemProperty.content
					
					vSphereServer = sysProp.SystemPropertyValue[0]										
					
					accountDetails = getSystemAccount CustomerID, cloudEnv, cloudProvider, "vSphere_Admin"									
					
					vSpherePass = accountDetails[1]
					vSphereUser = accountDetails[0]																	
					
					sh "mkdir -p $JENKINS_HOME/terraform/${directoryName}"
					sh "cp ${nfsPath}/terraform/statefiles/${directoryName}.tfstate $JENKINS_HOME/terraform/${directoryName}/terraform.tfstate"
					sh "cp ${nfsPath}/terraform/tfapplyfiles/${directoryName}.tf $JENKINS_HOME/terraform/${directoryName}/cloneVm.tf"
					
					stage("Terraform Init") {
						try {
							sh """set +x
							cd $JENKINS_HOME/terraform/${directoryName}
							VSPHERE_USER=${vSphereUser} VSPHERE_PASSWORD=${vSpherePass}  VSPHERE_SERVER=${vSphereServer} /usr/local/bin/terraform init"""
						}
						catch(error) {
							//write to file for error handling
							//sh """(echo Hostname: ${hostname}; echo IP Address: ${ip}; echo Failed terraform init; echo) >> ${nfsPath}/chefparamfiles/Decommission-${vmuuidList[myIndex]}-Failed.txt"""	
							currentBuild.result = 'FAILURE'
							throw new hudson.AbortException("Failed terraform init")				
						}
					}		
					stage("Terraform Destroy") {
						try {
							sh """set +x
							cd $JENKINS_HOME/terraform/${directoryName}
							VSPHERE_USER=${vSphereUser} VSPHERE_PASSWORD=${vSpherePass}  VSPHERE_SERVER=${vSphereServer} /usr/local/bin/terraform destroy -auto-approve"""
						}
						catch(error) {
							//write to file for error handling
							//sh """(echo Hostname: ${hostname}; echo IP Address: ${ip}; echo Failed terraform destroy; echo) >> ${nfsPath}/chefparamfiles/Decommission-${vmuuidList[myIndex]}-Failed.txt"""
							currentBuild.result = 'FAILURE'
							throw new hudson.AbortException("Failed terraform destroy")
						}			
					}
					stage("Remove Terraform Files") {
						sh """sudo rm -f ${nfsPath}/terraform/statefiles/${directoryName}.tfstate
						sudo rm -f ${nfsPath}/terraform/tfapplyfiles/${directoryName}.tf
						sudo find $JENKINS_HOME/terraform/${directoryName} -delete"""											
					}	
					stage("Remove ${hostname} from Chef") {
						try {
							sh """cd /etc/chef/chef-repo
							knife node delete ${hostname} -y
							knife client delete ${hostname} -y"""
						}
						catch(error) {
							echo "WARNING:"
							echo "Chef my not be installed on host ${hostname}, unable to successfully execute knife node/client delete."
						}
					}					
				}
			}
		}
		parallel vmuuids
		
		stage("Clearing ECDB Asset Tables") {   						
			def decommissionVm = httpRequest consoleLogResponseBody: false, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"vmuuid\":\"{${DecommissionValue}}\"}", responseHandle: 'LEAVE_OPEN', url: "http://${ecdbIP}/rpc/decommission_vm"
		}
	}
}

def call(myVmuuidString) {
	echo "INSIDE OF decommissionVM FUNCTION"
	
	stage("Decommission VM") {

		def vmuuidList = myVmuuidString.tokenize(",")
                   
		def vmuuids = [:]                     
		
		for (def vmuuidIndex = 0; vmuuidIndex < vmuuidList.size(); vmuuidIndex++) {
			def myIndex = vmuuidIndex
			
			vmuuids["vmuuid ${vmuuidList[vmuuidIndex]}"] = {
				
				stage("Decommission ${vmuuidList[myIndex]}") {                                     
					def getVmDetails = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/vm_build?select=VM_Build_ID,Build_ID,Hostname,CloudEnvironment,CloudProvider&VMUUID=eq.${vmuuidList[myIndex]}"
					def vmDetailsResponse = readJSON text: getVmDetails.content
					
					if (vmDetailsResponse[0].equals(null)) {
                            currentBuild.result = 'FAILURE'
							throw new hudson.AbortException("Failed no record found for VMUUID ${vmuuidList[myIndex]} and Customer_ID ${CustomerID}")
                    }
					
					def vmBuildId
					def directoryName									
					for (def index = 0; index < vmDetailsResponse.size(); index++) {
						vmBuildId = vmDetailsResponse.VM_Build_ID[index]
						def directoryNameCheck = "vmBuildID${vmBuildId}"											
															
						def fileCheck = sh (
							script: "[ -f ${nfsPath}/terraform/statefiles/${directoryNameCheck}.tfstate ] && echo \"File exist\" || echo \"File does not exist\"",
							returnStdout: true
						)							
						
						if (fileCheck == "File exist\n") {											
							directoryName = "vmBuildID${vmBuildId}"											
						}
					}								
					
					def getIpAddress = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/ip_pool?select=IPAddress&VMUUID=eq.${vmuuidList[myIndex]}"
					def ipAddress = readJSON text: getIpAddress.content										
					
					def hostname = vmDetailsResponse.Hostname[0]
					//def customerID = vmDetailsResponse.Customer_ID[0]
					def cloudEnv = vmDetailsResponse.CloudEnvironment[0]
					def cloudProvider = vmDetailsResponse.CloudProvider[0]
					def buildId = vmDetailsResponse.Build_ID[0]
					def ip = ipAddress.IPAddress[0]										
					
					def getSystemProperty = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/system_property_asset?select=SystemPropertyValue&SystemPropertyName=eq.vsphere_server&&CloudProvider=eq.${cloudProvider}&&CloudEnvironment=eq.${cloudEnv}&&Customer_ID=eq.${CustomerID}"								
					def sysProp = readJSON text: getSystemProperty.content
					
					vSphereServer = sysProp.SystemPropertyValue[0]										
					
					accountDetails = getSystemAccount CustomerID, cloudEnv, cloudProvider, "vSphere_Admin"									
					
					vSpherePass = accountDetails[1]
					vSphereUser = accountDetails[0]																	
					
					sh "mkdir -p $JENKINS_HOME/terraform/${directoryName}"
					sh "cp ${nfsPath}/terraform/statefiles/${directoryName}.tfstate $JENKINS_HOME/terraform/${directoryName}/terraform.tfstate"
					sh "cp ${nfsPath}/terraform/tfapplyfiles/${directoryName}.tf $JENKINS_HOME/terraform/${directoryName}/cloneVm.tf"
					
					stage("Terraform Init") {
						try {
							sh """set +x
							cd $JENKINS_HOME/terraform/${directoryName}
							VSPHERE_USER=${vSphereUser} VSPHERE_PASSWORD=${vSpherePass}  VSPHERE_SERVER=${vSphereServer} /usr/local/bin/terraform init"""
						}
						catch(error) {
							//write to file for error handling
							//sh """(echo Hostname: ${hostname}; echo IP Address: ${ip}; echo Failed terraform init; echo) >> ${nfsPath}/chefparamfiles/Decommission-${vmuuidList[myIndex]}-Failed.txt"""	
							currentBuild.result = 'FAILURE'
							throw new hudson.AbortException("Failed terraform init")				
						}
					}		
					stage("Terraform Destroy") {
						try {
							sh """set +x
							cd $JENKINS_HOME/terraform/${directoryName}
							VSPHERE_USER=${vSphereUser} VSPHERE_PASSWORD=${vSpherePass}  VSPHERE_SERVER=${vSphereServer} /usr/local/bin/terraform destroy -auto-approve"""
						}
						catch(error) {
							//write to file for error handling
							//sh """(echo Hostname: ${hostname}; echo IP Address: ${ip}; echo Failed terraform destroy; echo) >> ${nfsPath}/chefparamfiles/Decommission-${vmuuidList[myIndex]}-Failed.txt"""
							currentBuild.result = 'FAILURE'
							throw new hudson.AbortException("Failed terraform destroy")
						}			
					}
					stage("Remove Terraform Files") {
						sh """sudo rm -f ${nfsPath}/terraform/statefiles/${directoryName}.tfstate
						sudo rm -f ${nfsPath}/terraform/tfapplyfiles/${directoryName}.tf
						sudo find $JENKINS_HOME/terraform/${directoryName} -delete"""											
					}	
					stage("Remove ${hostname} from Chef") {
						try {
							sh """cd /etc/chef/chef-repo
							knife node delete ${hostname} -y
							knife client delete ${hostname} -y"""
						}
						catch(error) {
							echo "WARNING:"
							echo "Chef may not be installed on host ${hostname}, unable to successfully execute knife node/client delete."
						}
					}					
				}
			}
		}
		parallel vmuuids
		
		stage("Clearing ECDB Asset Tables") {   	
			def decommissionVm = httpRequest consoleLogResponseBody: false, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"vmuuid\":\"{${myVmuuidString}}\"}", responseHandle: 'LEAVE_OPEN', url: "http://${ecdbIP}/rpc/decommission_vm"
		}
	}
}
						