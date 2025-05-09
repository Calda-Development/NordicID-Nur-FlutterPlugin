# NordicID Nur Flutter Plugin

![Supported_platforms](https://img.shields.io/badge/Supported_platforms-Android-blue)

![Tested_on](https://img.shields.io/badge/Tested_on-Brady_HH86_RFID/Barcode_Reader-blue)

## Description

`nordicidnurplugin` is a Flutter plugin that integrates with Brady HH86 scanner device. 
It supports basic features like connecting to the scanner device, scanning QR codes, 
scanning single RFID tag and scanning multiple RFID tags at once.

## Example

nordicidnurplugin_example demonstrates how to use the nordicidnurplugin plugin.

## API reference

### Instantiate the plugin
```
final _nordicIdNurPlugin = Nordicidnurplugin();
```
### Handling required permissions
The plugin contains all the necessary logic for requesting required permissions, 
you just have to call the corresponding functions.

```
final doesHaveRequiredPermissions = await _nordicIdNurPlugin.doesHaveRequiredPermissions();
```

Returns true if the app has required permissions, false otherwise.

To request actual permissions, call:

```
await _nordicIdNurPlugin.requestRequiredPermissions();
```

### Initialising the plugin

```
await _nordicIdNurPlugin.init();
```

### Discover devices to connect to

```
await _nordicIdNurPlugin.startDeviceRequest();
```

### Scan QR/barcode

```
await _nordicIdNurPlugin.scanBarcode();
```

### Scan single RFID

```
await _nordicIdNurPlugin.scanSingleRFID();
```

### Scan multiple RFID

```
await _nordicIdNurPlugin.scanMultipleRFID();
```

