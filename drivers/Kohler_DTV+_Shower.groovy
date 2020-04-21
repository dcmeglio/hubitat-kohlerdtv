/**
 *  Kohler DTV+ Shower
 *
 *  Copyright 2019-2020 Dominick Meglio
 *
 *	If you find this useful, donations are always appreciated 
 *	https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=7LBRPJRLJSDDN&source=url
 *
 */
 
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
	schedule("5 5/30 * * * ? *", updateDeviceConfig)
}

def updateDevices()
{
	sendCgiCommand("system_info", null, deviceStatus)
}

def updateDeviceConfig()
{
	sendCgiCommand("values", null, deviceConfig)
}

def deviceConfig(hubResponse)
{
    def data = parseJsonFromBase64(parseLanMessage(hubResponse).payload)
	state.valve1PortAssignments = []
	state.valve2PortAssignments = []
	data.valve1PortsAvailable = data.valve1PortsAvailable.toInteger()
	data.valve2PortsAvailable = data.valve2PortsAvailable.toInteger()
	if (data.valve1PortsAvailable >= 6) {
		state.valve1PortAssignments[data.valve1_outlet6_func.id-1] = 6
	}
	if (data.valve1PortsAvailable >= 5) {
		state.valve1PortAssignments[data.valve1_outlet5_func.id-1] = 5
	}
	if (data.valve1PortsAvailable >= 4) {
		state.valve1PortAssignments[data.valve1_outlet4_func.id-1] = 4
	}
	if (data.valve1PortsAvailable >= 3) {
		state.valve1PortAssignments[data.valve1_outlet3_func.id-1] = 3
	}
	if (data.valve1PortsAvailable >= 2) {
		state.valve1PortAssignments[data.valve1_outlet2_func.id-1] = 2
	}
	if (data.valve1PortsAvailable >= 1) {
		state.valve1PortAssignments[data.valve1_outlet1_func.id-1] = 1
	}
	if (data.valve2PortsAvailable >= 6) {
		state.valve2PortAssignments[data.valve2_outlet6_func.id-1] = 6
	}
	if (data.valve2PortsAvailable >= 5) {
		state.valve2PortAssignments[data.valve2_outlet5_func.id-1] = 5
	}
	if (data.valve2PortsAvailable >= 4) {
		state.valve2PortAssignments[data.valve2_outlet4_func.id-1] = 4
	}
	if (data.valve2PortsAvailable >= 3) {
		state.valve2PortAssignments[data.valve2_outlet3_func.id-1] = 3
	}
	if (data.valve2PortsAvailable >= 2) {
		state.valve2PortAssignments[data.valve2_outlet2_func.id-1] = 2
	}
	if (data.valve2PortsAvailable >= 1) {
		state.valve2PortAssignments[data.valve2_outlet1_func.id-1] = 1
	}
}

