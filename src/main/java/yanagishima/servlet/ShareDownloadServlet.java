package yanagishima.servlet;

import yanagishima.repository.TinyOrm;

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

    private final TinyOrm db;

    @Inject
    public ShareDownloadServlet(TinyOrm db) {
        this.db = db;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        Optional<String> publishId = Optional.ofNullable(request.getParameter("publish_id"));
        if (publishId.isEmpty()) {
            return;
        }

        db.singlePublish("publish_id=?", publishId.get()).ifPresent(publish -> {
            String encode = getOrDefaultParameter(request, "encode", "UTF-8");
            boolean showHeader = getOrDefaultParameter(request, "header", true);
            boolean showBOM = getOrDefaultParameter(request, "bom", true);
            String fileName = publishId.get() + ".tsv";
            downloadTsv(response, fileName, publish.getDatasource(), publish.getQueryId(), encode, showHeader, showBOM);
        });
    }
}
