package com.example.yardimuygulamajava.util;

import java.util.Locale;

public final class TimeUtils {

    private TimeUtils() {
        // instance alınmasın diye
    }

    public static String formatRemainingSeconds(int seconds) {
        int s = Math.max(seconds, 0);
        int mm = s / 60;
        int ss = s % 60;
        return String.format(Locale.US, "%02d:%02d", mm, ss);
    }
}
