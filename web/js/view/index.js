var indexView;
IndexView = Backbone.View.extend({
  events: {
    "click #query-submit": "handleExecute",
    "keypress input": "handleKeyPress"
  },

  initialize: function(settings) {
    $("#error-msg").hide();
  },

  handleExecute: function(evt) {
    $("#query-submit").attr("disabled", "disabled");
    $("#query-results").empty();
    var tr = document.createElement("tr");
    var td = document.createElement("td");
    var img = document.createElement("img");
    $(img).attr("src", "img/loading_long_48.gif");
    $(td).append(img);
    $(tr).append(td);
    $("#query-results").append(tr);
    var query = $("#query").val();
    var requestURL = contextURL + "/presto";
     var requestData = {
        "query": query
      };
      var successHandler = function(data) {
        $("#query-submit").removeAttr("disabled");
        console.log(data);
        if (data.error) {
          $("#error-msg").text(data.error);
          $("#error-msg").slideDown("fast");
          $("#query-results").empty();
        } else {
          $("#error-msg").hide();
          $("#query-results").empty();
          var headers = data.headers;
          var tr = document.createElement("tr");
          for (var i = 0; i < headers.length; ++i) {
            var th = document.createElement("th");
            $(th).text(headers[i]);
            $(tr).append(th);
          }
          $("#query-results").append(tr);
          var rows = data.results;
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
  indexView = new IndexView({el: $("#query-form")});
});