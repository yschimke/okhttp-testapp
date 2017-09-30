package com.squareup.okhttptestapp.dumper

import com.facebook.stetho.dumpapp.DumperContext
import com.facebook.stetho.dumpapp.DumperPlugin
import com.squareup.okhttptestapp.OkHttpTestApp
import okhttp3.OkHttpClient
import okhttp3.Request

class TestRequestDumperPlugin : DumperPlugin {
  override fun dump(dumpContext: DumperContext) {
    val testClient = OkHttpTestApp.instance?.networkClients?.testClient

    if (testClient == null) {
      dumpContext.stderr.println("usage: GET url")
      return
    }

    var command = dumpContext.argsAsList.take(1).firstOrNull()?.toUpperCase()
    var rest = dumpContext.argsAsList.drop(1)

    when (command) {
      "GET" -> get(testClient, dumpContext, rest)
      else -> usage(dumpContext)
    }
  }

  private fun get(testClient: OkHttpClient, dumpContext: DumperContext, rest: List<String>) {
    val result = testClient.newCall(Request.Builder().url(rest[0]).build()).execute()

    dumpContext.stderr.println(result)
  }

  private fun usage(dumpContext: DumperContext) {
    dumpContext.stderr.println("usage: GET url")
  }

  override fun getName(): String {
    return "request"
  }

  companion object {
    val TAG = "TestRequestDumperPlugin"
  }
}
