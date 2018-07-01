package com.squareup.okhttptestapp

import okhttp3.Call
import okhttp3.Connection
import okhttp3.EventListener
import okhttp3.Handshake
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy

class TestEventListener : EventListener() {
  override fun callStart(call: Call?) {}

  override fun dnsStart(call: Call?, domainName: String?) {}

  override fun dnsEnd(call: Call?, domainName: String?, inetAddressList: List<InetAddress>?) {}

  override fun connectStart(call: Call?, inetSocketAddress: InetSocketAddress?, proxy: Proxy?) {}

  override fun secureConnectStart(call: Call?) {}

  override fun secureConnectEnd(call: Call?, handshake: Handshake?) {
//    Log.i(TAG, handshake?.cipherSuite()?.javaName())
  }

  override fun connectEnd(
      call: Call?,
      inetSocketAddress: InetSocketAddress?,
      proxy: Proxy?,
      protocol: Protocol?
  ) {
  }

  override fun connectFailed(
      call: Call?,
      inetSocketAddress: InetSocketAddress?,
      proxy: Proxy?,
      protocol: Protocol?,
      ioe: IOException?
  ) {
  }

  override fun connectionAcquired(call: Call?, connection: Connection?) {}

  override fun connectionReleased(call: Call?, connection: Connection?) {}

  override fun requestHeadersStart(call: Call?) {}

  override fun requestHeadersEnd(call: Call?, request: Request?) {}

  override fun requestBodyStart(call: Call?) {}

  override fun requestBodyEnd(call: Call?, byteCount: Long) {}

  override fun responseHeadersStart(call: Call?) {}

  override fun responseHeadersEnd(call: Call?, response: Response?) {}

  override fun responseBodyStart(call: Call?) {}

  override fun responseBodyEnd(call: Call?, byteCount: Long) {}

  override fun callEnd(call: Call?) {}

  override fun callFailed(call: Call?, ioe: IOException?) {}

  companion object {
    val TAG = "TestEventListener"
  }
}
