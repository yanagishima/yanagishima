package yanagishima.client.fluentd;

import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.komamitsu.fluency.Fluency;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.config.YanagishimaConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class FluencyClient {
  private final YanagishimaConfig config;
  private Fluency fluency;

  @PostConstruct
  public void postConstruct() {
    if (config.getFluentdExecutedTag().isPresent() || config.getFluentdFaliedTag().isPresent()
            || config.getFluentdPublishTag().isPresent()) {
      this.fluency = Fluency.defaultFluency(config.getFluentdHost(), config.getFluentdPort());
    } else {
      this.fluency = null;
    }
  }

  public void emitExecuted(Map<String, Object> event) {
    if (config.getFluentdExecutedTag().isEmpty()) {
      return;
    }

    try {
      fluency.emit(config.getFluentdExecutedTag().get(), event);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
  }

  public void emitFailed(Map<String, Object> event) {
    if (config.getFluentdFaliedTag().isEmpty()) {
      return;
    }

    try {
      fluency.emit(config.getFluentdFaliedTag().get(), event);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
  }

  public void emitPublish(Map<String, Object> event) {
    if (config.getFluentdPublishTag().isEmpty()) {
      return;
    }

    try {
      fluency.emit(config.getFluentdPublishTag().get(), event);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
  }
}
