package com.squareup.okhttptestapp.model

import android.graphics.Color
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import com.squareup.okhttptestapp.stackTraceString
import okhttp3.Call
import java.security.Provider
import javax.net.ssl.SSLContext

val errorSpan = ForegroundColorSpan(Color.parseColor("magenta"))
val successSpan = ForegroundColorSpan(Color.parseColor("olive"))
val progressSpan = ForegroundColorSpan(Color.GRAY)
val bodySpan = ForegroundColorSpan(Color.DKGRAY)

sealed class AppEvent {
  open fun display(expanded: Boolean): CharSequence {
    TODO("not implemented")
  }
}

data class CallEvent(val call: Call) : AppEvent()

data class SystemState(val state: String) : AppEvent() {
  override fun display(expanded: Boolean): CharSequence {
    return "System State: $state"
  }
}

data class GmsInstall(val error: String? = null,
    val e: Exception? = null) : AppEvent() {
  override fun display(expanded: Boolean): CharSequence {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      val r = SpannableStringBuilder("GMS Provider: ")

      if (error != null) {
        r.append(error, errorSpan, 0)
      } else {
        r.append("installed", successSpan, 0)
      }

      if (e != null && expanded) {
        r.append("\n", bodySpan, 0)
        r.append(e.stackTraceString(), bodySpan, 0)
      }

      return r
    } else {
      return "GMS Provider: ${error ?: "installed"}"
    }
  }
}

data class ClientCreated(val description: Provider, val platformName: String?,
    val clientOptions: ClientOptions) : AppEvent() {
  override fun display(expanded: Boolean): CharSequence {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      val r = SpannableStringBuilder("Client: ")

      r.append("${SSLContext.getDefault().provider} $platformName")

      if (expanded) {
        r.append("\n", bodySpan, 0)
        r.append(clientOptions.toString(), bodySpan, 0)
      }

      return r
    } else {
      return "Client: " + "${SSLContext.getDefault().provider} $platformName ${clientOptions.configSpec} ${clientOptions.iPvMode}"
    }
  }
}

data class NetworkEvent(val description: String) : AppEvent() {
  override fun display(expanded: Boolean): CharSequence {
    return "Network: $description"
  }
}

data class PlatformEvent(val error: String? = null) : AppEvent() {
  override fun display(expanded: Boolean): CharSequence {
    return "AndroidOptimizedPlatform: ${error ?: " available"}"
  }
}