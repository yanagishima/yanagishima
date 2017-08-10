package yanagishima.servlet;

import io.airlift.units.DataSize;
import me.geso.tinyorm.TinyORM;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Publish;
import yanagishima.row.Query;
import yanagishima.util.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Singleton
public class PublishServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory
            .getLogger(PublishServlet.class);

    private static final long serialVersionUID = 1L;

    @Inject
    private TinyORM db;

    private YanagishimaConfig yanagishimaConfig;

    @Inject
    public PublishServlet(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
    }

    @Override
    protected void doPost(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<String, Object>();

        try {
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
            String engine = HttpRequestUtil.getParam(request, "engine");
            String queryid = Optional.ofNullable(request.getParameter("queryid")).get();
            Optional<Publish> publishOptional = db.single(Publish.class).where("datasource=? and engine=? query_id=?", datasource, engine, queryid).execute();
            if(publishOptional.isPresent()) {
                retVal.put("publish_id", publishOptional.get().getPublishId());
            } else {
                String publish_id = DigestUtils.md5Hex(datasource + ";" + engine + ";" + queryid);
                db.insert(Publish.class)
                        .value("publish_id", publish_id)
                        .value("datasource", datasource)
                        .value("engine", engine)
                        .value("query_id", queryid)
                        .execute();
                retVal.put("publish_id", publish_id);
            }
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }

}
