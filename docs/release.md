# Release Notes

## Version 22.0
* Avoid scroll bar from hiding error message
* Remove embedded old bootstrap
* Add tooltip for presto row column type
* Delete unnecessary scss import
* Introduce starred schema
* Create URL to set bookmarked query with parameter
* Bundle ace editor
* Lazy load big and rarely used js libraries
* Redesign treeview layout
* Redesign Partition Layout
* Support presto date type
* Add presto query page link to query
* Add filter query list by source
* Fix bug that history tab doesn't show when you use elasticsearch
* Improve result tab download UI
* Improve Header transition
* Refactor server side logic
* Upgrade Gradle from 5.0 to 5.2
* Improve server side logging logic
* Fix bug when user submits very long query
* Introduce mkdocs documentation

## Version 21.0
* Add datasource color
* Add announce/notification feature
* Refactor UI
* Add detail query error message and semanticErrorName [presto#790](https://github.com/prestosql/presto/pull/790)
* Handle old presto due to [presto#224](https://github.com/prestosql/presto/issues/224)
* Improve message when result file is not found, allow.other.read.result.xxx=false
* Use JDK11, Gradle5
* Upgrade presto client library
* Enable to set datetime format pattern per datasource in treeview
* Show more information like execution time, file size, etc when query result file is removed
* Fix header layout
* Update config add user and source (#56)
* Avoid scroll bar from hiding note
* Improve partition column fetch logic in treeview
* Add webhdfs.proxy.user and webhdfs.proxy.password option
* Add a script to launch yanagishima in foreground process (#58)
* Support mysql as yanagishima backend RDBMS
yanagishima setting is the following
```
database.type=mysql
database.connection-url=jdbc:mysql://localhost:3306/yanagishima?useSSL=false
database.user=...
database.password=...
```

## Version 20.0
* Show query diff
* Enable user to download without column header

## Version 19.0
* Support Spark SQL
* Show stats for presto

## Version 18.0
* Fix the bug that desktop notification doesn't work
* Improve catalog setting logic if there is no hive catalog
* Improve error logging
* Fix bug that java.lang.IllegalArgumentException: float is illegal
* Fix bug that publish doesn't work in HTTP
* Improve partition fetching logic with webhdfs

## Version 17.0
* Add link which can open a schema/table in Treeview
* Pivot
* Fix bug which invisible.schema doesn't work
* Improve hive job handling when you create/drop table
* Add query id link to share page
* Update presto library
* Fix partition bug
* Remove kill button in hive
* Default value of ```use.new.show.partitions.xxx``` is true

## Version 16.0
* Refactoring with Vuex, Vue Router, Single File Components
* Add BOM in CSV/TSV download files
* Improve to display hive map data
* Add pretty print on share view
* Enable code folding
* Make underscores available in placeholders
* Move treeview to left end tab

## Version 15.0
* Support Elasticsearch SQL
* Add label feature like gmail
* Handle presto/hive reserved keywords(e.g. group, order)
* Integrate metadata service
* Add partition filter
* Show column number
* Suppresss stacktrace

## Version 14.0
* Fix wrong line number when result file contains a new line
* Metadata of 14.0 is NOT compatible with 13.0, so migration is required
* MigrateV14.sh is the script to migrate db file.
* Migration process is as follows
```
cp data/yanagishima.db data/yanagishima.db.bak
bin/migrateV14.sh data/yanagishima.db result
sqlite3 data/yanagishima.db
sqlite> alter table query rename to query_old;
sqlite> alter table query_v14 rename to query;
If you confirmed, drop table query_old
It takes about 10 hours if db file is more than 500MB and result file is about 1TB
```
* ```vacuum``` and ```create index deq_index on query(datasource, engine, query_id)``` and ```create index deu_index on query(datasource, engine, user)``` may be necessary if yanagishima.db is huge
* Fix infinite loop bug when /queryStatus returns 500
* Add kill hive query feature if you use a kerberized hadoop cluster
* Sort table name when you use ```Treeview```
* Copy publish url to clipboard but chrome user only due to Async Clipboard API
* Handle presto/hive reserved keywords(e.g. group, order)
* Use timestamp to index.js due to cache busting

## Version 13.0
* Improve code input performance especially when query result is huge
* Upgrade ace editor
* Add message if result count exceeds 500
* Improve history tab logic when result file is removed
* Add sort partition feature
* Fix bug that 3 pane compare result display disappear
* Don't create fluency instance every request due to performance improvement
* Handle issue that presto doesn't support ```show paritions``` since 0.202
* Add option to use webhdfs api when there are too many partitions

## Version 12.0
* Convert hive/presto query
* Support graphviz to visualize presto explain result
* Add tooltip to ```Set``` in History/Bookmark tab
* Add new presto functions(0.196) to completion list
* Fix bookmark bug
* Fix presto authentication failed bug

## Version 11.0
* Fix timezone bug
* Fix exponential notation bug
* Support UTF-8 encoding for CSV

## Version 10.0
* Add timeline tab

## Version 9.0
* Pretty print for map data
* Add left panel to compare query result
* Support presto/hive authentication with user/password
* If you want to use presto TLS, you need to execute ```keytool -import``` https://prestosql
.io/docs/current/security/tls.html
* Search query history
* Paging query history 
* Improve performance to write/read result file
* Result file format of 9.0 is tsv, prior to 9.0 is json, so migration is required
* MigrateV9.sh is the script to migrate result file.
* If migration error occur, you can check it.
```
$ bin/migrateV9.sh result dest
...
processing /path/to/yanagishima-9.0/result/your-presto/20171010/20171010_072513_02895_xxvvj.json
error /path/to/yanagishima-9.0/result/your-presto/20171010/20171010_072513_02895_xxvvj.json
java.lang.RuntimeException: org.codehaus.jackson.JsonParseException: Unexpected end-of-input: expected close marker for ARRAY (from [Source: java.io.StringReader@e320068; line: 1, column: 0])
 at [Source: java.io.StringReader@e320068; line: 1, column: 241]
        at yanagishima.migration.MigrateV9.main(MigrateV9.java:59)
Caused by: org.codehaus.jackson.JsonParseException: Unexpected end-of-input: expected close marker for ARRAY (from [Source: java.io.StringReader@e320068; line: 1, column: 0])
 at [Source: java.io.StringReader@e320068; line: 1, column: 241]
        at org.codehaus.jackson.JsonParser._constructError(JsonParser.java:1433)
        at org.codehaus.jackson.impl.JsonParserMinimalBase._reportError(JsonParserMinimalBase.java:521)
        at org.codehaus.jackson.impl.JsonParserMinimalBase._reportInvalidEOF(JsonParserMinimalBase.java:454)
        at org.codehaus.jackson.impl.JsonParserBase._handleEOF(JsonParserBase.java:473)
        at org.codehaus.jackson.impl.ReaderBasedParser._skipWSOrEnd(ReaderBasedParser.java:1496)
        at org.codehaus.jackson.impl.ReaderBasedParser.nextToken(ReaderBasedParser.java:368)
        at org.codehaus.jackson.map.deser.std.CollectionDeserializer.deserialize(CollectionDeserializer.java:211)
        at org.codehaus.jackson.map.deser.std.CollectionDeserializer.deserialize(CollectionDeserializer.java:194)
        at org.codehaus.jackson.map.deser.std.CollectionDeserializer.deserialize(CollectionDeserializer.java:30)
        at org.codehaus.jackson.map.ObjectMapper._readMapAndClose(ObjectMapper.java:2732)
        at org.codehaus.jackson.map.ObjectMapper.readValue(ObjectMapper.java:1863)
        at yanagishima.migration.MigrateV9.main(MigrateV9.java:56)
processing /path/to/yanagishima-9.0/result/your-presto/20171010/20171010_072517_02897_xxvvj.json
...
```

## Version 8.0
* Pretty print for json data
* Store query history/bookmark to server side db, but default setting is to use local storage
* Improve partition display
* Metadata of 9.0 is NOT compatible with 7.0, so migration is required
* Migration process is as follows
```
cp data/yanagishima.db data/yanagishima.db.bak
sqlite3 data/yanagishima.db
sqlite> create table query_new (datasource text, engine text, query_id text, fetch_result_time_string text, query_string text, user text, primary key(datasource, engine, query_id));
sqlite> insert into query_new select datasource, engine, query_id, fetch_result_time_string, query_string, null from query;
sqlite> alter table query rename to query_old;
sqlite> alter table query_new rename to query;
sqlite> create table publish_new (publish_id text, datasource text, engine text, query_id text, user text, primary key(publish_id));
sqlite> insert into publish_new select publish_id, datasource, engine, query_id, null from publish;
sqlite> alter table publish rename to publish_old;
sqlite> alter table publish_new rename to publish;
sqlite> create table bookmark_new (bookmark_id integer primary key autoincrement, datasource text, engine text, query text, title text, user text);
sqlite> insert into bookmark_new select bookmark_id, datasource, engine, query, title, null from bookmark;
sqlite> alter table bookmark rename to bookmark_old;
sqlite> alter table bookmark_new rename to bookmark;
If you confirmed, drop table query_old, publish_old, bookmark_old;
```

## Version 7.0
* Support hive on MapReduce(yanagishima executes ```set mapreduce.job.name=...```)
* Metadata of 7.0 is NOT compatible with 6.0, so migration is required
* Migration process is as follows
```
cp data/yanagishima.db data/yanagishima.db.bak
sqlite3 data/yanagishima.db
sqlite> create table query_new (datasource text, engine text, query_id text, fetch_result_time_string text, query_string text, primary key(datasource, engine, query_id));
sqlite> insert into query_new select datasource, 'presto', query_id, fetch_result_time_string, query_string from query;
sqlite> alter table query rename to query_old;
sqlite> alter table query_new rename to query;
sqlite> create table publish_new (publish_id text, datasource text, engine text, query_id text, primary key(publish_id));
sqlite> insert into publish_new select publish_id, datasource, 'presto', query_id from publish;
sqlite> alter table publish rename to publish_old;
sqlite> alter table publish_new rename to publish;
sqlite> create table bookmark_new (bookmark_id integer primary key autoincrement, datasource text, engine text, query text, title text);
sqlite> insert into bookmark_new select bookmark_id, datasource, 'presto', query, title from bookmark;
sqlite> alter table bookmark rename to bookmark_old;
sqlite> alter table bookmark_new rename to bookmark;
If you confirmed, drop table query_old, publish_old, bookmark_old;
```

## Version 6.0
* Support bookmark title, so add title column to bookmark table
* Metadata of 6.0 is NOT compatible with 5.0, so migration is required
* Migration process is as follows
```
cp data/yanagishima.db data/yanagishima.db.bak
sqlite3 data/yanagishima.db
sqlite> create table if not exists bookmark_new (bookmark_id integer primary key autoincrement, datasource text, query text, title text);
sqlite> insert into bookmark_new select bookmark_id, datasource, query, null from bookmark;
sqlite> alter table bookmark rename to bookmark_old;
sqlite> alter table bookmark_new rename to bookmark;
If you confirmed, drop table bookmark_old;
```
