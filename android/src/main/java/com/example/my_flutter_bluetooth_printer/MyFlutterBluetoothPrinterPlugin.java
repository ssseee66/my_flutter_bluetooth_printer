package com.example.my_flutter_bluetooth_printer;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BasicMessageChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.StandardMessageCodec;

/** MyBluetoothPrinterPlugin */
public class MyFlutterBluetoothPrinterPlugin implements FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  //  程序上下文
  private Context applicationContext;
  //  flutter于原生Android端通信通道名称
  private static final String FLUTTER_TO_ANDROID_CHANNEL = "flutter_printer_android";
  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    applicationContext = flutterPluginBinding.getApplicationContext();

    BasicMessageChannel<Object> flutter_channel = new BasicMessageChannel<>(
            flutterPluginBinding.getBinaryMessenger(),
            FLUTTER_TO_ANDROID_CHANNEL,
            StandardMessageCodec.INSTANCE
    );
    //  为通信通道实例对象设置监听方法，使其能够监听到来之flutter端的信息
    flutter_channel.setMessageHandler((message, reply) -> {
      Map<String, Object> channelMessage = castMap(message, String.class, Object.class);
      if (channelMessage == null) return;
      if (channelMessage.containsKey("channelName")) {   //  当flutter端请求创建蓝牙相关监听实例
        //  获取flutter端发送过来的通信通道名称
        String channel_name = (String) channelMessage.get("channelName");
        if (channel_name == null) return;
        Log.e("channelName", channel_name);
        new MyListener(     //  创建新的蓝牙事件监听类实例
                channel_name,
                applicationContext,
                flutterPluginBinding.getBinaryMessenger());
      }
    });
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else {
      result.notImplemented();
    }
  }
  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  public static <K, V> Map<K, V> castMap(Object obj, Class<K> key, Class<V> value) {
        /*
        对于对象转换为Map类型作出检查
        */
    Map<K, V> map = new HashMap<>();
    if (obj instanceof Map<?, ?>) {
      for (Map.Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
        map.put(key.cast(entry.getKey()), value.cast(entry.getValue()));
      }
      return map;
    }
    return null;
  }
}
