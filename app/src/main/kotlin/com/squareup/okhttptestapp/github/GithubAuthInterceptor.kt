package com.squareup.okhttptestapp.github

import okhttp3.Interceptor
import okhttp3.Response

class GithubAuthInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder().addHeader("Authorization", "bearer XXX").build()
        return chain.proceed(request)
    }
}
