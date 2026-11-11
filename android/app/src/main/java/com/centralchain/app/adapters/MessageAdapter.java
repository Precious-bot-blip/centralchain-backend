package com.centralchain.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.centralchain.app.R;
import com.centralchain.app.models.Message;

import java.util.List;

// chat bubbles, my messages on the right
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private static final int MY_MESSAGE = 1;
    private static final int OTHER_MESSAGE = 2;

    private List<Message> messages;
    private long myUserId;

    public MessageAdapter(List<Message> messages, long myUserId) {
        this.messages = messages;
        this.myUserId = myUserId;
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).senderId == myUserId) {
            return MY_MESSAGE;
        }
        return OTHER_MESSAGE;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = R.layout.item_message_other;
        if (viewType == MY_MESSAGE) {
            layout = R.layout.item_message_me;
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.textMessage.setText(message.messageText);
        if (message.senderName != null) {
            holder.textInfo.setText(message.senderName);
        } else {
            holder.textInfo.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage, textInfo;

        ViewHolder(View view) {
            super(view);
            textMessage = view.findViewById(R.id.msg_text);
            textInfo = view.findViewById(R.id.msg_meta);
        }
    }
}
