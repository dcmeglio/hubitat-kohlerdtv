/**
 *  Kohler DTV+ Valve Controller
 *
 *  Copyright 2019 Dominick Meglio
 *
 */
 
metadata {
    definition (name: "Kohler DTV+ Valve Controller", namespace: "kohlerdtv", author: "dmeglio@gmail.com") {
		capability "TemperatureMeasurement"
		capability "ThermostatSetpoint"
    }
}

