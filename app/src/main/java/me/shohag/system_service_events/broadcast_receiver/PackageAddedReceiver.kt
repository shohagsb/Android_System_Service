package me.shohag.system_service_events.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast

private const val TAG = "PackageAddedTAG"
open class PackageAddedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && context != null) {
            // Check this condition because the broadcast receiver
            // is getting triggered on some devices running above Oreo
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

                val packageManager = context.packageManager
                val appUid = intent.getIntExtra(Intent.EXTRA_UID, 0)

                if (intent.action == "android.intent.action.PACKAGE_FULLY_REMOVED") {
                    Log.d(TAG, "PACKAGE_FULLY_REMOVED $appUid")
                    onEventTrigger("PACKAGE_FULLY_REMOVED $appUid")
                } else {
                    val applicationInfo = packageManager?.getApplicationInfo(
                        packageManager.getNameForUid(appUid)!!, PackageManager.GET_META_DATA
                    )!!

                    val appName = packageManager.getApplicationLabel(applicationInfo).toString()
                    val appPackageName = applicationInfo.packageName

                    if (intent.action == "android.intent.action.PACKAGE_ADDED") {
                        Log.d(TAG, "PACKAGE_ADDED $appPackageName , $appName")
                        onEventTrigger("PACKAGE_ADDED $appPackageName , $appName")
                    } else if (intent.action == "android.intent.action.PACKAGE_REPLACED") {
                        Log.d(TAG, "PACKAGE_REPLACED $appPackageName , $appName")
                        onEventTrigger("PACKAGE_REPLACED $appPackageName , $appName")
                    }
                }
            }
        }
    }

    open fun onEventTrigger(msg: String) {

    }
}