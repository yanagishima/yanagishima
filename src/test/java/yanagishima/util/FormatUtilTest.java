package yanagishima.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class FormatUtilTest {
    @Test
    void toSuccinctDataSize() {
        assertNull(FormatUtil.toSuccinctDataSize(null));
        assertEquals("1B", FormatUtil.toSuccinctDataSize(1L));
        assertEquals("1kB", FormatUtil.toSuccinctDataSize(1024L));
        assertEquals("1MB", FormatUtil.toSuccinctDataSize((long) (1024 * 1024)));
        assertEquals("1GB", FormatUtil.toSuccinctDataSize((long) (1024 * 1024 * 1024)));
        assertEquals("2GB", FormatUtil.toSuccinctDataSize(2L * 1024 * 1024 * 1024));
        assertEquals("4GB", FormatUtil.toSuccinctDataSize(4L * 1024 * 1024 * 1024));
    }
}
