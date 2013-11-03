
goodMessage = (str) -> '&#x2713; ' + str
failMessage = (str) -> '&#x2717; ' + str
alertSuccess = (str) -> "<div class='alert alert-success'>" + goodMessage(str) + "</div>"
alertFail = (str) -> "<div class='alert alert-danger'>" + failMessage(str) + "</div>"

# Hook up our JS widgets
new GCSAPI.View.VehicleBased(
  el: '#telem-connected'
  watching: 'has_heartbeat'
  getContent: () ->
    msg = if this.model.has_heartbeat() 
      alertSuccess('Telemetry connected')
    else
      alertFail('Vehicle not connected')
)
new GCSAPI.View.VehicleBased(
  el: '#rc-connected'
  watching: 'rc_connected'
  getContent: () ->
    msg = if this.model.rc_connected() 
      alertSuccess('RC Transmitter is on')
    else
      alertFail('RC Transmitter offline')
)
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
new GCSAPI.View.VehicleBased(
  el: '#has-3d-fix'
  watching: 'location'
  getContent: () ->
    msg = if this.model.has_3d_fix()
      alertSuccess('GPS has 3D fix')
    else
      alertFail('GPS does not have 3D fix')
)



