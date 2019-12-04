package yanagishima.servlet;

import static java.util.Objects.requireNonNull;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.DownloadUtil.downloadCsv;
import static yanagishima.util.HttpRequestUtil.getOrDefaultParameter;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.geso.tinyorm.TinyORM;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Query;

@Singleton
public class CsvDownloadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final YanagishimaConfig config;
    private TinyORM db;

    @Inject
    public CsvDownloadServlet(YanagishimaConfig config, TinyORM db) {
        this.config = config;
        this.db = db;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        String queryId = request.getParameter("queryid");
        if (queryId == null) {
            return;
        }

        String fileName = queryId + ".csv";
        String datasource = getRequiredParameter(request, "datasource");
        if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
            sendForbiddenError(response);
            return;
        }
        String encode = getOrDefaultParameter(request, "encode", "UTF-8");
        boolean showHeader = getOrDefaultParameter(request, "header", true);
        boolean showBOM = getOrDefaultParameter(request, "bom", true);
        if (config.isAllowOtherReadResult(datasource)) {
            downloadCsv(response, fileName, datasource, queryId, encode, showHeader, showBOM);
            return;
        }
        String user = request.getHeader(config.getAuditHttpHeaderName());
        requireNonNull(user, "user is null");
        Optional<Query> userQuery = db.single(Query.class).where("query_id=? and datasource=? and user=?", queryId, datasource, user).execute();
        if (userQuery.isPresent()) {
            downloadCsv(response, fileName, datasource, queryId, encode, showHeader, showBOM);
        }
    }
}
