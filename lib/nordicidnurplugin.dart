import 'nordicidnurplugin_platform_interface.dart';

class NordicIDNurPlugin {
  NordicIDNurPlugin({required NordicIDNurPluginCallback callback}) {
    NordicidnurpluginPlatform.instance.setCallback(callback);
  }

  Future<String?> getPlatformVersion() {
    return NordicidnurpluginPlatform.instance.getPlatformVersion();
  }

  Future<bool> doesHaveRequiredPermissions() {
    return NordicidnurpluginPlatform.instance.doesHaveRequiredPermissions();
  }

  Future<bool> isInitialised() {
    return NordicidnurpluginPlatform.instance.isInitialised();
  }

  Future<void> requestRequiredPermissions() {
    return NordicidnurpluginPlatform.instance.requestRequiredPermissions();
  }

  Future<void> init({required bool autoConnect}) {
    return NordicidnurpluginPlatform.instance.init(autoConnect: autoConnect);
  }

  Future<bool> isConnected() {
    return NordicidnurpluginPlatform.instance.isConnected();
  }

  Future<void> startDeviceDiscovery() {
    return NordicidnurpluginPlatform.instance.startDeviceDiscovery();
  }

  Future<void> disconnect() {
    return NordicidnurpluginPlatform.instance.disconnect();
  }

  Future<void> scanBarcode({required int timeout}) {
    return NordicidnurpluginPlatform.instance.scanBarcode(timeout: timeout);
  }

  Future<void> scanSingleRFID({required int timeout}) {
    return NordicidnurpluginPlatform.instance.scanSingleRFID(timeout: timeout);
  }

  Future<void> setInventoryStreamMode() {
    return NordicidnurpluginPlatform.instance.setInventoryStreamMode();
  }

  Future<void> setRfidSetupTxLevel({required int txLevelValue}) {
    return NordicidnurpluginPlatform.instance
        .setRfidSetupTxLevel(txLevelValue: txLevelValue);
  }

  Future<int> getRfidSetupTxLevel() {
    return NordicidnurpluginPlatform.instance.getRfidSetupTxLevel();
  }

  Future<String> getReaderSerial() {
    return NordicidnurpluginPlatform.instance.getReaderSerial();
  }

  Future<String> getReaderAltSerial() {
    return NordicidnurpluginPlatform.instance.getReaderAltSerial();
  }
}

class NordicIDNurPluginCallback {
  void Function(bool isInitialised) onInitialised;
  void Function() onConnected;
  void Function() onDisconnected;
  void Function(String data) onBarcodeScanned;
  void Function(String data, RFIDScanError? error) onSingleRFIDScanned;
  void Function() onStartSingleRFIDScan;
  void Function() onStopSingleRFIDScan;
  void Function() onStartInventoryStream;
  void Function() onStopInventoryStream;
  void Function(List<String> data) onInventoryStreamEvent;

  NordicIDNurPluginCallback({
    required this.onInitialised,
    required this.onConnected,
    required this.onDisconnected,
    required this.onBarcodeScanned,
    required this.onSingleRFIDScanned,
    required this.onStartSingleRFIDScan,
    required this.onStopSingleRFIDScan,
    required this.onStartInventoryStream,
    required this.onStopInventoryStream,
    required this.onInventoryStreamEvent,
  });
}

class RFIDScanError {
  String message;
  int numberOfTags;

  RFIDScanError({required this.message, this.numberOfTags = 0});
}
