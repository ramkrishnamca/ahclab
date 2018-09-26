/*Jenkins String Parameters:
DecommissionType (Valid values: VM, FingerPrint, or Environment)
DecommissionValue
CustomerID
*/

@Library('pipeline-library-test')_

pipeline {
    agent any 
    stages {
        stage("Switch on Decommission Type") {
            steps { 
                script { 
                    withCredentials([string(credentialsId: 'f0696995-a8aa-4463-bcf6-e0895a0e6023', variable: 'jsonWebToken')]) {	
						switch ( DecommissionType ) {
							case "VM":
								decommissionVM()
								break
							case "FingerPrint":
								decommissionFingerPrint()
								break
							case "Environment":
								decommissionEnvironment()	
								break
						default:
							echo "${DecommissionType} is not a valid DecommissionType"
							currentBuild.result = 'FAILURE'
							throw new hudson.AbortException("${DecommissionType} is not a valid DecommissionType")
						}
					}
				}
			}
		}
	}
}