#!/usr/bin/env groovy

def call(String os, vmName, cpu, memory, folder, IpAddress, disks, subnetmask, gateway, dnsServer, dnsSuffix, password, windowsTimeZoneID) {
				stage("generate vSphere Vm Resource Section"){
				def OSoptions = generateTFvsphereOSCustomization os, vmName, password, dnsSuffix, windowsTimeZoneID 
				def disksSection = generateTFvsphereDisks disks
				def networkInterfaceSection = generateTFvsphereNetworkInterface IpAddress
				
				def interfaceString = ''
				for (def i = 0; i < IpAddress.size(); i++){
									networkData = """network_interface {
						ipv4_address = "${IpAddress[i]}"
						ipv4_netmask = "${subnetmask[i]}"
					  }\n"""
								 interfaceString = interfaceString + "${networkData}"
							}

				def vmResource = """resource "vsphere_virtual_machine" "vm" {
				  name             = "${vmName}"
				  folder           = "${folder}"
				  resource_pool_id = "\${data.vsphere_resource_pool.pool.id}"
				  datastore_id     = "\${data.vsphere_datastore.datastore.id}"

				  num_cpus  = ${cpu}
				  memory   = ${memory}
				  guest_id = "\${data.vsphere_virtual_machine.template.guest_id}"
				  wait_for_guest_net_timeout = -1

				  scsi_type = "\${data.vsphere_virtual_machine.template.scsi_type}"

				  ${networkInterfaceSection}

				  ${disksSection}

				  clone {
					template_uuid = "\${data.vsphere_virtual_machine.template.id}"

					customize {
					  ${OSoptions}

					  ${interfaceString }

					  ipv4_gateway = "${gateway}"
					  dns_server_list = [${dnsServer}]
					  dns_suffix_list = ["${dnsSuffix}"]
					}
				  }
				}
output "ip" {
  value = "\${vsphere_virtual_machine.vm.clone.0.customize.0.network_interface.0.ipv4_address}"
}

output "vmuuid" {
  value = "\${vsphere_virtual_machine.vm.uuid}"
}"""
				terraformString = "${vmResource}"

				
        return terraformString	
		}
}