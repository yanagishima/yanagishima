package yanagishima.util;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class DownloadUtil {

    public static void tsvDownload(HttpServletResponse response, String fileName, String datasource, String queryid) {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"))) {
            try(BufferedReader br = Files.newBufferedReader(PathUtil.getResultFilePath(datasource, queryid, false))) {
                String line = br.readLine();
                while (line != null) {
                    printWriter.println(line);
                    line = br.readLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void csvDownload(HttpServletResponse response, String fileName, String datasource, String queryid) {
        response.setContentType("text/csv; charset=Shift_JIS");
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "Shift_JIS"))) {
            try(BufferedReader br = Files.newBufferedReader(PathUtil.getResultFilePath(datasource, queryid, false));
                CSVPrinter csvPrinter = new CSVPrinter(printWriter, CSVFormat.EXCEL.withRecordSeparator(System.getProperty("line.separator")));) {
                CSVParser parse = CSVFormat.EXCEL.withDelimiter('\t').parse(br);
                for (CSVRecord csvRecord : parse) {
                    List<String> columnList = new ArrayList<>();
                    for (String column : csvRecord) {
                        columnList.add(column);
                    }
                    csvPrinter.printRecord(columnList);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
