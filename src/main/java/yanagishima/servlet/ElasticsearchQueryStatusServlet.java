package yanagishima.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Query;
import yanagishima.util.AccessControlUtil;
import yanagishima.util.HttpRequestUtil;
import yanagishima.util.Status;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Singleton
public class ElasticsearchQueryStatusServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchQueryStatusServlet.class);

    private static final long serialVersionUID = 1L;

    private YanagishimaConfig yanagishimaConfig;

    @Inject
    private TinyORM db;

    @Inject
    public ElasticsearchQueryStatusServlet(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

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

        String queryid = HttpRequestUtil.getParam(request, "queryid");
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        Map map = new HashMap<String, String>();
        Optional<Query> queryOptional = db.single(Query.class).where("query_id=? and datasource=? and engine=?", queryid, datasource, "elasticsearch").execute();
        if (queryOptional.isPresent()) {
            if (queryOptional.get().getStatus().equals(Status.SUCCEED.name())) {
                map.put("state", "FINISHED");
            } else if (queryOptional.get().getStatus().equals(Status.FAILED.name())) {
                map.put("state", "FAILED");
            } else {
                throw new IllegalArgumentException(String.format("unknown status=%s", queryOptional.get().getStatus()));
            }
        } else {
            map.put("state", "RUNNING");
        }
        ObjectMapper mapper = new ObjectMapper();
        writer.println(mapper.writeValueAsString(map));
    }

}
