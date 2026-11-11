package com.centralchain.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.centralchain.app.R;
import com.centralchain.app.utils.StatusStyle;
import com.centralchain.app.models.PurchaseOrder;

import java.util.List;

// shows a list of orders
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(PurchaseOrder order);
    }

    private List<PurchaseOrder> orders;
    private OnOrderClickListener listener;

    public OrderAdapter(List<PurchaseOrder> orders, OnOrderClickListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PurchaseOrder order = orders.get(position);
        holder.textOrderNumber.setText(order.poNumber);
        holder.textItem.setText("Item: " + order.itemName);
        holder.textQuantity.setText("Qty " + order.quantity);
        holder.textStatus.setText(StatusStyle.getLabel(order.status));
        holder.textStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), StatusStyle.getColor(order.status)));
        holder.itemView.setOnClickListener(v -> listener.onOrderClick(order));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textOrderNumber, textItem, textQuantity, textStatus;

        ViewHolder(View view) {
            super(view);
            textOrderNumber = view.findViewById(R.id.text_order_number);
            textItem = view.findViewById(R.id.text_item);
            textQuantity = view.findViewById(R.id.text_quantity);
            textStatus = view.findViewById(R.id.text_status);
        }
    }
}
