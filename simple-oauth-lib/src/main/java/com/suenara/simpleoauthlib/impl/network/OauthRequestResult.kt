package com.suenara.simpleoauthlib.impl.network

import android.net.Uri

sealed class OauthRequestResult {
    data class Success(
        val accessToken: String,
        val tokenType: String,
        val expiresIn: Long = Long.MAX_VALUE,
        val refreshToken: String? = null,
        val scope: String? = null,
        val idToken: String? = null
    ) : OauthRequestResult()

    sealed class Error : OauthRequestResult() {
        data class ServerError(
            val code: OauthErrorCode,
            val state: String? = null,
            val description: String? = null,
            val uri: Uri? = null,
        ) : Error() {
            enum class OauthErrorCode {
                INVALID_REQUEST,
                UNAUTHORIZED_CLIENT,
                ACCESS_DENIED,
                UNSUPPORTED_RESPONSE_TYPE,
                INVALID_SCOPE,
                SERVER_ERROR,
                TEMPORARILY_UNAVAILABLE
            }
        }

        data class IOError(val exception: Exception?) : Error()
    }
}