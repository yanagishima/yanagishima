package yanagishima.util;

import java.io.IOException;

import javax.annotation.Nullable;

import org.komamitsu.fluency.Fluency;

import yanagishima.config.YanagishimaConfig;

public final class FluentdUtil {
    private FluentdUtil() {}

    @Nullable
    public static Fluency buildStaticFluency(YanagishimaConfig config) {
        if (config.getFluentdExecutedTag().isPresent() || config.getFluentdFaliedTag().isPresent()) {
            try {
                return Fluency.defaultFluency(config.getFluentdHost(), config.getFluentdPort());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
