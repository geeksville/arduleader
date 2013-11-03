// **This example illustrates the declaration and instantiation of a minimalist View.**
//
// _Working example: [1.html](../1.html)._
// _[Go to Example 2](2.html)_

// Self-executing wrapper
(function($){
	
	// From http://backbonetutorials.com/what-is-a-model/
	var Vehicle = Backbone.Model.extend({
		  urlRoot: 'http://localhost:4404/api/vehicle',
		  /*
		  initialize: function() {
		    this.set({'title': 'a default title'});
		  },
		  give: function(user) {
		    // gives book to a user
		  },
		  sell: function(user) {
		    // sell a book to a user
		  },
		  clear: function() {
		    this.destroy();     // #destroy is provided by Backbone.js
		    this.view.remove(); // will explain in a few slides
		  }
		  */
		});
	
	/*
	var poller = (function worker() {
  $.ajax({
    url: 'ajax/test.html', 
    success: function(data) {
      $('.result').html(data);
    },
    complete: function() {
      // Schedule the next request when the current one's complete
      setTimeout(worker, 5000);
    }
  });
});
	*/
	
  var VehicleView = Backbone.View.extend({
    el: $('body'), // attaches `this.el` to an existing element.

    // This is where backbone binds DOM events to methods
    events: {
      'click button#refresh': 'refresh'
    },

    // `initialize()`: Automatically called upon instantiation. Where you make all types of bindings, _excluding_ UI events, such as clicks, etc.
    initialize: function(){
      _.bindAll(this, 'render', 'refresh'); // list all methods that use 'this' here

       this.vehicle = new Vehicle();

       this.render(); // not all views are self-rendering. This one is.
    },
    
    // `render()`: Function in charge of rendering the entire view in `this.el`. Needs to be manually called by the user.
    render: function(){
      $(this.el).append("<ul> <li>hello world</li> </ul>");

      $(this.el).append("<button id='refresh'>Refresh</button>");
      $(this.el).append("<ul></ul>");
    },

    refresh: function(){
      $('ul', this.el).append("<li>hello world "+ 4 +"</li>");
    }
  });

  // **listView instance**: Instantiate main app view.
  var vehicleView = new VehicleView();
})(jQuery);

// <div style="float:left; margin-bottom:40px;"><img style="width:42px; margin-right:10px;" src="https://twitter.com/images/resources/twitter-bird-light-bgs.png"/></div> <div style="background:rgb(245,245,255); padding:10px;">Follow me on Twitter: <a target="_blank" href="http://twitter.com/r2r">@r2r</a> </div>
// <script>
//   if (window.location.href.search(/\?x/) < 0) {
//     var _gaq = _gaq || [];
//     _gaq.push(['_setAccount', 'UA-924459-7']);
//     _gaq.push(['_trackPageview']);
//     (function() {
//       var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
//       ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
//       var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
//     })();
//   }
// </script>
