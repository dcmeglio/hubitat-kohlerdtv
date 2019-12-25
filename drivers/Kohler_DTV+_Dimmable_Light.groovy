/**
 *  Kohler DTV+ Dimmable Light
 *
 *  Copyright 2019 Dominick Meglio
 *
 */
 
metadata {
    definition (name: "Kohler DTV+ Dimmable Light", namespace: "kohlerdtv", author: "dmeglio@gmail.com") {
		capability "SwitchLevel"
    }
}

def setLevel(level, duration) {
	//parent.handleOff(device, device.deviceNetworkId.split(":")[1])
}