package com.alperensarac.projectmanagementkotlin.domain.model.tasks

/**
 * Backend TaskPriority enum değerlerinin Android karşılığıdır.
 */
enum class TaskPriority(
    val apiValue: String
) {

    LOW(
        apiValue = "Low"
    ),

    MEDIUM(
        apiValue = "Medium"
    ),

    HIGH(
        apiValue = "High"
    ),

    CRITICAL(
        apiValue = "Critical"
    );

    companion object {

        fun fromApiValue(
            value: String?
        ): TaskPriority? {

            if (value.isNullOrBlank()) {
                return null
            }

            return entries.firstOrNull { priority ->
                priority.apiValue.equals(
                    value,
                    ignoreCase = true
                )
            }
        }
    }
}