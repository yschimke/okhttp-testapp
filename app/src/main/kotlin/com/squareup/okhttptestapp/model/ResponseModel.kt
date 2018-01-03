package com.squareup.okhttptestapp.model

import okhttp3.Response

data class ResponseModel(val requestNum: Int, val body: String, val response: Response)
