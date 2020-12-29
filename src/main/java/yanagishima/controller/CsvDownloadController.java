package yanagishima.controller;

import static java.util.Objects.requireNonNull;
import static yanagishima.util.DownloadUtil.downloadCsv;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.config.YanagishimaConfig;
import yanagishima.model.db.Query;
import yanagishima.service.QueryService;

@RestController
@RequiredArgsConstructor
public class CsvDownloadController {
  private final QueryService queryService;
  private final YanagishimaConfig config;

  @DatasourceAuth
  @GetMapping("csvdownload")
  public void get(@RequestParam String datasource,
                  @RequestParam(name = "queryid", required = false) String queryId,
                  @RequestParam(defaultValue = "UTF-8") String encode,
                  @RequestParam(defaultValue = "true") boolean header,
                  @RequestParam(defaultValue = "true") boolean bom,
                  HttpServletRequest request, HttpServletResponse response) {
    if (queryId == null) {
      return;
    }

    String fileName = queryId + ".csv";
    if (config.isAllowOtherReadResult(datasource)) {
      downloadCsv(response, fileName, datasource, queryId, encode, header, bom);
      return;
    }
    String user = request.getHeader(config.getAuditHttpHeaderName());
    requireNonNull(user, "user is null");
    Optional<Query> userQuery = queryService.get(queryId, datasource, user);
    if (userQuery.isPresent()) {
      downloadCsv(response, fileName, datasource, queryId, encode, header, bom);
    }
  }
}
