package yanagishima.row;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.geso.tinyorm.Row;
import me.geso.tinyorm.annotations.Column;
import me.geso.tinyorm.annotations.PrimaryKey;
import me.geso.tinyorm.annotations.Table;

@Table("query_v14")
@Data
@EqualsAndHashCode(callSuper = false)
public class QueryV14 extends Row<QueryV14>{

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

    @Column("user")
    private String user;

    @Column("status")
    private String status;

    @Column("elapsed_time_millis")
    private Integer elapsedTimeMillis;

    @Column("result_file_size")
    private Integer resultFileSize;

    @Column("linenumber")
    private Integer linenumber;
}
