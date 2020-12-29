package com.suenara.simpleoauthlib.impl.network

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.Executor

class OauthRevokeTokenCallback(
    private val responseExecutor: Executor,
    private val resultFactory: OauthResultFactory,
    private val callback: (OauthRequestResult) -> Unit,
) : Callback {
    override fun onFailure(call: Call, e: IOException) {
        responseExecutor.execute { callback(OauthRequestResult.Error.IOError(e)) }
    }

    override fun onResponse(call: Call, response: Response) {
        val result = if (response.isSuccessful) {
            OauthRequestResult.Success.RefreshToken()
        } else {
            response.safeParse { resultFactory.parseErrorResponse(it) }
        }
        responseExecutor.execute { callback(result) }
    }
}