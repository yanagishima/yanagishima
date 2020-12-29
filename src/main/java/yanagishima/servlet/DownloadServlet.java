package yanagishima.servlet;

import static java.util.Objects.requireNonNull;
import static yanagishima.util.DownloadUtil.downloadTsv;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.config.YanagishimaConfig;
import yanagishima.model.db.Query;
import yanagishima.repository.TinyOrm;

@Api(tags = "download")
@RestController
@RequiredArgsConstructor
public class DownloadServlet {
  private final YanagishimaConfig config;
  private final TinyOrm db;

  @DatasourceAuth
  @GetMapping("download")
  public void get(@RequestParam String datasource,
                  @RequestParam(required = false) String queryid,
                  @RequestParam(defaultValue = "UTF-8") String encode,
                  @RequestParam(defaultValue = "true") boolean header,
                  @RequestParam(defaultValue = "true") boolean bom,
                  HttpServletRequest request, HttpServletResponse response) {
    if (queryid == null) {
      return;
    }

    String fileName = queryid + ".tsv";
    if (config.isAllowOtherReadResult(datasource)) {
      downloadTsv(response, fileName, datasource, queryid, encode, header, bom);
      return;
    }
    String user = request.getHeader(config.getAuditHttpHeaderName());
    requireNonNull(user, "Username must exist when auditing header name is enabled");
    Optional<Query> query = db.singleQuery("query_id = ? AND datasource = ? AND user = ?",
                                           queryid, datasource, user);
    if (query.isPresent()) {
      downloadTsv(response, fileName, datasource, queryid, encode, header, bom);
    }
  }
}
