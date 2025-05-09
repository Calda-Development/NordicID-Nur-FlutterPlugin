import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:nordicidnurplugin/nordicidnurplugin.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  bool _doesHaveRequiredPermissions = false;
  final _nordicidnurpluginPlugin = Nordicidnurplugin();

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion =
          await _nordicidnurpluginPlugin.getPlatformVersion() ??
          'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    bool doesHaveRequiredPermissions = false;
    doesHaveRequiredPermissions =
        await _nordicidnurpluginPlugin.doesHaveRequiredPermissions();

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
      _doesHaveRequiredPermissions = doesHaveRequiredPermissions;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('Plugin example app')),
        body: SingleChildScrollView(
          child: Padding(
            padding: const EdgeInsets.all(8.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Running on: $_platformVersion'),
                SizedBox(height: 8),
                Text('Does have permissions: $_doesHaveRequiredPermissions'),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    OutlinedButton(
                      onPressed: () async {
                        await _nordicidnurpluginPlugin
                            .requestRequiredPermissions();
                      },
                      child: Text('Request Permissions'),
                    ),
                  ],
                ),
                SizedBox(height: 8),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    OutlinedButton(
                      onPressed: () async {
                        await _nordicidnurpluginPlugin.init();
                      },
                      child: Text('Init'),
                    ),
                  ],
                ),
                SizedBox(height: 8),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    OutlinedButton(
                      onPressed: () async {
                        await _nordicidnurpluginPlugin.startDeviceRequest();
                      },
                      child: Text('startDeviceRequest'),
                    ),
                  ],
                ),
                SizedBox(height: 16),
                Text('1. Press and keep trigger button down. (Aiming started)'),
                Text('2. Aim to barcode'),
                Text('3. Release trigger for starting scan.'),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    OutlinedButton(
                      onPressed: () async {
                        await _nordicidnurpluginPlugin.scanBarcode();
                      },
                      child: Text('scanBarcode'),
                    ),
                  ],
                ),
                SizedBox(height: 16),
                Text('Single tag must found before read accepted.'),
                Text('Press Trigger button for starting single tag read.'),
                Text('Point reader antenna near tag (~5cm)'),
                Text(
                  'Reading stop automatically when single tag found or timeout (7 sec)',
                ),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    OutlinedButton(
                      onPressed: () async {
                        await _nordicidnurpluginPlugin.scanSingleRFID();
                      },
                      child: Text('scanSingleRFID'),
                    ),
                  ],
                ),
                SizedBox(height: 16),
                Text(
                  '1. Press and keep trigger button down. (Inventory started)',
                ),
                Text('2. Release trigger to stop'),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    OutlinedButton(
                      onPressed: () async {
                        await _nordicidnurpluginPlugin.scanMultipleRFID();
                      },
                      child: Text('scanMultipleRFID'),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
