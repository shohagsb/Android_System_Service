package me.shohag.system_service_events.broadcast_receiver


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.widget.Toast
import me.shohag.system_service_events.service.AppBackgroundService
import me.shohag.system_service_events.utils.Constants.ACTION_STOP_SERVICE
import me.shohag.system_service_events.utils.Constants.STOP_SERVICE_ACTION


private const val TAG = "BatteryReceiver"

open class BatteryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {

        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }

        // How charging?
        val chargePlug: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val usbCharge: Boolean = chargePlug == BatteryManager.BATTERY_PLUGGED_USB
        val acCharge: Boolean = chargePlug == BatteryManager.BATTERY_PLUGGED_AC


        when (intent?.action) {
            STOP_SERVICE_ACTION -> {
                context.startService(Intent(context, AppBackgroundService::class.java).apply {
                    this.action = ACTION_STOP_SERVICE
                })
            }
            Intent.ACTION_POWER_CONNECTED -> {
                val batteryPct: Float? = batteryPercentage(batteryStatus)
                onEventTrigger("POWER CONNECTED", batteryPct, acCharge, usbCharge)
                //context.sendBroadcast(Intent().setAction("BATTERY_PCT").putExtra("battery_pct", batteryPct))
                Toast.makeText(
                    context,
                    "POWER CONNECTED",
                    Toast.LENGTH_SHORT
                ).show()
            }

            Intent.ACTION_POWER_DISCONNECTED -> {
                val batteryPct: Float? = batteryPercentage(batteryStatus)
                onEventTrigger(
                    "POWER DISCONNECTED",
                    batteryPct,
                    acCharge,
                    usbCharge
                )
                Toast.makeText(
                    context,
                    "POWER DISCONNECTED",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }


    }

    open fun onEventTrigger(
        msg: String,
        batteryPct: Float?,
        acCharging: Boolean,
        usbCharging: Boolean
    ) {

    }


    private fun batteryPercentage(batteryStatus: Intent?): Float? {
        val batteryPct: Float? = batteryStatus?.let { intent1 ->
            val level: Int = intent1.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent1.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level * 100 / scale.toFloat()
        }
        return batteryPct
    }
}