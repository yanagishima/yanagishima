package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Label;
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

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;

@Singleton
public class LabelServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(LabelServlet.class);
    private final YanagishimaConfig yanagishimaConfig;
    @Inject
    private TinyORM db;

    @Inject
    public LabelServlet(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<>();

        try {
            String datasource = getRequiredParameter(request, "datasource");
            if (yanagishimaConfig.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return;
            }

            String engine = getRequiredParameter(request, "engine");
            String queryid = getRequiredParameter(request, "queryid");
            String labelName = getRequiredParameter(request, "labelName");

            int count = db.insert(Label.class).value("datasource", datasource)
                    .value("engine", engine)
                    .value("query_id", queryid)
                    .value("label_name", labelName)
                    .execute();

            retVal.put("datasource", datasource);
            retVal.put("engine", engine);
            retVal.put("queryid", queryid);
            retVal.put("labelName", labelName);
            retVal.put("count", count);

        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<>();

        try {
            String datasource = getRequiredParameter(request, "datasource");
            if (yanagishimaConfig.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return;
            }

            String engine = getRequiredParameter(request, "engine");
            String queryid = getRequiredParameter(request, "queryid");
            Optional<Label> optionalLabel = db.single(Label.class).where("datasource = ? and engine = ? and query_id = ?", datasource, engine, queryid).execute();
            if (optionalLabel.isPresent()) {
                retVal.put("label", optionalLabel.get().getLabelName());
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

        HashMap<String, Object> retVal = new HashMap<>();

        try {
            String datasource = getRequiredParameter(request, "datasource");
            if (yanagishimaConfig.isCheckDatasource()) {
                if (!validateDatasource(request, datasource)) {
                    try {
                        response.sendError(SC_FORBIDDEN);
                        return;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            String engine = getRequiredParameter(request, "engine");
            String queryid = getRequiredParameter(request, "queryid");
            Optional<Label> optionaldeletedLabel = db.single(Label.class).where("datasource = ? and engine = ? and query_id = ?", datasource, engine, queryid).execute();
            if (optionaldeletedLabel.isPresent()) {
                optionaldeletedLabel.get().delete();;
            }
            retVal.put("datasource", datasource);
            retVal.put("engine", engine);
            retVal.put("queryid", queryid);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }

}
