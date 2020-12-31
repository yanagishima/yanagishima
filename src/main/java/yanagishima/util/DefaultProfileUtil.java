package yanagishima.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class DefaultProfileUtil {
  public static final String SPRING_PROFILE_DEFAULT = "spring.profiles.default";
  public static final String SPRING_PROFILE_LOCAL = "local";

  public static void addDefaultProfile(SpringApplication app) {
    Map<String, Object> defaultProperties = new HashMap<>();
    /*
     * The default profile to use when no other profiles are defined
     * This cannot be set in the <code>application.yml</code> file.
     * See https://github.com/spring-projects/spring-boot/issues/1219
     */
    defaultProperties.put(SPRING_PROFILE_DEFAULT, SPRING_PROFILE_LOCAL);
    app.setDefaultProperties(defaultProperties);

    log.info("Added {}: {}", SPRING_PROFILE_DEFAULT, SPRING_PROFILE_LOCAL);
  }
}
