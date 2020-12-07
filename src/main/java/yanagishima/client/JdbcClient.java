package yanagishima.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.inject.Injector;

import yanagishima.repository.TinyOrm;

@Component
public class JdbcClient {
  private final TinyOrm tinyOrm;

  @Autowired
  public JdbcClient(Injector injector) {
    this.tinyOrm = injector.getInstance(TinyOrm.class);
  }

  public void executeQuery(String sql) {
    tinyOrm.executeQuery(sql);
  }
}
