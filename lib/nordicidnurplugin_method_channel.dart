import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:nordicidnurplugin/nordicidnurplugin.dart';

import 'nordicidnurplugin_platform_interface.dart';

/// An implementation of [NordicidnurpluginPlatform] that uses method channels.
class MethodChannelNordicidnurplugin extends NordicidnurpluginPlatform {
  final METHOD_onInitialised = "onInitialised";
  final METHOD_onConnected = "onConnected";
  final METHOD_onDisconnected = "onDisconnected";
  final METHOD_onBarcodeScanned = "onBarcodeScanned";

  final METHOD_onSingleRFIDScanned = "onSingleRFIDScanned";
  final METHOD_onStartSingleRFIDScan = "onStartSingleRFIDScan";
  final METHOD_onStopSingleRFIDScan = "onStopSingleRFIDScan";

  final METHOD_onStartInventoryStream = "onStartInventoryStream";
  final METHOD_onStopInventoryStream = "onStopInventoryStream";
  final METHOD_onInventoryStreamEvent = "onInventoryStreamEvent";

  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('nordicidnurplugin');

  MethodChannelNordicidnurplugin() {
    methodChannel.setMethodCallHandler((call) async {
      debugPrint(
        'MethodChannelNordicidnurplugin.methodChannel: ${call.method}, args: ${call.arguments}',
      );

      if (call.method == METHOD_onInitialised) {
        callback?.onInitialised(call.arguments);
      } else if (call.method == METHOD_onConnected) {
        callback?.onConnected();
      } else if (call.method == METHOD_onDisconnected) {
        callback?.onDisconnected();
      } else if (call.method == METHOD_onBarcodeScanned) {
        String data = call.arguments['data'];
        callback?.onBarcodeScanned(data);
      } else if (call.method == METHOD_onSingleRFIDScanned) {
        String data = call.arguments['data'];
        String? errorMessage = call.arguments['errorMessage'];
        int? errorNumberOfTags = call.arguments['errorNumberOfTags'];

        RFIDScanError? error;
        if (errorMessage != null) {
          error = RFIDScanError(
            message: errorMessage,
            numberOfTags: errorNumberOfTags ?? 0,
          );
        }

        callback?.onSingleRFIDScanned(data, error);
      } else if (call.method == METHOD_onStartSingleRFIDScan) {
        callback?.onStartSingleRFIDScan();
      } else if (call.method == METHOD_onStopSingleRFIDScan) {
        callback?.onStopSingleRFIDScan();
      } else if (call.method == METHOD_onStartInventoryStream) {
        callback?.onStartInventoryStream();
      } else if (call.method == METHOD_onStopInventoryStream) {
        callback?.onStopInventoryStream();
      } else if (call.method == METHOD_onInventoryStreamEvent) {
        List<String> epcList = List<String>.from(
          jsonDecode(call.arguments['data']),
        );

        callback?.onInventoryStreamEvent(epcList);
      } else {
        throw UnimplementedError('${call.method} has not been implemented.');
      }
    });
  }

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>(
      'getPlatformVersion',
    );
    return version;
  }

  @override
  Future<void> startDeviceDiscovery() async {
    await methodChannel.invokeMethod<void>('startDeviceDiscovery');
  }

  @override
  Future<void> disconnect() async {
    await methodChannel.invokeMethod<void>('disconnect');
  }

  @override
  Future<bool> doesHaveRequiredPermissions() async {
    final result = await methodChannel.invokeMethod<bool>(
      'doesHaveRequiredPermissions',
    );
    return result ?? false;
  }

  @override
  Future<bool> isInitialised() async {
    final result = await methodChannel.invokeMethod<bool>('isInitialised');
    return result ?? false;
  }

  @override
  Future<void> init({required bool autoConnect}) async {
    await methodChannel.invokeMethod<void>('init', <String, dynamic>{
      'autoConnect': autoConnect,
    });
  }

  @override
  Future<bool> isConnected() async {
    final result = await methodChannel.invokeMethod<bool>('isConnected');
    return result ?? false;
  }

  @override
  Future<void> requestRequiredPermissions() async {
    await methodChannel.invokeMethod<void>('requestRequiredPermissions');
  }

  @override
  Future<void> scanBarcode({required int timeout}) async {
    await methodChannel.invokeMethod<void>('scanBarcode', <String, dynamic>{
      'timeout': timeout,
    });
  }

  @override
  Future<void> scanSingleRFID({required int timeout}) async {
    await methodChannel.invokeMethod<void>('scanSingleRFID', <String, dynamic>{
      'timeout': timeout,
    });
  }

  @override
  Future<void> setInventoryStreamMode() async {
    await methodChannel.invokeMethod<void>('setInventoryStreamMode');
  }
}
