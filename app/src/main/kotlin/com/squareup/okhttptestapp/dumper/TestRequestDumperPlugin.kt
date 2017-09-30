package com.squareup.okhttptestapp.dumper

import com.facebook.stetho.dumpapp.DumperContext
import com.facebook.stetho.dumpapp.DumperPlugin

class TestRequestDumperPlugin: DumperPlugin {
  override fun dump(dumpContext: DumperContext) {
    dumpContext.stdout.println("Here")
  }

  override fun getName(): String {
    return "request"
  }
}
