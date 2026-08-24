package com.alperensarac.projectmanagementkotlin.domain.model.users

/**
 * Kullanıcı listeleme ve seçim ekranlarında kullanılan domain modelidir.
 */
data class User(
    val id: Int,

    val firstName: String,

    val lastName: String,

    val fullName: String,

    val email: String,

    val role: String,

    val department: String?,

    val isActive: Boolean,

    val createdAtUtc: String
)