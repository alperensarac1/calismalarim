package com.alperensarac.projectmanagementkotlin.domain.model.projects

/**
 * Kullanıcının proje içerisindeki rolünü temsil eder.
 *
 * DİKKAT:
 *
 * Bu roller sistem rolleri değildir.
 *
 * Sistem rolleri:
 *
 * Admin
 * ProjectManager
 * TeamMember
 *
 * Proje rolleri:
 *
 * Member
 * Contributor
 * Viewer
 *
 * Örneğin bir kullanıcının:
 *
 * systemRole  = TeamMember
 * projectRole = Viewer
 *
 * olması mümkündür.
 */
enum class ProjectMemberRole(
    val apiValue: String
) {

    MEMBER(
        apiValue = "Member"
    ),

    CONTRIBUTOR(
        apiValue = "Contributor"
    ),

    VIEWER(
        apiValue = "Viewer"
    );

    companion object {

        /**
         * Backend'den gelen String değeri enum'a çevirmeye çalışır.
         *
         * Yeni bir backend rolü eklenirse uygulama crash olmaz;
         * null döner.
         */
        fun fromApiValue(
            value: String?
        ): ProjectMemberRole? {
            if (value.isNullOrBlank()) {
                return null
            }

            return entries.firstOrNull { role ->
                role.apiValue.equals(
                    value,
                    ignoreCase = true
                )
            }
        }
    }
}