package yanagishima.controller;

import static java.lang.String.format;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.config.YanagishimaConfig;

@Api(tags = "query")
@RestController
@RequiredArgsConstructor
public class PrestoQueryDetailController {
  private final YanagishimaConfig config;

  @DatasourceAuth
  @GetMapping("prestoQueryDetail")
  public void get(@RequestParam String datasource, @RequestParam String queryid,
                  HttpServletResponse response) throws IOException {
    String redirectServer = config.getPrestoRedirectServer(datasource);
    response.sendRedirect(format("%s/query.html?%s", redirectServer, queryid));
  }
}
