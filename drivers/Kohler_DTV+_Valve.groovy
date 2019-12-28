/**
 *  Kohler DTV+ Light
 *
 *  Copyright 2019 Dominick Meglio
 *
 */
 
metadata {
    definition (name: "Kohler DTV+ Valve", namespace: "kohlerdtv", author: "dmeglio@gmail.com") {
		capability "Valve"
    }
}

def open() {
	parent.handleOpen(device, device.deviceNetworkId.split(":")[1])
}

def close() {
	parent.handleClose(device, device.deviceNetworkId.split(":")[1])
}