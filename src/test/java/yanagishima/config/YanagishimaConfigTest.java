package yanagishima.config;

import org.junit.Test;

import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class YanagishimaConfigTest {
    @Test
    public void testDefaultConfig() {
        YanagishimaConfig config = new YanagishimaConfig(new Properties());
        assertEquals(List.of(), config.getInvisibleSchemas("datasource", "catalog"));
        assertEquals(List.of(), config.getInvisibleDatabases("datasource"));
        assertEquals(List.of(), config.getHiveDisallowedKeywords("datasource"));
        assertEquals(List.of(), config.getHiveSecretKeywords("datasource"));
        assertEquals(List.of(), config.getPrestoSecretKeywords("datasource"));
        assertEquals(List.of(), config.getPrestoMustSpectifyConditions("datasource"));
        assertEquals(List.of(), config.getHiveMustSpectifyConditions("datasource"));
        assertEquals(List.of(), config.getElasticsearchSecretKeywords("datasource"));
        assertEquals(List.of(), config.getElasticsearchMustSpectifyConditions("datasource"));
        assertEquals(List.of(), config.getElasticsearchDisallowedKeywords("datasource"));
    }

    @Test
    public void testExplicitPropertyMappings() {
        Properties properties = new Properties();
        properties.setProperty("invisible.schema.datasource.catalog", "_hidden_schema1, _hidden_schema2");
        properties.setProperty("invisible.database.datasource", "_hidden_database1, _hidden_database2");
        properties.setProperty("hive.disallowed.keywords.datasource", "insert, drop");
        properties.setProperty("hive.secret.keywords.datasource", "_secret1, _secret2");
        properties.setProperty("presto.secret.keywords.datasource", "_secret1, _secret2");
        properties.setProperty("presto.must.specify.conditions.datasource", "part_col1, part_col2");
        properties.setProperty("hive.must.specify.conditions.datasource", "part_col1, part_col2");
        properties.setProperty("elasticsearch.secret.keywords.datasource", "_secret1, _secret2");
        properties.setProperty("elasticsearch.must.specify.conditions.datasource", "part_col1, part_col2");
        properties.setProperty("elasticsearch.disallowed.keywords.datasource", "disallowed1, disallowed2");

        YanagishimaConfig config = new YanagishimaConfig(properties);
        assertEquals(List.of("_hidden_schema1", "_hidden_schema2"), config.getInvisibleSchemas("datasource", "catalog"));
        assertEquals(List.of("_hidden_database1", "_hidden_database2"), config.getInvisibleDatabases("datasource"));
        assertEquals(List.of("insert", "drop"), config.getHiveDisallowedKeywords("datasource"));
        assertEquals(List.of("_secret1", "_secret2"), config.getHiveSecretKeywords("datasource"));
        assertEquals(List.of("_secret1", "_secret2"), config.getPrestoSecretKeywords("datasource"));
        assertEquals(List.of("part_col1", "part_col2"), config.getPrestoMustSpectifyConditions("datasource"));
        assertEquals(List.of("part_col1", "part_col2"), config.getHiveMustSpectifyConditions("datasource"));
        assertEquals(List.of("_secret1", "_secret2"), config.getElasticsearchSecretKeywords("datasource"));
        assertEquals(List.of("part_col1", "part_col2"), config.getElasticsearchMustSpectifyConditions("datasource"));
        assertEquals(List.of("disallowed1", "disallowed2"), config.getElasticsearchDisallowedKeywords("datasource"));
    }
}
