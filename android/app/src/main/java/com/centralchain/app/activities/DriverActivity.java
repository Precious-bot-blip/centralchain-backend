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
import com.centralchain.app.network.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// driver home page with open and finished deliveries
public class DriverActivity extends AppCompatActivity {

    private List<Delivery> openDeliveries = new ArrayList<>();
    private List<Delivery> finishedDeliveries = new ArrayList<>();
    private DeliveryAdapter openAdapter, finishedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        MainActivity.showRole(this);

        TextView title = findViewById(R.id.screen_title);
        title.setText("Hi " + SessionManager.getFullName(this));

        openAdapter = new DeliveryAdapter(openDeliveries, delivery -> openDelivery(delivery));
        finishedAdapter = new DeliveryAdapter(finishedDeliveries, delivery -> openDelivery(delivery));

        RecyclerView listOpen = findViewById(R.id.list_pending);
        listOpen.setLayoutManager(new LinearLayoutManager(this));
        listOpen.setAdapter(openAdapter);

        RecyclerView listFinished = findViewById(R.id.list_delivered);
        listFinished.setLayoutManager(new LinearLayoutManager(this));
        listFinished.setAdapter(finishedAdapter);

        findViewById(R.id.btn_chat).setOnClickListener(v ->
                startActivity(new Intent(this, ChatListActivity.class)));
        findViewById(R.id.btn_logout).setOnClickListener(v -> MainActivity.logout(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDeliveries();
    }

    private void loadDeliveries() {
        long myId = SessionManager.getUserId(this);
        ApiClient.getApi(this).getDriverDeliveries(myId).enqueue(new Callback<List<Delivery>>() {
            @Override
            public void onResponse(Call<List<Delivery>> call, Response<List<Delivery>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(DriverActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                    return;
                }

                // split into still to do and done
                openDeliveries.clear();
                finishedDeliveries.clear();
                for (Delivery delivery : response.body()) {
                    if (delivery.isOpen()) {
                        openDeliveries.add(delivery);
                    } else {
                        finishedDeliveries.add(delivery);
                    }
                }
                // a to z by item name
                Collections.sort(openDeliveries, (a, b) -> a.itemName.compareToIgnoreCase(b.itemName));
                Collections.sort(finishedDeliveries, (a, b) -> a.itemName.compareToIgnoreCase(b.itemName));
                openAdapter.notifyDataSetChanged();
                finishedAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<Delivery>> call, Throwable t) {
                Toast.makeText(DriverActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openDelivery(Delivery delivery) {
        Intent intent = new Intent(this, DeliveryDetailsActivity.class);
        intent.putExtra("deliveryId", delivery.deliveryId);
        startActivity(intent);
    }
}
