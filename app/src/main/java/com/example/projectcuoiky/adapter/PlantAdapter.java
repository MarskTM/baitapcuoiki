package com.example.projectcuoiky.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectcuoiky.DetailActivity;
import com.example.projectcuoiky.R;
import com.example.projectcuoiky.model.PlantItem;

import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {

    private final Context context;
    private final List<PlantItem> plantList;

    public PlantAdapter(Context context, List<PlantItem> plantList) {
        this.context = context;
        this.plantList = plantList;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.plant_item, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        PlantItem plant = plantList.get(position);

        holder.imagePlant.setImageResource(plant.getImageResId());
        holder.textName.setText(plant.getName());

        // Hiển thị Unlocked nếu giá là "0"
        if ("0".equals(plant.getPrice()) || "Unlocked".equalsIgnoreCase(plant.getPrice())) {
            holder.textPrice.setText("Unlocked");
            holder.textPrice.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
        } else {
            holder.textPrice.setText(plant.getPrice());
            holder.textPrice.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
        }

        // Khi click vào item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("plantName", plant.getName());
            intent.putExtra("plantPrice", plant.getPrice());
            intent.putExtra("plantImage", plant.getImageResId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return plantList.size();
    }

    public static class PlantViewHolder extends RecyclerView.ViewHolder {
        ImageView imagePlant;
        TextView textName, textPrice;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            imagePlant = itemView.findViewById(R.id.imagePlant);
            textName = itemView.findViewById(R.id.textPlantName);
            textPrice = itemView.findViewById(R.id.textPlantPrice);
        }
    }
}
