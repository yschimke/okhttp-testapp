package com.squareup.okhttptestapp

import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient

class NetworkClients(val context: Context) {
  val appClient: OkHttpClient by lazy {
    OkHttpClient.Builder().addNetworkInterceptor(StethoInterceptor()).build()
  }
  val testClient: OkHttpClient by lazy {
    val testBuilder = TestSetup.configureBuilder(context, OkHttpClient.Builder())
    testBuilder.addNetworkInterceptor(StethoInterceptor())
    testBuilder.eventListener(TestEventListener())
    testBuilder.build()
  }
}
