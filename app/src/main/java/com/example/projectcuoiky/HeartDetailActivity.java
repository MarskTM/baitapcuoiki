package com.example.projectcuoiky;

import com.example.projectcuoiky.MyApp;
import com.example.projectcuoiky.session.DeviceSession;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

public class HeartDetailActivity extends AppCompatActivity {

    private static final int REQUEST_BLUETOOTH_CONNECT = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_detail);

        // Nút back
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // View liên quan đến nhịp tim
        TextView textHeartRate = findViewById(R.id.textHeartRate);
        TextView textStatus = findViewById(R.id.textStatus);
        TextView textTrend = findViewById(R.id.textTrend);

        // View BLE
        TextView textBleName = findViewById(R.id.textBleName);
        TextView textBleAddress = findViewById(R.id.textBleAddress);
        TextView textBleMethod = findViewById(R.id.textBleMethod);

        // --------- Lấy thông tin ĐỒ THỊ ---------
        DeviceSession session = MyApp.getDeviceSession();

        if (session.getType() == DeviceSession.Type.SERVER) {
            String url = session.getServerUrl();
            fetchDataFromServer(url); // gọi hàm fetch server
        } else if (session.getType() == DeviceSession.Type.BLUETOOTH) {
            String name = session.getBluetoothDeviceName();
            String mac = session.getBluetoothMac();
            connectToBluetoothDevice(mac); // gọi hàm xử lý BLE
        } else {
            Toast.makeText(this, "Chưa có thiết bị kết nối", Toast.LENGTH_SHORT).show();
        }

        // --------- DỮ LIỆU NHỊP TIM MẪU ---------
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 98));
        entries.add(new Entry(1, 101));
        entries.add(new Entry(2, 103));
        entries.add(new Entry(3, 100));
        entries.add(new Entry(4, 105));

        int currentHeartRate = (int) entries.get(entries.size() - 1).getY();
        textHeartRate.setText(currentHeartRate + " nhịp/phút");

        // Phân loại tình trạng
        if (currentHeartRate < 60) {
            textStatus.setText("❤️ Tình trạng: Rối loạn nhẹ");
        } else if (currentHeartRate <= 100) {
            textStatus.setText("❤️ Tình trạng: Bình thường");
        } else {
            textStatus.setText("❤️ Tình trạng: Cảnh báo");
        }

        // Phân loại xu hướng
        int prev = (int) entries.get(entries.size() - 2).getY();
        int diff = currentHeartRate - prev;

        if (diff >= 3) {
            textTrend.setText("📈 Xu hướng: Tăng nhẹ");
        } else if (diff <= -3) {
            textTrend.setText("📈 Xu hướng: Giảm dần");
        } else {
            textTrend.setText("📈 Xu hướng: Ổn định");
        }

        // --------- HIỂN THỊ THIẾT BỊ BLE ---------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        REQUEST_BLUETOOTH_CONNECT);
                return;
            }
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();

            if (bondedDevices != null && bondedDevices.size() > 0) {
                BluetoothDevice device = bondedDevices.iterator().next();

                String name = device.getName();
                String address = device.getAddress();

                textBleName.setText("• Tên thiết bị: " + (name != null ? name : "Không rõ"));
                textBleAddress.setText("• Địa chỉ: " + address);
                textBleMethod.setText("• Phương thức: Bluetooth Classic");
            } else {
                textBleName.setText("• Tên thiết bị: Không tìm thấy");
                textBleAddress.setText("• Địa chỉ: --");
                textBleMethod.setText("• Phương thức: Không kết nối");
            }
        } else {
            textBleName.setText("• Tên thiết bị: Bluetooth đang tắt");
            textBleAddress.setText("• Địa chỉ: --");
            textBleMethod.setText("• Phương thức: Không kết nối");
        }

        // --------- VẼ ĐỒ THỊ ---------
        LineChart lineChart = findViewById(R.id.lineChart);

        Calendar now = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String[] timeLabels = new String[5];
        for (int i = 0; i < 5; i++) {
            Calendar cal = (Calendar) now.clone();
            cal.add(Calendar.HOUR_OF_DAY, i - 2);
            timeLabels[i] = sdf.format(cal.getTime());
        }

        LineDataSet dataSet = new LineDataSet(entries, "Nhịp tim");
        dataSet.setColor(getResources().getColor(android.R.color.holo_red_dark));
        dataSet.setCircleColor(getResources().getColor(android.R.color.holo_red_dark));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(timeLabels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(timeLabels.length, true);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.invalidate(); // refresh chart

    }

    // --- Xử lý kết quả khi người dùng cấp quyền ---
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_CONNECT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate(); // chạy lại activity khi đã có quyền
            }
        }
    }

    private void fetchDataFromServer(String url) {
        new Thread(() -> {
            try {
                URL serverUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) serverUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    JSONObject json = new JSONObject(response.toString());

                    int heartRate = json.getInt("heartRate");
                    double accel = json.getDouble("acceleration");
                    double x = json.getDouble("x");
                    double y = json.getDouble("y");
                    boolean fall = json.getBoolean("fall");

                    runOnUiThread(() -> {
                        // Cập nhật lên giao diện (bạn có thể tuỳ chỉnh theo màn hiện tại)
                        ((TextView) findViewById(R.id.textHeartRate)).setText(heartRate + " nhịp/phút");
                        ((TextView) findViewById(R.id.textAcceleration)).setText(accel + " m/s²");
                        ((TextView) findViewById(R.id.textX)).setText("x = " + x);
                        ((TextView) findViewById(R.id.textY)).setText("y = " + y);

                        TextView fallText = findViewById(R.id.textFallStatus);
                        fallText.setText(fall ? "Phát hiện té ngã" : "Không có té ngã");
                        fallText.setTextColor(fall ? Color.RED : Color.parseColor("#4CAF50"));
                    });

                } else {
                    Log.e("HTTP", "Lỗi kết nối server: " + responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("HTTP", "Lỗi: " + e.getMessage());
            }
        }).start();
    }

    private void connectToBluetoothDevice(String mac) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth không khả dụng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1001);
            return;
        }

        BluetoothDevice targetDevice = null;
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
            if (device.getAddress().equals(mac)) {
                targetDevice = device;
                break;
            }
        }

        if (targetDevice == null) {
            Toast.makeText(this, "Không tìm thấy thiết bị BLE", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Đây là nơi bạn thực hiện kết nối GATT hoặc custom BLE protocol của bạn
        Toast.makeText(this, "Đã tìm thấy BLE: " + targetDevice.getName(), Toast.LENGTH_SHORT).show();

        // TODO: Thêm xử lý kết nối GATT tại đây nếu cần
    }
}
