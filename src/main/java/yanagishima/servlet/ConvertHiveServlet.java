package yanagishima.servlet;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import yanagishima.model.dto.HiveQueryDto;

@Api(tags = "convert")
@RestController
public class ConvertHiveServlet {
  @PostMapping("convertHive")
  public HiveQueryDto post(@RequestParam String query) {
    return new HiveQueryDto(toHiveQuery(query));
  }

  private static String toHiveQuery(String prestoQuery) {
    return prestoQuery.replace("json_extract_scalar", "get_json_object")
                      .replace("cross join unnest", "lateral view explode");
  }
}
