/**
 *  Kohler DTV+ Integration
 *
 *  Copyright 2019 Dominick Meglio
 *
 */
definition(
    name: "Kohler DTV+ Integration",
    namespace: "dcm.kohlerdtv",
    author: "Dominick Meglio",
    description: "Integrate your Kohler DTV+ Shower with Hubitat",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	page(name: "prefKohlerIP", title: "Kohler DTV+ IP")
	page(name: "prefKohlerDevices", title: "Devices")
	page(name: "prefKohlerDeviceInfo", title: "Device Details")
}

def prefKohlerIP() {
	return dynamicPage(name: "prefKohlerIP", title: "Kohler DTV+", nextPage: "prefKohlerDevices", uninstall:false, install: false) {
		section("Kohler DTV+ Information"){
			input("dtvName", "text", title: "Shower Name", description: "Enter your Kohler DTV+ Shower Name", required: true)
            input("dtvIP", "text", title: "IP Address", description: "Enter your Kohler DTV+ IP Address", required: true)
            
		}
	}
}

def prefKohlerDevices() {
	return dynamicPage(name: "Devices", title: "Kohler DTV+ Devices", nextPage: "prefKohlerDeviceInfo", install: false, uninstall: false) {
		section("Device Information") {
            input("dtvValve1Count", "number", title: "How many valves does Controller 1 have?", required: true, range: "1..6", defaultValue: 1)
            input("dtvValve2Count", "number", title: "How many valves does Controller 2 have?", required: true, range: "0..6", defaultValue: 0)
            input("dtvLightCount", "number", title: "How many lights are configured?", required: true, range: "0..3", defaultValue: 0)
		}
	}
}

