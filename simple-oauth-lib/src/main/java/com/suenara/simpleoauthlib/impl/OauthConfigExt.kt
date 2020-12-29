package com.suenara.simpleoauthlib.impl

import android.net.Uri
import com.suenara.simpleoauthlib.OauthConfig

private const val KEY_CLIENT_ID = "client_id"
private const val KEY_SCOPE = "scope"
private const val KEY_REDIRECT_URI = "redirect_uri"
private const val KEY_RESPONSE_TYPE = "response_type"
private const val KEY_ACCESS_TYPE = "access_type"

private const val SCOPES_SEPARATOR = " "

internal fun OauthConfig.makeAuthorizationUri(): Uri = authEndpoint.buildUpon()
    .appendQueryParameter(KEY_CLIENT_ID, clientId)
    .appendQueryParameter(KEY_SCOPE, scopes.joinToString(SCOPES_SEPARATOR))
    .appendQueryParameter(KEY_REDIRECT_URI, redirectUri.toString())
    .appendQueryParameter(KEY_RESPONSE_TYPE, responseType.stringValue)
    .appendQueryParameter(KEY_ACCESS_TYPE, accessType.stringValue)
    .build()