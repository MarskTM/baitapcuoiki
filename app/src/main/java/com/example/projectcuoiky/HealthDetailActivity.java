package com.example.projectcuoiky;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HealthDetailActivity extends AppCompatActivity {

    private DeviceAdapter adapter;
    private RecyclerView recyclerView;
    private Button btnRefresh;

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

        // Nút quay lại
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Khởi tạo RecyclerView
        recyclerView = findViewById(R.id.deviceRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Nút tìm kiếm thiết bị
        btnRefresh = findViewById(R.id.btnRefresh);
        btnRefresh.setText("Tìm kiếm thiết bị");
        btnRefresh.setOnClickListener(v -> loadBluetoothDevices());
    }

    private void loadBluetoothDevices() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth đang tắt hoặc không khả dụng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Android 12+ yêu cầu quyền BLUETOOTH_CONNECT
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
            Toast.makeText(this, "Đã chọn thiết bị: " + (device.name != null ? device.name : device.address), Toast.LENGTH_SHORT).show();
            // TODO: Xử lý thêm khi chọn thiết bị
        });

        recyclerView.setAdapter(adapter);
    }

    // Xử lý khi người dùng cấp quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadBluetoothDevices();
        }
    }
}
