package com.squareup.okhttptestapp.model

data class ClientOptions(val gms: Boolean, val configSpec: ConfigSpec, val zipkin: Boolean, val optimized: Boolean)