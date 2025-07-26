package com.example.projectcuoiky.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectcuoiky.R;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    public static class DeviceInfo {
        public final String name;
        public final String address;

        public DeviceInfo(String name, String address) {
            this.name = name;
            this.address = address;
        }
    }

    public interface OnDeviceClickListener {
        void onDeviceClick(DeviceInfo device);
    }

    private final List<DeviceInfo> deviceList;
    private final OnDeviceClickListener listener;

    public DeviceAdapter(List<DeviceInfo> deviceList, OnDeviceClickListener listener) {
        this.deviceList = deviceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device_card, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        DeviceInfo device = deviceList.get(position);
        holder.deviceName.setText(device.name != null ? device.name : "Không rõ tên");
        holder.deviceAddress.setText(device.address);
        holder.itemView.setOnClickListener(v -> listener.onDeviceClick(device));
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        TextView deviceAddress;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.deviceName);
            deviceAddress = itemView.findViewById(R.id.deviceAddress);
        }
    }
}

