package com.alperensarac.projectmanagementkotlin.feature.projects.detail

/**
 * ProjectDetail ekranında tek sefer tüketilecek UI event'leri.
 *
 * StateFlow:
 * Kalıcı ekran durumları içindir.
 *
 * Channel/Event:
 * Snackbar, navigation gibi tek seferlik olaylar içindir.
 */
sealed interface ProjectDetailUiEvent {

    /**
     * Üye rolü değiştirme, üyeyi çıkarma gibi
     * yalnızca mesaj gösterilecek işlemler.
     */
    data class ShowMessage(
        val message: String
    ) : ProjectDetailUiEvent

    /**
     * Projenin archive durumu gibi proje listesini de
     * etkileyen bir değişiklik gerçekleşti.
     */
    data class ProjectChanged(
        val message: String
    ) : ProjectDetailUiEvent

    /**
     * Proje tamamen silindi.
     *
     * Bu event geldiğinde detay ekranından çıkacağız.
     */
    data class ProjectDeleted(
        val message: String
    ) : ProjectDetailUiEvent
}