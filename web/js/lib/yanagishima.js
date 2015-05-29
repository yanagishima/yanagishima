var yanagishima_tree = (function() {
  var tree = $("#tree").dynatree({
    imagePath: "img",
    initAjax: {
      url: "presto?query=show+catalogs"
    },
    postProcess: function (data, dataType) {
      headers = data["headers"];
      results = data["results"];
      if(headers == "Catalog") {
        for(var i=0; i<results.length; i++) {
          var catalog = results[i][0];
          var rootNode = $("#tree").dynatree("getRoot");
          rootNode.addChild({ title: catalog,  key: catalog, isFolder: true, isLazy: true, catalog: catalog});
        }
      }
    },
    onLazyRead: function(node){
      var param;
      if(node.data.catalog) {
        param = "show schemas from " + node.data.key;
      } else if(node.parent.data.catalog) {
        param = "show tables from " + node.parent.data.catalog + "." + node.data.key;
      } else if(node.parent.data.schema) {
        param = "show partitions from " + node.parent.parent.data.catalog + "." + node.parent.data.schema  + "." + node.data.key;
      }
      $.ajax({
            url: "presto",
            data: { query: param},
            type: "GET",
            dataType: "json"
        }).done(function(data) {
                if(data["error"]) {
                  console.log(data["error"]);
                  return;
                }
                headers = data["headers"];
                results = data["results"];
                if(headers == "Schema") {
                  for(var i=0; i<results.length; i++) {
                    var result = results[i][0];
                    node.addChild({title: result, key: result, isLazy: true, isFolder: true, schema: result});
                  }
                } else if(headers == "Table") {
                  for(var i=0; i<results.length; i++) {
                    var result = results[i][0];
                    node.addChild({title: result, key: result, isLazy: true, isFolder: true, table: result});
                  }
                } else {//show partitions
                    var partition_header_result_array = [];
                    for(var i=0; i<results.length; i++) {
                      var header_result_array = [];
                      for(var j=0; j<results[i].length; j++) {
                        var result_data = results[i][j];
                        if(typeof(result_data) == "string") {
                          result_data = "'" + result_data + "'";
                        }
                        header_result_array.push(headers[j] + "=" + result_data);
                      }
                      partition_header_result_array.push(header_result_array.join("/"));
                    }
  
                    var partition_nodes = [];
                    var treenodes = {};
  
                    var create_node = function(partition, hasChildren){
                      if (treenodes[partition])
                        return treenodes[partition];
                      var parts = partition.split('/');
                      var leafName = parts.pop();
                      var node = {title: leafName, key: leafName, isLazy: true, isFolder: true, partition: leafName};
                      if (hasChildren) {
                        node.children = [];
                      }
                      if (parts.length > 0) {
                        var parent = create_node(parts.join('/'), true);
                        parent.children.push(node);
                      }
                      else {
                        partition_nodes.push(node);
                      }
                      treenodes[partition] = node;
                      return node;
                    };
  
                    partition_header_result_array.forEach(function(partition){
                      create_node(partition);
                    });
                    partition_nodes.sort(
                      function(a, b) {
                        if(a.title < b.title) return -11;
                        if(a.title > b.title) return 1;
                        return 0;
                      }
                    );
                    node.addChild(partition_nodes);
  
                }
            node.setLazyNodeStatus(DTNodeStatus_Ok);
        }).fail(function() {
            node.data.isLazy = false;
            node.setLazyNodeStatus(DTNodeStatus_Ok);
            node.render();
        });
    },
    onCreate: function(node, span){
      if(node.data.table) {
        $(span).contextMenu({menu: "tableMenu"}, function(action, el, pos) {
          table = node.data.table;
          schema = node.parent.data.schema;
          catalog = node.parent.parent.data.catalog;
          if(action === "select") {
            query = "SELECT * FROM " + catalog + "." + schema + "." + table + " LIMIT 100";
            $("#query").val(query);
            $("#query-submit").click();
          } else if(action === "columns") {
            query = "SHOW COLUMNS FROM " + catalog + "." + schema + "." + table;
            var requestURL = "/presto";
            var requestData = {
              "query": query
            };
            var successHandler = function(data) {
              if (data.error) {
                $("#error-msg").text(data.error);
                $("#error-msg").slideDown("fast");
                $("#show-columns").empty();
                $("#tableName").empty();
              } else {
                $("#show-columns").empty();
                $("#tableName").text(catalog + "." + schema + "." + table);
                var headers = data.headers;
                var rows = data.results;
                create_table("#show-columns", headers, rows);
              }
            };
            $.get(requestURL, requestData, successHandler, "json");
          }
        });
      }
      if(node.data.partition) {
        $(span).contextMenu({menu: "partitionMenu"}, function(action, el, pos) {
          var parent_partition_tree = [];
          partition = node.data.partition;
          parent_partition_tree.push(partition);
          parent_node = node.parent;
          table = parent_node.data.table;
          while(!table) {
            if(parent_node.data.partition) {
              parent_partition_tree.push(parent_node.data.partition);
            }
            parent_node = parent_node.parent
            table = parent_node.data.table;
          }
          schema = parent_node.parent.data.schema;
          catalog = parent_node.parent.parent.data.catalog;
          query = "SELECT * FROM " + catalog + "." + schema + "." + table + " WHERE " + parent_partition_tree.join(" and ") + " LIMIT 1000";
          $("#query").val(query);
          $("#query-submit").click();
        });
      }
    }
  });
  return tree;
});

