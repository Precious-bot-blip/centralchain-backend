package com.centralchain.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.centralchain.app.R;
import com.centralchain.app.adapters.ChatAdapter;
import com.centralchain.app.models.PurchaseOrder;
import com.centralchain.app.models.Role;
import com.centralchain.app.network.ApiClient;
import com.centralchain.app.network.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// list of chats, one chat for each order
public class ChatListActivity extends AppCompatActivity {

    private List<PurchaseOrder> orders = new ArrayList<>();
    private ChatAdapter adapter;
    private TextView textEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        TextView title = findViewById(R.id.screen_title);
        title.setText("Chats");
        textEmpty = findViewById(R.id.text_empty);

        adapter = new ChatAdapter(orders, order -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("orderId", order.poId);
            intent.putExtra("poNumber", order.poNumber);
            startActivity(intent);
        });
        RecyclerView list = findViewById(R.id.list_chats);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        loadChats();
    }

    private void loadChats() {
        ApiClient.getApi(this).getOrders().enqueue(new Callback<List<PurchaseOrder>>() {
            @Override
            public void onResponse(Call<List<PurchaseOrder>> call, Response<List<PurchaseOrder>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ChatListActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                    return;
                }

                orders.clear();
                for (PurchaseOrder order : response.body()) {
                    if (isMyOrder(order)) {
                        orders.add(order);
                    }
                }
                adapter.notifyDataSetChanged();

                if (orders.isEmpty()) {
                    textEmpty.setVisibility(View.VISIBLE);
                } else {
                    textEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<PurchaseOrder>> call, Throwable t) {
                Toast.makeText(ChatListActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }

    // only show orders this user is part of
    private boolean isMyOrder(PurchaseOrder order) {
        String role = SessionManager.getRole(this);
        long myId = SessionManager.getUserId(this);

        if (Role.ADMIN.equals(role)) {
            return true;
        } else if (Role.BUYER.equals(role)) {
            return order.createdByUserId == myId;
        } else if (Role.SUPPLIER.equals(role)) {
            return order.assignedSupplierId == myId;
        } else if (Role.DRIVER.equals(role)) {
            return order.assignedDriverId != null && order.assignedDriverId == myId;
        }
        return false;
    }
}
