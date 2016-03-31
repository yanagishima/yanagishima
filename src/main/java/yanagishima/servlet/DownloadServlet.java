package yanagishima.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
                String currentPath = new File(".").getAbsolutePath();
                String yyyymmdd = queryid.substring(0, 8);
                Path src = Paths.get(String.format("%s/result/%s/%s.tsv", currentPath, yyyymmdd, queryid));
                try (InputStream in = Files.newInputStream(src)) {
                    byte[] buff = new byte[1024];
                    int len = 0;
                    while ((len = in.read(buff, 0, buff.length)) != -1) {
                        out.write(buff, 0, len);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

}
