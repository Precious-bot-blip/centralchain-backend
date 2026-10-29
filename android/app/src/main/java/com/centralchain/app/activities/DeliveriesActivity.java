package com.centralchain.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.centralchain.app.R;
import com.centralchain.app.adapters.DeliveryAdapter;
import com.centralchain.app.models.Delivery;
import com.centralchain.app.network.ApiClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// admin list of all deliveries
public class DeliveriesActivity extends AppCompatActivity {

    private List<Delivery> deliveries = new ArrayList<>();
    private DeliveryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliveries);

        TextView title = findViewById(R.id.screen_title);
        title.setText("Deliveries");

        adapter = new DeliveryAdapter(deliveries, delivery -> {
            Intent intent = new Intent(this, DeliveryDetailsActivity.class);
            intent.putExtra("deliveryId", delivery.deliveryId);
            startActivity(intent);
        });
        RecyclerView list = findViewById(R.id.list_deliveries);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDeliveries();
    }

    private void loadDeliveries() {
        ApiClient.getApi(this).getDeliveries().enqueue(new Callback<List<Delivery>>() {
            @Override
            public void onResponse(Call<List<Delivery>> call, Response<List<Delivery>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    deliveries.clear();
                    deliveries.addAll(response.body());
                    // a to z by item name
                    Collections.sort(deliveries, (a, b) -> a.itemName.compareToIgnoreCase(b.itemName));
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(DeliveriesActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Delivery>> call, Throwable t) {
                Toast.makeText(DeliveriesActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }
}
