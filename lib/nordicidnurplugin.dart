import 'nordicidnurplugin_platform_interface.dart';

class Nordicidnurplugin {
  Future<String?> getPlatformVersion() {
    return NordicidnurpluginPlatform.instance.getPlatformVersion();
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

  // NurDeviceListActivity.startDeviceRequest(MainActivity.this, mNurApi);
}
