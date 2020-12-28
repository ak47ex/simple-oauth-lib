package com.suenara.simpleoauthlib

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.suenara.simpleoauthlib.impl.OauthResponse
import com.suenara.simpleoauthlib.impl.OauthSharedPrefs
import com.suenara.simpleoauthlib.impl.makeAuthorizationUri
import com.suenara.simpleoauthlib.impl.network.OauthOkHttpNetworkClient
import com.suenara.simpleoauthlib.impl.network.OauthRequestResult

internal class OauthActivity : AppCompatActivity() {

    private val prefs by lazy { OauthSharedPrefs(this) }
    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private val netClient: OauthNetworkClient = OauthOkHttpNetworkClient { handler.post { it.run() } }
    private val oauthRequestCallback: (OauthRequestResult) -> Unit = {
        when (it) {
            is OauthRequestResult.Success -> handleRequestSuccess(it)
            is OauthRequestResult.Error -> handleRequestError(it)
        }
    }
    private var isFlowIntercepted = false
    private var isFlowLaunched = false

    override fun onCreate(savedInstanceState: Bundle?) {
        checkInternetPermission()
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let(::handleIntent)
    }

    override fun onResume() {
        super.onResume()
        if (isFlowIntercepted) {
            finishWithCancel()
        }
    }

    override fun onPause() {
        if (isFlowLaunched) {
            isFlowIntercepted = true
        }
        super.onPause()
        overridePendingTransition(0, 0)
    }

    private fun handleIntent(intent: Intent) {
        isFlowIntercepted = false
        isFlowLaunched = false
        when {
            isSignOut(intent) -> signOut()
            isInitFlow(intent) -> initFlow(intent)
            isOauthResponse(intent) -> handleOauthResponse(requireNotNull(intent.data))
            else -> finishWithCancel()
        }
    }

    private fun signOut() {
        with(prefs) {
            accessToken?.takeIf { it.isNotEmpty() }?.let { token ->
                netClient.revokeToken(revokeTokenUri.toString(), token) { isSuccess ->
                    if (isSuccess) {
                        resetToken()
                        finishWithSuccess()
                    } else {
                        finishWithError()
                    }
                }
            }
        }
    }

    private fun initFlow(intent: Intent) {
        populatePrefs(requireNotNull(intent.getParcelableExtra(KEY_CONFIG)))
        val isTokenExpired = isDateExpired(prefs.tokenExpirationDate)
        when {
            prefs.accessToken == null || (isTokenExpired && prefs.refreshToken == null) -> launchAuthFlow()
            isTokenExpired -> refreshToken()
            else -> finishWithSuccess()
        }
    }

    private fun launchAuthFlow() {
        isFlowLaunched = true
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(this, prefs.authUri)
    }

    private fun handleOauthResponse(response: Uri) {
        when (val oauthResponse = OauthResponse.fromUri(response)) {
            is OauthResponse.Success -> {
                prefs.code = oauthResponse.code
                requestToken()
            }
            is OauthResponse.Error -> {
                prefs.error = oauthResponse.description
            }
        }
    }

    private fun requestToken() = with(prefs) {
        netClient.requestTokenForm(
            tokenUri.toString(),
            code,
            clientId,
            redirectUri.toString(),
            OauthNetworkClient.GrantType.AUTHORIZATION_CODE,
            oauthRequestCallback
        )
    }

    private fun refreshToken() = with(prefs) {
        refreshToken?.let { refreshToken ->
            netClient.refreshTokenForm(
                tokenUri.toString(),
                refreshToken,
                clientId,
                OauthNetworkClient.GrantType.REFRESH_TOKEN,
                oauthRequestCallback
            )
        } ?: finishWithError("refresh token doesn't exist")
    }

    private fun handleRequestError(response: OauthRequestResult.Error) {
        finishWithError(response.toString())
    }

    private fun handleRequestSuccess(response: OauthRequestResult.Success) {
        saveToken(response)
        finishWithSuccess()
    }

    private fun populatePrefs(config: OauthConfig) {
        if (prefs.clientId != config.clientId) {
            resetToken()
        }
        prefs.authUri = buildAuthUri(config)
        prefs.redirectUri = config.redirectUri
        prefs.revokeTokenUri = config.revokeTokenEndpoint
        prefs.clientId = config.clientId
        prefs.tokenUri = config.tokenEndpoint
    }

    private fun finishWithCancel() {
        finish()
    }

    private fun finishWithError(message: String? = null) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(SimpleOauth.KEY_IS_SUCCESS, false)
            message?.let { putExtra(SimpleOauth.KEY_EXTRA_ERROR_MESSAGE, it) }
        })
        finish()
    }

    private fun finishWithSuccess() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(SimpleOauth.KEY_IS_SUCCESS, true)
            prefs.accessToken?.let { token -> putExtra(SimpleOauth.KEY_EXTRA_TOKEN, token) }
            prefs.idToken?.let { token -> putExtra(SimpleOauth.KEY_EXTRA_ID_TOKEN, token) }
        })
        finish()
    }

    private fun buildAuthUri(config: OauthConfig): Uri = config.makeAuthorizationUri()

    private fun saveToken(response: OauthRequestResult.Success) {
        with(prefs) {
            accessToken = response.accessToken
            tokenType = response.tokenType
            tokenExpirationDate = getCurrentTimeMillis() + response.expiresIn * MILLIS_IN_SECOND
            refreshToken = response.refreshToken

            idToken = response.idToken
        }
    }

    private fun resetToken() = with(prefs) {
        accessToken = null
        tokenType = null
        refreshToken = null
        tokenExpirationDate = 0L

        idToken = null
    }

    private fun isSignOut(intent: Intent): Boolean {
        return intent.getBooleanExtra(SimpleOauth.KEY_EXTRA_IS_SIGN_OUT, false)
    }

    private fun isInitFlow(intent: Intent): Boolean {
        return intent.data == null || intent.data?.scheme.isNullOrEmpty()
    }

    private fun isOauthResponse(intent: Intent): Boolean {
        return intent.action == Intent.ACTION_VIEW && intent.data?.scheme?.equals(prefs.redirectUri.scheme) == true
    }

    private fun isDateExpired(timestamp: Long): Boolean {
        return timestamp < getCurrentTimeMillis()
    }

    private fun checkInternetPermission() {
        if (packageManager.checkPermission(Manifest.permission.INTERNET,
                packageName) != PackageManager.PERMISSION_GRANTED
        ) {
            throw IllegalStateException("Oauth flow requires \"android.permission.INTERNET\" permission!")
        }
    }

    private fun getCurrentTimeMillis(): Long {
        return System.currentTimeMillis()
    }

    companion object {
        private const val KEY_CONFIG = SimpleOauth.KEY_EXTRA_CONFIG

        private const val MILLIS_IN_SECOND = 1000L
    }
}