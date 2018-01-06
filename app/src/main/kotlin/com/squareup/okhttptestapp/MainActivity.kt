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
import okhttp3.Response

class MainActivity : Activity() {
  lateinit var c: SectionContext
  val results = mutableListOf<ResponseModel>()

  private lateinit var lithoView: LithoView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    c = SectionContext(this)

    lithoView = LithoView.create(this, view())
    setContentView(lithoView)
  }

  private fun view() = MainComponent.create(c).initialUrl(
      "https://nghttp2.org/httpbin/get").executeListener({ executeCall(it) }).results(
      results.toList()).build()

  private var count: Int = 0

  private fun executeCall(request: Request) {
    Log.i(TAG, request.url().toString())

    async {
      results.add(responseModel(testClient().newCall(request).await()))
      lithoView.setComponentAsync(view())
    }
  }

  private fun responseModel(
      response: Response): ResponseModel {
    if (response.isSuccessful) {
      return ResponseModel(count++, response.body()?.string(), response)
    } else {
      return ResponseModel(count++, null, response)
    }
  }

  private fun testClient() = (application as OkHttpTestApp).networkClients.testClient

  companion object {
    var TAG = "MainActivity"
  }
}
