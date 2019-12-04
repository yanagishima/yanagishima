package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import yanagishima.row.Publish;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static yanagishima.util.DownloadUtil.downloadTsv;
import static yanagishima.util.HttpRequestUtil.getOrDefaultParameter;

@Singleton
public class ShareDownloadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_ENCODE = "UTF-8";

    private final TinyORM db;

    @Inject
    public ShareDownloadServlet(TinyORM db) {
        this.db = db;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        Optional<String> publishId = Optional.ofNullable(request.getParameter("publish_id"));
        if (publishId.isEmpty()) {
            return;
        }

        db.single(Publish.class).where("publish_id=?", publishId.get()).execute().ifPresent(publish -> {
            Optional<String> encode = Optional.ofNullable(request.getParameter("encode"));
            boolean showHeader = getOrDefaultParameter(request, "header", true);
            boolean showBOM = getOrDefaultParameter(request, "bom", true);
            String fileName = publishId.get() + ".tsv";
            downloadTsv(response, fileName, publish.getDatasource(), publish.getQueryId(), encode.orElse(DEFAULT_ENCODE), showHeader, showBOM);
        });
    }
}
