package com.example.projectcuoiky;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.projectcuoiky.session.DeviceSession;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class HeartDetailActivity extends AppCompatActivity {

    private static final int REQUEST_BLUETOOTH_CONNECT = 1001;
    private LineChart lineChart;
    private TextView textHeartRate, textStatus, textTrend, textLastUpdated;
    private TextView textBleTitle, textBleName, textBleAddress, textBleMethod;
    private final List<HeartRateEntry> heartRateData = new ArrayList<>();
    private Handler autoUpdateHandler = new Handler();
    private Runnable autoUpdateTask;

    static class HeartRateEntry {
        long timestamp;
        int bpm;

        HeartRateEntry(long timestamp, int bpm) {
            this.timestamp = timestamp;
            this.bpm = bpm;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_detail);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        textHeartRate = findViewById(R.id.textHeartRate);
        textStatus = findViewById(R.id.textStatus);
        textTrend = findViewById(R.id.textTrend);
        textLastUpdated = findViewById(R.id.textLastUpdated);
        lineChart = findViewById(R.id.lineChart);

        textBleTitle = findViewById(R.id.textBleTitle);
        textBleName = findViewById(R.id.textBleName);
        textBleAddress = findViewById(R.id.textBleAddress);
        textBleMethod = findViewById(R.id.textBleMethod);

        DeviceSession session = MyApp.getDeviceSession();

        if (session != null && session.getType() == DeviceSession.Type.SERVER) {
            textBleTitle.setText("🌐 Thiết bị đang kết nối:");
            textBleName.setText("• Tên thiết bị: " + session.getServerUrl());
            textBleAddress.setText("• Địa chỉ: Server từ xa");
            textBleMethod.setText("• Phương thức: Internet / HTTP");
            startAutoFetch(session.getServerUrl());

        } else if (session != null && session.getType() == DeviceSession.Type.BLUETOOTH) {
            textBleTitle.setText("📶 Thiết bị đang kết nối:");
            textBleName.setText("• Tên thiết bị: " + session.getBluetoothDeviceName());
            textBleAddress.setText("• Địa chỉ: " + session.getBluetoothMac());
            textBleMethod.setText("• Phương thức: Bluetooth");
            connectToBluetoothDevice(session.getBluetoothMac());

        } else {
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            String address = prefs.getString("server_address", "Không có");
            boolean connected = prefs.getBoolean("server_connected", false);

            if (connected) {
                textBleTitle.setText("🌐 Thiết bị mặc định:");
                textBleName.setText("• Tên thiết bị: " + address);
                textBleAddress.setText("• Địa chỉ: Server từ xa");
                textBleMethod.setText("• Phương thức: HTTP (mặc định)");
                startAutoFetch(address);
            } else {
                Toast.makeText(this, "Chưa có thiết bị kết nối", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startAutoFetch(String url) {
        autoUpdateTask = () -> {
            fetchDataFromServer(url);
            autoUpdateHandler.postDelayed(autoUpdateTask, 5000);
        };
        autoUpdateHandler.post(autoUpdateTask);
    }

    private void updateChartAndUI() {
        if (heartRateData.isEmpty()) {
            lineChart.clear();
            lineChart.setNoDataText("Không có dữ liệu để hiển thị.");
            return;
        }

        List<Entry> entries = new ArrayList<>();
        String[] labels = new String[heartRateData.size()];
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        for (int i = 0; i < heartRateData.size(); i++) {
            HeartRateEntry e = heartRateData.get(i);
            entries.add(new Entry(i, e.bpm));
            labels[i] = sdf.format(e.timestamp);
        }

        int latestBpm = heartRateData.get(heartRateData.size() - 1).bpm;
        textHeartRate.setText(latestBpm + " nhịp/phút");
        textHeartRate.setTextColor((latestBpm > 110 || latestBpm < 50) ? Color.RED : Color.parseColor("#E84C4F"));

        if (latestBpm < 60) textStatus.setText("❤️ Tình trạng: Rối loạn nhẹ");
        else if (latestBpm <= 100) textStatus.setText("❤️ Tình trạng: Bình thường");
        else textStatus.setText("❤️ Tình trạng: Cảnh báo");

        if (heartRateData.size() >= 2) {
            int prev = heartRateData.get(heartRateData.size() - 2).bpm;
            int diff = latestBpm - prev;
            if (diff >= 3) textTrend.setText("📈 Xu hướng: Tăng nhẹ");
            else if (diff <= -3) textTrend.setText("📈 Xu hướng: Giảm dần");
            else textTrend.setText("📈 Xu hướng: Ổn định");
        }

        String timeStr = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis());
        textLastUpdated.setText("Cập nhật lúc " + timeStr);

        LineDataSet dataSet = new LineDataSet(entries, "Nhịp tim");
        dataSet.setColor(Color.RED);
        dataSet.setCircleColor(Color.RED);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        lineChart.setData(new LineData(dataSet));

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        // ✅ Cải thiện hiển thị nhãn trục X
        xAxis.setLabelCount(Math.min(labels.length, 6), false);
        xAxis.setLabelRotationAngle(-30f);
        lineChart.setExtraBottomOffset(24f);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.invalidate();
    }

    private void fetchDataFromServer(String url) {
        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);

                int code = conn.getResponseCode();
                if (code == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) builder.append(line);
                    reader.close();

                    JSONArray jsonArray = new JSONArray(builder.toString());
                    if (jsonArray.length() == 0) return;

                    List<HeartRateEntry> tempList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        int bpm = Integer.parseInt(obj.getString("bpm"));
                        long ts = Long.parseLong(obj.getString("ts"));
                        tempList.add(new HeartRateEntry(ts, bpm));
                    }

                    tempList.sort(Comparator.comparingLong(e -> e.timestamp));

                    runOnUiThread(() -> {
                        heartRateData.clear();
                        heartRateData.addAll(tempList);
                        updateChartAndUI();
                    });

                } else {
                    Log.e("HTTP", "Lỗi server: " + code);
                }
            } catch (Exception e) {
                Log.e("HTTP", "Lỗi: " + e.getMessage());
            }
        }).start();
    }

    private void connectToBluetoothDevice(String mac) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth không khả dụng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    REQUEST_BLUETOOTH_CONNECT);
            return;
        }

        for (BluetoothDevice device : adapter.getBondedDevices()) {
            if (device.getAddress().equals(mac)) {
                Toast.makeText(this, "Đã kết nối BLE: " + device.getName(), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Toast.makeText(this, "Không tìm thấy thiết bị BLE", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        autoUpdateHandler.removeCallbacks(autoUpdateTask);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_CONNECT &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            recreate();
        }
    }
}
