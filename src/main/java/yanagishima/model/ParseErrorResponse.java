package yanagishima.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ParseErrorResponse {
  private final String error;
  private final int errorLineNumber;
  private final int errorColumnNumber;
}
