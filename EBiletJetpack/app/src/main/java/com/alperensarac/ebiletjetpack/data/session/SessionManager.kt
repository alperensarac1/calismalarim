package com.alperensarac.ebiletjetpack.data.session


import android.content.Context
import com.alperensarac.ebiletjetpack.data.model.User

/*
    SessionManager

    Kullanıcı giriş yaptıktan sonra token ve profil bilgilerini
    SharedPreferences içinde saklar.

    Compose tarafında da aynı mantık geçerli:
    - API token alınır
    - SharedPreferences'a yazılır
    - Ekranlar bu token ile API çağırır
*/
class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(
        "event_ticket_compose_session",
        Context.MODE_PRIVATE
    )

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

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

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

    fun getRole(): String {
        return prefs.getString(KEY_ROLE, "user") ?: "user"
    }

    fun isStaffOrAdmin(): Boolean {
        val role = getRole()
        return role == "staff" || role == "admin"
    }

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