package yanagishima.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static yanagishima.util.PrestoQueryProvider.listToValues;

import java.util.List;

import org.junit.Test;

public class PrestoQueryProviderTest {
    @Test
    public void testListToValues() {
        String expected = "SELECT * FROM ( VALUES\n"
                          + "(1, 2, 3),\n"
                          + "(4, 5, 6) ) AS t (col1, col2, col3)";
        List<String> csv = List.of("col1, col2, col3", "1, 2, 3", "4, 5, 6");
        assertEquals(expected, listToValues(csv, 100));

        expected = "SELECT * FROM ( VALUES\n"
                          + "(1, 2, 3),\n"
                          + "(4, 5, 6),\n"
                          + "(7, 8, 9) ) AS t (col1, col2, col3)";
        csv = List.of("col1, col2, col3", "1, 2, 3", "4, 5, 6", "7, 8, 9");
        assertEquals(expected, listToValues(csv, 100));

        try {
            listToValues(List.of("col1, col2, col3"), 100);
            fail();
        } catch (RuntimeException e) {
            assertEquals("At least, there must be 2 lines", e.getMessage());
        }

        try {
            listToValues(csv, 3);
            fail();
        } catch (RuntimeException e) {
            assertEquals("At most, there must be 3 lines", e.getMessage());
        }
    }
}
