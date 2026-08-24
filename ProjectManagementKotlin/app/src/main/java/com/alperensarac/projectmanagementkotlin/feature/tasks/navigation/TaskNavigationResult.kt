package com.alperensarac.projectmanagementkotlin.feature.tasks.navigation

/**
 * Task modülü içerisindeki ekranlar arasında kullanılan navigation
 * sonuç anahtarlarını tek yerde toplar.
 *
 * Örneğin:
 *
 * TaskDetailFragment
 *        ↓
 * görev güncellendi
 *        ↓
 * TasksFragment
 *        ↓
 * PagingDataAdapter.refresh()
 *
 * Bu yapıyla Fragment'ler birbirlerini doğrudan çağırmaz.
 */
object TaskNavigationResult {

    /**
     * Bir görevin:
     *
     * - güncellenmesi,
     * - status değiştirmesi,
     * - assignment değiştirmesi
     *
     * durumunda kullanılır.
     */
    const val TASK_CHANGED =
        "task_changed"

    /**
     * Görev tamamen silindiğinde kullanılır.
     */
    const val TASK_DELETED =
        "task_deleted"

    /**
     * Değişen/silinen görevin id değeridir.
     */
    const val TASK_ID =
        "task_result_id"
}