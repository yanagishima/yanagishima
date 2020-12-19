package yanagishima.servlet;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static yanagishima.repository.TinyOrm.value;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;

import java.util.Optional;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.config.YanagishimaConfig;
import yanagishima.model.db.Publish;
import yanagishima.model.dto.PublishDto;
import yanagishima.repository.TinyOrm;

@Slf4j
@Api(tags = "publish")
@RestController
@RequiredArgsConstructor
public class PublishServlet extends HttpServlet {
  private final YanagishimaConfig config;
  private final TinyOrm db;

  @PostMapping("publish")
  public PublishDto post(@RequestParam String datasource, @RequestParam String engine, @RequestParam String queryid,
                         HttpServletRequest request, HttpServletResponse response) {
    PublishDto publishDto = new PublishDto();
    if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
      sendForbiddenError(response);
      return publishDto;
    }

    try {
      String userName = request.getHeader(config.getAuditHttpHeaderName());
      if (config.isAllowOtherReadResult(datasource)) {
        publishDto.setPublishId(publishQuery(datasource, userName, engine, queryid));
        return publishDto;
      }
      requireNonNull(userName, "Username must exist when auditing header name is enabled");
      db.singleQuery("query_id = ? AND datasource = ? AND user = ?", queryid, datasource, userName)
        .orElseThrow(() -> new RuntimeException(format("Cannot find query id (%s) for publish", queryid)));
      publishDto.setPublishId(publishQuery(datasource, userName, engine, queryid));
      return publishDto;
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      publishDto.setError(e.getMessage());
    }
    return publishDto;
  }

  private String publishQuery(String datasource, String userName, String engine, String queryid) {
    Optional<Publish> publishedQuery = db.singlePublish("datasource = ? AND engine = ? AND query_id = ?", datasource, engine, queryid);
    if (publishedQuery.isPresent()) {
      return publishedQuery.get().getPublishId();
    }
    String publishId = md5Hex(datasource + ";" + engine + ";" + queryid);
    db.insert(Publish.class,
              value("publish_id", publishId),
              value("datasource", datasource),
              value("engine", engine),
              value("query_id", queryid),
              value("user", userName));
    return publishId;
  }
}
