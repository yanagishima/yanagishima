package yanagishima.servlet;

import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;

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
import yanagishima.model.dto.HistoryStatusDto;
import yanagishima.repository.TinyOrm;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HistoryStatusServlet {
    private final YanagishimaConfig config;
    private final TinyOrm db;

    @GetMapping("historyStatus")
    public HistoryStatusDto get(@RequestParam String datasource,
                                @RequestParam(required = false) String engine,
                                @RequestParam(name = "queryid", required = false) String queryId,
                                HttpServletRequest request, HttpServletResponse response) {
        HistoryStatusDto historyStatusDto = new HistoryStatusDto();
        historyStatusDto.setStatus("ng");
        if (queryId == null) {
            return historyStatusDto;
        }
        if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
            sendForbiddenError(response);
            return historyStatusDto;
        }

        try {
            findQuery(datasource, engine, queryId).ifPresent(query -> historyStatusDto.setStatus("ok"));
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            historyStatusDto.setError(e.getMessage());
        }
        return historyStatusDto;
    }

    private Optional<Query> findQuery(String datasource, String engine, String queryId) {
        if (engine == null) {
            return db.singleQuery("query_id=? and datasource=?", queryId, datasource);
        }
        return db.singleQuery("query_id=? and datasource=? and engine=?", queryId, datasource, engine);
    }
}
