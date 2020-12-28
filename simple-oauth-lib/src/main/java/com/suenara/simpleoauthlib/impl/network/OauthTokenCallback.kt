package com.suenara.simpleoauthlib.impl.network

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.Executor

internal class OauthTokenCallback(
    private val responseExecutor: Executor,
    private val callback: (OauthRequestResult) -> Unit,
) : Callback {
    override fun onFailure(call: Call, e: IOException) {
        responseExecutor.execute { callback(OauthRequestResult.Error.IOError(e)) }
    }

    override fun onResponse(call: Call, response: Response) {
        if (response.isSuccessful) {
            try {
                val stringBody = response.body?.string().orEmpty()
                val parsedResponse = parseSuccessResponse(stringBody)
                responseExecutor.execute { callback(parsedResponse) }
            } catch (e: JSONException) {
                responseExecutor.execute { callback(OauthRequestResult.Error.IOError(e)) }
            }
        } else {
            val exception = IllegalStateException("Invalid response code(${response.code}) on ${call.request().url}")
            val error = OauthRequestResult.Error.IOError(exception)
            responseExecutor.execute { callback(error) }
        }
    }

    private fun parseSuccessResponse(body: String): OauthRequestResult.Success =
        JSONObject(body).run {
            OauthRequestResult.Success(
                accessToken = getString(KEY_ACCESS_TOKEN),
                tokenType = getString(KEY_TOKEN_TYPE),
                expiresIn = if (has(KEY_EXPIRES_IN)) optLong(KEY_EXPIRES_IN,
                    DEFAULT_EXPIRES_IN) else DEFAULT_EXPIRES_IN,
                refreshToken = optString(KEY_REFRESH_TOKEN).takeUnless { it.isBlank() },
                scope = optString(KEY_SCOPE).takeUnless { it.isBlank() },
                idToken = optString(KEY_ID_TOKEN).takeUnless { it.isBlank() }
            )
        }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_TOKEN_TYPE = "token_type"
        private const val KEY_EXPIRES_IN = "expires_in"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_SCOPE = "scope"
        private const val KEY_ID_TOKEN = "id_token"


        private const val DEFAULT_EXPIRES_IN = Long.MAX_VALUE
    }
}