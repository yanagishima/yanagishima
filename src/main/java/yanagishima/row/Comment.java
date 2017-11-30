package yanagishima.row;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.geso.tinyorm.Row;
import me.geso.tinyorm.annotations.Column;
import me.geso.tinyorm.annotations.PrimaryKey;
import me.geso.tinyorm.annotations.Table;

@Table("comment")
@Data
@EqualsAndHashCode(callSuper = false)
public class Comment extends Row<Comment> {

    @PrimaryKey
    @Column("datasource")
    private String datasource;

    @PrimaryKey
    @Column("engine")
    private String engine;

    @PrimaryKey
    @Column("query_id")
    private String queryid;

    @Column("update_time_string")
    private String updateTimeString;

    @Column("content")
    private String content;

    @Column("user")
    private String user;

    @Column("like_count")
    private int likeCount;
}
