package com.alperensarac.projectmanagementkotlin.core.datastore.token

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

/**
 * Token bilgilerinin tutulduğu Preferences DataStore tanımıdır.
 *
 * Bu extension application Context üzerinden kullanılmalıdır.
 *
 * Dosya adı:
 *
 * project_management_token_store.preferences_pb
 *
 * Refresh token bu dosyaya düz metin olarak değil, Android Keystore ile
 * şifrelenmiş biçimde yazılacaktır.
 */
val Context.tokenDataStore by preferencesDataStore(
    name = "project_management_token_store"
)