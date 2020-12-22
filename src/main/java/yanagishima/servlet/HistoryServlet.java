package yanagishima.servlet;

import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HistoryUtil.createHistoryResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.config.YanagishimaConfig;
import yanagishima.model.db.Query;
import yanagishima.model.db.SessionProperty;
import yanagishima.repository.TinyOrm;
import yanagishima.service.SessionPropertyService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HistoryServlet {
    private final SessionPropertyService sessionPropertyService;
    private final YanagishimaConfig config;
    private final TinyOrm db;

    @GetMapping("history")
    public Map<String, Object> get(@RequestParam String datasource,
                                   @RequestParam(required = false) String engine,
                                   @RequestParam(name = "queryid", required = false) String queryId,
                                   HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> responseBody = new HashMap<>();
        if (queryId == null) {
            return responseBody;
        }

        try {
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return responseBody;
            }
            Optional<Query> queryOptional;
            if (engine == null) {
                queryOptional = db.singleQuery("query_id=? and datasource=?", queryId, datasource);
            } else {
                queryOptional = db.singleQuery("query_id=? and datasource=? and engine=?", queryId, datasource, engine);
            }

            String user = request.getHeader(config.getAuditHttpHeaderName());
            Optional<Query> userQueryOptional = db.singleQuery("query_id=? and datasource=? and user=?", queryId, datasource, user);
            responseBody.put("editLabel", userQueryOptional.isPresent());

            queryOptional.ifPresent(query -> {
                responseBody.put("engine", query.getEngine());
                boolean resultVisible;
                if (config.isAllowOtherReadResult(datasource)) {
                    resultVisible = true;
                } else {
                    resultVisible = userQueryOptional.isPresent();
                }
                List<SessionProperty> sessionPropertyList = sessionPropertyService.getAll(datasource, engine, queryId);
                createHistoryResult(responseBody, config.getSelectLimit(), datasource, query, resultVisible, sessionPropertyList);
            });
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        return responseBody;
    }
}
