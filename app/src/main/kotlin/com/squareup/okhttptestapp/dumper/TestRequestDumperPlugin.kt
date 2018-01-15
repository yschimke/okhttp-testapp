package com.squareup.okhttptestapp.dumper

import com.facebook.stetho.dumpapp.DumperContext
import com.facebook.stetho.dumpapp.DumperPlugin
import com.squareup.okhttptestapp.MainActivity
import com.squareup.okhttptestapp.OkHttpTestApp
import com.squareup.okhttptestapp.model.RequestOptions

class TestRequestDumperPlugin(val context: OkHttpTestApp) : DumperPlugin {
  override fun dump(dumpContext: DumperContext) {
    val command = dumpContext.argsAsList.take(1).firstOrNull()?.toUpperCase()
    val rest = dumpContext.argsAsList.drop(1)

    when (command) {
      "GET" -> get(context.mainActivity, dumpContext, rest)
      else -> usage(dumpContext)
    }
  }

  private fun get(testClient: MainActivity, dumpContext: DumperContext, rest: List<String>) {
    testClient.executeCall(RequestOptions(rest[0]))

//    result.use {
//      dumpContext.stderr.println(result)
//    }
  }

  private fun usage(dumpContext: DumperContext) {
    dumpContext.stderr.println("usage: GET url")
  }

  override fun getName(): String {
    return "request"
  }
}
