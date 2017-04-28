package yanagishima.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static Logger LOGGER = LoggerFactory.getLogger(DatasourceServlet.class);

    private static final long serialVersionUID = 1L;

    private YanagishimaConfig yanagishimaConfig;

    private static final int LIMIT = 100;

    @Inject
    public DatasourceServlet(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<String, Object>();


//        String header = HttpRequestUtil.getHeader(request, Constants.DATASOURCE_HEADER);
//        if(header.equals("*")) {
//            retVal.put("datasources", yanagishimaConfig.getDatasources());
//        } else {
//            List<String> headerDatasources = Arrays.asList(header.split(","));
//            retVal.put("datasources", yanagishimaConfig.getDatasources().stream().filter(datasource -> headerDatasources.contains(datasource)).collect(Collectors.toList()));
//        }

        retVal.put("datasources", yanagishimaConfig.getDatasources());

        JsonUtil.writeJSON(response, retVal);
    }

}
