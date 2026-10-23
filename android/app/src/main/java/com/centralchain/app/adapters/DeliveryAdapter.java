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
import com.centralchain.app.models.Delivery;

import java.util.List;

// shows a list of deliveries
public class DeliveryAdapter extends RecyclerView.Adapter<DeliveryAdapter.ViewHolder> {

    public interface OnDeliveryClickListener {
        void onDeliveryClick(Delivery delivery);
    }

    private List<Delivery> deliveries;
    private OnDeliveryClickListener listener;

    public DeliveryAdapter(List<Delivery> deliveries, OnDeliveryClickListener listener) {
        this.deliveries = deliveries;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_delivery, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Delivery delivery = deliveries.get(position);
        holder.textPoNumber.setText(delivery.poNumber);
        holder.textItem.setText(delivery.itemName + " x " + delivery.poQuantity);
        holder.textStatus.setText(StatusStyle.getLabel(delivery.deliveryStatus));
        holder.textStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), StatusStyle.getColor(delivery.deliveryStatus)));
        holder.itemView.setOnClickListener(v -> listener.onDeliveryClick(delivery));
    }

    @Override
    public int getItemCount() {
        return deliveries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textPoNumber, textItem, textStatus;

        ViewHolder(View view) {
            super(view);
            textPoNumber = view.findViewById(R.id.text_po_number);
            textItem = view.findViewById(R.id.text_item);
            textStatus = view.findViewById(R.id.text_status);
        }
    }
}
