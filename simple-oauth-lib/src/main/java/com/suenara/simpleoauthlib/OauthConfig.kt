package com.suenara.simpleoauthlib

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OauthConfig(
    val authEndpoint: Uri,
    val tokenEndpoint: Uri,
    val revocationEndpoint: Uri,
    val clientId: String,
    val redirectUri: Uri,
    val scopes: List<String>,
    val responseType: ResponseType,
    val audience: String = clientId,
    val accessType: AccessType = AccessType.OFFLINE,
) : Parcelable {

    constructor(
        endpoint: Uri,
        clientId: String,
        redirectUri: Uri,
        scopes: List<String>,
        audience: String = clientId,
    ) : this(
        endpoint.buildUpon().appendPath("auth").build(),
        endpoint.buildUpon().appendPath("token").build(),
        endpoint.buildUpon().appendPath("revoke").build(),
        clientId,
        redirectUri,
        scopes,
        ResponseType.CODE,
        audience,
        AccessType.OFFLINE
    )

    enum class ResponseType(val stringValue: String) {
        TOKEN("token"),
        CODE("code"),
        ID_TOKEN("id_token"),
        NONE("none"),
        CODE_TOKEN("code token"),
        CODE_ID_TOKEN("code id_token"),
        ID_TOKEN_TOKEN("id_token token"),
        CODE_ID_TOKEN_TOKEN("code id_token token")
    }

    enum class AccessType(val stringValue: String) { ONLINE("online"), OFFLINE("offline") }
}