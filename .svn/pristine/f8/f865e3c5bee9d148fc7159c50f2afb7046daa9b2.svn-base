/*Jenkins String Parameters:
BuildID
CustomerID
*/

@Library('pipeline-library-test')_

pipeline {
    agent any 
    stages {
        stage('Get Component Groups') {
            steps { 
                script { //get component group build order
                timestamps {
                    withCredentials([string(credentialsId: 'f0696995-a8aa-4463-bcf6-e0895a0e6023', variable: 'jsonWebToken')]) {
                        insertOrchestrationBuildIdXref()
                        reserveIPAM BuildID
                        compGroupBuildOrders = getCompGrpBO BuildID
                        getCompGrpDetails BuildID, compGroupBuildOrders
                        }
                    }
                }
            }
        }
    }
    post {
		always {
			script {
			    withCredentials([string(credentialsId: 'f0696995-a8aa-4463-bcf6-e0895a0e6023', variable: 'jsonWebToken')]) {
			        
			        def ifErrorMessage = assetTrackingPostActions()
			        
			        if (ifErrorMessage != null) {
			            removeChefServerNodeAttributes()
			            errorHandling()
			            currentBuild.result = 'FAILURE'
		                throw new hudson.AbortException("Asset Tracking Post Actions Failed: ${ifErrorMessage}")
			        }
			        removeChefServerNodeAttributes()
			        errorHandling()
			    }
			}
		}
	}  
}