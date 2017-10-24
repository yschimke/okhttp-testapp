package com.squareup.okhttptestapp

import android.content.Context
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
import okhttp3.Request
import java.util.concurrent.TimeUnit.SECONDS

object TestSetup {
  fun configureBuilder(context: Context, clientBuilder: OkHttpClient.Builder): Builder {
    // nothing by default

//    clientBuilder.readTimeout(3, SECONDS)

    clientBuilder.cookieJar(PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context)))

    return clientBuilder
  }

  fun createRequest(): Request {
    return Request.Builder().url("https://httpbin.org/cookies/set?name=value").build()
  }
}
