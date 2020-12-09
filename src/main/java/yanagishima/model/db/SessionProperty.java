package yanagishima.model.db;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.geso.tinyorm.Row;
import me.geso.tinyorm.annotations.Column;
import me.geso.tinyorm.annotations.PrimaryKey;
import me.geso.tinyorm.annotations.Table;
import org.codehaus.jackson.annotate.JsonProperty;

@Table("session_property")
@Data
@EqualsAndHashCode(callSuper = false)
public class SessionProperty extends Row<SessionProperty> {

    @PrimaryKey
    @Column("session_property_id")
    @JsonProperty("session_property_id")
    private int sessionPropertyId;

    @Column("datasource")
    private String datasource;

    @Column("engine")
    private String engine;

    @Column("query_id")
    @JsonProperty("query_id")
    private String queryId;

    @Column("session_key")
    @JsonProperty("session_key")
    private String sessionKey;

    @Column("session_value")
    @JsonProperty("session_value")
    private String sessionValue;
}
