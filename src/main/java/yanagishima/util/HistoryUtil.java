package yanagishima.util;

import static java.lang.String.format;
import static yanagishima.util.FormatUtil.toSuccinctDataSize;
import static yanagishima.util.PathUtil.getResultFilePath;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import lombok.experimental.UtilityClass;
import yanagishima.model.db.Query;
import yanagishima.model.db.SessionProperty;

@UtilityClass
public final class HistoryUtil {
  public static void createHistoryResult(Map<String, Object> responseBody, int limit, String datasource,
                                         Query query, boolean resultVisible,
                                         List<SessionProperty> sessionPropertyList) {
    String queryId = query.getQueryId();
    String queryString = query.getQueryString();
    responseBody.put("queryString", queryString);
    responseBody.put("finishedTime", query.getFetchResultTimeString());
    responseBody.put("lineNumber", query.getLinenumber());
    responseBody.put("elapsedTimeMillis", query.getElapsedTimeMillis());
    responseBody.put("rawDataSize", toSuccinctDataSize(query.getResultFileSize()));
    responseBody.put("userid", query.getUserid());

    Map<String, String> map = new HashMap<>();
    for (SessionProperty sessionProperty : sessionPropertyList) {
      map.put(sessionProperty.getSessionKey(), sessionProperty.getSessionValue());
    }
    responseBody.put("session_property", map);

    Path errorFilePath = getResultFilePath(datasource, queryId, true);
    if (errorFilePath.toFile().exists()) {
      try {
        responseBody.put("error",
                         String.join(System.getProperty("line.separator"), Files.readAllLines(errorFilePath)));
        return;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    if (!resultVisible) {
      responseBody.put("error", "you can't see query result which other submitted");
    }

    List<List<String>> rows = new ArrayList<>();
    int lineNumber = 0;
    Path resultFilePath = getResultFilePath(datasource, queryId, false);
    if (!resultFilePath.toFile().exists()) {
      responseBody.put("error", format("%s is not found", resultFilePath.getFileName()));
      return;
    }
    try (BufferedReader reader = Files.newBufferedReader(resultFilePath, StandardCharsets.UTF_8)) {
      CSVParser parser = CSVFormat.EXCEL.withDelimiter('\t').withNullString("\\N").parse(reader);
      for (CSVRecord record : parser) {
        List<String> row = new ArrayList<>();
        record.forEach(row::add);
        if (lineNumber == 0) {
          responseBody.put("headers", row);
        } else {
          if (queryString.toLowerCase().startsWith("show") || lineNumber <= limit) {
            rows.add(row);
          } else {
            break;
          }
        }
        lineNumber++;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    responseBody.put("results", rows);
  }
}
