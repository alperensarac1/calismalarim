package com.alperensarac.ebiletkotlin.data.session


import android.content.Context
import com.alperensarac.ebiletkotlin.data.model.User

/*
    SessionManager

    Kullanıcı giriş yaptıktan sonra bazı bilgileri telefonda saklamamız gerekir:

    - api_token
    - user_id
    - full_name
    - email
    - role

    Bunları SharedPreferences içinde saklayacağız.

    SharedPreferences:
    Küçük verileri cihazda kalıcı şekilde tutar.
    Uygulama kapansa bile bilgiler silinmez.

    Not:
    Çok hassas sistemlerde token saklama için daha güvenli çözümler kullanılabilir.
    Ama bu eğitim projesi için SharedPreferences yeterli.
*/
class SessionManager(context: Context) {

    /*
        SharedPreferences dosya adı.
    */
    private val prefs = context.getSharedPreferences(
        "event_ticket_session",
        Context.MODE_PRIVATE
    )

    /*
        Kullanıcı giriş yaptıktan sonra bilgileri kaydeder.
    */
    fun saveUser(user: User) {
        prefs.edit()
            .putInt(KEY_USER_ID, user.id)
            .putString(KEY_FULL_NAME, user.fullName)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_PHONE, user.phone)
            .putString(KEY_ROLE, user.role)
            .putString(KEY_API_TOKEN, user.apiToken)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    /*
        Kullanıcının giriş yapıp yapmadığını döndürür.
    */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /*
        API token değerini döndürür.

        Backend'e giden çoğu istekte bu token gerekli olacak.
    */
    fun getApiToken(): String {
        return prefs.getString(KEY_API_TOKEN, "") ?: ""
    }

    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, 0)
    }

    fun getFullName(): String {
        return prefs.getString(KEY_FULL_NAME, "") ?: ""
    }

    fun getEmail(): String {
        return prefs.getString(KEY_EMAIL, "") ?: ""
    }

    fun getPhone(): String {
        return prefs.getString(KEY_PHONE, "") ?: ""
    }

    /*
        role değerleri:
        user
        staff
        admin
    */
    fun getRole(): String {
        return prefs.getString(KEY_ROLE, "user") ?: "user"
    }

    /*
        Kullanıcı staff veya admin mi?
        QR kontrol ekranını sadece bu kullanıcılara açacağız.
    */
    fun isStaffOrAdmin(): Boolean {
        val role = getRole()
        return role == "staff" || role == "admin"
    }

    /*
        Çıkış yapma.

        Tüm kayıtlı session bilgilerini temizler.
    */
    fun logout() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_PHONE = "phone"
        private const val KEY_ROLE = "role"
        private const val KEY_API_TOKEN = "api_token"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
}