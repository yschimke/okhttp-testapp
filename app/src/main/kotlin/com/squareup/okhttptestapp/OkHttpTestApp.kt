package com.squareup.okhttptestapp

import android.app.Application
import android.os.StrictMode
import android.support.v4.app.Fragment
import com.bugsnag.android.Bugsnag
import com.facebook.soloader.SoLoader
import com.facebook.stetho.Stetho
import com.squareup.okhttptestapp.dumper.TestRequestDumperPlugin
import kotlinx.coroutines.experimental.async
import okhttp3.OkHttpClient

lateinit var application: OkHttpTestApp

class OkHttpTestApp : Application() {
  override fun onCreate() {
    super.onCreate()

    Bugsnag.init(applicationContext)

    strictMode()

    async {
      Stetho.initialize(Stetho.newInitializerBuilder(applicationContext)
          .enableDumpapp {
            Stetho.DefaultDumperPluginsBuilder(applicationContext)
                .provide(TestRequestDumperPlugin(this@OkHttpTestApp))
                .finish()
          }
          .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(applicationContext))
          .build())
    }

    SoLoader.init(this, false)

    application = this
  }

  private fun strictMode() {
    if (BuildConfig.DEBUG) {
      StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
          .detectDiskReads()
          .detectDiskWrites()
          .detectNetwork()
          .penaltyLog()
          .build())

      StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
          .detectLeakedSqlLiteObjects()
          .detectLeakedClosableObjects()
          .penaltyLog()
          .build())
    }
  }

  var okhttpClient: OkHttpClient? = null
}

fun Fragment.networkClients() = (this.activity!!.application as OkHttpTestApp).okhttpClient
