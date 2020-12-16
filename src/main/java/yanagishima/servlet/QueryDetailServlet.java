package yanagishima.servlet;

import yanagishima.config.YanagishimaConfig;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static java.lang.String.format;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.inject.Injector;

@RestController
public class QueryDetailServlet {
  private final YanagishimaConfig config;

  @Inject
  public QueryDetailServlet(Injector injector) {
    this.config = injector.getInstance(YanagishimaConfig.class);
  }

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
