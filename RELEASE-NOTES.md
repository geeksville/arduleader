FIXME: fix eq? https://groups.google.com/forum/?fromgroups=#!searchin/scala-on-android/reflection/scala-on-android/Q0E_OMYqE0A/KoQLxajQSccJ
FIXME: prompt and do basic droneshare upload
FIXME: show prefs screen if no droneshare username
FIXME: properly handle partial tlogs
FIXME: only upload interesting files
FIXME: restart after failed uploads...
FIXME: at end of upload offer to show kml locally or share via facebook
FIXME: move droneshare client stuff to common
internationalize strings

# 1.3.03
* Fix a few autobugs

# 1.3.02
* Add full support for the Archos Gamepad android device (see archos-notes.txt)
* Update to latest parameter docs (AC 2.91b/AP 2.71)
* Shut down FTDI more gracefully

# 1.2.16
* Merge in a fix contributed by danpe: Make maps more reliable when internet connection is marginal
* Allow entering negative numbers on the parameter and waypoint editors
* Rssi speech warning thresholds are now set automatically

# 1.2.15
* Experiment with using the FTDI library

# 1.2.13
* Fix the damn Samsung Note

# 1.2.07
* Scale down the gamepad inputs so they have more useful dynamic range
* Fix gamepad elevator direction
* Add support for multipane view on Note2 sized devices
* Disallow landscape view on phone sized devices (our info pane gets too small to be useful)
* Don't ever use param read by index, because it is not supported on older AC builds

# 1.2.05
* Gamepad support ready for alpha testing.  Press Y to do RTL, press L1 to toggle fence mode, use the two joysticks like a RC controller, press start to return control to RC transmitter.  
* Thanks to Robert W, Earl C, Jack O, Linux P, Anthony G, Robert L, Jeremy Z and Rana for donating so I could buy a gaming tablet.  Not yet purchased, but without their prodding I wouldn't have gotten around to
adding gamepad support for a while...
* For TCP connections, allow user to specify an outpound port #
* Parameter download is much faster for most devices
* Add a servos output tab that shows raw values driven to vehicle servos
* Fix problem where bluetooth connections would be torn down on screen rotation
* Fix problem where sometimes the vehicle mode would be listed wrong on application start
* Somehow I had disabled showing the keyboard for waypoint altitudes - fixed
* Default to showing the keyboard if we edit a parameter or waypoint

# 1.2.04
* Check for bluetooth by device class (should recognize all serial devices, rather than just the Deal Extreme one)

# 1.2.03
* Make bluetooth hw optional

# 1.2.02
* Add support for another FTDI device type
* Display groundspeed on the overview
* Allow editing waypoint advanced parameters
* Add menu option to manually level an Arduplane (useful at the field sometimes)

# 1.2.00
* Add support for bluetooth serial links.  Thanks to Pieter@diydrones for donating a bluetooth adapter for me to develop with.
* Default to speech on for new installs
* Cope with virgin APMs that don't have a GPS and therefore have no waypoints

# 1.1.02
* Show airspeed on overview screen
* Speak warnings for battery levels, radio signal levels, lack of sats
* Fix an autobug report: cope with devices that have multiple serial ports
* Use best source of altitude when making speech announcements
* I somehow busted altitude editing on waypoints - fixed
* Make the parameter documentation scrollable

# 1.1.01
* We now register as a handler for .wpt/.txt files so users can click on those file types and upload waypoints to the vehicle
* Parameter list updates are now much faster
* Waypoint list updates are now much faster
* I've double checked that setting waypoint type works - if you see a problem, please post in the group
* Properly update the mode menu for vehicle type if app is already running when vehicle connects
* We now show a dropdown menu to choose named parameter values

# 1.1.0
* We now register as a handler for .fen URLs/files - so you can easily click on those file types and auto create a geofence.
* Announce geofence breach via speech
* Default preferences to logging tlogs on
* Fix a rare bug with setting parameters

# 1.0.12
* Fix the ? param problem for VRBrain+XBee

# 1.0.11
* Geofence is now shown on the map (if set)
* Parameter list update code now much more efficient

# 1.0.10
* Replace DOA builds 7 through 9
* We now show baro based altitude (rather than GPS based) whenever possible

* automatically start new log file once heartbeat timer restarts
* arm/disarm menu option
* set home menu option
* add an option to set home
* droneshare upload (from spooldir on disk)
* save/load wpt files
* param file save and load
* have nestor parse text log files
* Display armed/not armed per MP:  (hb.base_mode & (byte)MAVLink.MAV_MODE_FLAG.SAFETY_ARMED) == (byte)MAVLink.MAV_MODE_FLAG.SAFETY_ARMED

For next release:
* Save waypints to device only upon user command
* FIXME: move to 3dr github
* FIXME: Use a state machine to ensure we don't get confused if someone moves a waypoint while we are busy uploading new waypoints
* Show docs on parameter names (if possible)
* FIXME: announce and display a failsafe condition (FIXME, look at arduplane)
* FIXME: announce arming and disarming. (FIXME - pull down arduplane code)
* Change parameter editing over to the new spiffy action bar style of the waypoint editor
* FIXME - altitude offset should only be set once device has calibrated
* FIXME - handle editing of some of the more exotic waypoint type
* FIXME Also, I can add a "Debug" pane in the next version that includes "# of params download, # of messages sent, etc..." which could provide some really good clues.
* FIXME if we timeout fetching waypoints, go ahead and try to fetch the params

