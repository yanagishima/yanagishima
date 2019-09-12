package yanagishima.filter;

import yanagishima.util.Constants;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
    public void doFilter(ServletRequest request, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        if(corsEnabled) {
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            if(auditHttpHeaderName == null) {
                response.setHeader("Access-Control-Allow-Headers", Constants.DATASOURCE_HEADER);
            } else {
                response.setHeader("Access-Control-Allow-Headers", Constants.DATASOURCE_HEADER + ", " + auditHttpHeaderName);
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
