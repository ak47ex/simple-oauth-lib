package com.suenara.simpleoauthlib

sealed class OauthResult {
    data class Success(val token: String, val idToken: String? = null) : OauthResult()
    data class Error(val description: String) : OauthResult()
    object Cancelled : OauthResult()
}