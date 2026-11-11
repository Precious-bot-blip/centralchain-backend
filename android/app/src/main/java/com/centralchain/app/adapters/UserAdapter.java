package com.centralchain.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.centralchain.app.R;
import com.centralchain.app.models.Role;
import com.centralchain.app.models.User;

import java.util.List;

// shows a list of users
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    private List<User> users;
    private OnUserClickListener listener;

    // listener can be null if the list is just for looking
    public UserAdapter(List<User> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.textName.setText(user.fullName);
        holder.textEmail.setText(user.email + " - " + Role.getLabel(user.roleName));
        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textEmail;

        ViewHolder(View view) {
            super(view);
            textName = view.findViewById(R.id.text_name);
            textEmail = view.findViewById(R.id.text_email);
        }
    }
}
