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
      if(node.data.partition || node.data.table) {
        $(span).contextMenu({menu: "myMenu"}, function(action, el, pos) {
          var param;
          if(node.data.table) {
            table = node.data.table;
            schema = node.parent.data.schema;
            catalog = node.parent.parent.data.catalog;
            param = "SELECT * FROM " + catalog + "." + schema + "." + table + " LIMIT 100";
          } else if(node.data.partition) {
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
            param = "SELECT * FROM " + catalog + "." + schema + "." + table + " WHERE " + parent_partition_tree.join(" and ") + " LIMIT 100";
          }
          $("#query").val(param);
          $("#query-submit").click();
        });
      }
    }
  });
  return tree;
});