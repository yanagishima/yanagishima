package yanagishima.servlet;

import yanagishima.config.YanagishimaConfig;
import yanagishima.repository.TinyOrm;
import yanagishima.model.db.Query;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.DownloadUtil.downloadTsv;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.inject.Injector;

@RestController
public class DownloadServlet {
  private final YanagishimaConfig config;
  private final TinyOrm db;

  @Inject
  public DownloadServlet(Injector injector) {
    this.config = injector.getInstance(YanagishimaConfig.class);
    this.db = injector.getInstance(TinyOrm.class);
  }

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

    if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
      sendForbiddenError(response);
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
