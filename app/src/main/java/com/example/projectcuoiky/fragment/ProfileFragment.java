package com.example.projectcuoiky.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.projectcuoiky.MyApp;
import com.example.projectcuoiky.R;
import com.example.projectcuoiky.session.UserSession;

import org.json.JSONObject;

public class ProfileFragment extends Fragment {

    private TextView tvUsername, tvRole;
    private EditText etNewUsername, etNewPassword;
    private Spinner spnNewRole;
    private Button btnCreateUser;
    private LinearLayout registerFormLayout;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lấy thông tin từ session
        UserSession session = MyApp.getUserSession();
        String username = session.getName();
        String role = session.getRole();

        // Ánh xạ view
        tvUsername = view.findViewById(R.id.tvUsername);
        tvRole = view.findViewById(R.id.tvRole);
        registerFormLayout = view.findViewById(R.id.registerFormLayout);
        etNewUsername = view.findViewById(R.id.etNewUsername);
        etNewPassword = view.findViewById(R.id.etNewPassword);
        spnNewRole = view.findViewById(R.id.spnNewRole);
        btnCreateUser = view.findViewById(R.id.btnCreateUser);

        // Hiển thị thông tin người dùng
        tvUsername.setText("👤 Tài khoản: " + username);
        tvRole.setText("🎖️ Vai trò: " + role);

        // Gán dữ liệu cho Spinner vai trò
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.roles_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnNewRole.setAdapter(adapter);

        // Hiển thị form nếu là admin
        if ("admin".equalsIgnoreCase(role)) {
            registerFormLayout.setVisibility(View.VISIBLE);
        } else {
            registerFormLayout.setVisibility(View.GONE);
        }

        // Xử lý tạo tài khoản mới
        btnCreateUser.setOnClickListener(v -> {
            String newUsername = etNewUsername.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String newRole = spnNewRole.getSelectedItem().toString().toLowerCase();

            if (newUsername.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject data = new JSONObject();
                data.put("username", newUsername);
                data.put("password", newPassword);
                data.put("role", newRole);
                data.put("creator_role", "admin");

                String url = "http://192.168.2.6/register.php";
                RequestQueue queue = Volley.newRequestQueue(requireContext());

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data,
                        response -> {
                            if (response.optBoolean("success", false)) {
                                Toast.makeText(getContext(), "Tạo tài khoản thành công!", Toast.LENGTH_SHORT).show();
                                etNewUsername.setText("");
                                etNewPassword.setText("");
                                spnNewRole.setSelection(0);
                            } else {
                                Toast.makeText(getContext(), response.optString("message", "Lỗi không xác định"), Toast.LENGTH_LONG).show();
                            }
                        },
                        error -> {
                            error.printStackTrace();
                            Toast.makeText(getContext(), "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
                        });

                queue.add(request);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
