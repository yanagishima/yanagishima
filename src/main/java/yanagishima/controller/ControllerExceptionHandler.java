package yanagishima.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
import yanagishima.exception.YanagishimaParseException;
import yanagishima.model.ErrorResponse;
import yanagishima.model.ParseErrorResponse;

@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler {
  @ResponseStatus(HttpStatus.OK) // Return 200 for backward compatibility
  @ExceptionHandler(YanagishimaParseException.class)
  public ParseErrorResponse handle(YanagishimaParseException e) {
    return getErrorResponse(e);
  }

  @ResponseStatus(HttpStatus.OK) // Return 200 for backward compatibility
  @ExceptionHandler(Throwable.class)
  public ErrorResponse handle(Throwable e) {
    log.error(e.getMessage(), e);
    return getErrorResponse(e);
  }

  private static ParseErrorResponse getErrorResponse(YanagishimaParseException e) {
    return new ParseErrorResponse(e.getMessage(), e.getErrorLineNumber(), e.getErrorColumnNumber());
  }

  private static ErrorResponse getErrorResponse(Throwable e) {
    return new ErrorResponse(e.getMessage());
  }
}
