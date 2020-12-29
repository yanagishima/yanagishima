package yanagishima.util;

import static java.lang.String.format;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class PathUtil {
  private static final String CURRENT_PATH = new File(".").getAbsolutePath();

  public static Path getResultFilePath(String datasource, String queryId, boolean error) {
    String date = queryId.substring(0, 8);
    File directory = new File(format("%s/result/%s/%s", CURRENT_PATH, datasource, date));
    directory.mkdirs();
    String extension = error ? "err" : "tsv";
    return Paths.get(format("%s/%s.%s", directory, queryId, extension));
  }
}
