# 0.1.4
* Properly handle screen rotation (no crashes)
* Send heartbeats to vehicle
* Download waypoints from vehicle (not yet shown)
* Zoom in to vehicle on first contact
* If device gets unplugged while app not in foreground, let service handle it
* Mark GPS as optional, in the hopes that this app will work on GPSless devices (please send reports of success/failure)

# 0.1.3
* Mavlink log files are now emitted to the logs directory (on Android versions this is in /sdcard/andropilot)
* Serial port connection/disconnection should be reliable now
* Show airplane in red if we lose heartbeats or USB connection
* Screen rotation may still have issues
* Next release will add: flight mode control and waypoint display



