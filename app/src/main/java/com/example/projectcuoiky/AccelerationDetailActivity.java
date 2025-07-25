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

        // Set padding để tránh bị che status bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ view
        accelerationValue = findViewById(R.id.acceleration_value);
        lineChart = findViewById(R.id.graph);
        checkboxStable = findViewById(R.id.checkboxStable);
        btnRefresh = findViewById(R.id.btn_refresh);
        btnBack = findViewById(R.id.btnBack);

        // Gán dữ liệu demo
        accelerationValue.setText("9.8 m/s²");
        checkboxStable.setChecked(true);

        // Xử lý nút Làm mới
        btnRefresh.setOnClickListener(v -> {
            Toast.makeText(this, "Đã làm mới giao diện!", Toast.LENGTH_SHORT).show();
            // TODO: Thêm logic cập nhật dữ liệu ở đây nếu có
        });

        // Xử lý nút Back
        btnBack.setOnClickListener(v -> finish());
    }
}
