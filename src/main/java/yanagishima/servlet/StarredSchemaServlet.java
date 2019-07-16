package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.StarredSchema;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Singleton
public class StarredSchemaServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory
            .getLogger(StarredSchemaServlet.class);

    private static final long serialVersionUID = 1L;

    @Inject
    private TinyORM db;

    private YanagishimaConfig yanagishimaConfig;

    @Inject
    public StarredSchemaServlet(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
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

            String userName = request.getHeader(yanagishimaConfig.getAuditHttpHeaderName());
            String catalog = HttpRequestUtil.getParam(request, "catalog");
            String engine = HttpRequestUtil.getParam(request, "engine");
            String schema = HttpRequestUtil.getParam(request, "schema");
            int count = db.insert(StarredSchema.class).value("datasource", datasource).value("engine", engine).value("catalog", catalog).value("schema", schema).value("user", userName).execute();
            List<StarredSchema> starredSchemaList = db.searchBySQL(StarredSchema.class, "select starred_schema_id, datasource, engine, catalog, `schema` from starred_schema where starred_schema_id = last_insert_id()");
            if (starredSchemaList.size() == 1) {
                retVal.put("starred_schema_id", starredSchemaList.get(0).getStarredSchemaId());
            } else {
                retVal.put("error", "too many starred schema list = " + starredSchemaList);
            }
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }

    @Override
    protected void doDelete(HttpServletRequest request,
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

            String starredSchemaId = HttpRequestUtil.getParam(request, "starred_schema_id");
            StarredSchema deletedStarredSchema = db.single(StarredSchema.class).where("starred_schema_id=?", starredSchemaId).execute().get();
            deletedStarredSchema.delete();

            String engine = HttpRequestUtil.getParam(request, "engine");
            String catalog = HttpRequestUtil.getParam(request, "catalog");
            String userName = request.getHeader(yanagishimaConfig.getAuditHttpHeaderName());
            List<StarredSchema> starredSchemaList = db.search(StarredSchema.class).where("datasource = ? and engine = ? and catalog = ? and user = ?", datasource, engine, catalog, userName).execute();

            List<Map> resultMapList = new ArrayList<>();
            for (StarredSchema starredSchema : starredSchemaList) {
                Map<String, Object> m = new HashMap<>();
                m.put("starred_schema_id", starredSchema.getStarredSchemaId());
                m.put("datasource", starredSchema.getDatasource());
                m.put("engine", starredSchema.getEngine());
                m.put("catalog", starredSchema.getCatalog());
                m.put("schema", starredSchema.getSchema());
                m.put("user", userName);
                resultMapList.add(m);
            }
            retVal.put("starredSchemaList", resultMapList);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }

    @Override
    protected void doGet(HttpServletRequest request,
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

            String engine = HttpRequestUtil.getParam(request, "engine");
            String catalog = HttpRequestUtil.getParam(request, "catalog");
            String userName = request.getHeader(yanagishimaConfig.getAuditHttpHeaderName());
            List<StarredSchema> starredSchemaList = db.search(StarredSchema.class).where("datasource = ? and engine = ? and catalog = ? and user = ?", datasource, engine, catalog, userName).execute();

            List<Map> resultMapList = new ArrayList<>();
            for (StarredSchema starredSchema : starredSchemaList) {
                Map<String, Object> m = new HashMap<>();
                m.put("starred_schema_id", starredSchema.getStarredSchemaId());
                m.put("datasource", starredSchema.getDatasource());
                m.put("engine", starredSchema.getEngine());
                m.put("catalog", starredSchema.getCatalog());
                m.put("schema", starredSchema.getSchema());
                m.put("user", userName);
                resultMapList.add(m);
            }
            retVal.put("starredSchemaList", resultMapList);

        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }

}
