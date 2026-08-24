package com.alperensarac.projectmanagementkotlin.data.mapper.projects

import com.alperensarac.projectmanagementkotlin.data.remote.dto.projects.ProjectMemberResponseDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.projects.ProjectResponseDto
import com.alperensarac.projectmanagementkotlin.domain.model.projects.Project
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMember

/**
 * ProjectResponseDto -> Project
 */
fun ProjectResponseDto.toDomain(): Project {
    return Project(
        id = id,
        name = name,
        description = description,
        startDateUtc = startDate,
        endDateUtc = endDate,
        status = status,
        ownerId = ownerId,
        ownerFullName = ownerFullName,
        ownerEmail = ownerEmail,
        isArchived = isArchived,
        archivedAtUtc = archivedAt,
        memberCount = memberCount,
        taskCount = taskCount,
        createdAtUtc = createdAt,
        updatedAtUtc = updatedAt
    )
}

/**
 * ProjectMemberResponseDto -> ProjectMember
 */
fun ProjectMemberResponseDto.toDomain(): ProjectMember {
    return ProjectMember(
        id = id,
        projectId = projectId,
        userId = userId,
        firstName = firstName,
        lastName = lastName,
        fullName = fullName,
        email = email,
        systemRole = systemRole,
        projectRole = projectRole,
        joinedAtUtc = joinedAt,
        isActive = isActive,
        isProjectOwner = isProjectOwner
    )
}