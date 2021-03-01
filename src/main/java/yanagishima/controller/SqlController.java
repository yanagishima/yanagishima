package yanagishima.controller;

import static io.trino.sql.parser.ParsingOptions.DecimalLiteralTreatment.AS_DOUBLE;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.trino.sql.SqlFormatter;
import io.trino.sql.parser.ParsingException;
import io.trino.sql.parser.ParsingOptions;
import io.trino.sql.parser.SqlParser;
import io.trino.sql.tree.Statement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.exception.YanagishimaParseException;
import yanagishima.model.dto.FormatSqlDto;
import yanagishima.model.dto.HiveQueryDto;
import yanagishima.model.dto.PrestoQueryDto;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SqlController {
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

  @PostMapping("convertHive")
  public HiveQueryDto convertHive(@RequestParam String query) {
    return new HiveQueryDto(toHiveQuery(query));
  }

  @PostMapping("convertPresto")
  public PrestoQueryDto convertPresto(@RequestParam String query) {
    return new PrestoQueryDto(toPrestoQuery(query));
  }

  private String formatQuery(String query) {
    SqlParser sqlParser = new SqlParser();
    Statement statement = sqlParser.createStatement(query, new ParsingOptions(AS_DOUBLE));
    return SqlFormatter.formatSql(statement);
  }

  private static String toHiveQuery(String prestoQuery) {
    return prestoQuery.replace("json_extract_scalar", "get_json_object")
                      .replace("cross join unnest", "lateral view explode");
  }

  private static String toPrestoQuery(String hiveQuery) {
    return hiveQuery.replace("get_json_object", "json_extract_scalar")
                    .replace("lateral view explode", "cross join unnest");
  }
}
