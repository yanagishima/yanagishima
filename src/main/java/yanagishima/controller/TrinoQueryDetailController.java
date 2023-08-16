package yanagishima.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.config.YanagishimaConfig;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.lang.String.format;

@Api(tags = "query")
@RestController
@RequiredArgsConstructor
public class TrinoQueryDetailController {
  private final YanagishimaConfig config;

  @DatasourceAuth
  @GetMapping("trinoQueryDetail")
  public void get(@RequestParam String datasource, @RequestParam String queryid,
                  HttpServletResponse response) throws IOException {
    String redirectServer = config.getTrinoRedirectServer(datasource);
    response.sendRedirect(format("%s/query.html?%s", redirectServer, queryid));
  }
}
