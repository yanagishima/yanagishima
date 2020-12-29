package yanagishima.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static yanagishima.util.HttpRequestUtil.getRequiredHeader;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;

class HttpRequestUtilTest {
  @Test
  void testGetRequiredHeader() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("key1")).thenReturn("value1");
    assertEquals("value1", getRequiredHeader(request, "key1"));
  }
}
