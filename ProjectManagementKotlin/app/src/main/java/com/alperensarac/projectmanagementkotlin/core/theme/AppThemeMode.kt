package com.alperensarac.projectmanagementkotlin.core.theme

/**
 * Uygulamanın desteklediği tema seçenekleri.
 *
 * Şimdilik:
 *
 * - LIGHT -> Aydınlık tema
 * - DARK  -> Karanlık tema
 */
enum class AppThemeMode(
    val storageValue: String
) {

    LIGHT(
        storageValue = "light"
    ),

    DARK(
        storageValue = "dark"
    );

    companion object {

        /**
         * DataStore'dan gelen String değeri tekrar enum'a çevirir.
         *
         * Geçersiz/eski bir değer gelirse LIGHT kullanılır.
         */
        fun fromStorageValue(
            value: String?
        ): AppThemeMode {

            return entries.firstOrNull { mode ->
                mode.storageValue == value
            } ?: LIGHT
        }
    }
}