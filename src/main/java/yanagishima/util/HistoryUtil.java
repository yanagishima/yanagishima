package yanagishima.util;

import io.airlift.units.DataSize;
import me.geso.tinyorm.TinyORM;
import yanagishima.row.Query;

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
import java.util.*;

public class HistoryUtil {

    public static void createHistoryResult(TinyORM db, int limit, HashMap<String, Object> retVal, String datasource, String queryid) {
        Optional<Query> queryOptional = db.single(Query.class).where("query_id=? and datasource=?", queryid, datasource).execute();
        queryOptional.ifPresent(query -> {
            String queryString = query.getQueryString();
            retVal.put("queryString", queryString);

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
                            String[] columns = line.split("\t");
                            retVal.put("headers", Arrays.asList(columns));
                        } else {
                            if (queryString.toLowerCase().startsWith("show") || lineNumber <= limit) {
                                String[] row = line.split("\t");
                                rowDataList.add(Arrays.asList(row));
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
                String fetchResultTimeString = query.getFetchResultTimeString();
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

        });
    }

}
