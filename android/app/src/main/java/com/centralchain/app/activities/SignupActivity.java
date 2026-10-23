package com.centralchain.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.centralchain.app.R;
import com.centralchain.app.models.RegisterRequest;
import com.centralchain.app.models.Role;
import com.centralchain.app.network.ApiClient;
import com.centralchain.app.utils.Validation;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// sign up page, you pick your role here
public class SignupActivity extends AppCompatActivity {

    // admin is left out so people cant make themselves admin
    private static final String[] ROLES = {Role.BUYER, Role.SUPPLIER, Role.DRIVER};

    private Spinner spinnerRole;
    private EditText inputName, inputEmail, inputPassword, inputConfirm;
    private MaterialButton btnCreate;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        TextView title = findViewById(R.id.screen_title);
        title.setText("Sign up");

        // role names in the spinner
        spinnerRole = findViewById(R.id.spinner_role);
        String[] labels = new String[ROLES.length];
        for (int i = 0; i < ROLES.length; i++) {
            labels[i] = Role.getLabel(ROLES[i]);
        }
        spinnerRole.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels));

        inputName = findViewById(R.id.input_name);
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        inputConfirm = findViewById(R.id.input_confirm);
        btnCreate = findViewById(R.id.btn_create);
        progress = findViewById(R.id.progress);
        MaterialButton btnBack = findViewById(R.id.btn_back);

        btnCreate.setOnClickListener(v -> signup());
        btnBack.setOnClickListener(v -> finish());
        findViewById(R.id.btn_go_login).setOnClickListener(v -> finish());
    }

    private void signup() {
        String name = inputName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString();
        String confirm = inputConfirm.getText().toString();
        String role = ROLES[spinnerRole.getSelectedItemPosition()];

        // check the fields
        boolean valid = true;
        if (name.isEmpty()) {
            inputName.setError("Full name is required");
            valid = false;
        }
        if (!Validation.isValidEmail(email)) {
            inputEmail.setError("Enter a valid email address");
            valid = false;
        }
        if (!Validation.isValidPassword(password)) {
            inputPassword.setError("Password must be at least 6 characters");
            valid = false;
        }
        if (!confirm.equals(password)) {
            inputConfirm.setError("Passwords do not match");
            valid = false;
        }
        if (!valid) {
            return;
        }

        progress.setVisibility(View.VISIBLE);
        btnCreate.setEnabled(false);

        RegisterRequest request = new RegisterRequest(name, email, password, role);
        ApiClient.getApi(this).register(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progress.setVisibility(View.GONE);
                btnCreate.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(SignupActivity.this, "Account created, you can log in now", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(SignupActivity.this, ApiClient.getErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progress.setVisibility(View.GONE);
                btnCreate.setEnabled(true);
                Toast.makeText(SignupActivity.this, "Could not connect to the server", Toast.LENGTH_LONG).show();
            }
        });
    }
}
