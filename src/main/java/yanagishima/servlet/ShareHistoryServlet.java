package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Publish;
import yanagishima.row.Query;
import yanagishima.util.HistoryUtil;
import yanagishima.util.JsonUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

@Singleton
public class ShareHistoryServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory
            .getLogger(ShareHistoryServlet.class);

    private static final long serialVersionUID = 1L;

    @Inject
    private TinyORM db;

    private YanagishimaConfig yanagishimaConfig;

    @Inject
    public ShareHistoryServlet(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<String, Object>();

        try {
            String publishId = Optional.ofNullable(request.getParameter("publish_id")).get();
            Optional<Publish> publishOptional = db.single(Publish.class).where("publish_id=?", publishId).execute();
            if(publishOptional.isPresent()) {
                String datasource = publishOptional.get().getDatasource();
                String queryid = publishOptional.get().getQueryId();
                Query query = db.single(Query.class).where("query_id=? and datasource=?", queryid, datasource).execute().get();
                retVal.put("engine", query.getEngine());
                HistoryUtil.createHistoryResult(retVal, yanagishimaConfig.getSelectLimit(), datasource, queryid, query.getQueryString(), query.getFetchResultTimeString());
            }
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }



}
