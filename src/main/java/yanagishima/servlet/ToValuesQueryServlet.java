package yanagishima.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.util.JsonUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Singleton
public class ToValuesQueryServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory.getLogger(ToValuesQueryServlet.class);

    private static final long serialVersionUID = 1L;

    private final YanagishimaConfig yanagishimaConfig;

    @Inject
    public ToValuesQueryServlet(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<String, Object>();

        try {
            Optional<String> csvOptional = Optional.ofNullable(request.getParameter("csv"));
            csvOptional.ifPresent(csv -> {
                String[] lines = csv.split("\n");
                if (lines.length < 2) {
                    throw new RuntimeException("At least, there must be 2 lines");
                }
                if (lines.length > yanagishimaConfig.getToValuesQueryLimit()) {
                    throw new RuntimeException(String.format("At most, there must be %d lines", yanagishimaConfig.getToValuesQueryLimit()));
                }
                int lineNumber = 0;
                List<String> rows = new ArrayList<>();
                for (String line : lines) {
                    if (lineNumber != 0) {
                        rows.add("(" + line + ")");
                    }
                    lineNumber++;
                }
                String valuesQuery = String.format("SELECT * FROM ( VALUES\n%s ) AS t (%s)", String.join(",\n", rows), lines[0]);
                LOGGER.info(String.format("query=%s", valuesQuery));
                retVal.put("query", valuesQuery);
            });
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }

}
