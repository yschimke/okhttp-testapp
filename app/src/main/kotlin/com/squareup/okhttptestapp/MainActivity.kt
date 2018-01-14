package com.squareup.okhttptestapp

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.util.Log
import com.facebook.litho.LithoView
import com.facebook.litho.sections.SectionContext
import com.facebook.litho.sections.widget.RecyclerCollectionEventsController
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import com.squareup.okhttptestapp.model.AppEvent
import com.squareup.okhttptestapp.model.ClientCreated
import com.squareup.okhttptestapp.model.GmsInstall
import com.squareup.okhttptestapp.model.RequestOptions
import com.squareup.okhttptestapp.model.ResponseModel
import com.squareup.okhttptestapp.network.NetworkListener
import com.squareup.okhttptestapp.spec.MainComponent
import kotlinx.coroutines.experimental.async
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.Provider
import java.security.Security
import javax.net.ssl.SSLContext

class MainActivity : Activity() {
  lateinit var c: SectionContext
  val results = mutableListOf<AppEvent>()

  private lateinit var lithoView: LithoView

  private lateinit var requestOptions: RequestOptions

  private lateinit var sharedPrefs: SharedPreferences

  lateinit var okhttpClient: OkHttpClient

  private var gmsProvider: Provider? = null

  private val scrollController = RecyclerCollectionEventsController()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    sharedPrefs = this.getSharedPreferences("com.squareup.okhttptestapp", Context.MODE_PRIVATE)
    requestOptions = readQueryFromSharedPreferences()

    c = SectionContext(this)
    lithoView = LithoView.create(this, view(requestOptions))
    setContentView(lithoView)

    loadGmsProvider()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      registerNetworkListener()
    }

    okhttpClient = createClient()
  }

  private fun readQueryFromSharedPreferences(): RequestOptions {
    val gms = sharedPrefs.getBoolean("gms", true)
    val url = sharedPrefs.getString("url", "https://www.howsmyssl.com/a/check")

    return RequestOptions(gms, url)
  }

  private fun saveQueryToSharedPrefs() {
    sharedPrefs.edit().clear().putString("url", requestOptions.url).putBoolean("gms",
        requestOptions.gms).apply()
  }

  private fun view(requestOptions: RequestOptions) =
      MainComponent.create(c)
          .requestOptions(requestOptions)
          .executeListener({ executeCall(it) })
          .gmsAvailable(gmsProvider != null)
          .scrollController(scrollController)
          .results(results.toList())
          .build()

  fun executeCall(newRequestOptions: RequestOptions) {
    requestOptions = newRequestOptions

    saveQueryToSharedPrefs()

    if (setupProviders()) {
      okhttpClient = createClient()
    }

    async {
      val request = Request.Builder().url(requestOptions.url).build()
      show(ResponseModel(okhttpClient.newCall(request)))
    }
  }

  fun show(model: AppEvent) {
    results.add(model)
    lithoView.setComponent(view(requestOptions))
    scrollController.requestScrollToPosition(results.size, true)
  }

  @RequiresApi(Build.VERSION_CODES.M)
  fun registerNetworkListener() {
    val connectivityManager = getSystemService(ConnectivityManager::class.java)
    val request = NetworkRequest.Builder().addCapability(
        NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
    connectivityManager.registerNetworkCallback(request, NetworkListener(this))
  }

  private fun setupProviders(): Boolean {
    if (requestOptions.gms) {
      if (gmsProvider != null && SSLContext.getDefault().provider.name != "GmsCore_OpenSSL") {
        SSLContext.setDefault(SSLContext.getInstance("Default", gmsProvider))
        return true
      } else {
        Log.i(TAG, "No GMS provider to install")
      }
    } else {
      if (SSLContext.getDefault().provider.name == "GmsCore_OpenSSL") {
        SSLContext.setDefault(SSLContext.getInstance("Default"))
        return true
      }
    }

    return false
  }

  private fun createClient(): OkHttpClient {
    val testBuilder = OkHttpClient.Builder()
    testBuilder.addNetworkInterceptor(StethoInterceptor())
    testBuilder.eventListener(TestEventListener())

    testBuilder.cache(Cache(File(cacheDir, "HttpResponseCache"), 10 * 1024 * 1024))

    testBuilder.cookieJar(
        PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(this)))

    val newClient = testBuilder.build()

    show(ClientCreated("${SSLContext.getDefault().provider}\n${newClient.connectionSpecs()}"))
    return newClient
  }

  private fun loadGmsProvider() {
    try {
      ProviderInstaller.installIfNeeded(this)
      show(GmsInstall())
    } catch (e: GooglePlayServicesRepairableException) {
      show(GmsInstall("Repairable: " + e.message))
      GoogleApiAvailability.getInstance().showErrorNotification(this, e.connectionStatusCode)
    } catch (e: GooglePlayServicesNotAvailableException) {
      show(GmsInstall("Google Play unavailable"))
    }

    Security.getProviders().forEach {
      Log.i(TAG, "" + it.name + " " + it.javaClass)
    }

    gmsProvider = Security.getProvider("GmsCore_OpenSSL");

    Security.removeProvider("GmsCore_OpenSSL")
  }

  companion object {
    var TAG = "MainActivity"
  }
}
