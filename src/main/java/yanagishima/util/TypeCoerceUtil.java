package yanagishima.util;

import java.math.BigDecimal;

public final class TypeCoerceUtil {
    private TypeCoerceUtil() { }

    public static String objectToString(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof Long) {
            return ((Long) object).toString();
        }
        if (object instanceof Double) {
            if (Double.isNaN((Double)object) || Double.isInfinite((Double) object)) {
                return object.toString();
            }
            return BigDecimal.valueOf((Double) object).toPlainString();
        }
        return object.toString();
    }
}
