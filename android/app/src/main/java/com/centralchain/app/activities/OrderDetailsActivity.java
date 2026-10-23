package com.centralchain.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.centralchain.app.R;
import com.centralchain.app.models.Delivery;
import com.centralchain.app.models.PurchaseOrder;
import com.centralchain.app.models.Role;
import com.centralchain.app.models.UpdateOrderRequest;
import com.centralchain.app.models.User;
import com.centralchain.app.network.ApiClient;
import com.centralchain.app.network.SessionManager;
import com.centralchain.app.utils.StatusStyle;
import com.centralchain.app.utils.Validation;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// shows one order, buttons change for each role
public class OrderDetailsActivity extends AppCompatActivity {

    private long orderId;
    private PurchaseOrder order;
    private String role;

    private TextView textPoNumber, textStatus, textItemName, textItemSku;
    private View cardDelivery;
    private View btnApprove, btnDispatch, btnEdit, btnCancel, btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        orderId = getIntent().getLongExtra("orderId", 0);
        role = SessionManager.getRole(this);

        TextView title = findViewById(R.id.screen_title);
        title.setText("Order details");

        textPoNumber = findViewById(R.id.text_po_number);
        textStatus = findViewById(R.id.text_status);
        textItemName = findViewById(R.id.text_item_name);
        textItemSku = findViewById(R.id.text_item_sku);
        cardDelivery = findViewById(R.id.card_delivery);
        btnApprove = findViewById(R.id.btn_approve);
        btnDispatch = findViewById(R.id.btn_dispatch);
        btnEdit = findViewById(R.id.btn_edit);
        btnCancel = findViewById(R.id.btn_cancel);
        btnDelete = findViewById(R.id.btn_delete);

        btnApprove.setOnClickListener(v ->
                ApiClient.getApi(this).approveOrder(orderId).enqueue(updateCallback("Order approved")));
        btnDispatch.setOnClickListener(v -> pickDriver());
        btnEdit.setOnClickListener(v -> changeQuantity());
        btnCancel.setOnClickListener(v -> confirmCancel());
        btnDelete.setOnClickListener(v -> confirmDelete());

