package yanagishima.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import yanagishima.config.YanagishimaConfig;
import yanagishima.util.Constants;

@Component
@RequiredArgsConstructor
public class YanagishimaFilter implements Filter {
  private final YanagishimaConfig config;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // No-op
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse servletResponse, FilterChain chain)
      throws IOException, ServletException {
    if (config.corsEnabled()) {
      HttpServletResponse response = (HttpServletResponse) servletResponse;
      if (config.getAuditHttpHeaderName() == null) {
        response.setHeader("Access-Control-Allow-Headers", Constants.DATASOURCE_HEADER);
      } else {
        response.setHeader("Access-Control-Allow-Headers",
                           Constants.DATASOURCE_HEADER + ", " + config.getAuditHttpHeaderName());
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
