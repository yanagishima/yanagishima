package yanagishima.filter;

import yanagishima.util.Constants;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class YanagishimaFilter implements Filter {

    private boolean corsEnabled;

    private String auditHttpHeaderName;

    public YanagishimaFilter(boolean corsEnabled, String auditHttpHeaderName) {
        this.corsEnabled = corsEnabled;
        this.auditHttpHeaderName = auditHttpHeaderName;
    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if(corsEnabled) {
            HttpServletResponse res = (HttpServletResponse) response;
            if(auditHttpHeaderName == null) {
                res.setHeader("Access-Control-Allow-Headers", Constants.DATASOURCE_HEADER);
            } else {
                res.setHeader("Access-Control-Allow-Headers", Constants.DATASOURCE_HEADER + ", " + auditHttpHeaderName);
            }
            res.setHeader("Access-Control-Allow-Origin", "*");
            res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE");
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
