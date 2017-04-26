package yanagishima.util;


import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;

public class DownloadUtil {

    public static void tsvDownload(HttpServletResponse response, String fileName, String datasource, String queryid) {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        try (OutputStream out = response.getOutputStream()) {
            try(BufferedReader br = Files.newBufferedReader(PathUtil.getResultFilePath(datasource, queryid, false))) {
                br.lines().forEach(line -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        List row = mapper.readValue(line, List.class);
                        out.write((String.join("\t", row) + System.getProperty("line.separator")).getBytes("UTF-8"));
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
        try (OutputStream out = response.getOutputStream()) {
            try(BufferedReader br = Files.newBufferedReader(PathUtil.getResultFilePath(datasource, queryid, false))) {
                br.lines().forEach(line -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        List row = mapper.readValue(line, List.class);
                        out.write((String.join(",", row) + System.getProperty("line.separator")).getBytes("Shift_JIS"));
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
