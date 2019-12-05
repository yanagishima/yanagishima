package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Query;

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
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;

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
        String queryId = request.getParameter("queryid");
        if (queryId == null) {
            return;
        }

        String datasource = getRequiredParameter(request, "datasource");
        if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
            sendForbiddenError(response);
            return;
        }

        String fileName = queryId + ".tsv";
        String encode = getOrDefaultParameter(request, "encode", "UTF-8");
        boolean showHeader = getOrDefaultParameter(request, "header", true);
        boolean showBOM = getOrDefaultParameter(request, "bom", true);
        if (config.isAllowOtherReadResult(datasource)) {
            downloadTsv(response, fileName, datasource, queryId, encode, showHeader, showBOM);
            return;
        }
        String user = request.getHeader(config.getAuditHttpHeaderName());
        requireNonNull(user, "Username must exist when auditing header name is enabled");
        Optional<Query> query = db.single(Query.class).where("query_id = ? AND datasource = ? AND user = ?", queryId, datasource, user).execute();
        if (query.isPresent()) {
            downloadTsv(response, fileName, datasource, queryId, encode, showHeader, showBOM);
        }
    }
}
