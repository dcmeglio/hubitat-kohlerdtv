metadata {
    definition (name: "Kohler DTV+ Shower", namespace: "kohlerdtv", author: "dmeglio@gmail.com") {
		capability "Switch"
    }
}

def on() {
	//parent.handleOn(device, device.deviceNetworkId.split(":")[1])
}

def off() {
	//parent.handleOff(device, device.deviceNetworkId.split(":")[1])
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unschedule()
	initialize()
}

def initialize() {
	log.debug "initializing"
	schedule("0/10 * * * * ? *", updateDevices)
}

def updateDevices()
{
    sendHubCommand(new hubitat.device.HubAction("GET /system_info.cgi HTTP/1.1\r\n\r\n", hubitat.device.Protocol.RAW_LAN, [destinationAddress: "${parent.dtvIP}:80", callback: deviceStatus]))
}

void deviceStatus(hubResponse)
{

    def data = parseJson(hubResponse.split("payload:")[1])

    // Update light status
    if (data.ui_shower_on) {
        sendEvent(name: "switch", value: "on")
    }
    else {
        sendEvent(name: "switch", value: "off")
    }
    if (parent.dtvLightCount >= 3) {
        if (parent.dtvLightDimmable_3) {
            if (data.LZ3Status != "Off") {
                def level3 = data.LZ3Status.replace("%","").toInteger()
                getChildDevice("kohlerdtv:light_3").sendEvent(name: "level", value: level3)
            }
            else {
                getChildDevice("kohlerdtv:light_3").sendEvent(name: "level", value: 0)
            }
        }
        else {
            if (data.LZ3Status == "On") {
                getChildDevice("kohlerdtv:light_3").sendEvent(name: "switch", value: "on")
            }
            else {
                getChildDevice("kohlerdtv:light_3").sendEvent(name: "switch", value: "off")
            }
        }
    }
    if (parent.dtvLightCount >= 2) {
        if (parent.dtvLightDimmable_2) {
            if (data.LZ2Status != "Off") {
                def level2 = data.LZ2Status.replace("%","").toInteger()
                getChildDevice("kohlerdtv:light_2").sendEvent(name: "level", value: level2)
            }
            else {
                getChildDevice("kohlerdtv:light_2").sendEvent(name: "level", value: 0)
            }
        }
        else {
            if (data.LZ2Status == "On") {
                getChildDevice("kohlerdtv:light_2").sendEvent(name: "switch", value: "on")
            }
            else {
                getChildDevice("kohlerdtv:light_2").sendEvent(name: "switch", value: "off")
            }
        }
    }
    if (parent.dtvLightCount >= 1) {
        if (parent.dtvLightDimmable_1) {
            if (data.LZ1Status != "Off") {
                def level1 = data.LZ1Status.replace("%","").toInteger()
                getChildDevice("kohlerdtv:light_1").sendEvent(name: "level", value: level1)
            }
            else {
                getChildDevice("kohlerdtv:light_1").sendEvent(name: "level", value: 0)
            }
        }
        else {
            if (data.LZ1Status == "On") {
                getChildDevice("kohlerdtv:light_1").sendEvent(name: "switch", value: "on")
            }
            else {
                getChildDevice("kohlerdtv:light_1").sendEvent(name: "switch", value: "off")
            }
        }
    }
    
    // Update valve status
    if (parent.dtvValve1Count >= 6) {
        if (data.valve1_Currentstatus == "On" && data.valve1outlet6) {
            getChildDevice("kohlerdtv:valve1_6").sendEvent(name: "valve", value: "open")
        }
        else {
            getChildDevice("kohlerdtv:valve1_6").sendEvent(name: "valve", value: "closed")
        }
    }
    if (parent.dtvValve1Count >= 5) {
        if (data.valve1_Currentstatus == "On" && data.valve1outlet5) {
            getChildDevice("kohlerdtv:valve1_5").sendEvent(name: "valve", value: "open")
        }   
        else {
            getChildDevice("kohlerdtv:valve1_5").sendEvent(name: "valve", value: "closed")
        }        
    }    
    if (parent.dtvValve1Count >= 4) {
        if (data.valve1_Currentstatus == "On" && data.valve1outlet4) {
            getChildDevice("kohlerdtv:valve1_4").sendEvent(name: "valve", value: "open")
        }
        else {
            getChildDevice("kohlerdtv:valve1_4").sendEvent(name: "valve", value: "closed")
        }        
    }  
    if (parent.dtvValve1Count >= 3) {
        if (data.valve1_Currentstatus == "On" && data.valve1outlet3) {
            getChildDevice("kohlerdtv:valve1_3").sendEvent(name: "valve", value: "open")
        }
        else {
            getChildDevice("kohlerdtv:valve1_3").sendEvent(name: "valve", value: "closed")
        }        
    } 
    if (parent.dtvValve1Count >= 2) {
        if (data.valve1_Currentstatus == "On" && data.valve1outlet2) {
            getChildDevice("kohlerdtv:valve1_2").sendEvent(name: "valve", value: "open")
        } 
        else {
            getChildDevice("kohlerdtv:valve1_2").sendEvent(name: "valve", value: "closed")
        }        
    } 
    if (parent.dtvValve1Count >= 1) {
        if (data.valve1_Currentstatus == "On" && data.valve1outlet1) {
            getChildDevice("kohlerdtv:valve1_1").sendEvent(name: "valve", value: "open")
        } 
        else {
            getChildDevice("kohlerdtv:valve1_1").sendEvent(name: "valve", value: "closed")
        }      

		getChildDevice("kohlerdtv:valve1").sendEvent(name: "thermostatSetpoint", value: data.valve1Setpoint)
    } 
    
    if (parent.dtvValve2Count >= 6) {
        if (data.valve2_Currentstatus == "On" && data.valve2outlet6) {
            getChildDevice("kohlerdtv:valve2_6").sendEvent(name: "valve", value: "open")
        } 
        else {
            getChildDevice("kohlerdtv:valve2_6").sendEvent(name: "valve", value: "closed")
        }        
    }
    if (parent.dtvValve2Count >= 5) {
        if (data.valve2_Currentstatus == "On" && data.valve2outlet5) {
            getChildDevice("kohlerdtv:valve2_5").sendEvent(name: "valve", value: "open")
        }      
        else {
            getChildDevice("kohlerdtv:valve2_5").sendEvent(name: "valve", value: "closed")
        }
    }    
    if (parent.dtvValve2Count >= 4) {
        if (data.valve2_Currentstatus == "On" && data.valve2outlet4) {
            getChildDevice("kohlerdtv:valve2_4").sendEvent(name: "valve", value: "open")
        } 
        else {
            getChildDevice("kohlerdtv:valve2_4").sendEvent(name: "valve", value: "closed")
        }        
    }  
    if (parent.dtvValve2Count >= 3) {
        if (data.valve2_Currentstatus == "On" && data.valve2outlet3) {
            getChildDevice("kohlerdtv:valve2_3").sendEvent(name: "valve", value: "open")
        }          
        else {
            getChildDevice("kohlerdtv:valve2_3").sendEvent(name: "valve", value: "closed")
        }
    } 
    if (parent.dtvValve2Count >= 2) {
        if (data.valve2_Currentstatus == "On" && data.valve2outlet2) {
            getChildDevice("kohlerdtv:valve2_2").sendEvent(name: "valve", value: "open")
        }  
        else {
            getChildDevice("kohlerdtv:valve2_2").sendEvent(name: "valve", value: "closed")
        }        
    } 
    if (parent.dtvValve2Count >= 1) {
        if (data.valve2_Currentstatus == "On" && data.valve2outlet1) {
            getChildDevice("kohlerdtv:valve2_1").sendEvent(name: "valve", value: "open")
        } 
        else {
            getChildDevice("kohlerdtv:valve2_1").sendEvent(name: "valve", value: "closed")
        }    
		getChildDevice("kohlerdtv:valve2").sendEvent(name: "thermostatSetpoint", value: data.valve2Setpoint)
    }
}