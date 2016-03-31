package yanagishima.util;

import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtil {

	public static Path getResultFilePath(String queryid) {

		String currentPath = new File(".").getAbsolutePath();
		String yyyymmdd = queryid.substring(0, 8);
		Path src = Paths.get(String.format("%s/result/%s/%s.tsv", currentPath, yyyymmdd, queryid));
		return src;
	}

}
