package com.squareup.okhttptestapp

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.facebook.litho.LithoView
import com.facebook.litho.sections.SectionContext
import com.squareup.okhttptestapp.model.ResponseModel
import com.squareup.okhttptestapp.spec.MainComponent
import com.tinsuke.icekick.extension.freezeInstanceState
import com.tinsuke.icekick.extension.state
import com.tinsuke.icekick.extension.unfreezeInstanceState
import kotlinx.coroutines.experimental.async
import okhttp3.Request

class MainActivity : Activity() {
  lateinit var c: SectionContext
  val results = mutableListOf<ResponseModel>()

  private lateinit var lithoView: LithoView

  private var requestOptions: RequestOptions? by state(RequestOptionsBundler)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    c = SectionContext(this)

    unfreezeInstanceState(savedInstanceState)

    requestOptions = requestOptions ?: RequestOptions(true, "https://nghttp2.org/httpbin/delay/1")

    lithoView = LithoView.create(this, view(requestOptions!!))
    setContentView(lithoView)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    freezeInstanceState(outState)
  }

  private fun view(requestOptions: RequestOptions) =
      MainComponent.create(c)
          .requestOptions(requestOptions)
          .executeListener({ executeCall(it) })
          .results(results.toList())
          .build()

  private fun executeCall(newRequestOptions: RequestOptions) {
    Log.i(TAG, "" + newRequestOptions)

    requestOptions = newRequestOptions

    async {
      val request = Request.Builder().url(requestOptions!!.url).build()
      results.add(ResponseModel(testClient().newCall(request)))
      lithoView.setComponentAsync(view(requestOptions!!))
    }
  }

  private fun testClient() = (application as OkHttpTestApp).networkClients.testClient

  companion object {
    var TAG = "MainActivity"
  }
}
