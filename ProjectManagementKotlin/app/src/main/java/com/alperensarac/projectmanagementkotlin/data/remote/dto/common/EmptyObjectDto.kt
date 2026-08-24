package com.alperensarac.projectmanagementkotlin.data.remote.dto.common
import kotlinx.serialization.Serializable

/**
 * Backend'in:
 *
 * data: {}
 *
 * şeklinde döndürdüğü response'ları deserialize etmek için kullanılır.
 *
 * Özellikle:
 *
 * DELETE /api/projects/{projectId}/members/{userId}
 *
 * endpointinde kullanacağız.
 */
@Serializable
class EmptyObjectDto