var indexView;
IndexView = Backbone.View.extend({
  events: {
    "click #query-submit": "handleExecute",
    'keypress input': 'handleKeyPress'
  },

  initialize: function(settings) {
    $('#msg').hide();
  },

  handleExecute: function(evt) {
    var query = $("#query").val();
    var requestURL = contextURL + "/presto";
     var requestData = {
        "query": query
      };
      var successHandler = function(data) {
        console.log(data);
        var results = data.results
        for (var i = 0; i < results.length; ++i) {
          var tr = document.createElement("tr");
          var td = document.createElement("td");
          $(td).text(results[i]);
          $(tr).append(td);
          $("#query-results").append(tr);
        }
      };
      $.get(requestURL, requestData, successHandler, "json");

  },

  handleKeyPress: function(evt) {
    if (evt.charCode == 13 || evt.keyCode == 13) {
      this.handleExecute();
    }
  },

  render: function() {
  }
});

$(function() {
  indexView = new IndexView({el: $('#query-form')});
});