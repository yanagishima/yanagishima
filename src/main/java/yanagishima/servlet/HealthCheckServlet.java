package yanagishima.servlet;

import lombok.RequiredArgsConstructor;
import yanagishima.client.JdbcClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthCheckServlet {
    private final JdbcClient jdbcClient;

    @GetMapping("healthCheck")
    public void get() {
        jdbcClient.executeQuery("select 1");
    }
}
