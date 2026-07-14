package com.alperensarac.ebiletjava.data.session;

import android.content.Context;
import android.content.SharedPreferences;

import com.alperensarac.ebiletjava.data.model.User;

/*
    SessionManager.java

    Kullanıcı giriş yaptıktan sonra bazı bilgileri telefonda saklamamız gerekir:

    - api_token
    - user_id
    - full_name
    - email
    - role

    Bunları SharedPreferences ile saklıyoruz.

    SharedPreferences:
    Küçük verileri cihazda kalıcı tutar.
    Uygulama kapanıp açılsa bile silinmez.
*/
public class SessionManager {

    private static final String PREF_NAME = "event_ticket_java_session";

    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ROLE = "role";
    private static final String KEY_API_TOKEN = "api_token";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /*
        Kullanıcı giriş/kayıt sonrası bilgileri kaydeder.
    */
    public void saveUser(User user) {
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_FULL_NAME, user.getFullName());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_PHONE, user.getPhone());
        editor.putString(KEY_ROLE, user.getRole());
        editor.putString(KEY_API_TOKEN, user.getApiToken());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);

        editor.apply();
    }

    /*
        Kullanıcı giriş yapmış mı?
    */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /*
        Backend isteklerinde kullanacağımız token.
    */
    public String getApiToken() {
        return prefs.getString(KEY_API_TOKEN, "");
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, 0);
    }

    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getPhone() {
        return prefs.getString(KEY_PHONE, "");
    }

    /*
        role değerleri:
        user
        staff
        admin
    */
    public String getRole() {
        return prefs.getString(KEY_ROLE, "user");
    }

    /*
        QR kontrol ekranı sadece staff/admin kullanıcıda açılacak.
    */
    public boolean isStaffOrAdmin() {
        String role = getRole();

        return "staff".equals(role) || "admin".equals(role);
    }

    /*
        Çıkış yapma.
        Tüm session bilgilerini siler.
    */
    public void logout() {
        prefs.edit().clear().apply();
    }
}
