package com.suenara.simpleoauthlib.impl.network

import okhttp3.Response
import org.json.JSONException
import java.io.IOException

inline fun Response.safeParse(block: (String) -> OauthRequestResult): OauthRequestResult {
    return try {
        block(body?.string().orEmpty())
    } catch (e: JSONException) {
        OauthRequestResult.Error.IOError(e)
    } catch (e: IOException) {
        OauthRequestResult.Error.IOError(e)
    }
}