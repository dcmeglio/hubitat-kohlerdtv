# hubitat-kohlerdtv
Kohler DTV+ Integration for Hubitat. Monitors lights and valves used in a Kohler DTV+ shower
 
## Devices
You must install the following device drivers for this to work
* Kohler DTV+ Shower
* Kohler DTV+ Valve Controller
* Kohler DTV+ Valve
* Kohler DTV+ Light
* Kohler DTV+ Dimmable Light

## Apps
The Kohler DTV+ Integration app configures your shower. It will create the child devices for all of the individual components.

### Configuration
To connect to the device you will need to specify the IP of your System Controller and specify the details about your lights and valves. If you have a Kohler Konnect module it can optionally use your Konnect credentials to automatically discover your system components.

## Limitations
Currently the only commands that can be sent are to turn the shower on/off and start a preset. There is currently no way to manage lights, music, or steam. Light status is reported however.