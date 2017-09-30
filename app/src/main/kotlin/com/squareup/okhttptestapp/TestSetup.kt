package com.squareup.okhttptestapp

import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
import okhttp3.Request
import java.util.concurrent.TimeUnit.SECONDS

object TestSetup {
  fun configureBuilder(clientBuilder: OkHttpClient.Builder): Builder {
    // nothing by default

    clientBuilder.readTimeout(3, SECONDS)

    return clientBuilder
  }

  fun createRequest(): Request {
    return Request.Builder().url("https://httpbin.org/delay/3").build()
  }
}
