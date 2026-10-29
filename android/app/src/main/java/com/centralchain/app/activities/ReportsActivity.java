package com.centralchain.app.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.centralchain.app.R;
import com.centralchain.app.models.ReportRow;
import com.centralchain.app.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// supplier reports for their own items
public class ReportsActivity extends AppCompatActivity {

    private LinearLayout listTopItems, listWarehouses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        TextView title = findViewById(R.id.screen_title);
        title.setText("Reports");

        listTopItems = findViewById(R.id.list_top_items);
        listWarehouses = findViewById(R.id.list_warehouses);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        loadTopItems();
        loadWarehouses();
    }

    private void loadTopItems() {
        ApiClient.getApi(this).getTopItems().enqueue(new Callback<List<ReportRow>>() {
            @Override
            public void onResponse(Call<List<ReportRow>> call, Response<List<ReportRow>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ReportsActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                    return;
                }
                listTopItems.removeAllViews();
                for (ReportRow row : response.body()) {
                    addRow(listTopItems, row.itemName, String.valueOf(row.totalOrdered));
                }
                if (response.body().isEmpty()) {
                    addRow(listTopItems, "No orders yet", "");
                }
            }

            @Override
            public void onFailure(Call<List<ReportRow>> call, Throwable t) {
                Toast.makeText(ReportsActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadWarehouses() {
        ApiClient.getApi(this).getStockByWarehouse().enqueue(new Callback<List<ReportRow>>() {
            @Override
            public void onResponse(Call<List<ReportRow>> call, Response<List<ReportRow>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ReportsActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                    return;
                }
                listWarehouses.removeAllViews();
                for (ReportRow row : response.body()) {
                    addRow(listWarehouses, row.warehouseLocation, String.valueOf(row.totalStock));
                }
                if (response.body().isEmpty()) {
                    addRow(listWarehouses, "No items yet", "");
                }
            }

            @Override
            public void onFailure(Call<List<ReportRow>> call, Throwable t) {
                Toast.makeText(ReportsActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }

    // adds one label and value row to a list
    private void addRow(LinearLayout list, String label, String value) {
        View row = LayoutInflater.from(this).inflate(R.layout.view_detail_row, list, false);
        TextView textLabel = row.findViewById(R.id.row_label);
        TextView textValue = row.findViewById(R.id.row_value);
        textLabel.setText(label);
        textValue.setText(value);
        list.addView(row);
    }
}
