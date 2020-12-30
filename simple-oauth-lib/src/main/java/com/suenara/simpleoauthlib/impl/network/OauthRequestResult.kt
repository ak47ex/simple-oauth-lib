package com.suenara.simpleoauthlib.impl.network

import android.net.Uri
import java.util.Locale

sealed class OauthRequestResult {
    sealed class Success : OauthRequestResult() {
        data class AccessToken(
            val accessToken: String,
            val tokenType: String,
            val expiresIn: Long = Long.MAX_VALUE,
            val refreshToken: String? = null,
            val scope: String? = null,
            val idToken: String? = null,
        ) : Success()

        class RefreshToken() : Success() {
            override fun equals(other: Any?): Boolean {
                return this === other
            }

            override fun hashCode(): Int {
                return System.identityHashCode(this)
            }
        }
    }

    sealed class Error : OauthRequestResult() {
        data class ServerError(
            val code: ErrorCode,
            val state: String? = null,
            val description: String? = null,
            val uri: Uri? = null,
        ) : Error()

        data class IOError(val exception: Exception?) : Error()
    }

    enum class ErrorCode {
        INVALID_REQUEST,
        INVALID_CLIENT,
        INVALID_GRANT,
        INVALID_TOKEN,
        UNSUPPORTED_GRANT_TYPE,
        UNAUTHORIZED_CLIENT,
        ACCESS_DENIED,
        UNSUPPORTED_RESPONSE_TYPE,
        INVALID_SCOPE,
        SERVER_ERROR,
        TEMPORARILY_UNAVAILABLE;

        companion object {
            private val codeMap = values().map { it.name.toLowerCase(Locale.ENGLISH) to it }.toMap()
            fun safeValueOf(error: String): ErrorCode? = codeMap[error.toLowerCase(Locale.ENGLISH)]
        }
    }
}