package com.alperensarac.projectmanagementkotlin.data.mapper.auth

import com.alperensarac.projectmanagementkotlin.data.remote.dto.auth.AuthSessionDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.auth.AuthUserDto
import com.alperensarac.projectmanagementkotlin.domain.model.auth.AuthSession
import com.alperensarac.projectmanagementkotlin.domain.model.auth.AuthUser

/**
 * Network DTO modellerini domain modellerine dönüştüren mapper fonksiyonlarıdır.
 *
 * Bu dönüşüm sayesinde backend model değişiklikleri doğrudan UI katmanını
 * etkilemez.
 */
fun AuthUserDto.toDomain(): AuthUser {
    return AuthUser(
        id = id,
        firstName = firstName,
        lastName = lastName,
        fullName = fullName,
        email = email,
        role = role,
        department = department,
        isActive = isActive,
        createdAtUtc = createdAt
    )
}

/**
 * Login veya refresh response modelini domain oturum modeline dönüştürür.
 */
fun AuthSessionDto.toDomain(): AuthSession {
    return AuthSession(
        accessToken = accessToken,
        refreshToken = refreshToken,
        tokenType = tokenType,
        expiresAtUtc = expiresAtUtc,
        user = user.toDomain()
    )
}