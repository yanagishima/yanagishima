package yanagishima.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.config.YanagishimaConfig;
import yanagishima.model.db.StarredSchema;
import yanagishima.model.dto.StarredSchemaCreateDto;
import yanagishima.model.dto.StarredSchemaDto;
import yanagishima.service.StarredSchemaService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StarredSchemaController {
  private final StarredSchemaService starredSchemaService;
  private final YanagishimaConfig config;

  @DatasourceAuth
  @PostMapping("starredSchema")
  public StarredSchemaCreateDto post(@RequestParam String datasource, @RequestParam String engine,
                                     @RequestParam String catalog, @RequestParam String schema,
                                     HttpServletRequest request) {
    StarredSchemaCreateDto starredSchemaCreateDto = new StarredSchemaCreateDto();
    try {
      String userName = request.getHeader(config.getAuditHttpHeaderName());
      StarredSchema starredSchema = starredSchemaService.insert(datasource, engine, catalog, schema, userName);
      starredSchemaCreateDto.setStarredSchemaId(starredSchema.getStarredSchemaId());
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      starredSchemaCreateDto.setError(e.getMessage());
    }
    return starredSchemaCreateDto;
  }

  @DatasourceAuth
  @DeleteMapping("starredSchema")
  public StarredSchemaDto delete(@RequestParam String datasource,
                                 @RequestParam String engine,
                                 @RequestParam String catalog,
                                 @RequestParam(name = "starred_schema_id") int starredSchemaId,
                                 HttpServletRequest request) {
    StarredSchemaDto starredSchemaDto = new StarredSchemaDto();
    try {
      starredSchemaService.delete(starredSchemaId);

      String userName = request.getHeader(config.getAuditHttpHeaderName());
      List<StarredSchema> starredSchemas = starredSchemaService.getAll(datasource, engine, catalog, userName);
      starredSchemaDto.setStarredSchemaList(starredSchemas);
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      starredSchemaDto.setError(e.getMessage());
    }
    return starredSchemaDto;
  }

  @DatasourceAuth
  @GetMapping("starredSchema")
  public StarredSchemaDto get(@RequestParam String datasource,
                              @RequestParam String engine,
                              @RequestParam String catalog,
                              HttpServletRequest request) {
    StarredSchemaDto starredSchemaDto = new StarredSchemaDto();
    try {
      String userName = request.getHeader(config.getAuditHttpHeaderName());
      List<StarredSchema> starredSchemas = starredSchemaService.getAll(datasource, engine, catalog, userName);
      starredSchemaDto.setStarredSchemaList(starredSchemas);
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      starredSchemaDto.setError(e.getMessage());
    }
    return starredSchemaDto;
  }
}
