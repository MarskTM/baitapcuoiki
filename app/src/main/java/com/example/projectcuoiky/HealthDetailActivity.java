package com.example.projectcuoiky;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectcuoiky.adapter.DeviceAdapter;

import java.util.Arrays;
import java.util.List;

public class HealthDetailActivity extends AppCompatActivity {

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
        RecyclerView recyclerView = findViewById(R.id.deviceRecyclerView);
        List<String> devices = Arrays.asList("BLE Device 1", "BLE Device 2", "BLE Device 3");

        DeviceAdapter adapter = new DeviceAdapter(devices, selectedDevice -> {
            Toast.makeText(this, "Đã chọn thiết bị: " + selectedDevice, Toast.LENGTH_SHORT).show();
            // TODO: Xử lý cập nhật dữ liệu, vẽ biểu đồ tại đây
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Nút Làm mới dữ liệu
        Button btnRefresh = findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(v -> {
            // TODO: Làm mới dữ liệu từ thiết bị đã chọn
            Toast.makeText(this, "Đang làm mới dữ liệu...", Toast.LENGTH_SHORT).show();
        });
    }
}
