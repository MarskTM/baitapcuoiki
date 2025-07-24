package com.example.projectcuoiky.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.projectcuoiky.LoginActivity;
import com.example.projectcuoiky.R;

public class SettingFragment extends Fragment {
    private boolean isAdmin = false; // test đổi sang true/false

    public SettingFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_setting, container, false);

        LinearLayout registerForm = v.findViewById(R.id.registerUserForm);
        LinearLayout normalLayout = v.findViewById(R.id.normalLayout);

        if (isAdmin) {
            registerForm.setVisibility(View.VISIBLE);
            normalLayout.setVisibility(View.GONE);
        } else {
            registerForm.setVisibility(View.GONE);
            normalLayout.setVisibility(View.VISIBLE);

            // 1. Set thông tin thiết bị
            TextView deviceNameText = v.findViewById(R.id.deviceNameText);
            TextView osText = v.findViewById(R.id.osText);
            TextView screenText = v.findViewById(R.id.screenText);

            deviceNameText.setText("Tên thiết bị: " + Build.MANUFACTURER + " " + Build.MODEL);
            osText.setText("Hệ điều hành: Android " + Build.VERSION.RELEASE);

            // Lấy kích thước màn hình (pixel)
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int width = metrics.widthPixels;
            int height = metrics.heightPixels;
            screenText.setText("Kích thước màn hình: " + width + " x " + height + " px");

            // 2. Bắt sự kiện switch
            Switch bluetoothSwitch = v.findViewById(R.id.bluetoothSwitch);
            Switch notifySwitch = v.findViewById(R.id.notifySwitch);

            bluetoothSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Toast.makeText(getActivity(),
                        isChecked ? "Bluetooth: BẬT truyền dữ liệu" : "Bluetooth: TẮT truyền dữ liệu",
                        Toast.LENGTH_SHORT).show();
                // Thêm xử lý logic Bluetooth nếu muốn
            });

            notifySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Toast.makeText(getActivity(),
                        isChecked ? "Đã bật thông báo" : "Đã tắt thông báo",
                        Toast.LENGTH_SHORT).show();
                // Thêm xử lý gửi notification thực tế nếu muốn
            });

            // 3. Đăng xuất
            Button logoutButton = v.findViewById(R.id.logoutButton);
            logoutButton.setOnClickListener(view -> {
                // Xử lý logic đăng xuất (xoá thông tin đăng nhập nếu có)
                Toast.makeText(getActivity(), "Đã đăng xuất!", Toast.LENGTH_SHORT).show();

                // Chuyển về LoginActivity và clear task stack để không quay lại MainActivity bằng nút back
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                // Optional: kết thúc MainActivity nếu cần (nếu đang mở qua Activity chứ không phải Fragment)
                getActivity().finish();
            });
        }
        return v;
    }
}
