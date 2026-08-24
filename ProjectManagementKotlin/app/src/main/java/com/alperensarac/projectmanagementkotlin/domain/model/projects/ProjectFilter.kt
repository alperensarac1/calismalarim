package com.alperensarac.projectmanagementkotlin.domain.model.projects

/**
 * Proje listesi için kullanılan filtre modelidir.
 *
 * Page ve PageSize değerlerini Paging 3 yöneteceği için burada yalnızca
 * kullanıcı tarafından değiştirilebilen filtreleri tutuyoruz.
 */
data class ProjectFilter(
    val search: String = "",
    val status: String? = null,
    val isArchived: Boolean? = null,
    val ownerId: Int? = null
)