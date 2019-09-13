package yanagishima.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.bean.HttpRequestContext;
import yanagishima.config.YanagishimaConfig;
import yanagishima.service.HiveService;
import yanagishima.util.AccessControlUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static yanagishima.util.JsonUtil.writeJSON;

@Singleton
public class HiveAsyncServlet extends HttpServlet {
    private static Logger LOGGER = LoggerFactory.getLogger(HiveAsyncServlet.class);
    private static final long serialVersionUID = 1L;

    private final YanagishimaConfig config;
    private final HiveService hiveService;

    @Inject
    public HiveAsyncServlet(YanagishimaConfig config, HiveService hiveService) {
        this.config = config;
        this.hiveService = hiveService;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpRequestContext context = new HttpRequestContext(request);
        HashMap<String, Object> retVal = new HashMap<>();

        Optional<String> queryOptional = Optional.ofNullable(context.getQuery());
        queryOptional.ifPresent(query -> {
            try {
                requireNonNull(context.getDatasource(), "datasource is null");
                requireNonNull(context.getEngine(), "engine is null");

                String userName = null;
                Optional<String> hiveUser = Optional.ofNullable(context.getUser());
                Optional<String> hivePassword = Optional.ofNullable(context.getPassword());
                if(config.isUseAuditHttpHeaderName()) {
                    userName = request.getHeader(config.getAuditHttpHeaderName());
                } else {
                    if (hiveUser.isPresent() && hivePassword.isPresent()) {
                        userName = hiveUser.get();
                    }
                }
                if (config.isUserRequired() && userName == null) {
                    try {
                        response.sendError(SC_FORBIDDEN);
                        return;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (config.isCheckDatasource()) {
                    if (!AccessControlUtil.validateDatasource(request, context.getDatasource())) {
                        try {
                            response.sendError(SC_FORBIDDEN);
                            return;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                if (userName != null) {
                    LOGGER.info(String.format("%s executed %s in datasource=%s, engine=%s", userName, query, context.getDatasource(), context.getEngine()));
                }
                String queryid = hiveService.doQueryAsync(context.getEngine(), context.getDatasource(), query, userName, hiveUser, hivePassword);
                retVal.put("queryid", queryid);
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
                retVal.put("error", e.getMessage());
            }
        });
        writeJSON(response, retVal);
    }
}
