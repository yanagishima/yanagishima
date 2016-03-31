package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Query;
import yanagishima.util.JsonUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
                setQueryString(retVal, queryid);
                setResults(retVal, queryid);
            });
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }

    private void setResults(HashMap<String, Object> retVal, String queryid) {
        int limit = yanagishimaConfig.getSelectLimit();
        String currentPath = new File(".").getAbsolutePath();
        String yyyymmdd = queryid.substring(0, 8);
        Path src = Paths.get(String.format("%s/result/%s/%s.tsv", currentPath, yyyymmdd, queryid));
        List<List<String>> rowDataList = new ArrayList<List<String>>();
        try (BufferedReader br = Files.newBufferedReader(src, StandardCharsets.UTF_8)) {
            String line = br.readLine();
            int lineNumber = 0;
            while (line != null) {
                if (lineNumber == 0) {
                    String[] columns = line.split("\t");
                    retVal.put("headers", Arrays.asList(columns));
                } else {
                    if (lineNumber <= limit) {
                        String[] row = line.split("\t");
                        rowDataList.add(Arrays.asList(row));
                    } else {
                        String warningMessage = String.format("now fetch size is %d. This is more than %d. So, fetch operation stopped.", rowDataList.size(), limit);
                        retVal.put("warn", warningMessage);
                    }
                }
                lineNumber++;
                line = br.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        retVal.put("results", rowDataList);
    }

    private void setQueryString(HashMap<String, Object> retVal, String queryid) {
        try {
            Optional<Query> queryOptional = db.single(Query.class).where("query_id=?", queryid).execute();
            queryOptional.ifPresent(query -> {
                retVal.put("queryString", query.getQueryString());
            });
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
