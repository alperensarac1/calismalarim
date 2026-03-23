package com.example.webtrafficviewerkotlin.util

object RequestFilterUtils {

    private val ignoredExtensions = listOf(
        ".png", ".jpg", ".jpeg", ".gif", ".webp", ".svg",
        ".css", ".js", ".map",
        ".woff", ".woff2", ".ttf", ".otf",
        ".ico", ".mp4", ".webm", ".mp3", ".aac", ".m4a"
    )

    private val apiKeywords = listOf(
        "/api/",
        "graphql",
        ".json",
        "ajax",
        "rest",
        "v1/",
        "v2/",
        "endpoint"
    )

    fun shouldIgnore(url: String): Boolean {
        val lower = url.lowercase()
        return ignoredExtensions.any { lower.contains(it) }
    }

    fun looksLikeApi(url: String): Boolean {
        val lower = url.lowercase()
        return apiKeywords.any { lower.contains(it) }
    }

    fun guessResourceType(url: String): String {
        val lower = url.lowercase()

        return when {
            lower.contains(".png") || lower.contains(".jpg") || lower.contains(".jpeg") ||
                    lower.contains(".webp") || lower.contains(".svg") -> "image"

            lower.contains(".css") -> "css"
            lower.contains(".js") -> "js"
            lower.contains(".json") || lower.contains("/api/") || lower.contains("graphql") -> "api"
            lower.contains(".mp4") || lower.contains(".webm") -> "video"
            else -> "other"
        }
    }
}