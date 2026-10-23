package com.centralchain.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.centralchain.app.R;
import com.centralchain.app.models.InventoryItem;
import com.centralchain.app.models.InventoryRequest;
import com.centralchain.app.network.ApiClient;
import com.centralchain.app.utils.Validation;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// add a new item or edit one
public class InventoryItemActivity extends AppCompatActivity {

    private long itemId;
    private boolean isNewItem;
    private EditText inputSku, inputName, inputLocation, inputQuantity, inputThreshold, inputPrice, inputAmount;
    private TextView textStock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_item);

        itemId = getIntent().getLongExtra("itemId", 0);
        isNewItem = itemId == 0;

        inputSku = findViewById(R.id.input_sku);
        inputName = findViewById(R.id.input_name);
        inputLocation = findViewById(R.id.input_location);
        inputQuantity = findViewById(R.id.input_quantity);
        inputThreshold = findViewById(R.id.input_threshold);
        inputPrice = findViewById(R.id.input_price);
        inputAmount = findViewById(R.id.input_delta);
        textStock = findViewById(R.id.text_stock);

        TextView title = findViewById(R.id.screen_title);
        if (isNewItem) {
            title.setText("New item");
        } else {
            title.setText("Edit item");
            // sku cant change and stock is changed with adjust
            inputSku.setEnabled(false);
            findViewById(R.id.group_quantity).setVisibility(View.GONE);
            findViewById(R.id.group_adjust).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_delete).setVisibility(View.VISIBLE);
            loadItem();
        }

        findViewById(R.id.btn_save).setOnClickListener(v -> saveItem());
        findViewById(R.id.btn_adjust).setOnClickListener(v -> adjustStock());
        findViewById(R.id.btn_delete).setOnClickListener(v -> confirmDelete());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void loadItem() {
        ApiClient.getApi(this).getInventoryItem(itemId).enqueue(new Callback<InventoryItem>() {
            @Override
            public void onResponse(Call<InventoryItem> call, Response<InventoryItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    InventoryItem item = response.body();
                    inputSku.setText(item.sku);
                    inputName.setText(item.itemName);
                    inputLocation.setText(item.warehouseLocation);
                    inputThreshold.setText(String.valueOf(item.minSafetyThreshold));
                    inputPrice.setText(String.valueOf(item.unitPrice));
                    textStock.setText("In stock: " + item.quantityInStock);
                } else {
                    Toast.makeText(InventoryItemActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<InventoryItem> call, Throwable t) {
                Toast.makeText(InventoryItemActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveItem() {
        String sku = inputSku.getText().toString().trim();
        String name = inputName.getText().toString().trim();
        String location = inputLocation.getText().toString().trim();
        String quantity = inputQuantity.getText().toString();
        String threshold = inputThreshold.getText().toString();
        String price = inputPrice.getText().toString();

        // check the fields
        boolean valid = true;
        if (sku.isEmpty()) {
            inputSku.setError("SKU is required");
            valid = false;
        }
        if (name.isEmpty()) {
            inputName.setError("Name is required");
            valid = false;
        }
        if (location.isEmpty()) {
            inputLocation.setError("Location is required");
            valid = false;
        }
        if (isNewItem && !Validation.isZeroOrMore(quantity)) {
            inputQuantity.setError("Enter a number");
            valid = false;
        }
        if (!Validation.isZeroOrMore(threshold)) {
            inputThreshold.setError("Enter a number");
            valid = false;
        }
        if (!Validation.isValidPrice(price)) {
            inputPrice.setError("Price must be more than 0");
            valid = false;
        }
        if (!valid) {
            return;
        }

        InventoryRequest request = new InventoryRequest();
        request.itemName = name;
        request.warehouseLocation = location;
        request.minSafetyThreshold = Integer.parseInt(threshold.trim());
        request.unitPrice = Double.parseDouble(price.trim());

        Call<InventoryItem> call;
        if (isNewItem) {
            request.sku = sku;
            request.quantityInStock = Integer.parseInt(quantity.trim());
            call = ApiClient.getApi(this).addInventoryItem(request);
        } else {
            call = ApiClient.getApi(this).updateInventoryItem(itemId, request);
        }

        call.enqueue(new Callback<InventoryItem>() {
            @Override
            public void onResponse(Call<InventoryItem> call, Response<InventoryItem> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(InventoryItemActivity.this, "Item saved", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(InventoryItemActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<InventoryItem> call, Throwable t) {
                Toast.makeText(InventoryItemActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }

    // add or remove stock, negative number removes
    private void adjustStock() {
        String amountText = inputAmount.getText().toString().trim();
        int amount;
        try {
            amount = Integer.parseInt(amountText);
        } catch (NumberFormatException e) {
            inputAmount.setError("Enter a number like 20 or -10");
            return;
        }
        if (amount == 0) {
            inputAmount.setError("Amount cant be 0");
            return;
        }

        ApiClient.getApi(this).adjustStock(itemId, amount).enqueue(new Callback<InventoryItem>() {
            @Override
            public void onResponse(Call<InventoryItem> call, Response<InventoryItem> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(InventoryItemActivity.this, "Stock updated", Toast.LENGTH_SHORT).show();
                    inputAmount.setText("");
                    loadItem();
                } else {
                    Toast.makeText(InventoryItemActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<InventoryItem> call, Throwable t) {
                Toast.makeText(InventoryItemActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete this item?")
                .setPositiveButton("Delete", (dialog, which) -> deleteItem())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteItem() {
        ApiClient.getApi(this).deleteInventoryItem(itemId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(InventoryItemActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(InventoryItemActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(InventoryItemActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }
}
