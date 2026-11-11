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
import com.centralchain.app.adapters.ProductAdapter;
import com.centralchain.app.models.InventoryItem;
import com.centralchain.app.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// list of products the buyer can order, tap one to order it
public class ProductsActivity extends AppCompatActivity {

    private List<InventoryItem> items = new ArrayList<>();
    private ProductAdapter adapter;
    private TextView textEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        TextView title = findViewById(R.id.screen_title);
        title.setText("Order supplies");
        textEmpty = findViewById(R.id.text_empty);

        adapter = new ProductAdapter(items, item -> {
            Intent intent = new Intent(this, CreateOrderActivity.class);
            intent.putExtra("itemId", item.itemId);
            startActivity(intent);
        });
        RecyclerView list = findViewById(R.id.list_products);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // reload so the amounts are up to date
        loadProducts();
    }

    private void loadProducts() {
        ApiClient.getApi(this).getInventory().enqueue(new Callback<List<InventoryItem>>() {
            @Override
            public void onResponse(Call<List<InventoryItem>> call, Response<List<InventoryItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    items.clear();
                    items.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (items.isEmpty()) {
                        textEmpty.setVisibility(View.VISIBLE);
                    } else {
                        textEmpty.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(ProductsActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<InventoryItem>> call, Throwable t) {
                Toast.makeText(ProductsActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }
}
