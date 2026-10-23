package com.centralchain.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.centralchain.app.R;
import com.centralchain.app.models.InventoryItem;

import java.util.List;

// shows the inventory list
public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(InventoryItem item);
    }

    private List<InventoryItem> items;
    private OnItemClickListener listener;

    public InventoryAdapter(List<InventoryItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InventoryItem item = items.get(position);
        holder.textName.setText(item.itemName);
        holder.textDetails.setText(item.sku + " - " + item.warehouseLocation);
        holder.textQuantity.setText(String.valueOf(item.quantityInStock));

        // red if low on stock
        int color = item.isLowStock() ? R.color.danger : R.color.ink;
        holder.textQuantity.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), color));

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textDetails, textQuantity;

        ViewHolder(View view) {
            super(view);
            textName = view.findViewById(R.id.text_name);
            textDetails = view.findViewById(R.id.text_details);
            textQuantity = view.findViewById(R.id.text_quantity);
        }
    }
}
