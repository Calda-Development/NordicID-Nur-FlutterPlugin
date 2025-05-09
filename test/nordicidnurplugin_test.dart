import 'package:flutter_test/flutter_test.dart';
import 'package:nordicidnurplugin/nordicidnurplugin.dart';
import 'package:nordicidnurplugin/nordicidnurplugin_platform_interface.dart';
import 'package:nordicidnurplugin/nordicidnurplugin_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockNordicidnurpluginPlatform
    with MockPlatformInterfaceMixin
    implements NordicidnurpluginPlatform {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');

  @override
  Future<void> startDeviceRequest() {
    // TODO: implement startDeviceRequest
    throw UnimplementedError();
  }
}

void main() {
  final NordicidnurpluginPlatform initialPlatform =
      NordicidnurpluginPlatform.instance;

  test('$MethodChannelNordicidnurplugin is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelNordicidnurplugin>());
  });

  test('getPlatformVersion', () async {
    Nordicidnurplugin nordicidnurpluginPlugin = Nordicidnurplugin();
    MockNordicidnurpluginPlatform fakePlatform =
        MockNordicidnurpluginPlatform();
    NordicidnurpluginPlatform.instance = fakePlatform;

    expect(await nordicidnurpluginPlugin.getPlatformVersion(), '42');
  });
}
