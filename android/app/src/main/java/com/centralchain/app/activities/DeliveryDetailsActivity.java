package com.centralchain.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.centralchain.app.R;
import com.centralchain.app.models.Delivery;
import com.centralchain.app.models.Role;
import com.centralchain.app.network.ApiClient;
import com.centralchain.app.network.SessionManager;
import com.centralchain.app.utils.StatusStyle;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// one delivery, driver marks it in transit then completes it
public class DeliveryDetailsActivity extends AppCompatActivity {

    private long deliveryId;
    private Delivery delivery;
    private TextView textPoNumber, textStatus, textItemName, textItemSku;
    private Button btnAction, btnFailed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_details);

        deliveryId = getIntent().getLongExtra("deliveryId", 0);

        TextView title = findViewById(R.id.screen_title);
        title.setText("Delivery details");

        textPoNumber = findViewById(R.id.text_po_number);
        textStatus = findViewById(R.id.text_status);
        textItemName = findViewById(R.id.text_item_name);
        textItemSku = findViewById(R.id.text_item_sku);
        btnAction = findViewById(R.id.btn_primary);
        btnFailed = findViewById(R.id.btn_failed);

        btnAction.setOnClickListener(v -> onActionClick());
        btnFailed.setOnClickListener(v -> confirmFailed());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // reload when coming back from proof of delivery
        loadDelivery();
    }

    private void loadDelivery() {
        ApiClient.getApi(this).getDelivery(deliveryId).enqueue(new Callback<Delivery>() {
            @Override
            public void onResponse(Call<Delivery> call, Response<Delivery> response) {
                if (response.isSuccessful() && response.body() != null) {
                    delivery = response.body();
                    showDelivery();
                } else {
                    Toast.makeText(DeliveryDetailsActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Delivery> call, Throwable t) {
                Toast.makeText(DeliveryDetailsActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDelivery() {
        textPoNumber.setText(delivery.poNumber);
        // status as plain coloured text
        textStatus.setText(StatusStyle.getLabel(delivery.deliveryStatus));
        textStatus.setTextColor(ContextCompat.getColor(this, StatusStyle.getColor(delivery.deliveryStatus)));
        textItemName.setText(delivery.itemName);
        textItemSku.setText(delivery.itemSku);

        setRow(R.id.row_quantity, "Quantity", String.valueOf(delivery.poQuantity));
        // drivers know it is theirs, only admins need to see who has it
        if (Role.DRIVER.equals(SessionManager.getRole(this))) {
            findViewById(R.id.row_driver).setVisibility(View.GONE);
        } else {
            setRow(R.id.row_driver, "Driver", delivery.driverName);
        }

        // these rows only show once there is something in them
        showRow(R.id.row_delivered_at, "Delivered on",
                delivery.deliveredAt != null ? StatusStyle.formatDate(delivery.deliveredAt) : null);
        showRow(R.id.row_photo, "Photo", delivery.photoProofUrl);
        showRow(R.id.row_signature, "Signature", delivery.signatureUrl);

        // buttons depend on the status
        if (Delivery.ASSIGNED.equals(delivery.deliveryStatus)) {
            btnAction.setText("Mark in transit");
            btnAction.setVisibility(View.VISIBLE);
            btnFailed.setVisibility(View.GONE);
        } else if (Delivery.IN_TRANSIT.equals(delivery.deliveryStatus)) {
            btnAction.setText("Complete delivery");
            btnAction.setVisibility(View.VISIBLE);
            btnFailed.setVisibility(View.VISIBLE);
        } else {
            btnAction.setVisibility(View.GONE);
            btnFailed.setVisibility(View.GONE);
        }
    }

    // fills in one label and value row
    private void setRow(int rowId, String label, String value) {
        View row = findViewById(rowId);
        TextView textLabel = row.findViewById(R.id.row_label);
        TextView textValue = row.findViewById(R.id.row_value);
        textLabel.setText(label);
        textValue.setText(value != null ? value : "-");
    }

    // same as above but hides the row when there is no value
    private void showRow(int rowId, String label, String value) {
        View row = findViewById(rowId);
        if (value == null) {
            row.setVisibility(View.GONE);
        } else {
            setRow(rowId, label, value);
            row.setVisibility(View.VISIBLE);
        }
    }

    private void onActionClick() {
        if (Delivery.ASSIGNED.equals(delivery.deliveryStatus)) {
            ApiClient.getApi(this).markInTransit(deliveryId).enqueue(updateCallback("Delivery is now in transit"));
        } else if (Delivery.IN_TRANSIT.equals(delivery.deliveryStatus)) {
            // take the photo and signature first
            Intent intent = new Intent(this, ProofOfDeliveryActivity.class);
            intent.putExtra("deliveryId", deliveryId);
            intent.putExtra("poNumber", delivery.poNumber);
            startActivity(intent);
        }
    }

    private void confirmFailed() {
        new AlertDialog.Builder(this)
                .setTitle("Mark this delivery as failed?")
                .setPositiveButton("Yes", (dialog, which) ->
                        ApiClient.getApi(this).markFailed(deliveryId).enqueue(updateCallback("Delivery marked as failed")))
                .setNegativeButton("No", null)
                .show();
    }

    private Callback<Delivery> updateCallback(String successMessage) {
        return new Callback<Delivery>() {
            @Override
            public void onResponse(Call<Delivery> call, Response<Delivery> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DeliveryDetailsActivity.this, successMessage, Toast.LENGTH_SHORT).show();
                    loadDelivery();
                } else {
                    Toast.makeText(DeliveryDetailsActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Delivery> call, Throwable t) {
                Toast.makeText(DeliveryDetailsActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        };
    }
}
