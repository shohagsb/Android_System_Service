package me.shohag.system_service_events.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

open class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                onEventTrigger("BOOT-COMPLETED")
                Toast.makeText(
                    context, "BOOT-COMPLETED", Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    open fun onEventTrigger(msg: String) {

    }
}