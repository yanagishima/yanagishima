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

public final class DownloadUtil {
    private static final byte[] BOM = new byte[]{(byte) 0xef, (byte) 0xbb, (byte) 0xbf};

    private DownloadUtil() { }

    public static void downloadTsv(HttpServletResponse response, String fileName, String datasource, String queryid, String encode, boolean showHeader, boolean showBOM) {
        Path filePath = PathUtil.getResultFilePath(datasource, queryid, false);
        if (!filePath.toFile().exists()) {
            throw new RuntimeException(filePath.toFile().getName());
        }
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");

        download(response, filePath, encode, showHeader, showBOM, '\t');
    }

    public static void downloadCsv(HttpServletResponse response, String fileName, String datasource, String queryid, String encode, boolean showHeader, boolean showBOM) {
        Path filePath = PathUtil.getResultFilePath(datasource, queryid, false);
        if (!filePath.toFile().exists()) {
            throw new RuntimeException(filePath.toFile().getName());
        }
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");

        download(response, filePath, encode, showHeader, showBOM, ',');
    }

    private static void download(HttpServletResponse response, Path resultFilePath, String encode, boolean showHeader, boolean showBOM, char delimiter) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), encode))) {
            try (BufferedReader reader = Files.newBufferedReader(resultFilePath);
                CSVPrinter printer = new CSVPrinter(writer, CSVFormat.EXCEL.withDelimiter(delimiter).withRecordSeparator(System.getProperty("line.separator")))) {
                CSVParser parser = CSVFormat.EXCEL.withDelimiter('\t').withNullString("\\N").parse(reader);

                if (showBOM) {
                    response.getOutputStream().write(BOM);
                }
                int rowNumber = 0;
                for (CSVRecord record : parser) {
                    List<String> columns = new ArrayList<>();
                    record.forEach(columns::add);

                    if (showHeader || rowNumber > 0) {
                        printer.printRecord(columns);
                    }
                    rowNumber++;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