void deviceStatus(hubResponse)
{
    def data = parseJsonFromBase64(parseLanMessage(hubResponse).payload)

    // Update light status
    if (data.ui_shower_on) {
        sendEvent(name: "switch", value: "on")
    }
    else {
        sendEvent(name: "switch", value: "off")
    }
    if (parent.dtvLightCount >= 3) {
        def lightDevice = getChildDevice("kohlerdtv:light_3")
        if (lightDevice.hasAttribute("level")) {
            if (data.LZ3Status != "Off") {
                def level3 = data.LZ3Status.replace("%","").toInteger()
                lightDevice.sendEvent(name: "level", value: level3)
                lightDevice.sendEvent(name: "switch", value: "on")
            }
            else {
                lightDevice.sendEvent(name: "level", value: 0)
                lightDevice.sendEvent(name: "switch", value: "off")
            }
        }
        else {
            if (data.LZ3Status == "On") {
                lightDevice.sendEvent(name: "switch", value: "on")
            }
            else {
                lightDevice.sendEvent(name: "switch", value: "off")
            }
        }
    }
    if (parent.dtvLightCount >= 2) {
        def lightDevice = getChildDevice("kohlerdtv:light_2")
        if (lightDevice.hasAttribute("level")) {
            if (data.LZ2Status != "Off") {
                def level2 = data.LZ2Status.replace("%","").toInteger()
                lightDevice.sendEvent(name: "level", value: level2)
                lightDevice.sendEvent(name: "switch", value: "on")
            }
            else {
                lightDevice.sendEvent(name: "level", value: 0)
                lightDevice.sendEvent(name: "switch", value: "off")
            }
        }
        else {
            if (data.LZ2Status == "On") {
                lightDevice.sendEvent(name: "switch", value: "on")
            }
            else {
                lightDevice.sendEvent(name: "switch", value: "off")
            }
        }
    }
    
    if (parent.dtvLightCount >= 1) {
        def lightDevice = getChildDevice("kohlerdtv:light_1")
        if (lightDevice.hasAttribute("level")) {
            if (data.LZ1Status != "Off") {
                def level1 = data.LZ1Status.replace("%","").toInteger()
                lightDevice.sendEvent(name: "level", value: level1)
                lightDevice.sendEvent(name: "switch", value: "on")
            }
            else {
                lightDevice.sendEvent(name: "level", value: 0)
                lightDevice.sendEvent(name: "switch", value: "off")
            }
        }
        else {
            if (data.LZ1Status == "On") {
                lightDevice.sendEvent(name: "switch", value: "on")
            }
            else {
                lightDevice.sendEvent(name: "switch", value: "off")
            }
        }
    }
    
    state.valve1Status = []
	state.valve2Status = []
    // Update valve status
    if (parent.dtvValve1Count >= 6) {
        if (data.valve1_Currentstatus == "On" && data.valve1outlet6) {
            getChildDevice("kohlerdtv:valve1_6").sendEvent(name: "valve", value: "open")
			state.valve1Status[5] = true
        }
        else {
            getChildDevice("kohlerdtv:valve1_6").sendEvent(name: "valve", value: "closed")
			state.valve1Status[5] = false
        }
		
    }
    if (parent.dtvValve1Count >= 5) {
        if (data.valve1_Currentstatus == "On" && data.valve1outlet5) {
            getChildDevice("kohlerdtv:valve1_5").sendEvent(name: "valve", value: "open")
			state.valve1Status[4] = true
        }   
        else {
            getChildDevice("kohlerdtv:valve1_5").sendEvent(name: "valve", value: "closed")
			state.valve1Status[4] = false
        }        
    }    
    if (parent.dtvValve1Count >= 4) {
        if (data.valve1_Currentstatus == "On" && data.valve1outlet4) {
            getChildDevice("kohlerdtv:valve1_4").sendEvent(name: "valve", value: "open")
			state.valve1Status[3] = true
        }
        else {
            getChildDevice("kohlerdtv:valve1_4").sendEvent(name: "valve", value: "closed")
			state.valve1Status[3] = false
        }        
    }  
    if (parent.dtvValve1Count >= 3) {
        if (data.valve1_Currentstatus == "On" && data.valve1outlet3) {
            getChildDevice("kohlerdtv:valve1_3").sendEvent(name: "valve", value: "open")
			state.valve1Status[2] = true
        }
        else {
            getChildDevice("kohlerdtv:valve1_3").sendEvent(name: "valve", value: "closed")
			state.valve1Status[2] = false
        }        
    } 
    if (parent.dtvValve1Count >= 2) {
        if (data.valve1_Currentstatus == "On" && data.valve1outlet2) {
            getChildDevice("kohlerdtv:valve1_2").sendEvent(name: "valve", value: "open")
			state.valve1Status[1] = true
        } 
        else {
            getChildDevice("kohlerdtv:valve1_2").sendEvent(name: "valve", value: "closed")
			state.valve1Status[1] = false
        }        
    } 
    if (parent.dtvValve1Count >= 1) {
        if (data.valve1_Currentstatus == "On" && data.valve1outlet1) {
            getChildDevice("kohlerdtv:valve1_1").sendEvent(name: "valve", value: "open")
			state.valve1Status[0] = true
        } 
        else {
            getChildDevice("kohlerdtv:valve1_1").sendEvent(name: "valve", value: "closed")
			state.valve1Status[0] = false
        }      
		def valve1Device = getChildDevice("kohlerdtv:valve1")
		valve1Device.sendEvent(name: "thermostatSetpoint", value: data.valve1Setpoint)
		valve1Device.sendEvent(name: "heatingSetpoint", value: data.valve1Setpoint)
		if (data.valve1Temp != null)
			valve1Device.sendEvent(name: "temperature", value: data.valve1Temp)
    } 
    
    if (parent.dtvValve2Count >= 6) {
        if (data.valve2_Currentstatus == "On" && data.valve2outlet6) {
            getChildDevice("kohlerdtv:valve2_6").sendEvent(name: "valve", value: "open")
			state.valve2Status[5] = true
        } 
        else {
            getChildDevice("kohlerdtv:valve2_6").sendEvent(name: "valve", value: "closed")
			state.valve2Status[5] = false
        }        
    }
    if (parent.dtvValve2Count >= 5) {
        if (data.valve2_Currentstatus == "On" && data.valve2outlet5) {
            getChildDevice("kohlerdtv:valve2_5").sendEvent(name: "valve", value: "open")
			state.valve2Status[4] = true
        }      
        else {
            getChildDevice("kohlerdtv:valve2_5").sendEvent(name: "valve", value: "closed")
			state.valve2Status[4] = false
        }
    }    
    if (parent.dtvValve2Count >= 4) {
        if (data.valve2_Currentstatus == "On" && data.valve2outlet4) {
            getChildDevice("kohlerdtv:valve2_4").sendEvent(name: "valve", value: "open")
			state.valve2Status[3] = true
        } 
        else {
            getChildDevice("kohlerdtv:valve2_4").sendEvent(name: "valve", value: "closed")
			state.valve2Status[3] = false
        }        
    }  
    if (parent.dtvValve2Count >= 3) {
        if (data.valve2_Currentstatus == "On" && data.valve2outlet3) {
            getChildDevice("kohlerdtv:valve2_3").sendEvent(name: "valve", value: "open")
			state.valve2Status[2] = true
        }          
        else {
            getChildDevice("kohlerdtv:valve2_3").sendEvent(name: "valve", value: "closed")
			state.valve2Status[2] = false
        }
    } 
    if (parent.dtvValve2Count >= 2) {
        if (data.valve2_Currentstatus == "On" && data.valve2outlet2) {
            getChildDevice("kohlerdtv:valve2_2").sendEvent(name: "valve", value: "open")
			state.valve2Status[1] = true
        }  
        else {
            getChildDevice("kohlerdtv:valve2_2").sendEvent(name: "valve", value: "closed")
			state.valve2Status[1] = false
        }        
    } 
    if (parent.dtvValve2Count >= 1) {
        if (data.valve2_Currentstatus == "On" && data.valve2outlet1) {
            getChildDevice("kohlerdtv:valve2_1").sendEvent(name: "valve", value: "open")
			state.valve2Status[0] = true
        } 
        else {
            getChildDevice("kohlerdtv:valve2_1").sendEvent(name: "valve", value: "closed")
			state.valve2Status[0] = false
        }
		def valve2Device = getChildDevice("kohlerdtv:valve2")
		valve2Device.sendEvent(name: "thermostatSetpoint", value: data.valve2Setpoint)
		valve2Device.sendEvent(name: "heatingSetpoint", value: data.valve2Setpoint)
		if (data.valve2Temp != null)
			valve2Device.sendEvent(name: "temperature", value: data.valve2Temp)
    }
	
	def amplifier = getChildDevice("kohlerdtv:amplifier")
	if (amplifier) {
		def volume = data.volStatus.replace("%","").toInteger()
		amplifier.sendEvent(name: "volume", value: volume)
		if (volume > 0)
			amplifier.sendEvent(name: "mute", value: "unmuted")
		else
			amplifier.sendEvent(name: "mute", value: "muted")
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

def handleOpen(device, id) {
	def valve1Outputs = ""
	def valve2Outputs = ""
	def outputNumber = id.split("_")[1].toInteger()
	if (id.startsWith("valve1_"))
	{
		valve1Outputs = buildValveString(state.valve1Status, state.valve1PortAssignments, true, state.valve1PortAssignments[outputNumber-1])
		state.valve1Status[outputNumber] = true
		valve2Outputs = buildValveString(state.valve2Status, state.valve2PortAssignments, false, null)
	}
	else if (id.startsWith("valve2_"))
	{
		valve1Outputs = buildValveString(state.valve1Status, state.valve1PortAssignments, true, null)
		valve2Outputs = buildValveString(state.valve2Status, state.valve2PortAssignments, false, state.valve2PortAssignments[outputNumber-1])
		state.valve2Status[outputNumber] = true
	}
	def data = [
		valve_num : 1,
		valve1_outlet: valve1Outputs,
		valve1_massage: 0,
		valve1_temp: getValveSetPoint(1),
		valve2_outlet: valve2Outputs,
		valve2_massage: 0,
		valve2_temp: getValveSetPoint(2)
	]
	sendCgiCommand("quick_shower", data, null)
}

def handleOn(device, id) {
	parent.handleOn(device, id)
}

def handleOff(device, id) {
    parent.handleOff(device, id)
}

def handleSetLevel(device, id, level) {
	parent.handleSetLevel(device, id, level)
}

def handleSetVolume(device, id, volumelevel) {
	parent.handleSetVolume(device, id, volumelevel)
}

def handleSetInput(device, id, input) {
	parent.handleSetInput(device, id, input)
}

def handleClose(device, id) {
	def valve1Outputs = ""
	def valve2Outputs = ""
	def outputNumber = id.split("_")[1].toInteger()
	if (id.startsWith("valve1_"))
	{
		log.debug "turning off ${outputNumber} -> ${state.valve1PortAssignments[outputNumber-1]}"
		valve1Outputs = buildValveString(state.valve1Status, state.valve1PortAssignments, false, state.valve1PortAssignments[outputNumber-1])
		valve2Outputs = buildValveString(state.valve2Status, state.valve2PortAssignments, false, null)
		state.valve1Status[outputNumber] = false
	}
	else if (id.startsWith("valve2_"))
	{
		valve1Outputs = buildValveString(state.valve1Status, state.valve1PortAssignments, false, null)
		valve2Outputs = buildValveString(state.valve2Status, state.valve2PortAssignments, false, state.valve2PortAssignments[outputNumber-1])
		state.valve2Status[outputNumber] = false
	}
	def data = [
		valve_num : 1,
		valve1_outlet: valve1Outputs,
		valve1_massage: 0,
		valve1_temp: getValveSetPoint(1),
		valve2_outlet: valve2Outputs,
		valve2_massage: 0,
		valve2_temp: getValveSetPoint(2)
	]
	sendCgiCommand("quick_shower", data, null)
}

def buildValveString(currentValveStates, currentValveAssignments, open, newValve) {
	def result = ""
	for (def i = 0; i < currentValveStates.size(); i++) {
		if (i+1 == newValve) {
			if (open)
				result += (i+1)
		}
		else {
			if (currentValveStates[i] == true)
				result += currentValveAssignments[i]
		}
	}
	log.debug "valve string: ${result}"
	return result
}

def handleHeatingSetpoint(device, id, temperature) {
	device.sendEvent(name: "thermostatSetpoint", value: temperature)
	device.sendEvent(name: "heatingSetpoint", value: temperature)
	if (currentValue("switch") == "on")
	{
		def valve1temp = getValveSetPoint(1)
		def valve2temp = getValveSetPoint(2)
		if (id.startsWith("valve1"))
			valve1temp = temperature
		else if (id.startsWith("valve2"))
			valve2temp = temperature
		def data = [
			valve_num : 1,
			valve1_outlet: buildValveString(state.valve1Status, state.valve1PortAssignments, false, null),
			valve1_massage: 0,
			valve1_temp: valve1temp,
			valve2_outlet: buildValveString(state.valve2Status, state.valve2PortAssignments, false, null),
			valve2_massage: 0,
			valve2_temp: valve2temp
		]
		sendCgiCommand("quick_shower", data, null)
	}
}

def getValveSetPoint(valve) {
	def device = getChildDevice("kohlerdtv:valve${valve}")
	if (device == null)
		return 100
	else
		return device.currentValue("heatingSetpoint")
}
