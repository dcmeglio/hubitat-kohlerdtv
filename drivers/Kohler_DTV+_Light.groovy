/**
 *  Kohler DTV+ Light
 *
 *  Copyright 2019 Dominick Meglio
 *
 */
 
metadata {
    definition (name: "Kohler DTV+ Light", namespace: "kohlerdtv", author: "dmeglio@gmail.com") {
		capability "Light"
    }
}

def off() {
	//parent.handleOff(device, device.deviceNetworkId.split(":")[1])
}

def on() {
	//parent.handleOn(device, device.deviceNetworkId.split(":")[1])
}