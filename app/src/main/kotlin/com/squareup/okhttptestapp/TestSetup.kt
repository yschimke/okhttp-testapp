package com.squareup.okhttptestapp

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
  fun configureBuilder(clientBuilder: OkHttpClient.Builder): Builder {
    // nothing by default

//    clientBuilder.readTimeout(3, SECONDS)

    val specTLS1_2 = ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS).build()
//
//    val spec = ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
////        .tlsVersions(TlsVersion.TLS_1_2)
////        .cipherSuites(
////            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
////            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
////            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
////            CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
//        .build()

    clientBuilder.connectionSpecs(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT))

//    installGmsProvider();

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
