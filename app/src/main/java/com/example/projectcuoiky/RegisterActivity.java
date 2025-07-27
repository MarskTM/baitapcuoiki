package com.example.projectcuoiky;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        EditText etUsername = findViewById(R.id.etUsername);
        EditText etPassword = findViewById(R.id.etPassword);
        EditText etConfirm = findViewById(R.id.etConfirmPassword);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirm.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject json = new JSONObject();
            try {
                json.put("username", username);
                json.put("password", password);
                json.put("role", "user");
                json.put("creator_role", "user");

                String url = "http://192.168.2.6/register.php";
                RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, json,
                        response -> {
                            try {
                                if (response.getBoolean("success")) {
                                    JSONObject user = response.getJSONObject("user");

                                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(RegisterActivity.this);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putInt("user_id", user.getInt("id"));
                                    editor.putString("username", user.getString("username"));
                                    editor.putString("role", user.getString("role"));
                                    editor.apply();

                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Lỗi xử lý phản hồi", Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> {
                            error.printStackTrace();
                            Toast.makeText(this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
                        });

                queue.add(request);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static class BluetoothConnector {
        private BluetoothSocket socket;
        private InputStream inputStream;
        private OutputStream outputStream;
        private final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        public interface Listener {
            void onConnected();
            void onDataReceived(String data);
            void onError(String message);
        }

        public void connect(String address, Listener listener) {
            new Thread(() -> {
                try {
                    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice device = adapter.getRemoteDevice(address);
                    socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                    socket.connect();

                    inputStream = socket.getInputStream();
                    outputStream = socket.getOutputStream();

                    listener.onConnected();

                    byte[] buffer = new byte[1024];
                    int bytes;
                    while ((bytes = inputStream.read(buffer)) != -1) {
                        String received = new String(buffer, 0, bytes);
                        listener.onDataReceived(received);
                    }

                } catch (Exception e) {
                    Log.e("BluetoothConnector", "Error", e);
                    listener.onError(e.getMessage());
                    disconnect();
                }
            }).start();
        }

        public void send(String message) {
            try {
                if (outputStream != null) {
                    outputStream.write(message.getBytes());
                }
            } catch (Exception e) {
                Log.e("BluetoothConnector", "Send failed", e);
            }
        }

        public void disconnect() {
            try {
                if (socket != null) socket.close();
            } catch (Exception ignored) {}
        }
    }
}
