package com.squareup.okhttptestapp

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.StrictMode
import com.bugsnag.android.Bugsnag
import com.facebook.soloader.SoLoader
import com.facebook.stetho.Stetho
import com.squareup.okhttptestapp.dumper.TestRequestDumperPlugin
import kotlinx.coroutines.experimental.async

lateinit var application: OkHttpTestApp

class OkHttpTestApp : Application() {
  lateinit var mainActivity: MainActivity

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


    registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
      override fun onActivityPaused(activity: Activity?) {
      }

      override fun onActivityResumed(activity: Activity?) {
      }

      override fun onActivityStarted(activity: Activity?) {
      }

      override fun onActivityDestroyed(activity: Activity?) {
      }

      override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
      }

      override fun onActivityStopped(activity: Activity?) {
      }

      override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        if (activity is MainActivity) {
          mainActivity = activity
        }
      }
    })
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
