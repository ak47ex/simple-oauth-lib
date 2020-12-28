package com.suenara.simpleoauthlib.impl

import android.net.Uri

internal sealed class OauthResponse {
    data class Success(val code: String) : OauthResponse()
    data class Error(val description: String) : OauthResponse()

    companion object {
        private const val PARAMETER_CODE = "code"
        private const val PARAMETER_ERROR = "error"

        fun fromUri(uri: Uri): OauthResponse? {
            val code = uri.getQueryParameter(PARAMETER_CODE)
            val errorDescription = uri.getQueryParameter(PARAMETER_ERROR)
            return when {
                errorDescription?.isNotBlank() == true -> Error(errorDescription)
                code?.isNotBlank() == true -> Success(code)
                else -> null
            }
        }
    }
}