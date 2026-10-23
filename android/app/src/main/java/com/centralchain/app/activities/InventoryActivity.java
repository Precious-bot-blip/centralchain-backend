package com.centralchain.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.centralchain.app.R;
import com.centralchain.app.adapters.InventoryAdapter;
import com.centralchain.app.models.InventoryItem;
import com.centralchain.app.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// inventory list, low stock shows in red
public class InventoryActivity extends AppCompatActivity {

    private List<InventoryItem> items = new ArrayList<>();
    private InventoryAdapter adapter;
    private boolean lowStockOnly = false;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        title = findViewById(R.id.screen_title);
        title.setText("Inventory");

        adapter = new InventoryAdapter(items, item -> {
            Intent intent = new Intent(this, InventoryItemActivity.class);
            intent.putExtra("itemId", item.itemId);
            startActivity(intent);
        });
        RecyclerView list = findViewById(R.id.list_inventory);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        // filter drop down
        findViewById(R.id.btn_filter).setOnClickListener(v -> showFilter(v));

        // no item id means add a new item
        findViewById(R.id.btn_add_item).setOnClickListener(v ->
                startActivity(new Intent(this, InventoryItemActivity.class)));
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInventory();
    }

    private void showFilter(View button) {
        PopupMenu menu = new PopupMenu(this, button);
        menu.getMenu().add("All items");
        menu.getMenu().add("Low stock");
        menu.setOnMenuItemClickListener(item -> {
            lowStockOnly = item.getTitle().equals("Low stock");
            if (lowStockOnly) {
                title.setText("Inventory - Low stock");
            } else {
                title.setText("Inventory");
            }
            loadInventory();
            return true;
        });
        menu.show();
    }

    private void loadInventory() {
        Call<List<InventoryItem>> call;
        if (lowStockOnly) {
            call = ApiClient.getApi(this).getLowStock();
        } else {
            call = ApiClient.getApi(this).getInventory();
        }

        call.enqueue(new Callback<List<InventoryItem>>() {
            @Override
            public void onResponse(Call<List<InventoryItem>> call, Response<List<InventoryItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    items.clear();
                    items.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(InventoryActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<InventoryItem>> call, Throwable t) {
                Toast.makeText(InventoryActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }
}
