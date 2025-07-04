import 'package:nordicidnurplugin/nordicidnurplugin.dart';
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

  NordicIDNurPluginCallback? callback;

  void setCallback(NordicIDNurPluginCallback callback) {
    this.callback = callback;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<bool> doesHaveRequiredPermissions() {
    throw UnimplementedError(
      'doesHaveRequiredPermissions() has not been implemented.',
    );
  }

  Future<bool> isInitialised() {
    throw UnimplementedError('isInitialised() has not been implemented.');
  }

  Future<void> requestRequiredPermissions() {
    throw UnimplementedError(
      'requestRequiredPermissions() has not been implemented.',
    );
  }

  Future<void> init({required bool autoConnect}) {
    throw UnimplementedError('init() has not been implemented.');
  }

  Future<bool> isConnected() {
    throw UnimplementedError('isConnected() has not been implemented.');
  }

  Future<void> startDeviceDiscovery() {
    throw UnimplementedError(
      'startDeviceDiscovery() has not been implemented.',
    );
  }

  Future<void> disconnect() {
    throw UnimplementedError('disconnect() has not been implemented.');
  }

  Future<void> scanBarcode({required int timeout}) {
    throw UnimplementedError('scanBarcode() has not been implemented.');
  }

  Future<void> scanSingleRFID({required int timeout}) {
    throw UnimplementedError('scanSingleRFID() has not been implemented.');
  }

  Future<void> setInventoryStreamMode() {
    throw UnimplementedError(
      'setInventoryStreamMode() has not been implemented.',
    );
  }

  Future<void> setRfidSetupTxLevel({required int txLevelValue}) {
    throw UnimplementedError(
      'setRfidSetupTxLevel() has not been implemented.',
    );
  }

  Future<int> getRfidSetupTxLevel() {
    throw UnimplementedError(
      'getRfidSetupTxLevel() has not been implemented.',
    );
  }

  Future<String> getReaderSerial() {
    throw UnimplementedError(
      'getReaderSerial() has not been implemented.',
    );
  }

  Future<String> getReaderAltSerial() {
    throw UnimplementedError(
      'getReaderAltSerial() has not been implemented.',
    );
  }
}
