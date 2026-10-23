package com.centralchain.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.centralchain.app.R;
import com.centralchain.app.adapters.OrderAdapter;
import com.centralchain.app.models.PurchaseOrder;
import com.centralchain.app.network.ApiClient;
import com.centralchain.app.network.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// buyer home page with past and active orders
public class BuyerActivity extends AppCompatActivity {

    private List<PurchaseOrder> pastOrders = new ArrayList<>();
    private List<PurchaseOrder> activeOrders = new ArrayList<>();
    private OrderAdapter pastAdapter, activeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer);

        TextView title = findViewById(R.id.screen_title);
        title.setText("Hi " + SessionManager.getFullName(this));

        // both lists open the order when clicked
        pastAdapter = new OrderAdapter(pastOrders, order -> openOrder(order));
        activeAdapter = new OrderAdapter(activeOrders, order -> openOrder(order));

        RecyclerView listPast = findViewById(R.id.list_past_orders);
        listPast.setLayoutManager(new LinearLayoutManager(this));
        listPast.setAdapter(pastAdapter);

        RecyclerView listActive = findViewById(R.id.list_active_orders);
        listActive.setLayoutManager(new LinearLayoutManager(this));
        listActive.setAdapter(activeAdapter);

        findViewById(R.id.btn_new_order).setOnClickListener(v ->
                startActivity(new Intent(this, ProductsActivity.class)));
        findViewById(R.id.btn_chat).setOnClickListener(v ->
                startActivity(new Intent(this, ChatListActivity.class)));
        findViewById(R.id.btn_logout).setOnClickListener(v -> MainActivity.logout(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // reload every time we come back to this page
        loadOrders();
    }

    private void loadOrders() {
        ApiClient.getApi(this).getOrders().enqueue(new Callback<List<PurchaseOrder>>() {
            @Override
            public void onResponse(Call<List<PurchaseOrder>> call, Response<List<PurchaseOrder>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(BuyerActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                    return;
                }

                long myId = SessionManager.getUserId(BuyerActivity.this);
                pastOrders.clear();
                activeOrders.clear();

                // only show my orders
                for (PurchaseOrder order : response.body()) {
                    if (order.createdByUserId != myId) {
                        continue;
                    }
                    if (order.isActive()) {
                        activeOrders.add(order);
                    } else {
                        pastOrders.add(order);
                    }
                }
                pastAdapter.notifyDataSetChanged();
                activeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<PurchaseOrder>> call, Throwable t) {
                Toast.makeText(BuyerActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openOrder(PurchaseOrder order) {
        Intent intent = new Intent(this, OrderDetailsActivity.class);
        intent.putExtra("orderId", order.poId);
        startActivity(intent);
    }
}
