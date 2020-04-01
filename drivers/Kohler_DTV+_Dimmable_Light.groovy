/**
 *  Kohler DTV+ Dimmable Light
 *
 *  Copyright 2019-2020 Dominick Meglio
 *
 *	If you find this useful, donations are always appreciated 
 *	https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=7LBRPJRLJSDDN&source=url
 *
 */
 
metadata {
    definition (name: "Kohler DTV+ Dimmable Light", namespace: "kohlerdtv", author: "dmeglio@gmail.com") {
		capability "SwitchLevel"
		capability "Switch"
		capability "Light"
    }
}

def setLevel(level, duration) {
	parent.handleSetLevel(device, device.deviceNetworkId.split(":")[1], level)
}

def setLevel(level) {
	parent.handleSetLevel(device, device.deviceNetworkId.split(":")[1], level)
}

def off() {
	parent.handleOff(device, device.deviceNetworkId.split(":")[1])
}

def on() {
	parent.handleOn(device, device.deviceNetworkId.split(":")[1])
}