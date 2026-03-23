package com.example.webtrafficviewerjetpack.util


import java.net.URLEncoder

object RequestUtils {

    fun shouldIgnoreUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return true

        val lower = url.lowercase()

        val ignoredExtensions = listOf(
            ".png", ".jpg", ".jpeg", ".gif", ".webp", ".svg",
            ".css", ".js", ".map",
            ".woff", ".woff2", ".ttf", ".otf",
            ".ico", ".mp4", ".webm", ".mp3", ".aac", ".m4a"
        )

        return ignoredExtensions.any { lower.contains(it) }
    }

    fun looksLikeApi(url: String?): Boolean {
        if (url.isNullOrBlank()) return false

        val lower = url.lowercase()

        val apiKeywords = listOf(
            "/api/",
            "graphql",
            ".json",
            "ajax",
            "rest",
            "v1/",
            "v2/",
            "endpoint"
        )

        return apiKeywords.any { lower.contains(it) }
    }

    fun guessResourceType(url: String?): String {
        if (url.isNullOrBlank()) return "unknown"

        val lower = url.lowercase()

        return when {
            lower.contains(".png") || lower.contains(".jpg") || lower.contains(".jpeg") ||
                    lower.contains(".webp") || lower.contains(".svg") -> "image"

            lower.contains(".css") -> "css"
            lower.contains(".js") -> "js"
            lower.contains(".mp4") || lower.contains(".webm") -> "video"
            lower.contains(".json") || lower.contains("/api/") || lower.contains("graphql") -> "api"
            else -> "other"
        }
    }

    fun formatHeaders(headers: Map<String, String>): String {
        if (headers.isEmpty()) return "Header yok"

        return buildString {
            headers.forEach { (key, value) ->
                append("$key: $value\n")
            }
        }.trim()
    }

    fun splitUrlAndQuery(fullUrl: String?): Pair<String, String> {
        if (fullUrl.isNullOrBlank()) return "" to ""

        val index = fullUrl.indexOf("?")
        return if (index == -1) {
            fullUrl to ""
        } else {
            fullUrl.substring(0, index) to fullUrl.substring(index + 1)
        }
    }

    fun buildFinalUrl(baseUrl: String, query: String): String {
        if (query.isBlank()) return baseUrl.trim()

        val normalizedBase = baseUrl.trim()

        val encodedQuery = query
            .split("&")
            .joinToString("&") { part ->
                val eqIndex = part.indexOf("=")
                if (eqIndex == -1) {
                    URLEncoder.encode(part, "UTF-8")
                } else {
                    val key = part.substring(0, eqIndex)
                    val value = part.substring(eqIndex + 1)
                    "$key=${URLEncoder.encode(value, "UTF-8")}"
                }
            }

        return if (normalizedBase.contains("?")) {
            "$normalizedBase&$encodedQuery"
        } else {
            "$normalizedBase?$encodedQuery"
        }
    }

    fun detectMediaType(bodyText: String): String {
        val trimmed = bodyText.trim()

        return when {
            trimmed.startsWith("{") || trimmed.startsWith("[") ->
                "application/json; charset=utf-8"

            trimmed.contains("=") && trimmed.contains("&") ->
                "application/x-www-form-urlencoded; charset=utf-8"

            trimmed.contains("=") ->
                "application/x-www-form-urlencoded; charset=utf-8"

            else ->
                "text/plain; charset=utf-8"
        }
    }
}