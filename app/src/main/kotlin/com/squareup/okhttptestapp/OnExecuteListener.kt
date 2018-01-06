package com.squareup.okhttptestapp

import okhttp3.Request

@FunctionalInterface
interface OnExecuteListener {
  fun onExecute(request: Request): Unit
}