def prefKohlerDeviceInfo() {
	return dynamicPage(name: "prefKohlerDeviceInfo", title: "Device Information", install: true, uninstall: true) {
		section("Valves") {
            for (def i = 0; i < dtvValve1Count; i++) {
                input(name: "dtvValve1_${i+1}", type: "text", title: "Valve 1 Output ${i+1} Name", required: true)
            }
            for (def i = 0; i < dtvValve2Count; i++) {
                input(name: "dtvValve2_${i+1}", type: "text", title: "Valve 2 Output ${i+1} Name", required: true)
            }
        }
        section("Lights") {
            for (def i = 0; i < dtvLightCount; i++) {
                input(name: "dtvLight_${i+1}", type: "text", title: "Light ${i+1} Name", required: true)
                input(name: "dtvLightDimmable_${i+1}", type: "bool", title: "Light ${i+1} Dimmable?", required: true)
            }
        }
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unschedule()
	unsubscribe()
	initialize()
}

def initialize() {
	log.debug "initializing"
	/*cleanupChildDevices()*/
	createChildDevices()
	/*cleanupSettings()*/
}

def getDTVDevices() {
	state.valves = [:]
	state.lights = [:]

    sendHubCommand(new hubitat.device.HubAction("""GET /values.cgi HTTP/1.1\r\n\r\n""", hubitat.device.Protocol.RAW_LAN, [callback: test, destinationAddress: "192.168.86.52:80"]))
    
return
	
	def params = [
        uri: "http://${dtvIP}",
		path: "/values.cgi",
        contentType : "application/json",
        textParser: true
	]
	try
	{
		httpGet(params) { resp ->
			log.debug resp.status
		}
	}
	catch (e)
	{
        log.debug e
		log.debug e.getMessage()
        log.debug e.message
	}
	return true
}

def createChildDevices() {
    def showerDevice = getChildDevice("kohlerdtv:shower")
	if (!showerDevice)
	{
		showerDevice = addChildDevice("kohlerdtv", "Kohler DTV+ Shower", "kohlerdtv:shower", 1234, ["name": dtvName, isComponent: true])
	}
    for (def i = 0; i < dtvValve1Count; i++) {
        if (!showerDevice.getChildDevice("kohlerdtv:valve1_${i+1}"))
            showerDevice.addChildDevice("kohlerdtv", "Kohler DTV+ Valve", "kohlerdtv:valve1_${i+1}", ["name": this.getProperty("dtvValve1_${i+1}"), isComponent: true])
    }
    for (def i = 0; i < dtvValve2Count; i++) {
        if (!showerDevice.getChildDevice("kohlerdtv:valve2_${i+1}"))
            showerDevice.addChildDevice("kohlerdtv", "Kohler DTV+ Valve", "kohlerdtv:valve2_${i+1}", ["name": this.getProperty("dtvValve2_${i+1}"), isComponent: true])
    }
    for (def i = 0; i < dtvLightCount; i++) {
        if (!showerDevice.getChildDevice("kohlerdtv:light_${i+1}"))
        {
            if (this.getProperty("dtvLightDimmable_${i+1}"))
                showerDevice.addChildDevice("kohlerdtv", "Kohler DTV+ Dimmable Light", "kohlerdtv:light_${i+1}", ["name": this.getProperty("dtvLight_${i+1}"), isComponent: true])
            else
                showerDevice.addChildDevice("kohlerdtv", "Kohler DTV+ Light", "kohlerdtv:light_${i+1}", ["name": this.getProperty("dtvLight_${i+1}"), isComponent: true])
        }
    }
}
/*

def cleanupChildDevices()
{
	for (device in getChildDevices())
	{
		def deviceId = device.deviceNetworkId.replace("dtv:","")
		
		def deviceFound = false
		for (doorbell in doorbells)
		{
			if (doorbell == deviceId)
			{
				deviceFound = true
				break
			}
		}
		
		if (deviceFound == true)
			continue
		
		for (camera in cameras)
		{
			if (camera == deviceId)
			{
				deviceFound = true
				break
			}
		}
		if (deviceFound == true)
			continue
		
		deleteChildDevice(device.deviceNetworkId)
	}
}

def cleanupSettings()
{
	def allProperties = this.settings
	def deviceName = null

	for (property in allProperties) {
		if (property.key.startsWith("doorbellMotionTrigger")) {
			deviceName = property.key.replace("doorbellMotionTrigger","")
			if (!getChildDevice("ring:" + deviceName)) {
				app.removeSetting(property.key)
			}
		}
		else if (property.key.startsWith("doorbellButtonTrigger")) {
			deviceName = property.key.replace("doorbellButtonTrigger","")
			if (!getChildDevice("ring:" + deviceName)) {
				app.removeSetting(property.key)
			}
		}
		else if (property.key.startsWith("cameraMotionTrigger")) {
			log.debug "checking for ${property.key}"
			deviceName = property.key.replace("cameraMotionTrigger","")
			if (!getChildDevice("ring:" + deviceName)) {
				log.debug "deleting it"
				app.removeSetting(property.key)
			}
		}
	}
}

def handleOn(device, cameraId) {
	log.debug "Handling On event for ${cameraId}"

	runCommandWithRetry(cameraId, "floodlight_light_on")
	pause(250)
	runCommandWithRetry(cameraId, "floodlight_light_on")
	
	device.sendEvent(name: "switch", value: "on")
}

def handleOff(device, cameraId) {
	log.debug "Handling Off event for ${cameraId}"
	device.updateDataValue("strobing", "false")
	log.debug device.getDataValue("strobing")
	runCommandWithRetry(cameraId, "floodlight_light_off")
	runCommandWithRetry(cameraId, "siren_off")
	
	device.sendEvent(name: "switch", value: "off")
}

def handleSiren(device, cameraId) {
	log.debug "Handling Siren event for ${cameraId}"
	//runCommandWithRetry(cameraId, "siren_on", "PUT", [duration: 10])
	device.sendEvent(name: "alarm", value: "siren")
}

def handleBoth(device, cameraId) {
	//runCommandWithRetry(cameraId, "siren_on", "PUT", [duration: 10])
	device.sendEvent(name: "alarm", value: "siren")
}

def handleStrobe(device, cameraId) {
/*	def strobePauseInMs = 3000
	def strobeCount = 5
	device.updateDataValue("strobing", "true")
	log.debug "Handling Strobe event for ${cameraId}"
	device.sendEvent(name: "alarm", value: "strobe")
	
	for (def i = 0; i < strobeCount; i++) {
		log.debug device.getDataValue("strobing")
		if (device.getDataValue("strobing") == "false")
			return
		runCommandWithRetry(cameraId, "floodlight_light_on")
		if (device.getDataValue("strobing") == "false")
			return
		runCommandWithRetry(cameraId, "floodlight_light_on")
		if (device.getDataValue("strobing") == "false")
			return
		pause(strobePauseInMs)
		if (device.getDataValue("strobing") == "false")
			return
		runCommandWithRetry(cameraId, "floodlight_light_off")
		if (device.getDataValue("strobing") == "false")
			return
		pause(strobePauseInMs)
	}
	device.updateDataValue("strobing", "false")
	//runCommandWithRetry(cameraId, "siren_on", "PUT", [duration: 10])
	device.sendEvent(name: "alarm", value: "siren")
}

def handleRefresh() {
	updateDevices()
}

def handleRecord(device, cameraId) {
	runCommandWithRetry(cameraId, "vod", "POST")
}



def runCommand(deviceId, command, method = "PUT", parameters = null) {
	def params = [
		uri: "https://api.ring.com",
		path: "/clients_api/doorbots/${deviceId}/${command}",
		headers: [
			"User-Agent": "iOS"
		],
		query: [
        	api_version: "10",
            "auth_token": state.token
    	]
	]
	if (parameters != null) {
		map.each { key, value -> 
			params.query[key] = value
		}
	}
	log.debug "/clients_api/doorbots/${deviceId}/${command}"
	def result = null
	if (method == "PUT")
	{
		httpPut(params) { resp ->
			result = resp.data
		}
	}
	else if (method == "POST")
	{
		httpPost(params) { resp ->
			result = resp.data
		}
	}
	return result
}

def runCommandWithRetry(deviceId, command, method = "PUT", parameters = null) {
	try
	{
		return runCommand(deviceId, command, method, parameters)
	}
	catch (e)
	{
		if (e.statusCode == 401)
		{
			login()
			return runCommand(deviceId, command, method, parameters)
		}
		else if (e.statusCode >= 200 && e.statusCode <= 299)
			return
		else
			log.debug e
	}
}

def trigger(level) {
	if (level == 0)
		return

	def allProperties = this.settings
	def deviceName = null
	def device = null
	for (property in allProperties) {
		if (property.key.startsWith("doorbellMotionTrigger")) {
			if (this.getProperty(property.key) == level) {
				deviceName = property.key.replace("doorbellMotionTrigger","")
				device = getChildDevice("ring:" + deviceName)
				if (device == null)
					continue
				log.debug "Triggering motion for ${device}"
				device.sendEvent(name: "motion", value: "active")
				runIn(5, inactivate, [overwrite: false, data: [device: deviceName]])
				break
			}
		}
		else if (property.key.startsWith("doorbellButtonTrigger")) {
			if (this.getProperty(property.key) == level) {
				deviceName = property.key.replace("doorbellButtonTrigger","")
				device = getChildDevice("ring:" + deviceName)
				if (device == null)
					continue
				log.debug "Triggering button press for ${device}"
				device.sendEvent(name: "pushed", value: "1")
				break
			}
		}
		else if (property.key.startsWith("cameraMotionTrigger")) {
			if (this.getProperty(property.key) == level) {
				deviceName = property.key.replace("cameraMotionTrigger","")
				device = getChildDevice("ring:" + deviceName)
				if (device == null)
					continue
				log.debug "Triggering motion for ${device}"
				device.sendEvent(name: "motion", value: "active")
				runIn(5, inactivate, [overwrite: false, data: [device: deviceName]])
				break
			}
		}
	}
}

def inactivate(data) {

	def device = getChildDevice("ring:" + data.device)
	log.debug "Cancelling motion for ${device}"
	device.sendEvent(name:"motion", value: "inactive")
}*/