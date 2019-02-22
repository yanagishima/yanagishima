package yanagishima.util;

import io.airlift.units.DataSize;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import yanagishima.row.Query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HistoryUtil {

    public static void createHistoryResult(HashMap<String, Object> retVal, int limit, String datasource, Query query) {
        String queryid = query.getQueryId();
        String queryString = query.getQueryString();
        retVal.put("queryString", queryString);
        retVal.put("finishedTime", query.getFetchResultTimeString());

        Path errorFilePath = PathUtil.getResultFilePath(datasource, queryid, true);
        if (errorFilePath.toFile().exists()) {
            try {
                retVal.put("error", String.join(System.getProperty("line.separator"), Files.readAllLines(errorFilePath)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            List<List<String>> rowDataList = new ArrayList<List<String>>();
            int lineNumber = 0;
            Path resultFilePath = PathUtil.getResultFilePath(datasource, queryid, false);
            if(!resultFilePath.toFile().exists()) {
                retVal.put("error", String.format("%s is not found", resultFilePath.getFileName()));
                return;
            }
            try (BufferedReader br = Files.newBufferedReader(resultFilePath, StandardCharsets.UTF_8)) {
                CSVParser parse = CSVFormat.EXCEL.withDelimiter('\t').withNullString("\\N").parse(br);
                for (CSVRecord csvRecord : parse) {
                    List<String> columnList = new ArrayList<>();
                    for(String column : csvRecord) {
                        columnList.add(column);
                    }
                    if (lineNumber == 0) {
                        retVal.put("headers", columnList);
                    } else {
                        if (queryString.toLowerCase().startsWith("show") || lineNumber <= limit) {
                            rowDataList.add(columnList);
                        } else {
                            break;
                        }
                    }
                    lineNumber++;

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            retVal.put("results", rowDataList);
            retVal.put("lineNumber", query.getLinenumber());

            retVal.put("elapsedTimeMillis", query.getElapsedTimeMillis());
            if(query.getResultFileSize() == null) {
                retVal.put("rawDataSize", null);
            } else {
                DataSize rawDataSize = new DataSize(query.getResultFileSize(), DataSize.Unit.BYTE);
                retVal.put("rawDataSize", rawDataSize.convertToMostSuccinctDataSize().toString());
            }

        }

    }

}