var handleExecute = (function() {
  $("#query-submit").attr("disabled", "disabled");
  $("#query-results").empty();
  $("#error-msg").hide();
  $("#warn-msg").hide();
  var tr = document.createElement("tr");
  var td = document.createElement("td");
  var img = document.createElement("img");
  $(img).attr("src", "img/loading_long_48.gif");
  $(td).append(img);
  $(tr).append(td);
  $("#query-results").append(tr);
  var query = $("#query").val();
  push_query(query);
  $("#query-histories").empty();
  update_query_histories_area();
  var requestURL = "/presto";
  var requestData = {
    "query": query
  };
  var successHandler = function(data) {
    $("#query-submit").removeAttr("disabled");
    if (data.error) {
      $("#error-msg").text(data.error);
      $("#error-msg").slideDown("fast");
      $("#query-results").empty();
    } else {
      if (data.warn) {
        $("#warn-msg").text(data.warn);
        $("#warn-msg").slideDown("fast");
      }
      $("#query-results").empty();
      var headers = data.headers;
      var rows = data.results;
      create_table("#query-results", headers, rows);
    }
  };
  $.get(requestURL, requestData, successHandler, "json");
});

var push_query = (function(query) {
  if (! window.localStorage) return;
  var list = query_histories();
  list.unshift(query);
  set_query_histories(list);
});

var query_histories = (function() {
  if (! window.localStorage) return [];
  var list = [];
  try {
    var listString = window.localStorage.query_histories;
    if (listString && listString.length > 0)
      list = JSON.parse(listString);
  } catch (e) { set_query_histories([]); list = []; }
  return list;
});

var set_query_histories = (function(list) {
  if (! window.localStorage) return;
  window.localStorage.query_histories = JSON.stringify(list);
});

var delete_query_histories = (function() {
  $("#query-histories").empty();
  if (! window.localStorage) return;
  window.localStorage.removeItem("query_histories");
});

