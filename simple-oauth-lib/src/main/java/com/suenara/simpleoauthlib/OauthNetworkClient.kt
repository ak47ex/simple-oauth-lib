package com.suenara.simpleoauthlib

import com.suenara.simpleoauthlib.impl.network.OauthRequestResult

interface OauthNetworkClient {
    fun requestTokenForm(
        tokenEndpoint: String,
        code: String,
        clientId: String,
        audience: String = clientId,
        redirectUri: String,
        grantType: GrantType,
        callback: (OauthRequestResult) -> Unit,
    )

    fun refreshTokenForm(
        tokenEndpoint: String,
        refreshToken: String,
        clientId: String,
        audience: String = clientId,
        grantType: GrantType,
        callback: (OauthRequestResult) -> Unit,
    )

    fun revokeToken(revokeEndpoint: String, token: String, callback: (OauthRequestResult) -> Unit)

    enum class GrantType(val stringValue: String) {
        AUTHORIZATION_CODE("authorization_code"), REFRESH_TOKEN("refresh_token")
    }

}