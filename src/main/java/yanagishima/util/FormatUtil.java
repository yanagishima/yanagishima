package yanagishima.util;

import javax.annotation.Nullable;

import io.airlift.units.DataSize;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FormatUtil {
  @Nullable
  public static String toSuccinctDataSize(Long size) {
    if (size == null) {
      return null;
    }
    return DataSize.ofBytes(size).succinct().toString();
  }
}