var update_query_histories_area = (function() {
  var tbody = document.createElement("tbody");
  var query_list = query_histories();
  for(var i=0; i<query_list.length; i++) {
    var tr = document.createElement("tr");
    var td = document.createElement("td");
    $(td).text(query_list[i]);
    $(tr).append(td);
    var a = document.createElement("a");
    $(a).text("copy to query area");
    $(a).attr("href", "#");
    $(a).bind("click", {query: query_list[i]}, copy_query);
    var td = document.createElement("td");
    $(td).append(a);
    $(tr).append(td);
    var a = document.createElement("a");
    $(a).text("delete");
    $(a).attr("href", "#");
    $(a).bind("click", {index: i}, delete_query);
    var td = document.createElement("td");
    $(td).append(a);
    $(tr).append(td);
    var a = document.createElement("a");
    $(a).text("bookmark");
    $(a).attr("href", "#");
    $(a).bind("click", {query: query_list[i]}, add_bookmark);
    var td = document.createElement("td");
    $(td).append(a);
    $(tr).append(td);
    $(tbody).append(tr);
  }
  $("#query-histories").append(tbody);
});

var update_query_bookmarks_area = (function() {
  var tbody = document.createElement("tbody");
  var query_list = query_bookmarks();
  for(var i=0; i<query_list.length; i++) {
    var tr = document.createElement("tr");
    var td = document.createElement("td");
    $(td).text(query_list[i]);
    $(tr).append(td);
    var a = document.createElement("a");
    $(a).text("copy to query area");
    $(a).attr("href", "#");
    $(a).bind("click", {query: query_list[i]}, copy_query);
    var td = document.createElement("td");
    $(td).append(a);
    $(tr).append(td);
    var a = document.createElement("a");
    $(a).text("delete");
    $(a).attr("href", "#");
    $(a).bind("click", {index: i}, delete_bookmark);
    var td = document.createElement("td");
    $(td).append(a);
    $(tr).append(td);
    $(tbody).append(tr);
  }
  $("#query-bookmarks").append(tbody);
});

var add_bookmark = (function(event) {
  if (! window.localStorage) return;
  var list = query_bookmarks();
  list.unshift(event.data.query);
  set_query_bookmarks(list);
  $("#query-bookmarks").empty();
  update_query_bookmarks_area();
});

var query_bookmarks = (function() {
  if (! window.localStorage) return [];
  var list = [];
  try {
    var listString = window.localStorage.query_bookmarks;
    if (listString && listString.length > 0)
      list = JSON.parse(listString);
  } catch (e) { set_query_bookmarks([]); list = []; }
  return list;
});

var set_query_bookmarks = (function(list) {
  if (! window.localStorage) return;
  window.localStorage.query_bookmarks = JSON.stringify(list);
});

var delete_bookmark = (function(event) {
  if (! window.localStorage) return;
  var query_list = query_bookmarks();
  query_list.splice(event.data.index, 1);
  set_query_bookmarks(query_list);
  $("#query-bookmarks").empty();
  update_query_bookmarks_area();
});

var delete_query_bookmarks = (function() {
  $("#query-bookmarks").empty();
  if (! window.localStorage) return;
  window.localStorage.removeItem("query_bookmarks");
});

var copy_query = (function(event) {
  $("#query").val(event.data.query);
});

var delete_query = (function(event) {
  if (! window.localStorage) return;
  var query_list = query_histories();
  query_list.splice(event.data.index, 1);
  set_query_histories(query_list);
  $("#query-histories").empty();
  update_query_histories_area();
});

var create_table = (function(table_id, headers, rows) {
  var thead = document.createElement("thead");
  var tr = document.createElement("tr");
  for (var i = 0; i < headers.length; ++i) {
    var th = document.createElement("th");
    $(th).text(headers[i]);
    $(tr).append(th);
  }
  $(thead).append(tr);
  $(table_id).append(thead);
  var tbody = document.createElement("tbody");
  for (var i = 0; i < rows.length; ++i) {
    var tr = document.createElement("tr");
    var columns = rows[i];
    for (var j = 0; j < columns.length; ++j) {
      var td = document.createElement("td");
      $(td).text(columns[j]);
      $(tr).append(td);
    }
    $(tbody).append(tr);
  }
  $(table_id).append(tbody);
  $(table_id).tablesorter();
});

