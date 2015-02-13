var indexView;
IndexView = Backbone.View.extend({
  events: {
    "click #query-submit": "handleExecute",
    'keypress input': 'handleKeyPress'
  },

  initialize: function(settings) {
    $('#error-msg').hide();
  },

  handleExecute: function(evt) {
    var query = $("#query").val();
    var requestURL = contextURL + "/presto";
     var requestData = {
        "query": query
      };
      var successHandler = function(data) {
        console.log(data);
        if (data.error) {
          $('#error-msg').text(data.error);
          $('#error-msg').slideDown('fast');
        } else {
          $('#error-msg').hide();
          var rows = data.results;
          $("#query-results").empty();
          for (var i = 0; i < rows.length; ++i) {
            var tr = document.createElement("tr");
            var columns = rows[i];
            for (var j = 0; j < columns.length; ++j) {
              var td = document.createElement("td");
              $(td).text(columns[j]);
              $(tr).append(td);
            }
            $("#query-results").append(tr);
          }
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