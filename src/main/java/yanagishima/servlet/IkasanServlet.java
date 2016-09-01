package yanagishima.servlet;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.util.JsonUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

@Singleton
public class IkasanServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory.getLogger(IkasanServlet.class);

    private static final long serialVersionUID = 1L;

    private final YanagishimaConfig yanagishimaConfig;

    @Inject
    public IkasanServlet(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<String, Object>();

        Optional<String> queryidOptional = Optional.ofNullable(request.getParameter("queryid"));
        queryidOptional.ifPresent(queryid -> {
            try {
                HttpResponse httpResponse = Request.Post(yanagishimaConfig.getIkasanUrl() + "/notice").bodyForm(Form.form().add("channel", yanagishimaConfig.getIkasanChannel())
                        .add("nickname", "yanagishima")
                        .add("color", "green")
                        .add("message", yanagishimaConfig.getPrestoRedirectServer() + "query.html?" + queryid)
                        .build()).execute().returnResponse();

                if(httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new RuntimeException("http status code is not 200");
                }
            } catch (IOException e) {
                retVal.put("error", e.getCause().getMessage());
            }
        });

        JsonUtil.writeJSON(response, retVal);

    }

}
