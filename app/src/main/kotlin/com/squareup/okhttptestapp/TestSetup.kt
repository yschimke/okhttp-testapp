package com.squareup.okhttptestapp

import android.content.Context
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
import okhttp3.Request
import java.util.concurrent.TimeUnit.SECONDS
import okhttp3.CipherSuite
import okhttp3.TlsVersion
import okhttp3.ConnectionSpec
import okhttp3.TlsVersion.TLS_1_1
import okhttp3.TlsVersion.TLS_1_2
import java.util.Collections


object TestSetup {
  fun configureBuilder(context: Context, clientBuilder: OkHttpClient.Builder): Builder {
    // nothing by default

//    clientBuilder.readTimeout(3, SECONDS)

//    clientBuilder.connectionSpecs(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT))

//    installGmsProvider();

    clientBuilder.cookieJar(PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context)))

    return clientBuilder
  }

//  private fun installGmsProvider() {
//
//    try {
//      ProviderInstaller.installIfNeeded(getContext())
//    } catch (e: GooglePlayServicesRepairableException) {
//
//      // Indicates that Google Play services is out of date, disabled, etc.
//
//      // Prompt the user to install/update/enable Google Play services.
//      GooglePlayServicesUtil.showErrorNotification(
//          e.getConnectionStatusCode(), getContext())
//
//      // Notify the SyncManager that a soft error occurred.
//      syncResult.stats.numIOExceptions++
//      return
//
//    } catch (e: GooglePlayServicesNotAvailableException) {
//      // Indicates a non-recoverable error; the ProviderInstaller is not able
//      // to install an up-to-date Provider.
//
//      // Notify the SyncManager that a hard error occurred.
//      syncResult.stats.numAuthExceptions++
//      return
//    }
//
//
//    // If this is reached, you know that the provider was already up-to-date,
//    // or was successfully updated.
//  }

  fun getDefaultUrl() = "https://www.kexindai.com/UploadFiles/Sys/Adv/c0f5fbd843b744bd9bfe942351ec3da2.jpg"

  fun createRequest(url: String): Request {
    return Request.Builder().url(url).build()
  }
}
