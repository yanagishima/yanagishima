package yanagishima.servlet;

import static java.lang.String.format;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import yanagishima.config.YanagishimaConfig;
import yanagishima.model.db.Query;
import yanagishima.model.dto.ElasticsearchQueryStatusDto;
import yanagishima.repository.TinyOrm;
import yanagishima.util.Status;

@Api(tags = "elasticsearch")
@RestController
@RequiredArgsConstructor
public class ElasticsearchQueryStatusServlet {
    private final YanagishimaConfig config;
    private final TinyOrm db;

    @PostMapping("elasticsearchQueryStatus")
    public ElasticsearchQueryStatusDto post(@RequestParam String datasource,
                                            @RequestParam(name = "queryid") String queryId,
                                            HttpServletRequest request, HttpServletResponse response) {
        ElasticsearchQueryStatusDto elasticsearchQueryStatusDto = new ElasticsearchQueryStatusDto();
        if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
            sendForbiddenError(response);
            return elasticsearchQueryStatusDto;
        }

        Optional<Query> query = db.singleQuery("query_id=? and datasource=? and engine=?", queryId, datasource, "elasticsearch");
        elasticsearchQueryStatusDto.setState(getStatus(query));
        return elasticsearchQueryStatusDto;
    }

    private static String getStatus(Optional<Query> query) {
        if (query.isEmpty()) {
            return "RUNNING";
        }
        String status = query.get().getStatus();
        if (status.equals(Status.SUCCEED.name())) {
            return "FINISHED";
        }
        if (status.equals(Status.FAILED.name())) {
            return "FAILED";
        }
        throw new IllegalArgumentException(format("unknown status=%s", status));
    }
}
