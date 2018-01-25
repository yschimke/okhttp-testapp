package com.squareup.okhttptestapp.model

import com.baulsupp.oksocial.network.IPvMode

data class ClientOptions(val gms: Boolean, val configSpec: ConfigSpec, val zipkin: Boolean, val optimized: Boolean, val iPvMode: IPvMode)