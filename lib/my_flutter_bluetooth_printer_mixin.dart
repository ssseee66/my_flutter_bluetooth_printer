import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:my_flutter_bluetooth_printer/my_flutter_bluetooth_printer_util.dart';

import 'my_flutter_bluetooth_printer.dart';

mixin MyFlutterBluetoothPrinterMixin<T extends StatefulWidget> on State<T> {
  late StreamSubscription streamSubscription;
  final MyFlutterBluetoothPrinterUtil util = MyFlutterBluetoothPrinterUtil();

  @override
  void initState() {
    super.initState();
    util.flutterChannel.setMessageHandler(setMessageChannelHandle);
    util.setMessageChannel(hashCode.toString(), listenerBluetoothPrinterAndroidHandle);
    util.sendChannelName("channelName", hashCode.toString());
  }
  @override
  void dispose() {
    super.dispose();
    util.messageChannel.setMessageHandler(null);
    util.flutterChannel.setMessageHandler(null);
  }

  Future<void> setMessageChannelHandle(dynamic message);
  Future<void> listenerBluetoothPrinterAndroidHandle(dynamic message);

}