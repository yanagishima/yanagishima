package yanagishima.servlet;

import static java.util.Objects.requireNonNull;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.DownloadUtil.downloadCsv;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import yanagishima.config.YanagishimaConfig;
import yanagishima.repository.TinyOrm;
import yanagishima.model.db.Query;
import yanagishima.model.HttpRequestContext;

@Singleton
public class CsvDownloadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final YanagishimaConfig config;
    private TinyOrm db;

    @Inject
    public CsvDownloadServlet(YanagishimaConfig config, TinyOrm db) {
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

        String fileName = context.getQueryId() + ".csv";
        if (config.isCheckDatasource() && !validateDatasource(request, context.getDatasource())) {
            sendForbiddenError(response);
            return;
        }

        if (config.isAllowOtherReadResult(context.getDatasource())) {
            downloadCsv(response, fileName, context.getDatasource(), context.getQueryId(), context.getEncode(), context.isShowHeader(), context.isShowBom());
            return;
        }
        String user = request.getHeader(config.getAuditHttpHeaderName());
        requireNonNull(user, "user is null");
        Optional<Query> userQuery = db.singleQuery("query_id=? and datasource=? and user=?", context.getQueryId(), context.getDatasource(), user);
        if (userQuery.isPresent()) {
            downloadCsv(response, fileName, context.getDatasource(), context.getQueryId(), context.getEncode(), context.isShowHeader(), context.isShowBom());
        }
    }
}
