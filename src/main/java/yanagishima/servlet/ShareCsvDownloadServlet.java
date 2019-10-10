package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import yanagishima.row.Publish;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static yanagishima.util.DownloadUtil.csvDownload;
import static yanagishima.util.HttpRequestUtil.getOrDefaultParameter;

@Singleton
public class ShareCsvDownloadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_ENCODE = "UTF-8";

    private final TinyORM db;

    @Inject
    public ShareCsvDownloadServlet(TinyORM db) {
        this.db = db;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        Optional<String> publishIdOptional = Optional.ofNullable(request.getParameter("publish_id"));
        if (publishIdOptional.isEmpty()) {
            return;
        }

        String publishId = publishIdOptional.get();
        db.single(Publish.class).where("publish_id=?", publishId).execute().ifPresent(publish -> {
            Optional<String> encode = Optional.ofNullable(request.getParameter("encode"));
            String fileName = publishId + ".csv";
            boolean showHeader = getOrDefaultParameter(request, "header", true);
            csvDownload(response, fileName, publish.getDatasource(), publish.getQueryId(), encode.orElse(DEFAULT_ENCODE), showHeader);
        });
    }
}
