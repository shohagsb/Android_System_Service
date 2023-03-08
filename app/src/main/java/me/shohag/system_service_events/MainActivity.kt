package me.shohag.system_service_events


import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import me.shohag.system_service_events.adapter.LogEventsAdapter
import me.shohag.system_service_events.databinding.ActivityMainBinding
import me.shohag.system_service_events.service.AppBackgroundService
import me.shohag.system_service_events.utils.Constants.ACTION_START_SERVICE
import me.shohag.system_service_events.utils.Constants.ACTION_STOP_SERVICE


class MainActivity : AppCompatActivity() {
    private lateinit var _binding: ActivityMainBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                when {
                    permissions.getOrDefault(Manifest.permission.POST_NOTIFICATIONS, false) -> {
                        // Precise location access granted.
//                            if (!isGranted) {
//                            }

                    }
                    permissions.getOrDefault(
                        Manifest.permission.PACKAGE_USAGE_STATS, false
                    ) -> {
                        // Only approximate location access granted.
                    }
                    else -> {
                        // No location access granted.
                        showExplanation(_binding.root)
                    }
                }

            }
            checkPermissions()
        }

        if (isMyServiceRunning(AppBackgroundService::class.java)) {
            visibleStopButton()
        } else {
            visibleStartButton()
        }

        _binding.buttonStartService.setOnClickListener {
            startBackgroundService()
            visibleStopButton()
        }

        _binding.buttonStopService.setOnClickListener {
            stopBackgroundService()
            visibleStartButton()
        }

        _binding.buttonClearLog.setOnClickListener {
            clearLogs()
        }
        showLogs()

    }

    private val adapter = LogEventsAdapter()

    private fun showLogs() {
        AppBackgroundService.logEvents.observe(this) {
            adapter.submitList(it)
            _binding.rvLog.adapter = adapter
        }
    }

    private fun clearLogs() {
        AppBackgroundService.logEventList.clear()
        AppBackgroundService.logEvents.postValue(AppBackgroundService.logEventList)
    }

    private fun visibleStartButton() {
        _binding.buttonStartService.visibility = View.VISIBLE
        _binding.buttonStopService.visibility = View.GONE
    }


    private fun visibleStopButton() {
        _binding.buttonStartService.visibility = View.GONE
        _binding.buttonStopService.visibility = View.VISIBLE
    }


    private fun startBackgroundService() {
        startService(Intent(
            this@MainActivity, AppBackgroundService::class.java
        ).apply {
            this.action = ACTION_START_SERVICE
        })
    }

    private fun stopBackgroundService() {
        startService(Intent(
            this@MainActivity, AppBackgroundService::class.java
        ).apply {
            this.action = ACTION_STOP_SERVICE
        })

    }


    private fun isMyServiceRunning(serviceClass: Class<AppBackgroundService>): Boolean {
        val manager: ActivityManager = getSystemService(
            Context.ACTIVITY_SERVICE
        ) as ActivityManager

        for (service: ActivityManager.RunningServiceInfo in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name.equals(service.service.className)) {
                return true
            }
        }

        return false
    }

    /**
     * Starts the permission check
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissions() {
        if (!isPermissionApproved()) {
            requestNotificationPermissions()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun isPermissionApproved(): Boolean {
        return ContextCompat.checkSelfPermission(
            this@MainActivity, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this@MainActivity, Manifest.permission.PACKAGE_USAGE_STATS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isPackageUsagePermissionApproved(): Boolean {
        return ContextCompat.checkSelfPermission(
            this@MainActivity, Manifest.permission.PACKAGE_USAGE_STATS
        ) == PackageManager.PERMISSION_GRANTED
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermissions() {
        if (isPermissionApproved()) return
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.PACKAGE_USAGE_STATS
            )
        )
    }

    private fun showExplanation(view: View) {
        val snackBar = Snackbar.make(
            view, R.string.notification_permission_explanation, Snackbar.LENGTH_LONG
        ).setAction(R.string.settings) {
            startActivity(Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(Color.WHITE)
        val textView =
            snackBarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        textView.textSize = 12f
        snackBar.show()
    }
}