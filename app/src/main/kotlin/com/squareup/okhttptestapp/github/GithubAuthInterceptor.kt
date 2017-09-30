package com.squareup.okhttptestapp.github

import okhttp3.Interceptor
import okhttp3.Response

class GithubAuthInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = when {
      chain.request().url().host() == "api.github.com" -> chain.request().newBuilder().addHeader("Authorization",
          "bearer xxx").build()
      else -> chain.request()
    }
    return chain.proceed(request)
  }
}