        findViewById(R.id.btn_chat).setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("orderId", orderId);
            if (order != null) {
                intent.putExtra("poNumber", order.poNumber);
            }
            startActivity(intent);
        });
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrder();
    }

    private void loadOrder() {
        ApiClient.getApi(this).getOrder(orderId).enqueue(new Callback<PurchaseOrder>() {
            @Override
            public void onResponse(Call<PurchaseOrder> call, Response<PurchaseOrder> response) {
                if (response.isSuccessful() && response.body() != null) {
                    order = response.body();
                    showOrder();
                } else {
                    Toast.makeText(OrderDetailsActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PurchaseOrder> call, Throwable t) {
                Toast.makeText(OrderDetailsActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showOrder() {
        textPoNumber.setText(order.poNumber);
        // status as plain coloured text
        textStatus.setText(StatusStyle.getLabel(order.status));
        textStatus.setTextColor(ContextCompat.getColor(this, StatusStyle.getColor(order.status)));
        textItemName.setText(order.itemName);
        textItemSku.setText(order.itemSku);

        setRow(R.id.row_quantity, "Quantity", String.valueOf(order.quantity));
        setRow(R.id.row_supplier, "Supplier", order.assignedSupplierName);
        setRow(R.id.row_created_by, "Ordered by", order.createdByUserName);
        setRow(R.id.row_date, "Ordered on", StatusStyle.formatDate(order.createdAt));

        // driver row only once there is a driver
        View rowDriver = findViewById(R.id.row_driver);
        if (order.assignedDriverName != null) {
            setRow(R.id.row_driver, "Driver", order.assignedDriverName);
            rowDriver.setVisibility(View.VISIBLE);
        } else {
            rowDriver.setVisibility(View.GONE);
        }

        boolean isBuyer = Role.BUYER.equals(role) || Role.ADMIN.equals(role);
        boolean isSupplier = Role.SUPPLIER.equals(role) || Role.ADMIN.equals(role);
        String status = order.status;
        boolean isPending = PurchaseOrder.PENDING.equals(status);
        boolean isApproved = PurchaseOrder.APPROVED.equals(status);
        boolean isProcessing = PurchaseOrder.PROCESSING.equals(status);
        boolean isCancelled = PurchaseOrder.CANCELLED.equals(status);

        // supplier approves the order then sends it with a driver
        showButton(btnApprove, isSupplier && isPending);
        showButton(btnDispatch, isSupplier && (isApproved || isProcessing));

        // buyer can change the quantity before it is approved
        showButton(btnEdit, isBuyer && isPending);

        // buyer and supplier cancel, the order stays in the history
        showButton(btnCancel, (isBuyer || isSupplier) && order.isActive());
        // only admin can delete an order for good
        showButton(btnDelete, Role.ADMIN.equals(role) && (isPending || isCancelled));

        // dispatched orders have a delivery
        if (order.assignedDriverId != null) {
            loadDelivery();
        } else {
            cardDelivery.setVisibility(View.GONE);
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

    private void loadDelivery() {
        ApiClient.getApi(this).getDeliveryForOrder(orderId).enqueue(new Callback<Delivery>() {
            @Override
            public void onResponse(Call<Delivery> call, Response<Delivery> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Delivery delivery = response.body();
                    setRow(R.id.row_delivery_status, "Status", StatusStyle.getLabel(delivery.deliveryStatus));
                    setRow(R.id.row_delivered_at, "Delivered on",
                            delivery.deliveredAt != null ? StatusStyle.formatDate(delivery.deliveredAt) : "Not yet");
                    cardDelivery.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<Delivery> call, Throwable t) {
                // just dont show the delivery
            }
        });
    }

    private void showButton(View button, boolean show) {
        if (show) {
            button.setVisibility(View.VISIBLE);
        } else {
            button.setVisibility(View.GONE);
        }
    }

    // only pending orders can be changed
    private void changeQuantity() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(order.quantity));

        new AlertDialog.Builder(this)
                .setTitle("New quantity")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String text = input.getText().toString();
                    if (!Validation.isPositiveNumber(text)) {
                        Toast.makeText(this, "Enter a quantity more than 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    UpdateOrderRequest request = new UpdateOrderRequest(Integer.parseInt(text.trim()), order.assignedSupplierId);
                    ApiClient.getApi(this).updateOrder(orderId, request).enqueue(updateCallback("Order updated"));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmCancel() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel this order?")
                .setPositiveButton("Yes", (dialog, which) ->
                        ApiClient.getApi(this).cancelOrder(orderId).enqueue(updateCallback("Order cancelled")))
                .setNegativeButton("No", null)
                .show();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete this order?")
                .setPositiveButton("Delete", (dialog, which) -> deleteOrder())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteOrder() {
        ApiClient.getApi(this).deleteOrder(orderId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(OrderDetailsActivity.this, "Order deleted", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(OrderDetailsActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(OrderDetailsActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }

    // get the drivers and pick one
    private void pickDriver() {
        ApiClient.getApi(this).getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(OrderDetailsActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                    return;
                }

                List<User> drivers = new ArrayList<>();
                for (User user : response.body()) {
                    if (Role.DRIVER.equals(user.roleName)) {
                        drivers.add(user);
                    }
                }
                if (drivers.isEmpty()) {
                    Toast.makeText(OrderDetailsActivity.this, "There are no drivers yet", Toast.LENGTH_LONG).show();
                    return;
                }

                String[] names = new String[drivers.size()];
                for (int i = 0; i < drivers.size(); i++) {
                    names[i] = drivers.get(i).fullName;
                }

                new AlertDialog.Builder(OrderDetailsActivity.this)
                        .setTitle("Pick a driver")
                        .setItems(names, (dialog, which) -> {
                            long driverId = drivers.get(which).userId;
                            ApiClient.getApi(OrderDetailsActivity.this).dispatchOrder(orderId, driverId)
                                    .enqueue(updateCallback("Order dispatched"));
                        })
                        .show();
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(OrderDetailsActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }

    // used by approve, dispatch, edit and cancel
    private Callback<PurchaseOrder> updateCallback(String successMessage) {
        return new Callback<PurchaseOrder>() {
            @Override
            public void onResponse(Call<PurchaseOrder> call, Response<PurchaseOrder> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(OrderDetailsActivity.this, successMessage, Toast.LENGTH_SHORT).show();
                    loadOrder();
                } else {
                    Toast.makeText(OrderDetailsActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PurchaseOrder> call, Throwable t) {
                Toast.makeText(OrderDetailsActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        };
    }
}
