#!/usr/bin/env groovy

def call(vmBuildId, compBuildId, customerID) {
   stage("get Install Parameters"){
	//get componentVmBuildId
	echo "inside install items function"
	def componentVmBuildId
	def passphrase = "PrismApplication"
	try {
		def componentVmBuildIdJson = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_vm_build?select=Component_VM_Build_ID&&VM_Build_ID=eq.${vmBuildId}&&Component_Build_ID=eq.${compBuildId}"
		componentVmBuildId = readJSON text: componentVmBuildIdJson.content
		componentVmBuildId  = componentVmBuildId.Component_VM_Build_ID[0]
	}
	catch(error) {
		def componentVmBuildIdJson = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_vm_build?select=Component_VM_Build_ID&&VM_Build_ID=eq.${vmBuildId}&&Component_Build_ID=eq.${compBuildId}", validResponseCodes: '100:504'
		componentVmBuildId = readJSON text: componentVmBuildIdJson.content
		
		//write to file for error handling
		sh """(echo Failed API call in getInstallItems.groovy; echo Reason: \"${componentVmBuildId.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
		currentBuild.result = 'FAILURE'
		throw new hudson.AbortException("Failed API: ${componentVmBuildId.message}")
	}    	
	def varString = ''
	//get regular custom attributes
	def customAttr
	try {
		def customAttrJson = httpRequest consoleLogResponseBody: false, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'LEAVE_OPEN', url: "http://${ecdbIP}/v-install_parameter_build_ca?select=ParameterName,ParameterValue,Encrypted&&Component_VM_Build_ID=eq.${componentVmBuildId}"
		customAttr = readJSON text: customAttrJson.content
		customAttrJson.close()
	}
	catch(error) {
		def customAttrJson = httpRequest consoleLogResponseBody: false, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'LEAVE_OPEN', url: "http://${ecdbIP}/v-install_parameter_build_ca?select=ParameterName,ParameterValue,Encrypted&&Component_VM_Build_ID=eq.${componentVmBuildId}", validResponseCodes: '100:504'
		customAttr = readJSON text: customAttrJson.content
		customAttrJson.close()
		//write to file for error handling
		sh """(echo Failed API call in getInstallItems.groovy; echo Reason: \"${customAttr.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
		currentBuild.result = 'FAILURE'
		throw new hudson.AbortException("Failed API: ${customAttr.message}")
	}	
	for (def i = 0; i < customAttr.size(); i++){
		def customAttrName = customAttr.ParameterName[i]
		def customAttrValue = customAttr.ParameterValue[i]
		varString = varString +  "\"${customAttrName}\": \"${customAttrValue}\",\n"
		}
		
	//get encypted custom attributes
	def encryptedCustomAttr
	try {
		def customAttrJson = httpRequest consoleLogResponseBody: false, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'LEAVE_OPEN', url: "http://${ecdbIP}/v-install_parameter_build_encrypted?select=ParameterName,ParameterValue,Encrypted&&Component_VM_Build_ID=eq.${componentVmBuildId}"
		encryptedCustomAttr = readJSON text: customAttrJson.content
	}
	catch(error) {
		def customAttrJson = httpRequest consoleLogResponseBody: false, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'LEAVE_OPEN', url: "http://${ecdbIP}/v-install_parameter_build_encrypted?select=ParameterName,ParameterValue,Encrypted&&Component_VM_Build_ID=eq.${componentVmBuildId}", validResponseCodes: '100:504'
		encryptedCustomAttr = readJSON text: customAttrJson.content
		
		//write to file for error handling
		sh """(echo Failed API call in getInstallItems.groovy; echo Reason: \"${encryptedCustomAttr.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
		currentBuild.result = 'FAILURE'
		throw new hudson.AbortException("Failed API: ${encryptedCustomAttr.message}")
	}	
	for (def i = 0; i < encryptedCustomAttr.size(); i++){
		def customAttrName = encryptedCustomAttr.ParameterName[i]
		customAttrValue = Decryption encryptedCustomAttr.ParameterValue[i], passphrase
		varString = varString +  "\"${customAttrName}\": \"${customAttrValue}\",\n"
		}
	//end get encypted custom attributes
	
	//get Ipam custom attributes
	def IpamCustAttr
	try {
		def IpamCustAttrJson = httpRequest consoleLogResponseBody: false, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'LEAVE_OPEN', url: "http://${ecdbIP}/v-install_parameter_build_ipam_ca?select=ParameterName,ParameterValue,Encrypted&&Component_VM_Build_ID=eq.${componentVmBuildId}"
		IpamCustAttr = readJSON text: IpamCustAttrJson.content
	}
	catch(error) {
		def IpamCustAttrJson = httpRequest consoleLogResponseBody: false, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'LEAVE_OPEN', url: "http://${ecdbIP}/v-install_parameter_build_ipam_ca?select=ParameterName,ParameterValue,Encrypted&&Component_VM_Build_ID=eq.${componentVmBuildId}", validResponseCodes: '100:504'
		IpamCustAttr = readJSON text: IpamCustAttrJson.content
		
		//write to file for error handling
		sh """(echo Failed API call in getInstallItems.groovy; echo Reason: \"${IpamCustAttr.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
		currentBuild.result = 'FAILURE'
		throw new hudson.AbortException("Failed API: ${IpamCustAttr.message}")
	}		
	for (def i = 0; i < IpamCustAttr.size(); i++){	
		println('inside of ipam cust attr loop')
		def parameterName = IpamCustAttr.ParameterName[i]
		if(IpamCustAttr.Encrypted[i] == "true"){
			echo "decrypting value"
			IpamValue = Decryption IpamCustAttr.ParameterValue[i], passphrase
			hostname = IpamCustAttr.ParameterValue[i].split("\\.")[0]
			network = IpamCustAttr.ParameterValue[i].split("\\.")[1]
			}
		else{
			echo "parameter value is not encypted"
		    
			hostname = IpamCustAttr.ParameterValue[i].split("\\.")[0]
			network = IpamCustAttr.ParameterValue[i].split("\\.")[1]
			}
		println("hostname ${hostname}, network ${network}")
		
		//get IP 
		try {
			def ipAttrJson = httpRequest consoleLogResponseBody: false, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'LEAVE_OPEN', url: "http://${ecdbIP}/v-ip_pool_allocated?select=IPAddress&&Hostname=eq.${hostname}&&NetworkID=eq.${network}&&Customer_ID=eq.${customerID}"
			def ipAttr = readJSON text: ipAttrJson.content
			ipAttr  = ipAttr.IPAddress[0]
			
			varString = varString + "\"${parameterName}\": \"${ipAttr}\",\n"
		}
		catch(error) {
			def ipAttrJson = httpRequest consoleLogResponseBody: false, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'LEAVE_OPEN', url: "http://${ecdbIP}/v-ip_pool_allocated?select=IPAddress&&Hostname=eq.${hostname}&&NetworkID=eq.${network}&&Customer_ID=eq.${customerID}", validResponseCodes: '100:504'
			def ipAttr = readJSON text: ipAttrJson.content
			
			//write to file for error handling
			sh """(echo Failed API call in getInstallItems.groovy; echo Reason: \"${ipAttr.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
			currentBuild.result = 'FAILURE'
			throw new hudson.AbortException("Failed API: ${ipAttr.message}")
		}		
	}
	varString = varString.trim()	
	if (varString != '') {			
	varString =varString.substring(0, varString.length() - 1)
}
	
	//create variable file on remote server
		
	return varString
	}		
}