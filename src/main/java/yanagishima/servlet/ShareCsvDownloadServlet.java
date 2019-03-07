package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.row.Publish;
import yanagishima.util.DownloadUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Singleton
public class ShareCsvDownloadServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory.getLogger(ShareCsvDownloadServlet.class);

    private static final long serialVersionUID = 1L;

    @Inject
    private TinyORM db;

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        Optional<String> publishIdOptional = Optional.ofNullable(request.getParameter("publish_id"));
        publishIdOptional.ifPresent(publishId -> {
            String fileName = publishId + ".csv";
            Optional<Publish> publishOptional = db.single(Publish.class).where("publish_id=?", publishId).execute();
            publishOptional.ifPresent(publish -> {
                String datasource = publishOptional.get().getDatasource();
                String queryid = publishOptional.get().getQueryId();
                Optional<String> encodeOptional = Optional.ofNullable(request.getParameter("encode"));
                String header = request.getParameter("header");
                if(header != null && header.equals("false")) {
                    DownloadUtil.csvDownload(response, fileName, datasource, queryid, encodeOptional.orElse("UTF-8"), false);
                } else {
                    DownloadUtil.csvDownload(response, fileName, datasource, queryid, encodeOptional.orElse("UTF-8"), true);
                }
            });
        });
    }

}
