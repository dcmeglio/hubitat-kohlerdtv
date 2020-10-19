/**
 *  Kohler DTV+ Integration
 *
 *  Copyright 2019-2020 Dominick Meglio
 *
 *	If you find this useful, donations are always appreciated 
 *	https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=7LBRPJRLJSDDN&source=url
 *
 * Revision History
 * v 2020.03.31 - Added the ability to control lights and amplifier using a Kohler Konnect module
 */
definition(
    name: "Kohler DTV+ Integration",
    namespace: "dcm.kohlerdtv",
    author: "Dominick Meglio",
    description: "Integrate your Kohler DTV+ Shower with Hubitat",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
	documentationLink: "https://github.com/dcmeglio/hubitat-kohlerdtv/blob/master/README.md")


preferences {
	page(name: "prefKohlerIP", title: "Kohler DTV+ IP")
	page(name: "prefKohlerDevices", title: "Devices")
	page(name: "prefKohlerDeviceInfo", title: "Device Details")
	page(name: "prefKohlerKonnectDeviceInfo", title: "Device Details")
}

def prefKohlerIP() {
	return dynamicPage(name: "prefKohlerIP", title: "Kohler DTV+", nextPage: "prefKohlerDevices", uninstall:false, install: false) {
		section("Kohler DTV+ Information"){
			input("dtvName", "text", title: "Shower Name", description: "Enter your Kohler DTV+ Shower Name", required: true)
            input("dtvIP", "text", title: "IP Address", description: "Enter your Kohler DTV+ IP Address", required: true)
			input("dtvKonnect", "bool", title: "Use Kohler Konnect?", description: "Use Kohler Konnect?", submitOnChange: true)
			if (dtvKonnect == true)
			{
				input ("dtvKonnectUser", "email", title: "Konnect Username", description: "Enter your Kohler Konnect Username", required: true)
				input ("dtvKonnectPassword", "password", title: "Konnect Password", description: "Enter your Kohler Konnect Password", required: true)
			}
			input("debugOutput", "bool", title: "Enable debug logging?", defaultValue: true, displayDuringSetup: false, required: false)
            
		}
	}
}

