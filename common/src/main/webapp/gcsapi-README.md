# GCSAPI

The GCSAPI is specified in GCSAPI.coffee (and implicitly GCSAPI.js).  However
we'll eventually grow this into a user manual on how to script APM ground
control stations.

## Checklists

Currently the main usage of GCSAPI is for checklists.  If you would like to
customize the checklists (and preferably contribute your improvements) you
probably want to edit plane.html or copter.html.  Those files use some shared
javascript that lives in checklist.js.

## Examples

Example javascript code that uses the API:

### Getting vehicle location:
```js
var loc = GCSAPI.vehicle.location()
```

### Plopping down a view that shows red or green based on vehicle mode (from checklist.coffee/js):
```js
new GCSAPI.View.VehicleBased(
  el: '#manual-mode'
  watching: 'manual_mode'
  getContent: () ->
    mode = this.model.current_mode()
    msg = if (mode == "MANUAL") || (mode == "STABILIZE")
      alertSuccess('Vehicle in MANUAL mode')
    else
      alertFail('Vehicle not in MANUAL mode')
)
```