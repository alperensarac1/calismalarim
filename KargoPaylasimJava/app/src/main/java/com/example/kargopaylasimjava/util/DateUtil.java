package com.example.kargopaylasimjava.util;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public final class DateUtil {
    private DateUtil() {}

    private static final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    public static Long parseServerDateMs(String serverStr) {
        if (serverStr == null || serverStr.trim().isEmpty()) return null;

        try {
            fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
            return fmt.parse(serverStr).getTime();
        } catch (Exception ignored) {}

        try {
            fmt.setTimeZone(TimeZone.getDefault());
            return fmt.parse(serverStr).getTime();
        } catch (Exception ignored) {}

        return null;
    }

    public static String remainingText(String expiresAt) {
        if (expiresAt == null || expiresAt.trim().isEmpty()) return "-";
        Long exp = parseServerDateMs(expiresAt);
        if (exp == null) return "-";

        long ms = exp - System.currentTimeMillis();
        if (ms <= 0) return "Süresi doldu";

        long totalSec = ms / 1000;
        long min = totalSec / 60;
        long sec = totalSec % 60;

        if (min >= 60) {
            long h = min / 60;
            long m = min % 60;
            return h + "s " + m + "dk";
        }
        return min + "dk " + sec + "sn";
    }
}

