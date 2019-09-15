package yanagishima.servlet.api;

import com.owlike.genson.annotation.JsonCreator;
import com.owlike.genson.annotation.JsonProperty;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/convertPresto")
@Produces(APPLICATION_JSON)
public class HiveToPrestoQueryConverter {
	@POST
	public HiveToPresto getConvertedQuery(@FormParam("query") String query) {
		return new HiveToPresto(query);
	}

	public static final class HiveToPresto {
		private final String query;

		@JsonCreator
		public HiveToPresto(String query) {
			this.query = query;
		}

		@JsonProperty("prestoQuery")
		public String getPrestoQuery() {
			if (query == null) {
				return null;
			}
			return query.replace("get_json_object", "json_extract_scalar").replace("lateral view explode", "cross join unnest");
		}
	}
}
