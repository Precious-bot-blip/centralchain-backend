package com.centralchain.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.centralchain.app.R;
import com.centralchain.app.network.SessionManager;

// admin home page, admin can do everything
public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        MainActivity.showRole(this);

        TextView title = findViewById(R.id.screen_title);
        title.setText("Admin - " + SessionManager.getFullName(this));

        findViewById(R.id.btn_orders).setOnClickListener(v ->
                startActivity(new Intent(this, SupplierOrdersActivity.class)));
        findViewById(R.id.btn_new_order).setOnClickListener(v ->
                startActivity(new Intent(this, ProductsActivity.class)));
        findViewById(R.id.btn_deliveries).setOnClickListener(v ->
                startActivity(new Intent(this, DeliveriesActivity.class)));
        findViewById(R.id.btn_inventory).setOnClickListener(v ->
                startActivity(new Intent(this, InventoryActivity.class)));
        findViewById(R.id.btn_users).setOnClickListener(v ->
                startActivity(new Intent(this, UsersActivity.class)));
        findViewById(R.id.btn_chat).setOnClickListener(v ->
                startActivity(new Intent(this, ChatListActivity.class)));
        findViewById(R.id.btn_logout).setOnClickListener(v -> MainActivity.logout(this));
    }
}
