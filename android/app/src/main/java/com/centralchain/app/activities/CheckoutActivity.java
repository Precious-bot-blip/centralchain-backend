package com.centralchain.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.centralchain.app.R;
import com.centralchain.app.models.CreateOrderRequest;
import com.centralchain.app.models.PurchaseOrder;
import com.centralchain.app.network.ApiClient;
import com.centralchain.app.network.SessionManager;
import com.centralchain.app.utils.Validation;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// buyer adds delivery and payment info then the order is placed
// the card details are only checked, they are not sent anywhere
public class CheckoutActivity extends AppCompatActivity {

    private EditText inputAddress, inputCardName, inputCardNumber, inputExpiry, inputCvv;
    private MaterialButton btnPay;
    private ProgressBar progress;

    private long itemId, supplierId;
    private int quantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        TextView title = findViewById(R.id.screen_title);
        title.setText("Checkout");

        // order info from the last page
        itemId = getIntent().getLongExtra("itemId", 0);
        supplierId = getIntent().getLongExtra("supplierId", 0);
        quantity = getIntent().getIntExtra("quantity", 0);
        double unitPrice = getIntent().getDoubleExtra("unitPrice", 0);
        double total = unitPrice * quantity;

        TextView itemName = findViewById(R.id.text_item_name);
        itemName.setText(getIntent().getStringExtra("itemName"));
        setRow(R.id.row_quantity, "Quantity", String.valueOf(quantity));
        setRow(R.id.row_price, "Price each", String.format(Locale.US, "R%.2f", unitPrice));
        setRow(R.id.row_supplier, "Supplier", getIntent().getStringExtra("supplierName"));
        setRow(R.id.row_total, "Total", String.format(Locale.US, "R%.2f", total));

        inputAddress = findViewById(R.id.input_address);
        inputCardName = findViewById(R.id.input_card_name);
        inputCardNumber = findViewById(R.id.input_card_number);
        inputExpiry = findViewById(R.id.input_expiry);
        inputCvv = findViewById(R.id.input_cvv);
        btnPay = findViewById(R.id.btn_pay);
        progress = findViewById(R.id.progress);

        btnPay.setText(String.format(Locale.US, "Pay R%.2f", total));
        btnPay.setOnClickListener(v -> pay());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void setRow(int rowId, String label, String value) {
        View row = findViewById(rowId);
        TextView textLabel = row.findViewById(R.id.row_label);
        TextView textValue = row.findViewById(R.id.row_value);
        textLabel.setText(label);
        textValue.setText(value != null ? value : "-");
    }

    private void pay() {
        String address = inputAddress.getText().toString().trim();

        // check the fields
        boolean valid = true;
        if (address.isEmpty()) {
            inputAddress.setError("Delivery address is required");
            valid = false;
        }
        if (Validation.isEmpty(inputCardName.getText().toString())) {
            inputCardName.setError("Name on card is required");
            valid = false;
        }
        if (!Validation.isValidCardNumber(inputCardNumber.getText().toString())) {
            inputCardNumber.setError("Enter a 16 digit card number");
            valid = false;
        }
        if (!Validation.isValidExpiry(inputExpiry.getText().toString())) {
            inputExpiry.setError("Use MM/YY");
            valid = false;
        }
        if (!Validation.isValidCvv(inputCvv.getText().toString())) {
            inputCvv.setError("3 digits");
            valid = false;
        }
        if (!valid) {
            return;
        }

        progress.setVisibility(View.VISIBLE);
        btnPay.setEnabled(false);

        // only the address goes to the api
        long myId = SessionManager.getUserId(this);
        CreateOrderRequest request = new CreateOrderRequest(itemId, quantity, myId, supplierId, address);
        ApiClient.getApi(this).createOrder(request).enqueue(new Callback<PurchaseOrder>() {
            @Override
            public void onResponse(Call<PurchaseOrder> call, Response<PurchaseOrder> response) {
                progress.setVisibility(View.GONE);
                btnPay.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(CheckoutActivity.this, "Payment done, order placed", Toast.LENGTH_SHORT).show();
                    // back to the home page
                    MainActivity.openHome(CheckoutActivity.this, SessionManager.getRole(CheckoutActivity.this));
                    finish();
                } else {
                    Toast.makeText(CheckoutActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PurchaseOrder> call, Throwable t) {
                progress.setVisibility(View.GONE);
                btnPay.setEnabled(true);
                Toast.makeText(CheckoutActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }
}
