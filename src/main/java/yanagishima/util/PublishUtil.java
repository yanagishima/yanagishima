package yanagishima.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class PublishUtil {

    public static boolean canAccessPublishedPage(String publishUser, String requestUser, String viewers) {
        if (publishUser != null && publishUser.equals(requestUser)) {
            return true;
        }

        if (viewers != null && viewers.contains(requestUser)) {
            return true;
        }

        return false;
    }
}
