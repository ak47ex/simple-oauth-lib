package com.suenara.simpleoauthlib.impl.network

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.Executor

class OauthRevokeTokenCallback(
    private val responseExecutor: Executor,
    private val callback: (Boolean) -> Unit,
) : Callback {
    override fun onFailure(call: Call, e: IOException) {
        responseExecutor.execute { callback(false) }
    }

    override fun onResponse(call: Call, response: Response) {
        responseExecutor.execute { callback(response.isSuccessful) }
    }
}