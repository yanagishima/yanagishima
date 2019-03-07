package yanagishima.util;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DownloadUtil {

    public static void tsvDownload(HttpServletResponse response, String fileName, String datasource, String queryid, String encode, boolean header) {
        Path resultFilePath = PathUtil.getResultFilePath(datasource, queryid, false);
        if(!resultFilePath.toFile().exists()) {
            throw new RuntimeException(resultFilePath.toFile().getName());
        }
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        try {
            writeBOM(response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), encode))) {
            try(BufferedReader br = Files.newBufferedReader(resultFilePath);
                CSVPrinter csvPrinter = new CSVPrinter(printWriter, CSVFormat.EXCEL.withDelimiter('\t').withRecordSeparator(System.getProperty("line.separator")));) {
                CSVParser parse = CSVFormat.EXCEL.withDelimiter('\t').withNullString("\\N").parse(br);
                int lineNumber = 0;
                for (CSVRecord csvRecord : parse) {
                    List<String> columnList = new ArrayList<>();
                    for (String column : csvRecord) {
                        columnList.add(column);
                    }
                    if(header || lineNumber > 0) {
                        csvPrinter.printRecord(columnList);
                    }
                    lineNumber++;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void csvDownload(HttpServletResponse response, String fileName, String datasource, String queryid, String encode, boolean header) {
        Path resultFilePath = PathUtil.getResultFilePath(datasource, queryid, false);
        if(!resultFilePath.toFile().exists()) {
            throw new RuntimeException(resultFilePath.toFile().getName());
        }
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        try {
            writeBOM(response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), encode))) {
            try(BufferedReader br = Files.newBufferedReader(resultFilePath);
                CSVPrinter csvPrinter = new CSVPrinter(printWriter, CSVFormat.EXCEL.withRecordSeparator(System.getProperty("line.separator")));) {
                CSVParser parse = CSVFormat.EXCEL.withDelimiter('\t').withNullString("\\N").parse(br);
                int lineNumber = 0;
                for (CSVRecord csvRecord : parse) {
                    List<String> columnList = new ArrayList<>();
                    for (String column : csvRecord) {
                        columnList.add(column);
                    }
                    if(header || lineNumber > 0) {
                        csvPrinter.printRecord(columnList);
                    }
                    lineNumber++;
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
