package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Query;
import yanagishima.util.DownloadUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;

@Singleton
public class CsvDownloadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private YanagishimaConfig yanagishimaConfig;

    @Inject
    private TinyORM db;

    @Inject
    public CsvDownloadServlet(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        Optional<String> queryidOptional = Optional.ofNullable(request.getParameter("queryid"));
        queryidOptional.ifPresent(queryid -> {
            String fileName = queryid + ".csv";
            String datasource = getRequiredParameter(request, "datasource");
            if (yanagishimaConfig.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return;
            }
            Optional<String> encodeOptional = Optional.ofNullable(request.getParameter("encode"));
            String header = request.getParameter("header");
            boolean headerFlag = true;
            if(header != null && header.equals("false")) {
                headerFlag = false;
            }
            String bom = Optional.ofNullable(request.getParameter("bom")).orElse("true");
            boolean showBOM = Boolean.parseBoolean(bom);
            if(yanagishimaConfig.isAllowOtherReadResult(datasource)) {
                DownloadUtil.downloadCsv(response, fileName, datasource, queryid, encodeOptional.orElse("UTF-8"), headerFlag, showBOM);
            } else {
                String userName = request.getHeader(yanagishimaConfig.getAuditHttpHeaderName());
                if (userName == null) {
                    throw new RuntimeException("user is null");
                }
                Optional<Query> userQueryOptional = db.single(Query.class).where("query_id=? and datasource=? and user=?", queryidOptional.get(), datasource, userName).execute();
                if(userQueryOptional.isPresent()) {
                    DownloadUtil.downloadCsv(response, fileName, datasource, queryid, encodeOptional.orElse("UTF-8"), headerFlag, showBOM);
                }
            }
        });

    }

}
