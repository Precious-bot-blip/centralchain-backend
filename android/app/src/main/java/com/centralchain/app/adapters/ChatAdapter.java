package com.centralchain.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.centralchain.app.R;
import com.centralchain.app.models.PurchaseOrder;

import java.util.List;

// one chat button per order
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    public interface OnChatClickListener {
        void onChatClick(PurchaseOrder order);
    }

    private List<PurchaseOrder> orders;
    private OnChatClickListener listener;

    public ChatAdapter(List<PurchaseOrder> orders, OnChatClickListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PurchaseOrder order = orders.get(position);
        holder.textChatName.setText("Chat - " + order.poNumber);
        holder.itemView.setOnClickListener(v -> listener.onChatClick(order));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textChatName;

        ViewHolder(View view) {
            super(view);
            textChatName = view.findViewById(R.id.text_chat_name);
        }
    }
}
