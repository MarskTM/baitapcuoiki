package com.example.projectcuoiky;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Arrays;
import java.util.List;

import com.example.projectcuoiky.MyApp;
import com.example.projectcuoiky.session.DeviceSession;

public class AlertDetailActivity extends AppCompatActivity {

    LinearLayout warningContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alert_detail);

        // Lấy thông tin thiết bị kết nối để nhận cảnh báo
        DeviceSession session = MyApp.getDeviceSession();

        if (session.getType() == DeviceSession.Type.SERVER) {
            checkFallFromServer(session.getServerUrl());
        } else if (session.getType() == DeviceSession.Type.BLUETOOTH) {
            readFallSensorFromBLE(session.getBluetoothMac());
        }

        // Apply padding để không bị che
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ container chứa danh sách card
        warningContainer = findViewById(R.id.warningContainer);

        // Dữ liệu mẫu cảnh báo
        List<WarningItem> warnings = Arrays.asList(
                new WarningItem("120", "Nhịp tim cao vượt ngưỡng an toàn!", "3 tháng 4 18:33"),
                new WarningItem("65", "Nhịp tim thấp bất thường!", "6 tháng 7 08:22"),
                new WarningItem("110", "Tăng nhẹ, cần theo dõi!", "15 tháng 2 17:40")
        );

        // Hiển thị từng card cảnh báo
        for (WarningItem item : warnings) {
            View card = LayoutInflater.from(this).inflate(R.layout.item_warning_card, warningContainer, false);
            ((TextView) card.findViewById(R.id.textHeartRate)).setText(item.heartRate + " nhịp/phút");
            ((TextView) card.findViewById(R.id.textTime)).setText(item.time);
            ((TextView) card.findViewById(R.id.textWarning)).setText(item.message);
            warningContainer.addView(card);
        }
    }

    // Lớp dữ liệu cảnh báo
    public static class WarningItem {
        public String heartRate;
        public String message;
        public String time;

        public WarningItem(String heartRate, String message, String time) {
            this.heartRate = heartRate;
            this.message = message;
            this.time = time;
        }
    }

    private void checkFallFromServer(String url) {  }
    private void readFallSensorFromBLE(String mac) {  }
}
