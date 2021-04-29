package yanagishima.config;

import java.util.List;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;
import yanagishima.interceptor.DatasourceInterceptor;
import yanagishima.interceptor.LoggingInterceptor;
import yanagishima.resolver.UserArgumentResolver;

@Configuration
@ComponentScan(basePackages = { "yanagishima.controller" })
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
  private final DatasourceInterceptor datasourceInterceptor;
  private final LoggingInterceptor loggingInterceptor;
  private final UserArgumentResolver userArgumentResolver;

  @Override
  public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
    configurer.enable();
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(datasourceInterceptor)
        .addPathPatterns("/**");

    registry
        .addInterceptor(loggingInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns("/swagger-resources/**", "/swagger-ui.html", "/swagger-ui/index.html",
                             "/webjars/**", "/error", "/csrf", "/healthCheck");
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    argumentResolvers.add(userArgumentResolver);
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
        .addResourceHandler("swagger-ui.html")
        .addResourceLocations("classpath:/META-INF/resources/");
  }
}
