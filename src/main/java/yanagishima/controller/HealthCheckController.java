package yanagishima.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@Api(tags = "internal")
@RestController
@RequiredArgsConstructor
public class HealthCheckController { // Implements HealthIndicator after migrating to Spring Boot
  private final DataSource dataSource;

  @GetMapping("healthCheck")
  public void get() throws SQLException {
    try (Connection connection = dataSource.getConnection();
         Statement statement = connection.createStatement()) {
      statement.executeQuery("select 1");
    }
  }
}
