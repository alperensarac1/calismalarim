package com.example.webtrafficviewerjava.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RequestUtils {

    public static boolean shouldIgnoreUrl(String url) {
        if (url == null) return true;

        String lower = url.toLowerCase();

        List<String> ignoredExtensions = Arrays.asList(
                ".png", ".jpg", ".jpeg", ".gif", ".webp", ".svg",
                ".css", ".js", ".map",
                ".woff", ".woff2", ".ttf", ".otf",
                ".ico", ".mp4", ".webm", ".mp3", ".aac", ".m4a"
        );

        for (String ext : ignoredExtensions) {
            if (lower.contains(ext)) {
                return true;
            }
        }

        return false;
    }

    public static boolean looksLikeApi(String url) {
        if (url == null) return false;

        String lower = url.toLowerCase();

        List<String> apiKeywords = Arrays.asList(
                "/api/",
                "graphql",
                ".json",
                "ajax",
                "rest",
                "v1/",
                "v2/",
                "endpoint"
        );

        for (String keyword : apiKeywords) {
            if (lower.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    public static String guessResourceType(String url) {
        if (url == null) return "unknown";

        String lower = url.toLowerCase();

        if (lower.contains(".png") || lower.contains(".jpg") || lower.contains(".jpeg")
                || lower.contains(".webp") || lower.contains(".svg")) {
            return "image";
        }

        if (lower.contains(".css")) return "css";
        if (lower.contains(".js")) return "js";
        if (lower.contains(".mp4") || lower.contains(".webm")) return "video";

        if (lower.contains(".json") || lower.contains("/api/") || lower.contains("graphql")) {
            return "api";
        }

        return "other";
    }

    public static String formatHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return "Header yok";
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append("\n");
        }

        return sb.toString().trim();
    }

    public static String[] splitUrlAndQuery(String fullUrl) {
        if (fullUrl == null) {
            return new String[]{"", ""};
        }

        int index = fullUrl.indexOf("?");
        if (index == -1) {
            return new String[]{fullUrl, ""};
        }

        return new String[]{
                fullUrl.substring(0, index),
                fullUrl.substring(index + 1)
        };
    }

    public static String buildFinalUrl(String baseUrl, String query) {
        if (baseUrl == null) return "";
        if (query == null || query.trim().isEmpty()) return baseUrl.trim();

        String normalizedBase = baseUrl.trim();

        String[] parts = query.split("&");
        StringBuilder encodedQuery = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            int eqIndex = part.indexOf("=");

            if (eqIndex == -1) {
                encodedQuery.append(encode(part));
            } else {
                String key = part.substring(0, eqIndex);
                String value = part.substring(eqIndex + 1);
                encodedQuery.append(key).append("=").append(encode(value));
            }

            if (i < parts.length - 1) {
                encodedQuery.append("&");
            }
        }

        if (normalizedBase.contains("?")) {
            return normalizedBase + "&" + encodedQuery;
        } else {
            return normalizedBase + "?" + encodedQuery;
        }
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    public static String detectMediaType(String bodyText) {
        if (bodyText == null) return "text/plain; charset=utf-8";

        String trimmed = bodyText.trim();

        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            return "application/json; charset=utf-8";
        }

        if (trimmed.contains("=") && trimmed.contains("&")) {
            return "application/x-www-form-urlencoded; charset=utf-8";
        }

        if (trimmed.contains("=")) {
            return "application/x-www-form-urlencoded; charset=utf-8";
        }

        return "text/plain; charset=utf-8";
    }
}
