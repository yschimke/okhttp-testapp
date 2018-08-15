package com.squareup.okhttptestapp

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun Call.await(): Response {
  return kotlinx.coroutines.suspendCancellableCoroutine { c ->
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