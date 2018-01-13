package com.squareup.okhttptestapp

import android.os.Bundle
import com.tinsuke.icekick.bundler.Bundler

object RequestOptionsBundler : Bundler<RequestOptions> {
  override fun save(bundle: Bundle, key: String, value: RequestOptions?) {
    value?.let {
      bundle.putString("url", value.url)
      bundle.putBoolean("gcm", value.gcm)
    }
  }

  override fun load(bundle: Bundle, key: String): RequestOptions? {
    val url = bundle.getString("url")
    val gcm = bundle.getBoolean("gcm")

    return url?.let { RequestOptions(gcm, url) }
  }
}

data class RequestOptions(val gcm: Boolean, val url: String)