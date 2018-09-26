#!/usr/bin/env groovy

def call(vsphereNetwork) {
			def terraformString = ''
			for (def i = 0; i < vsphereNetwork.size(); i++){	
				def networkData = """network_interface {
					network_id   = "\${data.vsphere_network.network${i}.id}"
					adapter_type = "\${data.vsphere_virtual_machine.template.network_interface_types[0]}"
				  }\n"""
				  terraformString = terraformString + "${networkData}"
				}
				
        return terraformString	
}