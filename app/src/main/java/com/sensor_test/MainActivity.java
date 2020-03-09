package com.sensor_test;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity {
    private static final String TAG="MainActivity";
//    private static final String ServiceUUID="f000b000-0451-4000-b000-000000000000";
//    private static final String CharaUUIDWrit="f000b001-0451-4000-b000-000000000000";
//    private static final String CharaUUIDRead="f000b002-0451-4000-b000-000000000000";
//    private static final String Notify="00002902-0000-1000-8000-00805f9b34fb";
//    public static final String CONNECT_STATUS="com.qijin.bledemo.connect_status";
//    public static final String DISCONNECT_STATUS="com.qijin.bledemo.disconnect_status";
//    public static final String WRITE_SUCCESS="com.qijin.bledemo.write_success";
//    public static final String NOTIFY_DATA="com.qijin.bledemo.notify_data";
    private List<Bean> nameList;
    private ListAdapter adapter;
    ListView view;
    BluetoothGatt mBluetoothGatt;
    private PermissionListener mListener;
    private static Activity activity ;
    final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private String[] permissions = {Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_ADMIN,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this ;
        requestAllPower();
        view=findViewById(R.id.list);
        nameList=new ArrayList<>();
        adapter=new ListAdapter(nameList,this);
        view.setAdapter(adapter);
        findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findPairDevice();
            }
        });
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bean bean = nameList.get(position);
//                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(bean.address);
//                BluetoothGatt  bluetoothGatt = device.connectGatt(MainActivity.this, false, mGattCallback);
                Intent intent=new Intent(MainActivity.this,Main3Activity.class);
                intent.putExtra("name",bean.name);
                intent.putExtra("address",bean.address);
                startActivity(intent);
            }
        });
    }

    public void findPairDevice(){
        if(mBluetoothAdapter != null){
            Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
            for(BluetoothDevice device : bondedDevices ){
                Log.e(TAG,"name"+device.getName());
                Bean bean=new Bean(device.getName(),device.getAddress());
                nameList.add(bean);
                adapter.notifyDataSetChanged();
            }
        }

    }

    public void requestAllPower() {
        if (Build.VERSION.SDK_INT >= 23) {//判断当前系统是不是Android6.0
            requestRuntimePermissions(permissions, new PermissionListener() {
                @Override
                public void granted() {
                    //权限申请通过
                }

                @Override
                public void denied(List<String> deniedList) {
                    //权限申请未通过
                    for (String denied : deniedList) {
                        if (denied.equals("android.permission.ACCESS_FINE_LOCATION")) {
                            Toast.makeText(MainActivity.this, "定位失败", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "无权限", Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            });
        }
    }

    public void requestRuntimePermissions(
            String[] permissions, PermissionListener listener) {
        mListener = listener;
        List<String> permissionList = new ArrayList<>();
        // 遍历每一个申请的权限，把没有通过的权限放在集合中
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity,permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            } else {
                mListener.granted();
            }
        }
        // 申请权限
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(activity,
                    permissionList.toArray(new String[permissionList.size()]), 1);
        }
    }

    /**
     * 申请后的处理
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            List<String> deniedList = new ArrayList<>();
            // 遍历所有申请的权限，把被拒绝的权限放入集合
            for (int i = 0; i < grantResults.length; i++) {
                int grantResult = grantResults[i];
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    mListener.granted();
                } else {
                    deniedList.add(permissions[i]);
                }
            }
            if (!deniedList.isEmpty()) {
                mListener.denied(deniedList);
            }
        }
    }

//    public void readCharacteristic() {
//        BluetoothGattCharacteristic characteristic = charaMap.get(CharaUUIDRead);
//        if (mBluetoothGatt == null) {
//            Log.w(TAG, " --------- BluetoothAdapter not initialized --------- ");
//            return;
//        }
//            boolean b = mBluetoothGatt.readCharacteristic(characteristic);
//        Log.e(TAG, "DEC" +b);
//    }

//    private void write(){
//        BluetoothGattCharacteristic mBleGattCharacteristic = charaMap.get(CharaUUIDWrit);
//        if (null == mBleGattCharacteristic) {
//            Log.e(TAG, "FAILED_INVALID_CHARACTER");
//            return;
//        }
//        Log.e(TAG, "write"+mBleGattCharacteristic.getPermissions());
//        byte[] bytes=HexUtil.hexStringToBytes("55AA2E00");
////        byte[] bytes=new byte[]{(byte) 0x55,(byte)0xAA,(byte)0x2E,(byte)0x00};
//        mBleGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
//        //设置数组进去
//        mBleGattCharacteristic.setValue(bytes);
//        //发送
////
//        boolean b = mBluetoothGatt.writeCharacteristic(mBleGattCharacteristic);
//              Log.e(TAG, "send:" + b + "data：" );
//    }
//
//    private BluetoothGattCallback mGattCallback=new BluetoothGattCallback() {
//
//        @Override
//        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//            if (newState == BluetoothProfile.STATE_CONNECTED){
//                Log.e("UUID","连接成功"+newState);
//                updateBrocast(CONNECT_STATUS,null);
//                gatt.discoverServices();
//
//            }else if (newState==BluetoothProfile.STATE_DISCONNECTED){
//                gatt.disconnect();
//                updateBrocast(DISCONNECT_STATUS,null);
//                Log.e("UUID","连接失败"+newState);
//            }
//        }
//
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                mBluetoothGatt=gatt;
//                List<BluetoothGattService> services = mBluetoothGatt.getServices();
//                for (int i = 0; i < services.size(); i++) {
//                    if (ServiceUUID.equals(services.get(i).getUuid().toString())){
//                        List<BluetoothGattCharacteristic> characteristics = services.get(i).getCharacteristics();
//                        for (int j = 0; j < characteristics.size(); j++) {
//                            Log.e(TAG,"characteristics"+characteristics.get(j).getUuid().toString());
//                            charaMap.put(characteristics.get(j).getUuid().toString(), characteristics.get(j));
//                        }
//                    }
//                }
//                BluetoothGattCharacteristic bluetoothGattCharacteristic = charaMap.get(CharaUUIDRead);
//                if (bluetoothGattCharacteristic == null)
//                    return;
//                enableGattServicesNotification(bluetoothGattCharacteristic);
//            } else {
//                Log.w(TAG, " --------- onServicesDiscovered received: " + status);
//            }
//        }
//
//        @Override
//        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            Log.e(TAG, " --------- onCharacteristicRead: " + status);
//            if (status==BluetoothGatt.GATT_SUCCESS){
//                String value=HexUtil.bytesToHexString(characteristic.getValue());
//                Log.e(TAG,value);
//            }
////            super.onCharacteristicRead(gatt, characteristic, status);
//        }
//
//        @Override
//        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            Log.e("onCharacteristicWrite中", "写入状态："+status);
//            if(status == BluetoothGatt.GATT_SUCCESS) {//写入成功
//                updateBrocast(WRITE_SUCCESS,null);
//                String value=HexUtil.bytesToHexString(characteristic.getValue());
//                Log.e("onCharacteristicWrite中", "写入成功"+value);
//            }
//            super.onCharacteristicWrite(gatt, characteristic, status);
//        }
//
//        @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//            String value=HexUtil.bytesToHexString(characteristic.getValue());
//            updateBrocast(NOTIFY_DATA,value);
//            Log.e(TAG, " --------- onCharacteristicChanged: "+value);
////            super.onCharacteristicChanged(gatt, characteristic);
//        }
//
//    };

//    private void updateBrocast(String action,String value){
//        Intent intent=new Intent(action);
//        intent.putExtra("value",value);
//        sendBroadcast(intent);
//    }
//
//    private void enableGattServicesNotification(BluetoothGattCharacteristic gattCharacteristic) {
//        if (gattCharacteristic == null) return;
//       setCharacteristicNotification(gattCharacteristic,true);
//    }
//
//    private void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
//                                              boolean enabled) {
//        if (mBluetoothGatt == null) {
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
//        try {
//            Thread.sleep(150);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//    }

    @Override
    protected void onDestroy() {
//        mBluetoothGatt.disconnect();
//        mBluetoothGatt.close();
        super.onDestroy();
    }
}