def prefKohlerDevices() {
	if (dtvKonnect) {
		authenticateKohlerKonnect()
		getKonnectDevices()
		return dynamicPage(name: "Devices", title: "Kohler DTV+ Devices", nextPage: "prefKohlerKonnectDeviceInfo", install: false, uninstall: false) {
			section("Device Information") {
				input(name: "dtv", type: "enum", title: "Device", required:false, multiple:false, options:state.dtvDevices)
			}
		}
	}
	else
	{
		return dynamicPage(name: "Devices", title: "Kohler DTV+ Devices", nextPage: "prefKohlerDeviceInfo", install: false, uninstall: false) {
			section("Device Information") {
				input("dtvValve1Count", "number", title: "How many valves does Controller 1 have?", required: true, range: "1..6", defaultValue: 1)
				input("dtvValve2Count", "number", title: "How many valves does Controller 2 have?", required: true, range: "0..6", defaultValue: 0)
				input("dtvLightCount", "number", title: "How many lights are configured?", required: true, range: "0..3", defaultValue: 0)
			}
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

def prefKohlerKonnectDeviceInfo() {
	getKonnectDeviceDetails(dtv)
	registerKonnectDevice()
	
	return dynamicPage(name: "prefKohlerKonnectDeviceInfo", title: "Device Information", install: true, uninstall: true) {
		section("Valves") {
            for (def i = 0; i < state.konnectDtvValve1Count; i++) {
                input(name: "dtvValve1_${i+1}", type: "text", title: "Valve 1 Output ${i+1} Name", required: true)
            }
            for (def i = 0; i < state.konnectDtvValve2Count; i++) {
                input(name: "dtvValve2_${i+1}", type: "text", title: "Valve 2 Output ${i+1} Name", required: true)
            }
        }
        section("Lights") {
            for (def i = 0; i < state.konnectDtvLightCount; i++) {
                input(name: "dtvLight_${i+1}", type: "text", title: "Light ${i+1} Name", required: true)
            }
        }
	}
}

def registerKonnectDevice() {
	def deviceId = getHubUID().replaceAll("-","")
	
	def token = getAccessToken()
	def body = '{"sku":"IOS","builddata":{"version":"1.7.4","type":"ios"},"serialnumber":"'+ deviceId + '"}'
	def params = [
		uri: "https://connect.kohler.io",
		path: "/api/v1/platform/devices/identity/all/mobile",
		contentType: "application/json",
		requestContentType: "application/json",
		headers: [
			"Authorization": "Bearer ${token}"
		],
		body: body
	]

	httpPost(params) { resp ->
		def iotprofile = resp.data.iotprofile
		
		state.deviceid = iotprofile.deviceid
		state.tenantid = iotprofile.tenantid
		state.hostname = iotprofile.hostname
		state.primarykey = iotprofile.primarykey
		state.sas = iotprofile.signature
	}	
}

def cookiesFromJar(jar)
{
	def cookies = ""
	
	jar.each { k, v ->
		cookies += (k + "=" + v + "; ")
	}
	return cookies
}


def authenticateKohlerKonnect() {
	def cookieJar = [:]

	//Login screen
	def params = [
		uri: "https://login.microsoftonline.com",
		path: "/te/konnectkohler.onmicrosoft.com/b2c_1_signinup/oauth2/v2.0/authorize",
		contentType: "text/html",
		query: [
			response_type: "code",
			response_mode: "form_post",
			scope: "https://konnectkohler.onmicrosoft.com/platformapi/read openid profile offline_access",
			redirect_uri: "msaldee521c5-2a72-4fcd-8c4d-a044e607ca8b://auth",
			client_id: "dee521c5-2a72-4fcd-8c4d-a044e607ca8b"
		],
		textParser: true
	]
	def csrf = ""
	def transId = ""
	httpGet(params) { resp ->
	
		resp.headers.each {
			if (it.name == "Set-Cookie")
			{
				def cookieKvp = it.value.split(';')?.getAt(0)
				def cookieMatch = (cookieKvp =~ /(.*?)=(.*)/)
				def cookieKey = cookieMatch[0][1]
				def cookieValue = cookieMatch[0][2]
				cookieJar[cookieKey] = cookieValue
			}
        }
		
		def settingsList = (resp.data.text =~ /var SETTINGS = (.*?);/)[0][1]
		csrf = (settingsList =~ /"csrf":"(.*?)"/)[0][1]
		transId = (settingsList =~ /"transId":"(.*?)"/)[0][1]		
	}
	
	// Send login
	params = [
		uri: "https://login.microsoftonline.com",
		path: "/konnectkohler.onmicrosoft.com/B2C_1_SignInUp/SelfAsserted",
		contentType: "application/x-www-form-urlencoded",
		query: [
			tx: transId,
			p: "B2C_1_SignInUp"
		],
		body: [
			request_type: "RESPONSE",
			logonIdentifier: dtvKonnectUser,
			password: dtvKonnectPassword
		],
		headers: [
			"Cookie": cookiesFromJar(cookieJar),
			"X-CSRF-TOKEN": csrf
		]

	]
	def loginSuccess = false
	httpPost(params) { resp ->
		resp.headers.each {
			if (it.name == "Set-Cookie")
			{
				def cookieKvp = it.value.split(';')?.getAt(0)
				def cookieMatch = (cookieKvp =~ /(.*?)=(.*)/)
				def cookieKey = cookieMatch[0][1]
				def cookieValue = cookieMatch[0][2]
				cookieJar[cookieKey] = cookieValue
			}
        }
	}
	
	//Get the auth code

	params = [
		uri: "https://login.microsoftonline.com",
		path: "/konnectkohler.onmicrosoft.com/B2C_1_SignInUp/api/CombinedSigninAndSignup/confirmed",
		query: [
			csrf_token: csrf,
			tx: transId,
			p: "B2C_1_SignInUp"
		],
		headers: [
			"Cookie": cookiesFromJar(cookieJar)		
		],
		textParser: true
	]

	def authCode = ""
	httpGet(params) { resp ->
		def html = resp.data.text
		
		authCode = (html =~ /<input type='hidden' name='code' id='code' value='(.*?)'/)[0][1]
	}

	params = [
		uri: "https://login.microsoftonline.com",
		path: "/te/konnectkohler.onmicrosoft.com/b2c_1_signinup/oauth2/v2.0/token",
		requestContentType: "application/x-www-form-urlencoded",
		contentType: "application/json",
		body: [
			client_info: 1,
			scope: "https://konnectkohler.onmicrosoft.com/platformapi/read openid profile offline_access",
			code: authCode,
			grant_type: "authorization_code",
			redirect_uri: "msaldee521c5-2a72-4fcd-8c4d-a044e607ca8b://auth",
			client_id: "dee521c5-2a72-4fcd-8c4d-a044e607ca8b"
			
		]
	]
	
	httpPost(params) { resp ->
		state.access_token = resp.data.access_token
		state.access_token_expiration = resp.data.expires_on
		state.refresh_token = resp.data.refresh_token
		state.refresh_token_not_before = 0
	}
}

def getAccessToken() {
	def nowInSec = now()/1000
	
	if (nowInSec < state.access_token_expiration)
	{
		logDebug "token is good"
		return state.access_token
	}
	else
	{
		logDebug "token expired"
		authenticateKohlerKonnect()
		return state.access_token
	}
}

def getKonnectDevices() {
	def token = getAccessToken()
	def params = [
		uri: "https://connect.kohler.io",
		path: "/api/v1/platform/tenant/devices",
		contentType: "application/json",
		headers: [
			"Authorization": "Bearer ${token}"
		]
	]
	state.dtvDevices = [:]

	httpGet(params) { resp ->
		for (def i = 0; i < resp.data.size(); i++)
		{
			def device = resp.data[i]
			if (device.sku == "DTV")
			{
				state.dtvDevices[device.id] = device.logicalname
			}
		}
	}
}

def getKonnectDeviceDetails(deviceId) {
	def token = getAccessToken()
		def params = [
		uri: "https://connect.kohler.io",
		path: "/api/v1/platform/devices/dtv/${deviceId}/configuration",
		contentType: "application/json",
		headers: [
			"Authorization": "Bearer ${token}"
		]
	]
	
	httpGet(params) { resp ->
		state.konnectDtvValve1Count = resp.data.config.valveone?.configuredoutlets?.toInteger() ?: 0
		state.konnectDtvValve2Count = resp.data.config.valvetwo?.configuredoutlets?.toInteger() ?: 0
		
		state.konnectDtvLightCount = 0
		if (resp.data.config.lightmoduletype.light3 != "Na")
			state.konnectDtvLightCount = 3
		else if (resp.data.config.lightmoduletype.light2 != "Na")
			state.konnectDtvLightCount = 2
		else if (resp.data.config.lightmoduletype.light1 != "Na")
			state.konnectDtvLightCount = 1
			
		if (resp.data.config.lightmoduletype.light1 == "Dimmer")
			state.konnectDtvLight1Dimmable = true
		else
			state.konnectDtvLight1Dimmable = false
			
		if (resp.data.config.lightmoduletype.light2 == "Dimmer")
			state.konnectDtvLight2Dimmable = true
		else
			state.konnectDtvLight2Dimmable = false
			
		if (resp.data.config.lightmoduletype.light3 == "Dimmer")
			state.konnectDtvLight3Dimmable = true
		else
			state.konnectDtvLight3Dimmable = false
			
		if (resp.data.config.parts.amplifier == "Connected")
			state.konnectHasMusic = true
		else
			state.konnectHasMusic = false
	}
}


def installed() {
	logDebug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	logDebug "Updated with settings: ${settings}"
	unschedule()
	unsubscribe()
	initialize()
}

def initialize() {
	logDebug "initializing"
	
	if (!dtvKonnect)
	{
		cleanupChildDevices()
		createChildDevices()
	}
	else
	{
		cleanupKonnectChildDevices()
		createKonnectChildDevices()
	}
	cleanupSettings()
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
	
	if (dtvValve1Count > 0)
	{
		if (!showerDevice.getChildDevice("kohlerdtv:valve1"))
			showerDevice.addChildDevice("kohlerdtv", "Kohler DTV+ Valve Controller", "kohlerdtv:valve1", ["name": "Valve 1", isComponent: true])
	}
	
	if (dtvValve2Count > 0)
	{
		if (!showerDevice.getChildDevice("kohlerdtv:valve2"))
			showerDevice.addChildDevice("kohlerdtv", "Kohler DTV+ Valve Controller", "kohlerdtv:valve2", ["name": "Valve 2", isComponent: true])
	}
}

def createKonnectChildDevices() {
    def showerDevice = getChildDevice("kohlerdtv:shower")
	if (!showerDevice)
	{
		showerDevice = addChildDevice("kohlerdtv", "Kohler DTV+ Shower", "kohlerdtv:shower", 1234, ["name": dtvName, isComponent: true])
	}
    for (def i = 0; i < state.konnectDtvValve1Count; i++) {
        if (!showerDevice.getChildDevice("kohlerdtv:valve1_${i+1}"))
            showerDevice.addChildDevice("kohlerdtv", "Kohler DTV+ Valve", "kohlerdtv:valve1_${i+1}", ["name": this.getProperty("dtvValve1_${i+1}"), isComponent: true])
    }
    for (def i = 0; i < state.konnectDtvValve2Count; i++) {
        if (!showerDevice.getChildDevice("kohlerdtv:valve2_${i+1}"))
            showerDevice.addChildDevice("kohlerdtv", "Kohler DTV+ Valve", "kohlerdtv:valve2_${i+1}", ["name": this.getProperty("dtvValve2_${i+1}"), isComponent: true])
    }
    for (def i = 0; i < state.konnectDtvLightCount; i++) {
        if (!showerDevice.getChildDevice("kohlerdtv:light_${i+1}"))
        {
			def dimmable = false
			if (i == 0 && konnectDtvLight1Dimmable)
				dimmable = true
			else if (i == 1 && konnectDtvLight2Dimmable)
				dimmable = true
			else if (i == 2 && konnectDtvLight3Dimmable)
				dimmable = true
            if (dimmable)
                showerDevice.addChildDevice("kohlerdtv", "Kohler DTV+ Dimmable Light", "kohlerdtv:light_${i+1}", ["name": this.getProperty("dtvLight_${i+1}"), isComponent: true])
            else
                showerDevice.addChildDevice("kohlerdtv", "Kohler DTV+ Light", "kohlerdtv:light_${i+1}", ["name": this.getProperty("dtvLight_${i+1}"), isComponent: true])
        }
    }
	
	if (state.konnectDtvValve1Count > 0)
	{
		if (!showerDevice.getChildDevice("kohlerdtv:valve1"))
			showerDevice.addChildDevice("kohlerdtv", "Kohler DTV+ Valve Controller", "kohlerdtv:valve1", ["name": "Valve 1", isComponent: true])
	}
	
	if (state.konnectDtvValve2Count > 0)
	{
		if (!showerDevice.getChildDevice("kohlerdtv:valve2"))
			showerDevice.addChildDevice("kohlerdtv", "Kohler DTV+ Valve Controller", "kohlerdtv:valve2", ["name": "Valve 2", isComponent: true])
	}
	
	if (state.konnectHasMusic) 
	{
		if (!showerDevice.getChildDevice("kohlerdtv:amplifier"))
			showerDevice.addChildDevice("kohlerdtv", "Kohler DTV+ Amplifier", "kohlerdtv:amplifier", ["name": "Amplifier", isComponent: true])
	}
}

def cleanupChildDevices()
{
	for (device in getChildDevices())
	{
		def deviceId = device.deviceNetworkId
		if (dtvValve1Count < 6)
		{
			for (def i = 6; i > dtvValve1Count; i--)
			{
				if (deviceId == "kohlerdtv:valve1_${i}")
				{
					deleteChildDevice(device.deviceNetworkId)
				}
			}
		}
		
		if (dtvValve1Count == 0)
			deleteChildDevice("kohlerdtv:valve1")
		
		if (dtvValve2Count < 6)
		{
			for (def i = 6; i > dtvValve2Count; i--)
			{
				if (deviceId == "kohlerdtv:valve2_${i}")
				{
					deleteChildDevice(device.deviceNetworkId)
				}
			}
		}
		
		if (dtvValve2Count == 0)
			deleteChildDevice("kohlerdtv:valve2")
		
		if (dtvLightCount < 3)
		{
			for (def i = 3; i > dtvLightCount; i--)
			{
				if (deviceId == "kohlerdtv:light_${i}")
				{
					deleteChildDevice(device.deviceNetworkId)
				}
			}
		}
	}
}

def cleanupKonnectChildDevices()
{
	for (device in getChildDevices())
	{
		def deviceId = device.deviceNetworkId
		if (state.konnectDtvValve1Count < 6)
		{
			for (def i = 6; i > state.konnectDtvValve1Count; i--)
			{
				if (deviceId == "kohlerdtv:valve1_${i}")
				{
					deleteChildDevice(device.deviceNetworkId)
				}
			}
		}
		
		if (konnectDtvValve1Count == 0)
			deleteChildDevice("kohlerdtv:valve1")
		
		if (state.konnectDtvValve2Count < 6)
		{
			for (def i = 6; i > state.konnectDtvValve2Count; i--)
			{
				if (deviceId == "kohlerdtv:valve2_${i}")
				{
					deleteChildDevice(device.deviceNetworkId)
				}
			}
		}
		
		if (konnectDtvValve2Count == 0)
			deleteChildDevice("kohlerdtv:valve2")
		
		if (state.konnectDtvLightCount < 3)
		{
			for (def i = 3; i > state.konnectDtvLightCount; i--)
			{
				if (deviceId == "kohlerdtv:light_${i}")
				{
					deleteChildDevice(device.deviceNetworkId)
				}
			}
		}
		
		if (!state.konnectHasMusic)
			deleteChildDevice("kohlerdtv:amplifier")
	}
}

def cleanupSettings()
{
	def allProperties = this.settings
	def deviceName = null

	for (property in allProperties) {
		def valve1Count = dtvKonnect ? state.konnectDtvValve1Count : dtvValve1Count
		if (valve1Count < 6)
		{
			for (def i = 6; i > valve1Count; i--)
			{
				if (property.key == "dtvValve1_${i}")
				{
					app.removeSetting(property.key)
				}
			}
		}
		def valve2Count = dtvKonnect ? state.konnectDtvValve2Count : dtvValve2Count
		if (valve2Count < 6)
		{
			for (def i = 6; i > valve2Count; i--)
			{
				if (property.key == "dtvValve2_${i}")
				{
					app.removeSetting(property.key)
				}
			}
		}
		def lightCount = dtvKonnect ? state.konnectDtvLightCount : dtvLightCount
		if (lightCount < 6)
		{
			for (def i = 6; i > lightCount; i--)
			{
				if (property.key == "dtvLight_${i}")
				{
					app.removeSetting(property.key)
				}
				else if (property.key == "dtvLightDimmable_${i}")
				{
					app.removeSetting(property.key)
				}
			}
		}
	}
}

def getValve1Count() {
	return dtvKonnect ? state.konnectDtvValve1Count : dtvValve1Count
}

def getValve2Count() {
	return dtvKonnect ? state.konnectDtvValve2Count : dtvValve2Count
}

def getLightCount() {
	return dtvKonnect ? state.konnectDtvLightCount : dtvLightCount
}

def handleOn(device, id) {
	if (dtvKonnect) {
		mqttOverHttps("DTV-BSNBBLPH", "control", "LIGHT_BRIDGE_CTRL", [
			code: "LIGHT_BRIDGE_CTRL",
			light: getLightById(id),
			brightness: "100",
			status: "On"
		])
	}
	else
		log.error "Lights are not supported without a Konnect bridge"

}

def handleOff(device, id) {
	if (dtvKonnect) {
		mqttOverHttps("DTV-BSNBBLPH", "control", "LIGHT_BRIDGE_CTRL", [
			code: "LIGHT_BRIDGE_CTRL",
			light: getLightById(id),
			brightness: "0",
			status: "Off"
		])
	}
	else
		log.error "Lights are not supported without a Konnect bridge"
}

def handleSetVolume(device, id, volumelevel) {
	if (dtvKonnect) {
		mqttOverHttps("DTV-BR9RHTDJ", "control", "VOLUME_UP_DOWN_CTRL", [
			code: "VOLUME_UP_DOWN_CTRL",
			percentage: volumelevel.toString()
		])
	}
	else
		log.error "Volume is not supported without a Konnect bridge"
}

def handleSetInput(device, id, input) {
	if (dtvKonnect) {
		mqttOverHttps("DTV-BQXWX8DL", "control", "SELECT_AUDIO_SOURCE_CTRL", [
			code: "SELECT_AUDIO_SOURCE_CTRL",
			source: input
		])
	}
	else
		log.error "Audio input is not supported without a Konnect bridge"
}

def handleSetLevel(device, id, level) {
	if (dtvKonnect) {
		mqttOverHttps("DTV-BSNBBLPH", "control", "LIGHT_BRIDGE_CTRL", [
			code: "LIGHT_BRIDGE_CTRL",
			light: getLightById(id),
			brightness: level.toString(),
			status: "On"
		])
	}
	else
		log.error "Lights are not supported without a Konnect bridge"
}

def getLightById(id) {
	if (id.startsWith("light_1"))
		return "1"
	else if (id.startsWith("light_2"))
		return "2"
	else if (id.startsWith("light_3"))
		return "3"
}

def logDebug(msg) {
    if (settings?.debugOutput) {
		log.debug msg
	}
}

def renewSasIfNeeded() {
	def stripPrefix = state.sas.replaceAll("SharedAccessSignature ","")
	def pairs = stripPrefix.split("&")
	
	for (kvp in pairs)
	{
		def keyAndValue = kvp.split("=")
		if (keyAndValue[0] == "se")
		{
			def expirationDate = Integer.parseInt(keyAndValue[1])
			def currentTime = now().intdiv(1000)
			if (expirationDate < currentTime)
			{
				logDebug "The SAS token has expired, renewing"
				registerKonnectDevice()
			}
			break
		}
	}
}

def mqttOverHttps(sysid, type, code, msgBody) {
	renewSasIfNeeded()
	def body = [
		messageid: UUID.randomUUID().toString(),
		protocol: "MQTT",
		timestamp:now().intdiv(1000),
		ttl:"3000",
		sku:"DTV",
		type:"CTL",
		internalid:UUID.randomUUID().toString(),
		data: [
			type: type,
			attributes: [msgBody],
			code: code
		],

		deviceid: dtv,
		tenantid: state.tenantid,
		ver: "1.0",
		sysid: sysid,
		simulated: true,
		durable: true
	]
	
	def params = [
		uri: "https://" + state.hostname,
		path: "/devices/" + state.deviceid + "/messages/events",
		query: [ "api-version": "2016-11-14"],
		contentType: "text/plain",
		requestContentType: "text/plain",
		headers: [
			"Authorization": state.sas,
			"Host": state.hostname
		],
		body: groovy.json.JsonOutput.toJson(body)
    ]
	def result = false
	httpPost(params) { resp ->
		if (resp.status == 204)
            result = true
        else
            result = false
	}
    return result
}