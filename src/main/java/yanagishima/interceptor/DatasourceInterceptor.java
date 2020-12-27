package yanagishima.interceptor;

import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import lombok.RequiredArgsConstructor;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.config.YanagishimaConfig;

@Component
@RequiredArgsConstructor
public class DatasourceInterceptor extends HandlerInterceptorAdapter {
  private final YanagishimaConfig config;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (handler instanceof ResourceHttpRequestHandler) {
      return true;
    }

    if (!(handler instanceof HandlerMethod)) {
      return true;
    }

    Method method = ((HandlerMethod) handler).getMethod();
    DatasourceAuth annotation = AnnotationUtils.findAnnotation(method, DatasourceAuth.class);
    if (annotation == null) {
      return true;
    }

    if (config.isCheckDatasource() && !validateDatasource(request, request.getParameter("datasource"))) {
      sendForbiddenError(response);
      return false;
    }
    return true;
  }
}
