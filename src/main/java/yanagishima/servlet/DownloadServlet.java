package yanagishima.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.util.AccessControlUtil;
import yanagishima.util.HttpRequestUtil;
import yanagishima.util.PathUtil;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

@Singleton
public class DownloadServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory.getLogger(DownloadServlet.class);

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        Optional<String> queryidOptional = Optional.ofNullable(request.getParameter("queryid"));
        queryidOptional.ifPresent(queryid -> {
            String fileName = queryid + ".tsv";
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
            try (OutputStream out = response.getOutputStream()) {
                String datasource = HttpRequestUtil.getParam(request, "datasource");
                AccessControlUtil.checkDatasource(request, datasource);
                try(BufferedReader br = Files.newBufferedReader(PathUtil.getResultFilePath(datasource, queryid, false))) {
                    br.lines().forEach(line -> {
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            List row = mapper.readValue(line, List.class);
                            out.write((String.join("\t", row) + System.getProperty("line.separator")).getBytes("UTF-8"));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

}
