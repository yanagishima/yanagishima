package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Query;
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
import static yanagishima.util.HttpRequestUtil.getOrDefaultParameter;

@Singleton
public class DownloadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final YanagishimaConfig config;
    private final TinyORM db;

    @Inject
    public DownloadServlet(YanagishimaConfig config, TinyORM db) {
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
        Optional<Query> query = db.single(Query.class).where("query_id = ? AND datasource = ? AND user = ?", context.getQueryId(), context.getDatasource(), user).execute();
        if (query.isPresent()) {
            downloadTsv(response, fileName, context.getDatasource(), context.getQueryId(), context.getEncode(), context.isShowHeader(), context.isShowBom());
        }
    }
}

