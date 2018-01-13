package com.squareup.okhttptestapp

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.util.Log
import com.facebook.litho.LithoView
import com.facebook.litho.sections.SectionContext
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
import com.squareup.okhttptestapp.model.NetworkEvent
import com.squareup.okhttptestapp.model.RequestOptions
import com.squareup.okhttptestapp.model.ResponseModel
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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    loadGmsProvider()

    sharedPrefs = this.getSharedPreferences("com.squareup.okhttptestapp", Context.MODE_PRIVATE)

    c = SectionContext(this)

    requestOptions = readQueryFromSharedPreferences()

    lithoView = LithoView.create(this, view(requestOptions))
    setContentView(lithoView)
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
          .results(results.toList())
          .build()

  private fun executeCall(newRequestOptions: RequestOptions) {
    requestOptions = newRequestOptions

    saveQueryToSharedPrefs()

    setupProviders()

    if (okHttpApplication().okhttpClient == null) {
      okHttpApplication().okhttpClient = createClient()
    }

    async {
      val request = Request.Builder().url(requestOptions.url).build()
      results.add(ResponseModel(okHttpApplication().okhttpClient!!.newCall(request)))
      lithoView.setComponentAsync(view(requestOptions))
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      registerNetworkListener()
    }
  }

  @RequiresApi(Build.VERSION_CODES.M)
  fun registerNetworkListener() {
    val connectivityManager = getSystemService(ConnectivityManager::class.java)
    val request = NetworkRequest.Builder().addCapability(
        NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
    val callback = object : ConnectivityManager.NetworkCallback() {
      override fun onCapabilitiesChanged(network: Network?,
          networkCapabilities: NetworkCapabilities?) {
        results.add(NetworkEvent("capabilities $network $networkCapabilities"))
      }

      override fun onAvailable(network: Network?) {
        results.add(NetworkEvent("available $network"))
      }

      override fun onUnavailable() {
        results.add(NetworkEvent("unavailable"))
      }
    }
    connectivityManager.registerNetworkCallback(request, callback)
  }

  private fun setupProviders() {
    if (requestOptions.gms) {
      if (gmsProvider != null && Security.getProviders().first().name != "GmsCore_OpenSSL") {
        Security.insertProviderAt(gmsProvider, 0)
        SSLContext.setDefault(SSLContext.getInstance("Default", "GmsCore_OpenSSL"))
        okHttpApplication().okhttpClient = null
      } else {
        Log.i(TAG, "No GMS provider to install")
      }
    } else {
      if (Security.getProvider("GmsCore_OpenSSL") != null) {
        Security.removeProvider("GmsCore_OpenSSL")
        SSLContext.setDefault(SSLContext.getInstance("Default"))
        okHttpApplication().okhttpClient = null
      }
    }
  }

  private fun createClient(): OkHttpClient? {
    val testBuilder = OkHttpClient.Builder()
    testBuilder.addNetworkInterceptor(StethoInterceptor())
    testBuilder.eventListener(TestEventListener())

    testBuilder.cache(Cache(File(cacheDir, "HttpResponseCache"), 10 * 1024 * 1024))

    testBuilder.cookieJar(
        PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(this)))

    val newClient = testBuilder.build()

    results.add(
        ClientCreated("${SSLContext.getDefault().provider}\n${newClient.connectionSpecs()}"))
    return newClient
  }

  private var gmsProvider: Provider? = null

  private fun loadGmsProvider() {
    try {
      ProviderInstaller.installIfNeeded(this)
      results.add(GmsInstall())
    } catch (e: GooglePlayServicesRepairableException) {
      results.add(GmsInstall("Repairable: " + e.message))
      GoogleApiAvailability.getInstance().showErrorNotification(this, e.connectionStatusCode)
    } catch (e: GooglePlayServicesNotAvailableException) {
      results.add(GmsInstall("Google Play unavailable"))
    }

    Security.getProviders().forEach {
      Log.i(TAG, "" + it.name + " " + it.javaClass)
    }

    gmsProvider = Security.getProvider("GmsCore_OpenSSL");
  }

  private fun okHttpApplication() = (application as OkHttpTestApp)

  companion object {
    var TAG = "MainActivity"
  }
}
