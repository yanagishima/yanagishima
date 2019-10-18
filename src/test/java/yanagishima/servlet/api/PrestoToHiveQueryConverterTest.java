package yanagishima.servlet.api;

import org.junit.Test;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;

public class PrestoToHiveQueryConverterTest {
    @Test
    public void testHiveToPrestoQuery() {
        PrestoToHiveQueryConverter.PrestoToHive converter = new PrestoToHiveQueryConverter.PrestoToHive("json_extract_scalar");
        assertEquals("get_json_object", converter.getHiveQuery());

        converter = new PrestoToHiveQueryConverter.PrestoToHive("cross join unnest");
        assertEquals("lateral view explode", converter.getHiveQuery());

        converter = new PrestoToHiveQueryConverter.PrestoToHive("");
        assertEquals("", converter.getHiveQuery());

        converter = new PrestoToHiveQueryConverter.PrestoToHive(null);
        assertNull(converter.getHiveQuery());
    }
}
