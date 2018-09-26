def call(vmBuildId, compBuildId) {
		stage("Get Bootstrap Recipes for vmBuildId: ${vmBuildId}, compBuildId: ${compBuildId}"){
			def componentVmBuildId
			try {
				def componentVmBuildIdJson = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_vm_build?select=Component_VM_Build_ID&&VM_Build_ID=eq.${vmBuildId}&&Component_Build_ID=eq.${compBuildId}"
				componentVmBuildId = readJSON text: componentVmBuildIdJson.content
				componentVmBuildId  = componentVmBuildId.Component_VM_Build_ID[0]
			}
			catch(error) {
				def componentVmBuildIdJson = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_vm_build?select=Component_VM_Build_ID&&VM_Build_ID=eq.${vmBuildId}&&Component_Build_ID=eq.${compBuildId}", validResponseCodes: '100:504'
				componentVmBuildId = readJSON text: componentVmBuildIdJson.content
				
				//write to file for error handling
				sh """(echo Failed API call in getBootstrapRecipes.groovy; echo Reason: \"${componentVmBuildId.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				currentBuild.result = 'FAILURE'
				throw new hudson.AbortException("Failed API: ${componentVmBuildId.message}")
			}
			def bootstrapItem
			try {
				def bootstrapItemResponse = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], 	responseHandle: 'NONE', url: "http://${ecdbIP}/v-install_item_build?select=ObjectID&&ItemType=eq.Bootstrap&&Component_VM_Build_ID=eq.${componentVmBuildId}"
				println('bootstrapItemResponse from ECDB: '+bootstrapItemResponse.content)                                                        
				bootstrapItem = readJSON text: bootstrapItemResponse.content
			}
			catch(error) {
				def bootstrapItemResponse = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], 	responseHandle: 'NONE', url: "http://${ecdbIP}/v-install_item_build?select=ObjectID&&ItemType=eq.Bootstrap&&Component_VM_Build_ID=eq.${componentVmBuildId}", validResponseCodes: '100:504'				
				bootstrapItem = readJSON text: bootstrapItemResponse.content
				
				//write to file for error handling
				sh """(echo Failed API call in getBootstrapRecipes.groovy; echo Reason: \"${bootstrapItem.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				currentBuild.result = 'FAILURE'
				throw new hudson.AbortException("Failed API: ${bootstrapItem.message}")
			}			
        	def recipesToRun = ''
        	for (def i = 0; i < bootstrapItem.size(); i++){
		        def recipe = bootstrapItem.ObjectID[i]
				if(i==0){
		        recipesToRun = recipesToRun + 'recipe[' +recipe + ']'
				}
				else{
				recipesToRun = recipesToRun + ', recipe[' +recipe + ']'
				}
		        }
		        echo "${recipesToRun}"
			return recipesToRun
		}
	}