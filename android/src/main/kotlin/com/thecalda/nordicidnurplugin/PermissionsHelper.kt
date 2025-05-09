package com.thecalda.nordicidnurplugin

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.app.Activity

object PermissionsHelper {

    private val APP_PERMISSION_REQ_CODE = 99

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    fun doesHaveRequiredPermissions(activity: Activity): Boolean {
        val permissionsNotGranted = permissions.any {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsNotGranted) {
            val shouldShowRationale = permissions.any {
                ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
            }

            if (!shouldShowRationale) {
                return true
            } else {
                return false
            }
        } else {
            return true
        }
    }

    fun requestPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(activity, permissions, APP_PERMISSION_REQ_CODE)
    }
}
