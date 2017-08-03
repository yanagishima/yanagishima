package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
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
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Singleton
public class HiveServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory
            .getLogger(HiveServlet.class);

    private static final long serialVersionUID = 1L;

    @Inject
    private TinyORM db;

    private YanagishimaConfig yanagishimaConfig;

    @Inject
    public HiveServlet(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<String, Object>();

        Optional<String> queryOptional = Optional.ofNullable(request.getParameter("query"));
        queryOptional.ifPresent(query -> {
            try {

                String userName = request.getHeader(yanagishimaConfig.getAuditHttpHeaderName());
                if (yanagishimaConfig.isUserRequired() && userName == null) {
                    try {
                        response.sendError(SC_FORBIDDEN);
                        return;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

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
                if (userName != null) {
                    LOGGER.info(String.format("%s executed %s in %s", userName, query, datasource));
                }

                Class.forName("org.apache.hive.jdbc.HiveDriver");

                String url = yanagishimaConfig.getHiveJdbcUrl(datasource).get();
                String user = yanagishimaConfig.getHiveJdbcUser(datasource).get();
                String password = yanagishimaConfig.getHiveJdbcPassword(datasource).get();

                try (Connection connection = DriverManager.getConnection(url, user, password)) {
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(query);
                    ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                    int columnCount = resultSetMetaData.getColumnCount();
                    List<String> columnNameList = new ArrayList<>();
                    for (int i = 1; i <= columnCount; i++) {
                        columnNameList.add(resultSetMetaData.getColumnName(i));
                    }
                    List<List<String>> rowDataList = new ArrayList<>();
                    while (resultSet.next()) {
                        List<String> columnDataList = new ArrayList<>();
                        for (int i = 1; i <= columnCount; i++) {
                            Object resultObject = resultSet.getObject(i);
                            if (resultObject instanceof Long) {
                                columnDataList.add(((Long) resultObject).toString());
                            } else {
                                if (resultObject == null) {
                                    columnDataList.add(null);
                                } else {
                                    columnDataList.add(resultObject.toString());
                                }
                            }
                        }
                        rowDataList.add(columnDataList);
                    }
                    retVal.put("headers", columnNameList);
                    retVal.put("results", rowDataList);
                }


            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
                retVal.put("error", e.getMessage());
            }
        });


        JsonUtil.writeJSON(response, retVal);

    }
}
