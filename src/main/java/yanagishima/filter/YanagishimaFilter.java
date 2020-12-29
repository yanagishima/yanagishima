package yanagishima.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import yanagishima.util.Constants;

public class YanagishimaFilter implements Filter {
  private final boolean corsEnabled;
  private final String auditHttpHeaderName;

  public YanagishimaFilter(boolean corsEnabled, String auditHttpHeaderName) {
    this.corsEnabled = corsEnabled;
    this.auditHttpHeaderName = auditHttpHeaderName;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // No-op
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse servletResponse, FilterChain chain)
      throws IOException, ServletException {
    if (corsEnabled) {
      HttpServletResponse response = (HttpServletResponse) servletResponse;
      if (auditHttpHeaderName == null) {
        response.setHeader("Access-Control-Allow-Headers", Constants.DATASOURCE_HEADER);
      } else {
        response.setHeader("Access-Control-Allow-Headers",
                           Constants.DATASOURCE_HEADER + ", " + auditHttpHeaderName);
      }
      response.setHeader("Access-Control-Allow-Origin", "*");
      response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE");
    }
    chain.doFilter(request, servletResponse);
  }

  @Override
  public void destroy() {
    // No-op
  }
}
