package com.example.projectcuoiky;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Ánh xạ view
        ImageView image = findViewById(R.id.detailImage);
        TextView name = findViewById(R.id.detailName);
        TextView price = findViewById(R.id.detailPrice);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        String plantName = intent.getStringExtra("plantName");
        String plantPrice = intent.getStringExtra("plantPrice");
        int plantImage = intent.getIntExtra("plantImage", R.drawable.ic_launcher_foreground);

        // Hiển thị lên giao diện
        name.setText(plantName);
        price.setText(plantPrice);
        image.setImageResource(plantImage);
    }
}
