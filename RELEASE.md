# Version 21.0
* add datasource color
* add announce/notification feature
* refactor UI
* add detail query error message and semanticErrorName https://github.com/prestosql/presto/pull/790
* handle old presto due to https://github.com/prestosql/presto/issues/224
* improve message when result file is not found, allow.other.read.result.xxx=false
* use JDK11, Gradle5
* upgrade presto client library
* enable to set datetime format pattern per datasource in treeview
* show more information like execution time, file size, etc when query result file is removed
* fix header layout
* update config add user and source (#56)
* Avoid scroll bar from hiding note
* improve partition column fetch logic in treeview
* Add webhdfs.proxy.user and webhdfs.proxy.password option
* Add a script to launch yanagishima in foreground process (#58)
* support mysql as yanagishima backend RDBMS create mysql table
```
create table if not exists query (datasource varchar(256), engine varchar(256), query_id varchar(256), fetch_result_time_string varchar(256), query_string text, user varchar(256), status varchar(256), elapsed_time_millis integer, result_file_size integer, linenumber integer, primary key(datasource, engine, query_id));
create table if not exists publish (publish_id varchar(256), datasource varchar(256), engine varchar(256), query_id varchar(256), user varchar(256), primary key(publish_id));
create table if not exists bookmark (bookmark_id integer primary key auto_increment, datasource varchar(256), engine varchar(256), query text, title varchar(256), user varchar(256));
create table if not exists comment (datasource varchar(256), engine varchar(256), query_id varchar(256), content text, update_time_string varchar(256), user varchar(256), like_count integer, primary key(datasource, engine, query_id));
create table if not exists label (datasource varchar(256), engine varchar(256), query_id varchar(256), label_name varchar(256), primary key(datasource, engine, query_id));
```
yanagishima setting is the following
```
database.type=mysql
database.connection-url=jdbc:mysql://localhost:3306/yanagishima?useSSL=false
database.user=...
database.password=...
```

# Version 20.0
* show query diff
* enable user to download without column header

# Version 19.0
* support Spark SQL
* show stats for presto

# Version 18.0
* fix the bug that desktop notification doesn't work
* improve catalog setting logic if there is no hive catalog
* improve error logging
* fix bug that java.lang.IllegalArgumentException: float is illegal
* fix bug that publish doesn't work in HTTP
* improve partition fetching logic with webhdfs

# Version 17.0
* add link which can open a schema/table in Treeview
* pivot
* fix bug which invisible.schema doesn't work
* improve hive job handling when you create/drop table
* add query id link to share page
* update presto library
* fix partition bug
* remove kill button in hive
* default value of ```use.new.show.partitions.xxx``` is true

# Version 16.0
* refactoring with Vuex, Vue Router, Single File Components
* add BOM in CSV/TSV download files
* improve to display hive map data
* add pretty print on share view
* enable code folding
* make underscores available in placeholders
* move treeview to left end tab

# Version 15.0
* support Elasticsearch SQL
* add label feature like gmail
* handle presto/hive reserved keywords(e.g. group, order)
* integrate metadata service
* add partition filter
* show column number
* suppresss stacktrace

# Version 14.0
* fix wrong line number when result file contains a new line
* metadata of 14.0 is NOT compatible with 13.0, so migration is required
* migrateV14.sh is the script to migrate db file.
* migration process is as follows
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
* fix infinite loop bug when /queryStatus returns 500
* add kill hive query feature if you use a kerberized hadoop cluster
* sort table name when you use ```Treeview```
* copy publish url to clipboard but chrome user only due to Async Clipboard API
* handle presto/hive reserved keywords(e.g. group, order)
* use timestamp to index.js due to cache busting

# Version 13.0
* improve code input performance especially when query result is huge
* upgrade ace editor
* add message if result count exceeds 500
* improve history tab logic when result file is removed
* add sort partition feature
* fix bug that 3 pane compare result display disappear
* don't create fluency instance every request due to performance improvement
* handle issue that presto doesn't support ```show paritions``` since 0.202
* add option to use webhdfs api when there are too many partitions

# Version 12.0
* convert hive/presto query
* support graphviz to visualize presto explain result
* add tooltip to ```Set``` in History/Bookmark tab
* add new presto functions(0.196) to completion list
* fix bookmark bug
* fix presto authentication failed bug

# Version 11.0
* fix timezone bug
* fix exponential notation bug
* support UTF-8 encoding for CSV

# Version 10.0
* add timeline tab

# Version 9.0
* pretty print for map data
* add left panel to compare query result
* support presto/hive authentication with user/password
* if you want to use presto TLS, you need to execute ```keytool -import``` https://prestosql.io/docs/current/security/tls.html
* search query history
* paging query history 
* improve performance to write/read result file
* result file format of 9.0 is tsv, prior to 9.0 is json, so migration is required
* migrateV9.sh is the script to migrate result file.
* if migration error occur, you can check it.
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

# Version 8.0
* pretty print for json data
* store query history/bookmark to server side db, but default setting is to use local storage
* improve partition display
* metadata of 9.0 is NOT compatible with 7.0, so migration is required
* migration process is as follows
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

# Version 7.0
* support hive on MapReduce(yanagishima executes ```set mapreduce.job.name=...```)
* metadata of 7.0 is NOT compatible with 6.0, so migration is required
* migration process is as follows
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

# Version 6.0
* support bookmark title, so add title column to bookmark table
* metadata of 6.0 is NOT compatible with 5.0, so migration is required
* migration process is as follows
```
cp data/yanagishima.db data/yanagishima.db.bak
sqlite3 data/yanagishima.db
sqlite> create table if not exists bookmark_new (bookmark_id integer primary key autoincrement, datasource text, query text, title text);
sqlite> insert into bookmark_new select bookmark_id, datasource, query, null from bookmark;
sqlite> alter table bookmark rename to bookmark_old;
sqlite> alter table bookmark_new rename to bookmark;
If you confirmed, drop table bookmark_old;
```
