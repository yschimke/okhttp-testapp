package com.squareup.okhttptestapp

import android.content.Context
import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.ApolloQueryCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.cache.normalized.lru.EvictionPolicy
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.sql.ApolloSqlHelper
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory
import com.apollographql.apollo.rx2.Rx2Apollo
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.squareup.okhttptestapp.github.GithubAuthInterceptor
import com.squareup.okhttptestapp.github.ISO8601Adapter
import com.squareup.okhttptestapp.github.IdFieldCacheKeyResolver
import com.squareup.okhttptestapp.github.type.CustomType
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient

class NetworkClients(context: Context) {
  private val SQL_CACHE_NAME = "github"

  val apolloClient: ApolloClient
  val appClient: OkHttpClient
  val testClient: OkHttpClient

  init {
    appClient = OkHttpClient.Builder().addNetworkInterceptor(
        GithubAuthInterceptor()).addNetworkInterceptor(
        StethoInterceptor()).build()

    val testBuilder = TestSetup.configureBuilder(context, OkHttpClient.Builder())
    testBuilder.addNetworkInterceptor(StethoInterceptor())
    testBuilder.eventListener(TestEventListener())
    testClient = testBuilder.build()

    val normalizedCacheFactory = LruNormalizedCacheFactory(EvictionPolicy.NO_EVICTION)
        .chain(SqlNormalizedCacheFactory(ApolloSqlHelper(context, SQL_CACHE_NAME)))

    apolloClient = ApolloClient.builder().serverUrl(
        "https://api.github.com/graphql").okHttpClient(appClient).normalizedCache(
        normalizedCacheFactory, IdFieldCacheKeyResolver)
        .addCustomTypeAdapter(CustomType.DATETIME, ISO8601Adapter())
        .build()
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
