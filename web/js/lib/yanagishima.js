var yanagishima_tree = (function() {
  var tree = $("#tree").dynatree({
    imagePath: "img",
    initAjax: {
      type: "POST",
      url: "presto",
      data: {"query":"show catalogs"}
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
        param = "show columns from " + node.parent.parent.data.catalog + "." + node.parent.data.schema  + "." + node.data.key;
      }
      $.ajax({
            url: "presto",
            data: { query: param},
            type: "POST",
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
                } else {
                  for(var i=0; i<results.length; i++) {
                    var result = results[i][0];
                    node.addChild({title: result, key: result, isLazy: true, isFolder: false});
                  }
                  $("#show-columns").empty();
                  create_table("#show-columns", headers, results);
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
          } else if(action === "select_no_execute") {
            query = "SELECT * FROM " + catalog + "." + schema + "." + table + " LIMIT 100";
            $("#query").val(query);
          } else if(action === "select_where") {
            select_data("SELECT * FROM", catalog, schema, table, true);
          } else if(action === "select_where_no_execute") {
            select_data("SELECT * FROM", catalog, schema, table, false);
          } else if(action === "select_count_where") {
            select_data("SELECT COUNT(*) FROM", catalog, schema, table, true);
          } else if(action === "select_count_where_no_execute") {
            select_data("SELECT COUNT(*) FROM", catalog, schema, table, false);
          } else if(action === "partitions") {
            query = "SHOW PARTITIONS FROM " + catalog + "." + schema + "." + table;
            $("#query").val(query);
            $("#query-submit").click();
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

var select_data = (function(select_query, catalog, schema, table, execute_flag){
  partition_query = "SHOW PARTITIONS FROM " + catalog + "." + schema + "." + table;
  var requestURL = "/presto";
  var requestData = {
    "query": partition_query
  };
  var successHandler = function(data) {
    if (data.error) {
      $("#error-msg").text(data.error);
      $("#error-msg").slideDown("fast");
    } else {
      var partition_column = data.headers;
      if(partition_column.length == 0) {
        query = select_query + " " + catalog + "." + schema + "." + table + " LIMIT 100";
        $("#query").val(query);
        $("#query-submit").click();
        return;
      }
      var rows = data.results;
      var latest_partition = rows[rows.length-1];
      var where = " WHERE ";
      for (var i = 0; i < partition_column.length; ++i) {
        if(typeof latest_partition[i] === "string") {
          where += partition_column[i] + "=" + "'" + latest_partition[i] + "'";
        } else {
          where += partition_column[i] + "=" + latest_partition[i];
        }
        if (i != partition_column.length - 1) {
          where += " AND "
        }
      }
      query = select_query + " " + catalog + "." + schema + "." + table + where + " LIMIT 100";
      $("#query").val(query);
      if(execute_flag) {
        $("#query-submit").click();
      }
    }
  };
  $.post(requestURL, requestData, successHandler, "json");
});

var selectLine = (function(n) {
    if (n<1) return false;
    $(".codelines .lineno.lineselect").removeClass("lineselect");
    $(".codelines .lineno").eq(n-1).addClass("lineselect");
});

var handle_execute = (function() {
  $("#query-submit").attr("disabled", "disabled");
  $("#query-explain").attr("disabled", "disabled");
  $("#query-explain-distributed").attr("disabled", "disabled");
  $("#query-clear").attr("disabled", "disabled");
  $("#query-format").attr("disabled", "disabled");
  $("#tsv-download").attr("disabled", "disabled");
  $("#query-results").fixedHeaderTable("destroy");
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
  var requestURL = "/presto";
  var requestData = {
    "query": query
  };
  var successHandler = function(data) {
    $("#query-submit").removeAttr("disabled");
    $("#query-explain").removeAttr("disabled");
    $("#query-explain-distributed").removeAttr("disabled");
    $("#query-clear").removeAttr("disabled");
    $("#query-format").removeAttr("disabled");
    if (data.error) {
      $("#error-msg").text(data.error);
      $("#error-msg").slideDown("fast");
      $("#query-results").empty();
      selectLine(data.errorLineNumber);
    } else {
      if (data.warn) {
        $("#warn-msg").text(data.warn);
        $("#warn-msg").slideDown("fast");
      }
      $(".codelines .lineno.lineselect").removeClass("lineselect");
      push_query(query);
      $("#query-histories").empty();
      update_query_histories_area();
      $("#query-results").empty();
      var headers = data.headers;
      var rows = data.results;
      create_table("#query-results", headers, rows);
      $("#tsv-download").removeAttr("disabled");
      push_result(headers, rows);
    }
  };
  $.post(requestURL, requestData, successHandler, "json");
});

var handle_explain = (function() {
  explain(false);
});

var handle_explain_distributed = (function() {
  explain(true);
});

var explain = (function(distributed) {
  $("#query-results").fixedHeaderTable("destroy");
  $("#query-results").empty();
  $("#error-msg").hide();
  $("#warn-msg").hide();
  var query;
  if(distributed) {
    query = "explain (type distributed) " + $("#query").val();
  } else {
    query = "explain " + $("#query").val();
  }
  var requestURL = "/presto";
  var requestData = {
    "query": query
  };
  var successHandler = function(data) {
    if (data.error) {
      $("#error-msg").text(data.error);
      $("#error-msg").slideDown("fast");
      $("#query-results").empty();
      selectLine(data.errorLineNumber);
    } else {
      if (data.warn) {
        $("#warn-msg").text(data.warn);
        $("#warn-msg").slideDown("fast");
      }
      $(".codelines .lineno.lineselect").removeClass("lineselect");
      $("#query-results").empty();
      var headers = data.headers;
      var rows = data.results;
      var thead = document.createElement("thead");
      var tr = document.createElement("tr");
      for (var i = 0; i < headers.length; ++i) {
        var th = document.createElement("th");
        $(th).text(headers[i]);
        $(tr).append(th);
      }
      $(thead).append(tr);
      $("#query-results").append(thead);
      var tbody = document.createElement("tbody");
      for (var i = 0; i < rows.length; ++i) {
        var tr = document.createElement("tr");
        var columns = rows[i];
        for (var j = 0; j < columns.length; ++j) {
          var pre = document.createElement("pre");
          $(pre).text(columns[j]);
          var td = document.createElement("td");
          $(td).append(pre);
          $(tr).append(td);
        }
        $(tbody).append(tr);
      }
      $("#query-results").append(tbody);
      $("#query-results").fixedHeaderTable("destroy");
      $("#query-results").fixedHeaderTable();
      $("#tsv-download").removeAttr("disabled");
      push_result(headers, rows);
    }
  };
  $.post(requestURL, requestData, successHandler, "json");
});

var query_clear = (function() {
  $("#query").val("");
});

var query_format = (function() {
  $("#error-msg").hide();
  $("#warn-msg").hide();
  var query = $("#query").val();
  var requestURL = "/format";
  var requestData = {
    "query": query
  };
  var successHandler = function(data) {
    if (data.error) {
      $("#error-msg").text(data.error);
      $("#error-msg").slideDown("fast");
      selectLine(data.errorLineNumber);
    } else {
      $(".codelines .lineno.lineselect").removeClass("lineselect");
      var format_query = data.formattedQuery;
      $("#query").val(format_query);
    }
  };
  $.post(requestURL, requestData, successHandler, "json");
});

var push_result = (function(headers, rows) {
  if (! window.sessionStorage) return;
  window.sessionStorage.query_header = JSON.stringify(headers);
  window.sessionStorage.query_result = JSON.stringify(rows);
});

var tsv_download = (function() {
  var query_header_string = window.sessionStorage.query_header;
  var query_result_string = window.sessionStorage.query_result;
  var headers = JSON.parse(query_header_string);
  var rows = JSON.parse(query_result_string);
  var text = headers.join("\t");
  text += "\n";
  for (var i = 0; i < rows.length; ++i) {
    var columns = rows[i];
    for (var j = 0; j < columns.length; ++j) {
      if (typeof columns[j] == "object") {
        text += JSON.stringify(columns[j]);
      } else {
        text += columns[j];
      }
      if (j != columns.length - 1) {
        text += "\t";
      }
    }
    text += "\n";
  }
  var name = "result"
  var blob = new Blob( [text], {type: 'text/plain'} )
  var link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = name + '.tsv'
  link.click()
});

var push_query = (function(query) {
  if (! window.localStorage) return;
  var list = query_histories();
  list.unshift(query);
  set_query_histories(list.slice(0, 100));
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
    var copy_button = document.createElement("button");
    $(copy_button).attr("type", "button");
    $(copy_button).attr("class", "btn btn-success");
    $(copy_button).text("copy to query area");
    $(copy_button).click({query: query_list[i]}, copy_query);
    var td = document.createElement("td");
    $(td).append(copy_button);
    $(tr).append(td);
    var delete_button = document.createElement("button");
    $(delete_button).attr("type", "button");
    $(delete_button).attr("class", "btn btn-info");
    $(delete_button).text("delete");
    $(delete_button).click({index: i}, delete_query);
    var td = document.createElement("td");
    $(td).append(delete_button);
    $(tr).append(td);
    var bookmark_button = document.createElement("button");
    $(bookmark_button).attr("type", "button");
    $(bookmark_button).attr("class", "btn btn-warning");
    $(bookmark_button).text("bookmark");
    $(bookmark_button).click({query: query_list[i]}, add_bookmark);
    var td = document.createElement("td");
    $(td).append(bookmark_button);
    $(tr).append(td);
    var td = document.createElement("td");
    $(td).text(query_list[i]);
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
    var copy_button = document.createElement("button");
    $(copy_button).attr("type", "button");
    $(copy_button).attr("class", "btn btn-success");
    $(copy_button).text("copy to query area");
    $(copy_button).click({query: query_list[i]}, copy_query);
    var td = document.createElement("td");
    $(td).append(copy_button);
    $(tr).append(td);
    var delete_button = document.createElement("button");
    $(delete_button).attr("type", "button");
    $(delete_button).attr("class", "btn btn-info");
    $(delete_button).text("delete");
    $(delete_button).click({index: i}, delete_bookmark);
    var td = document.createElement("td");
    $(td).append(delete_button);
    $(tr).append(td);
    var td = document.createElement("td");
    $(td).text(query_list[i]);
    $(tr).append(td);
    $(tbody).append(tr);
  }
  $("#query-bookmarks").append(tbody);
});

var add_bookmark = (function(event) {
  if (! window.localStorage) return;
  var list = query_bookmarks();
  list.unshift(event.data.query);
  set_query_bookmarks(list.slice(0, 100));
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
      if (typeof columns[j] == "object") {
        $(td).text(JSON.stringify(columns[j]));
      } else {
        $(td).text(columns[j]);
      }
      $(tr).append(td);
    }
    $(tbody).append(tr);
  }
  $(table_id).append(tbody);
  $(table_id).fixedHeaderTable("destroy");
  $(table_id).fixedHeaderTable();

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
