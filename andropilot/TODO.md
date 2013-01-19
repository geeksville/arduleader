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
* mark as foreground ( http://developer.android.com/guide/components/services.html#Foreground )
* in onStart register receiver to find out about device disconnection
* shutdown when device disconnects
* destroy all actors at onDestroy

Usb 
* Handle device disconnection per http://developer.android.com/guide/topics/connectivity/usb/host.html
* Only interested in our device type when scanning devices

InfoView (at top for now - eventually overlay on map)
* Show a spinner to select mode
* Show a text box with status: communicating/offline (get status from service - eventually an icon)

Maps layer
* https://developers.google.com/maps/documentation/android/reference/com/google/android/gms/maps/GoogleMapOptions#useViewLifecycleInFragment(boolean)
* turn on layers
* place marker for plane https://developers.google.com/maps/documentation/android/marker (not draggable)
setInfoWindowAdapter - show alt, battery, mode

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
