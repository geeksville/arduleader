	
VehicleView = Backbone.View.extend(
  el: $('body') # attaches `this.el` to an existing element.

  # This is where backbone binds DOM events to methods
  events: 
    'click button#refresh': 'refresh'

  initialize: () ->
    _.bindAll(this, 'render', 'refresh') # list all methods that use 'this' here

    this.model = GCSAPI.vehicle
    this.model.bind('change', this.render)
    this.render() # Render initial state
    
  # Draw our view
  render: () ->
    $(this.el).empty()
    $(this.el).append("<span>Location: "+this.model.location()+"</span>")

    #$(this.el).append("<button id='refresh'>Refresh</button>");
    #$(this.el).append("<ul></ul>");

  refresh: () ->
    this.model.sync()
)

# Instantiate main app view.
vehicleView = new VehicleView()

