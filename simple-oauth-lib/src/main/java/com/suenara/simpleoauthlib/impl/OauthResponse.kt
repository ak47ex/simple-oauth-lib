package com.suenara.simpleoauthlib.impl

import android.net.Uri

internal sealed class OauthResponse {
    data class Success(val values: Map<String, String>) : OauthResponse()
    data class Error(val description: String) : OauthResponse()

    companion object {
        private val PARAMETER_CODE = OauthParameter.CODE.key
        private val PARAMETER_ACCESS_TOKEN = OauthParameter.ACCESS_TOKEN.key
        private val PARAMETER_ID_TOKEN = OauthParameter.ID_TOKEN.key

        private val PARAMETER_ERROR = OauthParameter.ERROR.key

        fun fromUri(uri: Uri): OauthResponse? {
            val errorDescription = uri.getQueryParameter(PARAMETER_ERROR)
            return when {
                errorDescription?.isNotBlank() == true -> Error(errorDescription)
                isResponseSuccess(uri) -> {
                    Success(uri.queryParameterNames.map { it to uri.getQueryParameter(it).orEmpty() }.toMap())
                }
                uri.fragment.isNullOrBlank() -> null
                else -> fromUri(rebuildFragmentAsQuery(uri))
            }
        }

        private fun isResponseSuccess(uri: Uri): Boolean {
            return uri.run {
                (getQueryParameter(PARAMETER_CODE) ?: getQueryParameter(PARAMETER_ACCESS_TOKEN) ?: getQueryParameter(
                    PARAMETER_ID_TOKEN))?.let { true }
            } ?: false
        }

        private fun rebuildFragmentAsQuery(uri: Uri): Uri {
            return uri.buildUpon().clearQuery().fragment("").encodedQuery(uri.encodedFragment).build()
        }
    }
}