package yanagishima.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class FormatUtilTest {
  @Test
  void toSuccinctDataSize() {
    assertNull(FormatUtil.toSuccinctDataSize(null));
    assertEquals("1B", FormatUtil.toSuccinctDataSize(1));
    assertEquals("1kB", FormatUtil.toSuccinctDataSize(1024));
    assertEquals("1MB", FormatUtil.toSuccinctDataSize(1024 * 1024));
    assertEquals("1GB", FormatUtil.toSuccinctDataSize(1024 * 1024 * 1024));
  }
}
