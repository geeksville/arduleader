# General
* treat warnings as errors (in scala)
* turn on proguard warnings
* Use gittip?  <iframe style="border: 0; margin: 0; padding: 0;"
        src="https://www.gittip.com/geeksville/widget.html"
        width="48pt" height="22pt"></iframe>

# android:
* create wiki/site
* FIXME - sometimes we find two extra bytes - I bet those are the headers for a packet (seems ignorable for now)
* restore map after classloader fixed

Posixpilot

* make sure serial link still work
* make sure it exits cleanly
* (eventually) read and display waypoints, launch position, set launch position
* (eventually) log records to file 

Download waypoints from target
* MISSION_REQUEST_LIST {target_system : 1, target_component : 1}
* MISSION_COUNT {target_system : 255, target_component : 190, count : 1}
* MISSION_REQUEST {target_system : 1, target_component : 1, seq : 0}
* MISSION_ITEM {target_system : 255, target_component : 190, seq : 0, frame : 0, command : 16, current : 1, autocontinue : 1, param1 : 0.0, param2 : 0.0, param3 : 0.0, param4 : 0.0, x : 37.5209159851, y : -122.309059143, z : 143.479995728}

Service
* (test) mark as foreground 
* in onStart register receiver to find out about device disconnection
* shutdown when device disconnects
* (test) destroy all actors at onDestroy

Usb 
* (test) Handle device disconnection per http://developer.android.com/guide/topics/connectivity/usb/host.html

InfoView (at top for now - eventually overlay on map)
* Show a spinner to select mode
* Show a text box with status: communicating/offline (get status from service - eventually an icon)

Maps layer
* Use a custom adapter so the snippet can have multiple lines of data (https://github.com/commonsguy/cw-omnibus/tree/master/MapsV2/Popups/src/com/commonsware/android/mapsv2/popups for example)
* Fix model recreation when we rotate screen
* turn on layers
* Plane icon should rotate to match path

* (eventually) place marker for launch position (make draggable)
  onDrag show a toast and reset home position

* (eventually) place marker for each waypoint (draggable)
  onDrag reset waypoint
  Use https://developers.google.com/maps/documentation/android/lines between each waypoint

* (Eventually) show view from plane https://developers.google.com/maps/documentation/android/views

Info layer

* A two line list view ( http://developer.android.com/design/building-blocks/lists.html )

* Use just an action bar with swiping to change panels 
* Two panels
  * map
  * info (raw packets)
* show current vehicle mode as a spinner in the action bar (using ActionProvider)

http://developer.android.com/guide/topics/ui/actionbar.html
