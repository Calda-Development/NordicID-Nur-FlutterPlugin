package com.thecalda.nordicidnurplugin

import android.app.Activity
import android.content.Intent
import com.nordicid.nuraccessory.*
import com.nordicid.nurapi.*
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.PluginRegistry

// import io.flutter.plugin.common.MethodChannel.Result

/** NordicidnurpluginPlugin */
/// https://stackoverflow.com/questions/56852851/how-do-i-best-give-multiple-arguments-with-the-java-version-of-flutters-methodch

class NordicidnurpluginPlugin: FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var activity: Activity


  private val CHANNEL: String = "nordicidnurplugin"

  private val CHANNEL_doesHaveRequiredPermissions: String = "doesHaveRequiredPermissions"
  private val CHANNEL_requestRequiredPermissions: String = "requestRequiredPermissions"
  private val CHANNEL_init: String = "init"
  private val CHANNEL_isInitialised: String = "isInitialised"
  private val CHANNEL_isConnected: String = "isConnected"
  private val CHANNEL_startDeviceDiscovery: String = "startDeviceDiscovery"
  private val CHANNEL_disconnect: String = "disconnect"
  private val CHANNEL_scanBarcode: String = "scanBarcode"
  private val CHANNEL_scanSingleRFID: String = "scanSingleRFID"
  private val CHANNEL_setInventoryStreamMode: String = "setInventoryStreamMode"
  private val CHANNEL_setRfidSetupTxLevel: String = "setRfidSetupTxLevel"
  private val CHANNEL_getRfidSetupTxLevel: String = "getRfidSetupTxLevel"

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, CHANNEL)
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    if (call.method == "getPlatformVersion") {
      result.success("Android ${android.os.Build.VERSION.RELEASE}")

    } else if (call.method == CHANNEL_doesHaveRequiredPermissions) {
      val doesHaveRequiredPermissions: Boolean = PermissionsHelper.doesHaveRequiredPermissions(activity)
      result.success(doesHaveRequiredPermissions)

    } else if (call.method == CHANNEL_requestRequiredPermissions) {
      PermissionsHelper.requestPermissions(activity)
      result.success(true)

    } else if (call.method == CHANNEL_init) {

      val autoConnect: Boolean = call.argument("autoConnect") ?: false

      NurHelper.init(activity, channel, autoConnect)
      result.success(true)

    } else if (call.method == CHANNEL_isInitialised) {
      val isInitialised: Boolean = NurHelper.isInitialised()
      result.success(isInitialised)

    } else if (call.method == CHANNEL_isConnected) {
      val isConnected: Boolean = NurHelper.isConnected()
      result.success(isConnected)

    } else if (call.method == CHANNEL_startDeviceDiscovery) {

      NurHelper.startDeviceDiscovery(activity)
      result.success(true)

    } else if (call.method == CHANNEL_disconnect) {

      NurHelper.disconnect()
      result.success(true)

    } else if (call.method == CHANNEL_scanBarcode ) {

      val timeout: Int = call.argument("timeout") ?: 5000

      NurHelper.scanBarcode(timeout)

      result.success(true)
    } else if (call.method == CHANNEL_scanSingleRFID ) {

      val timeout: Int = call.argument("timeout") ?: 5000

      NurHelper.scanSingleRFID(timeout)

      result.success(true)
    } else if (call.method == CHANNEL_setInventoryStreamMode ) {

      NurHelper.setInventoryStreamMode()

      result.success(true)
    } else if (call.method == CHANNEL_setRfidSetupTxLevel ) {
      val txLevelValue: Int = call.argument("txLevelValue") ?: 7

      NurHelper.setRfidSetupTxLevel(txLevelValue);

      result.success(true)
    } else if (call.method == CHANNEL_getRfidSetupTxLevel ) {
      val rfidSetupTxLevel: Int = NurHelper.getRfidSetupTxLevel();

      result.success(rfidSetupTxLevel)
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
