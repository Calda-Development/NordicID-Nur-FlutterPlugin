import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'nordicidnurplugin_platform_interface.dart';

/// An implementation of [NordicidnurpluginPlatform] that uses method channels.
class MethodChannelNordicidnurplugin extends NordicidnurpluginPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('nordicidnurplugin');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>(
      'getPlatformVersion',
    );
    return version;
  }

  @override
  Future<void> startDeviceRequest() async {
    await methodChannel.invokeMethod<void>('startDeviceRequest');
  }

  @override
  Future<void> scanBarcode() async {
    await methodChannel.invokeMethod<void>('scanBarcode');
  }

  @override
  Future<void> scanSingleRFID() async {
    await methodChannel.invokeMethod<void>('scanSingleRFID');
  }

  @override
  Future<void> scanMultipleRFID() async {
    await methodChannel.invokeMethod<void>('scanMultipleRFID');
  }
}
