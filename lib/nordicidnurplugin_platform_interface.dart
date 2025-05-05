import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'nordicidnurplugin_method_channel.dart';

abstract class NordicidnurpluginPlatform extends PlatformInterface {
  /// Constructs a NordicidnurpluginPlatform.
  NordicidnurpluginPlatform() : super(token: _token);

  static final Object _token = Object();

  static NordicidnurpluginPlatform _instance = MethodChannelNordicidnurplugin();

  /// The default instance of [NordicidnurpluginPlatform] to use.
  ///
  /// Defaults to [MethodChannelNordicidnurplugin].
  static NordicidnurpluginPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [NordicidnurpluginPlatform] when
  /// they register themselves.
  static set instance(NordicidnurpluginPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
