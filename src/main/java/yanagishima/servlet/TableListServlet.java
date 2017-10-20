package yanagishima.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.service.PrestoService;
import yanagishima.util.AccessControlUtil;
import yanagishima.util.HttpRequestUtil;
import yanagishima.util.JsonUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static yanagishima.util.Constants.YANAGISHIMA_COMMENT;

@Singleton
public class TableListServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory
            .getLogger(TableListServlet.class);

    private static final long serialVersionUID = 1L;

    private PrestoService prestoService;

    private YanagishimaConfig yanagishimaConfig;

    @Inject
    public TableListServlet(PrestoService prestoService, YanagishimaConfig yanagishimaConfig) {
        this.prestoService = prestoService;
        this.yanagishimaConfig = yanagishimaConfig;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<String, Object>();

        try {

            String datasource = HttpRequestUtil.getParam(request, "datasource");
            if (yanagishimaConfig.isCheckDatasource()) {
                if (!AccessControlUtil.validateDatasource(request, datasource)) {
                    try {
                        response.sendError(SC_FORBIDDEN);
                        return;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            String catalog = HttpRequestUtil.getParam(request, "catalog");
            String userName = null;
            Optional<String> prestoUser = Optional.ofNullable(request.getParameter("user"));
            Optional<String> prestoPassword = Optional.ofNullable(request.getParameter("password"));
            if(yanagishimaConfig.isUseAuditHttpHeaderName()) {
                userName = request.getHeader(yanagishimaConfig.getAuditHttpHeaderName());
            } else {
                if (prestoUser.isPresent() && prestoPassword.isPresent()) {
                    userName = prestoUser.get();
                }
            }
            List<String> invisibleSchemas = yanagishimaConfig.getInvisibleSchemas(datasource, catalog);
            String notin = "('" + String.join("','", invisibleSchemas) + "')";
            String query = String.format("%sSELECT table_catalog || '.' || table_schema || '.' || table_name FROM %s.information_schema.tables WHERE table_schema NOT IN %s", YANAGISHIMA_COMMENT, catalog, notin);
            List<String> tables = prestoService.doQuery(datasource, query, userName, prestoUser, prestoPassword, false, Integer.MAX_VALUE).getRecords().stream().map(list -> list.get(0)).collect(Collectors.toList());

            retVal.put("tableList", tables);

        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }

}
