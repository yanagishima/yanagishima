package yanagishima.model;

import static yanagishima.util.HttpRequestUtil.getOrDefaultParameter;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

public class HttpRequestContext {
    private final String datasource;
    private final String engine;

    private final String queryId;
    private final String query;

    private final String title;
    private final String bookmarkId;
    private final String snippet;

    private final String like;
    private final String search;
    private final String sort;
    private final String content;

    private final String encode;
    private final boolean showHeader;
    private final boolean showBom;

    public HttpRequestContext(HttpServletRequest request) {
        this.datasource = request.getParameter("datasource");
        this.engine = request.getParameter("engine");
        this.queryId = request.getParameter("queryid");
        this.query = request.getParameter("query");
        this.title = request.getParameter("title");
        this.bookmarkId = request.getParameter("bookmark_id");
        this.snippet = request.getParameter("snippet");
        this.like = request.getParameter("like");
        this.search = request.getParameter("search");
        this.sort = request.getParameter("sort");
        this.content = request.getParameter("content");
        this.encode = getOrDefaultParameter(request, "encode", "UTF-8");
        this.showHeader = getOrDefaultParameter(request, "header", true);
        this.showBom = getOrDefaultParameter(request, "bom", true);
    }

    @Nullable
    public String getDatasource() {
        return datasource;
    }

    @Nullable
    public String getEngine() {
        return engine;
    }

    @Nullable
    public String getQueryId() {
        return queryId;
    }

    @Nullable
    public String getQuery() {
        return query;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    @Nullable
    public String getBookmarkId() {
        return bookmarkId;
    }

    @Nullable
    public String getSnippet() {
        return snippet;
    }

    @Nullable
    public String getLike() {
        return like;
    }

    @Nullable
    public String getSearch() {
        return search;
    }

    @Nullable
    public String getSort() {
        return sort;
    }

    @Nullable
    public String getContent() {
        return content;
    }

    public String getEncode() {
        return encode;
    }

    public boolean isShowHeader() {
        return showHeader;
    }

    public boolean isShowBom() {
        return showBom;
    }
}
