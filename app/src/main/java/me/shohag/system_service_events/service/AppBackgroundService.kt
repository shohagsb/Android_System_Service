package me.shohag.system_service_events.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.media.RingtoneManager
import android.net.wifi.WifiManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import me.shohag.system_service_events.R
import me.shohag.system_service_events.broadcast_receiver.BatteryReceiver
import me.shohag.system_service_events.broadcast_receiver.ConnectivityReceiver
import me.shohag.system_service_events.broadcast_receiver.ScreenReceiver
import me.shohag.system_service_events.model.LogModel
import me.shohag.system_service_events.utils.Constants.ACTION_START_SERVICE
import me.shohag.system_service_events.utils.Constants.ACTION_STOP_SERVICE
import me.shohag.system_service_events.utils.Constants.STOP_SERVICE_ACTION

class AppBackgroundService : LifecycleService() {
    private lateinit var screenReceiver: ScreenReceiver
    private lateinit var batteryReceiver: BatteryReceiver
    private lateinit var connectivityReceiver: ConnectivityReceiver

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        intent?.let {
            when (it.action) {
                ACTION_START_SERVICE -> {
                    showNotification()
                    registerBatteryBroadcast()
                    registerScreenBroadcast()
                    registerConnectivityBroadcast()
                    Toast.makeText(applicationContext, "Service Started", Toast.LENGTH_SHORT).show()
                }
                ACTION_STOP_SERVICE -> {
                    unregisterBroadcast()
                    stopForeground(true)
                    stopSelf()
                }
            }
        }

        return START_STICKY
    }


    private fun registerScreenBroadcast() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction("SCREEN_ACTION")

        screenReceiver = object : ScreenReceiver() {
            override fun onEventTrigger(msg: String) {
                super.onEventTrigger(msg)
                logEventList.add(LogModel(logMsg = msg))
                logEvents.postValue(logEventList)
            }
        }
        this.registerReceiver(screenReceiver, filter)
    }

    private fun registerBatteryBroadcast() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_POWER_CONNECTED)
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED)

        batteryReceiver = object : BatteryReceiver() {
            override fun onEventTrigger(
                msg: String,
                batteryPct: Float?,
                acCharging: Boolean,
                usbCharging: Boolean
            ) {
                super.onEventTrigger(msg, batteryPct, acCharging, usbCharging)
                logEventList.add(LogModel(logMsg = msg))
                logEventList.add(LogModel(logMsg = "Battery: $batteryPct%"))
                if(acCharging){
                    logEventList.add(LogModel(logMsg = "Battery: AC Charging"))
                }
                if(usbCharging){
                    logEventList.add(LogModel(logMsg = "Battery: USB Charging"))
                }
                logEvents.postValue(logEventList)
            }
        }
        this.registerReceiver(batteryReceiver, filter)
    }

    /**
     * Unregister all Broadcast Receivers
     * */
    private fun unregisterBroadcast() {
        this.unregisterReceiver(screenReceiver)
        this.unregisterReceiver(batteryReceiver)
        this.unregisterReceiver(connectivityReceiver)
    }


    private fun registerConnectivityBroadcast() {
        val filter = IntentFilter()
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)

        connectivityReceiver = object : ConnectivityReceiver() {
            override fun onEventTrigger(msg: String) {
                super.onEventTrigger(msg)
                logEventList.add(LogModel(logMsg = msg))
                logEvents.postValue(logEventList)
            }
        }
        this.registerReceiver(connectivityReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(applicationContext, "Service Stopped", Toast.LENGTH_SHORT).show()
    }

    private fun showNotification() {
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_CANCEL_CURRENT
        }
        val stopServiceIntent = Intent(this, BatteryReceiver::class.java).also {
            it.action = STOP_SERVICE_ACTION
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this, BROADCAST_REQUEST_CODE, stopServiceIntent, flag
        )

        val notification =
            NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("Monitoring your phone")
                .setContentText("Service is ready to listen events")
                .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                .addAction(R.color.purple_200, "Stop Service", stopPendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).build()

        notification.visibility = Notification.VISIBILITY_PUBLIC

        startForeground(FOREGROUND_NOTIFICATION_ID, notification)
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Background Notification", NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(
                NotificationManager::class.java
            )

            manager.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        val logEvents = MutableLiveData<List<LogModel>>()
        val logEventList = mutableListOf<LogModel>()

        private const val BROADCAST_REQUEST_CODE = 200

        const val CHANNEL_ID =
            "app_Background" // regular notification when apps start or start service(turn on) button push
        const val FOREGROUND_NOTIFICATION_ID = 1
    }
}
