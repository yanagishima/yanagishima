package yanagishima.util;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;

import java.util.List;
import java.util.stream.Collectors;

public final class PrestoQueryProvider {
    private PrestoQueryProvider() { }

    public static String listToValues(List<String> lines, int maxLines) {
        checkState(lines.size() >= 2, "At least, there must be 2 lines");
        checkState(lines.size() <= maxLines, format("At most, there must be %d lines", maxLines));

        List<String> rows = lines.stream().skip(1).map(line -> format("(%s)", line)).collect(Collectors.toList());
        return format("SELECT * FROM ( VALUES\n%s ) AS t (%s)", String.join(",\n", rows), lines.get(0));
    }
}
