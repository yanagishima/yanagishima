package yanagishima.servlet;

import lombok.extern.slf4j.Slf4j;
import yanagishima.config.YanagishimaConfig;
import yanagishima.repository.TinyOrm;
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
import static yanagishima.repository.TinyOrm.value;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.JsonUtil.writeJSON;

@Slf4j
@Singleton
public class StarredSchemaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final YanagishimaConfig config;
    private final TinyOrm db;

    @Inject
    public StarredSchemaServlet(YanagishimaConfig config, TinyOrm db) {
        this.config = config;
        this.db = db;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String datasource = getRequiredParameter(request, "datasource");
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return;
            }
            String userName = request.getHeader(config.getAuditHttpHeaderName());
            String catalog = getRequiredParameter(request, "catalog");
            String engine = getRequiredParameter(request, "engine");
            String schema = getRequiredParameter(request, "schema");
            db.insert(StarredSchema.class, value("datasource", datasource), value("engine", engine), value("catalog", catalog), value("schema", schema), value("user", userName));
            List<StarredSchema> starredSchemas;
            switch (config.getDatabaseType()) {
                case MYSQL:
                    starredSchemas = db.searchBySQL(StarredSchema.class, "SELECT starred_schema_id, datasource, engine, catalog, `schema` FROM starred_schema "
                            + "WHERE starred_schema_id = last_insert_id()");
                    break;
                case SQLITE:
                    starredSchemas = db.searchBySQL(StarredSchema.class, "SELECT starred_schema_id, datasource, engine, catalog, schema FROM starred_schema "
                            + "WHERE rowid = last_insert_rowid()");
                    break;
                default:
                    throw new IllegalArgumentException("Illegal database type: " + config.getDatabaseType());
            }
            checkState(starredSchemas.size() == 1, "Too many starred schemas: " + starredSchemas);
            writeJSON(response, Map.of("starred_schema_id", starredSchemas.get(0).getStarredSchemaId()));
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            writeJSON(response, Map.of("error", e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
        try {
            String datasource = getRequiredParameter(request, "datasource");
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return;
            }

            String starredSchemaId = getRequiredParameter(request, "starred_schema_id");
            StarredSchema deletedStarredSchema = db.singleStarredSchema("starred_schema_id=?", starredSchemaId).get();
            deletedStarredSchema.delete();

            String engine = getRequiredParameter(request, "engine");
            String catalog = getRequiredParameter(request, "catalog");
            String userName = request.getHeader(config.getAuditHttpHeaderName());
            List<StarredSchema> starredSchemas = db.searchStarredSchemas("datasource = ? AND engine = ? AND catalog = ? AND user = ?", datasource, engine, catalog, userName);
            writeJSON(response, Map.of("starredSchemaList", starredSchemas));
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            writeJSON(response, Map.of("error", e.getMessage()));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            String datasource = getRequiredParameter(request, "datasource");
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return;
            }

            String engine = getRequiredParameter(request, "engine");
            String catalog = getRequiredParameter(request, "catalog");
            String userName = request.getHeader(config.getAuditHttpHeaderName());
            List<StarredSchema> starredSchemas = db.searchStarredSchemas("datasource = ? AND engine = ? AND catalog = ? AND user = ?", datasource, engine, catalog, userName);
            writeJSON(response, Map.of("starredSchemaList", starredSchemas));
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            writeJSON(response, Map.of("error", e.getMessage()));
        }
    }
}
