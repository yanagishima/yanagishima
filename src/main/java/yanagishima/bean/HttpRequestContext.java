package yanagishima.bean;

import com.google.common.base.Splitter;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class HttpRequestContext {
    private static final Splitter SPLITTER = Splitter.on(',');

    private final String header;

    private final String datasource;
    private final String queryId;
    private final String engine;
    private final String query;
    private final String schema;
    private final String table;

    private final List<String> partitionColumns;
    private final List<String> partitionColumnTypes;
    private final List<String> partitionValues;

    private final String user;
    private final String password;

    private final String id;

    private final String like;
    private final String sort;
    private final String title;
    private final String translate;
    private final String search;
    private final String limit;
    private final String label;
    private final String store;
    private final String content;

    private final String csv;
    private final String encode;

    private final String publishId;

    public HttpRequestContext(HttpServletRequest request) {
        header = request.getParameter("header");
        datasource = request.getParameter("datasource");
        queryId = request.getParameter("queryid");
        engine = request.getParameter("engine");
        query = request.getParameter("query");
        schema = request.getParameter("schema");
        table = request.getParameter("table");

        String partitionColumn = request.getParameter("partitionColumn");
        partitionColumns = partitionColumn == null ? List.of() : SPLITTER.splitToList(partitionColumn);

        String partitionColumnType = request.getParameter("partitionColumnType");
        partitionColumnTypes = partitionColumnType == null ? List.of() : SPLITTER.splitToList(partitionColumnType);

        String partitionValue = request.getParameter("partitionValue");
        partitionValues = partitionColumnType == null ? List.of() : SPLITTER.splitToList(partitionValue);

        user = request.getParameter("user");
        password = request.getParameter("password");

        id = request.getParameter("id");

        like = request.getParameter("like");
        sort = request.getParameter("sort");
        title = request.getParameter("title");
        translate = request.getParameter("translate");
        search = request.getParameter("search");
        limit = request.getParameter("limit");
        label = request.getParameter("label");
        store = request.getParameter("store");
        content = request.getParameter("content");

        csv = request.getParameter("csv");
        encode = request.getParameter("encode");

        publishId = request.getParameter("publish_id");
    }

    public String getHeader() {
        return header;
    }

    public String getDatasource() {
        return datasource;
    }

    public String getQueryId() {
        return queryId;
    }

    public String getEngine() {
        return engine;
    }

    public String getQuery() {
        return query;
    }

    public String getSchema() {
        return schema;
    }

    public String getTable() {
        return table;
    }

    public List<String> getPartitionColumns() {
        return partitionColumns;
    }

    public List<String> getPartitionColumnTypes() {
        return partitionColumnTypes;
    }

    public List<String> getPartitionValues() {
        return partitionValues;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getId() {
        return id;
    }

    public String getLike() {
        return like;
    }

    public String getSort() {
        return sort;
    }

    public String getTitle() {
        return title;
    }

    public String getTranslate() {
        return translate;
    }

    public String getSearch() {
        return search;
    }

    public String getLimit() {
        return limit;
    }

    public String getLabel() {
        return label;
    }

    public String getStore() {
        return store;
    }

    public String getContent() {
        return content;
    }

    public String getCsv() {
        return csv;
    }

    public String getEncode() {
        return encode;
    }

    public String getPublishId() {
        return publishId;
    }
}
