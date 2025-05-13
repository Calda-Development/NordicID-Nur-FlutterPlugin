import 'dart:async';

import 'package:flutter/material.dart';
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
  bool _isInitialised = false;
  bool _isConnected = false;

  bool _autoConnect = true;

  late NordicIDNurPlugin _nordicIDNurPlugin;

  List<String> _barcodeScans = [];
  String _scannedRFIDTag = "";

  bool _inventoryStreamActive = false;
  List<String> _inventoryStreamResults = [];

  @override
  void initState() {
    super.initState();

    _nordicIDNurPlugin = NordicIDNurPlugin(
      callback: NordicIDNurPluginCallback(
        onInitialised: (bool isInitialised) {
          print('onInitialised: $isInitialised');

          setState(() {
            _isInitialised = isInitialised;
          });
        },
        onConnected: () {
          setState(() {
            _isConnected = true;
          });
        },
        onDisconnected: () {
          setState(() {
            _isConnected = false;
          });
        },
        onBarcodeScanned: (String data) {
          print('onBarcodeScanned: $data');
          _barcodeScans.add(data);
          setState(() {});
        },
        onSingleRFIDScanned: (String data, RFIDScanError? error) {
          print(
            'onSingleRFIDScanned: $data, ${error?.message}, ${error?.numberOfTags}',
          );

          setState(() {
            if (data.isNotEmpty) {
              _scannedRFIDTag = data;
            } else {
              _scannedRFIDTag = "${error?.message}, ${error?.numberOfTags}";
            }
          });
        },
        onStartInventoryStream: () {
          print('onStartInventoryStream');
          setState(() {
            _inventoryStreamActive = true;
          });
        },
        onStopInventoryStream: () {
          print('onStopInventoryStream');
          setState(() {
            _inventoryStreamActive = false;
          });
        },
        onInventoryStreamEvent: (List<String> data) {
          print('onInventoryStreamEvent $data');
          setState(() {
            _inventoryStreamResults = data;
          });
        },
      ),
    );

    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion =
          await _nordicIDNurPlugin.getPlatformVersion() ??
          'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    bool doesHaveRequiredPermissions = false;
    doesHaveRequiredPermissions =
        await _nordicIDNurPlugin.doesHaveRequiredPermissions();
    bool isInitialised = await _nordicIDNurPlugin.isInitialised();
    print('isInitialised: $isInitialised');
    bool isConnected = await _nordicIDNurPlugin.isConnected();

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
      _doesHaveRequiredPermissions = doesHaveRequiredPermissions;
      _isInitialised = isInitialised;
      _isConnected = isConnected;
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
                _buildDivider('Permissions'),
                _buildPermissionsWidget(),
                SizedBox(height: 8),
                _buildDivider('Initialisation'),
                _buildInitWidget(),
                SizedBox(height: 8),
                _buildDivider('Barcode scan'),
                _buildBarcodeScanWidget(),
                SizedBox(height: 8),
                _buildDivider('Single RFID tag scan'),
                _buildSingleRFIDTagScanWidget(),
                SizedBox(height: 8),
                _buildDivider('Multiple RFID tags scan'),
                _buildInventoryStreamWidget(),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildPermissionsWidget() {
    return Column(
      children: [
        Text('Does have permissions: $_doesHaveRequiredPermissions'),
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            OutlinedButton(
              onPressed: () async {
                await _nordicIDNurPlugin.requestRequiredPermissions();
              },
              child: Text('Request Permissions'),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildInitWidget() {
    return Column(
      children: [
        Text('Is initialised: $_isInitialised'),
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text('Auto Connect?'),
            Checkbox(
              value: _autoConnect,
              onChanged: (value) {
                setState(() {
                  _autoConnect = value!;
                });
              },
            ),
            SizedBox(width: 8),
            OutlinedButton(
              onPressed: () async {
                await _nordicIDNurPlugin.init(autoConnect: _autoConnect);
              },
              child: Text('Init'),
            ),
          ],
        ),
        SizedBox(height: 8),
        Text('Is connected: $_isConnected'),
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            OutlinedButton(
              onPressed: () async {
                await _nordicIDNurPlugin.startDeviceDiscovery();
              },
              child: Text('Start device discovery'),
            ),
          ],
        ),
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            OutlinedButton(
              onPressed: () async {
                await _nordicIDNurPlugin.disconnect();
              },
              child: Text('Disconnect'),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildBarcodeScanWidget() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('1. Press and keep trigger button down. (Aiming started)'),
        Text('2. Aim to barcode'),
        Text('3. Release trigger for starting scan.'),
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            OutlinedButton(
              onPressed: () async {
                await _nordicIDNurPlugin.scanBarcode(timeout: 5000);
              },
              child: Text('Scan barcode mode'),
            ),
          ],
        ),
        Text('Scanned barcodes: ${_barcodeScans.length}'),
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            ..._barcodeScans
                .asMap()
                .map((index, value) => MapEntry(index, Text('#$index: $value')))
                .values,
          ],
        ),
        if (_barcodeScans.isNotEmpty)
          OutlinedButton(
            onPressed: () async {
              setState(() {
                _barcodeScans = [];
              });
            },
            child: Text('clean'),
          ),
      ],
    );
  }

  Widget _buildDivider(String s) {
    return Row(
      children: [
        Expanded(child: Divider()),
        SizedBox(width: 8),
        Text(s),
        SizedBox(width: 8),
        Expanded(child: Divider()),
      ],
    );
  }

  Widget _buildSingleRFIDTagScanWidget() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('Single tag must found before read accepted.'),
        Text('1. Press Trigger button for starting single tag read.'),
        Text('2. Point reader antenna near tag (~5cm)'),
        Text(
          '3. Reading stop automatically when single tag found or timeout (5 sec)',
        ),
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            OutlinedButton(
              onPressed: () async {
                await _nordicIDNurPlugin.scanSingleRFID(timeout: 5000);
              },
              child: Text('scanSingleRFID mode'),
            ),
          ],
        ),
        Text('Scanned RFID: $_scannedRFIDTag'),
        if (_scannedRFIDTag.isNotEmpty)
          OutlinedButton(
            onPressed: () async {
              setState(() {
                _scannedRFIDTag = "";
              });
            },
            child: Text('clean'),
          ),
      ],
    );
  }

  // onStartInventoryStream
  Widget _buildInventoryStreamWidget() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('1. Press and keep trigger button down. (Inventory started)'),
        Text('2. Release trigger to stop'),
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            OutlinedButton(
              onPressed: () async {
                await _nordicIDNurPlugin.setInventoryStreamMode();
              },
              child: Text('InventoryStream mode'),
            ),
          ],
        ),
        Text('InventoryStreamActive: $_inventoryStreamActive'),
        Text('Scanned barcodes: ${_inventoryStreamResults.length}'),
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            ..._inventoryStreamResults
                .asMap()
                .map((index, value) => MapEntry(index, Text('#$index: $value')))
                .values,
          ],
        ),
        if (_inventoryStreamResults.isNotEmpty)
          OutlinedButton(
            onPressed: () async {
              setState(() {
                _inventoryStreamResults = [];
              });
            },
            child: Text('clean'),
          ),
      ],
    );
  }
}
