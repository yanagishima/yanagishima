package yanagishima.exception;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class YanagishimaParseException extends RuntimeException {
  private int errorLineNumber;
  private int errorColumnNumber;

  public YanagishimaParseException(String message, Throwable cause) {
    super(message, cause);
  }
}
