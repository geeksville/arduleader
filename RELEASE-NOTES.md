* FIXME - do background param fetch

# 2.0.1
* Fix a small race on loading wpts

# 2.0.0
* Add support for moving waypoints up/down to change flt plan order
* Add support for saving waypoints to SD card
* Add a 'delete all waypoints' menu entry
* KitKat only: Add support for loading waypoints from a menu (no need for a file-browser app) (Older androids
  can still use a file browser (like ASTRO File Manager) or web browser to open .wpt files).
  Supports google drive for storing wpt/fence/param files
* Minor checklist improvements
* Last known vehicle location is saved to flash (no longer will unknown locations be shown off coast of Africa ;-))
* Fix improper zooming which could sometimes occur on first vehicle connect

# 1.9.14
* Hook up GCSAPI to the checklists

# 1.9.13
* Allow users to place custom checklists in /sdcard/andropilot/checklists/plane.html or copter.html
* Support FBSOD for Naza-M autopilots (thanks Jay Davis and Mike Knott)
* List SPORT mode in the modes menu
* Fix a couple of rare autobugs

# 1.9.12
* Oops, the 1.9.10 beta broke speech announcement of altitude - thanks Luciano
* Hide OF_LOITER, TOY_A and TOY_B modes because no one seems to use them
* Fix an autobug

# 1.9.10
* ac3.1rc5 now reports gps, magnetometer and rc radio faults - Speak announce such faults (thx Randy)
* Run speech daemon in the background - this allows speech notifications while
phone is asleep in your pocket.
* Spend a bit more time with power profiling.  Using a more efficient version
of the mavlink code saves a lot of battery.  A Nexus 7 should now be able to
log for almost 2 hours per charge.
* Fix more minor autobugs

# 1.9.09
* Fix a couple of minor/rare autobugs

# 1.9.6
* Support 'spectator/read-only mode'.  The new 'Read Only' menu item will switch Andropilot into a mode where it _never_ sends packets to the vehicle.  This is useful for vehicles that only have a one-way radio link

# 1.9.5
* Support the new px4 USB device IDs (reenables direct USB connection to Pixhawk) (Thanks for the report Tridge)
* Support VERY small screens (320x240) with a special UI (move bluetooth options into menu etc...) (Thanks Rory San Miguel)
* Better support for nvidia shield 
* Prompt for arm & disarm in the menu  (thanks Edgar)
* Do not allow arming (from andropilot only) if the vehicle is not level (thanks Craig Baldwin for the idea)

# 1.9.3
* We now fetch the current param documentation from the diydrones servers (in both Droneshare and Andropilot)
* Lower AHRS data download rate when HUD is not up - to save battery & bandwidth
* Use a higher position download rate - for higher resolution tlogs/map view
* Support the new MAV_CMD_DO_SET_CAM_TRIGG_DIST command
* If the vehicle has never been armed, don't show the home icon until it has been so armed... (prevent user confusion with arducopter)

# 1.9.2
* The new style 3dr radios (shipped with Iris) would not be auto recognized by Andropilot on the first plug attempt (you had to plug them in a second time).  Fixed.
* If you enable GCS scripting (alpha test) in the settings, you will now have a spiffy new user extensible checklist window (Simon's proposed layout - but most code not hooked up)

# 1.9.1
* Cope nicely with radios that are dropping many packets (thanks to Glenn McLelland for in depth debugging on this)
* Promptly send files to droneshare (if so configured)
* Static content is now served for the beginnings of user runnable checkists and vehicle control scripts.  If you turn this feature on and go to http://localhost:4404/static/README.html you should get an interesting response ;-)
* Add support for simulating very high error rate links (crappy rctimer radios ;-))


# 1.9.0
* Fix bug that broke loading waypoints from files (thanks to Antonio Alfaro)
* Include new scripting API (not yet documented - off by default)
* Fix a couple of rare autobugs
* Fix a bug where the new radiation warning could fail on screen rotation of certain tablet types (non fatal - but ugh)

# 1.8.19
* Some non andropilot service crashes frequently without releasing the USB port.  If we find the tablet in that state, we warn the user and suggest a workaround.
* Add support for 'show on map' for big waypoint lists

# 1.8.17
* Warn about periods of high solar activity (thanks to NOAA for the data and ligi@ligi.de for a reference implementation).  The app will warn when first launched, or if you choose the new "Solar activity check" menu item.
* Include sysid for up to three vehicles in the tlog filename

