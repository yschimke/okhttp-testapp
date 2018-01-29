package com.squareup.okhttptestapp

import java.io.PrintWriter
import java.io.StringWriter

fun Exception.stackTraceString(): String = StringWriter().use {
  this.printStackTrace(PrintWriter(it))
  it.toString()
}