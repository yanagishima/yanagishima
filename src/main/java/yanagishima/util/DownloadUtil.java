package yanagishima.util;

import static org.apache.commons.csv.CSVFormat.EXCEL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class DownloadUtil {
  private static final byte[] BOM = new byte[] { (byte) 0xef, (byte) 0xbb, (byte) 0xbf };

  public static void downloadTsv(HttpServletResponse response, String fileName, String datasource,
                                 String queryId, String encode, boolean showHeader, boolean showBOM) {
    response.setContentType("application/octet-stream");
    response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");

    download(response, datasource, queryId, encode, showHeader, showBOM, '\t');
  }

  public static void downloadCsv(HttpServletResponse response, String fileName, String datasource,
                                 String queryId, String encode, boolean showHeader, boolean showBOM) {
    response.setContentType("text/csv");
    response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");

    download(response, datasource, queryId, encode, showHeader, showBOM, ',');
  }

  private static void download(HttpServletResponse response, String datasource, String queryId,
                               String encode, boolean showHeader, boolean showBOM, char delimiter) {
    Path filePath = PathUtil.getResultFilePath(datasource, queryId, false);
    if (!filePath.toFile().exists()) {
      throw new RuntimeException(filePath.toFile().getName());
    }

    CSVFormat format = EXCEL.withDelimiter(delimiter).withRecordSeparator(System.getProperty("line.separator"));
    try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), encode));
         BufferedReader reader = Files.newBufferedReader(filePath);
         CSVPrinter printer = new CSVPrinter(writer, format);
         CSVParser parser = EXCEL.withDelimiter('\t').withNullString("\\N").parse(reader)) {

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
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
