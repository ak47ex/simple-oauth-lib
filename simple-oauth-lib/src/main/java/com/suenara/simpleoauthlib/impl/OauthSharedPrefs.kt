package com.suenara.simpleoauthlib.impl

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import androidx.core.net.toUri
import com.suenara.simpleoauthlib.OauthPrefs
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class OauthSharedPrefs(context: Context) : OauthPrefs {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override var authUri: Uri by UriDelegate(KEY_AUTH_URI)
    override var redirectUri: Uri by UriDelegate(KEY_REDIRECT_URI)
    override var tokenUri: Uri by UriDelegate(KEY_TOKEN_URI)
    override var revokeTokenUri: Uri by UriDelegate(KEY_REVOKE_TOKEN_URI)
    override var code: String by PrefsDelegate(KEY_CODE) { it.orEmpty() }
    override var error: String by PrefsDelegate(KEY_ERROR) { it.orEmpty() }
    override var clientId: String by PrefsDelegate(KEY_CLIENT_ID) { it.orEmpty() }
    override var scopes: List<String>
        get() = prefs.getStringSet(KEY_SCOPES, emptySet()).orEmpty().toList()
        set(value) = prefs.edit { putStringSet(KEY_SCOPES, value.toSet()) }

    override var accessToken: String? by PrefsDelegate(KEY_ACCESS_TOKEN) { it }
    override var tokenType: String? by PrefsDelegate(KEY_TOKEN_TYPE) { it }
    override var refreshToken: String? by PrefsDelegate(KEY_REFRESH_TOKEN) { it }
    override var tokenExpirationDate: Long
        get() = prefs.getLong(KEY_TOKEN_EXPIRATION_DATE, 0L)
        set(value) = prefs.edit { putLong(KEY_TOKEN_EXPIRATION_DATE, value) }

    override var idToken: String? by PrefsDelegate(KEY_ID_TOKEN) { it }

    private open class PrefsDelegate<T>(
        private val key: String,
        private val serializer: (T) -> String? = { it?.toString() },
        private val deserializer: (String?) -> T,
    ) : ReadWriteProperty<OauthSharedPrefs, T> {
        override fun setValue(thisRef: OauthSharedPrefs, property: KProperty<*>, value: T) =
            thisRef.prefs.edit { putString(key, serializer(value)) }

        override fun getValue(thisRef: OauthSharedPrefs, property: KProperty<*>): T =
            thisRef.prefs.getString(key, null).let(deserializer)
    }

    private class UriDelegate(key: String) :
        PrefsDelegate<Uri>(key,
            serializer = { it.toString() },
            deserializer = { it.takeUnless { it.isNullOrEmpty() }?.toUri() ?: Uri.EMPTY }
        )

    companion object {
        private const val PREFS_NAME = "oauth_preferences"

        private const val KEY_AUTH_URI = "authentication_uri"
        private const val KEY_REDIRECT_URI = "redirect_uri"
        private const val KEY_TOKEN_URI = "token_uri"
        private const val KEY_REVOKE_TOKEN_URI = "revoke_token_uri"
        private const val KEY_CODE = "code"
        private const val KEY_ERROR = "error"
        private const val KEY_CLIENT_ID = "client_id"
        private const val KEY_SCOPES = "scopes"

        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_TYPE = "token_type"
        private const val KEY_TOKEN_EXPIRATION_DATE = "token_expires_in"

        private const val KEY_ID_TOKEN = "id_token"
    }
}