#!/usr/bin/env groovy

def call(clientID){

//call to DB to get Encrypted Passphrase
def passphraseResponse = httpRequest consoleLogResponseBody: false, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'LEAVE_OPEN',
 url: "http://${ecdbIP}/v-customer_id_asset?select=Passphrase&&Customer_ID=eq.${clientID}"                                                     
				
				passphraseJson = readJSON text: passphraseResponse.content
				//echo "just the content output ${passphraseResponse.content}"
				encryptedPassphrase = "\\" + "${passphraseJson.Passphrase[0]}"
				//echo "encryptedPassphrase is: ${encryptedPassphrase}"

//get Decrypted Passphrase from DB				
withCredentials([usernamePassword(credentialsId: 'ddicryptpassphrase', passwordVariable: 'jenkinscredidpw', usernameVariable: 'jenkinscredUser')]) {				

algorithm = "bf"	
//echo "{\"encryptedpw\":\"${encryptedPassphrase}\", \"jenkinscredidpw\":\"${jenkinscredidpw}\", \"algorithm\":\"${algorithm}\"}"
def decryptResponse = httpRequest consoleLogResponseBody: false, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"encryptedpw\":\"${encryptedPassphrase}\", \"jenkinscredidpw\":\"${jenkinscredidpw}\", \"algorithm\":\"${algorithm}\"}", responseHandle: 'LEAVE_OPEN', url: "http://${ecdbIP}/rpc/decrypt_passphrase"
                passphrase = decryptResponse.content	
				//echo "here's the decrpted passphrase respone form the db ${passphrase}"
				
				
				return passphrase
	}
}