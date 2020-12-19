package yanagishima.servlet;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import yanagishima.model.dto.PrestoQueryDto;

@Api(tags = "convert")
@RestController
public class ConvertPrestoServlet {
  @PostMapping("convertPresto")
  public PrestoQueryDto post(@RequestParam String query) {
    return new PrestoQueryDto(toPrestoQuery(query));
  }

  private static String toPrestoQuery(String hiveQuery) {
    return hiveQuery.replace("get_json_object", "json_extract_scalar")
                    .replace("lateral view explode", "cross join unnest");
  }
}
