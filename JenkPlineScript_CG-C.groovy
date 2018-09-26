pipeline {
    agent any 
    stages {
        stage('Get Component Groups') {
            steps { 
                script { //get component group build order
                    def response1 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"build_id\":${BuildID}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/uniquecgbo"
                    println('Response1 from ECDB: '+response1.content)
                    compGroupBuildOrders = readJSON text: response1.content
                    echo "compGroupBuildOrders"
                    println compGroupBuildOrders
                    
                    for (def compGroupbuildOrderIndex = 0; compGroupbuildOrderIndex < compGroupBuildOrders.size(); compGroupbuildOrderIndex++) {
                        def response2 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component_group?select=BuildOrder,Component_Group_Build_ID,ComponentGroupName&&Build_ID=eq.${BuildID}&&Built=eq.false&&BuildOrder=eq.${compGroupBuildOrders[compGroupbuildOrderIndex].buildorder}&&order=BuildOrder.asc"
                        println('Response2 from ECDB: '+response2.content)
                        compGroupDetails = readJSON text: response2.content
                        echo "compGroupDetails"
                        println compGroupDetails
                        componentGroupIDFormat = compGroupDetails.Component_Group_Build_ID.join(",")
                        def componentGroup = [:]
                        
                        for (def componentGroupIndex = 0; componentGroupIndex < compGroupDetails.size(); componentGroupIndex++) {
                            componentGroup["componentGroup ${compGroupDetails.ComponentGroupName}"] = {
                                stage("Build CG Build Order ${compGroupBuildOrders[compGroupbuildOrderIndex].buildorder} ${compGroupDetails.ComponentGroupName}") {
                                    stage("Get Component Group ${compGroupDetails.ComponentGroupName} Build Orders") {
                                        def response3 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], httpMode: 'POST', requestBody: "{\"build_id\":${BuildID},\"component_group_build_id\":${compGroupDetails[0].Component_Group_Build_ID}}", responseHandle: 'NONE', url: "http://${ecdbIP}/rpc/uniquecompbo"
                                        println('Response3 from ECDB: '+response3.content)
                                        componentBuildOrders = readJSON text: response3.content
                                       
                                        for (def compBuildOrderIndex = 0; compBuildOrderIndex < componentBuildOrders.size(); compBuildOrderIndex++) {
                                            def response4 = httpRequest consoleLogResponseBody: true, customHeaders: [[maskValue: true, name: 'Authorization', value: "Bearer ${jsonWebToken}"]], responseHandle: 'NONE', url: "http://${ecdbIP}/v-component?select=ComponentName&Build_ID=eq.${BuildID}&&Built=eq.false&&Component_Group_Build_ID=in.${componentGroupIDFormat}&&BuildOrder=eq.${componentBuildOrders[compBuildOrderIndex].buildorder}&&order=BuildOrder.asc"
                                            println('Response4 from ECDB: '+response4.content)
                                            componentDetails = readJSON text: response4.content
                                           
                                            def component = [:]
                                            
                                            for (def componentIndex = 0; componentIndex < componentDetails.size(); componentIndex++) { 
                                                def index = componentIndex
                                                component["component ${componentIndex}"] = {
                                                    stage("Build Component ${componentDetails.ComponentName[index]} (Build Order ${componentBuildOrders[compBuildOrderIndex].buildorder})") {
                                                        sleep(10)
                                                    }
                                                    
                                                }
                                            }
                                            parallel component
                                        }
                                    }
                                }
                            }
                        }
                        parallel componentGroup
                    }
                }
            }
        }
    }
}