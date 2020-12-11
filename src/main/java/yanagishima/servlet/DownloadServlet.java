package yanagishima.servlet;

import yanagishima.config.YanagishimaConfig;
import yanagishima.repository.TinyOrm;
import yanagishima.model.db.Query;
import yanagishima.model.HttpRequestContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.DownloadUtil.downloadTsv;

@Singleton
public class DownloadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final YanagishimaConfig config;
    private final TinyOrm db;

    @Inject
    public DownloadServlet(YanagishimaConfig config, TinyOrm db) {
        this.config = config;
        this.db = db;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        HttpRequestContext context = new HttpRequestContext(request);
        if (context.getQueryId() == null) {
            return;
        }

        requireNonNull(context.getDatasource(), "datasource is null");
        if (config.isCheckDatasource() && !validateDatasource(request, context.getDatasource())) {
            sendForbiddenError(response);
            return;
        }

        String fileName = context.getQueryId() + ".tsv";
        if (config.isAllowOtherReadResult(context.getDatasource())) {
            downloadTsv(response, fileName, context.getDatasource(), context.getQueryId(), context.getEncode(), context.isShowHeader(), context.isShowBom());
            return;
        }
        String user = request.getHeader(config.getAuditHttpHeaderName());
        requireNonNull(user, "Username must exist when auditing header name is enabled");
        Optional<Query> query = db.singleQuery("query_id = ? AND datasource = ? AND user = ?", context.getQueryId(), context.getDatasource(), user);
        if (query.isPresent()) {
            downloadTsv(response, fileName, context.getDatasource(), context.getQueryId(), context.getEncode(), context.isShowHeader(), context.isShowBom());
        }
    }
}

