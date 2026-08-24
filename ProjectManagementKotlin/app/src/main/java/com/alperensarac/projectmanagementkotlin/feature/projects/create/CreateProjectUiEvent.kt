package com.alperensarac.projectmanagementkotlin.feature.projects.create

/**
 * Bir kere tüketilmesi gereken olayları StateFlow yerine event olarak
 * gönderiyoruz.
 */
sealed interface CreateProjectUiEvent {

    /**
     * Proje başarıyla oluşturuldu.
     *
     * Oluşturulan projenin ID'sini de taşıyoruz.
     */
    data class ProjectCreated(
        val projectId: Int,
        val message: String
    ) : CreateProjectUiEvent
}