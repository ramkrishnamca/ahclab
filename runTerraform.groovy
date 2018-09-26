#!/usr/bin/env groovy

def call(directoryName, vSphereServer, hostname, ipAddress, vSphereAccount) {
	def vmID
	echo "Calling Terraform: directory name is ${directoryName}"
				
    
		vSpherePass = vSphereAccount[1]
		vSphereUser = vSphereAccount[0]
		//echo "vSpherePass: ${vSpherePass} vSphereUser: ${vSphereUser}"
        stage("Terraform Init") {
			try {
				sh """set +x
				cd $JENKINS_HOME/terraform/${directoryName}
				VSPHERE_USER=${vSphereUser} VSPHERE_PASSWORD=${vSpherePass}  VSPHERE_SERVER=${vSphereServer} /usr/local/bin/terraform init"""
			}
			catch(error) {
				//write to file for error handling
				sh """(echo Hostname: ${hostname}; echo IP Address: ${ipAddress}; echo Failed terraform init; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""	
				currentBuild.result = 'FAILURE'
				throw new hudson.AbortException("Failed terraform init")				
			}
        }		
        stage("Terraform Plan") {
            try {
				sh """set +x
				cd $JENKINS_HOME/terraform/${directoryName}
				VSPHERE_USER=${vSphereUser} VSPHERE_PASSWORD=${vSpherePass}  VSPHERE_SERVER=${vSphereServer} /usr/local/bin/terraform plan """
			}
			catch(error) {
				//write to file for error handling
				sh """(echo Hostname: ${hostname}; echo IP Address: ${ipAddress}; echo Failed terraform plan; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				currentBuild.result = 'FAILURE'
				throw new hudson.AbortException("Failed terraform plan")
			}			
        }
        stage("Terraform Apply") {
			try {			
				def output = sh script: """set +x
				cd $JENKINS_HOME/terraform/${directoryName}
				VSPHERE_USER=${vSphereUser} VSPHERE_PASSWORD=${vSpherePass}  VSPHERE_SERVER=${vSphereServer} /usr/local/bin/terraform apply -auto-approve""", returnStdout: true  
				vmID = output.split("\\(ID: ")[1].split("\\)")[0]
				   
				echo "Here is the terraform Apply output: ${output}"
			}
			catch(error) {
				//write to file for error handling
				sh """(echo Hostname: ${hostname}; echo IP Address: ${ipAddress}; echo Failed terraform apply; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				currentBuild.result = 'FAILURE'
				throw new hudson.AbortException("Failed terraform apply")
			}	
        } 
	
    
	echo "Here is the vm id: ${vmID}"
	sh "mv $JENKINS_HOME/terraform/${directoryName}/terraform.tfstate ${nfsPath}/terraform/statefiles/${directoryName}.tfstate"
        sh "mv $JENKINS_HOME/terraform/${directoryName}/cloneVm.tf ${nfsPath}/terraform/tfapplyfiles/${directoryName}.tf"
	sh "find $JENKINS_HOME/terraform/${directoryName} -delete"
        
	return vmID
	

}