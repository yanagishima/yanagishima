package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Query;
import yanagishima.util.AccessControlUtil;
import yanagishima.util.DownloadUtil;
import yanagishima.util.HttpRequestUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Singleton
public class CsvDownloadServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory.getLogger(CsvDownloadServlet.class);

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
            String datasource = HttpRequestUtil.getParam(request, "datasource");
            if(yanagishimaConfig.isCheckDatasource()) {
                if(!AccessControlUtil.validateDatasource(request, datasource)) {
                    try {
                        response.sendError(SC_FORBIDDEN);
                        return;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            Optional<String> encodeOptional = Optional.ofNullable(request.getParameter("encode"));
            String header = request.getParameter("header");
            boolean headerFlag = true;
            if(header != null && header.equals("false")) {
                headerFlag = false;
            }
            if(yanagishimaConfig.isAllowOtherReadResult(datasource)) {
                DownloadUtil.csvDownload(response, fileName, datasource, queryid, encodeOptional.orElse("UTF-8"), headerFlag);
            } else {
                String userName = request.getHeader(yanagishimaConfig.getAuditHttpHeaderName());
                if (userName == null) {
                    throw new RuntimeException("user is null");
                }
                Optional<Query> userQueryOptional = db.single(Query.class).where("query_id=? and datasource=? and user=?", queryidOptional.get(), datasource, userName).execute();
                if(userQueryOptional.isPresent()) {
                    DownloadUtil.csvDownload(response, fileName, datasource, queryid, encodeOptional.orElse("UTF-8"), headerFlag);
                }
            }
        });

    }

}
