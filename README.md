# Goal

Have a arduplane follow a hang-glider and take pictures.

## How to build

To build this project you need sbt (simple build tool).  On ubuntu you can do "apt-get install sbt", for other platforms see http://www.scala-sbt.org/release/docs/Getting-Started/Setup.  Then cd into the root
directory of this project and type "sbt run".  If you would like to use eclipse, then type "sbt eclipse" to 
autogenerate the project files.

Note: This is still a work in progress. Unless you are working on it with me, you probably don't want to bother
playing with it yet. ;-)

## Method
This is my current thinking, feel free to edit...

* Have a netbook + GPS on the pilot. (or possibly a PX4 or APM2.5).  This node is serving as FlightLead (see below).
* A custom scala app talks to local GPS and backend repeatedly setting a new desired target position for the vehicle (be careful to take into account airspeed, don't run into hang glider or cliff etc...)
* (Done) Do the proof-of-concept by just slaming in new target WPs and loitering, but eventually fork loiter in navigation.pde to be smart about ridge rules, stay just outside of pilot, at the correct speed and altitude
* (Done) App talks to plane via 900MHz and in turn forwards packets to UDP port 14550.
* (Done) Split the app into two parts.  One app uploads mavlink flight info
  for the hang-glider (per
  http://www.qgroundcontrol.org/mavlink/start).  Call this app
  FlightLead.  Make a variant of FlightLead that reads IGC files to
  allow testing in simulation.
* (Done) The other app watches a mavlink feed for any particular system ID
  and tries to follow that vehicle by sending set waypoint commands to
  another system id.  Call this app Wingman.
* By splitting things thusly I'd be making a general formation flight
  engine which could be used to follow anything (other arduplanes,
  attempted replay of previous flights, etc...).
* Only start sending waypoints if plane is ready-to-fly and enabled at shell
* figure out why waypoints are not shown on mission-controller

## Messages to send to Aurdupilot

* Rather than repeatedly setting waypoints, could I instead spoof the navcontroller state machine output?
  This might be a better way to do station keeping once we are in formation: MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT :   nav_roll=-8.98  nav_pitch=-12.349999  alt_error=95.32  aspd_error=1131.0656  xtrack_error=-0.088662714  nav_bearing=320  target_bearing=292  wp_dist=127
* Here's what mission planner sends when you choose go-to some point at 100m alt:
MAVLINK_MSG_ID_MISSION_ITEM :   param1=0.0  param2=0.0  param3=0.0  param4=0.0  x=37.52122  y=-122.31037  z=100.0  seq=0  command=16  target_system=1  target_component=1  frame=3  current=2  autocontinue=0
* The device responds with: INFO  c.g.mavlink.LogIncomingMavlink      : Rcv1: MAVLINK_MSG_ID_MISSION_ACK :   target_system=255  target_component=190  type=0
* At startup of mission planner it reads the props...
INFO  c.g.mavlink.LogIncomingMavlink      : Rcv255: MAVLINK_MSG_ID_PARAM_REQUEST_LIST :   target_system=1  target_component=1
INFO  c.g.mavlink.LogIncomingMavlink      : Rcv1: MAVLINK_MSG_ID_PARAM_VALUE :   param_value=13.0  param_count=256  param_index=0  param_id=FORMAT_VERSION  param_type=4

...

## Optionally replace MAVProxy 

(Done) Mavproxy currently assumes sending traffic to only one upstream master device port.  This doesn't work with my code creating a fake flightlead device.
I could extend MAVProxy, but to improve my understanding I'll experiment with talking to the 900MHz link directly from my app.  Rules:

* Forward any incoming serial packets to both our actor framework and direct passthrough to a UDP 41550 (typicaly ground control / mission planner process)
* Any packets that arrive on the local port we used to connect to 41550, send them to our event bus.  Also if sysid == 1 send it out the serial port.
* Any packets we generate locally, always send them to the 41550 port, if the sysid is 1 also send it out the serial port

## Changes to Ardupilot code

I'll try to put most smarts in the scala code.  However, the
primitives in Arudpilot might need to be improved.

* Improve loiter behavior to use speed control as much as possible to
  stay on position
* Allow scala code to set a turn direction to avoid.  If we fall more
  than X meters away from desired position, FIXME (somehow avoid
  running into pilot or ridge)
* Assume that the loiter target will be repeatedly reset by scala, do
  I need to do anything else?
* Properly handle return to launch when launch is above most of the
  flight

## To-do 

### Basic hw To-do

* Follow setup steps here: http://code.google.com/p/ardupilot-mega/wiki/APM2RC
* Setup telemetry
* Setup xplane per http://code.google.com/p/ardupilot-mega/wiki/Xplane
* Configure xmitter mode buttons http://diydrones.com/profiles/blogs/mode-switch-setup-for-turnigy-1 & http://www.diydrones.com/profiles/blogs/another-way-to-set-modes-on-turnigy-9x & http://diydrones.com/profiles/blogs/mode-switch-setup-for-turnigy
* config for plane http://code.google.com/p/ardupilot-mega/wiki/ConfigFiles

### Learning to-do

* build/run the hw in loop sim 
* build/run the sw in loop sim
* Test marelous with software & hardware in loop sim
* Develop scala client to talk to marvelous
* Use this as an example on how to develop the follow algorithm https://code.google.com/p/ardupilot-mega/wiki/FlightModesLoiter (manage speed and bearing)

### Arduleader to-do
Actual development on the primary app for the netbook.

* Read from serial port and tee mavlink packets to a socket (so other tools will work simultaneously)
* Read IGC to generate sim pilot movements
* Based on pilot movements set a new waypoint on plane every 2 secs
* Test on sim
* Add refinement for controlling airspeed as it reaches goal position
* Add ridge rule hack for staying outside and behind pilot
* If we need to loiter make sure to choose turn direction that respects ridge rule (these two improvements should guarantee no ridge collisions)

### Other to-do

* Relearn how to fly RC by hand
* Get IGC or other flight logs from pilots at the coast
* Test guided mode (originally meant for copters) with plane

# Marvelous software notes

* Install per https://github.com/geeksville/mavelous

# Arduplane sw notes

## To install on unbuntu:
* Put arduino SDK in /opt/local/arduino

## to build with cmake (cmake seems bitrotted - just use make instead)
* Use cmake to build per README
cd ArduPlane
mkdir build
cd build 
cmake .. -DAPM_PROGRAMMING_PORT=/dev/ttyACM0
* Type 'make' to build
* Type 'make ArduPlane-APM_HARDWARE_APM2-mega2560-HIL_MODE_DISABLED-upload' to program

## To build with make

* cd ArduPlane
* make configure
* vi ../config.mk
* make ARDUINO=~/Packages/arduino-1.0.3

# To build/run mavproxy (python GCS)

* sudo apt-get install python-matplotlib python-opencv python-serial python-wxgtk
* For docs see http://www.qgroundcontrol.org/mavlink/mavproxy_startpage
* to connect over serial port ./mavproxy.py --master=/dev/ttyACM0
* to dump log files use ../mavlink/pymavlink/examples/mavlogdump.py mav.tlog

# To build/run software in the loop sim (SITL)

* Build/run git@github.com:geeksville/jsbsim.git

# (OBSOLETE) What my app needs to do

* Run on android, using host mode ACM serial directly to the telmetry interface
* Change telemetry to use this debug API: http://code.google.com/p/ardupilot-mega/wiki/DebugTerminal
* Have android app set new waypoints dynamically...

# Hardware stuff

## To order

* a backup plane
* By combining the following: http://www.hobbyking.com/hobbyking/store/__24788__FrSky_D4R_II_4ch_2_4Ghz_ACCST_Receiver_w_telemetry_.html or http://www.hobbyking.com/hobbyking/store/__19968__FrSky_D8R_XP_2_4Ghz_Receiver_w_telemetry_.html
* with http://www.hobbyking.com/hobbyking/store/__14348__FrSky_FF_1_2_4Ghz_Combo_Pack_for_Futaba_w_Module_RX.html it should be possible to build a PPM based receiver link: https://code.google.com/p/arducopter/wiki/PPMsumRC & http://diydrones.ning.com/profiles/blogs/why-frsky-cppm-signal-is-so-disappointing
* Per blog post upgrade per http://www.frsky-rc.com/download.asp?id=23



