package yanagishima.servlet;

import static java.lang.String.format;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import yanagishima.config.YanagishimaConfig;

@Api(tags = "query")
@RestController
@RequiredArgsConstructor
public class QueryDetailServlet {
  private final YanagishimaConfig config;

  @GetMapping("queryDetail")
  public void get(@RequestParam String datasource, @RequestParam String queryid,
                  HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
      sendForbiddenError(response);
      return;
    }
    String redirectServer = config.getPrestoRedirectServer(datasource);
    response.sendRedirect(format("%s/query.html?%s", redirectServer, queryid));
  }
}
