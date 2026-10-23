package com.centralchain.app.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.centralchain.app.R;
import com.centralchain.app.models.CreateOrderRequest;
import com.centralchain.app.models.InventoryItem;
import com.centralchain.app.models.PurchaseOrder;
import com.centralchain.app.models.Role;
import com.centralchain.app.models.User;
import com.centralchain.app.network.ApiClient;
import com.centralchain.app.network.SessionManager;
import com.centralchain.app.utils.Validation;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// buyer places a new order
public class CreateOrderActivity extends AppCompatActivity {

    private Spinner spinnerItem, spinnerSupplier;
    private EditText inputQuantity;
    private List<InventoryItem> items = new ArrayList<>();
    private List<User> suppliers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);

        TextView title = findViewById(R.id.screen_title);
        title.setText("Order supplies");

        spinnerItem = findViewById(R.id.spinner_item);
        spinnerSupplier = findViewById(R.id.spinner_supplier);
        inputQuantity = findViewById(R.id.input_quantity);

        findViewById(R.id.btn_create).setOnClickListener(v -> createOrder());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        loadItems();
        loadSuppliers();
    }

    private void loadItems() {
        ApiClient.getApi(this).getInventory().enqueue(new Callback<List<InventoryItem>>() {
            @Override
            public void onResponse(Call<List<InventoryItem>> call, Response<List<InventoryItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    items = response.body();
                    ArrayAdapter<InventoryItem> adapter = new ArrayAdapter<>(CreateOrderActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, items);
                    spinnerItem.setAdapter(adapter);

                    // select the product picked on the list
                    long pickedId = getIntent().getLongExtra("itemId", 0);
                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).itemId == pickedId) {
                            spinnerItem.setSelection(i);
                        }
                    }
                } else {
                    Toast.makeText(CreateOrderActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<InventoryItem>> call, Throwable t) {
                Toast.makeText(CreateOrderActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadSuppliers() {
        ApiClient.getApi(this).getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // only keep suppliers
                    suppliers.clear();
                    for (User user : response.body()) {
                        if (Role.SUPPLIER.equals(user.roleName)) {
                            suppliers.add(user);
                        }
                    }
                    ArrayAdapter<User> adapter = new ArrayAdapter<>(CreateOrderActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, suppliers);
                    spinnerSupplier.setAdapter(adapter);
                } else {
                    Toast.makeText(CreateOrderActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(CreateOrderActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createOrder() {
        String quantityText = inputQuantity.getText().toString();

        if (items.isEmpty() || suppliers.isEmpty()) {
            Toast.makeText(this, "Pick an item and a supplier", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Validation.isPositiveNumber(quantityText)) {
            inputQuantity.setError("Enter a quantity more than 0");
            return;
        }

        InventoryItem item = items.get(spinnerItem.getSelectedItemPosition());
        User supplier = suppliers.get(spinnerSupplier.getSelectedItemPosition());
        int quantity = Integer.parseInt(quantityText.trim());
        long myId = SessionManager.getUserId(this);

        CreateOrderRequest request = new CreateOrderRequest(item.itemId, quantity, myId, supplier.userId);
        ApiClient.getApi(this).createOrder(request).enqueue(new Callback<PurchaseOrder>() {
            @Override
            public void onResponse(Call<PurchaseOrder> call, Response<PurchaseOrder> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreateOrderActivity.this, "Order placed", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CreateOrderActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PurchaseOrder> call, Throwable t) {
                Toast.makeText(CreateOrderActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }
}
