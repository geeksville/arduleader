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
