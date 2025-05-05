
import 'nordicidnurplugin_platform_interface.dart';

class Nordicidnurplugin {
  Future<String?> getPlatformVersion() {
    return NordicidnurpluginPlatform.instance.getPlatformVersion();
  }
}
