package yanagishima.util;

import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class JsonUtil {

	public static void writeJSON(HttpServletResponse resp, Object obj) {

		try {
			resp.setContentType("application/json");
			ObjectMapper mapper = new ObjectMapper();
			OutputStream stream = resp.getOutputStream();
			mapper.writeValue(stream, obj);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
