# 0.1.12
* Fix crash when no waypoints were on the target (auto bug report from China)
* Allow adjusting serial link baud rates
* (buggy) support of direct APM connection
* FIXME: display parameters when in portrait mode (parameter editing coming in next release)
* RTS/CTS flow control for the telemetry link
* Fix crash that could occur the first time a device was ever connected
* Ask for more stream types (so we work with arducopter)
* Add outbound UDP gateway support (see settings menu - for now you need to unplug/replug serial port if you change settings)

# 0.1.11
* Add a preferences screen
* Make logging controllable by preferences
* Make altitude to use for guided mode set by preferences
* Fix crash when waking from sleep
* Don't leave service running unless we must (to service the serial port) - saves battery
* Show battery voltage on the status pop-up
* When we download waypoints, don't blow away the icon for the plane (duh)

# 1.0.10
* Fixes for auto crash reports on devices I don't have to test with...

# 1.0.9
* Minor changes to work with ICS

# 1.0.8
* Switch to ACRA for even better crash reports

# 1.0.7
* Get exception reports through google

# 0.1.6
* First cut at showing waypoints (read only and not fully decoded)
* Long press to enter guided mode to the place you clicked on the screen (quick hack, hardwired to 100m AGL)  Real code will pop up a menu/dialog

# 0.1.5
* You can now set modes from the action bar

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



