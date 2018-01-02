package com.squareup.okhttptestapp

import okhttp3.*
import java.io.IOException

suspend fun Call.await(): Response {
  return kotlinx.coroutines.experimental.suspendCancellableCoroutine { c ->
    enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        c.resumeWithException(e)
      }

      override fun onResponse(call: Call, response: Response) {
        c.resume(response)
      }
    })
  }
}

suspend fun OkHttpClient.execute(request: Request): Response {
  val call = this.newCall(request)

  val response = call.await()

  if (!response.isSuccessful) {
    val responseString = response.body()!!.string()

    val msg: String = if (responseString.isNotEmpty()) {
      responseString
    } else {
      response.code().toString() + " " + response.message()
    }

    throw IOException(msg + ": " + response.code())
  }

  return response
}