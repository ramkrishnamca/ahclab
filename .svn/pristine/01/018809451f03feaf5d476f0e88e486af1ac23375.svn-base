#!/usr/bin/env groovy

def call(disks) {
			stage("generate vsphere disks"){
			echo "inside disk function"
				def terraformString = ''
				
				if(disks.toString() != "null"){
				def hdNumList = disks.HDNumber
                echo "hdNumList : ${hdNumList}"
               
               sortedHdNumList = hdNumList.sort()
               echo "sorted: ${sortedHdNumList}"
               largestNum = sortedHdNumList[sortedHdNumList.size()-1]
               echo "largest num :${largestNum}"
			
			
               for (def i = 0; i < largestNum; i++){
				def hdNumber = i + 1
				echo "disk numnber on TF file: ${i}"
				echo "corresponding hdNumber on TF:${hdNumber}"
				def currentDisk = disks.find { it['HDNumber'] ==  hdNumber }
                echo "current from DB: ${currentDisk}"
				
				if("${currentDisk}" =="null"){
                    echo "using default disk from vm template"
					def diskSection = """disk {
    label            = "disk${i}"
    size             = "\${data.vsphere_virtual_machine.template.disks.${i}.size}"
    eagerly_scrub    = "\${data.vsphere_virtual_machine.template.disks.${i}.eagerly_scrub}"
    thin_provisioned = "\${data.vsphere_virtual_machine.template.disks.${i}.thin_provisioned}"
    unit_number      = "${i}"
  }\n"""
								terraformString = terraformString + "${diskSection}"
                }
				else if(currentDisk.Operation == "Extend"){
				echo "extending disk ${i}"
				def diskSize = currentDisk.VolumeSize_GB
				def diskSection = """disk {
    label            = "disk${i}"
    size             = "${diskSize}"
    eagerly_scrub    = "\${data.vsphere_virtual_machine.template.disks.${i}.eagerly_scrub}"
    thin_provisioned = "\${data.vsphere_virtual_machine.template.disks.${i}.thin_provisioned}"
    unit_number      = "${i}"
  }\n"""
								 terraformString = terraformString + "${diskSection}"
								
				}
				else if(currentDisk.Operation == "NewLocal"){
				echo "adding new local ${i}"
				def diskSize = currentDisk.VolumeSize_GB
				echo "disk size is ${diskSize}"
				def diskSection = """disk {
    label            = "disk${i}"
    size             = "${diskSize}"
    eagerly_scrub    = "false"
    thin_provisioned = "true"
    unit_number      = "${i}"
  }\n"""
								 terraformString = terraformString + "${diskSection}"
								 
				}
				else{
				ehco "shouldn't be here"
				}
               }
			 }
			 else{
			echo "disk from DB was null"
			def diskSection = """disk {
    label            = "disk0"
    size             = "\${data.vsphere_virtual_machine.template.disks.0.size}"
    eagerly_scrub    = "\${data.vsphere_virtual_machine.template.disks.0.eagerly_scrub}"
    thin_provisioned = "\${data.vsphere_virtual_machine.template.disks.0.thin_provisioned}"
    unit_number      = "0"
  }\n"""
								terraformString = terraformString + "${diskSection}"
								
		}
		echo "tf: ${terraformString}"  
        return terraformString	
		}
}