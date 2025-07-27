package com.example.projectcuoiky;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnector {
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;

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
                    String data = new String(buffer, 0, bytes);
                    listener.onDataReceived(data);
                }

            } catch (Exception e) {
                listener.onError(e.getMessage());
                disconnect();
            }
        }).start();
    }

    public void send(String msg) {
        try {
            if (outputStream != null) {
                outputStream.write(msg.getBytes());
            }
        } catch (Exception e) {
            Log.e("BluetoothConnector", "Send error", e);
        }
    }

    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception ignored) {}
    }
}
