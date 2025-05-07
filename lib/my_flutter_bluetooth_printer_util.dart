import 'dart:async';

import 'package:flutter/services.dart';
import 'package:my_flutter_bluetooth_printer/printer_failed_code.dart';
import 'package:my_flutter_bluetooth_printer/printer_operation_code.dart';

class MyFlutterBluetoothPrinterUtil {
  MyFlutterBluetoothPrinterUtil._();

  factory MyFlutterBluetoothPrinterUtil() => _instance;
  static final MyFlutterBluetoothPrinterUtil _instance = MyFlutterBluetoothPrinterUtil._();

  String messageChannelName = "";
  BasicMessageChannel flutterChannel = const BasicMessageChannel("my_flutter_bluetooth_printer", StandardMessageCodec());
  BasicMessageChannel messageChannel = const BasicMessageChannel("null", StandardMessageCodec());

  void sendMessageToAndroid(String methodName, dynamic arg) async {
    messageChannel.send({methodName: arg});
  }

  void sendChannelName(String methodName, dynamic channelName) async {
    flutterChannel.send({methodName: channelName});
  }

  void setMessageChannel(String channelName, Future<dynamic> Function(dynamic message) handler) {
    messageChannel = BasicMessageChannel(channelName, const StandardMessageCodec());
    messageChannel.setMessageHandler(handler);
    }
  void startScan() {
    messageChannel.send({"startScan": true});
  }
  void stopScan() {
    messageChannel.send({"stopScan": true});
  }
  void connect(String address) {
    messageChannel.send({"startConnect": address});
  }
  void closeConnect() {
    messageChannel.send({"closeConnect": true});
  }
  void destroy() {
    messageChannel.send({"destroy": true});
  }
  void send(List<String> printData) {
    messageChannel.send({"startSend": printData});
  }
  String? getTurnCodeInfo(int code) {
    if (printerTurnCodeMap.isNotEmpty) {
      if (printerTurnCodeMap.containsKey(code)) {
        return printerTurnCodeMap[code];
      }
    }
    return null;
  }
  Enum? getPrinterFailedCode(int code) {
    switch (code) {
      case 0:
        return PrinterFailedCode.PERMISSION_DENIED;
      case 1:
        return PrinterFailedCode.SCAN_FAILED;
      case 2:
        return PrinterFailedCode.PRINTER_NOT_CONNECTED;
      case 3:
        return PrinterFailedCode.NOT_SUPPORT_BLUETOOTH;
      case 4:
        return PrinterFailedCode.BLUETOOTH_NOT_TURN_ON;
      default:
        return null;
    }
  }
  Enum? getPrinterOperationCode(int code) {
    switch (code) {
      case 0:
        return PrinterOperationCode.SCAN;
      case 1:
        return PrinterOperationCode.STOP_SCAN;
      case 2:
        return PrinterOperationCode.CONNECT;
      case 3:
        return PrinterOperationCode.CLOSE_CONNECT;
      case 4:
        return PrinterOperationCode.SEND;
      default:
        return PrinterOperationCode.ERROR_CODE;
    }
  }
  static const Map<int, String> printerTurnCodeMap = {
    // 初始化参数错误
    1 : "The initialization parameters are incorrect",
    // 断开设备连接
    4 : "Disconnect the device connection",
    // 蓝牙BLe连接成功
    256 : "The Bluetooth BLe connection was successful",
    // 蓝牙SPP连接成功
    257 : "The Bluetooth SPP connection was successful",
    // USB连接成功
    258 : "USB connection successful",
    // 未支持的连接类型
    512 : "Unsupported connection types",
    // 连接中进行重复连接
    513 : "Repeat the connection during the connection",
    // 已连接未断开进行重复连接
    514 : "Reconnect if the connection is already connected but not disconnected",
    // 参数输入错误
    515 : "Parameter input error",
    // 权限不足以完成后续流程
    516 : "The permission is insufficient to complete the subsequent process",
    // 手机系统错误
    517 : "Mobile phone system error",
    // 蓝牙SPP配对失败
    518 : "Bluetooth SPP pairing failed",
    // 蓝牙BLE服务匹配失败
    519 : "The Bluetooth BLE service matching failed",
  };

}