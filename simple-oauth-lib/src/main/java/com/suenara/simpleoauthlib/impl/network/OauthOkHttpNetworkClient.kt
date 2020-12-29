package com.suenara.simpleoauthlib.impl.network

import com.suenara.simpleoauthlib.OauthNetworkClient
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.concurrent.Executor

internal class OauthOkHttpNetworkClient(private val responseExecutor: Executor) : OauthNetworkClient {

    private val resultFactory = OauthJsonResultFactory()

    override fun requestTokenForm(
        tokenEndpoint: String,
        code: String,
        clientId: String,
        audience: String,
        redirectUri: String,
        grantType: OauthNetworkClient.GrantType,
        callback: (OauthRequestResult) -> Unit,
    ) {
        FormBody.Builder()
            .add(PARAMETER_CLIENT_ID, clientId)
            .add(PARAMETER_AUDIENCE, audience)
            .add(PARAMETER_CODE, code)
            .add(PARAMETER_REDIRECT_URI, redirectUri)
            .add(PARAMETER_GRANT_TYPE, grantType.stringValue)
            .build()
            .let { enqueuePostRequest(tokenEndpoint, it, callback) }
    }

    override fun refreshTokenForm(
        tokenEndpoint: String,
        refreshToken: String,
        clientId: String,
        audience: String,
        grantType: OauthNetworkClient.GrantType,
        callback: (OauthRequestResult) -> Unit,
    ) {
        FormBody.Builder()
            .add(PARAMETER_CLIENT_ID, clientId)
            .add(PARAMETER_REFRESH_TOKEN, refreshToken)
            .add(PARAMETER_GRANT_TYPE, grantType.stringValue)
            .add(PARAMETER_AUDIENCE, audience)
            .build()
            .let { enqueuePostRequest(tokenEndpoint, it, callback) }
    }

    override fun revokeToken(revokeEndpoint: String, token: String, callback: (OauthRequestResult) -> Unit) {
        val body = FormBody.Builder()
            .add(PARAMETER_REVOKING_TOKEN, token)
            .build()
        val request = Request.Builder().url(revokeEndpoint).post(body).build()
        buildClient().newCall(request).enqueue(OauthRevokeTokenCallback(responseExecutor, resultFactory, callback))
    }

    private fun enqueuePostRequest(
        url: String,
        body: RequestBody,
        callback: (OauthRequestResult) -> Unit,
    ) {
        val client = buildClient()
        val request = Request.Builder().url(url).post(body).build()
        client.newCall(request).enqueue(OauthTokenCallback(responseExecutor, resultFactory, callback))
    }

    private fun buildClient(): OkHttpClient {
        return OkHttpClient()
    }

    companion object {
        private const val PARAMETER_CODE = "code"
        private const val PARAMETER_CLIENT_ID = "client_id"
        private const val PARAMETER_REDIRECT_URI = "redirect_uri"
        private const val PARAMETER_GRANT_TYPE = "grant_type"
        private const val PARAMETER_REFRESH_TOKEN = "refresh_token"
        private const val PARAMETER_AUDIENCE = "audience"

        private const val PARAMETER_REVOKING_TOKEN = "token"
    }
}