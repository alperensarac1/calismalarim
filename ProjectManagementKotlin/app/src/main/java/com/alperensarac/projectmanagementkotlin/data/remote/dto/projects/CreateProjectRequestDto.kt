package com.alperensarac.projectmanagementkotlin.data.remote.dto.projects

import kotlinx.serialization.Serializable

/**
 * POST /api/Projects isteğinde backend'e gönderilecek DTO.
 *
 * C# karşılığı:
 *
 * CreateProjectRequestDto
 */
@Serializable
data class CreateProjectRequestDto(

    val name: String,

    val description: String? = null,

    /**
     * Backend DateTime bekliyor.
     *
     * Örnek:
     *
     * 2026-08-13T00:00:00
     *
     * Burada timezone eklemiyoruz; kullanıcının seçtiği takvim
     * tarihini doğrudan gönderiyoruz.
     */
    val startDate: String,

    val endDate: String? = null,

    /**
     * Planning / Active / OnHold / Completed / Cancelled
     */
    val status: String,

    /**
     * Backend'de opsiyoneldir.
     */
    val ownerId: Int? = null
)