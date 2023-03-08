package me.shohag.system_service_events.service


import android.app.*
import android.app.ActivityManager.RunningTaskInfo
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import me.shohag.system_service_events.R
import me.shohag.system_service_events.broadcast_receiver.*
import me.shohag.system_service_events.model.LogModel
import me.shohag.system_service_events.utils.Constants.ACTION_START_SERVICE
import me.shohag.system_service_events.utils.Constants.ACTION_STOP_SERVICE
import me.shohag.system_service_events.utils.Constants.STOP_SERVICE_ACTION
import kotlin.concurrent.thread


private const val TAG = "AppService"

class AppBackgroundService : LifecycleService() {
    private var screenReceiver: ScreenReceiver? = null
    private var batteryReceiver: BatteryReceiver? = null
    private var connectivityReceiver: ConnectivityReceiver? = null

    private var bootCompletedReceiver: BootCompletedReceiver? = null
    private var packageAddedReceiver: PackageAddedReceiver? = null
    private var cameraButtonReceiver: CameraButtonReceiver? = null

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private val delay = 5000L

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
                    //registerBootCompletedBroadcast()
                    registerPackageAddedBroadcast()
                    registerCameraButtonBroadcast()

                    thread {
                        // call the method from a background thread
                        checkChangedPackages(this.applicationContext)
                    }
                    Toast.makeText(applicationContext, "Service Started", Toast.LENGTH_SHORT).show()
//                    handler = Handler()
//                    runnable = Runnable {
//                        handler.postDelayed(runnable, delay)
//                        Toast.makeText(
//                            applicationContext, "Code Executed",
//                            Toast.LENGTH_SHORT
//                        ).show()
//
//                        logEventList.add(LogModel(logMsg = "Code Executed"))
//                        logEvents.postValue(logEventList)
//                        monitorApp()
//                    }
                  //  handler.postDelayed(runnable, delay)
                }
                ACTION_STOP_SERVICE -> {
                    unregisterBroadcast()
                    stopForeground(true)
                    stopSelf()
                }
                else -> {

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
                if (acCharging) {
                    logEventList.add(LogModel(logMsg = "Battery: AC Charging"))
                }
                if (usbCharging) {
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
        screenReceiver?.let {
            this.unregisterReceiver(it)
        }
        batteryReceiver?.let {
            this.unregisterReceiver(it)
        }
        connectivityReceiver?.let {
            this.unregisterReceiver(it)
        }
        bootCompletedReceiver?.let {
            this.unregisterReceiver(it)
        }
        packageAddedReceiver?.let {
            this.unregisterReceiver(it)
        }
        cameraButtonReceiver?.let {
            this.unregisterReceiver(it)
        }
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

    private fun registerBootCompletedBroadcast() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_BOOT_COMPLETED)

        bootCompletedReceiver = object : BootCompletedReceiver() {
            override fun onEventTrigger(msg: String) {
                super.onEventTrigger(msg)
                logEventList.add(LogModel(logMsg = msg))
                logEvents.postValue(logEventList)
            }
        }
        this.registerReceiver(bootCompletedReceiver, filter)
    }

    private fun registerPackageAddedBroadcast() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_PACKAGE_INSTALL)
        filter.addAction(Intent.ACTION_PACKAGE_ADDED)

        packageAddedReceiver = object : PackageAddedReceiver() {
            override fun onEventTrigger(msg: String) {
                super.onEventTrigger(msg)
                logEventList.add(LogModel(logMsg = msg))
                logEvents.postValue(logEventList)
            }
        }
        this.registerReceiver(packageAddedReceiver, filter)
    }

    private fun registerCameraButtonBroadcast() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_CAMERA_BUTTON)

        bootCompletedReceiver = object : BootCompletedReceiver() {
            override fun onEventTrigger(msg: String) {
                super.onEventTrigger(msg)
                logEventList.add(LogModel(logMsg = msg))
                logEvents.postValue(logEventList)
            }
        }
        this.registerReceiver(bootCompletedReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
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



    private fun monitorApp() {
//        val usage = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
//        val time = System.currentTimeMillis()
//        val stats = usage.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time)
//        val sortedStats = stats.sortedWith(compareBy { it.lastTimeUsed })
//        if (sortedStats.isNotEmpty()){
//            val packageName = sortedStats[sortedStats.size - 1].packageName
//            logEventList.add(LogModel(logMsg = packageName))
//            logEvents.postValue(logEventList)
//        }else{
//            logEventList.add(LogModel(logMsg = "Nothing Found"))
//            logEvents.postValue(logEventList)
//        }

//
//        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
//        // Getting the tasks in the form of a list
//        val mRecentTasks: List<ActivityManager.RunningTaskInfo> = activityManager.getRunningTasks(Int.MAX_VALUE)
//        for (runAppProcess in mRecentTasks) {
//            val mString = "${runAppProcess.baseActivity}"
//
//            logEventList.add(LogModel(logMsg = mString))
//            logEvents.postValue(logEventList)
//
//        }
    }

    private fun checkChangedPackages(context: Context) {
        val packageManagerApps = context.packageManager
        val sequenceNumber = getSequenceNumber(context)
        Log.d(TAG, "sequenceNumber = $sequenceNumber")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val changedPackages = packageManagerApps.getChangedPackages(sequenceNumber)

            if (changedPackages != null) {
                // Packages are changed

                // Get the list of changed packages
                // the list includes new, updated and deleted apps
                val changedPackagesNames = changedPackages.packageNames

                var appName: CharSequence

                for (packageName in changedPackagesNames) {
                    try {
                        appName = packageManagerApps.getApplicationLabel(
                            packageManagerApps.getApplicationInfo(
                                packageName, 0,
                            )
                        )

                        // Either a new or an updated app
                        Log.d(
                            TAG,
                            "New Or Updated App: $packageName , appName = $appName"
                        )

                        logEventList.add(LogModel(logMsg ="New Or Updated App: $packageName , appName = $appName"))
                        logEvents.postValue(logEventList)
                    } catch (e: PackageManager.NameNotFoundException) {
                        // The app is deleted
                        Log.d(TAG, "Deleted App: $packageName")
                        logEventList.add(LogModel(logMsg = "Deleted App: $packageName"))
                        logEvents.postValue(logEventList)
                    }
                }
                saveSequenceNumber(context, changedPackages.sequenceNumber)
            } else {
                // packages not changed
            }
        }
    }

    private fun getSequenceNumber(context: Context): Int {
        val sharedPrefFile = context.getSharedPreferences("your_file_name", MODE_PRIVATE)
        return sharedPrefFile.getInt("sequence_number", 0)
    }

    private fun saveSequenceNumber(context: Context, newSequenceNumber: Int) {
        val sharedPrefFile = context.getSharedPreferences("your_file_name", MODE_PRIVATE)
        val editor = sharedPrefFile.edit()
        editor.putInt("sequence_number", newSequenceNumber)
        editor.apply()
    }

    companion object {
        val logEvents = MutableLiveData<List<LogModel>>()
        val logEventList = mutableListOf<LogModel>()

        private const val BROADCAST_REQUEST_CODE = 200

        const val CHANNEL_ID =
            "app_Background" // regular notification when apps start or start service(turn on) button push
        const val FOREGROUND_NOTIFICATION_ID = 1
        const val ACTION_START_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    }
}
