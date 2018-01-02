package com.squareup.okhttptestapp.dumper

import android.content.Context
import com.facebook.stetho.dumpapp.DumperContext
import com.facebook.stetho.dumpapp.DumperPlugin
import com.squareup.okhttptestapp.OkHttpTestApp
import com.squareup.okhttptestapp.application
import okhttp3.OkHttpClient
import okhttp3.Request

class TestRequestDumperPlugin(val context: OkHttpTestApp) : DumperPlugin {
  override fun dump(dumpContext: DumperContext) {
    val testClient = context.networkClients.testClient

    val command = dumpContext.argsAsList.take(1).firstOrNull()?.toUpperCase()
    val rest = dumpContext.argsAsList.drop(1)

    when (command) {
      "GET" -> get(testClient, dumpContext, rest)
      else -> usage(dumpContext)
    }
  }

  private fun get(testClient: OkHttpClient, dumpContext: DumperContext, rest: List<String>) {
    val result = testClient.newCall(Request.Builder().url(rest[0]).build()).execute()

    result.use {
      dumpContext.stderr.println(result)
    }
  }

  private fun usage(dumpContext: DumperContext) {
    dumpContext.stderr.println("usage: GET url")
  }

  override fun getName(): String {
    return "request"
  }
}
