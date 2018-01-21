package com.squareup.okhttptestapp.model

import okhttp3.Response
import java.io.IOException

sealed class ResponseModel

data class InProgress(val since: Long = System.currentTimeMillis()): ResponseModel()

data class FailedResponse(val exception: IOException): ResponseModel()

data class CompletedResponse(val response: Response, val code: Int, val bodyText: String?): ResponseModel()
