package yanagishima.servlet.api;

import com.owlike.genson.annotation.JsonCreator;
import com.owlike.genson.annotation.JsonProperty;
import io.prestosql.sql.SqlFormatter;
import io.prestosql.sql.parser.ParsingException;
import io.prestosql.sql.parser.ParsingOptions;
import io.prestosql.sql.parser.SqlParser;
import io.prestosql.sql.tree.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static io.prestosql.sql.parser.ParsingOptions.DecimalLiteralTreatment.AS_DOUBLE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/format")
@Produces(APPLICATION_JSON)
public class PrestoSqlFormatter {
    private static final Logger LOG = LoggerFactory.getLogger(PrestoSqlFormatter.class);
    private static final SqlParser SQL_PARSER = new SqlParser();

    @POST
    public PrestoQuery getQuery(@FormParam("query") String query) {
        try {
            return new PrestoQuery(query);
        } catch (ParsingException e) {
            LOG.error(e.getMessage(), e);
            throw new WebApplicationException(Response.ok(new FormatError(e)).build());
        }
    }

    public static class PrestoQuery {
        private final String formattedQuery;

        @JsonCreator
        PrestoQuery(String query) {
            if (isNullOrEmpty(query)) {
                this.formattedQuery = null;
            } else {
                Statement statement = SQL_PARSER.createStatement(query, new ParsingOptions(AS_DOUBLE));
                this.formattedQuery = SqlFormatter.formatSql(statement, Optional.empty());
            }
        }

        @JsonProperty("formattedQuery")
        public String getFormattedQuery() {
            return formattedQuery;
        }
    }

    private static class FormatError {
        private final int errorLineNumber;
        private final String error;

        @JsonCreator
        FormatError(ParsingException e) {
            this.errorLineNumber = e.getLineNumber();
            this.error = e.getMessage();
        }

        @JsonProperty("error")
        public String getError() {
            return error;
        }

        @JsonProperty("errorLineNumber")
        public int getErrorLineNumber() {
            return errorLineNumber;
        }
    }
}
