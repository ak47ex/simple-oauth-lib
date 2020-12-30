package com.suenara.simpleoauthlib

import android.app.Activity
import android.content.Intent
import com.suenara.simpleoauthlib.impl.OauthSharedPrefs

object SimpleOauth {
    internal const val KEY_EXTRA_CONFIG = "config"
    internal const val KEY_EXTRA_IS_SIGN_OUT = "is_sign_out"
    internal const val KEY_EXTRA_TOKEN = "access_token"
    internal const val KEY_EXTRA_ID_TOKEN = "id_token"
    internal const val KEY_EXTRA_ERROR_MESSAGE = "error_msg"
    internal const val KEY_IS_SUCCESS = "is_success"

    private const val OAUTH_REQUEST_CODE = 23881
    private const val SIGNOUT_REQUEST_CODE = 23882

    fun launchSignInActivity(activity: Activity, config: OauthConfig) {
        activity.launchOauthActivity(OAUTH_REQUEST_CODE) { putExtra(KEY_EXTRA_CONFIG, config) }
    }

    fun isSignedIn(activity: Activity): Boolean {
        return OauthSharedPrefs(activity).run {
            (!accessToken.isNullOrBlank() || !refreshToken.isNullOrEmpty()) && tokenExpirationDate > System.currentTimeMillis()
        }
    }

    fun signOut(activity: Activity) {
        activity.launchOauthActivity(SIGNOUT_REQUEST_CODE) { putExtra(KEY_EXTRA_IS_SIGN_OUT, true) }
    }

    fun checkActivityResult(requestCode: Int, resultCode: Int, data: Intent?): OauthResult? {
        return if (requestCode == OAUTH_REQUEST_CODE || requestCode == SIGNOUT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                OauthResult.Cancelled
            } else {
                if (data.isSuccess()) {
                    val token = data?.getStringExtra(KEY_EXTRA_TOKEN).orEmpty()
                    OauthResult.Success(token, data?.getStringExtra(KEY_EXTRA_ID_TOKEN))
                } else {
                    OauthResult.Error(data?.getStringExtra(KEY_EXTRA_ERROR_MESSAGE).orEmpty())
                }
            }
        } else {
            null
        }
    }

    private fun Activity.launchOauthActivity(requestCode: Int, block: Intent.() -> Unit) {
        startActivityForResult(Intent(this, OauthActivity::class.java).also(block), requestCode)
        overridePendingTransition(0, 0)
    }

    private fun Intent?.isSuccess(): Boolean = this?.getBooleanExtra(KEY_IS_SUCCESS, false) ?: false
}