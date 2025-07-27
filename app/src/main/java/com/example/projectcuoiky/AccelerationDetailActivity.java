package com.example.projectcuoiky;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.List;
import java.util.Locale;

public class AccelerationDetailActivity extends AppCompatActivity {

    private LineChart chart;
    private TextView textAccel, textStatus, textX, textY, textZ;
    private TextView deviceName, deviceAddress, deviceMethod;

    private final List<Entry> entries = new ArrayList<>();
    private final List<String> labels = new ArrayList<>();
    private final Handler handler = new Handler();
    private Runnable fetchTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceleration_detail);

        // Back button
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // View binding
        chart = findViewById(R.id.graph);
        textAccel = findViewById(R.id.acceleration_value);
        textStatus = findViewById(R.id.accelStatus);
        textX = findViewById(R.id.textAccelX);
        textY = findViewById(R.id.textAccelY);
        textZ = findViewById(R.id.textAccelZ); // ✅ added

        deviceName = findViewById(R.id.deviceName);
        deviceAddress = findViewById(R.id.deviceAddress);
        deviceMethod = findViewById(R.id.deviceMethod);

        // Lấy thông tin server mặc định
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String serverUrl = prefs.getString("server_address", "http://192.168.2.6/getdata.php");

        // Cập nhật thông tin BLE nếu có
        DeviceSession session = MyApp.getDeviceSession();
        if (session.getType() == DeviceSession.Type.BLUETOOTH) {
            deviceName.setText("• Tên thiết bị: " + session.getBluetoothDeviceName());
            deviceAddress.setText("• Địa chỉ: " + session.getBluetoothMac());
            deviceMethod.setText("• Phương thức: Bluetooth");
        } else {
            deviceName.setText("• Tên thiết bị: " + serverUrl);
            deviceAddress.setText("• Địa chỉ: Server từ xa");
            deviceMethod.setText("• Phương thức: HTTP");
        }

        // Bắt đầu fetch dữ liệu định kỳ
        startAutoFetch(serverUrl);
    }

    private void startAutoFetch(String url) {
        fetchTask = () -> {
            fetchData(url);
            handler.postDelayed(fetchTask, 3000);
        };
        handler.post(fetchTask);
    }

    private void fetchData(String url) {
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

                    JSONArray arr = new JSONArray(builder.toString());
                    List<Entry> tempEntries = new ArrayList<>();
                    List<String> tempLabels = new ArrayList<>();

                    for (int i = arr.length() - 1; i >= 0; i--) {
                        JSONObject obj = arr.getJSONObject(i);
                        double x = obj.getDouble("x");
                        double y = obj.getDouble("y");
                        double z = obj.getDouble("z");
                        double a = Math.sqrt(x * x + y * y + z * z);
                        long ts = Long.parseLong(obj.getString("ts"));

                        int index = arr.length() - 1 - i; // đảo chiều index

                        tempEntries.add(new Entry(index, (float) a));
                        tempLabels.add(new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(ts));

                        // Hiển thị giá trị mới nhất (dòng cuối cùng trong JSON)
                        if (i == arr.length() - 1) {
                            double accel = a;
                            runOnUiThread(() -> {
                                textAccel.setText(String.format("%.2f m/s²", accel));
                                textX.setText("• Tọa độ X: " + x);
                                textY.setText("• Tọa độ Y: " + y);
                                textZ.setText("• Tọa độ Z: " + z);
                                textStatus.setText("📈 Tình trạng: " + (accel > 15 ? "Cảnh báo" : "Bình thường"));
                                textStatus.setTextColor(accel > 15 ? Color.RED : Color.parseColor("#4CAF50"));
                            });
                        }
                    }

                    runOnUiThread(() -> {
                        entries.clear();
                        labels.clear();
                        entries.addAll(tempEntries);
                        labels.addAll(tempLabels);
                        updateChart();
                    });
                }

            } catch (Exception e) {
                Log.e("FETCH", "Lỗi: " + e.getMessage());
            }
        }).start();
    }


    private void updateChart() {
        LineDataSet dataSet = new LineDataSet(entries, "Gia tốc");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        chart.setData(new LineData(dataSet));

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        // ✅ Giới hạn và xoay nhãn
        xAxis.setLabelCount(Math.min(labels.size(), 5), false); // tối đa 5 nhãn
        xAxis.setLabelRotationAngle(-30f); // xoay chữ

        chart.setExtraBottomOffset(24f); // ✅ đây là dòng quan trọng
        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.invalidate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(fetchTask);
    }
}
