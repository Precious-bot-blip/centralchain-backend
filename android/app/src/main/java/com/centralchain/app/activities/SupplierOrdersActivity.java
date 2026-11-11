package com.centralchain.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.centralchain.app.R;
import com.centralchain.app.adapters.OrderAdapter;
import com.centralchain.app.models.PurchaseOrder;
import com.centralchain.app.network.ApiClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// supplier orders with pending, active and completed tabs
public class SupplierOrdersActivity extends AppCompatActivity {

    private List<PurchaseOrder> allOrders = new ArrayList<>();
    private List<PurchaseOrder> shownOrders = new ArrayList<>();
    private OrderAdapter adapter;
    private String currentTab = "Pending";

    private Button btnPending, btnActive, btnCompleted;
    private TextView textCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier_orders);

        btnPending = findViewById(R.id.btn_tab_pending);
        btnActive = findViewById(R.id.btn_tab_active);
        btnCompleted = findViewById(R.id.btn_tab_completed);
        textCount = findViewById(R.id.text_count);

        adapter = new OrderAdapter(shownOrders, order -> {
            Intent intent = new Intent(this, OrderDetailsActivity.class);
            intent.putExtra("orderId", order.poId);
            startActivity(intent);
        });
        RecyclerView list = findViewById(R.id.list_orders);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        btnPending.setOnClickListener(v -> showTab("Pending"));
        btnActive.setOnClickListener(v -> showTab("Active"));
        btnCompleted.setOnClickListener(v -> showTab("Completed"));
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    private void loadOrders() {
        ApiClient.getApi(this).getOrders().enqueue(new Callback<List<PurchaseOrder>>() {
            @Override
            public void onResponse(Call<List<PurchaseOrder>> call, Response<List<PurchaseOrder>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(SupplierOrdersActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                    return;
                }

                // the api already only sends this supplier's orders
                allOrders.clear();
                allOrders.addAll(response.body());
                // a to z by item name
                Collections.sort(allOrders, (a, b) -> a.itemName.compareToIgnoreCase(b.itemName));
                showTab(currentTab);
            }

            @Override
            public void onFailure(Call<List<PurchaseOrder>> call, Throwable t) {
                Toast.makeText(SupplierOrdersActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showTab(String tab) {
        currentTab = tab;

        // fade the tabs that are not picked
        btnPending.setAlpha(tab.equals("Pending") ? 1f : 0.4f);
        btnActive.setAlpha(tab.equals("Active") ? 1f : 0.4f);
        btnCompleted.setAlpha(tab.equals("Completed") ? 1f : 0.4f);

        shownOrders.clear();
        for (PurchaseOrder order : allOrders) {
            String status = order.status;
            boolean pending = PurchaseOrder.PENDING.equals(status);
            boolean active = PurchaseOrder.APPROVED.equals(status) || PurchaseOrder.PROCESSING.equals(status)
                    || PurchaseOrder.DISPATCHED.equals(status);

            if (tab.equals("Pending") && pending) {
                shownOrders.add(order);
            } else if (tab.equals("Active") && active) {
                shownOrders.add(order);
            } else if (tab.equals("Completed") && !order.isActive()) {
                shownOrders.add(order);
            }
        }

        textCount.setText(tab + " orders (" + shownOrders.size() + ")");
        adapter.notifyDataSetChanged();
    }
}
