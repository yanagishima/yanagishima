package yanagishima.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import yanagishima.exception.YanagishimaParseException;
import yanagishima.model.ParseErrorResponse;

@RestControllerAdvice
public class ControllerExceptionHandler {
  @ResponseStatus(HttpStatus.OK) // Return 200 for backward compatibility
  @ExceptionHandler(YanagishimaParseException.class)
  public ParseErrorResponse handle(YanagishimaParseException e) {
    return getErrorResponse(e);
  }

  private static ParseErrorResponse getErrorResponse(YanagishimaParseException e) {
    return new ParseErrorResponse(e.getMessage(), e.getErrorLineNumber(), e.getErrorColumnNumber());
  }
}
