package yanagishima.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.util.PathUtil;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.util.Optional;

@Singleton
public class CsvDownloadServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory.getLogger(CsvDownloadServlet.class);

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        Optional<String> queryidOptional = Optional.ofNullable(request.getParameter("queryid"));
        queryidOptional.ifPresent(queryid -> {
            String fileName = queryid + ".csv";
            response.setContentType("text/csv; charset=Shift_JIS");
            response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
            try (OutputStream out = response.getOutputStream()) {
                try(BufferedReader br = Files.newBufferedReader(PathUtil.getResultFilePath(queryid, false))) {
                    br.lines().forEach(line -> {
                        try {
                            out.write((line.replaceAll("\t", ",") + System.getProperty("line.separator")).getBytes("Shift_JIS"));
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
