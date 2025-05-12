package com.example.my_flutter_bluetooth_printer;


import android.Manifest;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.ctaiot.ctprinter.ctpl.CTPL;
import com.ctaiot.ctprinter.ctpl.Device;
import com.ctaiot.ctprinter.ctpl.RespCallback;
import com.ctaiot.ctprinter.ctpl.param.PrintMode;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import io.flutter.plugin.common.BasicMessageChannel;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.StandardMessageCodec;

public class MyListener {
    private BasicMessageChannel<Object> message_channel;
    private final Map<String, Object> message_map = new HashMap<>();
    private Map<String, Object> arguments;
    private final Map<String, Consumer<String>> action_map = new HashMap<>();
    private final List<BluetoothDevice> deviceList = new ArrayList<>();
    private final Map<String, String> deviceMessageList = new HashMap<>();
    private final Context applicationContext;
    private boolean CONNECTED = false;
    private final BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
    private final BluetoothLeScanner leScanner = defaultAdapter.getBluetoothLeScanner();
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            if (!checkPermission()) {
                // 权限不足，请检查相关权限并授予
                message_map.clear();
                message_map.put("message", "Insufficient permissions. " +
                        "Please check the relevant permissions and grant them");
                message_map.put("isSuccessful", false);
                message_map.put("failedCode", 0);
                message_map.put("operationCode", 0);
                message_channel.send(message_map);
                return;
            }
            if (device.getType() != BluetoothDevice.DEVICE_TYPE_LE &&
                    device.getType() != BluetoothDevice.DEVICE_TYPE_DUAL)
                return;
            if (deviceList.contains(device)) return;
            if (device.getName().isEmpty()) return;
            Log.i("scanResult", device.getName() + ":" + device.getAddress());
            deviceList.add(device);
            deviceMessageList.put(device.getName(), device.getAddress());
            message_map.clear();
            message_map.put("message", deviceMessageList);
            message_map.put("isSuccessful", true);
            message_map.put("operationCode", 0);
            message_channel.send(message_map);    //  将蓝牙设备信息（蓝牙名称和蓝牙MAC地址）发送给flutter端
        }
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e("scanInfo", errorCode + "");
            message_map.clear();
            message_map.put("message", "Scan failed");
            message_map.put("isSuccessful", false);
            message_map.put("failedCode", 1);
            message_map.put("operationCode", 0);
            message_channel.send(message_map);
        }
    };
    private RespCallback respCallback = new RespCallback() {
        @Override
        public void onConnectRespsonse(int code_1, int code_2) {
            Log.e("connectResponseInfo", "Postback data: <" + code_1 + ">, <" + code_2 + ">");
            int operationCode;
            boolean isSuccessful = false;
            if (code_2 == 257 || code_2 == 256 || code_2 == 258 || code_2 == 513 || code_2 == 514) {
                // 连接成功
                operationCode = 2;
                CONNECTED = true;
                isSuccessful = true;
            } else if (code_2 == 4) {
                // 断开连接
                operationCode = 3;
                CONNECTED = false;
                isSuccessful = true;
            } else {
                // 连接失败
                operationCode = 2;
            }
            message_map.clear();
            message_map.put("printerTurnCode", code_2);
            message_map.put("operationCode", operationCode);
            message_map.put("isSuccessful", isSuccessful);
            message_channel.send(message_map);
        }

        @Override
        public void onDataResponse(HashMap<String, String> hashMap) {
            Log.i("onDataResponse", hashMap.toString());
        }

        @Override
        public boolean autoSPPBond() {
            return false;
        }
    };

    MyListener(String channelName, Context applicationContext, BinaryMessenger binaryMessenger) {
        message_channel = new BasicMessageChannel<>(   //  实例化通信通道对象
                binaryMessenger,
                channelName,
                StandardMessageCodec.INSTANCE
        );
        Log.e("listener_channel_name", channelName);
        this.applicationContext = applicationContext;

        setActions();

        CTPL.getInstance().init((Application) applicationContext, respCallback);
        message_channel.setMessageHandler((message, reply) -> {
            arguments = castMap(message, String.class, Object.class);
            String key = getCurrentKey();
            Log.e("key", key);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Objects.requireNonNull(action_map.get(key)).accept(key);
            }
        });
    }
    private String getCurrentKey() {
        String key = null;
        if (arguments.containsKey("startConnect"))      key = "startConnect";
        else if (arguments.containsKey("startSend"))    key = "startSend";
        else if (arguments.containsKey("startScan"))    key = "startScan";
        else if (arguments.containsKey("stopScan"))     key = "stopScan";
        else if (arguments.containsKey("closeConnect")) key = "closeConnect";
        else if (arguments.containsKey("destroy"))      key = "destroy";
        return key;
    }
    private void setActions() {
        action_map.put("startConnect",   this :: startConnect);
        action_map.put("startSend",      this :: startSend);
        action_map.put("startScan",      this :: startScan);
        action_map.put("stopScan",       this :: stopScan);
        action_map.put("closeConnect",   this :: closeConnect);
        action_map.put("destroy",        this :: destroy);
    }

    private void startScan(String key) {
        Object value = arguments.get(key);
        if (value == null) return;
        if (!(boolean) value) return;
        deviceList.clear();
        deviceMessageList.clear();
        if (defaultAdapter == null) {
            unsupportedBluetooth(0);
            return;
        }
        if (!defaultAdapter.isEnabled()) {
            unopenedBluetooth(0);
            return;
        }
        if (!checkPermission()) {
            permissionDenied(0);
            return;
        }
        leScanner.startScan(scanCallback);
        Log.i("startScanInfo", "startScan");
    }

    private void stopScan(String key) {
        Object value = arguments.get(key);
        if (value == null) return;
        if (!(boolean) value) return;
        if (defaultAdapter == null) {
            unsupportedBluetooth(1);
            return;
        }
        if (!defaultAdapter.isEnabled()) {
            unopenedBluetooth(1);
            return;
        }
        if (!checkPermission()) {
            permissionDenied(1);
            return;
        }
        leScanner.stopScan(scanCallback);
    }

    private void startConnect(String key){
        Object value = arguments.get(key);
        if (value == null) return;
        String deviceAddress = (String) value;
        Log.i("startConnectInfo", "Start connecting");
        BluetoothManager manager = (BluetoothManager)applicationContext.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager.getAdapter();
        adapter.enable();
        BluetoothDevice bluetoothDevice = adapter.getRemoteDevice(deviceAddress);
        String bluetoothType = bluetoothDevice.getType() == BluetoothDevice.DEVICE_TYPE_DUAL ?
                "SPP" : "BLE";
        CTPL.Port port = "SPP".equals(bluetoothType) ? CTPL.Port.SPP : CTPL.Port.BLE;
        Device device = new Device();
        device.setPort(port);
        device.setBluetoothMacAddr(deviceAddress);
        if (port == CTPL.Port.BLE) {
            device.setBleServiceUUID("49535343-fe7d-4ae5-8fa9-9fafd205e455");
        }
        CTPL.getInstance().connect(device);
    }
    private void startSend(String key) {
        Object value = arguments.get(key);
        if (value == null) return;
        List<String> printDataList = castList(value, String.class);
        Log.i("printDataListInfo", printDataList.toString());
        StringBuilder printData = new StringBuilder();
        for (String data : printDataList) {
            printData.append(data);
            printData.append("\r\n");
        }
        if (!CONNECTED) {
            if (!CTPL.getInstance().isConnected()) {
                Log.e("sendDataInfo", "The device is not connected.");
                message_map.clear();
                message_map.put("message", "The device is not connected.");
                message_map.put("operationCode", 4);
                message_map.put("isSuccessful", false);
                message_map.put("failedCode", 2);
                message_channel.send(message_map);
                return;
            }
        }
        Log.e("startSend", printData.toString());
        CTPL.getInstance().clean();
        CTPL.getInstance().setPrintMode(PrintMode.Label_Divide).execute();
        CTPL.getInstance().execute();
        CTPL.getInstance().clean();
        CTPL.getInstance().append(new byte[]{
                27, 64,
                27, 97, 1,//0居左,1居中,2居右
        });
        CTPL.getInstance().append(printData.toString().getBytes(Charset.forName("gb2312")));
        CTPL.getInstance().execute();
    }
    private void closeConnect(String key) {
        Object value = arguments.get(key);
        if (value == null) return;
        if (!(boolean) value) return;
        CTPL.getInstance().disconnect();
    }
    private void destroy(String key) {
        Object value = arguments.get(key);
        if (value == null) return;
        if (!(boolean) value) return;
        message_channel = null;
        leScanner.stopScan(scanCallback);
        CTPL.getInstance().disconnect();
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
    public static <V> List<V> castList(Object obj, Class<V> value) {
        /*
        对对象转换为List类型作出检查
         */
        List<V> list = new ArrayList<>();
        if (obj instanceof List<?>) {
            for (Object o : (List<?>)obj) {
                list.add(value.cast(o));
            }
            return list;
        }
        return null;
    }
    private void unsupportedBluetooth(int code) {
        message_map.clear();
        message_map.put("message", "This device does not support Bluetooth");
        message_map.put("isSuccessful", false);
        message_map.put("failedCode", 3);
        message_map.put("operationCode", code);
        message_channel.send(message_map);
    }
    private void unopenedBluetooth(int code) {
        message_map.clear();
        message_map.put("message", "Bluetooth is not turned on. Please turn it on");
        message_map.put("isSuccessful", false);
        message_map.put("failedCode", 4);
        message_map.put("operationCode", code);
        message_channel.send(message_map);
    }
    private void permissionDenied(int code) {
        // 权限不足，请检查相关权限并授予
        message_map.clear();
        message_map.put("message", "Insufficient permissions. " +
                "Please check the relevant permissions and grant them");
        message_map.put("isSuccessful", false);
        message_map.put("failedCode", 0);
        message_map.put("operationCode", code);
        message_channel.send(message_map);
    }
    private boolean checkPermission() {
        int denied = PackageManager.PERMISSION_DENIED;
        if (ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == denied) {
            Log.e("notPermission", "ACCESS_FINE_LOCATION");
            return false;
        }
        if (ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) == denied) {
            Log.e("notPermission", "ACCESS_COARSE_LOCATION");
            return false;
        }
        if (ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.BLUETOOTH) == denied) {
            Log.e("notPermission", "BLUETOOTH");
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext, Manifest.permission.BLUETOOTH_SCAN) == denied) {
                Log.e("notPermission", "BLUETOOTH_SCAN");
                return false;
            }
        }
        if (ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.BLUETOOTH_ADMIN) == denied) {
            Log.e("notPermission", "BLUETOOTH_ADMIN");
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext, Manifest.permission.BLUETOOTH_CONNECT) == denied) {
                Log.e("notPermission", "BLUETOOTH_CONNECT");
                return false;
            }
        }
        return true;
    }

    public BasicMessageChannel<Object> getMessage_channel() {
        return message_channel;
    }
}
