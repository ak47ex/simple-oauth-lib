package com.suenara.simpleoauthlib.impl.network

import androidx.core.net.toUri
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class OauthResultFactory {

    @Throws(JSONException::class, IOException::class)
    fun parseSuccessResponse(body: String): OauthRequestResult.Success = JSONObject(body).run {
        OauthRequestResult.Success.AccessToken(
            accessToken = getString(KEY_ACCESS_TOKEN),
            tokenType = getString(KEY_TOKEN_TYPE),
            expiresIn = if (has(KEY_EXPIRES_IN)) optLong(KEY_EXPIRES_IN,
                DEFAULT_EXPIRES_IN) else DEFAULT_EXPIRES_IN,
            refreshToken = optString(KEY_REFRESH_TOKEN).takeUnless { it.isBlank() },
            scope = optString(KEY_SCOPE).takeUnless { it.isBlank() },
            idToken = optString(KEY_ID_TOKEN).takeUnless { it.isBlank() }
        )
    }

    @Throws(JSONException::class, IOException::class)
    fun parseErrorResponse(body: String): OauthRequestResult.Error = JSONObject(body).run {
        val error = getString(KEY_ERROR).orEmpty()
        val description = optString(KEY_ERROR_DESCRIPTION).orEmpty()
        val errorUri = optString(KEY_ERROR_URI).orEmpty()
        OauthRequestResult.ErrorCode.safeValueOf(error)?.let { errorCode ->
            OauthRequestResult.Error.ServerError(
                errorCode,
                description = description,
                uri = errorUri.takeUnless { it.isBlank() }?.toUri()
            )
        } ?: OauthRequestResult.Error.IOError(IllegalArgumentException("failed to parse error response: $body"))
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_TOKEN_TYPE = "token_type"
        private const val KEY_EXPIRES_IN = "expires_in"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_SCOPE = "scope"
        private const val KEY_ID_TOKEN = "id_token"

        private const val KEY_ERROR = "error"
        private const val KEY_ERROR_DESCRIPTION = "error_description"
        private const val KEY_ERROR_URI = "error_uri"


        private const val DEFAULT_EXPIRES_IN = Long.MAX_VALUE
    }
}