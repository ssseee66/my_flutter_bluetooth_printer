import 'package:flutter_test/flutter_test.dart';
import 'package:my_flutter_bluetooth_printer/my_flutter_bluetooth_printer.dart';
import 'package:my_flutter_bluetooth_printer/my_flutter_bluetooth_printer_platform_interface.dart';
import 'package:my_flutter_bluetooth_printer/my_flutter_bluetooth_printer_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockMyFlutterBluetoothPrinterPlatform
    with MockPlatformInterfaceMixin
    implements MyFlutterBluetoothPrinterPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final MyFlutterBluetoothPrinterPlatform initialPlatform = MyFlutterBluetoothPrinterPlatform.instance;

  test('$MethodChannelMyFlutterBluetoothPrinter is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelMyFlutterBluetoothPrinter>());
  });

  test('getPlatformVersion', () async {
    MyFlutterBluetoothPrinter myFlutterBluetoothPrinterPlugin = MyFlutterBluetoothPrinter();
    MockMyFlutterBluetoothPrinterPlatform fakePlatform = MockMyFlutterBluetoothPrinterPlatform();
    MyFlutterBluetoothPrinterPlatform.instance = fakePlatform;

    expect(await myFlutterBluetoothPrinterPlugin.getPlatformVersion(), '42');
  });
}
