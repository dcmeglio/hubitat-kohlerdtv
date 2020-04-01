/**
 *  Kohler DTV+ Valve Controller
 *
 *  Copyright 2019 Dominick Meglio
 *
 *	If you find this useful, donations are always appreciated 
 *	https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=7LBRPJRLJSDDN&source=url
 *
 */
 
metadata {
    definition (name: "Kohler DTV+ Valve Controller", namespace: "kohlerdtv", author: "dmeglio@gmail.com") {
		capability "TemperatureMeasurement"
		capability "ThermostatSetpoint"
    }
}

def setHeatingSetpoint(temperature) {
    parent.handleHeatingSetpoint(device, device.deviceNetworkId.split(":")[1], temperature)
}
