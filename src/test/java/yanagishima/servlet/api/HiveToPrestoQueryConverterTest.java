package yanagishima.servlet.api;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HiveToPrestoQueryConverterTest {
    @Test
    public void testHiveToPrestoQuery() {
        HiveToPrestoQueryConverter.HiveToPresto converter = new HiveToPrestoQueryConverter.HiveToPresto("get_json_object");
        assertEquals("json_extract_scalar", converter.getPrestoQuery());

        converter = new HiveToPrestoQueryConverter.HiveToPresto("lateral view explode");
        assertEquals("cross join unnest", converter.getPrestoQuery());

        converter = new HiveToPrestoQueryConverter.HiveToPresto("");
        assertEquals("", converter.getPrestoQuery());

        converter = new HiveToPrestoQueryConverter.HiveToPresto(null);
        assertNull(converter.getPrestoQuery());
    }
}
