package yanagishima.row;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.geso.tinyorm.Row;
import me.geso.tinyorm.annotations.Column;
import me.geso.tinyorm.annotations.PrimaryKey;
import me.geso.tinyorm.annotations.Table;

@Table("starred_schema")
@Data
@EqualsAndHashCode(callSuper = false)
public class StarredSchema extends Row<StarredSchema>{

    @PrimaryKey
    @Column("starred_schema_id")
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
