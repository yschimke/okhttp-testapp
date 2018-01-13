package com.squareup.okhttptestapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import com.facebook.litho.LithoView
import com.facebook.litho.sections.SectionContext
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import com.jakewharton.processphoenix.ProcessPhoenix
import com.squareup.okhttptestapp.model.ResponseModel
import com.squareup.okhttptestapp.spec.MainComponent
import kotlinx.coroutines.experimental.async
import okhttp3.Request

class MainActivity : Activity() {
  lateinit var c: SectionContext
  val results = mutableListOf<ResponseModel>()

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
    }

    async {
      val request = Request.Builder().url(requestOptions.url).build()
      results.add(ResponseModel(testClient().newCall(request)))
      lithoView.setComponentAsync(view(requestOptions))
    }
  }

  fun installGms() {
    try {
      Log.i(TAG, "Installing GMS Provider")
      ProviderInstaller.installIfNeeded(this)
      gmsInstalled = true
    } catch (e: GooglePlayServicesRepairableException) {
      GoogleApiAvailability.getInstance().showErrorNotification(this, e.connectionStatusCode)
    } catch (e: GooglePlayServicesNotAvailableException) {
      TODO()
    }
  }

  private fun restartAndRun(): Nothing {
    ProcessPhoenix.triggerRebirth(applicationContext)
    throw IllegalStateException()
  }

  private fun testClient() = (application as OkHttpTestApp).networkClients.testClient

  companion object {
    var TAG = "MainActivity"
  }
}
