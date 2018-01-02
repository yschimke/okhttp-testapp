package com.squareup.okhttptestapp

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.OrientationHelper
import android.util.Log
import com.facebook.litho.Border
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.facebook.litho.widget.LinearLayoutInfo
import com.facebook.litho.widget.RecyclerBinder
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge
import com.squareup.okhttptestapp.spec.MainComponent
import kotlinx.coroutines.experimental.async
import okhttp3.Request

class MainActivity : Activity() {
  lateinit var c: ComponentContext
  lateinit var binder: RecyclerBinder

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    c = ComponentContext(this)

    binder = RecyclerBinder.Builder().layoutInfo(
        LinearLayoutInfo(c.baseContext, OrientationHelper.VERTICAL, false)).build(c)

    val component = MainComponent.create(c).initialUrl(
        "https://nghttp2.org/httpbin/get").executeListener({ executeCall(it) }).recyclerBinder(
        binder).build()

    setContentView(LithoView.create(this, component))
  }

  private fun executeCall(request: Request) {
    Log.i(TAG, request.url().toString())

    async {
      var response = (application as OkHttpTestApp).networkClients.testClient.execute(request)
      showResults(response.body()!!.string())
    }
  }

  private fun showResults(text: String) {
    val border = Border.create(c).color(YogaEdge.BOTTOM, Color.BLACK).widthDip(YogaEdge.BOTTOM,
        1).build()
    binder.insertItemAt(0,
        Text.create(c).text(text).textSizeSp(12f).border(border).build())
  }

  companion object {
    var TAG = "MainActivity"
  }
}
