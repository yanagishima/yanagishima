package yanagishima.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.util.List;

public class DownloadUtil {

    public static void tsvDownload(HttpServletResponse response, String fileName, String datasource, String queryid) {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"))) {
            try(BufferedReader br = Files.newBufferedReader(PathUtil.getResultFilePath(datasource, queryid, false));
                CSVPrinter csvPrinter = new CSVPrinter(printWriter, CSVFormat.EXCEL.withDelimiter('\t').withRecordSeparator(System.getProperty("line.separator")));) {
                br.lines().forEach(line -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        List row = mapper.readValue(line, List.class);
                        csvPrinter.printRecord(row);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
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
                br.lines().forEach(line -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        List row = mapper.readValue(line, List.class);
                        csvPrinter.printRecord(row);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
