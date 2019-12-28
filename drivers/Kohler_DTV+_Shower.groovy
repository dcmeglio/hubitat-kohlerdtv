metadata {
    definition (name: "Kohler DTV+ Shower", namespace: "kohlerdtv", author: "dmeglio@gmail.com") {
		capability "Switch"
		
		command "startPreset", ["number"]
    }
}

def on() {
	def data = [
		valve_num : 1,
		valve1_outlet: 1,
		valve1_massage: 0,
		valve1_temp: 100,
		valve2_outlet: 1,
		valve2_massage: 0,
		valve2_temp: 100
	]
	sendCgiCommand("quick_shower", data, null)
}

def off() {
	sendCgiCommand("stop_shower", null, null)
}

def startPreset(preset) {
	def data = [
		user: preset
	]
	sendCgiCommand("start_user", data, null)
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
	sendCgiCommand("system_info", null, deviceStatus)
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
		def valve1Device = getChildDevice("kohlerdtv:valve1")
		valve1Device.sendEvent(name: "thermostatSetpoint", value: data.valve1Setpoint)
		if (data.valve1Temp != null)
			valve1Device.sendEvent(name: "temperature", value: data.valve1Temp)
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
		def valve2Device = getChildDevice("kohlerdtv:valve2")
		valve2Device.sendEvent(name: "thermostatSetpoint", value: data.valve2Setpoint)
		if (data.valve2Temp != null)
			valve2Device.sendEvent(name: "temperature", value: data.valve2Temp)
    }
}

def sendCgiCommand(def name = "", def data = null, def handler = null)
{
	if (handler == null)
		handler = dummyHandler
	if (data != null) {
		def queryString = ""
		def keys = data.keySet()
		for (def i = 0; i < keys.size(); i++) {
			def queryKey = keys[i]
			def queryValue = data[keys[i]]
			queryString += "${queryKey}=${queryValue}&"
		}
		queryString = queryString.substring(0, queryString.size()-1)
		sendHubCommand(new hubitat.device.HubAction("GET /${name}.cgi?${queryString} HTTP/1.1\r\n\r\n", hubitat.device.Protocol.RAW_LAN, [destinationAddress: "${parent.dtvIP}:80", callback: handler]))
	}
	else
		sendHubCommand(new hubitat.device.HubAction("GET /${name}.cgi HTTP/1.1\r\n\r\n", hubitat.device.Protocol.RAW_LAN, [destinationAddress: "${parent.dtvIP}:80", callback: handler]))
}

def dummyHandler(hubResponse) {
}