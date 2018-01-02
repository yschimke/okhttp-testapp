package com.squareup.okhttptestapp

import android.app.Application
import android.support.v4.app.Fragment
import com.facebook.stetho.Stetho
import com.squareup.okhttptestapp.dumper.TestRequestDumperPlugin
import android.os.StrictMode
import com.bugsnag.android.Bugsnag
import com.facebook.soloader.SoLoader
import org.jetbrains.anko.doAsync

var application: OkHttpTestApp? = null

class OkHttpTestApp : Application() {
  lateinit var networkClients: NetworkClients

  override fun onCreate() {
    super.onCreate()

    Bugsnag.init(applicationContext)

    strictMode()

    networkClients = NetworkClients(this.applicationContext)

    doAsync {
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
}

fun Fragment.networkClients() = (this.activity!!.application as OkHttpTestApp).networkClients
