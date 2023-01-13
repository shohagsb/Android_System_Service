package me.shohag.system_service_events.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

private const val TAG = "ScreenReceiver"

open class ScreenReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SCREEN_OFF->{
                Log.d(TAG, "onReceive: Screen went OFF")
                onEventTrigger("Screen is OFF")
                Toast.makeText(
                    context,
                    "Screen is OFF",
                    Toast.LENGTH_SHORT
                ).show()
            }

            Intent.ACTION_SCREEN_ON->{
                Log.d(TAG, "onReceive: Screen went ON")
                onEventTrigger("Screen is ON")
                Toast.makeText(
                    context,
                    "Screen is ON",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else->{
                //Toast.makeText(context, "Screen onReceive: UNKNOWN-INTENT", Toast.LENGTH_SHORT).show()
            }
        }

    }

    open fun onEventTrigger(msg : String) {
    }


}