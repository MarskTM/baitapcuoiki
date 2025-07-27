package com.example.projectcuoiky;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.LineChart;

import com.example.projectcuoiky.MyApp;
import com.example.projectcuoiky.session.DeviceSession;

public class AccelerationDetailActivity extends AppCompatActivity {

    TextView accelerationValue;
    LineChart lineChart;
    CheckBox checkboxStable;
    Button btnRefresh;
    ImageView btnBack;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_acceleration_detail);

        // Lấy thông tin vẽ đồ thị gia tốc
        DeviceSession session = MyApp.getDeviceSession();

        if (session.getType() == DeviceSession.Type.SERVER) {
            fetchFromServer(session.getServerUrl());
        } else if (session.getType() == DeviceSession.Type.BLUETOOTH) {
            connectToBLE(session.getBluetoothMac());
        } else {
            Toast.makeText(this, "Không có kết nối thiết bị", Toast.LENGTH_SHORT).show();
        }

        // Set padding để tránh bị che status bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ view
        accelerationValue = findViewById(R.id.acceleration_value);
        lineChart = findViewById(R.id.graph);
        btnBack = findViewById(R.id.btnBack);

        // Gán dữ liệu demo
        accelerationValue.setText("9.8 m/s²");

        // Xử lý nút Back
        btnBack.setOnClickListener(v -> finish());
    }

    private void fetchFromServer(String url) {  }
    private void connectToBLE(String mac) {  }
}
