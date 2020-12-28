package com.suenara.simpleoauthlib

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OauthConfig(
    val authEndpoint: Uri,
    val tokenEndpoint: Uri,
    val revokeTokenEndpoint: Uri,
    val clientId: String,
    val redirectUri: Uri,
    val scopes: List<String>,
    val responseType: ResponseType,
) : Parcelable {

    constructor(
        endpoint: Uri,
        clientId: String,
        redirectUri: Uri,
        scopes: List<String>,
    ) : this(
        endpoint.buildUpon().appendPath("auth").build(),
        endpoint.buildUpon().appendPath("token").build(),
        endpoint.buildUpon().appendPath("revoke").build(),
        clientId,
        redirectUri,
        scopes,
        ResponseType.CODE
    )

    enum class ResponseType(val stringValue: String) { TOKEN("token"), CODE("code") }
}