package com.squareup.okhttptestapp.network

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import com.squareup.okhttptestapp.MainActivity
import com.squareup.okhttptestapp.model.NetworkEvent

@RequiresApi(Build.VERSION_CODES.M)
class NetworkListener(val main: MainActivity) : ConnectivityManager.NetworkCallback() {
  override fun onCapabilitiesChanged(
      network: Network?,
      networkCapabilities: NetworkCapabilities?
  ) {
    show(NetworkEvent("capabilities $network $networkCapabilities"))
  }

  private var lastEvent: NetworkEvent? = null
  private var lastEventTime: Long = 0

  private fun show(networkEvent: NetworkEvent) {
    val eventTime = System.currentTimeMillis()

    if (lastEvent == null || lastEvent != networkEvent || eventTime - lastEventTime > 50) {
      lastEvent = networkEvent
      lastEventTime = eventTime
      main.show(networkEvent)
    } else {
      Log.i("NetworkListener", "hide " + networkEvent)
    }
  }

  override fun onAvailable(network: Network?) {
    show(NetworkEvent("available $network"))
  }

  override fun onUnavailable() {
    show(NetworkEvent("unavailable"))
  }

  override fun onLost(network: Network?) {
    show(NetworkEvent("lost $network"))
  }

  override fun onLinkPropertiesChanged(network: Network?, linkProperties: LinkProperties?) {
    show(NetworkEvent("properties of $network $linkProperties"))
  }

  override fun onLosing(network: Network?, maxMsToLive: Int) {
    show(NetworkEvent("losing $network in $maxMsToLive ms"))
  }
}