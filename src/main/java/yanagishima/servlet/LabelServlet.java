package yanagishima.servlet;

import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
import yanagishima.model.db.Label;
import yanagishima.service.LabelService;

@Slf4j
@Deprecated
@RestController
@RequiredArgsConstructor
public class LabelServlet {
    private final LabelService labelService;
    private final YanagishimaConfig config;

    @PostMapping("label")
    public Map<String, Object> post(@RequestParam String datasource,
                                    @RequestParam String engine,
                                    @RequestParam(name = "queryid") String queryId,
                                    @RequestParam String labelName,
                                    HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> responseBody = new HashMap<>();
        try {
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return responseBody;
            }

            labelService.insert(datasource, engine, queryId, labelName);

            responseBody.put("datasource", datasource);
            responseBody.put("engine", engine);
            responseBody.put("queryid", queryId);
            responseBody.put("labelName", labelName);
            responseBody.put("count", 1);

        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        return responseBody;
    }

    @GetMapping("label")
    public Map<String, Object> get(@RequestParam String datasource,
                                   @RequestParam String engine,
                                   @RequestParam(name = "queryid") String queryId,
                                   HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> responseBody = new HashMap<>();

        try {
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return responseBody;
            }

            Optional<Label> optionalLabel = labelService.get(datasource, engine, queryId);
            if (optionalLabel.isPresent()) {
                responseBody.put("label", optionalLabel.get().getLabelName());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        return responseBody;
    }

    @DeleteMapping("label")
    public Map<String, Object> delete(@RequestParam String datasource,
                                      @RequestParam String engine,
                                      @RequestParam(name = "queryid") String queryId,
                                      HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> responseBody = new HashMap<>();
        try {
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return responseBody;
            }

            labelService.get(datasource, engine, queryId).ifPresent(labelService::delete);
            responseBody.put("datasource", datasource);
            responseBody.put("engine", engine);
            responseBody.put("queryid", queryId);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        return responseBody;
    }
}
