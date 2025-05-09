package com.thecalda.nordicidnurplugin

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nordicid.nuraccessory.*
import com.nordicid.nurapi.*
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry

// import io.flutter.plugin.common.MethodChannel.Result;

/** NordicidnurpluginPlugin */
class NordicidnurpluginPlugin: FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var activity: Activity

  private val APP_PERMISSION_REQ_CODE = 99

  private val CHANNEL: String = "nordicidnurplugin"
  private val CHANNEL_startDeviceRequest: String = "startDeviceRequest"
  private val CHANNEL_scanBarcode: String = "scanBarcode"
  private val CHANNEL_scanSingleRFID: String = "scanSingleRFID"
  private val CHANNEL_scanMultipleRFID: String = "scanMultipleRFID"


  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, CHANNEL)
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    if (call.method == "getPlatformVersion") {
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
    } else if (call.method == CHANNEL_startDeviceRequest) {

      /** Bluetooth Permission checks  */
      if (ContextCompat.checkSelfPermission(
          activity,
          Manifest.permission.ACCESS_COARSE_LOCATION
        ) !== PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
          activity,
          Manifest.permission.ACCESS_FINE_LOCATION
        ) !== PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
          activity,
          Manifest.permission.BLUETOOTH_ADMIN
        ) !== PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
          activity,
          Manifest.permission.BLUETOOTH_ADVERTISE
        ) !== PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
          activity,
          Manifest.permission.BLUETOOTH_CONNECT
        ) !== PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
          activity,
          Manifest.permission.BLUETOOTH_SCAN
        ) !== PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
          activity,
          Manifest.permission.READ_EXTERNAL_STORAGE
        ) !== PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
          activity,
          Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) !== PackageManager.PERMISSION_GRANTED
      ) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION) ||
          ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION) ||
          ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.BLUETOOTH_ADMIN) ||
          ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.BLUETOOTH_ADVERTISE) ||
          ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.BLUETOOTH_CONNECT) ||
          ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.BLUETOOTH_SCAN) ||
          ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE) ||
          ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ) {
        } else {
          ActivityCompat.requestPermissions(
            activity, arrayOf<String?>(
              Manifest.permission.ACCESS_COARSE_LOCATION,
              Manifest.permission.ACCESS_FINE_LOCATION,
              Manifest.permission.BLUETOOTH_ADMIN,
              Manifest.permission.BLUETOOTH_ADVERTISE,
              Manifest.permission.BLUETOOTH_CONNECT,
              Manifest.permission.BLUETOOTH_SCAN,
              Manifest.permission.READ_EXTERNAL_STORAGE,
              Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            APP_PERMISSION_REQ_CODE
          )
        }
      }

      NurHelper.init(activity);

      result.success(true)
    } else if (call.method == CHANNEL_scanBarcode ) {

      NurHelper.scanBarcode()

      result.success(true)
    } else if (call.method == CHANNEL_scanSingleRFID ) {

      NurHelper.scanSingleRFID()

      result.success(true)
    } else if (call.method == CHANNEL_scanMultipleRFID ) {

      NurHelper.scanMultipleRFID()

      result.success(true)
    } else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    this.activity = binding.activity;
    binding.addActivityResultListener(this);
  }

  override fun onDetachedFromActivityForConfigChanges() {
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    this.activity = binding.activity;
    binding.addActivityResultListener(this);
  }

  override fun onDetachedFromActivity() {
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
    NurHelper.onActivityResult(requestCode, resultCode, data)
    return true
  }
}
