#!/usr/bin/env groovy

def call(clientID){

//Generate Random Passphrase
integer = 12

def passphraseResponse = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST',requestBody: "{\"characters\":${integer}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/string_generator"
                                                     
				randomPassphrase = passphraseResponse.content
				echo "randomPassphrase is: ${randomPassphrase}"

//Encrypt Passphrase using Jenkins Credentials			
withCredentials([usernamePassword(credentialsId: 'ddicryptpassphrase', passwordVariable: 'jenkinscredidpw', usernameVariable: 'jenkinscredUser')]) {				

algorithm = "bf"		
echo "{\"passphrase\":${randomPassphrase}, \"key\":\"${jenkinscredidpw}\", \"cipher\":\"${algorithm}\"}"
def encryptedResponse = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"passphrase\":${randomPassphrase}, \"key\":\"${jenkinscredidpw}\", \"cipher\":\"${algorithm}\"}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/encrypt_passphrase"
                passphrase = encryptedResponse.content	
				echo "here's the encrpted passphrase respone form the db ${passphrase}"
			
	
	}
//Check to see if Customer_ID exists in customer_id_asset
def clientIdResponse = httpRequest consoleLogResponseBody: false, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'LEAVE_OPEN',
 url: "http://${ecdbIP}/v-customer_id_asset?Customer_ID=eq.${clientID}"                                                     
				def clientIdFromDB = readJSON text: clientIdResponse.content
				
if(clientIdFromDB[0] == "null" || clientIdFromDB[0] == ""){				
//Insert encrypted Passphrase into customer_id_asset table
def insertEncryptedPassphraseResponse = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"Customer_ID\":${CustomerID},\"Passphrase\":\"${passphrase}\"}", responseHandle: 'NONE', url: "http://${ecdbIP}/customer_id_asset"	
	}
else{
//Update Passphrase in customer_id_asset
 httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'PATCH', requestBody: "{\"Passphrase\":${passphrase}}", responseHandle: 'NONE', url: "http://${ecdbIP}/customer_id_asset?Customer_ID=eq.${clientID}"	
	}

}