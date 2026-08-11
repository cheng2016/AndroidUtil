package io.github.cheng2016.androidutil.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build

/**
 * 基于 [NetworkCapabilities] 的网络状态快照与监听（不使用已废弃的 NetworkInfo）。
 *
 * 不读取 Wi‑Fi SSID / MAC，避免额外定位权限与隐私风险。
 */
object NetworkStatus {
    /** 当前是否声明具备互联网能力（[NetworkCapabilities.NET_CAPABILITY_INTERNET]）。 */
    @JvmStatic
    fun isOnline(context: Context): Boolean {
        val caps = activeCaps(context) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /** 系统是否已验证网络可访问公网（[NetworkCapabilities.NET_CAPABILITY_VALIDATED]）。 */
    @JvmStatic
    fun isValidated(context: Context): Boolean {
        val caps = activeCaps(context) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /** 是否走蜂窝网络。 */
    @JvmStatic
    fun isCellular(context: Context): Boolean = hasTransport(context, NetworkCapabilities.TRANSPORT_CELLULAR)

    /** 是否走 Wi‑Fi。 */
    @JvmStatic
    fun isWifi(context: Context): Boolean = hasTransport(context, NetworkCapabilities.TRANSPORT_WIFI)

    /** 是否走 VPN。 */
    @JvmStatic
    fun isVpn(context: Context): Boolean = hasTransport(context, NetworkCapabilities.TRANSPORT_VPN)

    /**
     * 注册网络变化回调，返回可关闭的订阅。务必在生命周期结束时 [AutoCloseable.close]。
     */
    @JvmStatic
    fun observe(
        context: Context,
        listener: Listener,
    ): AutoCloseable {
        val app = context.applicationContext
        val cm = connectivity(app) ?: return AutoCloseable { }
        val callback =
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    listener.onChanged(snapshot(app))
                }

                override fun onLost(network: Network) {
                    listener.onChanged(snapshot(app))
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities,
                ) {
                    listener.onChanged(snapshot(app))
                }
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cm.registerDefaultNetworkCallback(callback)
        } else {
            val request =
                NetworkRequest
                    .Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
            @Suppress("DEPRECATION")
            cm.registerNetworkCallback(request, callback)
        }
        listener.onChanged(snapshot(app))
        return AutoCloseable {
            try {
                cm.unregisterNetworkCallback(callback)
            } catch (_: Exception) {
                // already unregistered
            }
        }
    }

    /** 一次性快照。 */
    @JvmStatic
    fun snapshot(context: Context): Snapshot {
        val caps = activeCaps(context)
        return Snapshot(
            online = isOnline(context),
            validated = isValidated(context),
            wifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true,
            cellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true,
            vpn = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true,
        )
    }

    private fun activeCaps(context: Context): NetworkCapabilities? {
        val cm = connectivity(context) ?: return null
        val network = cm.activeNetwork ?: return null
        return cm.getNetworkCapabilities(network)
    }

    private fun hasTransport(
        context: Context,
        transport: Int,
    ): Boolean = activeCaps(context)?.hasTransport(transport) == true

    private fun connectivity(context: Context): ConnectivityManager? =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    fun interface Listener {
        fun onChanged(snapshot: Snapshot)
    }

    data class Snapshot(
        val online: Boolean,
        val validated: Boolean,
        val wifi: Boolean,
        val cellular: Boolean,
        val vpn: Boolean,
    )
}
