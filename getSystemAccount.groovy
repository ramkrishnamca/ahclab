#!/usr/bin/env groovy

def call(clientID, CloudEnvironment, CloudProvider, accountName){
passphrase = getPassphrase clientID
				
def accountUserResponse = httpRequest consoleLogResponseBody: false, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'LEAVE_OPEN',
 url: "http://${ecdbIP}/v-system_account_asset?select=Username,Password&&Customer_ID=eq.${clientID}&&CloudProvider=eq.${CloudProvider}&&CloudEnvironment=eq.${CloudEnvironment}&&AccountTitle=eq.${accountName}"                                                     
	       			                                                        
				accountUser = readJSON text: accountUserResponse.content
				//echo "${accountUser}"
				
				accountUsername =  accountUser.Username[0]
				accountPassword = Decryption accountUser.Password[0], passphrase
				
				//echo "usr/pw ${accountUsername} ${accountPassword}"
				
				account = ["${accountUsername}","${accountPassword}"]
				
				return account
				

}