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

    private final Handler fetchHandler = new Handler();
    private final Runnable fetchRunnable = new Runnable() {
        @Override
        public void run() {
            fetchDataFromServer(serverUrl);
            fetchHandler.postDelayed(this, 300); // 0.3 giây
        }
    };

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

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.deviceRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnRefresh = findViewById(R.id.btnRefresh);
        btnRefresh.setText("Tìm kiếm thiết bị");
        btnRefresh.setOnClickListener(v -> loadBluetoothDevices());

        MyApp.getDeviceSession().setServer(serverUrl);
        fetchHandler.post(fetchRunnable);

        LinearLayout serverCard = findViewById(R.id.serverCard);
        serverCard.setOnClickListener(v -> {
            Animation clickAnim = AnimationUtils.loadAnimation(this, R.anim.click_scale);
            v.startAnimation(clickAnim);

            clickAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    runOnUiThread(() -> {
                        MyApp.getDeviceSession().setServer(serverUrl);
                        fetchDataFromServer(serverUrl);
                    });
                }

                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationRepeat(Animation animation) {}
            });
        });

        // ✅ Hiển thị thông tin server khả dụng
        TextView textServerName = findViewById(R.id.textServerName);
        TextView textServerStatus = findViewById(R.id.textServerStatus);

        // Đọc dữ liệu từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String address = prefs.getString("server_address", "Chưa có server");
        boolean connected = prefs.getBoolean("server_connected", false);

        // Gán hiển thị
        textServerName.setText("Địa chỉ: " + address);
        textServerStatus.setText("Tình trạng: " + (connected ? "đã kết nối" : "chưa kết nối"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fetchHandler.removeCallbacks(fetchRunnable);
        if (btConnector != null) {
            btConnector.disconnect();
        }
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

        adapter = new DeviceAdapter(deviceList, device -> {
            Toast.makeText(this, "Đang kết nối tới: " + device.address, Toast.LENGTH_SHORT).show();
            MyApp.getDeviceSession().setBluetooth(device.name, device.address);

            btConnector = new BluetoothConnector();
            btConnector.connect(device.address, new BluetoothConnector.Listener() {
                @Override
                public void onConnected() {
                    runOnUiThread(() -> Toast.makeText(HealthDetailActivity.this, "✅ Đã kết nối Bluetooth", Toast.LENGTH_SHORT).show());
                    btConnector.send("START"); // gửi lệnh mở gửi dữ liệu nếu cần
                }

                @Override
                public void onDataReceived(String data) {
                    Log.d("BLE-DATA", "Dữ liệu từ thiết bị: " + data);
                    // TODO: Nếu cần parse JSON tại đây
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> Toast.makeText(HealthDetailActivity.this, "❌ Lỗi Bluetooth: " + message, Toast.LENGTH_LONG).show());
                }
            });
        });

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadBluetoothDevices();
        }
    }

    private void fetchDataFromServer(String serverUrl) {
        new Thread(() -> {
            try {
                URL url = new URL(serverUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
                            ((TextView) findViewById(R.id.textHeartRate)).setText(heartRate + " nhịp/phút");
                            ((TextView) findViewById(R.id.textAcceleration)).setText(String.format("%.2f m/s²", accel));
                            ((TextView) findViewById(R.id.textX)).setText("x = " + x);
                            ((TextView) findViewById(R.id.textY)).setText("y = " + y);
                            ((TextView) findViewById(R.id.textZ)).setText("z = " + z);
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
}
