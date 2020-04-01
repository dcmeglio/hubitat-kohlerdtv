/**
 *  Kohler DTV+ Dimmable Light
 *
 *  Copyright 2019 Dominick Meglio
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

def off() {
	parent.handleOff(device, device.deviceNetworkId.split(":")[1])
}

def on() {
	parent.handleOn(device, device.deviceNetworkId.split(":")[1])
}