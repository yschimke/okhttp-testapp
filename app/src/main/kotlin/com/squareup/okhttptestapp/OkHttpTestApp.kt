package com.squareup.okhttptestapp

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import com.bugsnag.android.Bugsnag
import com.facebook.soloader.SoLoader
import com.facebook.sonar.android.AndroidSonarClient
import com.facebook.sonar.android.utils.SonarUtils
import com.facebook.sonar.plugins.inspector.DescriptorMapping
import com.facebook.sonar.plugins.inspector.InspectorSonarPlugin
import com.facebook.sonar.plugins.network.NetworkSonarPlugin
import com.facebook.sonar.plugins.sharedpreferences.SharedPreferencesSonarPlugin

lateinit var application: OkHttpTestApp

class OkHttpTestApp : Application() {
  lateinit var mainActivity: MainActivity

  val networkSonarPlugin = NetworkSonarPlugin()

  override fun onCreate() {
    super.onCreate()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      Bugsnag.init(applicationContext)
    }

    strictMode()

    SoLoader.init(this, false)
    if (SonarUtils.shouldEnableSonar(applicationContext)) {
      val descriptorMapping = DescriptorMapping.withDefaults()

      val client = AndroidSonarClient.getInstance(this)
      client.addPlugin(networkSonarPlugin)
      client.addPlugin(SharedPreferencesSonarPlugin(applicationContext))
      client.addPlugin(InspectorSonarPlugin(applicationContext, descriptorMapping))
      client.start()
    }

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
