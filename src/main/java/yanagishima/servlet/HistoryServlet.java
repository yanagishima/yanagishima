package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Query;
import yanagishima.util.JsonUtil;
import yanagishima.util.PathUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

@Singleton
public class HistoryServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory
            .getLogger(HistoryServlet.class);

    private static final long serialVersionUID = 1L;

    @Inject
    private TinyORM db;

    private YanagishimaConfig yanagishimaConfig;

    @Inject
    public HistoryServlet(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<String, Object>();

        try {
            Optional<String> queryidOptional = Optional.ofNullable(request.getParameter("queryid"));
            queryidOptional.ifPresent(queryid -> {

                try {
                    Optional<Query> queryOptional = db.single(Query.class).where("query_id=?", queryid).execute();
                    queryOptional.ifPresent(query -> {
                        String queryString = query.getQueryString();
                        retVal.put("queryString", queryString);

                        int limit = yanagishimaConfig.getSelectLimit();
                        List<List<String>> rowDataList = new ArrayList<List<String>>();
                        try (BufferedReader br = Files.newBufferedReader(PathUtil.getResultFilePath(queryid), StandardCharsets.UTF_8)) {
                            String line = br.readLine();
                            int lineNumber = 0;
                            while (line != null) {
                                if (lineNumber == 0) {
                                    String[] columns = line.split("\t");
                                    retVal.put("headers", Arrays.asList(columns));
                                } else {
                                    if (queryString.toLowerCase().startsWith("show") || lineNumber <= limit) {
                                        String[] row = line.split("\t");
                                        rowDataList.add(Arrays.asList(row));
                                    } else {
                                        String warningMessage = String.format("now fetch size is %d. This is more than %d. So, fetch operation stopped.", rowDataList.size(), limit);
                                        retVal.put("warn", warningMessage);
                                        break;
                                    }
                                }
                                lineNumber++;
                                line = br.readLine();
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        retVal.put("results", rowDataList);

                    });
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }

}
