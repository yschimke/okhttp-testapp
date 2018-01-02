package com.squareup.okhttptestapp

import android.content.Context
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
import okhttp3.Request
import java.io.File

object TestSetup {
  fun configureBuilder(context: Context, clientBuilder: OkHttpClient.Builder): Builder {
    clientBuilder.cache(Cache(File(context.cacheDir, "HttpResponseCache"), 10 * 1024 * 1024))

    clientBuilder.cookieJar(
        PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context)))

    return clientBuilder
  }

  fun getDefaultUrl() = "https://httpbin.org/get"

  fun createRequest(url: String): Request {
    return Request.Builder().url(url).build()
  }
}
