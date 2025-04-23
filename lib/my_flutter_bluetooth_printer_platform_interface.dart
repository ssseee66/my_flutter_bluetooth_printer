import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'my_flutter_bluetooth_printer_method_channel.dart';

abstract class MyFlutterBluetoothPrinterPlatform extends PlatformInterface {
  /// Constructs a MyFlutterBluetoothPrinterPlatform.
  MyFlutterBluetoothPrinterPlatform() : super(token: _token);

  static final Object _token = Object();

  static MyFlutterBluetoothPrinterPlatform _instance = MethodChannelMyFlutterBluetoothPrinter();

  /// The default instance of [MyFlutterBluetoothPrinterPlatform] to use.
  ///
  /// Defaults to [MethodChannelMyFlutterBluetoothPrinter].
  static MyFlutterBluetoothPrinterPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [MyFlutterBluetoothPrinterPlatform] when
  /// they register themselves.
  static set instance(MyFlutterBluetoothPrinterPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
