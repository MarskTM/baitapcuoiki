package com.example.projectcuoiky;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectcuoiky.adapter.DeviceAdapter;
import com.example.projectcuoiky.session.DeviceSession;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class HealthDetailActivity extends AppCompatActivity {

    private final String serverUrl = "http://192.168.2.6/getdata.php";
    private final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private DeviceAdapter adapter;
    private RecyclerView recyclerView;
    private Button btnRefresh;
    private BluetoothSocket bluetoothSocket;
    private BluetoothConnector btConnector;

    private Handler fetchHandler = new Handler();
    private Runnable fetchRunnable;

    private TextView textHeartRate, textAcceleration, textX, textY, textZ;
    private TextView textSourceInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_health_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khai báo TextView hiển thị thông số
        textHeartRate = findViewById(R.id.textHeartRate);
        textAcceleration = findViewById(R.id.textAcceleration);
        textX = findViewById(R.id.textX);
        textY = findViewById(R.id.textY);
        textZ = findViewById(R.id.textZ);

        // TextView hiển thị nguồn dữ liệu
        textSourceInfo = findViewById(R.id.deviceName); // Có thể sửa id nếu muốn label riêng cho "nguồn dữ liệu"

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.deviceRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnRefresh = findViewById(R.id.btnRefresh);
        btnRefresh.setText("Tìm kiếm thiết bị");
        btnRefresh.setOnClickListener(v -> loadBluetoothDevices());

        // Show thông tin server
        LinearLayout serverCard = findViewById(R.id.serverCard);
        serverCard.setOnClickListener(v -> {
            Animation clickAnim = AnimationUtils.loadAnimation(this, R.anim.click_scale);
            v.startAnimation(clickAnim);
            clickAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    runOnUiThread(() -> {
                        // Set source là server khi user chọn
                        MyApp.getDeviceSession().setType(DeviceSession.Type.SERVER);
                        showSourceInfo();
                        startServerAutoFetch(serverUrl);
                    });
                }
                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationRepeat(Animation animation) {}
            });
        });

        // Hiển thị tên server
        TextView textServerName = findViewById(R.id.textServerName);
        TextView textServerStatus = findViewById(R.id.textServerStatus);
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String address = prefs.getString("server_address", "Chưa có server");
        boolean connected = prefs.getBoolean("server_connected", false);
        textServerName.setText("Địa chỉ: " + address);
        textServerStatus.setText("Tình trạng: " + (connected ? "đã kết nối" : "chưa kết nối"));

        // Xử lý chọn nguồn dữ liệu ngay khi vào màn hình
        showSourceInfo();
        DeviceSession session = MyApp.getDeviceSession();

        if (session != null && session.getType() == DeviceSession.Type.SERVER) {
            startServerAutoFetch(serverUrl);
        } else if (session != null && session.getType() == DeviceSession.Type.BLUETOOTH) {
            connectToBluetoothDevice(session.getBluetoothMac());
        } else {
            // Nếu chưa chọn, mặc định là server nếu có kết nối
            if (connected) {
                MyApp.getDeviceSession().setType(DeviceSession.Type.SERVER);
                showSourceInfo();
                startServerAutoFetch(address);
            } else {
                Toast.makeText(this, "Chưa có thiết bị kết nối", Toast.LENGTH_SHORT).show();
            }
        }

        // Luôn hiển thị danh sách thiết bị
        loadBluetoothDevices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fetchRunnable != null) fetchHandler.removeCallbacks(fetchRunnable);
        if (btConnector != null) {
            btConnector.disconnect();
        }
    }

    // Hàm hiển thị nguồn dữ liệu đang chọn cho user
    private void showSourceInfo() {
        DeviceSession session = MyApp.getDeviceSession();
        if (session != null && session.getType() == DeviceSession.Type.SERVER) {
            textSourceInfo.setText("Nguồn dữ liệu: Server");
        } else if (session != null && session.getType() == DeviceSession.Type.BLUETOOTH) {
            textSourceInfo.setText("Nguồn dữ liệu: Thiết bị BLE");
        } else {
            textSourceInfo.setText("Nguồn dữ liệu: Chưa chọn");
        }
    }

    // Lấy dữ liệu server lặp lại 0.5 giây/lần
    private void startServerAutoFetch(String url) {
        if (fetchRunnable != null) fetchHandler.removeCallbacks(fetchRunnable);
        fetchRunnable = () -> {
            fetchDataFromServer(url);
            fetchHandler.postDelayed(fetchRunnable, 500);
        };
        fetchHandler.post(fetchRunnable);
    }

    private void loadBluetoothDevices() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth đang tắt hoặc không khả dụng", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1001);
                return;
            }
        }

        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        List<DeviceAdapter.DeviceInfo> deviceList = new ArrayList<>();
        for (BluetoothDevice device : bondedDevices) {
            String name = device.getName();
            String address = device.getAddress();
            deviceList.add(new DeviceAdapter.DeviceInfo(name, address));
        }

        // Đọc cache thiết bị BLE
        List<DeviceSession.BluetoothDeviceInfo> cachedDevices = DeviceSession.getBLEDevices(this);
        List<DeviceAdapter.DeviceInfo> allDevices = new ArrayList<>();

        for (DeviceSession.BluetoothDeviceInfo dev : cachedDevices) {
            boolean exists = false;
            for (DeviceAdapter.DeviceInfo d : allDevices) {
                if (d.address.equals(dev.mac)) {
                    exists = true; break;
                }
            }
            if (!exists)
                allDevices.add(new DeviceAdapter.DeviceInfo(dev.name, dev.mac));
        }
        for (DeviceAdapter.DeviceInfo d : deviceList) {
            boolean exists = false;
            for (DeviceAdapter.DeviceInfo a : allDevices) {
                if (a.address.equals(d.address)) {
                    exists = true; break;
                }
            }
            if (!exists)
                allDevices.add(d);
        }

        // Adapter nhận vào callback khi chọn BLE
        adapter = new DeviceAdapter(allDevices, device -> {
            Toast.makeText(this, "Đang kết nối tới: " + device.address, Toast.LENGTH_SHORT).show();
            MyApp.getDeviceSession().setBluetooth(device.name, device.address);
            MyApp.getDeviceSession().setType(DeviceSession.Type.BLUETOOTH);
            showSourceInfo();
            DeviceSession.addBLEDevice(this, device.name, device.address);

            // Nếu đang lấy dữ liệu từ server thì dừng lại
            if (fetchRunnable != null) fetchHandler.removeCallbacks(fetchRunnable);

            connectToBluetoothDevice(device.address);
        });
        recyclerView.setAdapter(adapter);
    }

    // Kết nối BLE và giả lập nhận dữ liệu BLE liên tục
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
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1001);
            return;
        }

        // Tìm thiết bị đã pair (giả lập demo)
        BluetoothDevice foundDevice = null;
        for (BluetoothDevice device : adapter.getBondedDevices()) {
            if (device.getAddress().equals(mac)) {
                foundDevice = device;
                break;
            }
        }
        if (foundDevice == null) {
            Toast.makeText(this, "Không tìm thấy thiết bị BLE", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Đã kết nối BLE: " + foundDevice.getName(), Toast.LENGTH_SHORT).show();

        // --- DEMO: Giả lập BLE gửi data 0.5 giây/lần ---
        if (fetchRunnable != null) fetchHandler.removeCallbacks(fetchRunnable);
        fetchRunnable = () -> {
            // Ở đây bạn phải thay bằng code nhận dữ liệu thật từ BLE
            // Ví dụ: String data = nhận từ BLE;
            // Dưới đây là demo JSON nhận được:
            String fakeData = "{\"bpm\": 0, \"x\": 0.0, \"y\": 0.00, \"z\": -0.00}";
            handleReceivedBleData(fakeData);
            fetchHandler.postDelayed(fetchRunnable, 500);
        };
        fetchHandler.post(fetchRunnable);
    }

    // Xử lý nhận dữ liệu BLE
    private void handleReceivedBleData(String data) {
        try {
            JSONObject json = new JSONObject(data);
            int heartRate = json.optInt("bpm", -1);
            double x = json.optDouble("x", 0);
            double y = json.optDouble("y", 0);
            double z = json.optDouble("z", 0);
            double accel = Math.sqrt(x * x + y * y + z * z);

            runOnUiThread(() -> {
                // CHỈ cập nhật UI nếu đang chọn nguồn BLE!
                if (MyApp.getDeviceSession().getType() == DeviceSession.Type.BLUETOOTH) {
                    if (heartRate != -1) {
                        textHeartRate.setText(heartRate + " nhịp/phút");
                    }
                    textAcceleration.setText(String.format("%.2f m/s²", accel));
                    textX.setText("x = " + x);
                    textY.setText("y = " + y);
                    textZ.setText("z = " + z);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("BLE-DATA", "Lỗi parse dữ liệu: " + e.getMessage());
        }
    }

    // Nhận dữ liệu từ server
    private void fetchDataFromServer(String url) {
        new Thread(() -> {
            try {
                URL u = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
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

                    JSONArray jsonArray = new JSONArray(response.toString());
                    if (jsonArray.length() > 0) {
                        JSONObject json = jsonArray.getJSONObject(0);
                        int heartRate = json.getInt("bpm");
                        double x = json.getDouble("x");
                        double y = json.getDouble("y");
                        double z = json.getDouble("z");
                        double accel = Math.sqrt(x * x + y * y + z * z);

                        runOnUiThread(() -> {
                            // CHỈ cập nhật UI nếu đang chọn nguồn SERVER!
                            if (MyApp.getDeviceSession().getType() == DeviceSession.Type.SERVER) {
                                textHeartRate.setText(heartRate + " nhịp/phút");
                                textAcceleration.setText(String.format("%.2f m/s²", accel));
                                textX.setText("x = " + x);
                                textY.setText("y = " + y);
                                textZ.setText("z = " + z);
                            }
                        });
                    }
                } else {
                    Log.e("HTTP", "Server response: " + responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("HTTP", "Error: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadBluetoothDevices();
        }
    }
}
