Misc personal to-dos for this project... -kevinh

# Goal

Have a arudplane follow a hangglider and take pictures.

## Method
This is my current thinking, feel free to edit...

* Have a netbook (for ease of development do android later) on the pilot
* The netbook is running the backend from https://github.com/geeksville/mavelous and xmitting over 900MHz to vehicle
* custom scala app talks to local GPS and backend repeatedly setting a new desired target position for the vehicle (be careful to take into account airspeed, don't run into hang glider or cliff etc...)
* Eventually port to android using https://code.google.com/p/android-scripting/ or http://kivy.org/#home
* start by just slaming in new target WPs and loitering, but eventually fork loiter in navigation.pde to be smart about ridge rules, stay just outside of pilot, at the correct speed and altitude
* properly handle return to launch when launch is above most of the flight

## To-do 

### Basic hw To-do

* Follow setup steps here: http://code.google.com/p/ardupilot-mega/wiki/APM2RC
* Setup telemetry
* Setup xplane per http://code.google.com/p/ardupilot-mega/wiki/Xplane
* Configure xmitter mode buttons http://diydrones.com/profiles/blogs/mode-switch-setup-for-turnigy-1 & http://www.diydrones.com/profiles/blogs/another-way-to-set-modes-on-turnigy-9x & http://diydrones.com/profiles/blogs/mode-switch-setup-for-turnigy
* config for plane http://code.google.com/p/ardupilot-mega/wiki/ConfigFiles

### Code to-do

* build/run the hw in loop sim 
* build/run the sw in loop sim
* Test marelous with software & hardware in loop sim
* Develop scala client to talk to marvelous
* Use this as an example on how to develop the follow algorithm https://code.google.com/p/ardupilot-mega/wiki/FlightModesLoiter (manage speed and bearing)

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



