package br.com.wtc_aplicattion.services

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREFS_NAME = "wtc_prefs"
    private const val KEY_TOKEN = "token"
    private const val KEY_REFRESH = "refresh_token"
    private const val KEY_EMAIL = "email"
    private const val KEY_ROLE = "role"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_NAME = "display_name"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(
        token: String,
        refreshToken: String,
        email: String,
        role: String,
        userId: String,
        displayName: String
    ) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_REFRESH, refreshToken)
            .putString(KEY_EMAIL, email)
            .putString(KEY_ROLE, role)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_NAME, displayName)
            .apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH, null)
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)
    fun getRole(): String? = prefs.getString(KEY_ROLE, null)
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    fun getDisplayName(): String? = prefs.getString(KEY_NAME, null)

    fun getBearerToken(): String? = getToken()?.let { "Bearer $it" }

    fun clearToken() {
        prefs.edit().clear().apply()
    }
}
