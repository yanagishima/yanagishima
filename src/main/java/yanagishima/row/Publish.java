package yanagishima.row;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.geso.tinyorm.Row;
import me.geso.tinyorm.annotations.Column;
import me.geso.tinyorm.annotations.PrimaryKey;
import me.geso.tinyorm.annotations.Table;

@Table("publish")
@Data
@EqualsAndHashCode(callSuper = false)
public class Publish extends Row<Publish>{

    @PrimaryKey
    @Column("publish_id")
    private String publishId;

    @Column("datasource")
    private String datasource;

    @Column("engine")
    private String engine;

    @Column("query_id")
    private String queryId;

}
