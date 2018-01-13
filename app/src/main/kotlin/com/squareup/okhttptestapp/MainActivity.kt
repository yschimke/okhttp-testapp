package com.squareup.okhttptestapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
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
import com.jakewharton.processphoenix.ProcessPhoenix
import com.squareup.okhttptestapp.model.AppEvent
import com.squareup.okhttptestapp.model.ClientCreated
import com.squareup.okhttptestapp.model.GmsInstall
import com.squareup.okhttptestapp.model.RequestOptions
import com.squareup.okhttptestapp.model.ResponseModel
import com.squareup.okhttptestapp.spec.MainComponent
import kotlinx.coroutines.experimental.async
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.Security
import javax.net.ssl.SSLContext

class MainActivity : Activity() {
  lateinit var c: SectionContext
  val results = mutableListOf<AppEvent>()

  private lateinit var lithoView: LithoView

  private lateinit var requestOptions: RequestOptions

  private lateinit var sharedPrefs: SharedPreferences

  private var gmsInstalled = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    sharedPrefs = this.getSharedPreferences("com.squareup.okhttptestapp", Context.MODE_PRIVATE)

    c = SectionContext(this)

    requestOptions = readQueryFromSharedPreferences()

    lithoView = LithoView.create(this, view(requestOptions))
    setContentView(lithoView)

    if (runInitial()) {
      executeCall(requestOptions)
    }
  }

  private fun runInitial(): Boolean {
    val run = ProcessPhoenix.isPhoenixProcess(this) || intent.hasCategory(
        "android.intent.category.DEFAULT")
    return run
  }

  private fun readQueryFromSharedPreferences(): RequestOptions {
    val gms = sharedPrefs.getBoolean("gms", true)
    val url = sharedPrefs.getString("url", "https://www.howsmyssl.com/a/check")

    return RequestOptions(gms, url)
  }

  @SuppressLint("ApplySharedPref")
  private fun saveQueryToSharedPrefs() {
    sharedPrefs.edit().clear().putString("url", requestOptions.url).putBoolean("gms",
        requestOptions.gms).commit()
  }

  private fun view(requestOptions: RequestOptions) =
      MainComponent.create(c)
          .requestOptions(requestOptions)
          .executeListener({ executeCall(it) })
          .results(results.toList())
          .build()

  private fun executeCall(newRequestOptions: RequestOptions) {
    requestOptions = newRequestOptions

    saveQueryToSharedPrefs()

    if (gmsInstalled && !requestOptions.gms) {
      restartAndRun()
    } else if (!gmsInstalled && requestOptions.gms) {
      installGms()
      okHttpApplication().okhttpClient = null
    }

    if (okHttpApplication().okhttpClient == null) {
      val newClient = createClient()

      okHttpApplication().okhttpClient = newClient
    }

    async {
      val request = Request.Builder().url(requestOptions.url).build()
      results.add(ResponseModel(okHttpApplication().okhttpClient!!.newCall(request)))
      lithoView.setComponentAsync(view(requestOptions))
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

    results.add(ClientCreated("${SSLContext.getDefault().provider}\n${newClient.connectionSpecs()}"))
    return newClient
  }

  fun installGms() {
    try {
      ProviderInstaller.installIfNeeded(this)
      gmsInstalled = true
      results.add(GmsInstall())
    } catch (e: GooglePlayServicesRepairableException) {
      results.add(GmsInstall(e.toString()))
      GoogleApiAvailability.getInstance().showErrorNotification(this, e.connectionStatusCode)
    } catch (e: GooglePlayServicesNotAvailableException) {
      results.add(GmsInstall(e.toString()))
    }

    Security.getProviders().forEach {
      Log.i(TAG, "" + it.name + " " + it.javaClass)
    }

    val s = SSLContext.getDefault().provider
    Log.i(TAG, "" + s)
  }

  private fun restartAndRun(): Nothing {
    ProcessPhoenix.triggerRebirth(applicationContext)
    throw IllegalStateException()
  }

  private fun okHttpApplication() = (application as OkHttpTestApp)

  companion object {
    var TAG = "MainActivity"
  }
}
