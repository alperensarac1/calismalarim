package com.alperensarac.projectmanagementkotlin.feature.projects

/**
 * Projeler ekranındaki filtre durumudur.
 *
 * PagingData ayrı Flow üzerinden yayınlanacağı için listeyi bu state
 * içerisinde tutmuyoruz.
 */
data class ProjectsUiState(
    val search: String = "",
    val selectedStatus: String? = null,
    val archivedFilter: ArchivedFilter = ArchivedFilter.ACTIVE_ONLY
)

/**
 * Arşiv filtresini nullable Boolean'dan daha okunabilir şekilde
 * temsil ediyoruz.
 */
enum class ArchivedFilter {
    /**
     * IsArchived=false
     */
    ACTIVE_ONLY,

    /**
     * IsArchived=true
     */
    ARCHIVED_ONLY,

    /**
     * IsArchived query parametresi gönderilmez.
     */
    ALL
}