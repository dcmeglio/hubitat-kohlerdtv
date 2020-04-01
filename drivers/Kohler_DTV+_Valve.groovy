/**
 *  Kohler DTV+ Valve
 *
 *  Copyright 2019-2020 Dominick Meglio
 *
 *	If you find this useful, donations are always appreciated 
 *	https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=7LBRPJRLJSDDN&source=url
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