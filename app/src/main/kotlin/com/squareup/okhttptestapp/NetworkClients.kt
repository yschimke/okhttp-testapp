package com.squareup.okhttptestapp

import android.content.Context
import android.util.Log
import com.apollographql.apollo.ApolloQueryCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.rx2.Rx2Apollo
import com.facebook.stetho.okhttp3.StethoInterceptor
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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

fun <T> ApolloQueryCall<T>.observable(): Observable<Response<T>> {
  return Rx2Apollo.from(this).subscribeOn(Schedulers.io()).observeOn(
      AndroidSchedulers.mainThread()).doOnNext({ d ->
    run {
      d.errors().forEach {
        Log.w(MainActivity.TAG, "Query Error: $it")
      }
    }
  })
}
