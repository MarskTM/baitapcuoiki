package com.example.projectcuoiky;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HeartDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_detail);

        // Xử lý nút back
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Hiển thị dữ liệu khác
        TextView textHeartRate = findViewById(R.id.textHeartRate);
        TextView textDevice = findViewById(R.id.textDevice);
        CheckBox checkboxStable = findViewById(R.id.checkboxStable);

        textHeartRate.setText("103 nhịp/phút");
        textDevice.setText("- Device 1");
        checkboxStable.setChecked(true);

        // --------- VẼ ĐỒ THỊ ---------
        LineChart lineChart = findViewById(R.id.lineChart);

        // 1. Tạo mốc thời gian từ hiện tại -2h đến +2h
        Calendar now = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String[] timeLabels = new String[5];
        for (int i = 0; i < 5; i++) {
            Calendar cal = (Calendar) now.clone();
            cal.add(Calendar.HOUR_OF_DAY, i - 2); // -2h, -1h, now, +1h, +2h
            timeLabels[i] = sdf.format(cal.getTime());
        }

        // 2. Dữ liệu mẫu tương ứng
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 98));
        entries.add(new Entry(1, 101));
        entries.add(new Entry(2, 103));
        entries.add(new Entry(3, 100));
        entries.add(new Entry(4, 105));

        // 3. Tạo DataSet
        LineDataSet dataSet = new LineDataSet(entries, "Nhịp tim");
        dataSet.setColor(getResources().getColor(android.R.color.holo_red_dark));
        dataSet.setCircleColor(getResources().getColor(android.R.color.holo_red_dark));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);

        // 4. Set Data cho Chart
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // 5. Cấu hình trục X (mốc giờ)
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(timeLabels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(timeLabels.length, true);

        // 6. Cấu hình thêm
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);

        // 7. Refresh chart
        lineChart.invalidate();
    }
}
