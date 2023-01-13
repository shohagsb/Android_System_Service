package me.shohag.system_service_events.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast

private const val TAG = "ConnectivityReceiver"

open class ConnectivityReceiver : BroadcastReceiver() {
    private lateinit var wifi: WifiManager

    override fun onReceive(context: Context, intent: Intent?) {

        when (intent?.action) {
            WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                wifi =
                    context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                if (wifi.isWifiEnabled) {
                    onEventTrigger("WiFi ON")
                    Toast.makeText(
                        context,
                        "WiFi ON",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    onEventTrigger("WiFi OFF")
                    Toast.makeText(
                        context,
                        "WiFi OFF",
                        Toast.LENGTH_SHORT
                    ).show()

                }
//
//                val message = mGetNetworkClass(context)
//                Log.d(TAG, "onReceive: Cellular $message")
//                Toast.makeText(context, "Connectivity onReceive:  Cellular $message", Toast.LENGTH_SHORT).show()
            }
        }
        //

    }


    // Function to find out type of network
    private fun mGetNetworkClass(context: Context): String {

        // ConnectionManager instance
        val mConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mInfo = mConnectivityManager.activeNetworkInfo

        // If not connected, "-" will be displayed
        if (mInfo == null || !mInfo.isConnected) return "-"

        // If Connected to Wifi
        if (mInfo.type == ConnectivityManager.TYPE_WIFI) return "WIFI"

        // If Connected to Mobile
        if (mInfo.type == ConnectivityManager.TYPE_MOBILE) {
            return when (mInfo.subtype) {
                TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN, TelephonyManager.NETWORK_TYPE_GSM -> "2G"
                TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "3G"
                TelephonyManager.NETWORK_TYPE_LTE, TelephonyManager.NETWORK_TYPE_IWLAN, 19 -> "4G"
                TelephonyManager.NETWORK_TYPE_NR -> "5G"
                else -> "?"
            }
        }
        return "?"
    }

    open fun onEventTrigger(msg: String) {
    }

}