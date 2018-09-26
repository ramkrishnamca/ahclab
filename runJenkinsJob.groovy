#!/usr/bin/env groovy

def call(jenkinsJobName, componentVmBuildId, vmBuildId, CloudProvider, CloudEnvironment) {

	stage("Run Jenkins Job") {
		echo "INSIDE OF runJenkinsJob FUNCTION"
		try {
			jenkinsJobNameForURL = jenkinsJobName.replace(" ", "%20")
			
			def getJenkinsParams = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-install_parameter_build?select=ParameterName,ParameterValue&&Component_VM_Build_ID=eq.${componentVmBuildId}&&ObjectID=eq.${jenkinsJobNameForURL}&&VM_Build_ID=eq.${vmBuildId}"
			getJenkinsParamsResponse = readJSON text: getJenkinsParams.content			
		}
		catch(error) {
			jenkinsJobNameForURL = jenkinsJobName.replace(" ", "%20")
			
			def getJenkinsParams = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-install_parameter_build?select=ParameterName,ParameterValue&&Component_VM_Build_ID=eq.${componentVmBuildId}&&ObjectID=eq.${jenkinsJobNameForURL}&&VM_Build_ID=eq.${vmBuildId}", validResponseCodes: '100:504'
			getJenkinsParamsResponse = readJSON text: getJenkinsParams.content
			
			//write to file for error handling
			sh """(echo Failed API call in runJenkinsJob.groovy; echo Reason: \"${getJenkinsParamsResponse.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
			currentBuild.result = 'FAILURE'
			throw new hudson.AbortException("Failed API: ${getJenkinsParamsResponse.message}")
		}
		
		//installItemString = getInstallItems vmBuildId, compBuildId, customerID		
		//echo "installItemString = ${installItemString}"	
		
		def myParams = []
			
		for (def myIndex = 0; myIndex < getJenkinsParamsResponse.size(); myIndex++) {
			myParams.add(string(name: "${getJenkinsParamsResponse.ParameterName[myIndex]}", value:"${getJenkinsParamsResponse.ParameterValue[myIndex]}"))
		} 

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
				def ipAttrJson = httpRequest consoleLogResponseBody: false, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'LEAVE_OPEN', url: "http://${ecdbIP}/v-ip_pool_allocated?select=IPAddress&&Hostname=eq.${hostname}&&NetworkID=eq.${network}&&Customer_ID=eq.${customerID}&&CloudProvider=eq.${CloudProvider}&&CloudEnvironment=eq.${CloudEnvironment}"
				def ipAttr = readJSON text: ipAttrJson.content
				ipAttr  = ipAttr.IPAddress[0]
				
				//varString = varString + "\"${parameterName}\": \"${ipAttr}\",\n"	
				myParams.add(string(name: "${parameterName}", value:"${ipAttr}"))					
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
			
		def myTest = build job: jenkinsJobName, parameters: myParams, propagate: false, wait: true                           
		def jobResult = myTest.getResult()
		echo "jobResult = ${jobResult}"	
				
		return jobResult
	}
}
