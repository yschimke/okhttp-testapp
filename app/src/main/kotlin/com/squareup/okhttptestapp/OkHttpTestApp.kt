package com.squareup.okhttptestapp

import android.app.Application
import android.support.v4.app.Fragment
import com.facebook.stetho.Stetho
import com.squareup.okhttptestapp.dumper.TestRequestDumperPlugin

class OkHttpTestApp : Application() {
  var networkClients: NetworkClients? = null

  override fun onCreate() {
    super.onCreate()

    networkClients = NetworkClients(this.applicationContext)

    Stetho.initialize(Stetho.newInitializerBuilder(applicationContext)
        .enableDumpapp {
          Stetho.DefaultDumperPluginsBuilder(applicationContext)
              .provide(TestRequestDumperPlugin())
              .finish()
        }
        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(applicationContext))
        .build())

    instance = this
  }

  companion object {
    var instance: OkHttpTestApp? = null
  }
}

fun Fragment.networkClients() = (this.activity!!.application as OkHttpTestApp).networkClients!!
