package com.squareup.okhttptestapp.model

import okhttp3.Call

sealed class AppEvent

data class CallEvent(val call: Call) : AppEvent()

data class SystemState(val state: String) : AppEvent()

data class GmsInstall(val error: String? = null) : AppEvent()

data class ClientCreated(val description: String) : AppEvent()

data class NetworkEvent(val description: String) : AppEvent()

data class PlatformEvent(val error: String? = null) : AppEvent()
