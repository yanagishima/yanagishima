package yanagishima.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.Test;

class YanagishimaConfigTest {
    @Test
    void testDefaultConfig() {
        YanagishimaConfig config = new YanagishimaConfig(new Properties());
        assertEquals(List.of(), config.getInvisibleSchemas("datasource", "catalog"));
        assertEquals(List.of(), config.getInvisibleDatabases("datasource"));
        assertEquals(List.of(), config.getHiveDisallowedKeywords("datasource"));
        assertEquals(List.of(), config.getHiveSecretKeywords("datasource"));
        assertEquals(List.of(), config.getPrestoSecretKeywords("datasource"));
        assertEquals(List.of(), config.getPrestoMustSpecifyConditions("datasource"));
        assertEquals(List.of(), config.getHiveMustSpecifyConditions("datasource"));
        assertEquals(List.of(), config.getElasticsearchSecretKeywords("datasource"));
        assertEquals(List.of(), config.getElasticsearchMustSpecifyConditions("datasource"));
        assertEquals(List.of(), config.getElasticsearchDisallowedKeywords("datasource"));
        assertTrue(config.isAllowOtherReadResult("datasource"));
        assertTrue(config.isUseNewShowPartitions("datasource"));
    }

    @Test
    void testExplicitPropertyMappings() {
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
        properties.setProperty("allow.other.read.result.datasource", "false");
        properties.setProperty("use.new.show.partitions.datasource", "false");

        YanagishimaConfig config = new YanagishimaConfig(properties);
        assertEquals(List.of("_hidden_schema1", "_hidden_schema2"), config.getInvisibleSchemas("datasource", "catalog"));
        assertEquals(List.of("_hidden_database1", "_hidden_database2"), config.getInvisibleDatabases("datasource"));
        assertEquals(List.of("insert", "drop"), config.getHiveDisallowedKeywords("datasource"));
        assertEquals(List.of("_secret1", "_secret2"), config.getHiveSecretKeywords("datasource"));
        assertEquals(List.of("_secret1", "_secret2"), config.getPrestoSecretKeywords("datasource"));
        assertEquals(List.of("part_col1", "part_col2"), config.getPrestoMustSpecifyConditions("datasource"));
        assertEquals(List.of("part_col1", "part_col2"), config.getHiveMustSpecifyConditions("datasource"));
        assertEquals(List.of("_secret1", "_secret2"), config.getElasticsearchSecretKeywords("datasource"));
        assertEquals(List.of("part_col1", "part_col2"), config.getElasticsearchMustSpecifyConditions("datasource"));
        assertEquals(List.of("disallowed1", "disallowed2"), config.getElasticsearchDisallowedKeywords("datasource"));
        assertFalse(config.isAllowOtherReadResult("datasource"));
        assertFalse(config.isUseNewShowPartitions("datasource"));
    }
}
