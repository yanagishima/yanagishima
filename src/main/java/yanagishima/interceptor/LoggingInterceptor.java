package yanagishima.interceptor;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.config.YanagishimaConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingInterceptor extends HandlerInterceptorAdapter {
  private static final String REQUEST_ATTRIBUTE_START_TIME = "startTime";

  private final YanagishimaConfig config;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    log.info("[preHandle] uid: {}, method: {}, path: {}",
             getUid(request), request.getMethod(), request.getRequestURI());

    request.setAttribute(REQUEST_ATTRIBUTE_START_TIME, System.nanoTime());

    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    long elapsed = System.nanoTime() - (long) request.getAttribute(REQUEST_ATTRIBUTE_START_TIME);

    log.info("[afterCompletion] uid: {}, method: {}, path: {}, status: {}, error: {}, elapsed_ms: {}",
             getUid(request), request.getMethod(), request.getRequestURI(),
             response.getStatus(), ex, elapsed / 1000000);
  }

  @Nullable
  private String getUid(HttpServletRequest request) {
    if (config.isUseAuditHttpHeaderName()) {
      return request.getHeader(config.getAuditHttpHeaderName());
    }
    return null;
  }
}
