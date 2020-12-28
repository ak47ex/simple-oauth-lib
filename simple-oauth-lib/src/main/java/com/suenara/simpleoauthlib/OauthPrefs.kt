package com.suenara.simpleoauthlib

import android.net.Uri

interface OauthPrefs {
    var authUri: Uri
    var redirectUri: Uri
    var tokenUri: Uri
    var revokeTokenUri: Uri
    var code: String
    var error: String
    var clientId: String

    var accessToken: String?
    var tokenType: String?
    var refreshToken: String?
    var tokenExpirationDate: Long

    var idToken: String?
}