# 1.8.15
* Use param by value to fetch params (I had turned this off a long time ago because old AC builds didn't support this).  Now that those old <2.93 builds are gone I can turn this back on - MUCH FASTER PARAMETER DOWNLOADS - especially for bluetooth!  (Thanks to Stefan and Peter for asking for this)
* Fix problems where new state machine would get confused if you attached to a vehicle that was already
half way through downloading parameters.

# 1.8.14
* Fix bug where parameter updates wouldn't show up until you moved at least two tabs away from the parameters
tab.  This bug was introduced in 1.8.01, much thanks to Brendan O'Reilly for the great 'steps to reproduce'.
* More support for javascript vehicle control (no UI yet)

# 1.8.04
* Make a new UI that is optimized for small phone screens - it should look pretty nice (if cramped once you turn on screen joysticks) - thanks to Remigijus (in Lithuania!)
* Allow the various SimpleUI buttons to scroll if necessary for small screens
* Fix the problem of the service not exiting - introduced in the last release.  Thanks Gary and Luciano for the reports

# 1.8.03
* Fix screen sleep problem reported by Luciano (thanks!)
* The new simple UI is mostly finished - just some drawer stuff & art assets left
* Info fields now float on top left of map view
* Auto hide the sidebar once we are ready to fly

# 1.8.01
* Abort RC override if the user of the RC xmitter changes the flight mode switch (Luciano - would you mind confirming this works for you?)
* First cut of new 'even simpler' UI
* Misc improvements to the RC joysticks (I've been flying with only the tablet lately)
* (Alpha) support for multiple vehicles (though not yet shown in most views) - No configuration needed, just attach multiple 3dr radios to a (POWERED) USB hub
* Beginning of scripting support 
* The sidebar pane is now dockable - just use the menu to turn it on/off

# 1.7.13
* Work around for hangs which could occur when sliding HUD into view (on some 4.3 devices): https://code.google.com/p/android/issues/detail?id=58385

# 1.7.12
* Known bug: using 'gestures' to slide between screens may cause hangs with this version of the app - for this beta
please just use the screen names at the top of each tab to switch...
* Add (basic) support for Pebble watches (go to 'music' inside your watch - this is a temporary home for this proof of concept)

# 1.7.10
* Fix problem introduced where we'd drop the vehicle link occasionally (thanks to beta testers Gary and Luciano)
* Add extra debugging (and change a suspected error to be non-fatal) if the new state machine gets confused
* Don't let vehicles arm if the are not at zero throttle (Thanks to Dave C for reporting this important safety issue)
* Sometimes the AC claims it is flying when it is disarmed, silently deal with this problem...

# 1.7.08
* Quick fix to make things work better on plane (broken in 1.7.07)
* Fix a nasty (very rare) race condition that could cause hangs in the HUD code

# 1.7.07
* SimpleUI: Move status messages into their own fragment (and make them update even if fragment is not visible)
* SimpleUI: Try to guess likely modes the user would want and put them as buttons at the bottom of the screen
* SimpleUI: Show the bluetooth device connect/disconnect button
* SimpleUI: Prompt user with extra dialog if they try especially dangerous mode switches (disconnecting or disarming while we think the vehicle is flying etc...)
* I played around with the profiler a bit and many operations should now be a bit faster
* When connected by bluetooth, show proper vehicle state on the icon
* Make popup modal bar appear in correct position on portrait 7" displays
* Show HDOP (horizontal position precision) in the gps display
* Only enable arm menu item if we have a heartbeat from the vehicle
* Fix the bug where mode changes would be announced twice
* Fix some minor display glitches
* Add support for profiling/tracing when in developer mode
* Fix a couple more rare autobugs
* SimpleUI: Lots more coming in the next release...

# 1.7.04
* Announce Arm/Disarm via voice
* Show estimated radio range as two blue circles (one circle centered on the GCS, one circle centered on the vehicle).
  This is the predicted range that radio will lose signal reception.

# 1.7.03
* If a copter fails to arm, voice announce the reason
* Improve the "Arm" menu item to work 100% correctly for arming/disarming copters
 
# 1.7.00
* Add support for copter style fences (plane was already supported)
* Dramatically speed up parameter download over low quality links
* We now show progress bar as parameters are being downloaded
* Update autodocs for parameters
* Update google play services

# 1.6.08
* Oops - typo, wasn't properly checking min battery voltage from the preferences

# 1.6.07
* Fix problem where screen joystick would not reappear following screen rotation.  Thanks Steve!

# 1.6.06
* Joystick trim position would be set correctly only if you had a real RC radio hooked up - fix to work totally without radio
* Make sure on screen joystick can go all the way to zero throttle
* Fix a couple of new autobugs

# 1.6.05
* Fix a couple rare auto bugs

# 1.6.04
* Oops - Last build accidentally broken Nexus 7 portrait.  Thank Steve S for the fix.
* Increase throttle travel 2x (thanks Steve S)
* Don't warn about no gps until we have comms with vehicle (thanks Thomas N)
* Don't warn about battery % charge if there is no charge data available (thanks Thomas N)

# 1.6.02
* Fix problems where the screen joysticks directions coule be reversed
* Add an on-screen panic button to cancel RC override
* Allow phone sized devices to show joysticks (in landscape orientation)

# 1.6.01
* Add on-screen joysticks (Choose 'show joystick' from menu - when holding tablet in landscape mode)
* Make joysticks appear as a translucent overlay
* Add haptic feedback to let user feel when they cross the zero position on sticks
* Add support for backup service (so prefs will be restored if phone gets replaced)

# 1.4.18
* change ground color on horizon to brown
* refetch waypoints (including Home) whenever vehicle gets rearmed
* Make joystick based mode changes work correctly on rover
* Properly handle MOUNT and DIGICAM configure and control waypoints

# 1.4.17
* add a "Navigate to vehicle" menu item which will use google maps to lead you to the last known vehicle location
* add support for arming via menus (copter/quad only)
* fix channel numbering to make RC channels start with 1 rather than zero
* voice announce arrival at a waypoint
* fix an autobug: java.lang.IllegalStateException: Adapter is detached
* fix an autobug: don't let users use invalid port numbers
* Make flurry optional

# 1.4.15
* Add support for upcoming accelerometer cal APM feature (not yet tested with real hardware)
* Update param docs from master
* Add support for APM Rover
* Add beginnings of support for direct connect to PX4 USB

# 1.4.14
* I broke waypoint editing in 1.4.12 - fixed
* Fix problem of not creating temp directories on some tablets
* Fix a couple of rare autobugs
* Sorry Rana: I still haven't figured out what causes UDP upload from the tablet to not work in mission planner (I'm working on another cool APM based project now, when I get back to Andropilot next week I'll fix...)

# 1.4.12
* Clean up UI a bit (switch to holo-dark theme)
* Perform waypoint upload only based on user selection

# 1.4.10
* fix a couple more autobugs in droneshare
* fix an auto bug - droneshare uploads of 'boring' flights would show incorrect message
* oops - last release broke lead-it/follow-me - fixed
* spoken battery percent was busted (due to localization changes) - fixed
* add support for the following advanced wpts:
    MAV_CMD.MAV_CMD_CONDITION_DISTANCE -> "CondDist",
    MAV_CMD.MAV_CMD_CONDITION_DELAY -> "CondDelay",
    MAV_CMD.MAV_CMD_CONDITION_CHANGE_ALT -> "CondAlt",
    MAV_CMD.MAV_CMD_DO_CHANGE_SPEED -> "ChangeSpd",
    MAV_CMD.MAV_CMD_DO_SET_SERVO -> "SetServo",
    MAV_CMD.MAV_CMD_DO_SET_RELAY -> "SetRelay",
    MAV_CMD.MAV_CMD_DO_REPEAT_SERVO -> "RepeatServo",
    MAV_CMD.MAV_CMD_DO_REPEAT_RELAY -> "RepeatRelay"

# 1.4.09
* Fix a few minor autobugs

# 1.4.06
* Move droneshare out of beta
* Add support for localizing into german or other languages (need to convert a few more strings)
* Register with Android as able to manage our own network bandwidth
* Show follow-me as a checkbox (to make it more obvious on how to cancel)

# 1.4.05
* Apply altitude changes to markers immediately (so if the user clicks on GOTO etc... without clicking Done it will do the right thing)
* Use best possible altitude on hud view
* Add an optimized view layout for 10" tablets in portrait mode (Such as the beautiful Nexus 10" - Thank you anonymous Googler for the gift!)
* Don't say 'underscore' when reading out modes

TODO:
install android tools
Fix: localize resources
Fix: add button to force parameter download
Fix: Rana's bug with UDP uploading
FIXME: report droneshare failures via google
FIXME: make sure cache control is correct for view, static and lists.  https://groups.google.com/forum/?fromgroups=#!topic/scalatra-user/UrwL01iBygY
FIXME: move droneshare client stuff to common
report upload exceptions via google
s3 use reduced redundancy
put size limits on uploads

# 1.4.03
* Fix a couple of autobugs with the new droneshare feature

# 1.4.02
* Automatically resume droneshare uploads when network connection is attached
* Report Rssi as the difference between Rssi and noise (which is really what matters)
* Fix an autobug for phones that don't have the market app
* Fix an autobug accessing waypoints served up from the network

# 1.4.01
* Beta test feature: Optional sharing tlogs with G+, gmail, facebook, google earth & web...
* We automatically delete 'boring' tlogs (no vehicle motion) - controllable from settings
* Use the new android 4.0 style preferences UI

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



