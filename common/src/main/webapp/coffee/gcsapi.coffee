
# Save a reference to the global object (`window` in the browser, `exports`
# on the server).
root = this

# Put GCSAPI into root namespace
GCSAPI = root.GCSAPI = {}

###
This is the official 'public' GCSAPI interface for access to vehicle model.
It is accessible through the GCSAPI.vehicle global.  (Eventually multiple
vehicles will be exposed, at that point the 'vehicle' global will point
to the vehicle the user has designated as the selected vehicle in the GUI)
###
Vehicle = Backbone.Model.extend(
  urlRoot: 'http://localhost:4404/api/vehicle'
  initialize: () ->
    # Pull down initial state
    this.fetch()

    # Update state from server every 5 secs (yucky - FIXME)
    window.setInterval((() => this.fetch()), 5 * 1000);

  # API methods
  location: () -> this.get('location')
  waypoints: () -> this.get('waypoints')
  has_heartbeat: () -> this.get('has_heartbeat')
  gps_hdop: () -> this.get('gps_hdop')
  rc_channels: () -> this.get('rc_channels')
  rc_connected: () -> this.get('rc_connected')
  status_messages: () -> this.get('status_messages')
  current_mode: () -> this.get('current_mode')

  has_3d_fix: () -> 
    loc = this.location()
    if loc then !!loc[2] else false
)

GCSAPI.View = {}

###
A simple view that redraws using a getContent callback whenever the vehicle
changes
###
GCSAPI.View.VehicleBased = Backbone.View.extend(

  initialize: (options) ->
    _.bindAll(this, 'render') # list all methods that use 'this' here

    this.model = GCSAPI.vehicle
    this.model.bind('change:' + options.watching, this.render)
    this.getContent = options.getContent
    this.render() # Show initial state

  # Draw our view
  render: () ->
    $(this.el).empty()
    $(this.el).append(this.getContent())
)

GCSAPI.vehicle = new Vehicle()


