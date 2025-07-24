package com.example.projectcuoiky;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

import com.example.projectcuoiky.adapter.PlantAdapter;
import com.example.projectcuoiky.model.PlantItem;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.projectcuoiky.SpaceItemDecoration;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        // ✅ Sử dụng GridLayoutManager với 2 cột
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // ✅ Thêm khoảng cách giữa các item
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        recyclerView.addItemDecoration(new SpaceItemDecoration(spacingInPixels));

        // ✅ Dữ liệu mẫu
        List<PlantItem> plantList = new ArrayList<>();
        plantList.add(new PlantItem("Geraniums", "1200", R.drawable.plant1));
        plantList.add(new PlantItem("Jacaranda", "Unlocked", R.drawable.plant2));
        plantList.add(new PlantItem("Golden Trumpet", "2000", R.drawable.plant3));
        plantList.add(new PlantItem("Tangerine Tree", "1200", R.drawable.plant4));

        // Gán adapter
        PlantAdapter adapter = new PlantAdapter(this, plantList);
        recyclerView.setAdapter(adapter);
    }
}

