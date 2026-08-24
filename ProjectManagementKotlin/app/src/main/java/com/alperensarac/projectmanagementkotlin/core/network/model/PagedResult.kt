package com.alperensarac.projectmanagementkotlin.core.network.model

import kotlinx.serialization.Serializable

/**
 * Backend tarafından kullanılan sayfalama response modelidir.
 *
 * Projeler, görevler, kullanıcılar ve mailbox listelerinde kullanılacaktır.
 *
 * Paging 3 entegrasyonu sırasında:
 *
 * - page mevcut sayfayı,
 * - hasNextPage sonraki sayfanın olup olmadığını,
 * - items ise ekrana gösterilecek kayıtları
 *
 * belirlemek için kullanılacaktır.
 */
@Serializable
data class PagedResult<T>(
    /**
     * Mevcut sayfadaki kayıtlar.
     */
    val items: List<T> = emptyList(),

    /**
     * Mevcut sayfa numarası.
     *
     * Backend sayfalaması 1'den başlamaktadır.
     */
    val page: Int,

    /**
     * Sayfa başına döndürülen kayıt sayısı.
     */
    val pageSize: Int,

    /**
     * Filtreye uyan toplam kayıt sayısı.
     */
    val totalCount: Int,

    /**
     * Toplam sayfa sayısı.
     */
    val totalPages: Int,

    /**
     * Önceki sayfanın bulunup bulunmadığını belirtir.
     */
    val hasPreviousPage: Boolean,

    /**
     * Sonraki sayfanın bulunup bulunmadığını belirtir.
     */
    val hasNextPage: Boolean
)