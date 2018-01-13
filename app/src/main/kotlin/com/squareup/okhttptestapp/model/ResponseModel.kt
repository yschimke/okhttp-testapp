package com.squareup.okhttptestapp.model

import okhttp3.Call

sealed class AppEvent

data class ResponseModel(val call: Call) : AppEvent()

data class SystemState(val state: String) : AppEvent()

data class GmsInstall(val error: String? = null) : AppEvent()

data class ClientCreated(val descripton: String) : AppEvent()
