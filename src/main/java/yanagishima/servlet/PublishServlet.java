package yanagishima.servlet;

import io.airlift.units.DataSize;
import me.geso.tinyorm.TinyORM;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Publish;
import yanagishima.row.Query;
import yanagishima.util.HistoryUtil;
import yanagishima.util.JsonUtil;
import yanagishima.util.PathUtil;

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

@Singleton
public class PublishServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory
            .getLogger(PublishServlet.class);

    private static final long serialVersionUID = 1L;

    @Inject
    private TinyORM db;

    @Override
    protected void doPost(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<String, Object>();

        try {
            String datasource = Optional.ofNullable(request.getParameter("datasource")).get();
            String queryid = Optional.ofNullable(request.getParameter("queryid")).get();
            Optional<Publish> publishOptional = db.single(Publish.class).where("datasource=? and query_id=?", datasource, queryid).execute();
            if(publishOptional.isPresent()) {
                retVal.put("publish_id", publishOptional.get().getPublishId());
            } else {
                String publish_id = DigestUtils.md5Hex(datasource + ";" + queryid);
                db.insert(Publish.class)
                        .value("publish_id", publish_id)
                        .value("datasource", datasource)
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
