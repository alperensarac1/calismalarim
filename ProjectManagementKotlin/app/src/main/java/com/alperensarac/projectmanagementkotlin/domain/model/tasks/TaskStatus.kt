package com.alperensarac.projectmanagementkotlin.domain.model.tasks

/**
 * Backend ProjectTaskStatus enum değerlerinin Android karşılığıdır.
 */
enum class TaskStatus(
    val apiValue: String
) {

    TODO(
        apiValue = "Todo"
    ),

    IN_PROGRESS(
        apiValue = "InProgress"
    ),

    IN_REVIEW(
        apiValue = "InReview"
    ),

    DONE(
        apiValue = "Done"
    );

    companion object {

        fun fromApiValue(
            value: String?
        ): TaskStatus? {

            if (value.isNullOrBlank()) {
                return null
            }

            return entries.firstOrNull { status ->
                status.apiValue.equals(
                    value,
                    ignoreCase = true
                )
            }
        }
    }
}