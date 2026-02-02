package com.example.kargopaylasimjava.util;

public final class PhoneUtil {
    private PhoneUtil() {}

    public static String normalizeTrToE164(String input) {
        if (input == null) return "";
        String raw = input.trim().replace(" ", "").replace("-", "");
        if (raw.isEmpty()) return raw;

        if (raw.startsWith("+90")) return "+90" + raw.substring(3).replaceAll("\\D+", "");
        if (raw.startsWith("90") && raw.length() >= 12) return "+90" + raw.substring(2).replaceAll("\\D+", "");
        if (raw.startsWith("0")) return "+90" + raw.substring(1).replaceAll("\\D+", "");
        if (raw.startsWith("5")) return "+90" + raw.replaceAll("\\D+", "");
        return raw;
    }

    public static boolean isLikelyTrPhoneE164(String phone) {
        if (phone == null || !phone.startsWith("+90")) return false;
        String digits = phone.substring(1).replaceAll("\\D+", ""); // "90xxxxxxxxxx"
        return digits.length() == 12;
    }
}

