package com.squareup.okhttptestapp

import android.app.Activity
import android.net.*
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
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

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    c = SectionContext(this)

    lithoView = LithoView.create(this, view())
    setContentView(lithoView)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      val cm = this.applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
      val request = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
      cm.registerNetworkCallback(request, object: ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
          Log.i("MainActivity", "onCapabilitiesChanged")
        }

        override fun onLost(network: Network) {
          Log.i("MainActivity", "onLost")
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
          Log.i("MainActivity", "onLinkPropertiesChanged " + linkProperties.httpProxy)
        }

        override fun onUnavailable() {
          Log.i("MainActivity", "onUnavailable")
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
          Log.i("MainActivity", "onLosing")
        }

        override fun onAvailable(network: Network) {
          Log.i("MainActivity", "onAvailable")
        }
      })
    }
  }

  private fun view() = MainComponent.create(c).initialUrl(
      "https://nghttp2.org/httpbin/delay/1").executeListener({ executeCall(it) }).results(
      results.toList()).build()

  private fun executeCall(request: Request) {
    Log.i(TAG, request.url().toString())

    async {
      results.add(ResponseModel(testClient().newCall(request)))
      lithoView.setComponentAsync(view())
    }
  }

  private fun testClient() = (application as OkHttpTestApp).networkClients.testClient

  companion object {
    var TAG = "MainActivity"
  }
}
