package com.alperensarac.projectmanagementkotlin.domain.model.auth

/**
 * Uygulama içerisindeki kullanıcı modelidir.
 *
 * Retrofit veya Kotlin Serialization anotasyonları içermez.
 * Bu nedenle domain katmanı network teknolojilerinden bağımsızdır.
 */
data class AuthUser(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val email: String,
    val role: String,
    val department: String?,
    val isActive: Boolean,
    val createdAtUtc: String
) {

    /**
     * Admin rolüne sahip olup olmadığını belirtir.
     */
    val isAdmin: Boolean
        get() = role.equals(
            other = ROLE_ADMIN,
            ignoreCase = true
        )

    /**
     * ProjectManager rolüne sahip olup olmadığını belirtir.
     */
    val isProjectManager: Boolean
        get() = role.equals(
            other = ROLE_PROJECT_MANAGER,
            ignoreCase = true
        )

    /**
     * TeamMember rolüne sahip olup olmadığını belirtir.
     */
    val isTeamMember: Boolean
        get() = role.equals(
            other = ROLE_TEAM_MEMBER,
            ignoreCase = true
        )

    private companion object {
        const val ROLE_ADMIN = "Admin"
        const val ROLE_PROJECT_MANAGER = "ProjectManager"
        const val ROLE_TEAM_MEMBER = "TeamMember"
    }
}