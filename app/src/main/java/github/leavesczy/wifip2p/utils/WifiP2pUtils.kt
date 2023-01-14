package github.leavesczy.wifip2p.utils

import android.net.wifi.p2p.WifiP2pDevice

/**
 * @Author: leavesCZY
 * @Desc:
 */
object WifiP2pUtils {

    fun getDeviceStatus(deviceStatus: Int): String {
        return when (deviceStatus) {
            WifiP2pDevice.AVAILABLE -> "usable"
            WifiP2pDevice.INVITED -> "invited"
            WifiP2pDevice.CONNECTED -> "connected"
            WifiP2pDevice.FAILED -> "failure"
            WifiP2pDevice.UNAVAILABLE -> "unavailable"
            else -> "未知"
        }
    }

}