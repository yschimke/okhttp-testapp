package com.squareup.okhttptestapp.model

import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.TlsVersion

sealed class ConfigSpec {
  abstract fun connectionSpec(): ConnectionSpec
}

object Modern : ConfigSpec() {
  override fun connectionSpec(): ConnectionSpec = ConnectionSpec.MODERN_TLS
  override fun toString(): String = "Modern"
}

object Compatible : ConfigSpec() {
  override fun connectionSpec(): ConnectionSpec = ConnectionSpec.COMPATIBLE_TLS
  override fun toString(): String = "Compatible"
}

object Example : ConfigSpec() {
  override fun connectionSpec(): ConnectionSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
      .tlsVersions(TlsVersion.TLS_1_2)
      .cipherSuites(
          CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
          CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
          CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
      .build()

  override fun toString(): String = "Example"
}

val allSpecs = listOf(Modern, Compatible, Example)
