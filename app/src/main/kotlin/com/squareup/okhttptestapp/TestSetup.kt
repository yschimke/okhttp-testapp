package com.squareup.okhttptestapp

import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
import okhttp3.Request

object TestSetup {
  fun configureBuilder(clientBuilder: OkHttpClient.Builder): Builder {
    // nothing by default

    return clientBuilder
  }

  fun createRequest(clientBuilder: OkHttpClient.Builder): Request {
    return Request.Builder().url("https://google.com/robots.txt").build()
  }
}
