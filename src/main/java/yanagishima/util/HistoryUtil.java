package yanagishima.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.airlift.units.DataSize;

import java.io.BufferedReader;
import java.io.IOException;
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

    public static void createHistoryResult(HashMap<String, Object> retVal, int limit, String datasource, String queryid, String queryString, String fetchResultTimeString) {
        retVal.put("queryString", queryString);
        retVal.put("finishedTime", fetchResultTimeString);

        Path errorFilePath = PathUtil.getResultFilePath(datasource, queryid, true);
        if (errorFilePath.toFile().exists()) {
            try (BufferedReader br = Files.newBufferedReader(errorFilePath, StandardCharsets.UTF_8)) {
                String line = br.readLine();
                retVal.put("error", line);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            List<List<String>> rowDataList = new ArrayList<List<String>>();
            int lineNumber = 0;
            try (BufferedReader br = Files.newBufferedReader(PathUtil.getResultFilePath(datasource, queryid, false), StandardCharsets.UTF_8)) {
                String line = br.readLine();
                while (line != null) {
                    if (lineNumber == 0) {
                        ObjectMapper columnsMapper = new ObjectMapper();
                        List columns = columnsMapper.readValue(line, List.class);
                        retVal.put("headers", columns);
                    } else {
                        if (queryString.toLowerCase().startsWith("show") || lineNumber <= limit) {
                            ObjectMapper resultsMapper = new ObjectMapper();
                            List row = resultsMapper.readValue(line, List.class);
                            rowDataList.add(row);
                        }
                    }
                    lineNumber++;
                    line = br.readLine();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            retVal.put("results", rowDataList);
            retVal.put("lineNumber", Integer.toString(lineNumber));
            LocalDateTime submitTimeLdt = LocalDateTime.parse(queryid.substring(0, "yyyyMMdd_HHmmss".length()), DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            ZonedDateTime submitTimeZdt = submitTimeLdt.atZone(ZoneId.of("GMT", ZoneId.SHORT_IDS));
            ZonedDateTime fetchResultTime = ZonedDateTime.parse(fetchResultTimeString);
            long elapsedTimeMillis = ChronoUnit.MILLIS.between(submitTimeZdt, fetchResultTime);
            retVal.put("elapsedTimeMillis", elapsedTimeMillis);
            try {
                long size = Files.size(PathUtil.getResultFilePath(datasource, queryid, false));
                DataSize rawDataSize = new DataSize(size, DataSize.Unit.BYTE);
                retVal.put("rawDataSize", rawDataSize.convertToMostSuccinctDataSize().toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