var redraw = (function() {
    d3.json('/query', function (queries)
    {
        var runningQueries = [];
        var doneQueries = [];
        if (queries) {
            runningQueries = queries.filter(function (query)
            {
                return query.state != 'FINISHED' && query.state != 'FAILED' && query.state != 'CANCELED';
            });

            doneQueries = queries.filter(function (query)
            {
                return query.state == 'FINISHED' || query.state == 'FAILED' || query.state == 'CANCELED';
            });
        }

        renderRunningQueries(runningQueries);
        renderDoneQueries(doneQueries);
    });
});

var renderRunningQueries = (function(queries) {
    var tbody = d3.select("#running").select("tbody");

    var rows = tbody.selectAll("tr")
            .data(queries, function (query) { return query.queryId; })

    rows.exit()
            .remove();

    rows.enter()
            .append("tr")
            .attr("class", "info")
            .append('button')
            .text('Kill')
            .attr('type', 'button').on('click', function(query) {
                d3.xhr("/kill?queryId=" + query.queryId).send('GET');
                $(this).attr('disabled', 'disabled');
            });

    var cells = rows.selectAll("td")
            .data(function (queryInfo)
                  {
                      var splits = queryInfo.totalDrivers;
                      var completedSplits = queryInfo.completedDrivers;

                      var runningSplits = queryInfo.runningDrivers;
                      var queuedSplits = queryInfo.queuedDrivers;

                      var query = queryInfo.query;
                      if (query.length > 200) {
                          query = query.substring(0, 200) + "...";
                      }

                      var progress = "N/A";
                      if (queryInfo.scheduled) {
                          progress = d3.format("%")(splits == 0 ? 0 : completedSplits / splits);
                      }

                      return [
                          queryInfo.queryId,
                          queryInfo.elapsedTime,
                          query,
                          queryInfo.session.source,
                          queryInfo.session.user,
                          queryInfo.state,
                          progress,
                          queuedSplits,
                          runningSplits,
                          completedSplits
                      ]
                  });

    cells.text(function (d) { return d; });

    cells.enter()
            .append("td")
            .text(function (d) { return d; });

    tbody.selectAll("tr")
            .sort(function (a, b) { return d3.descending(a.createTime, b.createTime); });
});

var renderDoneQueries = (function(queries) {
    var tbody = d3.select("#done").select("tbody");

    var rows = tbody.selectAll("tr")
            .data(queries, function (query) { return query.queryId; });

    rows.enter()
            .append("tr")
            .attr("class", function (query) {
                          switch (query.state) {
                          case "FINISHED":
                              return "success";
                          case "FAILED":
                              return "danger";
                          case "CANCELED":
                              return "warning";
                          default:
                              return "info";
                      }
                  });

    rows.exit()
            .remove();

    rows.selectAll("td")
            .data(function (queryInfo) {
                      var splits = queryInfo.totalDrivers;
                      var completedSplits = queryInfo.completedDrivers;

                      var query = queryInfo.query;
                      if (query.length > 200) {
                          query = query.substring(0, 200) + "...";
                      }

                      return [
                          queryInfo.queryId,
                          queryInfo.elapsedTime,
                          query,
                          queryInfo.session.source,
                          queryInfo.session.user,
                          queryInfo.state,
                          shortErrorType(queryInfo.errorType),
                          completedSplits,
                          splits,
                          d3.format("%")(splits == 0 ? 0 : completedSplits / splits)
                      ]
                  })
            .enter()
            .append("td")
            .text(function (d)
                  {
                      return d;
                  });

    tbody.selectAll("tr")
            .sort(function (a, b) { return d3.descending(a.endTime, b.endTime); });
});

var shortErrorType = (function(errorType) {
    switch (errorType) {
        case "USER_ERROR":
            return "USER";
        case "INTERNAL_ERROR":
            return "INTERNAL";
        case "INSUFFICIENT_RESOURCES":
            return "RESOURCES";
    }
    return errorType;
});
