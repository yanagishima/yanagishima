package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.StarredSchema;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.JsonUtil.writeJSON;

@Singleton
public class StarredSchemaServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(StarredSchemaServlet.class);
    private static final long serialVersionUID = 1L;

    private final YanagishimaConfig config;
    private final TinyORM db;

    @Inject
    public StarredSchemaServlet(YanagishimaConfig config, TinyORM db) {
        this.config = config;
        this.db = db;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String datasource = getRequiredParameter(request, "datasource");
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                try {
                    response.sendError(SC_FORBIDDEN);
                    return;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            String userName = request.getHeader(config.getAuditHttpHeaderName());
            String catalog = getRequiredParameter(request, "catalog");
            String engine = getRequiredParameter(request, "engine");
            String schema = getRequiredParameter(request, "schema");
            db.insert(StarredSchema.class).value("datasource", datasource).value("engine", engine).value("catalog", catalog).value("schema", schema).value("user", userName).execute();
            List<StarredSchema> starredSchemas = db.searchBySQL(StarredSchema.class, "SELECT starred_schema_id, datasource, engine, catalog, `schema` FROM starred_schema WHERE starred_schema_id = last_insert_id()");
            checkState(starredSchemas.size() == 1, "Too many starred schemas: " + starredSchemas);
            writeJSON(response, Map.of("starred_schema_id", starredSchemas.get(0).getStarredSchemaId()));
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            writeJSON(response, Map.of("error", e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
        try {
            String datasource = getRequiredParameter(request, "datasource");
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                try {
                    response.sendError(SC_FORBIDDEN);
                    return;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            String starredSchemaId = getRequiredParameter(request, "starred_schema_id");
            StarredSchema deletedStarredSchema = db.single(StarredSchema.class).where("starred_schema_id=?", starredSchemaId).execute().get();
            deletedStarredSchema.delete();

            String engine = getRequiredParameter(request, "engine");
            String catalog = getRequiredParameter(request, "catalog");
            String userName = request.getHeader(config.getAuditHttpHeaderName());
            List<StarredSchema> starredSchemas = db.search(StarredSchema.class).where("datasource = ? AND engine = ? AND catalog = ? AND user = ?", datasource, engine, catalog, userName).execute();
            writeJSON(response, Map.of("starredSchemaList", starredSchemas));
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            writeJSON(response, Map.of("error", e.getMessage()));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            String datasource = getRequiredParameter(request, "datasource");
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                try {
                    response.sendError(SC_FORBIDDEN);
                    return;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            String engine = getRequiredParameter(request, "engine");
            String catalog = getRequiredParameter(request, "catalog");
            String userName = request.getHeader(config.getAuditHttpHeaderName());
            List<StarredSchema> starredSchemas = db.search(StarredSchema.class).where("datasource = ? AND engine = ? AND catalog = ? AND user = ?", datasource, engine, catalog, userName).execute();
            writeJSON(response, Map.of("starredSchemaList", starredSchemas));
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            writeJSON(response, Map.of("error", e.getMessage()));
        }
    }
}
