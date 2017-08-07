package yanagishima.row;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.geso.tinyorm.Row;
import me.geso.tinyorm.annotations.Column;
import me.geso.tinyorm.annotations.PrimaryKey;
import me.geso.tinyorm.annotations.Table;

@Table("query")
@Data
@EqualsAndHashCode(callSuper = false)
public class Query extends Row<Query>{

    @PrimaryKey
    @Column("datasource")
    private String datasource;

    @PrimaryKey
    @Column("engine")
    private String engine;

    @PrimaryKey
    @Column("query_id")
    private String queryId;

    @Column("fetch_result_time_string")
    private String fetchResultTimeString;

    @Column("query_string")
    private String queryString;
}
