package com.suenara.simpleoauthlib.impl.network

import androidx.core.net.toUri
import com.suenara.simpleoauthlib.impl.OauthParameter
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class OauthJsonResultFactory {

    @Throws(JSONException::class, IOException::class)
    fun parseSuccessResponse(body: String): OauthRequestResult.Success = JSONObject(body).run {
        OauthRequestResult.Success.AccessToken(
            accessToken = getString(OauthParameter.ACCESS_TOKEN.key),
            tokenType = getString(OauthParameter.TOKEN_TYPE.key),
            expiresIn = if (has(OauthParameter.EXPIRES_IN.key)) optLong(OauthParameter.EXPIRES_IN.key,
                DEFAULT_EXPIRES_IN) else DEFAULT_EXPIRES_IN,
            refreshToken = optString(OauthParameter.REFRESH_TOKEN.key).takeUnless { it.isBlank() },
            scope = optString(OauthParameter.SCOPE.key).takeUnless { it.isBlank() },
            idToken = optString(OauthParameter.ID_TOKEN.key).takeUnless { it.isBlank() }
        )
    }

    @Throws(JSONException::class, IOException::class)
    fun parseErrorResponse(body: String): OauthRequestResult.Error = JSONObject(body).run {
        val error = getString(OauthParameter.ERROR.key).orEmpty()
        val description = optString(OauthParameter.ERROR_DESCRIPTION.key).orEmpty()
        val errorUri = optString(OauthParameter.ERROR_URI.key).orEmpty()
        OauthRequestResult.ErrorCode.safeValueOf(error)?.let { errorCode ->
            OauthRequestResult.Error.ServerError(
                errorCode,
                description = description,
                uri = errorUri.takeUnless { it.isBlank() }?.toUri()
            )
        } ?: OauthRequestResult.Error.IOError(IllegalArgumentException("failed to parse error response: $body"))
    }

    companion object {
        private const val DEFAULT_EXPIRES_IN = Long.MAX_VALUE
    }
}