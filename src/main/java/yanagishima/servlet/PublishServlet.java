package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Publish;
import yanagishima.row.Query;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.JsonUtil.writeJSON;

@Singleton
public class PublishServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(PublishServlet.class);
    private static final long serialVersionUID = 1L;

    private final YanagishimaConfig config;
    private final TinyORM db;

    @Inject
    public PublishServlet(YanagishimaConfig config, TinyORM db) {
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
            String engine = getRequiredParameter(request, "engine");
            String queryid = getRequiredParameter(request, "queryid");
            if (config.isAllowOtherReadResult(datasource)) {
                writeJSON(response, Map.of("publish_id", publishQuery(datasource, userName, engine, queryid)));
                return;
            }
            requireNonNull(userName, "Username must exist when auditing header name is enabled");
            db.single(Query.class).where("query_id = ? AND datasource = ? AND user = ?", queryid, datasource, userName).execute()
                    .orElseThrow(() -> new RuntimeException(format("Cannot find query id (%s) for publish", queryid)));
            writeJSON(response, Map.of("publish_id", publishQuery(datasource, userName, engine, queryid)));
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            writeJSON(response, Map.of("error", e.getMessage()));
        }
    }

    private String publishQuery(String datasource, String userName, String engine, String queryid) {
        Optional<Publish> publishedQuery = db.single(Publish.class).where("datasource = ? AND engine = ? AND query_id = ?", datasource, engine, queryid).execute();
        if (publishedQuery.isPresent()) {
            return publishedQuery.get().getPublishId();
        }
        String publishId = md5Hex(datasource + ";" + engine + ";" + queryid);
        db.insert(Publish.class)
                .value("publish_id", publishId)
                .value("datasource", datasource)
                .value("engine", engine)
                .value("query_id", queryid)
                .value("user", userName)
                .execute();
        return publishId;
    }
}
