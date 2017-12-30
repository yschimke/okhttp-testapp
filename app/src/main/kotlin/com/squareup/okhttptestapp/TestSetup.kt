package com.squareup.okhttptestapp

import android.content.Context
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
import okhttp3.Request
import java.io.File

object TestSetup {
  fun configureBuilder(context: Context, clientBuilder: OkHttpClient.Builder): Builder {
    val cache = Cache(File(context.cacheDir, "HttpResponseCache"), 10 * 1024 * 1024)
    clientBuilder.cache(cache)

    clientBuilder.cookieJar(
        PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context)))

    clientBuilder.addInterceptor({ chain ->
      val cacheControl = if (first) {
        first = false; CacheControl.FORCE_NETWORK
      } else {
        CacheControl.FORCE_CACHE
      }

      println(cacheControl)

      val request = chain.request().newBuilder().cacheControl(cacheControl).build()

      chain.proceed(request)
    })

    return clientBuilder
  }

  fun getDefaultUrl() = "https://httpbin.org/get"

  var first = true

  fun createRequest(url: String): Request {
    return Request.Builder().url(url).build()
  }
}
