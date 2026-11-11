package com.centralchain.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.centralchain.app.R;
import com.centralchain.app.models.InventoryItem;
import com.centralchain.app.network.ApiClient;
import com.centralchain.app.utils.Validation;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// buyer picks an item and quantity, the supplier comes from the item
public class CreateOrderActivity extends AppCompatActivity {

    private Spinner spinnerItem;
    private TextView textSupplier;
    private EditText inputQuantity;
    private List<InventoryItem> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);

        TextView title = findViewById(R.id.screen_title);
        title.setText("Order supplies");

        spinnerItem = findViewById(R.id.spinner_item);
        textSupplier = findViewById(R.id.text_supplier);
        inputQuantity = findViewById(R.id.input_quantity);

        // show the supplier of the picked item
        spinnerItem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                InventoryItem item = items.get(position);
                textSupplier.setText(item.supplierId != null ? item.supplierName : "No supplier yet");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        findViewById(R.id.btn_create).setOnClickListener(v -> createOrder());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        loadItems();
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

    // check the form then go to checkout
    private void createOrder() {
        String quantityText = inputQuantity.getText().toString();

        if (items.isEmpty()) {
            Toast.makeText(this, "Pick an item", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Validation.isPositiveNumber(quantityText)) {
            inputQuantity.setError("Enter a quantity more than 0");
            return;
        }

        InventoryItem item = items.get(spinnerItem.getSelectedItemPosition());
        if (item.supplierId == null) {
            Toast.makeText(this, "This item has no supplier yet", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra("itemId", item.itemId);
        intent.putExtra("itemName", item.itemName);
        intent.putExtra("unitPrice", item.unitPrice);
        intent.putExtra("quantity", Integer.parseInt(quantityText.trim()));
        intent.putExtra("supplierId", (long) item.supplierId);
        intent.putExtra("supplierName", item.supplierName);
        startActivity(intent);
    }
}
