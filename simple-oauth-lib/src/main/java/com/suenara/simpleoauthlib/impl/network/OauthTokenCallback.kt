package com.suenara.simpleoauthlib.impl.network

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.Executor

internal class OauthTokenCallback(
    private val responseExecutor: Executor,
    private val resultFactory: OauthJsonResultFactory,
    private val callback: (OauthRequestResult) -> Unit,
) : Callback {

    override fun onFailure(call: Call, e: IOException) {
        responseExecutor.execute { callback(OauthRequestResult.Error.IOError(e)) }
    }

    override fun onResponse(call: Call, response: Response) {
        response.safeParse {
            if (response.isSuccessful) resultFactory.parseSuccessResponse(it) else resultFactory.parseErrorResponse(it)
        }.let { result -> responseExecutor.execute { callback(result) } }
    }
}