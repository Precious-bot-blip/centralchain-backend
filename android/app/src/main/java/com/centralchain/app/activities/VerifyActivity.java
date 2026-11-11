package com.centralchain.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.centralchain.app.R;
import com.centralchain.app.models.VerifyRequest;
import com.centralchain.app.network.ApiClient;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// enter the code after signing up
public class VerifyActivity extends AppCompatActivity {

    private String email;
    private EditText inputCode;
    private MaterialButton btnVerify;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        TextView title = findViewById(R.id.screen_title);
        title.setText("Verify email");

        email = getIntent().getStringExtra("email");
        TextView info = findViewById(R.id.text_info);
        info.setText("Enter the code sent to " + email);

        // show the code for now since no email gets sent
        String code = getIntent().getStringExtra("code");
        if (code != null) {
            TextView textCode = findViewById(R.id.text_code);
            textCode.setText("Your code: " + code);
            textCode.setVisibility(View.VISIBLE);
        }

        inputCode = findViewById(R.id.input_code);
        btnVerify = findViewById(R.id.btn_verify);
        progress = findViewById(R.id.progress);

        btnVerify.setOnClickListener(v -> verify());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void verify() {
        String code = inputCode.getText().toString().trim();
        if (!code.matches("\\d{6}")) {
            inputCode.setError("Enter the 6 digit code");
            return;
        }

        progress.setVisibility(View.VISIBLE);
        btnVerify.setEnabled(false);

        ApiClient.getApi(this).verify(new VerifyRequest(email, code)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progress.setVisibility(View.GONE);
                btnVerify.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(VerifyActivity.this, "Email verified, you can log in now", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(VerifyActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progress.setVisibility(View.GONE);
                btnVerify.setEnabled(true);
                Toast.makeText(VerifyActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }
}
