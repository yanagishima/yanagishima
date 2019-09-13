package yanagishima.util;

import static java.lang.String.format;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class PathUtil {
	private PathUtil() {}

	public static Path getResultFilePath(String datasource, String queryId, boolean error) {
		String currentPath = new File(".").getAbsolutePath();
		String yyyymmdd = queryId.substring(0, 8);
		File datasourceDir = new File(format("%s/result/%s", currentPath, datasource));
		if (!datasourceDir.isDirectory()) {
			datasourceDir.mkdir();
		}
		File yyyymmddDir = new File(format("%s/result/%s/%s", currentPath, datasource, yyyymmdd));
		if (!yyyymmddDir.isDirectory()) {
			yyyymmddDir.mkdir();
		}

		String extension = error ? "err" : "tsv";
		return Paths.get(format("%s/result/%s/%s/%s.%s", currentPath, datasource, yyyymmdd, queryId, extension));
	}
}
