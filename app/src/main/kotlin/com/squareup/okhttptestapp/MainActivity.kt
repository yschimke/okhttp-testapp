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
import brave.Tracing
import brave.http.HttpTracing
import brave.internal.Platform
import brave.propagation.TraceContext
import brave.sampler.Sampler
import com.baulsupp.oksocial.tracing.HttpUriHandler
import com.baulsupp.oksocial.tracing.UriTransportRegistry
import com.baulsupp.oksocial.tracing.ZipkinTracingInterceptor
import com.baulsupp.oksocial.tracing.ZipkinTracingListener
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
import com.squareup.okhttptestapp.model.ClientOptions
import com.squareup.okhttptestapp.model.GmsInstall
import com.squareup.okhttptestapp.model.Modern
import com.squareup.okhttptestapp.model.RequestOptions
import com.squareup.okhttptestapp.model.ResponseModel
import com.squareup.okhttptestapp.network.NetworkListener
import com.squareup.okhttptestapp.spec.MainComponent
import kotlinx.coroutines.experimental.async
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.platform.AndroidOptimisedPlatform
import java.io.File
import java.io.Flushable
import java.net.URI
import java.security.Provider
import java.security.Security
import java.util.Timer
import java.util.TimerTask
import java.util.function.Consumer
import javax.net.ssl.SSLContext
import kotlin.concurrent.scheduleAtFixedRate

class MainActivity : Activity() {
  lateinit var c: SectionContext
  val results = mutableListOf<AppEvent>()

  private lateinit var lithoView: LithoView

  private lateinit var requestOptions: RequestOptions

  private lateinit var clientOptions: ClientOptions

  private lateinit var sharedPrefs: SharedPreferences

  private lateinit var okhttpClient: OkHttpClient

  private var gmsProvider: Provider? = null

  private val scrollController = RecyclerCollectionEventsController()

  private var optimisedPlatform: AndroidOptimisedPlatform? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    sharedPrefs = this.getSharedPreferences("com.squareup.okhttptestapp", Context.MODE_PRIVATE)
    readQueryFromSharedPreferences()

    c = SectionContext(this)
    lithoView = LithoView.create(this, view())
    setContentView(lithoView)

    loadGmsProvider()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      registerNetworkListener()
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      optimisedPlatform = AndroidOptimisedPlatform.install(this)
    }

    okhttpClient = createClient()
  }

  private fun readQueryFromSharedPreferences() {
    val gms = sharedPrefs.getBoolean("gms", true)
    clientOptions = ClientOptions(gms = gms, configSpec = Modern, zipkin = true)

    val url = sharedPrefs.getString("url", "https://www.howsmyssl.com/a/check")
    requestOptions = RequestOptions(url)
  }

  private fun saveQueryToSharedPrefs() {
    sharedPrefs.edit().clear().putString("url", requestOptions.url).putBoolean("gms",
        clientOptions.gms).apply()
  }

  private fun view() =
      MainComponent.create(c)
          .initialClientOptions(clientOptions)
          .initialRequestOptions(requestOptions)
          .executeListener { executeCall(it) }
          .configListener { updateClientOptions(it) }
          .gmsAvailable(gmsProvider != null)
          .scrollController(scrollController)
          .results(results.toList())
          .build()

  private fun updateClientOptions(clientOptions: ClientOptions) {
    this.clientOptions = clientOptions

    setupProviders()
    okhttpClient = createClient()

    saveQueryToSharedPrefs()
  }

  fun executeCall(newRequestOptions: RequestOptions) {
    requestOptions = newRequestOptions

    saveQueryToSharedPrefs()

    async {
      val request = Request.Builder().url(requestOptions.url).build()
      show(ResponseModel(okhttpClient.newCall(request)))
    }
  }

  fun show(model: AppEvent) {
    results.add(model)
    lithoView.setComponent(view())
    scrollController.requestScrollToPosition(results.size, true)
  }

  @RequiresApi(Build.VERSION_CODES.M)
  private fun registerNetworkListener() {
    val connectivityManager = getSystemService(ConnectivityManager::class.java)
    val request = NetworkRequest.Builder().addCapability(
        NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
    connectivityManager.registerNetworkCallback(request, NetworkListener(this))
  }

  private fun setupProviders(): Boolean {
    if (clientOptions.gms) {
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

  private val zipkinSenderUri: String? = "http://kali:9411/api/v2/spans"

  private fun createClient(): OkHttpClient {
    val testBuilder = OkHttpClient.Builder()
    testBuilder.eventListener(TestEventListener())

    testBuilder.cache(Cache(File(cacheDir, "HttpResponseCache"), 10 * 1024 * 1024))

    testBuilder.cookieJar(
        PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(this)))

    testBuilder.connectionSpecs(
        listOf(clientOptions.configSpec.connectionSpec(), ConnectionSpec.CLEARTEXT))

//    val credentialsStore = InMemoryCredentialsStore()
//    var serviceInterceptor = ServiceInterceptor(testBuilder.build(), credentialsStore)
//    testBuilder.addNetworkInterceptor(serviceInterceptor)
    testBuilder.addNetworkInterceptor(StethoInterceptor())

    if (clientOptions.zipkin) {
      applyZipkin(zipkinSenderUri, testBuilder)
    }

    val newClient = testBuilder.build()

    show(ClientCreated("${SSLContext.getDefault().provider} ${clientOptions.configSpec}"))
    return newClient
  }

  private fun applyZipkin(zipkinSenderUri: String?, testBuilder: OkHttpClient.Builder) {
    val reporter = if (zipkinSenderUri != null) {
      HttpUriHandler().buildSender(URI.create(zipkinSenderUri))
    } else {
      Platform.get().reporter()
    }

    val tracing = Tracing.newBuilder()
        .localServiceName("okhttp-testclient")
        .spanReporter(reporter)
        .sampler(Sampler.ALWAYS_SAMPLE)
        .build()

    val httpTracing = HttpTracing.create(tracing)
    val tracer = tracing.tracer()

    val opener = Consumer { tc: TraceContext ->
      if (reporter is Flushable) {
        reporter.flush()
      }
      Log.i("MainActivity", "trace ${tc.traceIdString()}")
    }

    testBuilder.eventListenerFactory { call ->
      ZipkinTracingListener(call, tracer, httpTracing, opener, true)
    }
    testBuilder.addNetworkInterceptor(ZipkinTracingInterceptor(tracing))
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

    gmsProvider = Security.getProvider("GmsCore_OpenSSL")

    Security.removeProvider("GmsCore_OpenSSL")
  }

  companion object {
    var TAG = "MainActivity"
  }
}
