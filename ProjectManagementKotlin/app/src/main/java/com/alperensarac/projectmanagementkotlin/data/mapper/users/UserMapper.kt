package com.alperensarac.projectmanagementkotlin.data.mapper.users

import com.alperensarac.projectmanagementkotlin.data.remote.dto.users.UserResponseDto
import com.alperensarac.projectmanagementkotlin.domain.model.users.User

/**
 * Network DTO -> Domain model dönüşümü.
 */
fun UserResponseDto.toDomain(): User {
    return User(
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