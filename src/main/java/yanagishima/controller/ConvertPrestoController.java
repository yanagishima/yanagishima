package yanagishima.controller;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.owlike.genson.annotation.JsonCreator;
import com.owlike.genson.annotation.JsonProperty;

@Path("/convertPresto")
@Produces(APPLICATION_JSON)
public class ConvertPrestoController {
    @POST
    public Converter getConvertedQuery(@FormParam("query") String query) {
        return new Converter(query);
    }

    static class Converter {
        private final String query;

        @JsonCreator
        Converter(@JsonProperty String query) {
            this.query = query;
        }

        @JsonProperty("prestoQuery")
        public String getPrestoQuery() {
            if (query == null) {
                return null;
            }
            return query.replace("get_json_object", "json_extract_scalar")
                        .replace("lateral view explode", "cross join unnest");
        }
    }
}
