/**
 *  Kohler DTV+ Amplifier
 *
 *  Copyright 2019-2020 Dominick Meglio
 *
 *	If you find this useful, donations are always appreciated 
 *	https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=7LBRPJRLJSDDN&source=url
 *
 */
 
metadata {
    definition (name: "Kohler DTV+ Amplifier", namespace: "kohlerdtv", author: "dmeglio@gmail.com") {
		capability "AudioVolume"
		
		command "setInput", [[name:"Input*","type":"ENUM","description":"Input","constraints":["AUX1", "AUX2", "Bluetooth"]]]
    }
}

def mute() {
	state.previousVolume = device.currentValue("volume")
	setVolume(0)
}

def setVolume(volumelevel) {
	parent.handleSetVolume(device, device.deviceNetworkId.split(":")[1], volumelevel)
}

def setInput(input) {
	parent.handleSetInput(device, device.deviceNetworkId.split(":")[1], input)
}

def unmute() {
	if (device.currentValue("volume") == 0)
		setVolume(state.previousVolume)
}

def volumeDown() {
	if (device.currentValue("volume") == 0)
		return
	else if (device.currentValue("volume") < 5)
		setVolume(0)
	else
		setVolume(device.currentValue("volume")-5)
}

def volumeUp() {
	if (device.currentValue("volume") == 100)
		return
	else if (device.currentValue("volume") > 95)
		setVolume(100)
	else
		setVolume(device.currentValue("volume")+5)
}
