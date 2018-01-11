package com.squareup.okhttptestapp

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.facebook.litho.LithoView
import com.facebook.litho.sections.SectionContext
import com.squareup.okhttptestapp.model.ResponseModel
import com.squareup.okhttptestapp.spec.MainComponent
import kotlinx.coroutines.experimental.async
import okhttp3.Request

class MainActivity : Activity() {
  lateinit var c: SectionContext
  val results = mutableListOf<ResponseModel>()

  private lateinit var lithoView: LithoView

  private lateinit var requestOptions: RequestOptions

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    c = SectionContext(this)

    requestOptions = if (savedInstanceState != null) {
      val gcm = savedInstanceState.getBoolean("gcm", true)
      val url = savedInstanceState.getString("url", "https://nghttp2.org/httpbin/delay/1")
      RequestOptions(gcm = gcm, url = url)
    } else {
      RequestOptions(gcm = true, url = "https://nghttp2.org/httpbin/delay/1")
    }

    lithoView = LithoView.create(this, view())
    setContentView(lithoView)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)

    Log.i(TAG, "onSaveInstanceState " + requestOptions)

    outState.putBoolean("gcm", requestOptions.gcm)
    outState.putString("url", requestOptions.url)
  }

  private fun view() =
      MainComponent.create(c)
          .requestOptions(requestOptions)
          .executeListener({ executeCall(it) })
          .results(results.toList())
          .build()

  private fun executeCall(newRequestOptions: RequestOptions) {
    Log.i(TAG, "" + newRequestOptions)

    requestOptions = newRequestOptions

    async {
      val request = Request.Builder().url(requestOptions.url).build()
      results.add(ResponseModel(testClient().newCall(request)))
      lithoView.setComponentAsync(view())
    }
  }

  private fun testClient() = (application as OkHttpTestApp).networkClients.testClient

  companion object {
    var TAG = "MainActivity"
  }
}
