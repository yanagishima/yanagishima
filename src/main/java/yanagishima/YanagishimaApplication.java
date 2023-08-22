package yanagishima;

import java.net.InetAddress;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import lombok.extern.slf4j.Slf4j;
import yanagishima.util.DefaultProfileUtil;

@Slf4j
@SpringBootApplication
public class YanagishimaApplication {
  public static void main(String[] args) throws Exception {
    SpringApplication app = new SpringApplication(YanagishimaApplication.class);
    DefaultProfileUtil.addDefaultProfile(app);
    Environment env = app.run(args).getEnvironment();

    String protocol = "http";
    if (env.getProperty("server.ssl.key-store") != null) {
      protocol = "https";
    }

    log.info("\n----------------------------------------------------------\n\t"
             + "Application '{}' is running! Access URLs:\n\t"
             + "Local: \t\t{}://localhost:{}\n\t"
             + "External: \t{}://{}:{}\n\t"
             + "Profile(s): \t{}\n----------------------------------------------------------",
             env.getProperty("spring.application.name"),
             protocol,
             env.getProperty("server.port"),
             protocol,
             InetAddress.getLocalHost().getHostAddress(),
             env.getProperty("server.port"),
             env.getActiveProfiles());

  }
}
