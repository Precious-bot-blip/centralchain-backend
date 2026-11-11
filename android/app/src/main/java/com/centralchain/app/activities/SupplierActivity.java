package com.centralchain.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.centralchain.app.R;
import com.centralchain.app.network.SessionManager;

// supplier home page
public class SupplierActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier);
        MainActivity.showRole(this);

        TextView title = findViewById(R.id.screen_title);
        title.setText("Hi " + SessionManager.getFullName(this));

        findViewById(R.id.btn_inventory).setOnClickListener(v ->
                startActivity(new Intent(this, InventoryActivity.class)));
        findViewById(R.id.btn_orders).setOnClickListener(v ->
                startActivity(new Intent(this, SupplierOrdersActivity.class)));
        findViewById(R.id.btn_reports).setOnClickListener(v ->
                startActivity(new Intent(this, ReportsActivity.class)));
        findViewById(R.id.btn_chat).setOnClickListener(v ->
                startActivity(new Intent(this, ChatListActivity.class)));
        findViewById(R.id.btn_logout).setOnClickListener(v -> MainActivity.logout(this));
    }
}
