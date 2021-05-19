package yanagishima.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.junit.jupiter.api.Test;

class QueryIdUtilTest {
  @Test
  void datetimeOf() {
    assertEquals(LocalDateTime.of(2021, 5, 19, 0, 0, 0), QueryIdUtil.datetimeOf("20210519_000000_00000_aaa"));
    assertEquals(LocalDateTime.of(2021, 5, 20, 1, 2, 3), QueryIdUtil.datetimeOf("20210520_010203_00000_bbb"));
    assertEquals(LocalDateTime.of(2021, 5, 20, 1, 2, 3), QueryIdUtil.datetimeOf("20210520_010203_12345_ccc"));

    assertThrows(DateTimeParseException.class, () -> QueryIdUtil.datetimeOf("invalid_query_id"));
  }
}
