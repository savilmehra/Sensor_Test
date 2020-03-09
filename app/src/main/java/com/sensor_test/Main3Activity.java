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

public class Main3Activity extends AppCompatActivity implements View.OnClickListener, OnChartValueSelectedListener {
    private static final String TAG = Main3Activity.class.getSimpleName();
    private LineChart chart;
    private MyService mBluetoothLeService;
    private String name, address;
    private Button connectButton, newvalue;
    private Button sensorStatus;
    private TextView connectStatus;
    private boolean mConnect = false;
    private boolean mOpen = false;
    private StringBuffer buffer = new StringBuffer();
    private TextView notify;
    private LineDataSet x, y, z;
    BluetoothGattCharacteristic characteristic;
    boolean plotData = false;
    ArrayList<Entry> values3;
    ArrayList<Entry> values1;
    ArrayList<Entry> values2;
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

        chart = findViewById(R.id.chart1);
        setGraph();
        newvalue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEntry(((float) (Math.random() * 40) + 30f), ((float) (Math.random() * 10) + 20f), ((float) (Math.random() * -2) + 10f));
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


    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    private void addEntry(float xValue, float yValue, float zValue) {
        values1.add(new Entry(chart.getData().getEntryCount(), xValue));
        values2.add(new Entry(chart.getData().getEntryCount(), yValue));
        values3.add(new Entry(chart.getData().getEntryCount(), zValue));
        data = chart.getData();
        chart.getDescription().setEnabled(false);
        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            x = (LineDataSet) chart.getData().getDataSetByIndex(0);
            y = (LineDataSet) chart.getData().getDataSetByIndex(1);
            z = (LineDataSet) chart.getData().getDataSetByIndex(2);
            x.setValues(values1);
            y.setValues(values2);
            z.setValues(values3);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(120);
            // chart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            chart.moveViewToX(chart.getData().getEntryCount());
        } else {
            // create a dataset and give it a type
            x = new LineDataSet(values1, "x value");

            x.setAxisDependency(YAxis.AxisDependency.LEFT);
            x.setColor(ColorTemplate.getHoloBlue());
            x.setCircleColor(Color.TRANSPARENT);
            x.setLineWidth(2f);
            x.setCircleRadius(3f);
            x.setFillAlpha(65);
            x.setFillColor(ColorTemplate.getHoloBlue());
            x.setHighLightColor(Color.rgb(244, 117, 117));
            x.setDrawCircleHole(false);
            //x.setFillFormatter(new MyFillFormatter(0f));
            //x.setDrawHorizontalHighlightIndicator(false);
            //x.setVisible(false);
            //x.setCircleHoleColor(Color.WHITE);

            // create a dataset and give it a type
            y = new LineDataSet(values2, "y value");
            y.setAxisDependency(YAxis.AxisDependency.RIGHT);
            y.setColor(Color.RED);
            y.setCircleColor(Color.TRANSPARENT);
            y.setLineWidth(2f);
            y.setCircleRadius(3f);
            y.setFillAlpha(65);
            y.setFillColor(Color.RED);
            y.setDrawCircleHole(false);
            y.setHighLightColor(Color.rgb(244, 117, 117));
            //y.setFillFormatter(new MyFillFormatter(900f));

            z = new LineDataSet(values3, "z value");
            z.setAxisDependency(YAxis.AxisDependency.RIGHT);
            z.setColor(Color.YELLOW);
            z.setCircleColor(Color.TRANSPARENT);
            z.setLineWidth(2f);
            z.setCircleRadius(3f);
            z.setFillAlpha(65);
            z.setFillColor(ColorTemplate.colorWithAlpha(Color.YELLOW, 200));
            z.setDrawCircleHole(false);
            z.setHighLightColor(Color.rgb(244, 117, 117));

            // create a data object with the data sets
            LineData data = new LineData(x, y, z);
            data.setValueTextColor(Color.TRANSPARENT);
            data.setValueTextSize(9f);

            // set data
            chart.setData(data);
        }


    }


    private Thread thread;

    private void feedMultiple() {

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
    }

}
