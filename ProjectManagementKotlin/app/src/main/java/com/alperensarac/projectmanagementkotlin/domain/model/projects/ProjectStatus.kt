package com.alperensarac.projectmanagementkotlin.domain.model.projects

/**
 * Backend ProjectStatus enum'unun Android karşılığıdır.
 *
 * Backend:
 *
 * Planning  = 1
 * Active    = 2
 * OnHold    = 3
 * Completed = 4
 * Cancelled = 5
 *
 * API üzerinde enum'ları String olarak kullandığımız için apiValue alanı
 * backend enum adını birebir taşır.
 */
enum class ProjectStatus(
    val apiValue: String,
    val displayName: String
) {

    PLANNING(
        apiValue = "Planning",
        displayName = "Planlama"
    ),

    ACTIVE(
        apiValue = "Active",
        displayName = "Aktif"
    ),

    ON_HOLD(
        apiValue = "OnHold",
        displayName = "Beklemede"
    ),

    COMPLETED(
        apiValue = "Completed",
        displayName = "Tamamlandı"
    ),

    CANCELLED(
        apiValue = "Cancelled",
        displayName = "İptal Edildi"
    );

    companion object {

        fun fromApiValue(
            value: String?
        ): ProjectStatus? {

            return entries.firstOrNull {
                it.apiValue.equals(
                    value,
                    ignoreCase = true
                )
            }
        }
    }
}