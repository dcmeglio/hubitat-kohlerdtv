# hubitat-kohlerdtv
Kohler DTV+ Integration for Hubitat. Monitors lights, amplifier, and valves used in a Kohler DTV+ shower
 
## Devices
You must install the following device drivers for this to work
* Kohler DTV+ Shower
* Kohler DTV+ Valve Controller
* Kohler DTV+ Valve
* Kohler DTV+ Light
* Kohler DTV+ Dimmable Light
* Kohler DTV+ Amplifier

## Apps
The Kohler DTV+ Integration app configures your shower. It will create the child devices for all of the individual components.

### Configuration
To connect to the device you will need to specify the IP of your System Controller and specify the details about your lights and valves. If you have a Kohler Konnect module it can optionally use your Konnect credentials to automatically discover your system components.

## Limitations
Currently, with local control, you can turn the shower on/off, turn on presets, and control individual valves. To control the lights or amplifier volume you must have a Kohler Konnect module installed. Note that control of those components is therefore cloud based. This is due to limitations that currently exist in the Kohler DTV+ Controller device. If Kohler ever adds these capabilities through local API calls, this will be corrected.

## Donations
If you find this app useful, please consider making a [donation](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=7LBRPJRLJSDDN&source=url)! 

## Revision History
* v 2020.03.31 - Added the ability to control lights and amplifier using a Kohler Konnect module