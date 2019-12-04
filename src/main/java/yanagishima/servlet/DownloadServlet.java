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
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;

@Singleton
public class DownloadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_ENCODE = "UTF-8";

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
        String encode = Optional.ofNullable(request.getParameter("encode")).orElse(DEFAULT_ENCODE);
        String header = Optional.ofNullable(request.getParameter("header")).orElse("true");
        boolean showHeader = Boolean.parseBoolean(header);
        String bom = Optional.ofNullable(request.getParameter("bom")).orElse("true");
        boolean showBOM = Boolean.parseBoolean(bom);
        if (config.isAllowOtherReadResult(datasource)) {
            downloadTsv(response, fileName, datasource, queryId, encode, showHeader, showBOM);
            return;
        }
        String userName = request.getHeader(config.getAuditHttpHeaderName());
        requireNonNull(userName, "Username must exist when auditing header name is enabled");
        Optional<Query> query = db.single(Query.class).where("query_id = ? AND datasource = ? AND user = ?", queryId, datasource, userName).execute();
        if (query.isPresent()) {
            downloadTsv(response, fileName, datasource, queryId, encode, showHeader, showBOM);
        }
    }
}
