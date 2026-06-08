package com.yanachernaya.lumie.presentation.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkMonitor @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    fun getCurrentConnectionType(): ConnectionType {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return ConnectionType.NO_INTERNET
        val capabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return ConnectionType.NO_INTERNET

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.WIFI
            else -> ConnectionType.UNKNOWN
        }
    }
}

enum class ConnectionType {
    WIFI,
    CELLULAR,
    NO_INTERNET,
    UNKNOWN
}