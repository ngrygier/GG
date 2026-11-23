package pl.gg.crm.common;

import java.util.Locale;

public final class LikePatterns {

    public static final char ESCAPE = '\\';

    private LikePatterns() {
    }

    public static String containsIgnoreCase(String text) {
        String escaped = text.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
