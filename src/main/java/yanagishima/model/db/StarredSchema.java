package yanagishima.model.db;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.geso.tinyorm.Row;
import me.geso.tinyorm.annotations.Column;
import me.geso.tinyorm.annotations.PrimaryKey;
import me.geso.tinyorm.annotations.Table;
import org.codehaus.jackson.annotate.JsonProperty;

@Table("starred_schema")
@Data
@EqualsAndHashCode(callSuper = false)
public class StarredSchema extends Row<StarredSchema> {

    @PrimaryKey
    @Column("starred_schema_id")
    @JsonProperty("starred_schema_id")
    private int starredSchemaId;

    @Column("datasource")
    private String datasource;

    @Column("engine")
    private String engine;

    @Column("catalog")
    private String catalog;

    @Column("schema")
    private String schema;

    @Column("user")
    private String user;
}
