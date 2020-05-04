package com.sensor_test;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main3Activity extends AppCompatActivity implements View.OnClickListener, OnChartValueSelectedListener {
    private static final String TAG = Main3Activity.class.getSimpleName();
    private LineChart chart, chart2;
    private MyService mBluetoothLeService;
    private String name, address;
    private Button connectButton, newvalue,checkRotation;
    private Button sensorStatus;
    private TextView connectStatus;
    private boolean mConnect = false;
    private boolean mOpen = false;
    private StringBuffer buffer = new StringBuffer();
    private TextView notify;
    private LineDataSet x, y, z, a, b, c;
    BluetoothGattCharacteristic characteristic;
    boolean plotData = false;
    ArrayList<Entry> values3;
    ArrayList<Entry> values1;
    ArrayList<Entry> values2;
    ArrayList<Entry> values4;
    ArrayList<Entry> values5;
    ArrayList<Entry> values6;
    LineData data;
    int counter = 0;
    // Code to manage Service lifecycle.
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((MyService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            try {
                mBluetoothLeService.connect(address);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        values3 = new ArrayList<>();
        values1 = new ArrayList<>();
        values2 = new ArrayList<>();
        values4 = new ArrayList<>();
        values5 = new ArrayList<>();
        values6 = new ArrayList<>();
        name = getIntent().getStringExtra("name");
        address = getIntent().getStringExtra("address");
        Log.e(TAG, "address:" + address);
        TextView nameView = findViewById(R.id.device_name);
        nameView.setText("device_name:" + name);
        TextView addressView = findViewById(R.id.device_address);
        nameView.setText("device_address:" + address);
        notify = findViewById(R.id.notify);
        connectButton = findViewById(R.id.connect);
        newvalue = findViewById(R.id.newvalue);
        sensorStatus = findViewById(R.id.open);
        connectStatus = findViewById(R.id.device_status);
        connectStatus.setText("连接状态：disconnect");
        connectButton.setOnClickListener(this);
        sensorStatus.setOnClickListener(this);
        Intent gattServiceIntent = new Intent(this, MyService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        checkRotation = findViewById(R.id.checkRotation);
        checkRotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main3Activity.this, RotationActivty.class);
                startActivity(intent);
            }
        });
        chart = findViewById(R.id.chart1);
        chart2 = findViewById(R.id.chart2);
        setGraph();
        setGraph2();
        newvalue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = random24numbers();
                notify.setText(s);
                String[] f = splitToNChar(s, 2);
                addEntry((float) Float.valueOf(f[0]), (float) Float.valueOf(f[1]), (float) Float.valueOf(f[2]), (float) Float.valueOf(f[3]), (float) Float.valueOf(f[4]), (float) Float.valueOf(f[5]));
                addEntry2((float) Float.valueOf(f[6]), (float) Float.valueOf(f[7]), (float) Float.valueOf(f[8]), (float) Float.valueOf(f[9]), (float) Float.valueOf(f[10]), (float) Float.valueOf(f[11]));


            }
        });
        /* feedMultiple();*/
    }

    private BroadcastReceiver sensorBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MyService.CONNECT_STATUS.equals(action)) {
                mConnect = true;
                connectButton.setText("关闭连接");
                connectStatus.setText("连接状态：connect");
            } else if (MyService.DISCONNECT_STATUS.equals(action)) {
                mConnect = false;
                connectButton.setText("连接");
                connectStatus.setText("连接状态：disconnect");

            } else if (MyService.WRITE_SUCCESS.equals(action)) {
                String stringExtra = intent.getStringExtra(MyService.VALUE);
                characteristic = mBluetoothLeService.getBluetoothGattCharacteristic();
                if (stringExtra.equals("0")) {
                    if (mOpen == false) {
                        mOpen = true;
                        sensorStatus.setText("关闭陀螺仪");
                        mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                    } else {
                        mOpen = false;
                        sensorStatus.setText("打开陀螺仪");
                    }
                }

            } else if (MyService.NOTIFY_DATA.equals(action)) {
                String stringExtra = intent.getStringExtra(MyService.VALUE);
                Log.e(TAG, "brodcast" + stringExtra);
                notify.setText(stringExtra);
                String s = stringExtra;
                String[] f = splitToNChar(s, 2);
                addEntry((float) Float.valueOf(f[0]), (float) Float.valueOf(f[1]), (float) Float.valueOf(f[2]), (float) Float.valueOf(f[3]), (float) Float.valueOf(f[4]), (float) Float.valueOf(f[5]));
                addEntry2((float) Float.valueOf(f[6]), (float) Float.valueOf(f[7]), (float) Float.valueOf(f[8]), (float) Float.valueOf(f[9]), (float) Float.valueOf(f[10]), (float) Float.valueOf(f[11]));

            }
        }
    };

    //重启时、开始时注册广播
    @Override
    protected void onResume() {
        super.onResume();
        //注册广播
        registerReceiver(sensorBroadcast, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(address);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    //停止时，注销广播
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(sensorBroadcast);
    }

    //关闭activity
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    //注册广播时定义intent的各种属性
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyService.CONNECT_STATUS);
        intentFilter.addAction(MyService.DISCONNECT_STATUS);
        intentFilter.addAction(MyService.WRITE_SUCCESS);
        intentFilter.addAction(MyService.NOTIFY_DATA);
        return intentFilter;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connect:
                if (mConnect) {
                    mBluetoothLeService.disconnect();
                } else {
                    mBluetoothLeService.connect(address);
                }
                break;
            case R.id.open:
                if (mConnect) {
                    if (mOpen) {
                        sendMsg("55AA2F00");
                    } else {
                        sendMsg("55AA2E00");
                    }
                } else {
                    Toast.makeText(this, "请先连接设备", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    private void sendMsg(String order) {
        BluetoothGattCharacteristic writ = mBluetoothLeService.getWritBluetoothGattCharacteristic();
        byte[] bytes = HexUtil.hexStringToBytes(order);
        writ.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        writ.setValue(bytes);
        this.mBluetoothLeService.writeCharacteristic(writ);
    }

    private void setGraph() {
        chart.setOnChartValueSelectedListener(Main3Activity.this);

        // enable description text
        chart.getDescription().setEnabled(true);

        // enable touch gestures
        chart.setTouchEnabled(true);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);

        // set an alternative background color
        chart.setBackgroundColor(Color.LTGRAY);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        chart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);

        l.setTextColor(Color.WHITE);

        XAxis xl = chart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

    }

    private void setGraph2() {
        chart2.setOnChartValueSelectedListener(Main3Activity.this);

        // enable description text
        chart2.getDescription().setEnabled(true);

        // enable touch gestures
        chart2.setTouchEnabled(true);

        // enable scaling and dragging
        chart2.setDragEnabled(true);
        chart2.setScaleEnabled(true);
        chart2.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        chart2.setPinchZoom(true);

        // set an alternative background color
        chart2.setBackgroundColor(Color.LTGRAY);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        chart2.setData(data);

        // get the legend (only possible after setting data)
        Legend l = chart2.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);

        l.setTextColor(Color.WHITE);

        XAxis xl = chart2.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = chart2.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
        YAxis rightAxis = chart2.getAxisRight();
        rightAxis.setEnabled(false);

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    private void addEntry(float xValue, float yValue, float zValue, float aValue, float bValue, float cValue) {

        values1.add(new Entry(chart.getData().getEntryCount(), xValue));
        values2.add(new Entry(chart.getData().getEntryCount(), yValue));
        values3.add(new Entry(chart.getData().getEntryCount(), zValue));
        values4.add(new Entry(chart.getData().getEntryCount(), aValue));
        values5.add(new Entry(chart.getData().getEntryCount(), bValue));
        values6.add(new Entry(chart.getData().getEntryCount(), cValue));
        data = chart.getData();
        chart.getDescription().setEnabled(false);
        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            x = (LineDataSet) chart.getData().getDataSetByIndex(0);
            y = (LineDataSet) chart.getData().getDataSetByIndex(1);
            z = (LineDataSet) chart.getData().getDataSetByIndex(2);
            a = (LineDataSet) chart.getData().getDataSetByIndex(3);
            b = (LineDataSet) chart.getData().getDataSetByIndex(4);
            c = (LineDataSet) chart.getData().getDataSetByIndex(5);
            x.setValues(values1);
            y.setValues(values2);
            z.setValues(values3);
            a.setValues(values4);
            b.setValues(values5);
            c.setValues(values6);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(120);
            // chart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            chart.moveViewToX(chart.getData().getEntryCount());
        } else {
            // create a dataset and give it a type
            x = new LineDataSet(values1, "acc_x_H");
            x.setAxisDependency(YAxis.AxisDependency.LEFT);
            x.setColor(Color.RED);
            x.setCircleColor(Color.TRANSPARENT);
            x.setLineWidth(2f);
            x.setCircleRadius(3f);
            x.setFillAlpha(65);
            x.setFillColor(ColorTemplate.getHoloBlue());
            x.setHighLightColor(Color.rgb(244, 117, 117));
            x.setDrawCircleHole(false);
            y = new LineDataSet(values2, "acc_x_L");
            y.setAxisDependency(YAxis.AxisDependency.RIGHT);
            y.setColor(Color.RED);
            y.setCircleColor(Color.TRANSPARENT);
            y.setLineWidth(2f);
            y.setCircleRadius(3f);
            y.setFillAlpha(65);
            y.setFillColor(Color.RED);
            y.setDrawCircleHole(false);
            y.setHighLightColor(Color.rgb(244, 117, 117));
            z = new LineDataSet(values3, "acc_y_H");
            z.setAxisDependency(YAxis.AxisDependency.RIGHT);
            z.setColor(Color.YELLOW);
            z.setCircleColor(Color.TRANSPARENT);
            z.setLineWidth(2f);
            z.setCircleRadius(3f);
            z.setFillAlpha(65);
            z.setFillColor(ColorTemplate.colorWithAlpha(Color.YELLOW, 200));
            z.setDrawCircleHole(false);
            z.setHighLightColor(Color.rgb(244, 117, 117));

            c = new LineDataSet(values3, "acc_y_L");
            c.setAxisDependency(YAxis.AxisDependency.RIGHT);
            c.setColor(Color.YELLOW);
            c.setCircleColor(Color.TRANSPARENT);
            c.setLineWidth(2f);
            c.setCircleRadius(3f);
            c.setFillAlpha(65);
            c.setFillColor(ColorTemplate.colorWithAlpha(Color.YELLOW, 200));
            c.setDrawCircleHole(false);
            c.setHighLightColor(Color.rgb(244, 117, 117));

            a = new LineDataSet(values3, "acc_z_H");
            a.setAxisDependency(YAxis.AxisDependency.RIGHT);
            a.setColor(Color.WHITE);
            a.setCircleColor(Color.TRANSPARENT);
            a.setLineWidth(2f);
            a.setCircleRadius(3f);
            a.setFillAlpha(65);
            a.setFillColor(ColorTemplate.colorWithAlpha(Color.YELLOW, 200));
            a.setDrawCircleHole(false);
            a.setHighLightColor(Color.rgb(244, 117, 117));
            b = new LineDataSet(values3, "acc_z_L");
            b.setAxisDependency(YAxis.AxisDependency.RIGHT);
            b.setColor(Color.WHITE);
            b.setCircleColor(Color.TRANSPARENT);
            b.setLineWidth(2f);
            b.setCircleRadius(3f);
            b.setFillAlpha(65);
            b.setFillColor(ColorTemplate.colorWithAlpha(Color.YELLOW, 200));
            b.setDrawCircleHole(false);
            b.setHighLightColor(Color.rgb(244, 117, 117));


            LineData data = new LineData(x, y, z, a, b, c);
            data.setValueTextColor(Color.TRANSPARENT);
            data.setValueTextSize(9f);

            // set data
            chart.setData(data);
        }


    }

    private void addEntry2(float xValue, float yValue, float zValue, float aValue, float bValue, float cValue) {

        values1.add(new Entry(chart2.getData().getEntryCount(), xValue));
        values2.add(new Entry(chart2.getData().getEntryCount(), yValue));
        values3.add(new Entry(chart2.getData().getEntryCount(), zValue));
        values4.add(new Entry(chart2.getData().getEntryCount(), aValue));
        values5.add(new Entry(chart2.getData().getEntryCount(), bValue));
        values6.add(new Entry(chart2.getData().getEntryCount(), cValue));
        data = chart2.getData();
        chart2.getDescription().setEnabled(false);
        if (chart2.getData() != null &&
                chart2.getData().getDataSetCount() > 0) {
            x = (LineDataSet) chart2.getData().getDataSetByIndex(0);
            y = (LineDataSet) chart2.getData().getDataSetByIndex(1);
            z = (LineDataSet) chart2.getData().getDataSetByIndex(2);
            a = (LineDataSet) chart2.getData().getDataSetByIndex(3);
            b = (LineDataSet) chart2.getData().getDataSetByIndex(4);
            c = (LineDataSet) chart2.getData().getDataSetByIndex(5);
            x.setValues(values1);
            y.setValues(values2);
            z.setValues(values3);
            a.setValues(values4);
            b.setValues(values5);
            c.setValues(values6);
            chart2.getData().notifyDataChanged();
            chart2.notifyDataSetChanged();
            // limit the number of visible entries
            chart2.setVisibleXRangeMaximum(120);
            // chart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            chart2.moveViewToX(chart2.getData().getEntryCount());
        } else {
            // create a dataset and give it a type
            x = new LineDataSet(values1, "gyr_x_H");
            x.setAxisDependency(YAxis.AxisDependency.LEFT);
            x.setColor(Color.RED);
            x.setCircleColor(Color.TRANSPARENT);
            x.setLineWidth(2f);
            x.setCircleRadius(3f);
            x.setFillAlpha(65);
            x.setFillColor(ColorTemplate.getHoloBlue());
            x.setHighLightColor(Color.rgb(244, 117, 117));
            x.setDrawCircleHole(false);
            y = new LineDataSet(values2, "gyr_x_L");
            y.setAxisDependency(YAxis.AxisDependency.RIGHT);
            y.setColor(Color.RED);
            y.setCircleColor(Color.TRANSPARENT);
            y.setLineWidth(2f);
            y.setCircleRadius(3f);
            y.setFillAlpha(65);
            y.setFillColor(Color.RED);
            y.setDrawCircleHole(false);
            y.setHighLightColor(Color.rgb(244, 117, 117));
            z = new LineDataSet(values3, "gyr_y_H");
            z.setAxisDependency(YAxis.AxisDependency.RIGHT);
            z.setColor(Color.YELLOW);
            z.setCircleColor(Color.TRANSPARENT);
            z.setLineWidth(2f);
            z.setCircleRadius(3f);
            z.setFillAlpha(65);
            z.setFillColor(ColorTemplate.colorWithAlpha(Color.YELLOW, 200));
            z.setDrawCircleHole(false);
            z.setHighLightColor(Color.rgb(244, 117, 117));

            c = new LineDataSet(values3, "gyr_y_L");
            c.setAxisDependency(YAxis.AxisDependency.RIGHT);
            c.setColor(Color.YELLOW);
            c.setCircleColor(Color.TRANSPARENT);
            c.setLineWidth(2f);
            c.setCircleRadius(3f);
            c.setFillAlpha(65);
            c.setFillColor(ColorTemplate.colorWithAlpha(Color.YELLOW, 200));
            c.setDrawCircleHole(false);
            c.setHighLightColor(Color.rgb(244, 117, 117));

            a = new LineDataSet(values3, "gyr_z_H");
            a.setAxisDependency(YAxis.AxisDependency.RIGHT);
            a.setColor(Color.WHITE);
            a.setCircleColor(Color.TRANSPARENT);
            a.setLineWidth(2f);
            a.setCircleRadius(3f);
            a.setFillAlpha(65);
            a.setFillColor(ColorTemplate.colorWithAlpha(Color.YELLOW, 200));
            a.setDrawCircleHole(false);
            a.setHighLightColor(Color.rgb(244, 117, 117));
            b = new LineDataSet(values3, "gyr_z_L");
            b.setAxisDependency(YAxis.AxisDependency.RIGHT);
            b.setColor(Color.WHITE);
            b.setCircleColor(Color.TRANSPARENT);
            b.setLineWidth(2f);
            b.setCircleRadius(3f);
            b.setFillAlpha(65);
            b.setFillColor(ColorTemplate.colorWithAlpha(Color.YELLOW, 200));
            b.setDrawCircleHole(false);
            b.setHighLightColor(Color.rgb(244, 117, 117));


            LineData data = new LineData(x, y, z, a, b, c);
            data.setValueTextColor(Color.TRANSPARENT);
            data.setValueTextSize(9f);

            // set data
            chart2.setData(data);
        }


    }

    private Thread thread;

    /*    private void feedMultiple() {

            if (thread != null)
                thread.interrupt();

            final Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    addEntry(((float) (Math.random() * 40) + 30f), ((float) (Math.random() * 10) + 20f), ((float) (Math.random() * -2) + 10f));
                }
            };

            thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    for (int i = 0; i < 1000; i++) {

                        // Don't generate garbage runnables inside the loop.
                        runOnUiThread(runnable);

                        try {
                            Thread.sleep(25);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            thread.start();
        }*/
    public static String random24numbers() {
        final int min = 0;
        final int max = 9;
        StringBuilder randomStringBuilder = new StringBuilder();
        char tempChar;
        for (int i = 0; i < 25; i++) {
            randomStringBuilder.append(String.valueOf(new Random().nextInt((max - min) + 1) + min));
        }
        return randomStringBuilder.toString();
    }

    private static String[] splitToNChar(String text, int size) {
        List<String> parts = new ArrayList<>();

        int length = text.length();
        for (int i = 0; i < length; i += size) {
            parts.add(text.substring(i, Math.min(length, i + size)));
        }
        return parts.toArray(new String[0]);
    }
}
