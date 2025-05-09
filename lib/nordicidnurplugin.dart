import 'nordicidnurplugin_platform_interface.dart';

class Nordicidnurplugin {
  Future<String?> getPlatformVersion() {
    return NordicidnurpluginPlatform.instance.getPlatformVersion();
  }

  Future<bool> doesHaveRequiredPermissions() {
    return NordicidnurpluginPlatform.instance.doesHaveRequiredPermissions();
  }

  Future<void> requestRequiredPermissions() {
    return NordicidnurpluginPlatform.instance.requestRequiredPermissions();
  }

  Future<void> init() {
    return NordicidnurpluginPlatform.instance.init();
  }

  Future<void> startDeviceRequest() {
    return NordicidnurpluginPlatform.instance.startDeviceRequest();
  }

  Future<void> scanBarcode() {
    return NordicidnurpluginPlatform.instance.scanBarcode();
  }

  Future<void> scanSingleRFID() {
    return NordicidnurpluginPlatform.instance.scanSingleRFID();
  }

  Future<void> scanMultipleRFID() {
    return NordicidnurpluginPlatform.instance.scanMultipleRFID();
  }
}
