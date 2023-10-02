package yanagishima.service;

import static java.lang.String.format;
import static yanagishima.util.PathUtil.getResultFilePath;
import static yanagishima.util.QueryIdUtil.datetimeOf;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import io.airlift.units.Duration;
import lombok.RequiredArgsConstructor;
import yanagishima.model.User;
import yanagishima.model.db.Query;
import yanagishima.repository.QueryRepository;
import yanagishima.util.PathUtil;
import yanagishima.util.Status;

@Service
@RequiredArgsConstructor
public class QueryService {
  private final QueryRepository queryRepository;

  public List<Query> getAll(String datasource, String engine, String user, String query, int limit) {
    return queryRepository.findAllByDatasourceAndEngineAndUseridAndQueryStringContainsOrderByQueryIdDesc(
        datasource, engine, user, query, PageRequest.of(0, limit));
  }

  public List<Query> getAll(String datasource, String engine, List<String> queryIds) {
    return queryRepository.findAllByDatasourceAndEngineAndQueryIdIn(datasource, engine, queryIds);
  }

  public List<Query> getAll(String datasource, List<String> queryIds) {
    return queryRepository.findAllByDatasourceAndQueryIdIn(datasource, queryIds);
  }

  public Optional<Query> get(String queryId, String datasource, User user) {
    return queryRepository.findByQueryIdAndDatasourceAndUserid(queryId, datasource, user.getId());
  }

  public Optional<Query> getByEngine(String queryId, String datasource, String engine) {
    return queryRepository.findByQueryIdAndDatasourceAndEngine(queryId, datasource, engine);
  }

  public Optional<Query> get(String queryId, String datasource) {
    return queryRepository.findByQueryIdAndDatasource(queryId, datasource);
  }

  public long count(String datasource, String engine, String user) {
    return queryRepository.countAllByDatasourceAndEngineAndUserid(datasource, engine, user);
  }

  public Query insert(String datasource, String engine, String queryId, String fetchResultTimeString,
                      String queryString,
                      String user, String status, Integer elapsedTimeMillis, Long resultFileSize,
                      Integer linenumber) {
    Query query = new Query();
    query.setDatasource(datasource);
    query.setEngine(engine);
    query.setQueryId(queryId);
    query.setFetchResultTimeString(fetchResultTimeString);
    query.setQueryString(queryString);
    query.setUserid(user);
    query.setStatus(status);
    query.setElapsedTimeMillis(elapsedTimeMillis);
    query.setResultFileSize(resultFileSize);
    query.setLinenumber(linenumber);
    return queryRepository.save(query);
  }

  public void saveTimeout(Duration queryMaxRunTime, long start, String datasource, String engine,
                          String queryId, String query, String user) {
    if (System.currentTimeMillis() - start > queryMaxRunTime.toMillis()) {
      String message = format("Query failed (#%s) in %s: Query exceeded maximum time limit of %s", queryId,
                              datasource, queryMaxRunTime.toString());
      saveError(datasource, engine, queryId, query, user, message);
      throw new RuntimeException(message);
    }
  }

  public void saveError(String datasource, String engine, String queryId, String queryString, String user,
                        String errorMessage) {
    try {
      LocalDateTime submitTimeLdt = datetimeOf(queryId);
      ZonedDateTime submitTimeZdt = submitTimeLdt.atZone(ZoneId.of("GMT", ZoneId.SHORT_IDS));
      String fetchResultTimeString = ZonedDateTime.now().toString();
      ZonedDateTime fetchResultTime = ZonedDateTime.parse(fetchResultTimeString);
      long elapsedTimeMillis = ChronoUnit.MILLIS.between(submitTimeZdt, fetchResultTime);

      Path dst = getResultFilePath(datasource, queryId, true);
      try (BufferedWriter bw = Files.newBufferedWriter(dst, StandardCharsets.UTF_8)) {
        bw.write(errorMessage);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      long resultFileSize = Files.size(dst);
      Query query = new Query();
      query.setDatasource(datasource);
      query.setEngine(engine);
      query.setQueryId(queryId);
      query.setFetchResultTimeString(fetchResultTimeString);
      query.setQueryString(queryString);
      query.setUserid(user);
      query.setStatus(Status.FAILED.name());
      query.setElapsedTimeMillis((int) elapsedTimeMillis);
      query.setResultFileSize(resultFileSize);
      queryRepository.save(query);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void save(String datasource, String engine, String queryString, String user, String queryId,
                   int linenumber) {
    try {
      LocalDateTime submitTimeLdt = datetimeOf(queryId);
      ZonedDateTime submitTimeZdt = submitTimeLdt.atZone(ZoneId.of("GMT", ZoneId.SHORT_IDS));
      String fetchResultTimeString = ZonedDateTime.now().toString();
      ZonedDateTime fetchResultTime = ZonedDateTime.parse(fetchResultTimeString);
      long elapsedTimeMillis = ChronoUnit.MILLIS.between(submitTimeZdt, fetchResultTime);

      Path resultFilePath = PathUtil.getResultFilePath(datasource, queryId, false);
      long resultFileSize = Files.size(resultFilePath);
      Query query = new Query();
      query.setDatasource(datasource);
      query.setEngine(engine);
      query.setQueryId(queryId);
      query.setFetchResultTimeString(fetchResultTimeString);
      query.setQueryString(queryString);
      query.setUserid(user);
      query.setStatus(Status.SUCCEED.name());
      query.setElapsedTimeMillis((int) elapsedTimeMillis);
      query.setResultFileSize(resultFileSize);
      query.setLinenumber(linenumber);
      queryRepository.save(query);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
