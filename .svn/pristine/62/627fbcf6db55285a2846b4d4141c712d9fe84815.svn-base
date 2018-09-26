#!/usr/bin/env groovy

def call(buildId, compGroupBuildOrders) {
	echo "INSIDE OF getCompGrpDetails FUNCTION"
	
	def buildWithoutCG = false	
	
	try {
		def checkComponentVmBuild = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_vm?select=VM_Build_ID&&Build_ID=eq.${buildId}&&Component_Group_ID=is.null" 	
		checkComponentVmBuildResponse = readJSON text: checkComponentVmBuild.content
	}
	catch (error) {
		def checkComponentVmBuild = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_vm?select=VM_Build_ID&&Build_ID=eq.${buildId}&&Component_Group_ID=is.null" 	
		checkComponentVmBuildResponse = readJSON text: checkComponentVmBuild.content
		
		//write to file for error handling
		sh """(echo Failed API call in getCompGrpDetails.groovy; echo Reason: \"${checkComponentVmBuildResponse.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
		currentBuild.result = 'FAILURE'
		throw new hudson.AbortException("Failed API: ${checkComponentVmBuildResponse.message}")
	}
		
	if (!checkComponentVmBuildResponse[0].equals(null)) {
		buildWithoutCG = true
	}
	
	if (!buildWithoutCG) {
			echo "build with CGs"
		for (def compGroupbuildOrderIndex = 0; compGroupbuildOrderIndex < compGroupBuildOrders.size(); compGroupbuildOrderIndex++) {
			
			try {
				def response2 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_group?select=BuildOrder,Component_Group_Build_ID,ComponentGroupName,Component_Group_ID&&Build_ID=eq.${buildId}&&Built=eq.false&&BuildOrder=eq.${compGroupBuildOrders[compGroupbuildOrderIndex].buildorder}&&order=BuildOrder.asc"
							
				println('Response2 from ECDB: '+response2.content)
							
				compGroupDetails = readJSON text: response2.content
						  
				componentGroupBuildIDFormat = compGroupDetails.Component_Group_Build_ID.join(",")
				//componentGroupBuildIDFormat = compGroupDetails.Component_Group_Build_ID
			}
			catch (error) {
				def response2 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_group?select=BuildOrder,Component_Group_Build_ID,ComponentGroupName,Component_Group_ID&&Build_ID=eq.${buildId}&&Built=eq.false&&BuildOrder=eq.${compGroupBuildOrders[compGroupbuildOrderIndex].buildorder}&&order=BuildOrder.asc", validResponseCodes: '100:504'
				compGroupDetails = readJSON text: response2.content
				
				//write to file for error handling
				sh """(echo Failed API call in getCompGrpDetails.groovy; echo Reason: \"${compGroupDetails.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				currentBuild.result = 'FAILURE'
				throw new hudson.AbortException("Failed API: ${compGroupDetails.message}")
			}
				
			
			def componentGroup = [:]                     
							
			for (def componentGroupIndex = 0; componentGroupIndex < compGroupDetails.size(); componentGroupIndex++) {
				
				componentGroup["componentGroup ${compGroupDetails.ComponentGroupName}"] = {
									
					stage("Build CG Build Order ${compGroupBuildOrders[compGroupbuildOrderIndex].buildorder} ${compGroupDetails.ComponentGroupName}") {
										
						stage("Get Component Group ${compGroupDetails.ComponentGroupName} Build Orders") {                                     
							componentBuildOrders = getUniqueCompBO BuildID, compGroupDetails[0].Component_Group_Build_ID                                        
							getCompDetails BuildID, componentBuildOrders, componentGroupBuildIDFormat
							updateCompGrpBuild compGroupDetails[0].Component_Group_Build_ID           
						}
								  
					}
								
				}
							
			}
							
			parallel componentGroup                  
		}		
	}
	else if (buildWithoutCG) {
		echo "build with and without CGs"
		
		for (def compGroupbuildOrderIndex = 0; compGroupbuildOrderIndex < compGroupBuildOrders.size(); compGroupbuildOrderIndex++) {
			
			try {
				def response2 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_group?select=BuildOrder,Component_Group_Build_ID,ComponentGroupName,Component_Group_ID&&Build_ID=eq.${buildId}&&Built=eq.false&&BuildOrder=eq.${compGroupBuildOrders[compGroupbuildOrderIndex].buildorder}&&order=BuildOrder.asc"
							
				println('Response2 from ECDB: '+response2.content)
							
				compGroupDetails = readJSON text: response2.content
						  
				componentGroupBuildIDFormat = compGroupDetails.Component_Group_Build_ID.join(",")
				//componentGroupBuildIDFormat = compGroupDetails.Component_Group_Build_ID
			}
			catch (error) {
				def response2 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_group?select=BuildOrder,Component_Group_Build_ID,ComponentGroupName,Component_Group_ID&&Build_ID=eq.${buildId}&&Built=eq.false&&BuildOrder=eq.${compGroupBuildOrders[compGroupbuildOrderIndex].buildorder}&&order=BuildOrder.asc", validResponseCodes: '100:504'
				compGroupDetails = readJSON text: response2.content
				
				//write to file for error handling
				sh """(echo Failed API call in getCompGrpDetails.groovy; echo Reason: \"${compGroupDetails.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				currentBuild.result = 'FAILURE'
				throw new hudson.AbortException("Failed API: ${compGroupDetails.message}")
			}
			
			parallel(
				cg: {			
					def componentGroup = [:]                     
									
					for (def componentGroupIndex = 0; componentGroupIndex < compGroupDetails.size(); componentGroupIndex++) {
						
						componentGroup["componentGroup ${compGroupDetails.ComponentGroupName}"] = {
											
							stage("Build CG Build Order ${compGroupBuildOrders[compGroupbuildOrderIndex].buildorder} ${compGroupDetails.ComponentGroupName}") {
												
								stage("Get Component Group ${compGroupDetails.ComponentGroupName} Build Orders") {  
									componentBuildOrders = getUniqueCompBO BuildID, compGroupDetails[0].Component_Group_Build_ID                                        
									getCompDetails BuildID, componentBuildOrders, componentGroupBuildIDFormat
									updateCompGrpBuild compGroupDetails[0].Component_Group_Build_ID           
								}							
							}							
						}					
					}						
					parallel componentGroup
				},
				nocg: {			
					
					compBuildId = "noCG"
			
					def noComponentGroup = [:]
					
					for (def noComponentGroupIndex = 0; noComponentGroupIndex < checkComponentVmBuildResponse.size(); noComponentGroupIndex++) {
						
						def myIndex = noComponentGroupIndex
						
						noComponentGroup["VM_Build_ID ${checkComponentVmBuildResponse.VM_Build_ID[myIndex]}"] = {				

							stage("Build VM_Build_ID: ${checkComponentVmBuildResponse.VM_Build_ID[myIndex]}") {                                      				
								
								def vmBuildID = checkComponentVmBuildResponse.VM_Build_ID[myIndex]	
								//echo "this vmBuildID = ${vmBuildID}"
							
								try {
									def response6 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"vm_build_id\":${vmBuildID}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/vmbuilddata"
									println('Response6 from ECDB: '+response6.content)
																				
									vmDetails = readJSON text: response6.content
									echo "VM Details = ${vmDetails}"		
								}
								catch(error) {
									def response6 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"vm_build_id\":${vmBuildID}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/vmbuilddata", validResponseCodes: '100:504'
									vmDetails = readJSON text: response6.content
									
									//write to file for error handling
									sh """(echo Failed API call in getCompGrpDetails.groovy; echo Reason: \"${vmDetails.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
									currentBuild.result = 'FAILURE'
									throw new hudson.AbortException("Failed API: ${vmDetails.message}")
								}
								if (vmDetails.built.join(",") == "false") {
									def vmuuid = provisionVmVsphere customerID, vmBuildID, compBuildId, vmDetails
									updateVmBuilt vmBuildID
									assetTrackingUpdateVmBuild vmuuid
								}
							}
						}
					}
					
					parallel noComponentGroup
					
				}
			)
		}		
		
		if (compGroupBuildOrders.size() < 1) {
			echo "build only VMs"
			compBuildId = "noCG"
			
			def noComponentGroup = [:]
			
			for (def noComponentGroupIndex = 0; noComponentGroupIndex < checkComponentVmBuildResponse.size(); noComponentGroupIndex++) {
				
				def myIndex = noComponentGroupIndex
				
				noComponentGroup["VM_Build_ID ${checkComponentVmBuildResponse.VM_Build_ID[myIndex]}"] = {				

					stage("Build VM_Build_ID: ${checkComponentVmBuildResponse.VM_Build_ID[myIndex]}") {                                      				
						
						def vmBuildID = checkComponentVmBuildResponse.VM_Build_ID[myIndex]	
						//echo "this vmBuildID = ${vmBuildID}"
					
						try {
							def response6 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"vm_build_id\":${vmBuildID}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/vmbuilddata"
							println('Response6 from ECDB: '+response6.content)
																		
							vmDetails = readJSON text: response6.content
							echo "VM Details = ${vmDetails}"		
						}
						catch(error) {
							def response6 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"vm_build_id\":${vmBuildID}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/vmbuilddata", validResponseCodes: '100:504'
							vmDetails = readJSON text: response6.content
							
							//write to file for error handling
							sh """(echo Failed API call in getCompGrpDetails.groovy; echo Reason: \"${vmDetails.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
							currentBuild.result = 'FAILURE'
							throw new hudson.AbortException("Failed API: ${vmDetails.message}")
						}
						if (vmDetails.built.join(",") == "false") {
							def vmuuid = provisionVmVsphere customerID, vmBuildID, compBuildId, vmDetails
							updateVmBuilt vmBuildID
							assetTrackingUpdateVmBuild vmuuid
						}
					}
				}
			}
			
			parallel noComponentGroup	
			
		}		
	}
}