# 1.0.07
* Fix a minor auto reported bug: https://github.com/geeksville/arduleader/issues/49

# 1.0.06
* Mission Motors Inc. kindly open source the tiny HTTP server we now embed into the application (useful for a future extension...)
* don't fetch waypoints until we receive our first MISSION_CURRENT msg (fixes http://diydrones.com/group/andropilot-users-group/forum/topics/new-release-1-0-05-should-hopefully-fix-problems-with-android-4-1?commentId=705844%3AComment%3A1140559&groupId=705844%3AGroup%3A1132500)
* Fix an auto crash report for folks who try follow-me mode on a device that doesn't have a GPS

# 1.0.05
* Fix an autoreported bug which apparently only occurs on android 4.1.1/4.1.2.  If you have _any_ crashes on the app now, please post in the group with
your phone model, time of crash UTC and what you were doing.  Hopefully I can knock any last ones down.

# 1.0.04
* Okay - that was fast.  A clean fix is now in for the hack of a fix in 1.0.03.  Parameter display should be reliable again.

# 1.0.03
* Ouch - 0.2.01 introduced a nasty crash which would occur the _second_ time the parameters list was two screens away from the current screen.
This release is a quick patch to fix that problem.  However, there is still a problem that sometimes the parameter screen can show up blank.
Until I fix this more minor problem, just rotate the tablet and the parameters screen will be okay. 

# 1.0.00
* Make lead-it/follow-me more responsive
* Auto turn-off follow-me if the mode is changed by someone else
* Make lead-it not a checkbox, rather just change to some other mode to stop following

# 0.2.01
* 'Lead-it' mode now ready for testing.  Like follow-me, but if you rotate the vehicle will stay in front (or behind you), if you tilt tablet fore and back it will move close or away.  To use this mode, go to settings and set the min and max vehicle distance.  Then hold screen in portrait orientation and click the 'lead-it' menu item.
* Add support for the new 2.69 arduplane 'training' mode
* We now show parameter docs on the parameter list

# 0.2.00
* We now decode parameter values to show nice names for most parameters
* Can now change waypoint type (via menu dropdown)
* Wrote a developers guide: https://github.com/geeksville/arduleader/wiki/Developers-Guide
* Support direct TCP connections (useful for connecting to the SITL simulator)
* Add understanding for JUMP waypoints - all waypoint types should now be understood
* Don't show bogus waypoints if x & y are not set
* Use new waypoint icons contributed by Scott Berfield
* Don't enable scroll gestures for maps (conficts with page switch gesture)
* Print better user info for UDP connections
* Add a TCP uplink option (for development use)
* Setup SITL testing framework (see https://github.com/diydrones/ardupilot/commit/deb825b57583a4dd0fb8452ad0afdad07ab34c5b)
* HUD now burns lots less CPU 
* Default autocontinue to true for new waypoints
* Previously we only kept the screen on if the map was shown, we now do it if any screen is shown
* Previously if you switched to the waypoints or parameters view too quickly they would never update - fixed

# 0.1.36
* Spiffy new Quad icons contributed by Scott Berfield.  Thanks Scott!
* Add help menu contributed by Peter Meister.  Thanks Peter!
* 'Follow-me' mode added to menu-bar (change settings for 'Lead distance' if you prefer lead-it mode)
* Internal code cleanup
* Fix heartbeat vehicle type
* Show vehicle alts as AGL (currently just relative to the home position)
* Speak "Heartbeat lost" if we lose vehicle comms
* Show mode as "unknown" if we don't have comms
* NOT YET READY FOR TESTING: 'Lead-it' mode.  Like follow-me, but if you rotate the vehicle will stay in front (or behind you), if you tilt tablet fore and back it will move close or away.
     
# 0.1.35
* Add settings option to control # of meter change needed for voice announcement (actually this was in 0.34)
* Shorten some mode names so they sound better
* Make usb host mode optional (so folks can use UDP on non host mode devices)
* Fix overview layout so it will look good on more devices 
* Add preferences option to automatically back-up parameters to the SD card

# 0.1.34
* Add back nasty USB hack if urb.position is always zero (should fix at least android 4.0, probably 3.1)
* If usb hack needed, limit most mavlink streams to 1 hz
* Turn on direct zerocopy usb buffers
* Inbound UDP now works: Use this if your serial uplink is on some other machine (a different tablet or a PC)
* Outbound UDP now works: Use this if you want to share your serial link with some other machine
* Don't announce speech enabled when orientation changes
* Shorten many of the spoken phrases (but FIXME, haven't yet shortened mode names)
* Use urgent mode for speech of mode changes (to make sure this overrides batteryish msgs)
* In portrait mode, don't show the info pane - use swipes to switch between map etc...
* On phones never show info panels

# 0.1.31

* Add speech output
* Add HUD display (in portrait mode) (thanks to the copter-gcs source!)
* Fix nasty USB performance problem - we can get a very high packet rate now
* Fix a problem where I was inadvertently never dequeing something from a worker thread (really nasty)
* Set background colors on overview screen, so hacked up android builds will work better
* Bump up stream rate on RC and stuff I need for the HUD

# 0.1.29

* Add an overview panel - make it the default panel

# 0.1.28

* Add a basic RC channels info panel
* Add preferences option to force the screen to stay on (default off)
* Fix mode display when waking from sleep
* Change param list over to new fragment system
* Use gestures to change the info panels
* Add periodic updates for rcchannels (share actor client code with map & mainactivity)


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



