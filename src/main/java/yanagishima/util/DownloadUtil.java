package yanagishima.util;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class DownloadUtil {

    public static void tsvDownload(HttpServletResponse response, String fileName, String datasource, String queryid, String encode) {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        try {
            writeBOM(response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), encode))) {
            try(BufferedReader br = Files.newBufferedReader(PathUtil.getResultFilePath(datasource, queryid, false));
                CSVPrinter csvPrinter = new CSVPrinter(printWriter, CSVFormat.EXCEL.withDelimiter('\t').withRecordSeparator(System.getProperty("line.separator")));) {
                CSVParser parse = CSVFormat.EXCEL.withDelimiter('\t').withNullString("\\N").parse(br);
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

    public static void csvDownload(HttpServletResponse response, String fileName, String datasource, String queryid, String encode) {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        try {
            writeBOM(response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), encode))) {
            try(BufferedReader br = Files.newBufferedReader(PathUtil.getResultFilePath(datasource, queryid, false));
                CSVPrinter csvPrinter = new CSVPrinter(printWriter, CSVFormat.EXCEL.withRecordSeparator(System.getProperty("line.separator")));) {
                CSVParser parse = CSVFormat.EXCEL.withDelimiter('\t').withNullString("\\N").parse(br);
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

    public static void writeBOM(OutputStream out) throws IOException {
        out.write(new byte[]{ (byte)0xef,(byte)0xbb, (byte)0xbf });
    }

}
