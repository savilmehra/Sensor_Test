package com.sensor_test;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.UUID;


public class MyService extends Service {
    private final static String TAG = MyService.class.getSimpleName();
    //蓝牙模块的某个服务的UUID
    private static final String ServiceUUID="f000b000-0451-4000-b000-000000000000";
    private static final String CharaUUIDWrit="f000b001-0451-4000-b000-000000000000";
    private static final String CharaUUIDRead="f000b002-0451-4000-b000-000000000000";
    private static final String Notify="00002902-0000-1000-8000-00805f9b34fb";
    public static final String CONNECT_STATUS="com.qijin.bledemo.connect_status";
    public static final String DISCONNECT_STATUS="com.qijin.bledemo.disconnect_status";
    public static final String WRITE_SUCCESS="com.qijin.bledemo.write_success";
    public static final String NOTIFY_DATA="com.qijin.bledemo.notify_data";
    public static final String VALUE="ex_value";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private MyService mBluetoothLeService;

    public int number = 0;

    // GATT返回值，例如连接状态和service的改变 etc
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        //连接状态改变
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = CONNECT_STATUS;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.e(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = DISCONNECT_STATUS;
                mConnectionState = STATE_DISCONNECTED;
                Log.e(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        //发现服务
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG,"Connected to GATT server. ==成功");
//                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

                BluetoothGattCharacteristic bluetoothGattCharacteristic = getBluetoothGattCharacteristic();
                Log.e(TAG, "onServicesDiscovered received: " + status+"--chara"+bluetoothGattCharacteristic.getProperties());
                if (bluetoothGattCharacteristic == null)
                    return;
                enableGattServicesNotification(bluetoothGattCharacteristic);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }


        //特性改变
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            String value=HexUtil.bytesToHexString(characteristic.getValue());
            Log.e(TAG, System.currentTimeMillis()+" write"+value);
            broadcastUpdate(NOTIFY_DATA, value);

        }

        //特性书写
        @Override
        public void onCharacteristicWrite (BluetoothGatt gatt,
                                           BluetoothGattCharacteristic characteristic,
                                           int status){
//            String value=HexUtil.bytesToHexString(characteristic.getValue());
            broadcastUpdate(WRITE_SUCCESS, status+"");
            Log.e(TAG," onCharacteristicWrite"+status);

        }
    };

    //广播连接状态的改变
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);

    }

    //广播的更新，包括数据的处理,读取heart的数据
    private void broadcastUpdate(final String action,
                                 final String value) {
        final Intent intent = new Intent(action);

            if (value != null) {
                intent.putExtra(VALUE, value);
            }
            //TODO 处理数据
        sendBroadcast(intent);
    }

    //初始化本地iBinder
    public class LocalBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    //初始化本地蓝牙适配器，如果初始化成功，返回true
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    //连接远程GATTserver，如果初始化成功，返回true。回调触发函数BluetoothGattCallback#onConnectionStateChange
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                Log.d(TAG, "connect success");
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;

        return true;
    }

    //断开连接远程GATTserver，回调触发函数BluetoothGattCallback#onConnectionStateChange
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    //结束连接ble设备后，释放资源
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    //获取C2541的特性服务
    public BluetoothGattCharacteristic getBluetoothGattCharacteristic()
    {
        return mBluetoothGatt.getService(UUID.fromString(ServiceUUID)).getCharacteristic(UUID.fromString(CharaUUIDRead));
    }

    public BluetoothGattCharacteristic getWritBluetoothGattCharacteristic()
    {
        return mBluetoothGatt.getService(UUID.fromString(ServiceUUID)).getCharacteristic(UUID.fromString(CharaUUIDWrit));
    }

    //读取characteristic，回调触发函数BluetoothGattCallback#onCharacteristicRead
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    //发送characteristic，回调触发函数BluetoothGattCallback#onCharacteristicWrite
    //TODO 添加write的函数
    public boolean  writeCharacteristic (BluetoothGattCharacteristic characteristic){
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        boolean b = mBluetoothGatt.writeCharacteristic(characteristic);
        Log.e(TAG,"writ--"+b);
        return true;
    }

    private void enableGattServicesNotification(BluetoothGattCharacteristic gattCharacteristic) {
        if (gattCharacteristic == null) return;
       setCharacteristicNotification(gattCharacteristic,true);
    }

//    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
//                                              boolean enabled) {
//        if (mBluetoothAdapter == null ||mBluetoothGatt == null) {
//            Log.w(TAG, " --------- BluetoothAdapter not initialized --------- ");
//            return;
//        }
//        boolean isEnableNotification= mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
//
//        if(isEnableNotification) {
//            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(Notify));
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            boolean b = mBluetoothGatt.writeDescriptor(descriptor);
//            Log.e(TAG,"DEC"+b);
//        }
//
//
//    }

//    //开启或者关闭notification  虽然里面有heart 的内容
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        //TODO  这里都是需要改变的
        // This is specific to Heart Rate Measurement.
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                UUID.fromString(Notify));
        if (enabled) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        }else {
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        mBluetoothGatt.writeDescriptor(descriptor);
    }

}
