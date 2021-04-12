package yanagishima.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PublishUtilTest {

    @Test
    void testCanAccessPublishedPage() {
        assertTrue(PublishUtil.canAccessPublishedPage("jp11111", "jp11111",
                "jp12345,jp67890"));
        assertTrue(PublishUtil.canAccessPublishedPage("jp11111", "jp12345",
                "jp12345,jp67890"));
        assertFalse(PublishUtil.canAccessPublishedPage("jp11111", "jp99999",
                "jp12345,jp67890"));
        assertFalse(PublishUtil.canAccessPublishedPage(null, "jp99999",
                "jp12345,jp67890"));
        assertFalse(PublishUtil.canAccessPublishedPage("jp11111", "jp99999",
                null));
    }
}
