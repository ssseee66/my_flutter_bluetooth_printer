
import 'package:flutter/services.dart';

import 'my_flutter_bluetooth_printer_platform_interface.dart';

class MyFlutterBluetoothPrinter {
  Future<String?> getPlatformVersion() {
    return MyFlutterBluetoothPrinterPlatform.instance.getPlatformVersion();
  }
  static const MethodChannel _channel =
  MethodChannel('my_bluetooth_printer');

  static Future<String?> Init() async {
    final String? code = await _channel.invokeMethod('init');
    return code;
  }
}
