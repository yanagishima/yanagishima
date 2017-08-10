package yanagishima.servlet;

import yanagishima.config.YanagishimaConfig;
import yanagishima.util.Constants;
import yanagishima.util.HttpRequestUtil;
import yanagishima.util.JsonUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class DatasourceServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private YanagishimaConfig yanagishimaConfig;

    @Inject
    public DatasourceServlet(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<String, Object>();

        Optional<String> engineOptional = Optional.ofNullable(request.getParameter("engine"));
        if (engineOptional.isPresent()) {
            String engine = engineOptional.get();
            if (yanagishimaConfig.isCheckDatasource()) {
                String header = HttpRequestUtil.getHeader(request, Constants.DATASOURCE_HEADER);
                if (header.equals("*")) {
                    retVal.put("datasources", yanagishimaConfig.getDatasources(engine));
                } else {
                    List<String> headerDatasources = Arrays.asList(header.split(","));
                    retVal.put("datasources", yanagishimaConfig.getDatasources(engine).stream().filter(datasource -> headerDatasources.contains(datasource)).collect(Collectors.toList()));
                }
            } else {
                retVal.put("datasources", yanagishimaConfig.getDatasources(engine));
            }
        } else {
            if (yanagishimaConfig.isCheckDatasource()) {
                String header = HttpRequestUtil.getHeader(request, Constants.DATASOURCE_HEADER);
                if (header.equals("*")) {
                    retVal.put("datasources", yanagishimaConfig.getDatasources());
                } else {
                    List<String> headerDatasources = Arrays.asList(header.split(","));
                    retVal.put("datasources", yanagishimaConfig.getDatasources().stream().filter(datasource -> headerDatasources.contains(datasource)).collect(Collectors.toList()));
                }
            } else {
                retVal.put("datasources", yanagishimaConfig.getDatasources());
            }
        }

        JsonUtil.writeJSON(response, retVal);
    }

}
