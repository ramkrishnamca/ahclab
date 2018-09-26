#!/usr/bin/env groovy

def call(hostname, ipAddress, clientID, recipeName, os, CloudProvider, CloudEnvironment, itemType, runAs) {
    
    stage("Run Chef Recipe"){
	 // add recipe to run list
	 sh """cd /etc/chef/chef-repo
knife node run_list add ${hostname} 'recipe[${recipeName}]'"""
	try {	
      if(os.toLowerCase() == 'linux'){
	   //connect to remote linux node and run recipe
     sh """set +x 
	 sudo -u root cd /root/.ssh
sudo ssh -i /root/.ssh/${clientID}-id_rsa ${ipAddress} chef-client -j /etc/chef/variable.json"""
        }
          //run on remote windows node
      else if( os.toLowerCase() == 'windows'){
       
		
			accountUser = getSystemAccount clientID, CloudEnvironment, CloudProvider, runAs		
			
			//def accountUser
			def accountUsername = accountUser[0]
			def accountPassword = accountUser[1]
			//echo "inside runChefRecipe username: ${accountUsername} password:${accountPassword}"
			
		/*	try {
				def accountUserResponse = httpRequest consoleLogResponseBody: false, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'LEAVE_OPEN', url: "http://${ecdbIP}/v-system_account_asset?select=Username,Password&&Customer_ID=eq.${clientID}&&CloudProvider=eq.${CloudProvider}&&CloudEnvironment=eq.${CloudEnvironment}&&AccountTitle=eq.${runAs}"                                                     
	       			                                                        
				accountUser = readJSON text: accountUserResponse.content
				accountUsername =  accountUser.Username[0]
				accountPassword = accountUser.Password[0]
			}
			catch(error) {
				def accountUserResponse = httpRequest consoleLogResponseBody: false, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'LEAVE_OPEN', url: "http://${ecdbIP}/v-system_account_asset?select=Username,Password&&Customer_ID=eq.${clientID}&&CloudProvider=eq.${CloudProvider}&&CloudEnvironment=eq.${CloudEnvironment}&&AccountTitle=eq.${runAs}", validResponseCodes: '100:504'
				accountUser = readJSON text: accountUserResponse.content
				
				//write to file for error handling
				sh """(echo Failed API call in runChefRecipe.groovy; echo Reason: \"${accountUser.message}\"; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
				currentBuild.result = 'FAILURE'
				throw new hudson.AbortException("Failed API: ${accountUser.message}")
			}
		*/		
				
		echo "inside windows loop"		
        sh """set +x
		cd /etc/chef/chef-repo
knife winrm 'name:${hostname}' 'chef-client -c c:/chef/client.rb -j c:/Users/Administrator/Desktop/variable.json' --winrm-user '${accountUsername}' --winrm-password '${accountPassword}'"""
           
       }
       else{
         echo "incorrect os type"
       }
	}
	catch(err) {
			echo "failed to run chef recipe, removing from run list"
			//write to file for error handling
			sh """(echo Hostname: ${hostname}; echo IP Address: ${ipAddress}; echo Recipe Name: ${recipeName}; echo Item Type: ${itemType}; echo Failed to run Chef Recipe; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""			
			sh """cd /etc/chef/chef-repo
knife node run_list remove ${hostname} 'recipe[${recipeName}]'"""	
			error "${err}"
		}
    
		if (itemType == 'Reboot') {			
			for (def count = 1; count <= 20; count++) {
				def testPing = sh (
					script: "ping -c 10 ${ipAddress} | awk '/packets transmitted/ {print \$6}' | sed 's/\\%//g'",
					returnStdout: true
				) 
				if (testPing.toInteger() == 0) {
					break
				}
				else if (count == 20) {
					//write to file for error handling
					sh """(echo Hostname: ${hostname}; echo IP Address: ${ipAddress}; echo Recipe Name: ${recipeName}; echo Item Type: ${itemType}; echo Ping Test failed after reboot; echo) >> ${nfsPath}/chefparamfiles/FingerPrint-${BuildID}-Failed.txt"""
					
					currentBuild.result = 'FAILURE'
					throw new hudson.AbortException('Failed to ping OS')
					break
				}
				else {
					sleep(2)
				}
			}
		}
	
	
    //remove recipe from run list
     sh """cd /etc/chef/chef-repo
knife node run_list remove ${hostname} 'recipe[${recipeName}]'"""
    
	}   
    
}