#!/usr/bin/env groovy

def call(vshpereDatacenter, vsphereDatastore, vsphereResourcePool, vsphereVirtualMachineTemplate, vsphereNetwork) {
				def dataSection = """provider "vsphere" {
				  # if you have a self-signed cert
				  allow_unverified_ssl = true
				}

				data "vsphere_datacenter" "dc" {
				  name = "${vshpereDatacenter}"
				}

				data "vsphere_datastore" "datastore" {
				  name          = "${vsphereDatastore}"
				  datacenter_id = "\${data.vsphere_datacenter.dc.id}"
				}

				data "vsphere_resource_pool" "pool" {
				  name          = "${vsphereResourcePool}"
				  datacenter_id = "\${data.vsphere_datacenter.dc.id}"
				}

				data "vsphere_virtual_machine" "template" {
				  name          = "${vsphereVirtualMachineTemplate}"
				  datacenter_id = "\${data.vsphere_datacenter.dc.id}"
				}\n"""
				def terraformString = "${dataSection}"

				for (def i = 0; i < vsphereNetwork.size(); i++){
									def networkData = """data "vsphere_network" "network${i}" {
				  name          = "${vsphereNetwork[i]}"
				  datacenter_id = "\${data.vsphere_datacenter.dc.id}"}\n"""
								 terraformString = terraformString + "${networkData}"
							}
        return terraformString	
}