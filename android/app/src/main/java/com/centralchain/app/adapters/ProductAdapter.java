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
import java.util.Locale;

// products the buyer can order
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(InventoryItem item);
    }

    private List<InventoryItem> items;
    private OnProductClickListener listener;

    public ProductAdapter(List<InventoryItem> items, OnProductClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InventoryItem item = items.get(position);
        holder.textName.setText(item.itemName);
        holder.textPrice.setText(String.format(Locale.US, "R%.2f each", item.unitPrice));

        if (item.quantityInStock > 0) {
            holder.textAvailable.setText(item.quantityInStock + " available");
            holder.textAvailable.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.success));
            holder.itemView.setAlpha(1f);
            holder.itemView.setOnClickListener(v -> listener.onProductClick(item));
        } else {
            // cant order something that is out of stock
            holder.textAvailable.setText("Out of stock");
            holder.textAvailable.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.danger));
            holder.itemView.setAlpha(0.5f);
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textPrice, textAvailable;

        ViewHolder(View view) {
            super(view);
            textName = view.findViewById(R.id.text_name);
            textPrice = view.findViewById(R.id.text_price);
            textAvailable = view.findViewById(R.id.text_available);
        }
    }
}
