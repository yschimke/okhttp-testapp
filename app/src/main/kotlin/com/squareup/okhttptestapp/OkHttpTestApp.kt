package com.squareup.okhttptestapp

import android.app.Application
import android.support.v4.app.Fragment
import com.facebook.stetho.Stetho
import com.squareup.okhttptestapp.dumper.TestRequestDumperPlugin
import android.os.StrictMode
import org.jetbrains.anko.doAsync

class OkHttpTestApp : Application() {
  var networkClients: NetworkClients? = null

  override fun onCreate() {
    super.onCreate()

    strictMode()

    networkClients = NetworkClients(this.applicationContext)

    doAsync {
      Stetho.initialize(Stetho.newInitializerBuilder(applicationContext)
          .enableDumpapp {
            Stetho.DefaultDumperPluginsBuilder(applicationContext)
                .provide(TestRequestDumperPlugin())
                .finish()
          }
          .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(applicationContext))
          .build())
    }

    instance = this
  }

  fun strictMode() {
    StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
        .detectDiskReads()
        .detectDiskWrites()
        .detectNetwork()   // or .detectAll() for all detectable problems
        .penaltyLog()
        .build())
    StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
        .detectLeakedSqlLiteObjects()
        .detectLeakedClosableObjects()
        .penaltyLog()
        .penaltyDeath()
        .build())
  }

  companion object {
    var instance: OkHttpTestApp? = null
  }
}

fun Fragment.networkClients() = (this.activity!!.application as OkHttpTestApp).networkClients!!
