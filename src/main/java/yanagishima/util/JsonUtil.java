package yanagishima.util;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;
import org.codehaus.jackson.map.ObjectMapper;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class JsonUtil {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static void writeJSON(HttpServletResponse response, Object obj) {
    response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
    try (OutputStream stream = response.getOutputStream()) {
      OBJECT_MAPPER.writeValue(stream, obj);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
