package com.squareup.okhttptestapp.model

import com.squareup.okhttptestapp.network.Cipher
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

class AndroidSpec(val version: Int, val enabledTlsVersions: List<TlsVersion>,
    val supportedTlsVersions: List<TlsVersion>, val enabled: List<String>,
    val supported: List<String>) : ConfigSpec() {

  // TODO warning for modes not supported on running version
  override fun connectionSpec(): ConnectionSpec = ConnectionSpec.Builder(
      ConnectionSpec.COMPATIBLE_TLS)
      .tlsVersions(*enabledTlsVersions.toTypedArray())
      .cipherSuites(*enabled.toTypedArray())
      .supportsTlsExtensions(true)
      .build()

  override fun toString(): String = "Android " + version
}

val androidSpecs: List<AndroidSpec> = (27 downTo 1).map { version ->
  val enabledTls = when (version) {
    in 1..15 -> listOf(TlsVersion.SSL_3_0, TlsVersion.TLS_1_0)
    in 16..22 -> listOf(TlsVersion.SSL_3_0, TlsVersion.TLS_1_0, TlsVersion.TLS_1_1,
        TlsVersion.TLS_1_2)
    in 23..27 -> listOf(TlsVersion.TLS_1_0, TlsVersion.TLS_1_1, TlsVersion.TLS_1_2)
    else -> TODO()
  }

  val supportedTls = when (version) {
    in 1..15 -> listOf(TlsVersion.SSL_3_0, TlsVersion.TLS_1_0)
    in 16..25 -> listOf(TlsVersion.SSL_3_0, TlsVersion.TLS_1_0, TlsVersion.TLS_1_1,
        TlsVersion.TLS_1_2)
    in 26..27 -> listOf(TlsVersion.TLS_1_0, TlsVersion.TLS_1_1, TlsVersion.TLS_1_2)
    else -> TODO()
  }

  val enabled = Cipher.values().filter { it.enabledRange != null && version in it.enabledRange }.map { it.name }
  val supported = Cipher.values().filter { it.supportedRange != null && version in it.supportedRange }.map { it.name }

  AndroidSpec(version, enabledTls, supportedTls, enabled, supported)
}

val allSpecs: List<ConfigSpec> = listOf(Modern, Compatible, Example) + androidSpecs
