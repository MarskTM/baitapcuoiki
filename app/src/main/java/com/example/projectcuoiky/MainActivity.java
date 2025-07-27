package com.example.projectcuoiky;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import com.example.projectcuoiky.adapter.PlantAdapter;
import com.example.projectcuoiky.model.PlantItem;
import com.example.projectcuoiky.SpaceItemDecoration;
import com.example.projectcuoiky.fragment.ProfileFragment;
import com.example.projectcuoiky.fragment.SettingFragment;
import com.example.projectcuoiky.session.DeviceSession;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayout homeButton, profileButton, settingButton;
    private View fragmentContainer;

    private ImageView homeIcon, profileIcon, settingIcon;
    private TextView homeText, profileText, settingText;

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

        // Khởi tạo view
        recyclerView = findViewById(R.id.recyclerView);
        fragmentContainer = findViewById(R.id.fragmentContainer);

        homeButton = findViewById(R.id.homeButton);
        profileButton = findViewById(R.id.profileButton);
        settingButton = findViewById(R.id.settingButton);

        homeIcon = findViewById(R.id.homeIcon);
        profileIcon = findViewById(R.id.profileIcon);
        settingIcon = findViewById(R.id.settingIcon);
        homeText = findViewById(R.id.homeText);
        profileText = findViewById(R.id.profileText);
        settingText = findViewById(R.id.settingText);

        // ✅ Tự động ghi nhớ server mặc định nếu chưa có
        if (MyApp.getDeviceSession().getType() == DeviceSession.Type.NONE) {
            String defaultServer = "http://192.168.2.6/getdata.php";
            MyApp.getDeviceSession().setServer(defaultServer);
            Log.d("SESSION", "Đang kết nối server tại " + defaultServer);

            // ✅ Lưu thông tin server đã kết nối vào SharedPreferences
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            prefs.edit()
                    .putString("server_address", defaultServer)
                    .putBoolean("server_connected", true)
                    .apply();
        }

        // Thiết lập RecyclerView cho tab Home
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        recyclerView.addItemDecoration(new SpaceItemDecoration(spacingInPixels));
        List<PlantItem> plantList = new ArrayList<>();
        plantList.add(new PlantItem("Sức khỏe", "1200", R.drawable.plant1));
        plantList.add(new PlantItem("Trái tim", "Unlocked", R.drawable.plant2));
        plantList.add(new PlantItem("Gia tốc", "2000", R.drawable.plant3));
        plantList.add(new PlantItem("Cảnh báo", "1200", R.drawable.plant4));
        PlantAdapter adapter = new PlantAdapter(this, plantList);
        recyclerView.setAdapter(adapter);

        // Mặc định hiển thị Home tab
        showHome();
        highlightTab(0);

        // Xử lý chuyển tab khi bấm nút dưới cùng
        homeButton.setOnClickListener(v -> {
            showHome();
            highlightTab(0);
        });
        profileButton.setOnClickListener(v -> {
            showFragment(new ProfileFragment());
            highlightTab(1);
        });
        settingButton.setOnClickListener(v -> {
            showFragment(new SettingFragment());
            highlightTab(2);
        });
    }

    private void showHome() {
        recyclerView.setVisibility(View.VISIBLE);
        fragmentContainer.setVisibility(View.GONE);
    }

    private void showFragment(Fragment fragment) {
        recyclerView.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void highlightTab(int tab) {
        int colorActive = getResources().getColor(android.R.color.white);
        int colorInactive = getResources().getColor(R.color.gray_inactive);

        homeIcon.setColorFilter(tab == 0 ? colorActive : colorInactive);
        homeText.setTextColor(tab == 0 ? colorActive : colorInactive);

        profileIcon.setColorFilter(tab == 1 ? colorActive : colorInactive);
        profileText.setTextColor(tab == 1 ? colorActive : colorInactive);

        settingIcon.setColorFilter(tab == 2 ? colorActive : colorInactive);
        settingText.setTextColor(tab == 2 ? colorActive : colorInactive);
    }
}
