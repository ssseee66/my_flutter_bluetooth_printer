import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'my_flutter_bluetooth_printer_platform_interface.dart';

/// An implementation of [MyFlutterBluetoothPrinterPlatform] that uses method channels.
class MethodChannelMyFlutterBluetoothPrinter extends MyFlutterBluetoothPrinterPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('my_flutter_bluetooth_printer');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
