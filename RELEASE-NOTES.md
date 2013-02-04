# 0.1.29

* Add an overview panel - make it the default panel

# 0.1.28

* Add a basic RC channels info panel
* Add preferences option to force the screen to stay on (default off)
* Fix mode display when waking from sleep
* Change param list over to new fragment system
* Use gestures to change the info panels
* Add periodic updates for rcchannels (share actor client code with map & mainactivity)

For next release:
* Do something to allow waypoint list selection with action bar ( http://stackoverflow.com/questions/3111354/android-listview-stay-selected )
* Show heartbeat lost on the overview screen
* FIXME: fix udp receiver
* FIXME: add waypoint change type (via menu dropdown)
* FIXME: make a website
* Change waypoint icons as appropriate...
* FIXME: Use a state machine to ensure we don't get confused if someone moves a waypoint while we are busy uploading new waypoints

# 0.1.27

* Don't consider -1 for battery pct a problem (it just means vehicle doesn't have appropriate hw)
* Fix rare autobug of first time direct connect to APM failing...

# 0.1.26

* Show a line to indicate where the plane is heading (if we know)
* Implicitly set AUTO mode if someone does GOTO on a waypoint

# 0.1.25

* keep CPU awake if we are connected to the serial port (needed to keep logging etc...) - you should unplug the device when not in use
* all mavlink packets are now stored to the log file (not just packets we were expecting)
* draw plane in yellow if voltage falls too low
* draw plane in yellow if rssi falls too low
* show # of sats in view & warn if below min # (per http://www.diydrones.com/forum/topics/apm2-5-gone)

# 0.1.24

* Fix mode display/setting for arducopter (if you are using an arducopter, please report success/failure in the forum)
* Auto close keyboard after editing altitude
* Turn on optimization in the compiler
* Store last known vehicle position in android prefs (so even if the app is terminated you still know where the plane was)

# 0.1.22

* add an altitude editor box for waypoints
* waypoint command codes are now displayed as strings (land, rtl, etc...)
* altitudes now displayed as AGL or MSL as appropriate
* add support for auto continue checkbox
* Remove an autocrash for another whacky Android clone with quasi-legal maps
* If you drag a waypoint while the parameter list is being downloaded, bad things no longer happen (needed separate retry timers)

# 0.1.21

* Add waypoint editing (drag, add, delete, etc...)
* Add waypoint goto

# 0.1.18

* Fix a FC if google maps was not installed

# 0.1.16

* Clean up param list to be two nice columns
* fix an auto bug report

# 0.1.15

* Show home icon for first waypoint (home).  For safety that waypoint is not yet draggable.
* Allow click/drag to move waypoints.
* Show a small 'toast' window whenever we receive vehicle status messages
* We now ignore stale mode messages from the device (prevents mode menu flickering)
* Substantial cleanup of map code
* We now listen for WAYPOINT_CURRENT message and show current waypoint in red

# 0.1.14

* Support inbound UDP (in addition to the outbound support from the previous release)
* parameter display works!!!
* serial port IO is now fully async (prevents nasty retry hacks)
* Basic (but ugly) parameter editing
* Fix one more whacky auto crash report from strange devices...
* FIXME: Add checkbox to save params to flash (currently changes just live in RAM)
* Sort parameters by name
* Add inbound UDP gateway support (previous release added outbound support)

# 0.1.13

* Support GPS_RAW_INT - makes copters work
* Fix race condition with USB notification on some devices
* Replace the bum 0.1.12 release with something that doesn't crash

# 0.1.12
* Fix crash when no waypoints were on the target (auto bug report from China)
* Allow adjusting serial link baud rates
* support of direct APM connection
* FIXME: parameter display/editing not quite ready 
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



