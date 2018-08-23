package yanagishima.row;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.geso.tinyorm.Row;
import me.geso.tinyorm.annotations.Column;
import me.geso.tinyorm.annotations.PrimaryKey;
import me.geso.tinyorm.annotations.Table;

@Table("label")
@Data
@EqualsAndHashCode(callSuper = false)
public class Label extends Row<Label> {

    @PrimaryKey
    @Column("datasource")
    private String datasource;

    @PrimaryKey
    @Column("engine")
    private String engine;

    @PrimaryKey
    @Column("query_id")
    private String queryid;

    @Column("label_name")
    private String labelName;

}
