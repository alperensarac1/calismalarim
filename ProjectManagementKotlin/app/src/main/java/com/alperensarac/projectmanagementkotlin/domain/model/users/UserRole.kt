package com.alperensarac.projectmanagementkotlin.domain.model.users

/**
 * Backend UserRole enum'unun Android karşılığıdır.
 *
 * Backend:
 *
 * Admin          = 1
 * ProjectManager = 2
 * TeamMember     = 3
 *
 * API JSON tarafında enum isimlerini kullanıyoruz.
 */
enum class UserRole(
    val apiValue: String,
    val displayName: String
) {

    ADMIN(
        apiValue = "Admin",
        displayName = "Admin"
    ),

    PROJECT_MANAGER(
        apiValue = "ProjectManager",
        displayName = "Proje Yöneticisi"
    ),

    TEAM_MEMBER(
        apiValue = "TeamMember",
        displayName = "Takım Üyesi"
    );

    companion object {

        fun fromApiValue(
            value: String?
        ): UserRole? {

            return entries.firstOrNull { role ->

                role.apiValue.equals(
                    value,
                    ignoreCase = true
                )
            }
        }
    }
}