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
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
