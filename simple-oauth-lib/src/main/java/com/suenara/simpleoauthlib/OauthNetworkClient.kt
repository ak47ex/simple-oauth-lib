package com.suenara.simpleoauthlib

interface OauthNetworkClient {
    fun requestTokenForm(
        tokenEndpoint: String,
        code: String,
        clientId: String,
        redirectUri: String,
        grantType: GrantType,
        callback: (com.suenara.simpleoauthlib.impl.network.OauthRequestResult) -> Unit,
    )

    fun refreshTokenForm(
        tokenEndpoint: String,
        refreshToken: String,
        clientId: String,
        grantType: GrantType,
        callback: (com.suenara.simpleoauthlib.impl.network.OauthRequestResult) -> Unit,
    )

    fun revokeToken(revokeEndpoint: String, token: String, callback: (Boolean) -> Unit)

    enum class GrantType(val stringValue: String) {
        AUTHORIZATION_CODE("authorization_code"), REFRESH_TOKEN("refresh_token")
    }

}