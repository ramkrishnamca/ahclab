#!/usr/bin/env groovy

def call(String os, vmName, password, dnsSuffix, windowsTimeZoneID) {
				if(os.toLowerCase() == 'windows'){
				     OSoptions = """windows_options {
        computer_name  = "${vmName}"
        workgroup      = "tftest"
        admin_password = "${password}"
	time_zone = "${windowsTimeZoneID}"
      }"""
	  }
				else
					OSoptions = """linux_options {
        host_name = "${vmName}"
        domain    = "${dnsSuffix}"
		time_zone = "America/New_York"
		hw_clock_utc = "false"
      }"""
	  
	  return OSoptions
	}