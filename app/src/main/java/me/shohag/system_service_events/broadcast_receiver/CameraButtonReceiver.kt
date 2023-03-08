package me.shohag.system_service_events.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

open class CameraButtonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            Intent.ACTION_CAMERA_BUTTON->{
                onEventTrigger("CAMERA-ACTIVATED")
                Toast.makeText(
                    context, "CAMERA-ACTIVATED", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    open fun onEventTrigger(msg: String) {

    }
}