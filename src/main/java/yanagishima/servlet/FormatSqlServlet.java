package yanagishima.servlet;

import static io.prestosql.sql.parser.ParsingOptions.DecimalLiteralTreatment.AS_DOUBLE;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.prestosql.sql.SqlFormatter;
import io.prestosql.sql.parser.ParsingException;
import io.prestosql.sql.parser.ParsingOptions;
import io.prestosql.sql.parser.SqlParser;
import io.prestosql.sql.tree.Statement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.exception.YanagishimaParseException;
import yanagishima.model.dto.FormatSqlDto;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FormatSqlServlet {
  @PostMapping("format")
  public FormatSqlDto post(@RequestParam String query) {
    try {
      return new FormatSqlDto(formatQuery(query));
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      YanagishimaParseException exception = new YanagishimaParseException(e.getMessage(), e);
      if (e instanceof ParsingException) {
        exception.setErrorLineNumber(((ParsingException) e).getLineNumber());
        exception.setErrorColumnNumber(((ParsingException) e).getColumnNumber());
      }
      throw exception;
    }
  }

  private String formatQuery(String query) {
    SqlParser sqlParser = new SqlParser();
    Statement statement = sqlParser.createStatement(query, new ParsingOptions(AS_DOUBLE));
    return SqlFormatter.formatSql(statement);
  }
}
