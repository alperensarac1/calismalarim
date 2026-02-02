object DateUtil {

    private val fmt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)

    fun parseServerDateMs(serverStr: String): Long? {
        if (serverStr.isBlank()) return null

        // 1) UTC olarak dene
        try {
            fmt.timeZone = java.util.TimeZone.getTimeZone("UTC")
            return fmt.parse(serverStr)?.time
        } catch (_: Exception) {}

        // 2) Olmadı local dene
        return try {
            fmt.timeZone = java.util.TimeZone.getDefault()
            fmt.parse(serverStr)?.time
        } catch (_: Exception) { null }
    }

    fun remainingText(expiresAt: String?): String {
        if (expiresAt.isNullOrBlank()) return "-"
        val exp = parseServerDateMs(expiresAt) ?: return "-"
        val ms = exp - System.currentTimeMillis()

        if (ms <= 0) return "Süresi doldu"

        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60

        return if (min >= 60) {
            val h = min / 60
            val m = min % 60
            "${h}s ${m}dk"
        } else {
            "${min}dk ${sec}sn"
        }
    }
}
