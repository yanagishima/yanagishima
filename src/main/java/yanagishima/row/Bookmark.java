package yanagishima.row;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.geso.tinyorm.Row;
import me.geso.tinyorm.annotations.Column;
import me.geso.tinyorm.annotations.PrimaryKey;
import me.geso.tinyorm.annotations.Table;
import org.codehaus.jackson.annotate.JsonProperty;

@Table("bookmark")
@Data
@EqualsAndHashCode(callSuper = false)
public class Bookmark extends Row<Bookmark> {
    @PrimaryKey
    @Column("bookmark_id")
    @JsonProperty("bookmark_id")
    private int bookmarkId;

    @Column("datasource")
    private String datasource;

    @Column("engine")
    private String engine;

    @Column("query")
    private String query;

    @Column("title")
    private String title;

    @Column("user")
    private String user;

    @Column("snippet")
    private String snippet;
}
