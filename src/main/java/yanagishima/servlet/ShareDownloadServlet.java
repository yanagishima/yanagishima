package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.row.Publish;
import yanagishima.util.PathUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Optional;

@Singleton
public class ShareDownloadServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory.getLogger(ShareDownloadServlet.class);

    private static final long serialVersionUID = 1L;

    @Inject
    private TinyORM db;

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        Optional<String> publishIdOptional = Optional.ofNullable(request.getParameter("publish_id"));
        publishIdOptional.ifPresent(publishId -> {
            String fileName = publishId + ".tsv";
            Optional<Publish> publishOptional = db.single(Publish.class).where("publish_id=?", publishId).execute();
            publishOptional.ifPresent(publish -> {
                String datasource = publishOptional.get().getDatasource();
                String queryid = publishOptional.get().getQueryId();

                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
                try (OutputStream out = response.getOutputStream()) {
                    try (InputStream in = Files.newInputStream(PathUtil.getResultFilePath(datasource, queryid, false))) {
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
        });
    }

}
