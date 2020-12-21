package yanagishima.servlet;

import static com.google.common.base.Preconditions.checkState;
import static yanagishima.repository.TinyOrm.value;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.config.YanagishimaConfig;
import yanagishima.model.db.StarredSchema;
import yanagishima.model.dto.StarredSchemaCreateDto;
import yanagishima.model.dto.StarredSchemaDto;
import yanagishima.repository.TinyOrm;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StarredSchemaServlet {
    private final YanagishimaConfig config;
    private final TinyOrm db;

    @PostMapping("starredSchema")
    public StarredSchemaCreateDto post(@RequestParam String datasource, @RequestParam String engine,
                                       @RequestParam String catalog, @RequestParam String schema,
                                       HttpServletRequest request, HttpServletResponse response) {
        StarredSchemaCreateDto starredSchemaCreateDto = new StarredSchemaCreateDto();
        try {
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return starredSchemaCreateDto;
            }
            String userName = request.getHeader(config.getAuditHttpHeaderName());
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
            starredSchemaCreateDto.setStarredSchemaId(starredSchemas.get(0).getStarredSchemaId());
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            starredSchemaCreateDto.setError(e.getMessage());
        }
        return starredSchemaCreateDto;
    }

    @DeleteMapping("starredSchema")
    public StarredSchemaDto delete(@RequestParam String datasource,
                                   @RequestParam String engine,
                                   @RequestParam String catalog,
                                   @RequestParam(name = "starred_schema_id") String starredSchemaId,
                                   HttpServletRequest request, HttpServletResponse response) {
        StarredSchemaDto starredSchemaDto = new StarredSchemaDto();
        try {
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return starredSchemaDto;
            }

            db.deleteStarredSchema("starred_schema_id=?", starredSchemaId);

            String userName = request.getHeader(config.getAuditHttpHeaderName());
            List<StarredSchema> starredSchemas = db.searchStarredSchemas("datasource = ? AND engine = ? AND catalog = ? AND user = ?", datasource, engine, catalog, userName);
            starredSchemaDto.setStarredSchemaList(starredSchemas);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            starredSchemaDto.setError(e.getMessage());
        }
        return starredSchemaDto;
    }

    @GetMapping("starredSchema")
    public StarredSchemaDto get(@RequestParam String datasource,
                                @RequestParam String engine,
                                @RequestParam String catalog,
                                HttpServletRequest request, HttpServletResponse response) {
        StarredSchemaDto starredSchemaDto = new StarredSchemaDto();
        try {
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return starredSchemaDto;
            }

            String userName = request.getHeader(config.getAuditHttpHeaderName());
            List<StarredSchema> starredSchemas = db.searchStarredSchemas("datasource = ? AND engine = ? AND catalog = ? AND user = ?", datasource, engine, catalog, userName);
            starredSchemaDto.setStarredSchemaList(starredSchemas);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            starredSchemaDto.setError(e.getMessage());
        }
        return starredSchemaDto;
    }
}
