package yanagishima.servlet.api;

import com.owlike.genson.annotation.JsonCreator;
import com.owlike.genson.annotation.JsonProperty;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/convertHive")
@Produces(APPLICATION_JSON)
public class PrestoToHiveQueryConverter {
	@POST
	public PrestoToHive getConvertedQuery(@FormParam("query") String query) {
		return new PrestoToHive(query);
	}

	public static class PrestoToHive {
		private final String query;

		@JsonCreator
		PrestoToHive(String query) {
			this.query = query;
		}

		@JsonProperty("hiveQuery")
		public String getHiveQuery() {
			if (query == null) {
				return null;
			}
			return query.replace("json_extract_scalar", "get_json_object").replace("cross join unnest", "lateral view explode");
		}
	}
}
