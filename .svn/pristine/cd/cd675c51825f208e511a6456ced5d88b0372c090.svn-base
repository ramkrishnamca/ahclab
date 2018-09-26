#!/usr/bin/env groovy

def call() {

	echo "INSIDE OF removeChefServerNodeAttributes FUNCTION"
	
	try {
		def response7 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/vm_build?select=VM_Build_ID&Build_ID=eq.${BuildID}"
		println('Response7 from ECDB: '+response7.content)
		buildIdList = readJSON text: response7.content
	}
	catch(error) {
		def response7 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/vm_build?select=VM_Build_ID&Build_ID=eq.${BuildID}", validResponseCodes: '100:504'
		buildIdList = readJSON text: response7.content
		
		//write to file for error handling
		sh """(echo Failed API call in removeChefServerNodeAttributes.groovy; echo Reason: \"${buildIdList.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
		currentBuild.result = 'FAILURE'
		throw new hudson.AbortException("Failed API: ${buildIdList.message}")
	}	
	
	def getParameterList = [:]
	
	def encryptedCustomAttr = ''
	for (def count = 0; count < buildIdList.size(); count++) {
		def getParameterListIndex = count
		getParameterList["VM Parameter List ${count}"] = {		
			vmBuildID = buildIdList.VM_Build_ID[getParameterListIndex]
			
			try {
				def customAttrJson = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-install_parameter_build_encrypted?select=ParameterName,Hostname&&VM_Build_ID=eq.${vmBuildID}"
				encryptedCustomAttr = readJSON text: customAttrJson.content
			}
			catch(error) {
				def customAttrJson = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-install_parameter_build_encrypted?select=ParameterName,Hostname&&VM_Build_ID=eq.${vmBuildID}", validResponseCodes: '100:504'
				encryptedCustomAttr = readJSON text: customAttrJson.content
				
				//write to file for error handling
				sh """(echo Failed API call in removeChefServerNodeAttributes.groovy; echo Reason: \"${encryptedCustomAttr.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				currentBuild.result = 'FAILURE'
				throw new hudson.AbortException("Failed API: ${encryptedCustomAttr.message}")
			}
			def myCommand = ''
			for (def count2 = 0; count2 < encryptedCustomAttr.size(); count2++) {
				myCommand = myCommand + "n.normal_attrs.delete('${encryptedCustomAttr.ParameterName[count2]}');"
			}
			
			sh """cd /etc/chef/chef-repo
			knife exec -E \"nodes.transform('name:${encryptedCustomAttr.Hostname[getParameterListIndex]}') { |n| ${myCommand} n.save() }\""""			
		}
	}	
	parallel getParameterList	
}