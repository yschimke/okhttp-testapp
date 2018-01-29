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
import com.baulsupp.oksocial.authenticator.ServiceInterceptor
import com.baulsupp.oksocial.credentials.InMemoryCredentialsStore
import com.baulsupp.oksocial.network.DnsSelector
import com.baulsupp.oksocial.network.IPvMode
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
import com.squareup.okhttptestapp.model.PlatformEvent
import com.squareup.okhttptestapp.model.RequestOptions
import com.squareup.okhttptestapp.model.CallEvent
import com.squareup.okhttptestapp.network.NetworkListener
import com.squareup.okhttptestapp.spec.MainComponent
import kotlinx.coroutines.experimental.async
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.platform.AndroidOptimisedPlatform
import java.io.File
import java.security.Provider
import java.security.Security
import java.util.function.Consumer
import javax.net.ssl.SSLContext

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
  private lateinit var androidPlatform: okhttp3.internal.platform.Platform

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
      optimisedPlatform = AndroidOptimisedPlatform.build(this)
      show(PlatformEvent())
    } else {
      show(PlatformEvent("AndroidOptimisedPlatform not available"))
    }
    androidPlatform = AndroidOptimisedPlatform.buildAndroidPlatform()

    okhttpClient = createClient()
  }

  private fun readQueryFromSharedPreferences() {
    val gms = sharedPrefs.getBoolean("gms", true)
    val zipkin = sharedPrefs.getBoolean("zipkin", false)
    val optimized = sharedPrefs.getBoolean("optimized", false)
    val ipMode = sharedPrefs.getString("ipmode", null)?.let { IPvMode.fromString(it) } ?: IPvMode.SYSTEM
    clientOptions = ClientOptions(gms = gms, configSpec = Modern, zipkin = zipkin,
        optimized = optimized, iPvMode = ipMode)

    val url = sharedPrefs.getString("url", "https://www.howsmyssl.com/a/check")
    requestOptions = RequestOptions(url)
  }

  private fun saveQueryToSharedPrefs() {
    sharedPrefs.edit().clear().putString("url", requestOptions.url).putBoolean("gms",
        clientOptions.gms).putBoolean("zipkin", clientOptions.zipkin).putBoolean("optimized",
        clientOptions.optimized).putString("ipmode", clientOptions.iPvMode.name).apply()
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
      show(CallEvent(okhttpClient.newCall(request)))
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
      }
    } else {
      if (SSLContext.getDefault().provider.name == "GmsCore_OpenSSL") {
        SSLContext.setDefault(SSLContext.getInstance("Default"))
        return true
      }
    }

    return false
  }

  private val zipkinUri: String? = "http://kali:9411/"

  private fun createClient(): OkHttpClient {
    var testBuilder = OkHttpClient.Builder()

    val platformName = if (clientOptions.optimized) {
      AndroidOptimisedPlatform.installPlatform(optimisedPlatform)
      optimisedPlatform!!.configureBuilder(testBuilder)
      AndroidOptimisedPlatform::class.simpleName
    } else {
      AndroidOptimisedPlatform.installPlatform(androidPlatform)
      "AndroidPlatform"
    }

    testBuilder.eventListener(TestEventListener())

    testBuilder.cache(Cache(File(cacheDir, "HttpResponseCache"), 10 * 1024 * 1024))

    testBuilder.cookieJar(
        PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(this)))

    testBuilder.connectionSpecs(
        listOf(clientOptions.configSpec.connectionSpec(), ConnectionSpec.CLEARTEXT))

    testBuilder.dns(DnsSelector(clientOptions.iPvMode, Dns.SYSTEM))

    val credentialsStore = InMemoryCredentialsStore()
    var serviceInterceptor = ServiceInterceptor(testBuilder.build(), credentialsStore)
    testBuilder.addNetworkInterceptor(serviceInterceptor)
    testBuilder.addNetworkInterceptor(StethoInterceptor())

    if (clientOptions.zipkin) {
      applyZipkin(testBuilder)
    }

    val newClient = testBuilder.build()

    show(ClientCreated(SSLContext.getDefault().provider, platformName, clientOptions))
    return newClient
  }

  private fun applyZipkin(testBuilder: OkHttpClient.Builder) {
    val reporter = if (zipkinUri != null) {
      UriTransportRegistry.forUri("${zipkinUri}api/v2/spans")
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
      if (zipkinUri != null) {
        Log.i("MainActivity", "trace ${zipkinUri}zipkin/traces/${tc.traceIdString()}")
      }
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
      show(GmsInstall("Repairable: " + e.message, e))
      GoogleApiAvailability.getInstance().showErrorNotification(this, e.connectionStatusCode)
    } catch (e: GooglePlayServicesNotAvailableException) {
      show(GmsInstall("Google Play unavailable", e))
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
